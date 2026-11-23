package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PhantomGreen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusStopped
import com.example.ui.theme.StatusWarning

fun getCountryFlagEmoji(countryCode: String): String {
  if (countryCode.equals("DIRECT", ignoreCase = true) || countryCode.isBlank()) {
    return "🌐"
  }
  val upper = countryCode.uppercase().take(2)
  if (upper.length != 2) return "🌐"
  val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
  val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
  return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

@Composable
fun OsIconBadge(
  os: String,
  modifier: Modifier = Modifier,
  tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
  val icon: ImageVector = when (os.lowercase()) {
    "windows" -> Icons.Default.Computer
    "macos" -> Icons.Default.Laptop
    "linux" -> Icons.Default.Terminal
    "ios" -> Icons.Default.PhoneIphone
    else -> Icons.Default.Android
  }
  Icon(
    imageVector = icon,
    contentDescription = "$os OS",
    tint = tint,
    modifier = modifier.size(18.dp)
  )
}

@Composable
fun StatusBadge(
  isRunning: Boolean,
  modifier: Modifier = Modifier
) {
  val bg = if (isRunning) StatusActive.copy(alpha = 0.15f) else StatusStopped.copy(alpha = 0.15f)
  val textColor = if (isRunning) StatusActive else StatusStopped
  val dotColor = if (isRunning) StatusActive else StatusStopped
  val label = if (isRunning) "ACTIVE" else "STOPPED"

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = bg,
    border = androidx.compose.foundation.BorderStroke(1.dp, dotColor.copy(alpha = 0.3f)),
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(dotColor)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = label,
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
fun RiskScoreBadge(
  riskScore: Float,
  riskLabel: String,
  modifier: Modifier = Modifier
) {
  val (color, icon) = when {
    riskScore <= 0.20f -> Pair(StatusActive, Icons.Default.CheckCircle)
    riskScore <= 0.50f -> Pair(StatusWarning, Icons.Default.WarningAmber)
    else -> Pair(StatusError, Icons.Default.ErrorOutline)
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = color.copy(alpha = 0.12f),
    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = modifier
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = riskLabel,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun AcceptableUseNoticeDialog(
  onAccept: () -> Unit
) {
  AlertDialog(
    onDismissRequest = {},
    icon = {
      Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = PhantomGreen,
        modifier = Modifier.size(36.dp)
      )
    },
    title = {
      Text(
        text = "Acceptable Use Notice",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Text(
        text = "For privacy, testing, and legitimate multi-account management only.\n\n" +
          "PhantomProfile provides sandboxed browser isolation, fingerprint customization, and proxy routing strictly for legitimate developers, privacy advocates, QA testers, and authorized multi-identity workflows.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    confirmButton = {
      Button(
        onClick = onAccept,
        colors = ButtonDefaults.buttonColors(containerColor = PhantomGreen),
        modifier = Modifier.testTag("accept_terms_button")
      ) {
        Text("I Understand & Accept", color = Color.White)
      }
    }
  )
}
