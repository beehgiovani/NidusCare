package com.developersbeeh.medcontrol.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentPrescriptionScannerBinding
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class PrescriptionScannerFragment : Fragment() {

    private var _binding: FragmentPrescriptionScannerBinding? = null
    private val binding get() = _binding!!
    private val args: PrescriptionScannerFragmentArgs by navArgs()
    private lateinit var currentPhotoUri: Uri
    private lateinit var userPreferences: UserPreferences

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            analyzeImage(currentPhotoUri)
        } else {
            Toast.makeText(requireContext(), getString(R.string.image_capture_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            analyzeImage(it)
        } ?: Toast.makeText(requireContext(), getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        setupClickListeners()
        showGuideIfFirstTime()
    }

    private fun setupClickListeners() {
        binding.buttonFromCamera.setOnClickListener {
            checkCameraPermission()
        }
        binding.buttonFromGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun showGuideIfFirstTime() {
        if (!userPreferences.hasSeenScannerGuide()) {
            binding.root.doOnPreDraw {
                val activity = activity ?: return@doOnPreDraw
                TapTargetSequence(activity)
                    .targets(
                        TapTarget.forView(binding.buttonFromCamera, getString(R.string.guide_scanner_camera_title), getString(R.string.guide_scanner_camera_subtitle))
                            .style(),
                        TapTarget.forView(binding.buttonFromGallery, getString(R.string.guide_scanner_gallery_title), getString(R.string.guide_scanner_gallery_subtitle))
                            .style()
                    )
                    .listener(object : TapTargetSequence.Listener {
                        override fun onSequenceFinish() { userPreferences.setScannerGuideSeen(true) }
                        override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                        override fun onSequenceCanceled(lastTarget: TapTarget?) { userPreferences.setScannerGuideSeen(true) }
                    }).start()
            }
        }
    }

    private fun TapTarget.style(): TapTarget {
        return this.outerCircleColor(R.color.md_theme_primary)
            .targetCircleColor(R.color.white)
            .titleTextColor(R.color.white)
            .descriptionTextColor(R.color.white)
            .cancelable(true)
            .tintTarget(false)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(requireContext(), getString(R.string.error_creating_image_file), Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                it
            )
            takePictureLauncher.launch(currentPhotoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        val storageDir: File? = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun analyzeImage(imageUri: Uri) {
        val action = PrescriptionScannerFragmentDirections
            .actionPrescriptionScannerFragmentToPrescriptionAnalysisFragment(
                dependentId = args.dependentId,
                dependentName = args.dependentName,
                imageUri = imageUri.toString()
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}