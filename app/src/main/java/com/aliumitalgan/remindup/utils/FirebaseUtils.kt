package com.aliumitalgan.remindup.utils



import com.google.firebase.firestore.FirebaseFirestore
import com.aliumitalgan.remindup.models.Goal

object FirebaseUtils {
    private val db = FirebaseFirestore.getInstance()

    // Hedef ekleme
    fun addGoal(goal: Goal) {
        val goalMap = hashMapOf(
            "title" to goal.title,
            "progress" to goal.progress
        )

        db.collection("goals")
            .add(goalMap)
            .addOnSuccessListener { documentReference ->
                println("Goal added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding goal: $e")
            }
    }

    // Hedefleri çekme
    fun getGoals(onSuccess: (List<Goal>) -> Unit) {
        db.collection("goals")
            .get()
            .addOnSuccessListener { result ->
                val goals = mutableListOf<Goal>()
                for (document in result) {
                    val goal = document.toObject(Goal::class.java)
                    goals.add(goal)
                }
                onSuccess(goals)
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }
}
