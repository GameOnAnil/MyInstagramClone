package com.gameonanil.instagramcloneapp.ui.start

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.handleCoroutineException

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth:FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        btn_signup.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            findNavController().navigate(action)
        }

        btn_login.setOnClickListener {
            if (etEmail.text.isBlank() || etPassword.text.isBlank()){
                Toast.makeText(activity, "Please fill both email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btn_login.isEnabled = false

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            auth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener { result->
                    btn_login.isEnabled = true
                    Toast.makeText(activity, "login successfull!!! uid: ${result?.user?.uid}", Toast.LENGTH_SHORT).show()
                    goToMainActivity()


                }.addOnFailureListener{expection->
                    btn_login.isEnabled = true
                    Toast.makeText(activity, "Exception: $expection", Toast.LENGTH_SHORT).show()

                }

        }
    }


    private fun goToMainActivity(){
        val intent = Intent(activity,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}