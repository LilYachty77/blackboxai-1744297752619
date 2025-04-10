package com.example.onlinepaluwagan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.text.NumberFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var userRole: String = ""
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = Firebase.auth
        userRole = intent.getStringExtra("USER_ROLE") ?: ""

        setupUI()
        loadDashboardData()
        setupClickListeners()
    }

    private fun setupUI() {
        // Configure toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Setup RecyclerView
        rvUpcomingCollections.layoutManager = LinearLayoutManager(this)
        
        // Show/hide FAB based on role
        fabAddGroup.visibility = if (userRole == "head") View.VISIBLE else View.GONE
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        // Load user's groups
        db.collection("groups")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { groups ->
                val activeGroups = groups.size()
                tvActiveGroups.text = activeGroups.toString()

                // Calculate total funds
                var totalFunds = 0.0
                for (group in groups) {
                    totalFunds += group.getDouble("totalFunds") ?: 0.0
                }
                tvTotalFunds.text = currencyFormat.format(totalFunds)

                // Calculate progress
                val completedGroups = groups.count { it.getBoolean("isCompleted") ?: false }
                val progress = if (activeGroups > 0) {
                    (completedGroups.toFloat() / activeGroups.toFloat() * 100).toInt()
                } else {
                    0
                }
                tvGroupProgress.text = "$progress%"

                // Load upcoming collections
                loadUpcomingCollections(groups.documents.map { it.id })
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load dashboard: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUpcomingCollections(groupIds: List<String>) {
        if (groupIds.isEmpty()) {
            // Show empty state
            return
        }

        db.collection("collections")
            .whereIn("groupId", groupIds)
            .whereEqualTo("status", "pending")
            .orderBy("dueDate")
            .limit(5)
            .get()
            .addOnSuccessListener { collections ->
                // Update RecyclerView with collections
                // In a real app, you would use a proper adapter here
                // For this example, we'll just show the count
                if (collections.isEmpty) {
                    // Show empty state
                } else {
                    // Update RecyclerView
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load collections: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        btnViewProgress.setOnClickListener {
            // Navigate to progress details
            Toast.makeText(this, "View Progress clicked", Toast.LENGTH_SHORT).show()
        }

        btnViewGroups.setOnClickListener {
            // Navigate to groups list
            Toast.makeText(this, "View Groups clicked", Toast.LENGTH_SHORT).show()
        }

        btnViewFunds.setOnClickListener {
            // Navigate to funds details
            Toast.makeText(this, "View Funds clicked", Toast.LENGTH_SHORT).show()
        }

        btnViewAll.setOnClickListener {
            // Navigate to all collections
            Toast.makeText(this, "View All Collections clicked", Toast.LENGTH_SHORT).show()
        }

        fabAddGroup.setOnClickListener {
            // Navigate to create group
            Toast.makeText(this, "Add Group clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // Add menu for settings, logout, etc.
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Navigate to settings
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
