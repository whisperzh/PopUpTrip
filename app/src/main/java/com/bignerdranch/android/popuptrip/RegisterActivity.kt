package com.bignerdranch.android.popuptrip

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.widget.Toast
import com.bignerdranch.android.popuptrip.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private var dbReference:DatabaseReference=FirebaseDatabase.getInstance().getReferenceFromUrl("https://popup-trip-default-rtdb.firebaseio.com/")

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindComponents()
        auth = Firebase.auth

    }

    private fun bindComponents(){
        binding.signUpButton.setOnClickListener {
            var userName=binding.regUsernameEditText.editText!!.text.toString()
            var email=binding.regEmailEditText.editText!!.text.toString()
            var password=binding.regPassword.editText!!.text.toString()

            if(userName.equals("")||email.equals("")||password.equals(""))
            {
                Toast.makeText(this,R.string.pleaseFillAllTheBlanks,Toast.LENGTH_SHORT)
            }else if(!binding.regPassword.editText!!.text.toString().equals(binding.regCofirmPassword.editText!!.text.toString()))
            {
                Toast.makeText(this,R.string.pleaseDoubleCheckYourPassword,Toast.LENGTH_SHORT)
            }else if(password.length<6){
                Toast.makeText(this,R.string.yourPasswordIsTooShort,Toast.LENGTH_SHORT)
            }else
            {
//                dbReference.child("User_Table").child(email).child("username").setValue(userName)
//                dbReference.child("User_Table").child(email).child("passhash").setValue(password.hashCode())
                try {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser!!
                                Toast.makeText(
                                    baseContext,
                                    "Authentication succeed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                                dbReference.child("User_Table").child(user.uid).child("username").setValue(userName)
                                finish()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }catch (Exp:Exception)
                {
                    print(Exp)
                }
            }

        }
    }

}