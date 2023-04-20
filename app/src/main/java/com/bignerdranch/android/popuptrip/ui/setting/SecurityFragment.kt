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
import com.bignerdranch.android.popuptrip.databinding.DialogChangeEmailBinding
import com.bignerdranch.android.popuptrip.databinding.FragmentProfileBinding
import com.bignerdranch.android.popuptrip.databinding.FragmentSecurityBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream

class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private var save=false
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var oldPassword=""
    var oldEmail=""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)

        val passwordButton = binding.changePasswordButton
        passwordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        val EmailButton=binding.changeEmailButton
        EmailButton.setOnClickListener{
            showChangeEmailDialog()
        }

        val quitButton=binding.quitSecurity
        quitButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.navigation_profile,null))
        val SecurityViewmodel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root: View = binding.root

 //       val textView: TextView = binding.textSecurity
   //     SecurityViewmodel.text.observe(viewLifecycleOwner) {
     //       textView.text = it
       // }
        return root
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
        super.onDestroyView()
        _binding = null
    }
}