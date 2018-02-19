package com.openexchange.metrics.descriptors;

import java.util.concurrent.TimeUnit;

public class MeterDescriptor extends MetricDescriptor {

    private TimeUnit rate;

    private String unit;

    public MeterDescriptor() {
        super();
    }

    public static final class Builder extends AbstractBuilder<MeterDescriptor> {

        private TimeUnit rate = TimeUnit.SECONDS;

        private String unit = "events";

        public Builder(String group, String name) {
            super(group, name);
        }

        public Builder withRate(TimeUnit unit) {
            this.rate = unit;
            return this;
        }

        public Builder withUnit(String unit) {
            this.unit = unit;
            return this;
        }

        @Override
        protected MeterDescriptor prepare() {
            MeterDescriptor descriptor = new MeterDescriptor();
            descriptor.setGroup(group);
            descriptor.setName(name);
            return descriptor;
        }

        @Override
        protected void check() {
            checkNotNull(rate, "rate");
            checkNotNull(unit, "unit");
        }

        @Override
        protected void fill(MeterDescriptor descriptor) {
            descriptor.setRate(rate);
            descriptor.setUnit(unit);
        }

    }

    public static Builder newBuilder(String group, String name) {
        return new Builder(group, name);
    }

    public TimeUnit getRate() {
        return rate;
    }

    public String getUnit() {
        return unit;
    }

    public void setRate(TimeUnit rate) {
        this.rate = rate;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
