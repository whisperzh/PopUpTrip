package com.bignerdranch.android.popuptrip.ui.setting
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val PICK_IMAGE_REQUEST_CODE=1

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val passwordButton = binding.changingPassword
        passwordButton.setOnClickListener {
            showChangePasswordDialog()
        }
        val imageButton=binding.imageButton
        imageButton.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)        }
        val saveButton=binding.save
        saveButton.setOnClickListener{
            //save the data
        }
        val quitButton=binding.quit
        quitButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.navigation_setting,null))
        val profileViewmodel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root: View = binding.root

        val textView: TextView = binding.titleProfile
        profileViewmodel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            val imageButton=binding.imageButton
            imageButton.setImageURI(selectedImage)
            //and also need to save in data base, not yet for now
        }
    }
    private fun showChangePasswordDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.old_password_edit_text)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.new_password_edit_text)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton("OK") { _, _ ->
                // Handle password change
                val oldPassword = oldPasswordEditText.text.toString() //need to same to previous, but not have data base yet
                val newPassword = newPasswordEditText.text.toString()
                val pattern ="^(?=.*[A-Z])(?=.*[0-9])(?=\\S+$).{6,}$".toRegex() //set the logic(one number&Uppercase)
                if (newPassword.matches(pattern)) {
                    // password is valid, save to database
                } else {
                    Toast.makeText(requireContext(), "Password does not meet requirements", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}