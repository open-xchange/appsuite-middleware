/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.mail.transport;

import javax.mail.Address;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;

/**
 * {@link MailTransport} - Provides operations related to a mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailTransport {

    /**
     * Triggers all implementation-specific startup actions; especially its configuration initialization
     *
     * @param transport A {@link MailTransport transport}
     * @throws OXException If implementation start-up fails
     */
    static final void startupImpl(final MailTransport transport) throws OXException {
        transport.startup();
    }

    /**
     * Triggers all implementation-specific shutdown actions; especially its configuration shut-down
     *
     * @param transport A {@link MailTransport transport}
     * @throws OXException If implementation shut-down fails
     */
    static final void shutdownImpl(final MailTransport transport) throws OXException {
        transport.shutdown();
    }

    /**
     * Gets the proper instance of {@link MailTransport mail transport} parameterized with given session.
     * <p>
     * Note: Don't forget to call final {@link #close()} on obtained {@link MailTransport mail transport}:
     *
     * <pre>
     * final MailTransport mailTransport = MailTransport.getInstance(session);
     * try {
     *     // Do something
     * } finally {
     *     mailTransport.close();
     * }
     * </pre>
     *
     * @param session The session
     * @return A proper instance of {@link MailTransport}
     * @throws OXException If instantiation fails
     */
    public static final MailTransport getInstance(final Session session) throws OXException {
        return getInstance(session, MailAccount.DEFAULT_ID);
    }

    /**
     * Gets the proper instance of {@link MailTransport mail transport} for session user's default transport account.
     * <p>
     * Note: Don't forget to call final {@link #close()} on obtained {@link MailTransport mail transport}:
     *
     * <pre>
     * final MailTransport mailTransport = MailTransport.getInstance(session, accountId);
     * try {
     *     // Do something
     * } finally {
     *     mailTransport.close();
     * }
     * </pre>
     *
     * @param session The session
     * @param accountId The account identifier
     * @return A proper instance of {@link MailTransport}
     * @throws OXException If instantiation fails
     */
    public static final MailTransport getInstance(final Session session, final int accountId) throws OXException {
        /*
         * Check for proper initialization
         */
        if (!TransportInitialization.getInstance().isInitialized()) {
            throw MailExceptionCode.INITIALIZATION_PROBLEM.create();
        }
        /*
         * Create a new mail transport through user's transport provider
         */
        return TransportProviderRegistry.getTransportProviderBySession(session, accountId).createNewMailTransport(session, accountId);
    }

    /**
     * Sends a mail message
     * <p>
     * This is a convenience method that invokes {@link #sendMailMessage(ComposedMailMessage, ComposeType, Address[])} with the latter
     * parameter set to <code>null</code> if {@link ComposedMailMessage#hasRecipients()} is <code>false</code>; otherwise
     * {@link ComposedMailMessage#getRecipients()} is passed.
     *
     * @param transportMail The mail message to send (containing necessary header data and body)
     * @param sendType The send type
     * @return The sent mail message
     * @throws OXException If transport fails
     */
    public MailMessage sendMailMessage(final ComposedMailMessage transportMail, final ComposeType sendType) throws OXException {
        return sendMailMessage(transportMail, sendType, null);
    }

    /**
     * Sends a mail message
     *
     * @param transportMail The mail message to send (containing necessary header data and body)
     * @param sendType The send type
     * @param allRecipients An array of {@link Address addresses} to send this message to; may be <code>null</code> to extract recipients
     *            from message headers TO, CC, BCC, and NEWSGROUPS.
     * @return The sent mail message
     * @throws OXException If transport fails
     */
    public abstract MailMessage sendMailMessage(ComposedMailMessage transportMail, ComposeType sendType, Address[] allRecipients) throws OXException;

    /**
     * Sends specified message's raw ascii bytes. The given bytes are interpreted dependent on implementation, but in most cases it's
     * treated as an rfc822 MIME message.
     * <p>
     * This is a convenience method that invokes {@link #sendRawMessage(byte[], Address[])} with the latter parameter set to
     * <code>null</code>.
     *
     * @param asciiBytes The raw ascii bytes
     * @return The sent mail message
     * @throws OXException If sending fails
     */
    public MailMessage sendRawMessage(final byte[] asciiBytes) throws OXException {
        return sendRawMessage(asciiBytes, null);
    }

    /**
     * Sends specified message's raw ascii bytes. The given bytes are interpreted dependent on implementation, but in most cases it's
     * treated as an rfc822 MIME message.
     *
     * @param asciiBytes The raw ascii bytes
     * @param allRecipients An array of {@link Address addresses} to send this message to; may be <code>null</code> to extract recipients
     *            from message headers TO, CC, BCC, and NEWSGROUPS.
     * @return The sent mail message
     * @throws OXException If sending fails
     */
    public abstract MailMessage sendRawMessage(byte[] asciiBytes, Address[] allRecipients) throws OXException;

    /**
     * Sends a receipt acknowledgment for the specified message.
     *
     * @param srcMail The source mail
     * @param fromAddr The from address (as unicode string). If set to <code>null</code>, user's default email address is used as value for
     *            header <code>From</code>
     * @throws OXException If transport fails
     */
    public abstract void sendReceiptAck(MailMessage srcMail, String fromAddr) throws OXException;

    /**
     * Pings the transport server to check if a connection can be established.
     *
     * @throws OXException If the ping fails
     */
    public abstract void ping() throws OXException;

    /**
     * Closes this mail transport
     *
     * @throws OXException If closing fails
     */
    public abstract void close() throws OXException;

    /**
     * Returns the transport configuration appropriate for current user. It provides needed connection and login information.
     *
     * @return The transport configuration
     */
    public abstract TransportConfig getTransportConfig() throws OXException;

    /**
     * Trigger all necessary startup actions; especially configuration start-up
     *
     * @throws OXException If startup actions fail
     */
    protected abstract void startup() throws OXException;

    /**
     * Trigger all necessary shutdown actions; especially configuration shut-down
     *
     * @throws OXException If shutdown actions fail
     */
    protected abstract void shutdown() throws OXException;

    /**
     * Gets an implementation-specific new instance of {@link ITransportProperties}.
     *
     * @return An implementation-specific new instance of {@link ITransportProperties}
     * @throws OXException If creating a new instance of {@link ITransportProperties} fails
     */
    protected abstract ITransportProperties createNewMailProperties() throws OXException;
}
