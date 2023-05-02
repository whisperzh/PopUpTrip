package com.bignerdranch.android.popuptrip.ui

import android.app.Activity
import android.os.Binder
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResetPasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResetPasswordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentResetPasswordBinding.inflate(layoutInflater,container,false)
        binding.submitButton.setOnClickListener {
            var email=binding.forgotEmailEditText.editText!!.text.toString()
            resetPassword(email)
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResetPasswordFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResetPasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun resetPassword(email:String){
        if(email.isEmpty())
        {
            Toast.makeText(context,R.string.emailIsRequired, Toast.LENGTH_SHORT).show()
        }
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            auth.sendPasswordResetEmail(email).addOnCompleteListener(activity as Activity) { task->
                if(task.isSuccessful)
                {
                    Toast.makeText(context,R.string.pleaseCheckYourEmailToResetPassword, Toast.LENGTH_SHORT).show()
                }else
                {
                    Toast.makeText(context,R.string.somethingWentWrong, Toast.LENGTH_SHORT).show()
                }

            }
        }else
        {
            Toast.makeText(context,R.string.invalidEmail, Toast.LENGTH_SHORT).show()
        }
    }
}