package com.example.onlinepaluwagan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_head_dashboard.*
import java.text.NumberFormat
import java.util.*

class HeadDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_head_dashboard)

        auth = Firebase.auth

        setupUI()
        loadDashboardData()
        setupClickListeners()
    }

    private fun setupUI() {
        // Configure toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        // Load groups where user is head
        db.collection("groups")
            .whereEqualTo("headId", userId)
            .get()
            .addOnSuccessListener { groups ->
                // Update active groups count
                tvActiveGroups.text = groups.size().toString()

                // Calculate total funds
                var totalFunds = 0.0
                var totalPaid = 0.0
                var totalUnpaid = 0.0

                for (group in groups) {
                    totalFunds += group.getDouble("totalFunds") ?: 0.0
                    
                    // Calculate paid and unpaid amounts
                    val collections = group.get("collections") as? Map<String, Any> ?: continue
                    for ((_, value) in collections) {
                        val collection = value as? Map<String, Any> ?: continue
                        if (collection["status"] == "paid") {
                            totalPaid += (collection["amount"] as? Double) ?: 0.0
                        } else {
                            totalUnpaid += (collection["amount"] as? Double) ?: 0.0
                        }
                    }
                }

                // Update UI
                tvTotalFunds.text = currencyFormat.format(totalFunds)
                tvPaidAmount.text = currencyFormat.format(totalPaid)
                tvUnpaidAmount.text = currencyFormat.format(totalUnpaid)

                // Load upcoming collections
                loadUpcomingCollections(groups.documents.map { it.id })
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load dashboard: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUpcomingCollections(groupIds: List<String>) {
        if (groupIds.isEmpty()) return

        db.collection("collections")
            .whereIn("groupId", groupIds)
            .whereEqualTo("status", "pending")
            .orderBy("dueDate")
            .limit(5)
            .get()
            .addOnSuccessListener { collections ->
                // Update RecyclerView with collections
                // In a real app, you would use a proper adapter here

                // Show notifications for upcoming collections
                collections.forEachIndexed { index, collection ->
                    val groupId = collection.getString("groupId") ?: return@forEachIndexed
                    val amount = collection.getDouble("amount") ?: 0.0
                    val dueDate = collection.getTimestamp("dueDate")?.toDate()?.toString() ?: return@forEachIndexed

                    // Get group name
                    db.collection("groups").document(groupId).get()
                        .addOnSuccessListener { groupDoc ->
                            val groupName = groupDoc.getString("name") ?: "Unknown Group"
                            
                            // Show notification for upcoming payment
                            com.example.onlinepaluwagan.utils.NotificationManager
                                .showUpcomingPaymentNotification(
                                    this,
                                    groupName,
                                    amount,
                                    dueDate,
                                    index + 1 // Use index as notification ID
                                )
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load collections: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        btnCreateGroup.setOnClickListener {
            // Navigate to create group screen
            Toast.makeText(this, "Create Group clicked", Toast.LENGTH_SHORT).show()
        }

        btnViewAll.setOnClickListener {
            // Navigate to all collections screen
            Toast.makeText(this, "View All Collections clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
