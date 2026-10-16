package com.example.sudoku.multiplayer

import android.content.Context
import android.util.Log
import com.example.sudoku.multiplayer.payload.GamePayload
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NearbyGameManager(private val context: Context) {

    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _opponentPayload = MutableStateFlow<GamePayload?>(null)
    val opponentPayload: StateFlow<GamePayload?> = _opponentPayload.asStateFlow()

    private var currentEndpointId: String? = null
    private var isHost: Boolean = false

    companion object {
        private const val TAG = "NearbyGameManager"
        private const val SERVICE_ID = "com.example.sudoku.multiplayer"
        private val STRATEGY = Strategy.P2P_POINT_TO_POINT
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    // Defense-in-depth: limit payload size to 32KB to prevent OOM/DoS
                    if (bytes.size > 32768) {
                        Log.w(TAG, "Payload size (${bytes.size} bytes) exceeds 32KB limit, dropping")
                        return@let
                    }
                    val gamePayload = GamePayload.fromByteArray(bytes)
                    if (gamePayload != null) {
                        _opponentPayload.value = gamePayload
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with: \${info.endpointName}")

            // Reject incoming connections if we are already connected to prevent session hijacking
            if (currentEndpointId != null) {
                Log.w(TAG, "Rejecting connection from $endpointId, already connected")
                connectionsClient.rejectConnection(endpointId)
                return
            }

            // Automatically accept connection for simplicity in a game
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.d(TAG, "Connection successful: \$endpointId")
                currentEndpointId = endpointId
                _connectionState.update { ConnectionState.Connected(endpointId) }
                stopDiscovery()
                stopAdvertising()
            } else {
                Log.e(TAG, "Connection failed: \${result.status.statusMessage}")
                _connectionState.update { ConnectionState.Disconnected }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from: \$endpointId")
            currentEndpointId = null
            _connectionState.update { ConnectionState.Disconnected }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found: \${info.endpointName} (\$endpointId)")
            _connectionState.update { ConnectionState.Discovering(endpointId) }
            connectionsClient.requestConnection("SudokuPlayer", endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Endpoint lost: \$endpointId")
        }
    }

    fun startAdvertising() {
        isHost = true
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            "SudokuHost",
            SERVICE_ID,
            connectionLifecycleCallback,
            options
        ).addOnSuccessListener {
            Log.d(TAG, "Advertising started")
            _connectionState.update { ConnectionState.Advertising }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Advertising failed", e)
            _connectionState.update { ConnectionState.Disconnected }
        }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        if (_connectionState.value is ConnectionState.Advertising) {
             _connectionState.update { ConnectionState.Disconnected }
        }
    }

    fun startDiscovery() {
        isHost = false
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            options
        ).addOnSuccessListener {
            Log.d(TAG, "Discovery started")
            _connectionState.update { ConnectionState.Discovering(null) }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Discovery failed", e)
             _connectionState.update { ConnectionState.Disconnected }
        }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        if (_connectionState.value is ConnectionState.Discovering) {
             _connectionState.update { ConnectionState.Disconnected }
        }
    }

    fun sendPayload(payload: GamePayload) {
        currentEndpointId?.let { endpointId ->
            val bytes = GamePayload.toByteArray(payload)
            connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytes))
        }
    }

    fun disconnect() {
        currentEndpointId?.let { endpointId ->
            connectionsClient.disconnectFromEndpoint(endpointId)
        }
        currentEndpointId = null
        _connectionState.update { ConnectionState.Disconnected }
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Advertising : ConnectionState()
        data class Discovering(val endpointId: String?) : ConnectionState()
        data class Connected(val endpointId: String) : ConnectionState()
    }
}
