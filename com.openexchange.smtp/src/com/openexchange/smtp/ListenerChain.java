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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.smtp;

import java.util.List;
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
    public Result onBeforeMessageTransport(MimeMessage message, SecuritySettings securitySettings, Session session) throws OXException {
        List<MailTransportListener> listeners = this.listeners.getServiceList();
        if (null == listeners || listeners.isEmpty()) {
            return new ChainResult(message, Reply.NEUTRAL);
        }

        for (MailTransportListener listener : listeners) {
            Result result = listener.onBeforeMessageTransport(message, securitySettings, session);
            Reply reply = result.getReply();
            if (Reply.NEUTRAL != reply) {
                return result;
            }
        }

        return new ChainResult(message, Reply.NEUTRAL);
    }

    @Override
    public void onAfterMessageTransport(MimeMessage message, Exception exception, Session session) throws OXException {
        List<MailTransportListener> listeners = this.listeners.getServiceList();
        if (null == listeners || listeners.isEmpty()) {
            return;
        }

        for (MailTransportListener listener : listeners) {
            listener.onAfterMessageTransport(message, exception, session);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private static class ChainResult implements Result {

        private final MimeMessage mimeMessage;
        private final Reply reply;

        ChainResult(MimeMessage mimeMessage, Reply reply) {
            super();
            this.mimeMessage = mimeMessage;
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

    }

}
