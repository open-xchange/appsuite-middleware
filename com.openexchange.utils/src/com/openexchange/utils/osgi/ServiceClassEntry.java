package com.openexchange.utils.osgi;


/**
 * A specific class for holding a service class name which is used for filtering in OSGi and the real class you are interested in 
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @param <T>
 */
public class ServiceClassEntry<T> {

    private final Class<T> clazz;

    private final Class<?> service;

    /**
     * Convenience method if service filter class and service implementation class are the same 
     * @param clazz
     */
    public ServiceClassEntry(final Class<T> clazz) {
        super();
        this.clazz = clazz;
        this.service = clazz;
    }

    /**
     * Initializes a new {@link ServiceClassEntry}.
     * @param clazz the service implementation class
     * @param service the service filter class
     */
    public ServiceClassEntry(final Class<T> clazz, final Class<?> service) {
        super();
        this.clazz = clazz;
        this.service = service;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public Class<?> getService() {
        return service;
    }

}
