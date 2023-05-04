package com.bignerdranch.android.popuptrip.ui

import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.bignerdranch.android.popuptrip.R
import com.bignerdranch.android.popuptrip.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var dbReference: DatabaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://popup-trip-default-rtdb.firebaseio.com/")
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
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
        binding= FragmentRegisterBinding.inflate(layoutInflater,container,false)
        bindComponents()
        return binding.root
    }

    private fun bindComponents(){
        binding.signUpButton.setOnClickListener {
            var userName=binding.regUsernameEditText.editText!!.text.toString()
            var email=binding.regEmailEditText.editText!!.text.toString()
            var password=binding.regPassword.editText!!.text.toString()

            if(userName.equals("")||email.equals("")||password.equals(""))
            {
                Toast.makeText(context,R.string.pleaseFillAllTheBlanks, Toast.LENGTH_SHORT)
            }else if(!binding.regPassword.editText!!.text.toString().equals(binding.regCofirmPassword.editText!!.text.toString()))
            {
                Toast.makeText(context,R.string.pleaseDoubleCheckYourPassword, Toast.LENGTH_SHORT)
            }else if(password.length<6){
                Toast.makeText(context,R.string.yourPasswordIsTooShort, Toast.LENGTH_SHORT)
            }else
            {
//                dbReference.child("User_Table").child(email).child("username").setValue(userName)
//                dbReference.child("User_Table").child(email).child("passhash").setValue(password.hashCode())
                fireBaseCreateUser(email, password,userName)
            }

        }
    }

    private fun fireBaseCreateUser(email:String,password:String,userName:String)
    {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity as Activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(ContentValues.TAG, "createUserWithEmail:success")
                        val user = auth.currentUser!!
                        Toast.makeText(
                            context,
                            R.string.authen_succeed,
                            Toast.LENGTH_SHORT,
                        ).show()
                        dbReference.child("User_Table").child(user.uid).child("username").setValue(userName)
//                        finish()
                        (activity as FragmentActivity).supportFragmentManager.popBackStack()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            context,
                            R.string.authen_fail,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }catch (Exp:Exception)
        {
            print(Exp)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}