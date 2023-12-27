package com.example.peepochat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import java.util.UUID

var selectedPhotoUri: Uri? = null

const val databaseUrl = "https://peepochat-88c1c-default-rtdb.europe-west1.firebasedatabase.app/"
//var storageUrl = "gs://peepochat-88c1c.appspot.com"

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<Button>(R.id.register_button_register).setOnClickListener {
            performRegister()
        }

        findViewById<TextView>(R.id.alreadyhaveanaccount_text_register).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.selectphoto_button_register).setOnClickListener {
            Log.d("All", "Clicked select button")
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("All", "Tried to select img")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("All", "Selected img")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(this.resources,bitmap)
            findViewById<ImageButton>(R.id.selectphoto_button_register).setImageResource(0)
            findViewById<ImageButton>(R.id.selectphoto_button_register).setBackgroundDrawable(bitmapDrawable)
        }
    }


    private fun performRegister() {
        val username = findViewById<EditText>(R.id.username_edittext_register).text.toString()
        val email = findViewById<EditText>(R.id.email_edittext_register).text.toString()
        val password = findViewById<EditText>(R.id.password_edittext_register).text.toString()

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a valid Username!", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid Email!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty() || password.length<6) {
            Toast.makeText(this, "Please enter a valid Password!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener
                Log.d("All", "Successfully created user with uid: ${it.result.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("All", "Failed to create user: ${it.message}")
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri == null) {
            //FirebaseStorage.getInstance().reference.child("/images/default.jpg").downloadUrl
            //FirebaseStorage.getInstance().getReferenceFromUrl("gs://peepochat-88c1c.appspot.com/images/default.jpg").downloadUrl
            //    .addOnSuccessListener {
            //        selectedPhotoUri = it
            //}
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().reference.child("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("All", "Successfully uploaded image: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("All", "File Location $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
        }
            .addOnFailureListener {
                Log.d("All", "Something went wrong when uploading image: ${it.message}")
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance(databaseUrl).getReference("/users/$uid")

        val user = User(uid, findViewById<EditText>(R.id.username_edittext_register).text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("All", "User saved to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("All", "Failed to set value to database: ${it.message}")
            }
    }

}

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String): Parcelable {
    constructor() : this("","","")
}