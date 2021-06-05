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

package com.openexchange.mail.compose.impl.open;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.AttachmentStorage;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractOpener} - Abstract utility for opening a composition space for a certain purpose (reply, forward, edit, ...)
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class AbstractOpener {

    protected static final com.openexchange.mail.compose.Message.ContentType TEXT_PLAIN = com.openexchange.mail.compose.Message.ContentType.TEXT_PLAIN;
    protected static final com.openexchange.mail.compose.Message.ContentType TEXT_HTML = com.openexchange.mail.compose.Message.ContentType.TEXT_HTML;

    // -----------------------------------------------------------------------------------------------------------------------

    protected final ServiceLookup services;
    private final AttachmentStorageService attachmentStorageService;

    /**
     * Initializes a new {@link AbstractOpener}.
     */
    protected AbstractOpener(AttachmentStorageService attachmentStorageService, ServiceLookup services) {
        super();
        this.attachmentStorageService = attachmentStorageService;
        this.services = services;
    }

    /**
     * Gets referenced mail
     *
     * @param mailPath The mail path for the mail
     * @param mailInterface The service to use
     * @return The mail
     * @throws OXException If mail cannot be returned
     */
    protected MailMessage requireMailMessage(MailPath mailPath, MailServletInterface mailInterface) throws OXException {
        MailMessage mailMessage = mailInterface.getMessage(mailPath.getFolderArgument(), mailPath.getMailID(), false);
        if (null == mailMessage) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(mailPath.getMailID(), mailPath.getFolderArgument());
        }
        return mailMessage;
    }

    /**
     * Gets the attachment storage for given session.
     *
     * @return The composition space service
     * @throws OXException If composition space service cannot be returned
     */
    protected AttachmentStorage getAttachmentStorage(Session session) throws OXException {
        return attachmentStorageService.getAttachmentStorageFor(session);
    }

    /**
     * Gets the context associated with given session.
     *
     * @param session The session
     * @return The associated context
     * @throws OXException If context cannot be returned
     */
    protected Context getContext(Session session) throws OXException {
        return session instanceof ServerSession ? ((ServerSession) session).getContext() : services.getService(ContextService.class).getContext(session.getContextId());
    }

    /**
     * Converts given Internet email address to an {@link Address} instance.
     *
     * @param addr The Internet email address to convert
     * @param withPersonalIfPresent <code>true</code> to take over possible personal part; otherwise <code>false</code> to not set it
     * @return The resulting {@code Address} instance
     */
    protected static Address toAddress(QuotedInternetAddress addr, boolean withPersonalIfPresent) {
        return null == addr ? null : new Address(withPersonalIfPresent ? addr.getPersonal() : null, addr.getUnicodeAddress());
    }

    /**
     * Converts given Internet email address to an {@link Address} instance.
     *
     * @param addr The Internet email address to convert
     * @param withPersonalIfPresent <code>true</code> to take over possible personal part; otherwise <code>false</code> to not set it
     * @return The resulting {@code Address} instance
     */
    protected static Address toAddress(InternetAddress addr, boolean withPersonalIfPresent) {
        return null == addr ? null : new Address(withPersonalIfPresent ? addr.getPersonal() : null, IDNA.toIDN(addr.getAddress()));
    }

    /**
     * Converts given Internet email addresses to {@link Address} instances.
     *
     * @param addrs The Internet email addresses to convert
     * @return The resulting {@code Address} instances
     */
    protected static List<Address> toAddresses(InternetAddress[] addrs) {
        if (null == addrs || 0 == addrs.length) {
            return Collections.emptyList();
        }

        List<Address> addresses = new ArrayList<Address>(addrs.length);
        for (InternetAddress addr : addrs) {
            Address address = toAddress(addr, true);
            if (null != address) {
                addresses.add(address);
            }
        }
        return addresses;
    }

}
