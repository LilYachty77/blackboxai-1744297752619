package com.example.onlinepaluwagan.utils

import com.example.onlinepaluwagan.models.Collection
import com.example.onlinepaluwagan.models.Group
import com.example.onlinepaluwagan.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseManager private constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
        }
    }

    // Authentication Methods
    suspend fun signUpWithEmail(email: String, password: String, userData: Map<String, Any>): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")
            
            val userModel = User(
                id = user.uid,
                email = email,
                fullName = userData["fullName"] as String,
                phoneNumber = userData["phoneNumber"] as String,
                createdAt = System.currentTimeMillis()
            )
            
            db.collection("users").document(user.uid)
                .set(userModel.toMap())
                .await()
            
            Result.success(userModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Login failed")
            val userData = getUserData(user.uid)
            Result.success(userData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign in failed")
            
            // Check if user exists
            val existingUser = try {
                getUserData(user.uid)
            } catch (e: Exception) {
                null
            }

            if (existingUser == null) {
                // Create new user
                val userModel = User(
                    id = user.uid,
                    email = user.email ?: "",
                    fullName = account.displayName ?: "",
                    profileImageUrl = account.photoUrl?.toString() ?: "",
                    createdAt = System.currentTimeMillis()
                )
                
                db.collection("users").document(user.uid)
                    .set(userModel.toMap())
                    .await()
                
                Result.success(userModel)
            } else {
                Result.success(existingUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // User Methods
    suspend fun getUserData(userId: String): User {
        val doc = db.collection("users").document(userId).get().await()
        return User.fromMap(doc.data ?: mapOf())
    }

    suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update(
                    mapOf(
                        "role" to role,
                        "roleSelectedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Group Methods
    suspend fun createGroup(group: Group): Result<Group> {
        return try {
            val groupRef = db.collection("groups").document()
            val groupWithId = group.copy(
                id = groupRef.id,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            groupRef.set(groupWithId.toMap()).await()
            Result.success(groupWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupsForUser(userId: String): Result<List<Group>> {
        return try {
            val groups = db.collection("groups")
                .whereArrayContains("members", userId)
                .get()
                .await()
                .documents
                .map { Group.fromMap(it.data ?: mapOf()) }
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Collection Methods
    suspend fun getUpcomingCollections(userId: String): Result<List<Collection>> {
        return try {
            val collections = db.collection("collections")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", Collection.STATUS_PENDING)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
                .documents
                .map { Collection.fromMap(it.data ?: mapOf()) }
            Result.success(collections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCollectionStatus(
        collectionId: String,
        status: String,
        paymentMethod: String? = null,
        paymentReference: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            
            if (status == Collection.STATUS_PAID) {
                updates["paidDate"] = System.currentTimeMillis()
                paymentMethod?.let { updates["paymentMethod"] = it }
                paymentReference?.let { updates["paymentReference"] = it }
            }
            
            db.collection("collections")
                .document(collectionId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
