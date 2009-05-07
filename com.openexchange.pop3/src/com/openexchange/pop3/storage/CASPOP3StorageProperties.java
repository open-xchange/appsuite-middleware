
package com.openexchange.pop3.storage;

import com.openexchange.mail.MailException;

/**
 * {@link CASPOP3StorageProperties} - Enhances {@link POP3StorageProperties} by a method to atomically compare-and-set a property.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CASPOP3StorageProperties extends POP3StorageProperties {

    /**
     * Atomically sets the property value to the given updated value if the current property value == the expected property value.
     * 
     * @param propertyName The property name
     * @param expectedPropertyValue The expected property value
     * @param newPropertyValue The new property value (if current property value == the expected property value)
     * @return <code>true</code> if successful; otherwise <code>false</code> if the actual value was not equal to the expected value.
     * @throws MailException If adding mapping fails
     */
    public boolean compareAndSetProperty(final String propertyName, final String expectedPropertyValue, final String newPropertyValue) throws MailException;

}
