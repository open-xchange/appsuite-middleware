package com.openexchange.spamhandler.spamassassin.api;

import com.openexchange.mail.MailException;
import com.openexchange.session.Session;

/**
 * An interface for getting information about the SpamdInstallation for the current user.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface SpamdService {

    /**
     * Gets the provider data for this session
     * 
     * @param session A {@link Session} object
     * @return A {@link SpamdProvider} object with the information needed for spamd can't be null. If the provider
     * cannot be fetched a {@link MailException} has to be raised
     * @throws MailException if something went wrong during getProvider operation
     */
    public SpamdProvider getProvider(final Session session) throws MailException;
}
