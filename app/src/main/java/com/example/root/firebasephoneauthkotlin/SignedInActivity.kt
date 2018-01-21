package com.example.root.firebasephoneauthkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signed_in.*

class SignedInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signed_in)

        but_sign_out.setOnClickListener({
            FirebaseAuth.getInstance().signOut()
            finish()
        })
    }
}
