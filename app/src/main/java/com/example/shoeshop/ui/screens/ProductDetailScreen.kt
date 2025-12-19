// ui/screens/ProductDetailScreen.kt
package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shoeshop.R
import com.example.shoeshop.data.model.Product
import com.example.shoeshop.ui.components.ProductImage  // ← НОВЫЙ ИМПОРТ
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.ProductDetailViewModel
import com.example.shoeshop.util.ImageConfig
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavoriteInHome: (Product) -> Unit
) {
    val viewModel: ProductDetailViewModel = viewModel()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val related by viewModel.related.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()

    val currentProduct: Product? = when {
        related.isNotEmpty() && selectedIndex in related.indices -> related[selectedIndex]
        else -> product
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "",
                        style = AppTypography.bodyMedium16
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* переход в корзину */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bag_2),
                            contentDescription = "Корзина",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Ошибка", color = Color.Red)
                        Text(text = error ?: "")
                        Button(onClick = { viewModel.loadProduct(productId) }) {
                            Text(text = "Повторить")
                        }
                    }
                }

                product != null -> {
                    currentProduct?.let { shown ->
                        key(shown.id) {                      // ← добавили
                            ProductDetailContent(
                                product = shown,
                                related = related,
                                selectedIndex = selectedIndex,
                                onSelectRelated = { index -> viewModel.selectRelated(index) },
                                onAddToCart = onAddToCart,
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(shown)
                                    onToggleFavoriteInHome(shown)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Товар не найден")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductDetailContent(
    product: Product,
    related: List<Product>,
    selectedIndex: Int,
    onSelectRelated: (Int) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val categoryText = product.displayCategory
        ?: product.categoryId
        ?: "Категория не указана"

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { maxOf(1, related.size) }
    )

    // когда выбрали товар по мини‑карточке — пролистываем pager
    LaunchedEffect(selectedIndex) {
        if (related.isNotEmpty() && selectedIndex in related.indices) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    // когда пролистали pager свайпом — сообщаем наружу
    LaunchedEffect(pagerState.currentPage) {
        if (related.isNotEmpty()
            && pagerState.currentPage in related.indices
            && pagerState.currentPage != selectedIndex
        ) {
            onSelectRelated(pagerState.currentPage)
        }
    }

    Column(
        modifier = modifier
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // 1. Название, категория, цена
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = product.name,
                style = AppTypography.headingRegular26.copy(color = Color.Black),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = categoryText,
                style = AppTypography.bodyRegular16.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = product.getFormattedPrice(),
                style = AppTypography.bodyRegular24.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // 2. Пейджер: блок с фото, который свайпается
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) { page ->
            val current = when {
                related.isNotEmpty() && page in related.indices -> related[page]
                else -> product
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.under_product),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.FillWidth
                )

                ProductImage(
                    productId = current.id,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-40).dp)
                )
            }
        }

        // 3. Мини‑карточки той же категории
        if (related.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                related.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onSelectRelated(index)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ProductImage(
                            productId = item.id,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // 4. Описание
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = product.description,
                style = AppTypography.bodyRegular16.copy(color = Color.Black),
                lineHeight = 24.sp,
                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (product.description.lines().size > 3 || product.description.length > 150) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { isDescriptionExpanded = !isDescriptionExpanded }
                    ) {
                        Text(
                            text = if (isDescriptionExpanded)
                                stringResource(id = R.string.hide)
                            else
                                stringResource(id = R.string.more_details),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 5. Нижняя панель – как была у тебя
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onToggleFavorite() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Избранное",
                        tint = if (product.isFavorite) Color.Red else Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Button(
                    onClick = { onAddToCart(product) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bag_2),
                            contentDescription = "В корзину",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "Добавить в корзину",
                            style = AppTypography.bodyMedium16
                        )
                    }
                }
            }
        }
    }
}



