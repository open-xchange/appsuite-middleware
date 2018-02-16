package com.openexchange.metrics.impl.dropwizard;

import com.openexchange.metrics.Meter;


public class MeterImpl implements Meter {

    private final com.codahale.metrics.Meter delegate;

    public MeterImpl(com.codahale.metrics.Meter delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void mark() {
        delegate.mark();
    }

    @Override
    public void mark(long n) {
        delegate.mark(n);
    }

}
