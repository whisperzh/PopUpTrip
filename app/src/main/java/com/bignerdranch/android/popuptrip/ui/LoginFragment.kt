package com.bignerdranch.android.popuptrip.ui

import android.R
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bignerdranch.android.popuptrip.R as popR
import com.bignerdranch.android.popuptrip.MainActivity
import com.bignerdranch.android.popuptrip.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding

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
        binding=FragmentLoginBinding.inflate(layoutInflater,container,false)
        // Inflate the layout for this fragment
        bindComponents()

        return binding.root
    }
    private fun bindComponents() {
            binding.createAccountButton.setOnClickListener {
//                val intent = Intent(this, RegisterActivity::class.java)
//                startActivity(intent)
                val nextFrag = RegisterFragment.newInstance("1","2")
                activity?.supportFragmentManager!!.beginTransaction()
                    .replace(com.bignerdranch.android.popuptrip.R.id.fragment_container, nextFrag, "findThisFragment")
                    .addToBackStack(null)
                    .commit()
            }
            binding.LoginButton.setOnClickListener {
                auth = Firebase.auth
                login()
            }
            binding.resetPasswordButton.setOnClickListener {
                val nextFrag = ResetPasswordFragment.newInstance("1","2")
                activity?.supportFragmentManager!!.beginTransaction()
                    .replace(com.bignerdranch.android.popuptrip.R.id.fragment_container, nextFrag, "findThisFragment")
                    .addToBackStack(null)
                    .commit()
            }
    }

    private fun login() {

            var email = binding.username.editText?.text.toString()
            var password = binding.password.editText?.text.toString()
            if(email.equals("")||password.equals(""))
            {
                Toast.makeText(context,popR.string.fill_blank_toast,Toast.LENGTH_SHORT).show()
            }else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(activity as Activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            Toast.makeText(
                                context,
                                popR.string.authen_succeed,
                                Toast.LENGTH_SHORT,
                            ).show()
                            val intent = Intent(context, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                context,
                                popR.string.authen_fail,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}