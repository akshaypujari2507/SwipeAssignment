package com.swipeassignment.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.swipeassignment.R
import com.swipeassignment.databinding.FragmentAddProductBinding
import com.swipeassignment.network.ApiInstance
import com.swipeassignment.network.ApiServices
import com.swipeassignment.repo.Repository
import com.swipeassignment.utilities.ApiResponce
import com.swipeassignment.viewmodel.ProductViewModel
import com.swipeassignment.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddProductFragment : Fragment(R.layout.fragment_add_product) {

    private lateinit var viewModel: ProductViewModel

    private var _binding: FragmentAddProductBinding? = null
//    private var _binding: Fragment? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddProductBinding.bind(view)

        initializeViewModel()

        binding.apply {

            // Observing Ui State
//            observeUiState()

            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnAdd.setOnClickListener {

                val name = productName.text
                val type = productType.text
                val price = productPrice.text
                val tax = productTax.text

                // Checking if any field is empty or not.
                if (
                    name.isNullOrEmpty() ||
                    type.isNullOrEmpty() ||
                    price.isNullOrEmpty() ||
                    tax.isNullOrEmpty()
                ){
                    Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.addProductDetails(
                    productName = name.toString(),
                    productType = type.toString(),
                    price = price.toString().toDouble(),
                    tax = tax.toString().toDouble(),
                )
                binding.progressBar.visibility = View.VISIBLE

            }

            viewModel.productLiveData.observe(viewLifecycleOwner, Observer {

                when(it){
                    is ApiResponce.Loading ->{

                    }
                    is ApiResponce.Successful ->{
                        clearText()
                        Toast.makeText(requireContext(), it.data?.message, Toast.LENGTH_SHORT).show()
                    }
                    is ApiResponce.Error ->{
                        Toast.makeText(requireContext(), it.data?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            })


            btnSelectImage.setOnClickListener {
                binding.tvImageName.text = ""
                viewModel.setImageFile(null)
                launchGallery()
            }

        }

    }

    private fun clearText() {
        binding.progressBar.visibility = View.GONE
        binding.tvImageName.text = ""
        binding.productName.setText("")
        binding.productType.setText("")
        binding.productPrice.setText("")
        binding.productTax.setText("")
    }

    private fun initializeViewModel() {
        val apiInstance = ApiInstance.getApiIntence().create(ApiServices::class.java)
        viewModel = ViewModelProvider(this, ProductViewModelFactory(Repository(apiInstance, requireContext())))[ProductViewModel::class.java]
    }


    // Permission required.
    private val requiredPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    private fun launchGallery() {

        // Different scenarios with permissions
        when{
            ContextCompat.checkSelfPermission(requireContext(),requiredPermission)
                    == PackageManager.PERMISSION_GRANTED
            -> {
                val pickIntent = Intent(Intent.ACTION_PICK)
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")

                // URI is path of the image inside external storage
                openGalleryLauncher.launch(pickIntent)

            }
            shouldShowRequestPermissionRationale(requiredPermission) -> showRationaleDialog()
            else -> requestPermission.launch( arrayOf(requiredPermission) )
        }
    }

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if( result.resultCode == Activity.RESULT_OK && result.data != null){

                lifecycleScope.launch {

                    val file : File? = try {
                        uriToImageFile(result.data?.data!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    binding.tvImageName.text = file?.name

                    withContext(Dispatchers.Default){
                        // Recording selected file.
                        viewModel.setImageFile(file)
                    }

                }

            }
        }

    private suspend fun uriToImageFile(uri: Uri): File? =
        withContext(Dispatchers.IO) {

            var result: File? = null
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = requireContext().contentResolver.query(uri, filePathColumn, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    result = File(filePath)
                }
                cursor.close()
            }

            return@withContext result
        }

    // Permission launcher
    private val requestPermission : ActivityResultLauncher<Array<String>> =
        registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { isGranted ->
                if (!isGranted.value) {
                    Snackbar.make(requireView(), "Storage access denied.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    private fun showRationaleDialog() {

        fun helpDialog() {
            AlertDialog
                .Builder(requireContext())
                .apply {
                    setTitle("Grant Access Manually")
                    setMessage("To grant access manually, Hold app icon > Click \"App info\" > Permissions")
                    setPositiveButton("OK"){helpDialog , _ ->
                        helpDialog.dismiss()
                    }
                    show()
                }
        }

        // Rationale Dialog
        AlertDialog
            .Builder(requireContext())
            .apply {
                setTitle("No Storage Access Granted")
                setMessage("Cannot access gallery due to permission restrictions from system.")
                setPositiveButton("Ask Again"){ _, _ ->
                    requestPermission.launch( arrayOf(requiredPermission) )
                }
                setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
                setNeutralButton("Help"){ _,_ ->
                    // Help dialog
                    helpDialog()
                }
                show()
            }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}