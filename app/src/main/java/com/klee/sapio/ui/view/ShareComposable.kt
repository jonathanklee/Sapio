package com.klee.sapio.ui.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.klee.sapio.R
import com.klee.sapio.data.Rating
import com.klee.sapio.data.Rating.Companion.GREEN_CIRCLE_EMOJI
import com.klee.sapio.data.Rating.Companion.RED_CIRCLE_EMOJI
import com.klee.sapio.data.Rating.Companion.YELLOW_CIRCLE_EMOJI
import com.klee.sapio.data.SharedEvaluation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
@Composable
fun ShareScreenshot(
    sharedEvaluation: SharedEvaluation,
) {
    Column(
        modifier = Modifier
            .requiredWidth(200.dp)
            .requiredHeight(115.dp)
            .background(color = Gray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Android Compatibility",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 7.sp
                ),
                modifier = Modifier
                    .padding(start = 15.dp)
            )

            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .padding(start = 70.dp)
                    .size(40.dp),
                contentDescription = "Sapio icon",
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = sharedEvaluation.icon.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 15.dp)
                    .clip(CircleShape)
                    .size(40.dp)
                    .align(Alignment.CenterVertically),
            )

            Column(
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Text(
                    sharedEvaluation.name,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(2.dp)
                )
                Text(
                    sharedEvaluation.packageName,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 6.sp
                    ),
                    modifier = Modifier.padding(3.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        " microG ",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 8.sp
                        ),
                        modifier = Modifier
                            .width(45.dp)
                            .padding(2.dp)
                            .background(color = Blue200)
                    )

                    Text(
                        text = when (sharedEvaluation.ratingMicrog) {
                            Rating.GOOD -> String(Character.toChars(GREEN_CIRCLE_EMOJI))
                            Rating.AVERAGE -> String(Character.toChars(YELLOW_CIRCLE_EMOJI))
                            Rating.BAD -> String(Character.toChars(RED_CIRCLE_EMOJI))
                            else -> ""
                        },
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 8.sp,
                        ),
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        " bareAOSP ",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 8.sp
                        ),
                        modifier = Modifier
                            .width(45.dp)
                            .padding(2.dp)
                            .background(color = Blue700)
                    )

                    Text(
                        text = when (sharedEvaluation.ratingBareAOSP) {
                            Rating.GOOD -> String(Character.toChars(GREEN_CIRCLE_EMOJI))
                            Rating.AVERAGE -> String(Character.toChars(YELLOW_CIRCLE_EMOJI))
                            Rating.BAD -> String(Character.toChars(RED_CIRCLE_EMOJI))
                            else -> ""
                        },
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 8.sp,
                        ),
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        }

        Text(
            LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
            ),
            style = TextStyle(
                color = Color.White,
                fontSize = 5.sp
            ),
            modifier = Modifier
                .padding(top = 5.dp, end = 15.dp)
                .align(Alignment.End)
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
