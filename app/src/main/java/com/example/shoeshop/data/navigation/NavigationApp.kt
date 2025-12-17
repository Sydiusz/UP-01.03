package com.example.shoeshop.data.navigation

import EmailVerificationScreen
import RecoveryVerificationScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shoeshop.ui.screens.CategoryProductsScreen
import com.example.shoeshop.ui.screens.ForgotPasswordScreen
import com.example.shoeshop.ui.screens.HomeScreen
import com.example.shoeshop.ui.screens.OnboardScreen
import com.example.shoeshop.ui.screens.RegisterAccountScreen
import com.example.shoeshop.ui.screens.SignInScreen

// В NavigationApp обновите маршруты
@Composable
fun NavigationApp(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "start_menu"
    ) {
        composable("sign_up") {
            RegisterAccountScreen(
                onSignInClick = { navController.navigate("sign_in") },
                onSignUpClick = { navController.navigate("email_verification") }
            )
        }
        composable("sign_in") {
            SignInScreen(
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onSignInClick = { navController.navigate("home") },
                onSignUpClick = { navController.navigate("sign_up") }
            )
        }

        composable("email_verification") {
            EmailVerificationScreen(
                onSignInClick = { navController.navigate("sign_in") },
                onVerificationSuccess = { navController.navigate("home") }
            )
        }
        composable("reset_password") {
            RecoveryVerificationScreen({},{}
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateToOtpVerification = { navController.navigate("reset_password") },
            )
        }

        composable("start_menu") {
            OnboardScreen (
                onGetStartedClick = { navController.navigate("sign_up") },
            )
        }

        composable("home") {
            HomeScreen({},{},{})
        }

        composable("home") {
            HomeScreen(
                onProductClick = { product ->
                    // Навигация на экран товара
                    // navController.navigate("product/${product.id}")
                },
                onCartClick = {
                    // Навигация на корзину
                    // navController.navigate("cart")
                },
                onSearchClick = {
                    // Навигация на поиск
                    // navController.navigate("search")
                },
                onCategoryClick = { categoryName ->
                    // Навигация на экран категории
                    navController.navigate("category/$categoryName")
                }
            )
        }

        composable(
            route = "category/{categoryName}",
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryProductsScreen(
                categoryName = categoryName,
                onProductClick = { product ->
                    // Навигация на экран товара
                    // navController.navigate("product/${product.id}")
                },
                onBackClick = { navController.popBackStack() },
                onCategorySelected = { newCategoryName ->
                    // Навигация на другую категорию
                    navController.navigate("category/$newCategoryName") {
                        // Очищаем стек чтобы не было много экранов категорий
                        popUpTo("category/{categoryName}") { inclusive = true }
                    }
                }
            )
        }

    }
}