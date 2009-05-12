
package com.openexchange.pop3.config;

import com.openexchange.mail.api.IMailProperties;

/**
 * {@link IPOP3Properties} - Properties for POP3.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IPOP3Properties extends IMailProperties {

    /**
     * Gets the POP3 authentication encoding.
     * 
     * @return The POP3 authentication encoding
     */
    public String getPOP3AuthEnc();

    /**
     * Gets the POP3 connection idle time in milliseconds.
     * 
     * @return The POP3 connection idle time in milliseconds
     */
    public int getPOP3ConnectionIdleTime();

    /**
     * Gets the POP3 connection timeout in milliseconds.
     * 
     * @return The POP3 connection timeout in milliseconds
     */
    public int getPOP3ConnectionTimeout();

    /**
     * Gets the POP3 temporary down in milliseconds.
     * 
     * @return The POP3 temporary down in milliseconds
     */
    public int getPOP3TemporaryDown();

    /**
     * Gets the POP3 timeout in milliseconds.
     * 
     * @return The POP3 timeout in milliseconds
     */
    public int getPOP3Timeout();

}
