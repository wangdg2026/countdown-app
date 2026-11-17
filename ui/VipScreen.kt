package com.example.countdown.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.countdown.vm.RedeemResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipScreen(
    isVip: Boolean,
    vipExpire: Long,
    onActivate: (permanent: Boolean) -> Unit,
    onRedeem: suspend (String) -> RedeemResult,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var redeemMsg by remember { mutableStateOf<String?>(null) }
    var redeemIsError by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VIP") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("当前状态", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (isVip) {
                        Text(
                            if (vipExpire == Long.MAX_VALUE) "永久 VIP"
                            else "VIP 有效期至 ${fmt.format(Date(vipExpire))}",
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text("免费版（最多 5 个倒计时）")
                    }
                }
            }

            Text("解锁无限倒计时数量", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = {
                    onActivate(false)
                    dialogText = "模拟支付成功：按年 VIP 已激活"
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("按年订阅 · ¥1/年（模拟）") }

            Button(
                onClick = {
                    onActivate(true)
                    dialogText = "模拟支付成功：永久 VIP 已激活"
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("一次性买断 · ¥10 永久（模拟）") }

            HorizontalDivider()

            Text("兑换码", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("输入兑换码") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    scope.launch {
                        val result = onRedeem(code)
                        when (result) {
                            is RedeemResult.Success -> {
                                redeemMsg = result.message
                                redeemIsError = false
                            }
                            is RedeemResult.Invalid -> {
                                redeemMsg = result.message
                                redeemIsError = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("兑换") }
            redeemMsg?.let {
                Text(
                    it,
                    color = if (redeemIsError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            Text("邮件申请兑换码", style = MaterialTheme.typography.titleSmall)
            Text("联系邮箱：sdboygw@163.com", fontSize = 14.sp)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:sdboygw@163.com")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, "申请倒计时 VIP 兑换码")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "您好，我想申请倒计时 VIP 兑换码。\n" +
                                "类型：按年 / 永久（请选择）\n" +
                                "设备信息：${android.os.Build.MODEL}\n" +
                                "感谢！"
                        )
                    }
                    try {
                        ctx.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "未找到邮件应用，请手动发送至 sdboygw@163.com", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("发送邮件申请兑换码") }

            HorizontalDivider()

            Text(
                "兑换码规则：\n" +
                    "1. 按年 VIP 兑换码共 500 个，可循环使用；\n" +
                    "2. 同一兑换码使用后 10 天内不可再次使用；\n" +
                    "3. 永久 VIP 兑换码为对应按年 VIP 兑换码的倒序字符串；\n" +
                    "4. 后续可通过公众号关注回复「VIP」或「永久VIP」获取兑换码，敬请期待；\n" +
                    "5. 第一版为本地模拟解锁，未接入真实支付。",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("好的") }
            },
            title = { Text("提示") },
            text = { Text(dialogText) }
        )
    }
}