package com.example.onlinepaluwagan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_role_selection.*

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        auth = Firebase.auth

        // Prevent going back to login/signup
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        cardHead.setOnClickListener {
            selectRole("head")
        }

        cardMember.setOnClickListener {
            selectRole("member")
        }
    }

    private fun selectRole(role: String) {
        val user = auth.currentUser
        if (user != null) {
            // Update user document with selected role
            db.collection("users").document(user.uid)
                .update(
                    mapOf(
                        "role" to role,
                        "roleSelectedAt" to System.currentTimeMillis()
                    )
                )
                .addOnSuccessListener {
                    // Navigate to appropriate dashboard based on role
                    val dashboardIntent = when (role) {
                        "head" -> Intent(this, HeadDashboardActivity::class.java)
                        "member" -> Intent(this, MemberDashboardActivity::class.java)
                        else -> Intent(this, DashboardActivity::class.java)
                    }.apply {
                        putExtra("USER_ROLE", role)
                        // Clear back stack
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(dashboardIntent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to save role: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // User somehow got logged out
            Toast.makeText(
                this,
                "Session expired. Please login again.",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Disable back button
    override fun onBackPressed() {
        // Do nothing
    }
}
