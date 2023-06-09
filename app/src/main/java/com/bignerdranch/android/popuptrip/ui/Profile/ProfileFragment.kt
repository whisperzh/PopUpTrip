package com.bignerdranch.android.popuptrip.ui.Profile
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import com.bignerdranch.android.popuptrip.R as popR
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bignerdranch.android.popuptrip.MainActivity
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    private var lastSelectedItem: String? = null
    private var _binding: FragmentProfileBinding? = null
    private val PICK_IMAGE_REQUEST_CODE=1
    private var save=false
    var text=""
    private var dbReference:DatabaseReference=FirebaseDatabase.getInstance().getReferenceFromUrl("https://popup-trip-default-rtdb.firebaseio.com/")
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs:SharedPreferences
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var oldPassword=""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth=Firebase.auth
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
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
        binding.profileName.setText((activity as MainActivity).user.userName)
        binding.profileName.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                dbReference.child("User_Table").child(auth.currentUser!!.uid).child("username").setValue(s.toString())
                (activity as MainActivity).user.userName=s.toString()
                prefs.edit().putString("USER_NAME",s.toString()).commit()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })
        val passwordButton = binding.changePasswordButton
        passwordButton.setOnClickListener {
            showChangePasswordDialog()
        }
        val imageButton=binding.imageButton
        imageButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }

        val backButton = binding.GoPrefButton
        backButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.navigation_preference,null))
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

    private fun showChangePasswordDialog() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val email=dialogView.findViewById<EditText>(R.id.email_edit_text)
        //val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.old_password_edit_text)
        //val newPasswordEditText = dialogView.findViewById<EditText>(R.id.new_password_edit_text)
//        val dialog = MaterialAlertDialogBuilder(requireContext())
//            .setView(dialogView)
//            .setTitle("Change Password")
//            .setPositiveButton("OK") { _, _ ->
//                // Handle password change
//                oldPassword = prefs.getString("password","Qaz123").toString() //need to same to previous, but not have data base yet
//                val newPassword = newPasswordEditText.text.toString()
//                val pattern =
//                    "^(?=.*[A-Z])(?=.*[0-9])(?=\\S+$).{6,}$".toRegex() //set the logic(one number&Uppercase)
//                if (oldPasswordEditText.text.toString().equals(oldPassword)) {//fit old password
//                    if (newPassword.matches(pattern)) {
//                        // password is valid, save to database
//                        prefs.edit().putString("password", newPassword).apply()//save to sharePref(need to implement encryption)
//                        oldPassword=newPassword
//                        Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT)
//                            .show()
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Password does not meet requirements",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }else{
//                    Toast.makeText(
//                        requireContext(),
//                        "Wrong Old Password",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .create()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton(popR.string.sendEmail) { _, _ ->
                resetPassword(email.text.toString())
            }.setNegativeButton(popR.string.cancel,null)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
    private fun resetPassword(email:String){
        if(email.isEmpty())
        {
            Toast.makeText(requireContext(),R.string.emailIsRequired,Toast.LENGTH_SHORT).show()
        }
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            auth.sendPasswordResetEmail(email).addOnCompleteListener(requireActivity()) { task->
                if(task.isSuccessful)
                {
                    Toast.makeText(requireContext(),R.string.pleaseCheckYourEmailToResetPassword,Toast.LENGTH_SHORT).show()
                }else
                {
                    Toast.makeText(requireContext(),R.string.somethingWentWrong,Toast.LENGTH_SHORT).show()

                }

            }
        }else
        {
            Toast.makeText(requireContext(),R.string.invalidEmail,Toast.LENGTH_SHORT).show()
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