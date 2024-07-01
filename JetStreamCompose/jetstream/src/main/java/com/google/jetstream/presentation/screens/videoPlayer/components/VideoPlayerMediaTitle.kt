/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.theme.JetStreamTheme

enum class VideoPlayerMediaTitleType { AD, LIVE, DEFAULT }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerMediaTitle(
    title: String,
    secondaryText: String,
    tertiaryText: String,
    classification: String,
    modifier: Modifier = Modifier,
    type: VideoPlayerMediaTitleType = VideoPlayerMediaTitleType.DEFAULT
) {
    val subTitle = buildString {
        append(secondaryText)
        if (secondaryText.isNotEmpty() && tertiaryText.isNotEmpty()) append(" • ")
        append(tertiaryText)
    }
    Column(modifier.fillMaxWidth()) {
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.height(35.dp)) {
            // TODO: Replaced with Badge component once developed
            when (type) {
                VideoPlayerMediaTitleType.AD -> {
                    Text(
                        text = stringResource(R.string.ad),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black,
                        modifier = Modifier
                            .background(Color(0xFFFBC02D), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .alignByBaseline()
                    )
                    Spacer(Modifier.width(8.dp))
                }

                VideoPlayerMediaTitleType.LIVE -> {
                    Text(
                        text = stringResource(R.string.live),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier
                            .background(Color(0xFFCC0000), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .alignByBaseline()
                    )

                    Spacer(Modifier.width(8.dp))
                }

                VideoPlayerMediaTitleType.DEFAULT -> {}
            }

            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(size = 3.dp),
                            brush = Brush.verticalGradient(
                                startY = Float.POSITIVE_INFINITY,
                                endY = 0f,
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White.copy(alpha = 0.25f),
                                )
                            )
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxHeight()
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        text = classification,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Preview(name = "TV Series", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewSeries() {
    JetStreamTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "True Detective",
                secondaryText = "S1E5",
                tertiaryText = "The Secret Fate Of All Life",
                classification = "MA15+",
                type = VideoPlayerMediaTitleType.DEFAULT
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Preview(name = "Live", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewLive() {
    JetStreamTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "MacLaren Reveal Their 2022 Car: The MCL36",
                secondaryText = "Formula 1",
                tertiaryText = "54K watching now",
                classification = "PG",
                type = VideoPlayerMediaTitleType.LIVE
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Preview(name = "Ads", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewAd() {
    JetStreamTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "Samsung Galaxy Note20 | Ultra 5G",
                secondaryText = "Get the most powerful Note yet",
                tertiaryText = "",
                classification = "PG",
                type = VideoPlayerMediaTitleType.AD
            )
        }
    }
}
