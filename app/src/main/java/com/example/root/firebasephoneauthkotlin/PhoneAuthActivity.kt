package com.example.root.firebasephoneauthkotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_phone_auth.*
import java.util.*

class PhoneAuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        val isUserSignedIn = FirebaseAuth.getInstance().currentUser != null

        but_fui_sign_in_.setOnClickListener({
            if (!isUserSignedIn) signIn()
        })

        but_fsdk_sign_in_.setOnClickListener({startActivity(
                Intent(PhoneAuthActivity@this, PhoneAuthWithSdk::class.java))})
        }


    private fun signIn(){
        val params = Bundle()
        params.putString(AuthUI.EXTRA_DEFAULT_COUNTRY_CODE, "ng")
        params.putString(AuthUI.EXTRA_DEFAULT_NATIONAL_NUMBER, "23456789")

        val phoneConfigWithDefaultNumber = AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER)
                .setParams(params)
                .build()

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(phoneConfigWithDefaultNumber))
                        .build(),
                RC_SIGN_IN)
    }

    fun signOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    // user is now signed out
                    //todo:
                    showSnackbar("sign out successful")
                    finish()
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...)
        // when starting the sign in flow.

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            when {
                resultCode == Activity.RESULT_OK -> {
                    // Successfully signed in
                    showSnackbar("SignIn successful")
                    startActivity(Intent(PhoneAuthActivity@this,  SignedInActivity::class.java))
                    finish()
                }

                response == null -> {
                    // Sign in failed
                    // User pressed back button
                    showSnackbar("Sign in cancelled")
                    return
                }

                response.errorCode == ErrorCodes.NO_NETWORK -> {
                    // Sign in failed
                    //No Internet Connection
                    showSnackbar("No Internet connection")
                    return
                }

                response.errorCode == ErrorCodes.UNKNOWN_ERROR -> {
                    // Sign in failed
                    //Unknown Error
                    showSnackbar("Unknown error")
                    return
                }

                else -> {
                    showSnackbar("Unknown Response")
                }

            }
            // Successfully signed in
            /*if (resultCode == RESULT_OK) {
                //todo:
                showSnackbar("SignIn successful")
//                startActivity()
                finish()
                return
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar("Sign in cancelled")
                    return
                }

                if (response.errorCode == ErrorCodes.NO_NETWORK) {
                    showSnackbar("No Internet connection")
                    return
                }

                if (response.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar("Unknown error")
                    return
                }
            }

            showSnackbar("Unknown Response");*/
        }
    }

    fun showSnackbar(message: String){
        Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val RC_SIGN_IN = 123
    }

}


