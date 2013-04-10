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

package com.openexchange.messaging.mail;

import static com.openexchange.log.LogFactory.getLog;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.Address;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link MailMessagingAccountTransport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public class MailMessagingAccountTransport implements MessagingAccountTransport {

    private final MailTransport mailTransport;

    /**
     * Initializes a new {@link MailMessagingAccountTransport}.
     *
     * @param accountId The account ID
     * @param session The session providing user data
     * @throws OXException If initialization fails
     */
    public MailMessagingAccountTransport(final int accountId, final Session session) throws OXException {
        super();
        try {
            mailTransport = MailTransport.getInstance(session, accountId);
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void transport(final MessagingMessage message, final Collection<MessagingAddressHeader> recipients) throws OXException {
        try {
            final UnsynchronizedByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
            message.writeTo(out);
            final List<Address> addrs = new ArrayList<Address>(recipients.size());
            for (final MessagingAddressHeader mah : recipients) {
                final QuotedInternetAddress addr = new QuotedInternetAddress();
                addr.setAddress(mah.getAddress());
                addr.setPersonal(mah.getPersonal());
            }
            // TODO: Use ContentAwareComposeMailMessage instead
            mailTransport.sendRawMessage(out.toByteArray(), addrs.toArray(new Address[0]));
        } catch (final UnsupportedEncodingException e) {
            throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw e;
        } catch (final IOException e) {
            throw MessagingExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean cacheable() {
        return false;
    }

    @Override
    public void close() {
        try {
            mailTransport.close();
        } catch (final OXException e) {
            getLog(MailMessagingAccountTransport.class).error(e.getMessage(), e);
        }
    }

    @Override
    public void connect() throws OXException {
        // Nope
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean ping() throws OXException {
        try {
            mailTransport.ping();
            return true;
        } catch (final OXException e) {
            getLog(MailMessagingAccountTransport.class).error(e.getMessage(), e);
            return false;
        }
    }

}
