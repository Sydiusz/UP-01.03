package com.example.shoeshop.data.navigation

import EmailVerificationScreen
import RecoveryVerificationScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shoeshop.ui.screens.CategoryProductsScreen
import com.example.shoeshop.ui.screens.CreateNewPasswordScreen
import com.example.shoeshop.ui.screens.FavoritesScreen
import com.example.shoeshop.ui.screens.ForgotPasswordScreen
import com.example.shoeshop.ui.screens.HomeScreen
import com.example.shoeshop.ui.screens.OnboardScreen
import com.example.shoeshop.ui.screens.ProductDetailScreen
import com.example.shoeshop.ui.screens.RegisterAccountScreen
import com.example.shoeshop.ui.screens.SignInScreen
import com.example.shoeshop.ui.viewmodel.HomeViewModel
import com.example.shoeshop.util.saveUserEmail

@Composable
fun NavigationApp(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "start_menu"
    ) {
        composable("sign_up") {
            RegisterAccountScreen(
                onSignInClick = { navController.navigate("sign_in") },
                onSignUpClick = { email ->
                    // передаём email как аргумент
                    navController.navigate("email_verification/$email")
                }
            )
        }
        composable("sign_in") {
            SignInScreen(
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onSignInClick = { navController.navigate("home") },
                onSignUpClick = { navController.navigate("sign_up") }
            )
        }

        composable("reset_password") {
            RecoveryVerificationScreen(
                onSignInClick = { navController.navigate("sign_in") },
                onResetPasswordClick = { accessToken ->
                    navController.navigate("create_new_password/$accessToken")
                }
            )
        }

        composable("forgot_password") { navBackStackEntry ->
            val context = LocalContext.current

            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToOtpVerification = { email ->
                    saveUserEmail(context, email)
                    navController.navigate("reset_password")
                }
            )
        }

        composable("start_menu") {
            OnboardScreen(
                onGetStartedClick = { navController.navigate("sign_up") },
            )
        }

        // home
        composable("home") { backStackEntry ->
            val homeViewModel: HomeViewModel = viewModel(backStackEntry)
            HomeScreen(
                homeViewModel = homeViewModel,
                onProductClick = { product ->
                    navController.navigate("product/${product.id}")
                },
                onCartClick = { /* ... */ },
                onSearchClick = { /* ... */ },
                onSettingsClick = { },
                onCategoryClick = { categoryName ->
                    navController.navigate("category/$categoryName")
                }
            )
        }

        composable(
            route = "email_verification/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val emailArg = backStackEntry.arguments?.getString("email") ?: ""

            EmailVerificationScreen(
                email = emailArg,
                onSignInClick = { navController.navigate("sign_in") },
                onVerificationSuccess = { navController.navigate("home") }
            )
        }

        composable(
            route = "category/{categoryName}",
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""

            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry)

            CategoryProductsScreen(
                homeViewModel = homeViewModel,
                categoryName = categoryName,
                onProductClick = { product ->
                    navController.navigate("product/${product.id}")
                },
                onBackClick = { navController.popBackStack() },
                onCategorySelected = { newCategoryName ->
                    navController.navigate("category/$newCategoryName") {
                        popUpTo("category/{categoryName}") { inclusive = true }
                    }
                }
            )
        }

        // product
        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry)

            ProductDetailScreen(
                productId = productId,
                onBackClick = { navController.popBackStack() },
                onAddToCart = { /* TODO */ },
                onToggleFavoriteInHome = { product ->
                    homeViewModel.toggleFavorite(product)
                }
            )
        }

        composable(
            "create_new_password/{token}",
            arguments = listOf(navArgument("token") { type = NavType.StringType })
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")

            CreateNewPasswordScreen(
                userToken = token,
                onPasswordChanged = {
                    navController.navigate("sign_in") {
                        popUpTo("sign_in") { inclusive = false }
                    }
                }
            )
        }



    }
}
