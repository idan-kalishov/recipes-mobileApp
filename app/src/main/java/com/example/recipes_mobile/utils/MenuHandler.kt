package com.example.recipes_mobile.utils

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.navigation.NavController
import com.example.recipes_mobile.R
import com.google.firebase.auth.FirebaseAuth

object MenuUtils {

    fun showPopupMenu(
        context: Context,
        view: View,
        navController: NavController,
        auth: FirebaseAuth
    ) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.main_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_recipe -> {
                    // Navigate to Add Recipe
                    navController.navigate(R.id.action_recipeFeed_to_addRecipe)
                    true
                }
                R.id.menu_user_profile -> {
                    //TODO add user profile when available
                    navController.navigate(R.id.action_recipeFeed_to_addRecipe)
                    true
                }
                R.id.menu_logout -> {
                    // Handle Logout
                    auth.signOut()
                    navController.navigate(R.id.loginFragment)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
