package com.klee.sapio.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.klee.sapio.R
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.ui.model.Rating.Companion.GREEN_CIRCLE_EMOJI
import com.klee.sapio.ui.model.Rating.Companion.RED_CIRCLE_EMOJI
import com.klee.sapio.ui.model.Rating.Companion.YELLOW_CIRCLE_EMOJI
import com.klee.sapio.ui.model.SharedEvaluation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
@Composable
fun ShareScreenshot(
    sharedEvaluation: SharedEvaluation,
) {
    Box(
        modifier = Modifier
            .requiredWidth(200.dp)
            .requiredHeight(115.dp)
            .background(color = Gray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Android Compatibility",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 8.5.sp
                        )
                    )
                    Text(
                        "without Google Play Services",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 5.sp
                        )
                    )
                }

                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd),
                    contentDescription = "Sapio icon",
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Blue200.copy(alpha = 0.18f),
                            shape = CircleShape
                        )
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = sharedEvaluation.icon.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(36.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        sharedEvaluation.name,
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp
                        )
                    )
                    Text(
                        sharedEvaluation.packageName,
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    RatingPill(
                        label = "microG",
                        rating = sharedEvaluation.ratingMicrog,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    RatingPill(
                        label = "bareAOSP",
                        rating = sharedEvaluation.ratingBareAOSP,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Sapio",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 5.5.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Text(
                    "Support privacy-focused Android apps!",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 4.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    ),
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 5.sp
                    ),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun RatingPill(
    label: String,
    rating: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp))
            .padding(start = 7.dp, end = 7.dp, top = 3.dp, bottom = 3.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 6.sp
            ),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = when (rating) {
                Rating.GOOD -> String(Character.toChars(GREEN_CIRCLE_EMOJI))
                Rating.AVERAGE -> String(Character.toChars(YELLOW_CIRCLE_EMOJI))
                Rating.BAD -> String(Character.toChars(RED_CIRCLE_EMOJI))
                else -> ""
            },
            style = TextStyle(
                color = Color.Unspecified,
                fontSize = 6.sp,
            ),
            modifier = Modifier.width(10.dp),
            textAlign = TextAlign.End,
        )
    }
}

const val WIDTH = 5
const val HEIGHT = 5

@Preview
@Composable
fun ShareScreenshotPreview() {
    val sharedEvaluation =
        SharedEvaluation(
            "My great app",
            "my.great.app",
            createBitmap(WIDTH, HEIGHT),
            1,
            2
        )
    ShareScreenshot(sharedEvaluation)
}
