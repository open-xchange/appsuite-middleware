
package com.openexchange.tools.servlet.ratelimit;

import java.util.concurrent.atomic.AtomicLong;

public interface Rate {

    /**
     * The rate result.
     */
    public static enum Result {
        SUCCESS, FAILED, DEPRECATED;
    }

    /**
     * Gets the lastLogStamp
     *
     * @return The lastLogStamp
     */
    AtomicLong getLastLogStamp();

    /**
     * Gets this rate's last-accessed time stamp.
     *
     * @return The last-accessed time stamp
     */
    long lastAccessTime();

    /**
     * Checks if this rate is deprecated
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     * @see #markDeprecatedIfElapsed(long)
     */
    boolean isDeprecated();

    /**
     * Marks this rate as deprecated if elapsed in comparison to given threshold.
     *
     * @param threshold The threshold
     * @return <code>true</code> if elapsed (and marked as deprecated); otherwise <code>false</code>
     */
    boolean markDeprecatedIfElapsed(long threshold);

    /**
     * Consumes one slot from this rate.
     *
     * @param now The current time stamp
     * @return The rate result
     */
    Result consume(long now);

    /**
     * Gets the number of permits.
     *
     * @return The permits
     */
    int getPermits();

    /**
     * Gets the time window in milliseconds
     *
     * @return The time window
     */
    long getTimeInMillis();

    /**
     * Sets the number of permits.
     *
     * @param permits
     */
    void setPermits(int permits);

    /**
     * Sets the time window in milliseconds
     *
     * @param timeInMillis The time window
     */
    void setTimeInMillis(long timeInMillis);

}
