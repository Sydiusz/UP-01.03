// screens/OnboardingScreen.kt
package com.example.shoeshop.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoeshop.R
import com.example.shoeshop.data.model.OnboardingSlide
import com.example.shoeshop.ui.components.OnboardButtun
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.theme.ShoeShopTheme
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardScreen(
    onGetStartedClick: () -> Unit
) {
    // Данные для слайдов
    val slides = listOf(
        OnboardingSlide(
            title = stringResource(id = R.string.welcome),
            subtitle = "",
            description = "",
            buttonText = stringResource(id = R.string.start),
            backgroundColor = 0xFF48B2E7,
            imageRes = R.drawable.image_1 // Добавьте ресурсы в res/drawable
        ),
        OnboardingSlide(
            title = stringResource(id = R.string.journey),
            subtitle = stringResource(id = R.string.smart),
            description = "",
            buttonText = stringResource(id = R.string.next),
            backgroundColor = 0xFF48B2E7,
            imageRes = R.drawable.image_2
        ),
        OnboardingSlide(
            title = stringResource(id = R.string.power),
            subtitle = stringResource(id = R.string.beatiful),
            description = "",
            buttonText = stringResource(id = R.string.next),
            backgroundColor = 0xFF48B2E7,
            imageRes = R.drawable.image_3
        )
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Слайды на весь экран
        HorizontalPager(
            count = slides.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingSlideItem(
                slide = slides[page],
                isFirstSlide = page == 0,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(290.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f), // Начинаем с более темного
                            Color.Black.copy(alpha = 0.6f), // Средняя часть темнее
                            Color.Black.copy(alpha = 0.7f)  // Самый низ - почти черный
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Индикаторы и кнопка поверх слайдов (внизу экрана)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Индикаторы точек
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(slides.size) { index ->
                    val size = animateDpAsState(
                        targetValue = if (pagerState.currentPage == index) 12.dp else 8.dp
                    )
                    val dotColor = if (pagerState.currentPage == index) {
                        Color.White
                    } else {
                        Color.White.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .size(size.value)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            // Кнопка
            OnboardButtun(
                onClick = {
                    if (pagerState.currentPage == slides.size - 1) {
                        // Последний слайд - переход к основному приложению
                        onGetStartedClick()
                    } else {
                        // Переход к следующему слайду
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                text = slides[pagerState.currentPage].buttonText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingSlideItem(
    slide: OnboardingSlide,
    isFirstSlide: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(slide.backgroundColor))
            .fillMaxSize()
            .padding(top = if (isFirstSlide) 60.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isFirstSlide) {
            Arrangement.Top
        } else {
            Arrangement.Center
        }
    ) {
        if (isFirstSlide) {
            // Для первого слайда: заголовок сверху
            FirstSlideContent(slide)
        } else {
            // Для остальных слайдов: картинка сверху, текст в центре
            OtherSlidesContent(slide)
        }
    }
}

@Composable
private fun FirstSlideContent(slide: OnboardingSlide) {
    // Заголовок вверху
    Text(
        text = slide.title,
        style = AppTypography.headingRegular32.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 40.dp)
    )

    // Картинка под заголовком
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {

            Image(
                painter = painterResource(id = slide.imageRes),
                contentDescription = slide.title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )

    }
}

@Composable
private fun OtherSlidesContent(slide: OnboardingSlide) {
    // Картинка вверху
    Box(
        modifier = Modifier
            .padding(bottom = 40.dp)
            .height(250.dp)
            .fillMaxWidth(0.8f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = slide.imageRes),
            contentDescription = slide.title,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentScale = ContentScale.Fit
        )
    }

    // Заголовок
    Text(
        text = slide.title,
        style = AppTypography.headingRegular32.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
    )

    // Подзаголовок
    if (slide.subtitle.isNotEmpty()) {
        Text(
            text = slide.subtitle,
            style = AppTypography.subtitleRegular16.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(bottom = 16.dp)
        )
    }

    // Описание/призыв к действию
    if (slide.description.isNotEmpty()) {
        Text(
            text = slide.description,
            style = AppTypography.bodyMedium16.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardScreenPreview() {
    ShoeShopTheme {
        OnboardScreen(
            onGetStartedClick = {}
        )
    }
}