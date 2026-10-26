package com.bahri.ryfy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.ryfy.RyFyTab
import com.bahri.ryfy.core.RyFyStrings
import com.bahri.ryfy.ui.theme.GlassBaseColor
import com.bahri.ryfy.ui.theme.GlassBorderActiveBrush
import com.bahri.ryfy.ui.theme.GlassBorderBrush
import com.bahri.ryfy.ui.theme.GlassHighlightColor
import com.bahri.ryfy.ui.theme.SpotifyGreen
import com.bahri.ryfy.ui.theme.SpotifyText
import com.bahri.ryfy.ui.theme.SpotifyTextSecondary
import com.bahri.ryfy.ui.theme.liquidGlass

@Composable
fun LiquidGlassBottomBar(
    selectedTab: RyFyTab,
    onTabSelected: (RyFyTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .liquidGlass(
                shape = RoundedCornerShape(28.dp),
                backgroundColor = GlassBaseColor,
                borderWidth = 1.dp,
                borderBrush = GlassBorderBrush
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RyFyTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val tabLabel = when (tab) {
                    RyFyTab.HOME -> RyFyStrings.get(RyFyStrings.Key.NAV_HOME)
                    RyFyTab.SEARCH -> RyFyStrings.get(RyFyStrings.Key.NAV_SEARCH)
                    RyFyTab.LIBRARY -> RyFyStrings.get(RyFyStrings.Key.NAV_LIBRARY)
                    RyFyTab.SETTINGS -> RyFyStrings.get(RyFyStrings.Key.NAV_SETTINGS)
                }

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "tab_scale"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) SpotifyGreen else SpotifyTextSecondary,
                    label = "tab_icon_color"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) SpotifyText else SpotifyTextSecondary,
                    label = "tab_text_color"
                )

                val tabModifier = if (isSelected) {
                    Modifier.liquidGlass(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = GlassHighlightColor,
                        borderWidth = 0.75.dp,
                        borderBrush = GlassBorderActiveBrush
                    )
                } else {
                    Modifier
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp, vertical = 2.dp)
                        .then(tabModifier)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTabSelected(tab)
                            }
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.scale(scale)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tabLabel,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = tabLabel,
                            color = textColor,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
