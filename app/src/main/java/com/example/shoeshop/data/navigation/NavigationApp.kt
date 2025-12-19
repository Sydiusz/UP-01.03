package com.example.shoeshop.data.navigation

import EmailVerificationScreen
import RecoveryVerificationScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shoeshop.ui.screens.CartScreen
import com.example.shoeshop.ui.screens.CategoryProductsScreen
import com.example.shoeshop.ui.screens.CheckoutScreen
import com.example.shoeshop.ui.screens.CreateNewPasswordScreen
import com.example.shoeshop.ui.screens.FavoritesScreen
import com.example.shoeshop.ui.screens.ForgotPasswordScreen
import com.example.shoeshop.ui.screens.HomeScreen
import com.example.shoeshop.ui.screens.OnboardScreen
import com.example.shoeshop.ui.screens.OrderDetailsScreen
import com.example.shoeshop.ui.screens.ProductDetailScreen
import com.example.shoeshop.ui.screens.RegisterAccountScreen
import com.example.shoeshop.ui.screens.SignInScreen
import com.example.shoeshop.ui.viewmodel.CartViewModel
import com.example.shoeshop.ui.viewmodel.CheckoutViewModel
import com.example.shoeshop.ui.viewmodel.HomeViewModel
import com.example.shoeshop.ui.viewmodel.ProfileViewModel
import com.example.shoeshop.util.getUserEmail
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
        composable("cart") { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry)
            val cartViewModel: CartViewModel = viewModel(backStackEntry)

            val state by cartViewModel.uiState.collectAsStateWithLifecycle()

            CartScreen(
                items = state.items,
                isLoading = state.isLoading,
                onBackClick = { navController.popBackStack() },
                onIncrement = { item ->
                    cartViewModel.increment(item)
                    homeViewModel.refreshCartFlags()
                },
                onDecrement = { item ->
                    cartViewModel.decrement(item)
                    homeViewModel.refreshCartFlags()
                },
                onRemove = { item ->
                    cartViewModel.remove(item)
                    homeViewModel.refreshCartFlags()
                },
                onCheckoutClick = {
                    navController.navigate("checkout")
                }
            )
        }

        composable("checkout") { backStackEntry ->
            val homeBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }

            val cartViewModel: CartViewModel = viewModel(backStackEntry)
            val checkoutViewModel: CheckoutViewModel = viewModel(backStackEntry)
            val profileViewModel: ProfileViewModel = viewModel(homeBackStackEntry)
            val homeViewModel: HomeViewModel = viewModel(homeBackStackEntry)

            val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                profileViewModel.loadProfile()
            }

            LaunchedEffect(profileState.profile) {
                val profile = profileState.profile
                val emailFromPrefs = com.example.shoeshop.util.getUserEmail(context) ?: ""
                android.util.Log.d(
                    "CheckoutNav",
                    "updateFromProfile email=$emailFromPrefs, phone=${profile?.phone}, addr=${profile?.address}"
                )

                checkoutViewModel.updateFromProfile(
                    email = emailFromPrefs,
                    phone = profile?.phone ?: "",
                    address = profile?.address ?: ""
                )
            }

            CheckoutScreen(
                cartViewModel = cartViewModel,
                checkoutViewModel = checkoutViewModel,
                onBackClick = { navController.popBackStack() },
                onOrderCreated = {
                    // сообщаем home, что корзина изменилась
                    homeViewModel.refreshCartFlags()
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }


        composable("start_menu") {
            OnboardScreen(
                onGetStartedClick = { navController.navigate("sign_up") },
            )
        }
        composable("forgot_password") { navBackStackEntry ->
            val context = LocalContext.current

            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToOtpVerification = { email ->
                    // сохраняем email в SharedPreferences для дальнейшего использования
                    saveUserEmail(context, email)
                    navController.navigate("reset_password")
                }
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
                onCartClick = { navController.navigate("cart") },
                onSearchClick = { /* ... */ },
                onSettingsClick = { },
                onCategoryClick = { categoryName ->
                    navController.navigate("category/$categoryName")
                },
                onOrderClick = { orderId ->
                    navController.navigate("order_details/$orderId")
                },
                initialTab = 0
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
                onAddToCart = { product ->
                    homeViewModel.toggleCart(product)      // ← обновляем флаг isInCart на Home
                },
                onToggleFavoriteInHome = { product ->
                    homeViewModel.toggleFavorite(product)  // уже было
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
        composable(
            route = "order_details/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            OrderDetailsScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() } // важно: без navigate("home")
            )
        }



    }
}
