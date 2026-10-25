package com.bahri.ryfy.ui.dialogs

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.core.AppLanguageManager
import com.bahri.ryfy.ui.theme.SpotifyElevated
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyElevated,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Pilih Bahasa / Language",
                fontWeight = FontWeight.Bold,
                color = SpotifyText,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppLanguageManager.supportedLanguages.forEach { option ->
                    val isSelected = option.code.equals(currentLanguageCode, ignoreCase = true)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SpotifyGreen.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                if (!isSelected) {
                                    AppLanguageManager.setLanguage(context, option.code, activity)
                                }
                                onDismiss()
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    AppLanguageManager.setLanguage(context, option.code, activity)
                                }
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = SpotifyGreen,
                                unselectedColor = SpotifyTextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.nativeName,
                                color = if (isSelected) SpotifyGreen else SpotifyText,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (option.nativeName != option.englishName && option.code != AppLanguageManager.LANG_SYSTEM) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = option.englishName,
                                    color = SpotifyTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    color = SpotifyGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
