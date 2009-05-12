
package com.openexchange.smtp.config;

import com.openexchange.mail.transport.config.ITransportProperties;

public interface ISMTPProperties extends ITransportProperties {

    /**
     * Gets the smtpLocalhost
     * 
     * @return the smtpLocalhost
     */
    public String getSmtpLocalhost();

    /**
     * Gets the smtpAuth
     * 
     * @return the smtpAuth
     */
    public boolean isSmtpAuth();

    /**
     * Gets the smtpEnvelopeFrom
     * 
     * @return the smtpEnvelopeFrom
     */
    public boolean isSmtpEnvelopeFrom();

    /**
     * Gets the smtpAuthEnc
     * 
     * @return the smtpAuthEnc
     */
    public String getSmtpAuthEnc();

    /**
     * Gets the smtpTimeout
     * 
     * @return the smtpTimeout
     */
    public int getSmtpTimeout();

    /**
     * Gets the smtpConnectionTimeout
     * 
     * @return the smtpConnectionTimeout
     */
    public int getSmtpConnectionTimeout();

}
