package com.example.sudoku.multiplayer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sudoku.multiplayer.NearbyGameManager

@Composable
fun MultiplayerMatchmakingScreen(
    nearbyGameManager: NearbyGameManager,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onGameStart: () -> Unit
) {
    val connectionState by nearbyGameManager.connectionState.collectAsState()

    LaunchedEffect(connectionState) {
        if (connectionState is NearbyGameManager.ConnectionState.Connected) {
            onGameStart()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasPermissions) {
            Text(
                "Nearby Multiplayer requires permissions",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermissions) {
                Text("Grant Permissions")
            }
        } else {
            when (connectionState) {
                is NearbyGameManager.ConnectionState.Disconnected -> {
                    Text(
                        "Zero-Server Duel Matchmaking",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { nearbyGameManager.startAdvertising() }) {
                        Text("Host Game")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { nearbyGameManager.startDiscovery() }) {
                        Text("Find Game")
                    }
                }
                is NearbyGameManager.ConnectionState.Advertising -> {
                    Text("Hosting Game...")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { nearbyGameManager.stopAdvertising() }) {
                        Text("Cancel")
                    }
                }
                is NearbyGameManager.ConnectionState.Discovering -> {
                    Text("Finding Game...")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { nearbyGameManager.stopDiscovery() }) {
                        Text("Cancel")
                    }
                }
                is NearbyGameManager.ConnectionState.Connected -> {
                    Text("Connected! Starting game...")
                }
            }
        }
    }
}
