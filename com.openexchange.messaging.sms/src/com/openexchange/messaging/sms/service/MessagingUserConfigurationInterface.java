
package com.openexchange.messaging.sms.service;

import java.util.List;

/**
 * An interface describing what information a user configuration has to provide
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface MessagingUserConfigurationInterface {

    /**
     * Get the list of allowed sender addresses
     * @return
     */
    public List<String> getAddresses();

    /**
     * TODO
     * @return
     */
    public String getDisplayString();

    /**
     * Get the maximum allowed length of the message
     * @return
     */
    public int getLength();
    
    /**
     * If the messaging service is enabled for the user or not
     * 
     * @return
     */
    public boolean isEnabled();
    
    /**
     * If the message service uses captchas
     * @return
     */
    public boolean isCaptcha();

    /**
     * If the backend is allowed to send multiple SMS, if yes, the GUI shows a counter for the number of SMS messages to be sent
     * @return
     */
    public boolean getMultiSMS();

    /**
     * If the backend is allowed to send MMS messages, if yes, the GUI allows to upload images
     * @return
     */
    public boolean isMMS();
    
    /**
     * Returns an optional Upsell link, if the user has no SMS enabled.
     * @return
     */
    public String getUpsellLink();
    
    /**
     * If the user should have the option to append a signature to the outgoing SMS
     * @return
     */
    public boolean isSignatureOption();
    
    /**
     * Returns the max. number of of recipients, use 0 for unlimited
     * @return
     */
    public int getRecipientLimit();
    
    /**
     * Returns the max. number of of sms, use 0 for unlimited
     * @return
     */
    public int getSmsLimit();

    /**
     * Return the RegEx which will be used to clean numbers in the GUI. Can be null if the default should be used
     * @return
     */
    public String getNumCleanRegEx();
}
