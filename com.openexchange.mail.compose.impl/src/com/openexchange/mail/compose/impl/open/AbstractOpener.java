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
 *    trademarks of the OX Software GmbH. group of companies.
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
 * {@link AbstractOpener}
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
     * @throws OXException If context xannot be returned
     */
    protected Context getContext(Session session) throws OXException {
        return session instanceof ServerSession ? ((ServerSession) session).getContext() : services.getService(ContextService.class).getContext(session.getContextId());
    }

    /**
     * Converts given (internet) address to an {@link Address} instance
     *
     * @param addr The address to convert
     * @return The resulting {@code Address} instance
     */
    protected static Address toAddress(QuotedInternetAddress addr) {
        return null == addr ? null : new Address(addr.getPersonal(), addr.getUnicodeAddress());
    }

    /**
     * Converts given (internet) address to an {@link Address} instance
     *
     * @param addr The address to convert
     * @return The resulting {@code Address} instance
     */
    protected static Address toAddress(InternetAddress addr) {
        return null == addr ? null : new Address(addr.getPersonal(), IDNA.toIDN(addr.getAddress()));
    }

    /**
     * Converts given (internet) addresses to an {@link Address} instances
     *
     * @param addrs The addresses to convert
     * @return The resulting {@code Address} instances
     */
    protected static List<Address> toAddresses(InternetAddress[] addrs) {
        if (null == addrs || 0 == addrs.length) {
            return Collections.emptyList();
        }

        List<Address> addresses = new ArrayList<Address>(addrs.length);
        for (InternetAddress addr : addrs) {
            Address address = toAddress(addr);
            if (null != address) {
                addresses.add(address);
            }
        }
        return addresses;
    }

}
