package com.openexchange.utils.propertyhandling;

/**
 * Interface which defines how a property should look like
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface PropertyInterface {

    public Class<? extends Object> getClazz();

    /**
     * Defines if the corresponding property is required, might be a clear true or false, or a dependency on a condition
     * 
     * @return a {@link Required} object
     */
    public Required getRequired();

    /**
     * Get the name of the property in the property file
     * 
     * @return the name
     */
    public String getName();

    /**
     * If the property should be logged or not, might be usefull for password properties
     * 
     * @return true or false
     */
    public boolean isLog();

}