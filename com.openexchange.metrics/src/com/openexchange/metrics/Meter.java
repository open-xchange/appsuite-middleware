package com.openexchange.metrics;


public interface Meter extends Metric {

    /**
     * Mark the occurrence of an event.
     */
    void mark();

    /**
     * Mark the occurrence of a given number of events.
     *
     * @param n the number of events
     */
    void mark(long n);

}
