package com.bignerdranch.android.popuptrip.ui.setting
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.DialogChangeEmailBinding
import com.bignerdranch.android.popuptrip.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentProfileBinding? = null
    private val PICK_IMAGE_REQUEST_CODE=1
    private var save=false
    var text=""
    private val dataList =  listOf("Walk","Public","Drive","Cycling")
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
        val position = prefs.getInt("MethodSpinnerPosition", 0)
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
        val passwordButton = binding.changePasswordButton
        passwordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        val EmailButton=binding.changeEmailButton
        EmailButton.setOnClickListener{
            showChangeEmailDialog()
        }
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
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataList)//set adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.methodSpinner.adapter = adapter//bind the adapter
        binding.methodSpinner.setSelection(position)
        binding.methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position) as String
                if (lastSelectedItem != null) {
                    val toast = Toast.makeText(
                        requireContext(),
                        "You choose $selectedItem",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    Log.d("MyFragment", "Selected item: $selectedItem")
                }
                lastSelectedItem = selectedItem
                prefs.edit().putInt("SpinnerPosition", binding.methodSpinner.selectedItemPosition).apply()//save spinner position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
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

    private fun showChangePasswordDialog(){
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.old_password_edit_text)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.new_password_edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton("OK") { _, _ ->
                // Handle password change
                oldPassword = prefs.getString("password","Qaz123").toString() //need to same to previous, but not have data base yet
                val newPassword = newPasswordEditText.text.toString()
                val pattern =
                    "^(?=.*[A-Z])(?=.*[0-9])(?=\\S+$).{6,}$".toRegex() //set the logic(one number&Uppercase)
                if (oldPasswordEditText.text.toString().equals(oldPassword)) {//fit old password
                    if (newPassword.matches(pattern)) {
                        // password is valid, save to database
                        prefs.edit().putString("password", newPassword).apply()//save to sharePref(need to implement encryption)
                        oldPassword=newPassword
                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Password does not meet requirements",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    Toast.makeText(
                        requireContext(),
                        "Wrong Old Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    @SuppressLint("MissingInflateParams")
    private fun showChangeEmailDialog() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val binding = DialogChangeEmailBinding.inflate(layoutInflater)

        var code = ""

        binding.emailButton.setOnClickListener {
            val oldEmail = prefs.getString("emailAddress", "bu123@bu.edu").toString() // need to same to previous, but not have data base yet
            if (oldEmail == binding.oldEmailEditText.text.toString()) {
                // send code to old email address
                code = "1234" // example code
                Toast.makeText(
                    requireContext(),
                    "Code sent to $oldEmail",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Wrong old email address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setTitle("Change Email Address")
            .setPositiveButton("OK") { _, _ ->
                // Handle email change
                val newEmail = binding.newEmailEditText.text.toString()
                val newRepeatEmail = binding.repeatNewEmailEditText.text.toString()
                val inputCode = binding.captchaEditText.text.toString()
                val pattern =
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$".toRegex() // set the logic(one number&Uppercase)
                if (newEmail == newRepeatEmail && newEmail.matches(pattern)) { // check for new address
                    if (inputCode == code) { // fit the captcha, so can change the email
                        prefs.edit().putString("emailAddress", newEmail).apply() // save to sharePref(need to implement encryption)
                        Toast.makeText(
                            requireContext(),
                            "Email Address updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Wrong Verification Code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Wrong New Address format",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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