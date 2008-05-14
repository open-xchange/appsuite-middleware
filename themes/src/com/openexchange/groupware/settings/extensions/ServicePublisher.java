package com.openexchange.groupware.settings.extensions;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ServicePublisher {

    public void publishService(Class clazz,Object service);

    public void removeService(Class clazz, Object service);

    public void removeAllServices();

}
