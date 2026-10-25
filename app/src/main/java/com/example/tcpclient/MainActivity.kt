package com.example.tcpclient

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TcpClientTheme {
                TcpClientScreen()
            }
        }
    }
}

@Composable
fun TcpClientScreen() {
    var serverIp by remember { mutableStateOf("10.0.2.2") }
    var serverPort by remember { mutableStateOf("9999") }
    var message by remember { mutableStateOf("Halo dari Android") }
    var status by remember { mutableStateOf("Belum terhubung") }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TCP Client Android",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = serverIp,
                onValueChange = { serverIp = it },
                label = { Text("IP Server") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Pesan") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val port = serverPort.toInt()
                            val socket = Socket(serverIp, port)
                            val output = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)
                            val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                            output.println(message)
                            status = "Terkirim: $message"

                            val response = input.readLine()
                            if (response != null) {
                                status = "Balasan: $response"
                            }

                            output.close()
                            input.close()
                            socket.close()
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kirim ke Server")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
