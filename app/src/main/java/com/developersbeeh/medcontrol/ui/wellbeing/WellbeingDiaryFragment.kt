package com.developersbeeh.medcontrol.ui.wellbeing

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.BuildConfig
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.MealAnalysisResult
import com.developersbeeh.medcontrol.data.model.QualidadeSono
import com.developersbeeh.medcontrol.data.model.TipoRefeicao
import com.developersbeeh.medcontrol.databinding.*
import com.developersbeeh.medcontrol.ui.common.LoadingDialogFragment
// ✅ IMPORT CORRETO
import com.developersbeeh.medcontrol.ui.sleep.getDisplayName
// ✅ IMPORT CORRETO
import com.developersbeeh.medcontrol.ui.meals.getDisplayName
import com.developersbeeh.medcontrol.ui.wellbeing.timer.ActivityTimerService
import com.developersbeeh.medcontrol.ui.wellbeing.timer.TimerServiceManager
import com.developersbeeh.medcontrol.ui.wellbeing.timer.TimerState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class WellbeingDiaryFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences
    private var _binding: FragmentWellbeingDiaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BemEstarViewModel by viewModels()
    private val args: WellbeingDiaryFragmentArgs by navArgs()

    private var tempImageUri: Uri? = null
    private var mealDialog: AlertDialog? = null
    private var sleepDialog: AlertDialog? = null

    private var aiAnalysisResult: MealAnalysisResult? = null
    private val locale = Locale("pt", "BR")

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let {
                showAddMealDialog(isAnalyzing = true)
                viewModel.analyzeMealPhoto(it)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            showAddMealDialog(isAnalyzing = true)
            viewModel.analyzeMealPhoto(it)
        }
    }

    private val timerStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTIVITY_TIMER_STOP_ACTION") {
                val elapsedTimeMs = intent.getLongExtra("ELAPSED_TIME_MS", 0)
                val activityType = intent.getStringExtra("ACTIVITY_TYPE") ?: getString(R.string.timer_default_activity_type)
                val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMs).toInt()

                if (durationMinutes > 0) {
                    viewModel.addAtividadeFisica(activityType, durationMinutes)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWellbeingDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initialize(args.dependentId)
        userPreferences = UserPreferences(requireContext())
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.wellbeing_diary_title)

        val dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_header_format), locale)
        binding.textViewDate.text = LocalDate.now().format(dateFormatter).replaceFirstChar { it.uppercase() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(timerStopReceiver, IntentFilter("ACTIVITY_TIMER_STOP_ACTION"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().registerReceiver(timerStopReceiver, IntentFilter("ACTIVITY_TIMER_STOP_ACTION"))
        }

        setupListeners()
        observeViewModel()
        observeTimerState()
        setupAds()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(timerStopReceiver)
        _binding = null
    }

    private fun setupAds() {
        MobileAds.initialize(requireContext()) {}
        if (!userPreferences.isPremium()) {
            binding.adView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        } else {
            binding.adView.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        binding.cardWeight.root.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToWeightTrackerFragment(args.dependentId)
            findNavController().navigate(action)
        }
        binding.cardHydration.root.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToHydrationTrackerFragment(args.dependentId)
            findNavController().navigate(action)
        }
        binding.cardActivity.root.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToPhysicalActivityTrackerFragment(args.dependentId)
            findNavController().navigate(action)
        }
        binding.cardMeal.root.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToMealTrackerFragment(args.dependentId)
            findNavController().navigate(action)
        }
        binding.cardSleep.root.setOnClickListener {
            val action = NavGraphDirections.actionGlobalToSleepTrackerFragment(args.dependentId)
            findNavController().navigate(action)
        }

        binding.cardWeight.buttonUpdateWeight.setOnClickListener { showUpdateWeightDialog() }
        binding.cardHydration.buttonAdd200ml.setOnClickListener { viewModel.addWaterIntake(200) }
        binding.cardHydration.buttonAdd500ml.setOnClickListener { viewModel.addWaterIntake(500) }
        binding.cardActivity.buttonStartActivity.setOnClickListener { showActivityDialog(isStartingTimer = true) }
        binding.cardActivity.buttonAddActivityManually.setOnClickListener { showActivityDialog(isStartingTimer = false) }
        binding.cardActivity.buttonPauseTimer.setOnClickListener { sendCommandToService(ActivityTimerService.ACTION_PAUSE) }
        binding.cardActivity.buttonStopTimer.setOnClickListener { sendCommandToService(ActivityTimerService.ACTION_STOP) }
        binding.cardMeal.buttonAddMeal.setOnClickListener { showAddMealDialog() }
        binding.cardMeal.buttonAnalyzeMeal.setOnClickListener { handleMealAnalysisClick() }
        binding.cardSleep.buttonAddEditSleep.setOnClickListener { showAddSleepDialog() }

        binding.cardHydration.buttonAdd200ml.text = getString(R.string.hydration_add_200ml)
        binding.cardHydration.buttonAdd500ml.text = getString(R.string.hydration_add_500ml)
        binding.cardActivity.buttonStartActivity.text = getString(R.string.activity_start_timer)
        binding.cardActivity.buttonAddActivityManually.text = getString(R.string.activity_add_manually)
        binding.cardActivity.buttonStopTimer.text = getString(R.string.activity_timer_stop)
        binding.cardMeal.buttonAddMeal.text = getString(R.string.meal_add)
        binding.cardMeal.buttonAnalyzeMeal.text = getString(R.string.meal_analyze)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val dependent = state.dependente ?: return@observe
            val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in)

            updateWeightCard(state)

            if (!binding.cardHydration.root.isVisible) binding.cardHydration.root.startAnimation(animation)
            binding.cardHydration.root.isVisible = true
            animateTextViewUpdate(binding.cardHydration.textViewConsumed, "${state.hidratacaoTotalMl}")
            binding.cardHydration.progressBarWater.setProgressCompat(state.hidratacaoPorcentagem, true)
            binding.cardHydration.textViewGoal.text = getString(R.string.water_goal_format, state.hidratacaoMetaMl)
            binding.cardHydration.labelHydration.text = getString(R.string.category_hydration)

            if (!binding.cardActivity.root.isVisible) binding.cardActivity.root.startAnimation(animation)
            binding.cardActivity.root.isVisible = true
            binding.cardActivity.progressBarActivity.setProgressCompat(state.atividadePorcentagem, true)
            animateTextViewUpdate(binding.cardActivity.textViewMinutes, "${state.atividadeTotalMin}")
            binding.cardActivity.textViewActivityGoal.text = getString(R.string.activity_goal_format, state.atividadeMetaMin)
            binding.cardActivity.labelActivity.text = getString(R.string.category_activity)

            if (!binding.cardMeal.root.isVisible) binding.cardMeal.root.startAnimation(animation)
            binding.cardMeal.root.isVisible = true
            binding.cardMeal.progressCalories.setProgressCompat(state.caloriasPorcentagem, true)
            animateTextViewUpdate(binding.cardMeal.textViewCaloriesProgress, getString(R.string.calories_progress_format, state.caloriasTotal, state.caloriasMeta))
            binding.cardMeal.labelMeal.text = getString(R.string.category_meal)

            if (!binding.cardSleep.root.isVisible) binding.cardSleep.root.startAnimation(animation)
            binding.cardSleep.root.isVisible = true
            val registroSono = state.registroSono

            // ✅ LÓGICA DE SONO ATUALIZADA
            // Verifica se o registro de sono é válido PARA HOJE (ou seja, se a pessoa acordou hoje)
            val registroDeHoje = registroSono?.takeIf {
                it.getDataAsLocalDate().isEqual(LocalDate.now())
            }

            if (registroDeHoje != null) {
                animateTextViewUpdate(binding.cardSleep.textViewSleepDuration, "${state.sonoTotalHoras}h ${state.sonoTotalMinutos}m")
                val qualidade = (try { QualidadeSono.valueOf(registroDeHoje.qualidade) } catch (e: Exception) { QualidadeSono.RAZOAVEL }).getDisplayName(requireContext())
                animateTextViewUpdate(binding.cardSleep.textViewSleepQuality, getString(R.string.sleep_quality_format, qualidade))
                binding.cardSleep.buttonAddEditSleep.text = getString(R.string.sleep_button_edit)
            } else {
                // Se não há registro HOJE, mostra o estado de "Registrar"
                binding.cardSleep.textViewSleepDuration.text = getString(R.string.sleep_duration_empty)
                binding.cardSleep.textViewSleepQuality.text = getString(R.string.sleep_no_record_today)
                binding.cardSleep.buttonAddEditSleep.text = getString(R.string.sleep_button_register)

                // Limpa o registro "antigo" da UI se ele ainda estiver lá
                if (state.registroSono != null) {
                    viewModel.clearCurrentSleepRecordForNewEntry()
                }
            }
            binding.cardSleep.labelSleep.text = getString(R.string.category_sleep)
        }

        viewModel.actionFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        observeSleepSuggestion()
    }

    private fun observeSleepSuggestion() {
        viewModel.sleepSuggestionEvent.observe(viewLifecycleOwner) { event ->
            if (sleepDialog != null && sleepDialog!!.isShowing) {
                event.getContentIfNotHandled()?.let { (suggestedBedtime, suggestedWaketime) ->
                    // ✅ CORREÇÃO: Bloco try-catch envolvendo a busca e o bind
                    try {
                        val rootView = sleepDialog!!.findViewById<View>(R.id.dialog_add_sleep_root)
                        if (rootView != null) {
                            val dialogBinding = DialogAddSleepBinding.bind(rootView)
                            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                            dialogBinding.editTextBedtime.setText(suggestedBedtime.format(timeFormatter))
                            dialogBinding.editTextWakeUp.setText(suggestedWaketime.format(timeFormatter))
                            Toast.makeText(context, getString(R.string.feedback_sleep_suggestion_applied), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("WellbeingDiaryFragment", "Erro ao tentar atualizar o diálogo de sono", e)
                    }
                }
            }
        }
    }


    private fun updateWeightCard(state: BemEstarUiState) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_fade_in)
        val dependent = state.dependente ?: return
        val card = binding.cardWeight.root

        if (dependent.peso.isNotBlank()) {
            if (!card.isVisible) {
                card.startAnimation(animation)
                card.isVisible = true
            }
            binding.cardWeight.textViewCurrentWeight.text = dependent.peso.replace(',', '.')
        } else {
            card.isVisible = false
            return
        }

        binding.cardWeight.labelWeight.text = getString(R.string.category_weight)
        binding.cardWeight.textViewWeightUnit.text = getString(R.string.unit_kg)
        binding.cardWeight.buttonUpdateWeight.text = getString(R.string.update)

        binding.cardWeight.layoutImc.isVisible = state.imcResult != null
        state.imcResult?.let {
            binding.cardWeight.textViewImcValue.text = String.format(locale, "%.1f", it.value)
            binding.cardWeight.textViewImcClassification.text = it.classification
            binding.cardWeight.textViewImcClassification.setTextColor(ContextCompat.getColor(requireContext(), it.color))
        }

        binding.cardWeight.goalContainer.isVisible = true
        binding.cardWeight.layoutWeightGoal.isVisible = state.pesoMetaStatus != null
        binding.cardWeight.buttonSetGoal.isVisible = state.pesoMetaStatus == null

        state.pesoMetaStatus?.let {
            binding.cardWeight.progressWeightGoal.progress = it.progress
            binding.cardWeight.textViewWeightGoalProgress.text = it.progressText
            binding.cardWeight.progressWeightGoal.setIndicatorColor(ContextCompat.getColor(requireContext(), it.color))
        } ?: binding.cardWeight.buttonSetGoal.setOnClickListener {
            findNavController().navigate(NavGraphDirections.actionGlobalToHealthGoalsFragment(args.dependentId))
        }

        binding.cardWeight.buttonSetGoal.text = getString(R.string.weight_goal_not_set)
    }

    private fun observeTimerState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                TimerServiceManager.timerState.collectLatest { state ->
                    updateActivityCardUI(state)
                }
            }
        }
    }

    private fun animateTextViewUpdate(textView: TextView, newText: String) {
        if (textView.text == newText || !textView.isShown) {
            textView.text = newText
            return
        }
        val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                textView.text = newText
                textView.startAnimation(fadeIn)
            }
        })
        textView.startAnimation(fadeOut)
    }

    private fun updateActivityCardUI(state: TimerState) {
        if (_binding == null) return

        val cardActivityBinding = binding.cardActivity
        val timeInMillis = when (state) {
            is TimerState.Running -> state.elapsedTime
            is TimerState.Paused -> state.elapsedTime
            is TimerState.Idle -> 0L
        }

        cardActivityBinding.layoutStartActivity.isVisible = state is TimerState.Idle
        cardActivityBinding.layoutTimerRunning.isVisible = state is TimerState.Running || state is TimerState.Paused

        if (state is TimerState.Idle) {
            val totalMinutos = viewModel.uiState.value?.atividadeTotalMin ?: 0
            cardActivityBinding.textViewMinutes.text = totalMinutos.toString()
        } else {
            val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
            cardActivityBinding.textViewTimer.text = if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

        when(state) {
            is TimerState.Running -> {
                cardActivityBinding.buttonPauseTimer.text = getString(R.string.activity_timer_pause)
                cardActivityBinding.buttonPauseTimer.setIconResource(R.drawable.ic_timer_pause)
                cardActivityBinding.buttonPauseTimer.setOnClickListener { sendCommandToService(ActivityTimerService.ACTION_PAUSE) }
            }
            is TimerState.Paused -> {
                cardActivityBinding.buttonPauseTimer.text = getString(R.string.activity_timer_resume)
                cardActivityBinding.buttonPauseTimer.setIconResource(R.drawable.ic_play_arrow)
                cardActivityBinding.buttonPauseTimer.setOnClickListener { sendCommandToService(ActivityTimerService.ACTION_START_RESUME) }
            }
            is TimerState.Idle -> {}
        }
    }

    private fun showUpdateWeightDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_single_input, null)
        val textInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textInputLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.editTextSingleInput)
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        textInputLayout.hint = getString(R.string.dialog_hint_new_weight_kg)
        editText.setText(viewModel.uiState.value?.dependente?.peso ?: "")
        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dialog_title_update_weight))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setPositiveButton(getString(R.string.dialog_button_save)) { _, _ ->
                val newWeight = editText.text.toString().trim()
                if (newWeight.isNotEmpty()) {
                    viewModel.updateWeight(newWeight)
                }
            }
            .show()
    }

    private fun showActivityDialog(isStartingTimer: Boolean) {
        val dialogBinding = DialogAddActivityBinding.inflate(LayoutInflater.from(context))
        val dialogTitle = if (isStartingTimer) getString(R.string.dialog_title_start_activity_timer) else getString(R.string.dialog_title_log_activity_manual)
        val positiveButtonText = if (isStartingTimer) getString(R.string.dialog_button_start) else getString(R.string.dialog_button_save)

        dialogBinding.tilDuration.isVisible = !isStartingTimer
        dialogBinding.tilActivityType.hint = getString(R.string.dialog_hint_activity_type)
        dialogBinding.tilDuration.hint = getString(R.string.dialog_hint_activity_duration)

        val activities = resources.getStringArray(R.array.activity_suggestions)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, activities)
        dialogBinding.autoCompleteActivityType.setAdapter(adapter)

        MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(dialogTitle)
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setPositiveButton(positiveButtonText) { _, _ ->
                val tipo = dialogBinding.autoCompleteActivityType.text.toString()
                if (tipo.isBlank()) {
                    Toast.makeText(context, getString(R.string.error_activity_type_required), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (isStartingTimer) {
                    sendCommandToService(ActivityTimerService.ACTION_START_RESUME, tipo)
                } else {
                    val duracao = dialogBinding.editTextDuration.text.toString().toIntOrNull() ?: 0
                    if (duracao <= 0) {
                        Toast.makeText(context, getString(R.string.error_activity_duration_required), Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    viewModel.addAtividadeFisica(tipo, duracao)
                }
            }
            .show()
    }

    private fun handleMealAnalysisClick() {
        val isPremium = UserPreferences(requireContext()).isPremium()
        if (isPremium) {
            showImageSourceDialog()
        } else {
            findNavController().navigate(R.id.action_global_to_premiumPlansFragment)
            Toast.makeText(context, getString(R.string.error_premium_feature_meal_analysis), Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(getString(R.string.image_source_camera), getString(R.string.image_source_gallery))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.image_source_dialog_title))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Câmera
                        try {
                            tempImageUri = getTmpFileUri()
                            takePictureLauncher.launch(tempImageUri)
                        } catch (ex: IOException) {
                            Toast.makeText(requireContext(), getString(R.string.error_create_image_file_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> { // Galeria
                        pickImageLauncher.launch("image/*")
                    }
                }
            }
            .show()
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("meal_photo_", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun showAddMealDialog(isAnalyzing: Boolean = false) {
        if (mealDialog != null && mealDialog!!.isShowing) {
            return
        }

        val dialogBinding = DialogAddMealBinding.inflate(LayoutInflater.from(context))
        aiAnalysisResult = null

        dialogBinding.radioCafeDaManha.text = getString(R.string.meal_type_breakfast)
        dialogBinding.radioAlmoco.text = getString(R.string.meal_type_lunch)
        dialogBinding.radioJantar.text = getString(R.string.meal_type_dinner)
        dialogBinding.radioLanche.text = getString(R.string.meal_type_snack)

        dialogBinding.buttonAnalyzeWithCamera.text = getString(R.string.meal_dialog_analyze_with_camera)

        dialogBinding.labelBenefits.text = getString(R.string.meal_dialog_benefits_title)
        dialogBinding.labelTips.text = getString(R.string.meal_dialog_nidus_tip_title)
        dialogBinding.labelMealType.text = getString(R.string.meal_dialog_meal_type_title)

        dialogBinding.tilDescription.hint = getString(R.string.meal_dialog_hint_description)
        dialogBinding.tilCalories.hint = getString(R.string.meal_dialog_hint_calories)
        dialogBinding.tilCalories.suffixText = getString(R.string.meal_dialog_suffix_kcal)

        mealDialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(getString(R.string.dialog_add_meal_title))
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.dialog_button_cancel)) { _, _ -> viewModel.resetMealAnalysisState() }
            .setPositiveButton(getString(R.string.dialog_button_save)) { _, _ ->
                val selectedId = dialogBinding.radioGroupMealType.checkedRadioButtonId
                val tipo = when (selectedId) {
                    R.id.radioCafeDaManha -> TipoRefeicao.CAFE_DA_MANHA
                    R.id.radioAlmoco -> TipoRefeicao.ALMOCO
                    R.id.radioJantar -> TipoRefeicao.JANTAR
                    else -> TipoRefeicao.LANCHE
                }
                val descricao = dialogBinding.editTextDescription.text.toString()
                val calorias = dialogBinding.editTextCalories.text.toString().toIntOrNull()

                // ✅ CORREÇÃO: Passa o aiAnalysisResult (que pode ser null) para o ViewModel
                viewModel.addRefeicao(tipo, descricao, calorias, aiAnalysisResult)
            }
            .create()

        mealDialog?.setOnShowListener {
            val positiveButton = mealDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)

            viewModel.mealAnalysisState.observe(viewLifecycleOwner) { state ->
                dialogBinding.progressBarAnalysis.isVisible = state is MealAnalysisState.Loading
                dialogBinding.buttonAnalyzeWithCamera.isEnabled = state !is MealAnalysisState.Loading
                positiveButton.isEnabled = state !is MealAnalysisState.Loading

                if(state is MealAnalysisState.Loading) {
                    dialogBinding.buttonAnalyzeWithCamera.text = getString(R.string.meal_dialog_analyzing)
                } else {
                    dialogBinding.buttonAnalyzeWithCamera.text = getString(R.string.meal_dialog_analyze_with_camera)
                }

                when (state) {
                    is MealAnalysisState.Success -> {
                        aiAnalysisResult = state.result
                        populateFieldsFromAI(dialogBinding, state.result)
                        viewModel.resetMealAnalysisState()
                    }
                    is MealAnalysisState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetMealAnalysisState()
                        mealDialog?.dismiss()
                    }
                    else -> {}
                }
            }

            if (isAnalyzing) {
                // Dispara a observação; o ViewModel já deve estar em estado de Loading
                viewModel.mealAnalysisState.value?.let { /* O observador irá capturar */ }
            }
        }

        dialogBinding.buttonAnalyzeWithCamera.setOnClickListener {
            handleMealAnalysisClick()
            mealDialog?.dismiss()
        }
        mealDialog?.setOnDismissListener {
            viewModel.mealAnalysisState.removeObservers(viewLifecycleOwner)
            viewModel.resetMealAnalysisState()
            aiAnalysisResult = null
            mealDialog = null
        }
        mealDialog?.show()
    }

    private fun populateFieldsFromAI(dialogBinding: DialogAddMealBinding, result: MealAnalysisResult) {
        dialogBinding.layoutAiResults.isVisible = true
        dialogBinding.dividerAi.isVisible = true
        dialogBinding.editTextDescription.setText(result.descricao)
        if (result.calorias > 0) {
            dialogBinding.editTextCalories.setText(result.calorias.toString())
        }
        dialogBinding.textViewBenefits.text = result.beneficios
        dialogBinding.textViewTips.text = result.dicas

        val radioId = when (result.tipoRefeicao) {
            TipoRefeicao.CAFE_DA_MANHA.name -> R.id.radioCafeDaManha
            TipoRefeicao.ALMOCO.name -> R.id.radioAlmoco
            TipoRefeicao.JANTAR.name -> R.id.radioJantar
            else -> R.id.radioLanche
        }
        dialogBinding.radioGroupMealType.check(radioId)
    }

    private fun showAddSleepDialog() {
        if (sleepDialog != null && sleepDialog!!.isShowing) return

        val dialogBinding = DialogAddSleepBinding.inflate(layoutInflater)

        // ✅ CORREÇÃO: Pega o registro da UI (que pode ser nulo se já for de ontem)
        val existingRecord = viewModel.uiState.value?.registroSono
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        var bedtime = existingRecord?.getHoraDeDormirAsLocalTime() ?: LocalTime.of(22, 30)
        var wakeTime = existingRecord?.getHoraDeAcordarAsLocalTime() ?: LocalTime.of(7, 0)
        var interruptions = existingRecord?.interrupcoes ?: 0

        dialogBinding.editTextBedtime.setText(bedtime.format(timeFormatter))
        dialogBinding.editTextWakeUp.setText(wakeTime.format(timeFormatter))
        dialogBinding.textViewInterruptionsCount.text = interruptions.toString()
        dialogBinding.editTextNotes.setText(existingRecord?.notas)

        dialogBinding.chipQualityGood.text = getString(R.string.sleep_quality_good)
        dialogBinding.chipQualityOk.text = getString(R.string.sleep_quality_reasonable)
        dialogBinding.chipQualityBad.text = getString(R.string.sleep_quality_bad)
        dialogBinding.tilBedtime.hint = getString(R.string.sleep_bedtime_label)
        dialogBinding.tilWakeUp.hint = getString(R.string.sleep_wake_time_label)
        dialogBinding.buttonSuggestTime.text = getString(R.string.sleep_suggest_time_button)
        dialogBinding.textViewInterruptionsLabel.text = getString(R.string.sleep_interruptions_label)
        dialogBinding.tilNotes.hint = getString(R.string.sleep_notes_hint)

        val initialQuality = try {
            existingRecord?.let { QualidadeSono.valueOf(it.qualidade) } ?: QualidadeSono.RAZOAVEL
        } catch (e: Exception) { QualidadeSono.RAZOAVEL }

        when (initialQuality) {
            QualidadeSono.BOM -> dialogBinding.chipGroupSleepQuality.check(R.id.chipQualityGood)
            QualidadeSono.RAZOAVEL -> dialogBinding.chipGroupSleepQuality.check(R.id.chipQualityOk)
            QualidadeSono.RUIM -> dialogBinding.chipGroupSleepQuality.check(R.id.chipQualityBad)
        }

        dialogBinding.editTextBedtime.setOnClickListener {
            showTimePicker(getString(R.string.sleep_bedtime_label), bedtime) { time ->
                bedtime = time
                dialogBinding.editTextBedtime.setText(time.format(timeFormatter))
            }
        }
        dialogBinding.editTextWakeUp.setOnClickListener {
            showTimePicker(getString(R.string.sleep_wake_time_label), wakeTime) { time ->
                wakeTime = time
                dialogBinding.editTextWakeUp.setText(time.format(timeFormatter))
            }
        }

        dialogBinding.buttonSuggestTime.setOnClickListener { viewModel.requestSleepTimeSuggestion() }

        dialogBinding.buttonAddInterruption.setOnClickListener {
            interruptions++
            dialogBinding.textViewInterruptionsCount.text = interruptions.toString()
        }
        dialogBinding.buttonRemoveInterruption.setOnClickListener {
            if (interruptions > 0) {
                interruptions--
                dialogBinding.textViewInterruptionsCount.text = interruptions.toString()
            }
        }

        val dialogTitle = if (existingRecord != null) getString(R.string.dialog_add_sleep_title_edit) else getString(R.string.dialog_add_sleep_title_new)

        sleepDialog = MaterialAlertDialogBuilder(requireContext(), R.style.AppTheme_DialogAnimation)
            .setTitle(dialogTitle)
            .setView(dialogBinding.root)
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setPositiveButton(getString(R.string.dialog_button_save)) { _, _ ->
                val qualidade = when (dialogBinding.chipGroupSleepQuality.checkedChipId) {
                    R.id.chipQualityGood -> QualidadeSono.BOM
                    R.id.chipQualityBad -> QualidadeSono.RUIM
                    else -> QualidadeSono.RAZOAVEL
                }
                val notas = dialogBinding.editTextNotes.text.toString().trim().takeIf { it.isNotEmpty() }

                // ✅ CORREÇÃO LÓGICA: Determina a data do registro
                val dataDoRegistro = if (bedtime.isAfter(wakeTime)) LocalDate.now().minusDays(1) else LocalDate.now()

                viewModel.saveSono(dataDoRegistro, bedtime, wakeTime, qualidade, notas, interruptions)
            }
            .setOnDismissListener {
                viewModel.sleepSuggestionEvent.removeObservers(viewLifecycleOwner)
                sleepDialog = null
            }
            .create()

        sleepDialog?.show()
    }

    private fun showTimePicker(title: String, initialTime: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialTime.hour)
            .setMinute(initialTime.minute)
            .setTitleText(title)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            onTimeSelected(LocalTime.of(timePicker.hour, timePicker.minute))
        }
        timePicker.show(parentFragmentManager, "TIME_PICKER_WELLBEING")
    }

    private fun sendCommandToService(action: String, activityType: String? = null) {
        val intent = Intent(requireContext(), ActivityTimerService::class.java).apply {
            this.action = action
            putExtra(ActivityTimerService.EXTRA_DEPENDENT_ID, args.dependentId)
            activityType?.let {
                putExtra(ActivityTimerService.EXTRA_ACTIVITY_TYPE, it)
            }
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }
}