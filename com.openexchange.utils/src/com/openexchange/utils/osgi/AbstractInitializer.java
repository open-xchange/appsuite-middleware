package com.openexchange.utils.osgi;

import com.openexchange.exception.OXException;


/**
 * An interface which all classes have to implement which use the
 * {@link OSGiAbstractor#addService(Class, AbstractInitializer, java.util.Dictionary, Class[], com.openexchange.utils.propertyhandling.PropertyInterface[])}
 * method
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public interface AbstractInitializer {

    /**
     * Sets the required service objects. The order in which the service objects are given corresponds to the order the services are
     * presented in the
     * {@link OSGiAbstractor#addService(Class, AbstractInitializer, java.util.Dictionary, Class[], com.openexchange.utils.propertyhandling.PropertyInterface[])}
     * method, see parameter {@code dependingServices}
     * 
     * @param obj an array of object in the same order like described above. The object need to be casted by yourself 
     * @throws OXException if anything wents wrong
     */
    public void setObjects(final Object[] obj) throws OXException;

}
