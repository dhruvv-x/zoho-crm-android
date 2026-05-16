// engine/list/ListSkeleton.kt
package com.pookie.octfis.engine.list

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

// ── Shimmer brush ─────────────────────────────────────────────────────────────
// Identical algorithm to DetailSkeleton — keep in sync if you ever change timing.

@Composable
private fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.08f),
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.08f),
    )
    val transition = rememberInfiniteTransition(label = "listShimmer")
    val translateAnim by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1000f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "listShimmerTranslate",
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start  = Offset(translateAnim - 200f, 0f),
        end    = Offset(translateAnim,         0f),
    )
}

// ── Primitive blocks ──────────────────────────────────────────────────────────

@Composable
private fun SkeletonBlock(
    width    : Dp,
    height   : Dp,
    modifier : Modifier = Modifier,
    shape    : androidx.compose.ui.graphics.Shape = RoundedCornerShape(6.dp),
    baseColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .background(baseColor)
            .drawBehind { drawRect(brush = brush) }
    )
}

@Composable
private fun SkeletonLine(
    width   : Dp,
    height  : Dp       = 14.dp,
    modifier: Modifier = Modifier,
) = SkeletonBlock(width = width, height = height, modifier = modifier)

// ── Single list-row skeleton ──────────────────────────────────────────────────
// Matches RecordListItem layout exactly:
//   [44 dp circle] [12 dp gap] [primary line + secondary line] [chevron ghost]

@Composable
private fun SkeletonListRow(secondaryWidth: Dp) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circle
        SkeletonBlock(
            width     = 44.dp,
            height    = 44.dp,
            shape     = CircleShape,
        )

        Spacer(Modifier.width(12.dp))

        // Text lines
        Column(modifier = Modifier.weight(1f)) {
            SkeletonLine(width = 160.dp, height = 15.dp)
            Spacer(Modifier.height(6.dp))
            SkeletonLine(width = secondaryWidth, height = 12.dp)
        }

        Spacer(Modifier.width(8.dp))

        // Chevron ghost
        SkeletonBlock(
            width  = 16.dp,
            height = 16.dp,
            shape  = RoundedCornerShape(3.dp),
        )
    }
}

// ── Divider ghost ─────────────────────────────────────────────────────────────

@Composable
private fun SkeletonDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp)   // matches HorizontalDivider indent in real list
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

// ── Public composable ─────────────────────────────────────────────────────────

/**
 * Full-page list skeleton shown while [RecordListScreen] is in Loading state.
 *
 * Renders [rowCount] shimmering rows that visually match [RecordListItem].
 * Secondary-line widths are varied across rows so the result looks organic.
 *
 * Drop-in replacement for the old `CircularProgressIndicator` in the Loading
 * branch of [RecordListScreen].
 */
@Composable
fun RecordListSkeleton(
    modifier : Modifier = Modifier,
    rowCount : Int      = 12,
) {
    // Width cycle for secondary lines — feels more natural than uniform widths.
    val secondaryWidths = listOf(100.dp, 120.dp, 80.dp, 140.dp, 95.dp, 110.dp)

    Column(modifier = modifier.fillMaxSize()) {
        repeat(rowCount) { index ->
            SkeletonListRow(secondaryWidth = secondaryWidths[index % secondaryWidths.size])
            if (index < rowCount - 1) {
                SkeletonDivider()
            }
        }
    }
}