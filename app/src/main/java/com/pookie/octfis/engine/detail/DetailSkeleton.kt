// engine/detail/DetailSkeleton.kt
package com.pookie.octfis.engine.detail

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pookie.octfis.ui.theme.CrmPrimary

// ── Shimmer brush ─────────────────────────────────────────────────────────────

@Composable
private fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.08f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1000f,
        animationSpec  = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start  = Offset(translateAnim - 200f, 0f),
        end    = Offset(translateAnim,        0f),
    )
}

// ── Skeleton block helpers ────────────────────────────────────────────────────

@Composable
private fun SkeletonBlock(
    width    : Dp,
    height   : Dp,
    modifier : Modifier  = Modifier,
    shape    : androidx.compose.ui.graphics.Shape = RoundedCornerShape(6.dp),
    baseColor: Color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .background(baseColor)
            .drawBehind {
                drawRect(brush = brush)
            }
    )
}

@Composable
private fun SkeletonLine(
    width   : Dp,
    height  : Dp    = 14.dp,
    modifier: Modifier = Modifier,
) = SkeletonBlock(width = width, height = height, modifier = modifier)

// ── Public skeleton composable ────────────────────────────────────────────────

/**
 * Full-page skeleton shown while RecordDetailScreen is in Loading state.
 * Mirrors the real layout: amber avatar header + two field section cards.
 */
@Composable
fun RecordDetailSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(
            rememberScrollState(),
            enabled = false,          // non-interactive while loading
        ),
    ) {

        // ── Avatar header skeleton ────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color    = CrmPrimary,
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Avatar circle
                SkeletonBlock(
                    width     = 72.dp,
                    height    = 72.dp,
                    shape     = CircleShape,
                    baseColor = Color.White.copy(alpha = 0.20f),
                )
                Spacer(Modifier.height(14.dp))
                // Title line
                SkeletonBlock(
                    width     = 180.dp,
                    height    = 20.dp,
                    baseColor = Color.White.copy(alpha = 0.20f),
                )
                Spacer(Modifier.height(8.dp))
                // Subtitle line
                SkeletonBlock(
                    width     = 100.dp,
                    height    = 13.dp,
                    baseColor = Color.White.copy(alpha = 0.15f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Section label skeleton ────────────────────────────────────────────
        SkeletonLine(
            width    = 80.dp,
            height   = 12.dp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        )

        // ── Field card skeleton ───────────────────────────────────────────────
        SkeletonCard(rowCount = 6)

        Spacer(Modifier.height(16.dp))

        SkeletonLine(
            width    = 80.dp,
            height   = 12.dp,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        )

        SkeletonCard(rowCount = 3)

        Spacer(Modifier.height(88.dp))
    }
}

@Composable
private fun SkeletonCard(rowCount: Int) {
    Surface(
        modifier       = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape          = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
    ) {
        Column {
            repeat(rowCount) { index ->
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Label placeholder
                    SkeletonLine(width = 90.dp)
                    Spacer(Modifier.width(16.dp))
                    // Value placeholder — varied widths look natural
                    val valueWidth = when (index % 3) {
                        0    -> 140.dp
                        1    -> 100.dp
                        else -> 120.dp
                    }
                    SkeletonLine(width = valueWidth)
                }
                if (index < rowCount - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                }
            }
        }
    }
}