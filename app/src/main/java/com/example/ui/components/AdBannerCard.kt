package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdConfig
import com.example.ui.theme.DramaBorder
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant

@Composable
fun AdBannerCard(
  adConfig: AdConfig,
  modifier: Modifier = Modifier,
  onAdClick: (() -> Unit)? = null
) {
  if (!adConfig.isBannerEnabled) return

  val context = LocalContext.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(DramaCardBg)
      .border(1.dp, DramaBorder, RoundedCornerShape(10.dp))
      .clickable {
        onAdClick?.invoke()
        runCatching {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(adConfig.bannerAdActionUrl))
          context.startActivity(intent)
        }
      }
      .testTag("adsense_banner_card")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(DramaRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (adConfig.isAdSenseEnabled) "ADSENSE AD" else "SPONSORED",
              color = DramaRed,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = "Slot #${adConfig.bannerSlotId}",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
          )
        }

        Icon(
          imageVector = Icons.Default.Campaign,
          contentDescription = "Ad Provider",
          tint = Color.White.copy(alpha = 0.6f),
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = adConfig.bannerAdTitle,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = adConfig.bannerAdDescription,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 12.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 2.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (adConfig.isAdSenseEnabled) "adsbygoogle.js verified" else "Custom campaign active",
          color = Color(0xFF4CAF50),
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(DramaSurfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Visit Sponsor",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }
  }
}
