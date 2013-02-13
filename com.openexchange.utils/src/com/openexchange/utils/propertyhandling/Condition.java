
package com.openexchange.utils.propertyhandling;


/**
 * Property framework
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public class Condition {

    private final PropertyInterface property;

    private final Object value;

    public Condition(final PropertyInterface property, final Object value) {
        super();
        this.property = property;
        this.value = value;
    }

    public PropertyInterface getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }

}