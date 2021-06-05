/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.smtp;

import java.util.Iterator;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.transport.listener.MailTransportListener;
import com.openexchange.mail.transport.listener.Reply;
import com.openexchange.mail.transport.listener.Result;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;

/**
 * {@link ListenerChain} - The listener chain.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ListenerChain implements MailTransportListener {

    private static volatile ListenerChain instance;

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ListenerChain getInstance() {
        return instance;
    }

    /**
     * Initializes the instance
     *
     * @param listing The associated service listing
     */
    public static synchronized void initInstance(ServiceListing<MailTransportListener> listing) {
        if (null == instance) {
            instance = new ListenerChain(listing);
        }
    }

    /**
     * Release the instance
     */
    public static synchronized void releaseInstance() {
        instance = null;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final ServiceListing<MailTransportListener> listeners;

    /**
     * Initializes a new {@link ListenerChain}.
     */
    private ListenerChain(ServiceListing<MailTransportListener> listeners) {
        super();
        this.listeners = listeners;
    }

    @Override
    public boolean checkSettings(SecuritySettings securitySettings, Session session) throws OXException {
        Iterator<MailTransportListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return false;
        }

        do {
            MailTransportListener listener = iterator.next();
            if (listener.checkSettings(securitySettings, session)) {
                return true;
            }
        } while (iterator.hasNext());

        return false;
    }

    @Override
    public Result onBeforeMessageTransport(MimeMessage message, Address[] recipients, SecuritySettings securitySettings, Session session) throws OXException {
        Iterator<MailTransportListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return new ChainResult(message, recipients, Reply.NEUTRAL);
        }

        do {
            MailTransportListener listener = iterator.next();
            Result result = listener.onBeforeMessageTransport(message, recipients, securitySettings, session);
            Reply reply = result.getReply();
            if (Reply.NEUTRAL != reply) {
                return result;
            }
        } while (iterator.hasNext());

        return new ChainResult(message, recipients, Reply.NEUTRAL);
    }

    @Override
    public void onAfterMessageTransport(MimeMessage message, Exception exception, Session session) throws OXException {
        Iterator<MailTransportListener> iterator = this.listeners.iterator();
        if (false == iterator.hasNext()) {
            return;
        }

        do {
            MailTransportListener listener = iterator.next();
            listener.onAfterMessageTransport(message, exception, session);
        } while (iterator.hasNext());
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private static class ChainResult implements Result {

        private final MimeMessage mimeMessage;
        private final Reply reply;
        private final Address[] recipients;

        ChainResult(MimeMessage mimeMessage, Address[] recipients, Reply reply) {
            super();
            this.mimeMessage = mimeMessage;
            this.recipients = recipients;
            this.reply = reply;
        }

        @Override
        public Reply getReply() {
            return reply;
        }

        @Override
        public MimeMessage getMimeMessage() {
            return mimeMessage;
        }

        @Override
        public Address[] getRecipients() {
            return recipients;
        }

    }

}
