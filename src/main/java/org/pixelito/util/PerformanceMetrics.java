package org.pixelito.util;

/**
 * Simple utility class for measuring and reporting performance metrics.
 */
public class PerformanceMetrics {
    private static long startTime;
    private static long endTime;
    private static long memoryBefore;
    private static long memoryAfter;
    
    /**
     * Start measuring performance.
     */
    public static void startMeasurement() {
        // Suggest garbage collection to get more accurate memory readings
        System.gc();
        
        // Record starting memory and time
        memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        startTime = System.nanoTime();
    }
    
    /**
     * Stop measuring performance and return a formatted string with results.
     * 
     * @param operation Description of the operation being measured
     * @param additionalInfo Additional info to include (e.g., vertex count)
     * @return Formatted string with performance metrics
     */
    public static String stopMeasurement(String operation, String additionalInfo) {
        endTime = System.nanoTime();
        System.gc();
        memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        double timeInMs = (endTime - startTime) / 1_000_000.0;
        long memoryUsedKB = (memoryAfter - memoryBefore) / 1024;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Performance Metrics ===\n");
        sb.append("Operation: ").append(operation).append("\n");
        sb.append("Time: ").append(String.format("%.2f", timeInMs)).append(" ms\n");
        sb.append("Memory: ").append(memoryUsedKB).append(" KB\n");
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            sb.append(additionalInfo).append("\n");
        }
        
        sb.append("==========================");
        
        return sb.toString();
    }
}
