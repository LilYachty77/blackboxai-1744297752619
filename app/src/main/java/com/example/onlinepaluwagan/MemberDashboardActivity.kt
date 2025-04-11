package com.example.onlinepaluwagan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_member_dashboard.*
import java.text.NumberFormat
import java.util.*

class MemberDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_dashboard)

        auth = Firebase.auth

        setupUI()
        loadDashboardData()
    }

    private fun setupUI() {
        // Configure toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return

        // Load member's groups and contributions
        db.collection("groups")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { groups ->
                // Update UI with group data
                tvActiveGroups.text = groups.size().toString()

                // Calculate total contributions
                var totalContributions = 0.0
                for (group in groups) {
                    val contributions = group.get("contributions") as? Map<String, Double>
                    totalContributions += contributions?.get(userId) ?: 0.0
                }
                tvTotalContributions.text = currencyFormat.format(totalContributions)

                // Load upcoming payments
                loadUpcomingPayments(groups.documents.map { it.id })
            }
    }

    private fun loadUpcomingPayments(groupIds: List<String>) {
        if (groupIds.isEmpty()) return

        val userId = auth.currentUser?.uid ?: return

        db.collection("collections")
            .whereIn("groupId", groupIds)
            .whereEqualTo("status", "pending")
            .whereEqualTo("memberId", userId)
            .orderBy("dueDate")
            .limit(5)
            .get()
            .addOnSuccessListener { collections ->
                // Update RecyclerView with upcoming payments
                // In a real app, you would use a proper adapter here

                // Show notifications for upcoming payments
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
                                    index + 100 // Use index + 100 as notification ID to avoid conflicts with head notifications
                                )
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load payments: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }
}
