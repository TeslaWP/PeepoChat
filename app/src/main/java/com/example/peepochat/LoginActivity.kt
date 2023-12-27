package com.example.peepochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#7D52CA")))
        findViewById<Button>(R.id.login_button_login).setOnClickListener {
            performLogin()
        }

        findViewById<TextView>(R.id.donthaveanaccount_text_login).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }



    private fun performLogin() {
        val email = findViewById<EditText>(R.id.email_edittext_login).text.toString()
        val password = findViewById<EditText>(R.id.password_edittext_login).text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid Email!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty() || password.length<6) {
            Toast.makeText(this, "Please enter a valid Password!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener
                Log.d("All", "Successfully logged in a user with uid: ${it.result.user?.uid}")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("All", "Failed to log in a user: ${it.message}")
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}