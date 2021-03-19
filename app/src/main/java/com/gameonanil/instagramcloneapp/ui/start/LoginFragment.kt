package com.gameonanil.instagramcloneapp.ui.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment(R.layout.fragment_login) {
    companion object {
        private const val RC_SIGN_IN = 1234
        private const val TAG = "LoginFragment"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseFireStore: FirebaseFirestore
    private lateinit var mCallbackManager: CallbackManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        LoginManager.getInstance().logOut()
        firebaseFireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)


        tvSignUp.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            findNavController().navigate(action)
        }


        btn_login.setOnClickListener {
            if (etEmail!!.text!!.isBlank() || etPassword!!.text!!.isBlank()) {
                Toast.makeText(activity, "Please fill both email and password.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            btn_login.isEnabled = false

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    btn_login.isEnabled = true
                    Toast.makeText(
                        activity,
                        "login successfull!!! uid: ${result?.user?.uid}",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMainActivity()
                }.addOnFailureListener { expection ->
                    btn_login.isEnabled = true
                    Toast.makeText(activity, "Exception: $expection", Toast.LENGTH_SHORT).show()
                }
        }

        btn_login_google.setOnClickListener {
            signInWithGoogle()
        }


        mCallbackManager = CallbackManager.Factory.create()

        btn_login_facebook.fragment = this
        btn_login_facebook.setPermissions("email", "public_profile")
        btn_login_facebook.registerCallback(
            mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    btn_login_facebook.isEnabled = false
                    progress_log_in.isVisible = true
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    btn_login_facebook.isEnabled = true
                    progress_log_in.isVisible = false
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    btn_login_facebook.isEnabled = true
                    progress_log_in.isVisible = false
                }
            })
    }

    private fun signInWithGoogle() {
        signOutGoogle()

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun goToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "onActivityResult: ")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (task.isSuccessful) {
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        val account = task.getResult(ApiException::class.java)!!
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e)
                    }
                } else {
                    Log.e(TAG, "onActivityResult: task failed!!!!!!!")
                }

            }
        }
    }

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }*/


    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "firebaseAuthWithGoogle: firebaseAuthWithGoogle called")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    user?.let { currentUser ->
                        val userMap = HashMap<String, Any>()
                        userMap["uid"] = currentUser.uid
                        userMap["username"] = currentUser.displayName!!
                        userMap["email"] = user.email!!
                        userMap["profile_image"] = ""
                        userMap["fullname"] = user.displayName!!

                        val documentRef = firebaseFireStore.collection("users").document(user.uid)
                        documentRef.set(userMap).addOnSuccessListener {
                            Toast.makeText(activity, "Login Successful", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "firebaseAuthWithGoogle: login successful!!!!!!")
                            goToMainActivity()
                        }
                    }


                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "signInWithCredential:failure", task.exception)

                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

            val credential = FacebookAuthProvider.getCredential(token.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = auth.currentUser

                        user?.let { currentUser ->
                            val userMap = HashMap<String, Any>()
                            userMap["uid"] = currentUser.uid
                            userMap["username"] = currentUser.displayName!!
                            userMap["email"] = user.email!!
                            userMap["profile_image"] = ""
                            userMap["fullname"] = user.displayName!!

                            val documentRef =
                                firebaseFireStore.collection("users").document(user.uid)
                            documentRef.set(userMap).addOnSuccessListener {
                                Toast.makeText(activity, "Login Successful", Toast.LENGTH_SHORT)
                                    .show()
                                Log.d(TAG, "firebaseAuthWithGoogle: login successful!!!!!!")
                                btn_login_facebook.isEnabled = true
                                progress_log_in.isVisible = false
                                goToMainActivity()
                            }
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        try {
                            throw task.exception!!
                        }catch (e: FirebaseAuthUserCollisionException) {
                            LoginManager.getInstance().logOut()
                            btn_login_facebook.isEnabled = true
                            progress_log_in.isVisible = false
                            Toast.makeText(activity, "You already have an Gmail account with this email, Please login through that.", Toast.LENGTH_LONG).show()
                        }

                    }
                }



    }


    private fun signOutGoogle() {
        auth.signOut()
        googleSignInClient.signOut()

    }
}