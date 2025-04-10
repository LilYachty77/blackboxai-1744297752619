package com.example.onlinepaluwagan

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = Firebase.firestore

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign up failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Setup click listeners
        btnSignUp.setOnClickListener {
            if (validateInputs()) {
                signUpWithEmailPassword()
            }
        }

        btnGoogleSignUp.setOnClickListener {
            signUpWithGoogle()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (fullName.isEmpty()) {
            tilFullName.error = "Full name is required"
            return false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Valid email is required"
            return false
        }

        // Validate Philippines phone number (must be 10 digits starting with 9)
        if (!phone.matches(Regex("^9\\d{9}$"))) {
            tilPhone.error = "Enter valid Philippines mobile number (10 digits starting with 9)"
            return false
        }

        if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            return false
        }

        if (password != confirmPassword) {
            tilConfirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun signUpWithEmailPassword() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserData()
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // For Google sign up, we still need phone number
                    showPhoneNumberDialog()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userData = hashMapOf(
                "fullName" to etFullName.text.toString(),
                "email" to etEmail.text.toString(),
                "phone" to "+63${etPhone.text.toString()}",
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users").document(user.uid)
                .set(userData)
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save user data: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showPhoneNumberDialog() {
        // In a real app, you would show a dialog to collect phone number
        // For this example, we'll just navigate to role selection
        startActivity(Intent(this, RoleSelectionActivity::class.java))
        finish()
    }
}
