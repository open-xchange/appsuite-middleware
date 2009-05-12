
package com.openexchange.imap.config;

import java.util.Map;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailConfig.BoolCapVal;

/**
 * {@link IIMAPProperties} - Properties for IMAP.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IIMAPProperties extends IMailProperties {

    /**
     * Checks if fast <code>FETCH</code> is enabled.
     * 
     * @return <code>true</code> if fast <code>FETCH</code> is enabled; otherwise <code>false</code>
     */
    public boolean isFastFetch();

    /**
     * Gets the IMAP authentication encoding.
     * 
     * @return The IMAP authentication encoding
     */
    public String getImapAuthEnc();

    /**
     * Gets the IMAP connection idle time.
     * 
     * @return The IMAP connection idle time
     */
    public int getImapConnectionIdleTime();

    /**
     * Gets the IMAP connection timeout.
     * 
     * @return The IMAP connection timeout
     */
    public int getImapConnectionTimeout();

    /**
     * Gets the IMAP temporary down.
     * 
     * @return The IMAP temporary down
     */
    public int getImapTemporaryDown();

    /**
     * Checks if IMAP search is enabled.
     * 
     * @return <code>true</code> if IMAP search is enabled; otherwise <code>false</code>
     */
    public boolean isImapSearch();

    /**
     * Checks if IMAP sort is enabled.
     * 
     * @return <code>true</code> if IMAP sort is enabled; otherwise <code>false</code>
     */
    public boolean isImapSort();

    /**
     * Gets the IMAP timeout.
     * 
     * @return The IMAP timeout
     */
    public int getImapTimeout();

    /**
     * Indicates support for ACLs.
     * 
     * @return The support for ACLs
     */
    public BoolCapVal getSupportsACLs();

    /**
     * Gets the block size in which large IMAP commands' UIDs/sequence numbers arguments get splitted.
     * 
     * @return The block size
     */
    public int getBlockSize();

    /**
     * Gets the map holding IMAP servers with new ACL Extension.
     * 
     * @return The map holding IMAP servers with new ACL Extension
     * @deprecated Should be unnecessary due to new ACL extension detection
     */
    @Deprecated
    public Map<String, Boolean> getNewACLExtMap();

}
