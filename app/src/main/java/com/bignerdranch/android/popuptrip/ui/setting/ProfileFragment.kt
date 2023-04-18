package com.bignerdranch.android.popuptrip.ui.setting
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentProfileBinding
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val PICK_IMAGE_REQUEST_CODE=1
    private var save=false
    var text=""
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var oldPassword=""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val saved=prefs.getString("EditedName","")
        binding.profileName.setText(saved)
        text = saved ?: "" // Set text variable to saved value, or empty string if null
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, "IMG_Profile.jpg")
        if (imageFile.exists()) {
            val imageUri = FileProvider.getUriForFile(requireContext(), "com.example.app.fileprovider", imageFile)
            if (imageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                binding.imageButton.setImageBitmap(bitmap)
            }
        }
        val passwordButton = binding.changingPassword
        passwordButton.setOnClickListener (
            Navigation.createNavigateOnClickListener(R.id.navigation_security,null)
        )
        val imageButton=binding.imageButton
        imageButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }

        val saveButton=binding.save
        text = binding.profileName.text.toString()
        saveButton.setOnClickListener{
            save=true
        }
        val quitButton=binding.quit
        quitButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.navigation_settings,null))
        val profileViewmodel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root: View = binding.root

    //    val textView: TextView = binding.titleProfile
      //  profileViewmodel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            val imageButton=binding.imageButton
            imageButton.setImageURI(selectedImage)
            //and also need to save in data base, not yet for now
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImage)
            //save the image on image button instead of just the vector
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val fileName = "IMG_Profile.jpg" //random file name
            val imageFile = File(storageDir, fileName)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        }
    }
    override fun onDestroyView() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if(save) {
            prefs.edit().putString("EditedName",binding.profileName.text.toString()).apply()//save the switch status
        }
        super.onDestroyView()
        _binding = null
    }
}