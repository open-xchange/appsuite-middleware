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

package com.openexchange.mail.compose;

import java.util.UUID;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardUtil;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CompositonSpaces} - Utility class for composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CompositonSpaces {

    /**
     * Initializes a new {@link CompositonSpaces}.
     */
    private CompositonSpaces() {
        super();
    }

    /**
     * Gets the vCard for the user associated with specified session.
     *
     * @param session The session providing user information
     * @return The vCard as byte array
     * @throws OXException
     */
    public static byte[] getUserVCardBytes(Session session) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        Contact contact = contactService.getUser(session, session.getUserId());
        VCardExport vCardExport = null;
        try {
            vCardExport = VCardUtil.exportContact(contact, session);
            return vCardExport.toByteArray();
        } finally {
            Streams.close(vCardExport);
        }
    }

    /**
     * Gets the file name of the vCard file for the user associated with specified session.
     *
     * @param session The session providing user information
     * @return The vCard file name
     * @throws OXException
     */
    public static String getUserVCardFileName(Session session) throws OXException {
        String displayName;
        if (session instanceof ServerSession) {
            displayName = ((ServerSession) session).getUser().getDisplayName();
        } else {
            displayName = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getDisplayName();
        }
        String saneDisplayName = Strings.replaceWhitespacesWith(displayName, "");
        return saneDisplayName + ".vcf";
    }

    /**
     * Gets the vCard information for the user associated with specified session.
     *
     * @param session The session providing user information
     * @return The vCard information
     * @throws OXException
     */
    public static VCardAndFileName getUserVCard(Session session) throws OXException {
        return new VCardAndFileName(getUserVCardBytes(session), getUserVCardFileName(session));
    }

    /*
     * The display of a users given and sur name name in e.g. notification mails (Hello John Doe, ...).
     * The placeholders mean $givenname $surname.
     */
    private static final String USER_NAME = "%1$s %2$s";

    /**
     * Gets the vCard for the given contact.
     *
     * @param contactId The identifier of the contact
     * @param folderId The identifier of the folder in which the contact resides
     * @param session The session providing user information
     * @return The vCard as byte array
     * @throws OXException
     */
    public static VCardAndFileName getContactVCard(String contactId, String folderId, Session session) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        Contact contact = contactService.getContact(session, folderId, contactId);

        byte[] vcard;
        {
            VCardExport vCardExport = null;
            try {
                vCardExport = VCardUtil.exportContact(contact, session);
                vcard = vCardExport.toByteArray();
            } finally {
                Streams.close(vCardExport);
            }
        }

        String displayName = contact.getDisplayName();
        if (Strings.isEmpty(displayName)) {
            TranslatorFactory translatorFactory = ServerServiceRegistry.getInstance().getService(TranslatorFactory.class);

            User user;
            if (session instanceof ServerSession) {
                user = ((ServerSession) session).getUser();
            } else {
                user = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
            }

            Translator translator = translatorFactory.translatorFor(user.getLocale());

            String givenName = contact.getGivenName();
            String surname = contact.getSurName();
            displayName = String.format(translator.translate(USER_NAME), givenName, surname);
        }
        String saneDisplayName = Strings.replaceWhitespacesWith(displayName, "");
        String fileName = saneDisplayName + ".vcf";

        return new VCardAndFileName(vcard, fileName);
    }

    /**
     * Parses a composition space's UUID from specified unformatted string.
     *
     * @param id The composition space identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws OXException If passed string in invalid
     */
    public static UUID parseCompositionSpaceId(String id) throws OXException {
        try {
            return UUIDs.fromUnformattedString(id);
        } catch (IllegalArgumentException e) {
            throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(e, id);
        }
    }

    /**
     * Parses an attachment's UUID from specified unformatted string.
     *
     * @param id The attachment identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID
     * @throws OXException If passed string in invalid
     */
    public static UUID parseAttachmentId(String id) throws OXException {
        try {
            return UUIDs.fromUnformattedString(id);
        } catch (IllegalArgumentException e) {
            throw CompositionSpaceErrorCode.NO_SUCH_ATTACHMENT_RESOURCE.create(e, id);
        }
    }

    /**
     * Parses an attachment's UUID from specified unformatted string.
     *
     * @param id The attachment identifier as an unformatted string; e.g. <code>067e61623b6f4ae2a1712470b63dff00</code>
     * @return The UUID or <code>null</code> if passed string in invalid
     */
    public static UUID parseAttachmentIdIfValid(String id) {
        try {
            return UUIDs.fromUnformattedString(id);
        } catch (@SuppressWarnings("unused") IllegalArgumentException x) {
            return null;
        }
    }

}
