// src/main/java/com/developersbeeh/medcontrol/MainActivity.kt
package com.developersbeeh.medcontrol

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope // ✅ ADIÇÃO: Importação necessária
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.ActivityMainBinding
import com.developersbeeh.medcontrol.databinding.NavHeaderBinding
import com.developersbeeh.medcontrol.ui.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // ✅ ADIÇÃO: Importação necessária
import kotlinx.coroutines.launch // ✅ ADIÇÃO: Importação necessária

private const val TAG = "MainActivityUpdate"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPreferences: UserPreferences
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navHeaderBinding: NavHeaderBinding

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val updateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                findViewById(android.R.id.content),
                "Nova versão pronta para instalar.",
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("REINICIAR") { appUpdateManager.completeUpdate() }
                show()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                showPermissionDeniedDialog()
            }
        }

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            runOnUiThread { binding.textViewOfflineStatus.visibility = View.GONE }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            runOnUiThread { binding.textViewOfflineStatus.visibility = View.VISIBLE }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)
        applySelectedTheme()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headerView = binding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setupNavigation()
        observeViewModel()

        // ✅ ADIÇÃO: Chamada para iniciar o observador de status premium
        observePremiumStatus()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateDrawerMenuAndToolbar(destination.id)
        }

        checkAndRequestNotificationPermission()
        checkForAppUpdate()
        appUpdateManager.registerListener(updateListener)
    }

    override fun onResume() {
        super.onResume()


        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Nova versão pronta para instalar.",
                    Snackbar.LENGTH_INDEFINITE
                ).apply {
                    setAction("REINICIAR") { appUpdateManager.completeUpdate() }
                    show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(updateListener)
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Network callback já foi removido ou nunca registrado.")
        }
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.caregiverDashboardFragment, R.id.dashboardDependenteFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        setupDrawerMenu()
    }

    private fun observeViewModel() {
        mainViewModel.logoutEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        mainViewModel.userProfile.observe(this) { user ->
            setupNavHeader(user)
        }
    }

    // ✅ ADIÇÃO: Nova função que configura o observador para o status premium
    private fun observePremiumStatus() {
        lifecycleScope.launch {
            mainViewModel.listenToPremiumStatus()?.collectLatest { isPremium ->
                // Este bloco será executado sempre que o status premium mudar no Firestore
                val currentStatusInPrefs = userPreferences.isPremium()
                if (isPremium != currentStatusInPrefs) {
                    Log.d("MainActivity", "Status premium mudou para: $isPremium. Atualizando UserPreferences.")
                    userPreferences.saveIsPremium(isPremium)

                    // Opcional: Reconfigura o menu para refletir a mudança imediatamente
                    configureDrawerMenuForCaregiver()
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    this,
                    101 // Request code for update
                )
            }
        }
    }

    private fun applySelectedTheme() {
        val theme = userPreferences.getTheme()
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupNavHeader(user: com.developersbeeh.medcontrol.data.model.Usuario?) {
        val isCaregiver = userPreferences.getIsCaregiver()

        if (isCaregiver) {
            if (user != null) {
                navHeaderBinding.textViewUserName.text = user.name
                navHeaderBinding.textViewUserEmail.text = user.email
                navHeaderBinding.textViewPremiumBadge.visibility = if (user.premium) View.VISIBLE else View.GONE
                navHeaderBinding.imageViewUser.load(user.photoUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_logo)
                    transformations(CircleCropTransformation())
                }
            } else { // Fallback
                navHeaderBinding.textViewUserName.text = userPreferences.getUserName()
                navHeaderBinding.textViewUserEmail.text = userPreferences.getUserEmail()
                navHeaderBinding.imageViewUser.setImageResource(R.drawable.ic_logo)
                navHeaderBinding.textViewPremiumBadge.visibility = if (userPreferences.isPremium()) View.VISIBLE else View.GONE
            }
        } else {
            navHeaderBinding.textViewUserName.text = userPreferences.getSelectedDependentName()
            navHeaderBinding.textViewUserEmail.text = "Modo Dependente"
            navHeaderBinding.imageViewUser.setImageResource(R.drawable.ic_person)
            navHeaderBinding.textViewPremiumBadge.visibility = View.GONE
        }
    }

    private fun updateDrawerMenuAndToolbar(currentDestinationId: Int) {
        val isCaregiver = userPreferences.getIsCaregiver()
        if(isCaregiver){
            mainViewModel.startListeningToUserProfile()
        }

        val isFullScreen = currentDestinationId in listOf(
            R.id.splashFragment, R.id.loginFragment, R.id.registerFragment,
            R.id.roleSelectionFragment, R.id.linkDependentFragment, R.id.onboardingFragment
        )

        if (isFullScreen) {
            binding.toolbar.visibility = View.GONE
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        } else {
            binding.toolbar.visibility = View.VISIBLE
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            if (isCaregiver) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                configureDrawerMenuForCaregiver()
            } else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    private fun configureDrawerMenuForCaregiver() {
        val menu = binding.navView.menu
        val isPremium = mainViewModel.userProfile.value?.premium ?: userPreferences.isPremium()

        menu.findItem(R.id.nav_my_invites).isVisible = true
        menu.findItem(R.id.nav_profile).isVisible = true
        menu.findItem(R.id.nav_settings).isVisible = true
        menu.findItem(R.id.nav_logout).isVisible = true
        menu.findItem(R.id.nav_premium).isVisible = !isPremium
    }

    private fun setupDrawerMenu() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_my_invites -> navController.navigate(R.id.receivedInvitesFragment)
                R.id.nav_profile -> navController.navigate(R.id.profileFragment)
                R.id.nav_settings -> navController.navigate(R.id.settingsFragment)
                R.id.nav_premium -> navController.navigate(R.id.premiumPlansFragment)
                R.id.nav_logout -> mainViewModel.onLogoutRequest()
                else -> {
                    val dependentId = userPreferences.getDependentId()
                    if (dependentId != null) {
                        // Navega para o destino correspondente, se houver
                    } else {
                        Snackbar.make(binding.root, "Selecione um dependente no painel para continuar.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                showPermissionExplanationDialog()
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        MaterialAlertDialogBuilder(this, R.style.AppTheme_DialogAnimation)
            .setTitle("Permissão de Notificação")
            .setMessage("O NidusCare precisa da sua permissão para enviar notificações. Sem ela, os lembretes de medicamentos e alarmes não funcionarão. Por favor, ative as notificações para garantir que você não perca nenhuma dose.")
            .setPositiveButton("Ativar Agora") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Agora Não", null)
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this, R.style.AppTheme_DialogAnimation)
            .setTitle("Permissão Negada")
            .setMessage("Você negou a permissão de notificações. Para que os lembretes funcionem, você precisará ativá-la manualmente nas configurações do aplicativo. Deseja ir para as configurações agora?")
            .setPositiveButton("Ir para Configurações") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .create()
            .show()
    }
}