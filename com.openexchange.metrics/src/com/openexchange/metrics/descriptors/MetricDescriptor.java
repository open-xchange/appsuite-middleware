package com.openexchange.metrics.descriptors;


public abstract class MetricDescriptor {

    private String group;

    private String name;

    public MetricDescriptor() {
        super();
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setName(String name) {
        this.name = name;
    }

    static abstract class AbstractBuilder<T extends MetricDescriptor> {

        protected static final String MISSING_FIELD = "A %s must be set!";

        protected final String group;

        protected final String name;


        public AbstractBuilder(final String group, final String name) {
            super();
            this.group = group;
            this.name = name;
        }

        public T build() {
            checkNotNull(group, "group");
            checkNotNull(name, "name");
            T descriptor = prepare();
            fill(descriptor);
            return descriptor;
        }

        protected abstract void check();

        protected abstract T prepare();

        protected abstract void fill(T descriptor);

        static <T> T checkNotNull(T reference, String errorMessage) {
            if (reference == null) {
                throw new NullPointerException(errorMessage);
            }
            return reference;
        }

    }

}
