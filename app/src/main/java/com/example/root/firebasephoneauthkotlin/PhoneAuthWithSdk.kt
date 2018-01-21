package com.example.root.firebasephoneauthkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_phone_auth_with_sdk.*
import java.util.concurrent.TimeUnit

class PhoneAuthWithSdk : AppCompatActivity() {

    private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"

    lateinit var mAuth: FirebaseAuth
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mVerificationId: String? = "default"
    private var mVerificationInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth_with_sdk)


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        initUI()
        initCallback()
    }

    fun initUI(){
        btn_sign_in_.setOnClickListener({
            if (!validatePhoneNumber()) {
                return@setOnClickListener
            }
            startPhoneNumberVerification(tv_phone_number.text.toString())
        })
        btn_verify.setOnClickListener({
            val code = getVerificationCode()
            if (TextUtils.isEmpty(code)) {
                showSnackbar("Phone cannot be empty.")
                return@setOnClickListener
            }
            verifyPhoneNumberWithCode( mVerificationId!!.toString(), code)
        })
        btn_resend.setOnClickListener({
            resendVerificationCode(tv_phone_number.text.toString(), mResendToken)
        })

    }

    fun initCallback(){
        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                showSnackbar("onVerificationCompleted:" + credential)
                // [START_EXCLUDE silent]
                mVerificationInProgress = false

                // [END_EXCLUDE]

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // [START_EXCLUDE silent]
                showSnackbar("onVerificationFailed")
                mVerificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    showSnackbar("Invalid phone number.")
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showSnackbar("Quota exceeded.")
                }

                // Show a message and update the UI
                showSnackbar("Verification failed")

            }

            override fun onCodeSent(verificationId: String?,
                                    token: PhoneAuthProvider.ForceResendingToken?) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                showSnackbar("onCodeSent:" + verificationId)

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String?) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                showSnackbar("Code timeout")
            }
        }
        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser

        showSnackbar("User signed in")
        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(tv_phone_number.text.toString())
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60,             // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,           // Activity (for callback binding)
                mCallbacks)        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        mVerificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    // [START resend_verification]
    private fun resendVerificationCode(phoneNumber: String,
                                       token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                mCallbacks, // OnVerificationStateChangedCallbacks
                token)             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        showSnackbar("signInWithCredential:success")
                        val user = task.result.user
                        startActivity(Intent(this, SignedInActivity::class.java))
                    } else {
                        // Sign in failed, display a message and update the UI
                        showSnackbar("signInWithCredential:failure")
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            showSnackbar("Invalid code.")
                        }
                        showSnackbar("Sign in failed")
                    }
                }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = tv_phone_number.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            showSnackbar("Invalid phone number.")
            return false
        }

        return true
    }

    fun getVerificationCode(): String{
        val code = ver_code_one.text.toString() + ver_code_two.text.toString() +
                ver_code_three.text.toString() + ver_code_four.text.toString() +
                ver_code_five.text.toString() + ver_code_six.text.toString()
        return  code
    }


    fun showSnackbar(message:String){
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }
}
