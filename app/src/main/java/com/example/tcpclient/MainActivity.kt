package com.example.tcpclient

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tcpclient.ui.theme.TcpClientTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException

class MainActivity : ComponentActivity() {
    private val serverIp = "203.0.113.10"
    private val serverPort = 9999
    private val token = "mysecret123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TcpClientTheme {
                RemoteControlScreen()
            }
        }
    }

    @Composable
    fun RemoteControlScreen() {
        var status by remember { mutableStateOf("Belum terhubung") }
        var log by remember { mutableStateOf("Log siap") }

        val scope = rememberCoroutineScope()

        fun connectAndSend(command: String) {
            scope.launch(Dispatchers.IO) {
                try {
                    val socket = Socket(serverIp, serverPort)
                    val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    writer.println("AUTH:$token")
                    val authResponse = reader.readLine()
                    if (authResponse == null || authResponse != "AUTH_OK") {
                        status = "Autentikasi gagal"
                        socket.close()
                        return@launch
                    }

                    writer.println("CMD:$command")
                    val response = reader.readLine()
                    if (response != null) {
                        status = "Server: $response"
                        when {
                            response.startsWith("ACK:TOAST|") -> {
                                val text = response.substringAfter("ACK:TOAST|")
                                runOnUiThread { log = "Toast: $text" }
                            }
                            response.startsWith("ACK:URL|") -> {
                                val url = response.substringAfter("ACK:URL|")
                                runOnUiThread {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    startActivity(intent)
                                }
                            }
                            response == "ACK:VIBRATE" -> {
                                val vibrator = getSystemService(Vibrator::class.java)
                                vibrator?.vibrate(500)
                                runOnUiThread { log = "Vibration triggered" }
                            }
                            response.startsWith("BATTERY:") -> {
                                val battery = response.substringAfter("BATTERY:")
                                runOnUiThread { log = "Battery: $battery" }
                            }
                            response.startsWith("GPS:") -> {
                                val gps = response.substringAfter("GPS:")
                                runOnUiThread { log = "GPS: $gps" }
                            }
                            response.startsWith("DEVICE_INFO:") -> {
                                val info = response.substringAfter("DEVICE_INFO:")
                                runOnUiThread { log = info }
                            }
                            response.startsWith("STATUS:") -> {
                                val info = response.substringAfter("STATUS:")
                                runOnUiThread { log = info }
                            }
                            response.startsWith("ERR:") -> {
                                runOnUiThread { log = response }
                            }
                        }
                    }

                    writer.close()
                    reader.close()
                    socket.close()
                } catch (e: SocketException) {
                    status = "Socket error: ${e.message}"
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Remote Control Client",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = serverIp,
                onValueChange = {},
                enabled = false,
                label = { Text("Server IP") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = serverPort.toString(),
                onValueChange = {},
                enabled = false,
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = token,
                onValueChange = {},
                enabled = false,
                label = { Text("Token") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { connectAndSend("INFO") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Get Device Info")
                }
                Button(onClick = { connectAndSend("PING") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ping")
                }
                Button(onClick = { connectAndSend("BATTERY") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Battery")
                }
                Button(onClick = { connectAndSend("GPS") }, modifier = Modifier.fillMaxWidth()) {
                    Text("GPS")
                }
                Button(onClick = { connectAndSend("VIBRATE") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Vibrate")
                }
                Button(onClick = { connectAndSend("TOAST|Halo dari server") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Show Toast")
                }
                Button(onClick = { connectAndSend("URL|https://google.com") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Open URL")
                }
                Button(onClick = { connectAndSend("FLASH_ON") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Flash On")
                }
                Button(onClick = { connectAndSend("FLASH_OFF") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Flash Off")
                }
                Button(onClick = { connectAndSend("SCREEN_ON") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Screen On")
                }
                Button(onClick = { connectAndSend("SCREEN_OFF") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Screen Off")
                }
                Button(onClick = { connectAndSend("REBOOT") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Reboot")
                }
                Button(onClick = { connectAndSend("GET_STATUS") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Get Status")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(status)
            Text(log)
        }
    }
}
