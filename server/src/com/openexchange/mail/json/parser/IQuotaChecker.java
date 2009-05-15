
package com.openexchange.mail.json.parser;

import com.openexchange.mail.MailException;

/**
 * {@link IQuotaChecker} - Monitors attached files when parsing a mail typically done on mail transport/append.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
interface IQuotaChecker {

    /**
     * Adds specified number of bytes to this quota checker's amount of consumed bytes.
     * 
     * @param size The number of bytes being consumed
     * @param fileName The file name
     * @throws MailException Depending on implementation it may indicate an exceeded quota, but other errors as well
     */
    public void addConsumed(final long size, final String fileName) throws MailException;

}
