package wiki.tk.fistarium.core.utils

import kotlinx.coroutines.delay

/**
 * Retry utility for network operations with exponential backoff.
 */
object RetryUtils {
    
    /**
     * Execute a suspending operation with retry logic.
     * 
     * @param times Maximum number of retry attempts
     * @param initialDelayMs Initial delay between retries in milliseconds
     * @param maxDelayMs Maximum delay between retries in milliseconds
     * @param factor Multiplier for exponential backoff
     * @param shouldRetry Predicate to determine if exception should trigger retry
     * @param block The operation to execute
     * @return Result of the operation
     */
    suspend fun <T> withRetry(
        times: Int = 3,
        initialDelayMs: Long = 100,
        maxDelayMs: Long = 2000,
        factor: Double = 2.0,
        shouldRetry: (Exception) -> Boolean = { isRetryableException(it) },
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                
                // Don't retry on last attempt or non-retryable exceptions
                if (attempt == times - 1 || !shouldRetry(e)) {
                    throw e
                }
                
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }
        
        throw lastException ?: RuntimeException("Retry failed without exception")
    }
    
    /**
     * Execute a suspending operation with retry logic, returning Result.
     */
    suspend fun <T> withRetryResult(
        times: Int = 3,
        initialDelayMs: Long = 100,
        maxDelayMs: Long = 2000,
        factor: Double = 2.0,
        shouldRetry: (Exception) -> Boolean = { isRetryableException(it) },
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(withRetry(times, initialDelayMs, maxDelayMs, factor, shouldRetry, block))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Determine if an exception is retryable (typically network-related).
     */
    private fun isRetryableException(e: Exception): Boolean {
        val message = e.message?.lowercase() ?: ""
        return when {
            // Network errors
            e is java.net.SocketTimeoutException -> true
            e is java.net.UnknownHostException -> true
            e is java.net.ConnectException -> true
            e is java.io.IOException && message.contains("network") -> true
            
            // Firebase specific transient errors
            message.contains("network") -> true
            message.contains("timeout") -> true
            message.contains("unavailable") -> true
            message.contains("deadline") -> true
            
            // Not retryable
            message.contains("permission") -> false
            message.contains("unauthenticated") -> false
            message.contains("invalid") -> false
            
            else -> false
        }
    }
}
