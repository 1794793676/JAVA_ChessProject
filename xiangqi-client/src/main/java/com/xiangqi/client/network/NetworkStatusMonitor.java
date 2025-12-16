package com.xiangqi.client.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Monitors network connection status and provides callbacks for status changes.
 * Tracks connection quality, latency, and connection stability.
 */
public class NetworkStatusMonitor {
    private static final Logger LOGGER = Logger.getLogger(NetworkStatusMonitor.class.getName());
    
    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        UNSTABLE,
        FAILED
    }
    
    public interface NetworkStatusListener {
        void onStatusChanged(ConnectionStatus oldStatus, ConnectionStatus newStatus);
        void onLatencyChanged(long latencyMs);
        void onConnectionQualityChanged(ConnectionQuality quality);
        void onReconnectionAttempt(int attemptNumber, int maxAttempts);
        void onReconnectionFailed();
    }
    
    public enum ConnectionQuality {
        EXCELLENT(0, 50),      // < 50ms
        GOOD(50, 150),         // 50-150ms
        FAIR(150, 300),        // 150-300ms
        POOR(300, 1000),       // 300ms-1s
        VERY_POOR(1000, Long.MAX_VALUE); // > 1s
        
        private final long minLatency;
        private final long maxLatency;
        
        ConnectionQuality(long minLatency, long maxLatency) {
            this.minLatency = minLatency;
            this.maxLatency = maxLatency;
        }
        
        public static ConnectionQuality fromLatency(long latencyMs) {
            for (ConnectionQuality quality : values()) {
                if (latencyMs >= quality.minLatency && latencyMs < quality.maxLatency) {
                    return quality;
                }
            }
            return VERY_POOR;
        }
    }
    
    private final List<NetworkStatusListener> listeners = new ArrayList<>();
    private final AtomicBoolean monitoring = new AtomicBoolean(false);
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private final AtomicLong currentLatency = new AtomicLong(0);
    
    private volatile ConnectionStatus currentStatus = ConnectionStatus.DISCONNECTED;
    private volatile ConnectionQuality currentQuality = ConnectionQuality.POOR;
    
    // Statistics
    private long totalPacketsSent = 0;
    private long totalPacketsReceived = 0;
    private long totalPacketsLost = 0;
    private long connectionStartTime = 0;
    private int reconnectionAttempts = 0;
    
    /**
     * Adds a network status listener.
     */
    public void addListener(NetworkStatusListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }
    
    /**
     * Removes a network status listener.
     */
    public void removeListener(NetworkStatusListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Starts monitoring network status.
     */
    public void startMonitoring() {
        if (monitoring.compareAndSet(false, true)) {
            connectionStartTime = System.currentTimeMillis();
            LOGGER.info("Network status monitoring started");
        }
    }
    
    /**
     * Stops monitoring network status.
     */
    public void stopMonitoring() {
        if (monitoring.compareAndSet(true, false)) {
            LOGGER.info("Network status monitoring stopped");
        }
    }
    
    /**
     * Updates the connection status.
     */
    public void updateStatus(ConnectionStatus newStatus) {
        ConnectionStatus oldStatus = currentStatus;
        if (oldStatus != newStatus) {
            currentStatus = newStatus;
            
            if (newStatus == ConnectionStatus.CONNECTED) {
                reconnectionAttempts = 0;
            }
            
            notifyStatusChanged(oldStatus, newStatus);
            LOGGER.info("Network status changed: " + oldStatus + " -> " + newStatus);
        }
    }
    
    /**
     * Records a ping sent to measure latency.
     */
    public void recordPingSent() {
        lastPingTime.set(System.currentTimeMillis());
        totalPacketsSent++;
    }
    
    /**
     * Records a ping response received.
     */
    public void recordPingReceived() {
        long pingTime = lastPingTime.get();
        if (pingTime > 0) {
            long latency = System.currentTimeMillis() - pingTime;
            updateLatency(latency);
            totalPacketsReceived++;
        }
    }
    
    /**
     * Records a packet loss.
     */
    public void recordPacketLoss() {
        totalPacketsLost++;
        
        // If packet loss is high, mark connection as unstable
        double lossRate = getPacketLossRate();
        if (lossRate > 0.1 && currentStatus == ConnectionStatus.CONNECTED) { // > 10% loss
            updateStatus(ConnectionStatus.UNSTABLE);
        }
    }
    
    /**
     * Records a reconnection attempt.
     */
    public void recordReconnectionAttempt(int attemptNumber, int maxAttempts) {
        reconnectionAttempts = attemptNumber;
        updateStatus(ConnectionStatus.RECONNECTING);
        notifyReconnectionAttempt(attemptNumber, maxAttempts);
    }
    
    /**
     * Records a failed reconnection.
     */
    public void recordReconnectionFailed() {
        updateStatus(ConnectionStatus.FAILED);
        notifyReconnectionFailed();
    }
    
    /**
     * Updates the current latency and connection quality.
     */
    private void updateLatency(long latencyMs) {
        currentLatency.set(latencyMs);
        
        ConnectionQuality newQuality = ConnectionQuality.fromLatency(latencyMs);
        if (newQuality != currentQuality) {
            currentQuality = newQuality;
            notifyConnectionQualityChanged(newQuality);
        }
        
        notifyLatencyChanged(latencyMs);
    }
    
    /**
     * Gets the current connection status.
     */
    public ConnectionStatus getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * Gets the current connection quality.
     */
    public ConnectionQuality getCurrentQuality() {
        return currentQuality;
    }
    
    /**
     * Gets the current latency in milliseconds.
     */
    public long getCurrentLatency() {
        return currentLatency.get();
    }
    
    /**
     * Gets the packet loss rate (0.0 to 1.0).
     */
    public double getPacketLossRate() {
        if (totalPacketsSent == 0) {
            return 0.0;
        }
        return (double) totalPacketsLost / totalPacketsSent;
  