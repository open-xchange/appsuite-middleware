package com.openexchange.utils.osgi;

/**
 * An interface defining methods which are called if a service gets available
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 * @param <T> The type of the object
 */
public interface AvailabilityActivationClosure<T> {

    /**
     * Would should be done if the service is available
     * 
     * @param object Object of the service
     */
    public void serviceAvailable(final T object);

}