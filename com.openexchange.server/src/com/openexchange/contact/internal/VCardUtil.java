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

package com.openexchange.contact.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Streams;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VCardUtil}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class VCardUtil {

    /**
     * Serializes the supplied contact as vCard and writes it to the output stream. A referenced original vCard is considered implicitly.
     *
     * @param contact The contact to serialize
     * @param outputStream The target output stream
     * @param session The session
     * @return The exported vCard for further processing
     */
    public static VCardExport exportContact(Contact contact, Session session) throws OXException {
        VCardService vCardService = ServerServiceRegistry.getInstance().getService(VCardService.class, true);
        InputStream originalVCard = null;
        try {
            if (null != contact.getVCardId()) {
                VCardStorageService vCardStorageService = getVCardStorageService(session.getContextId());
                if (vCardStorageService != null) {
                    originalVCard = vCardStorageService.getVCard(contact.getVCardId(), session.getContextId());
                }
            }
            return vCardService.exportContact(contact, originalVCard, vCardService.createParameters(session));
        } finally {
            Streams.close(originalVCard);
        }
    }

    /**
     * Serializes the supplied contact as vCard and writes it to the output stream.
     *
     * @param contact The contact to serialize
     * @param session The session
     * @param outputStream The target output stream
     */
    public static void exportContact(Contact contact, Session session, OutputStream outputStream) throws OXException {
        VCardExport vCardExport = VCardUtil.exportContact(contact, session);
        try {
            InputStream inputStream = null;
            byte[] buffer = new byte[0xFFFF];
            try {
                inputStream = vCardExport.getVCard().getStream();
                for (int len; (len = inputStream.read(buffer, 0, buffer.length)) > 0;) {
                    outputStream.write(buffer, 0, len);
                }
            } finally {
                Streams.close(inputStream);
            }
        } catch (IOException e) {
            throw DataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(vCardExport);
        }
    }

    /**
     * Imports the first contact from the supplied vCard stream.
     *
     * @param inputStream The input stream carrying the vCard
     * @param session The session
     * @param keepOriginalVCard <code>true</code> to keep a reference for the original vCard data, <code>false</code>, otherwise
     * @return The vCard import
     */
    public static VCardImport importContact(InputStream inputStream, ServerSession session, boolean keepOriginalVCard) throws OXException {
        VCardService vCardService = ServerServiceRegistry.getInstance().getService(VCardService.class, true);
        return vCardService.importVCard(inputStream, null, vCardService.createParameters(session).setKeepOriginalVCard(keepOriginalVCard));
    }

    /**
     * Imports the first contact from the supplied vCard stream and stores it in the user's default contact folder. If supported by the
     * storage, the original vCard data is persisted implicitly.
     *
     * @param inputStream The input stream carrying the vCard
     * @param session The session
     * @return The imported contact
     */
    public static Contact importContactToDefaultFolder(InputStream inputStream, ServerSession session) throws OXException {
        int defaultFolderID = new OXFolderAccess(session.getContext()).getDefaultFolderID(session.getUserId(), FolderObject.CONTACT);
        return importContactToFolder(inputStream, String.valueOf(defaultFolderID), session);
    }

    /**
     * Imports the first contact from the supplied vCard stream and stores it in a specific folder. If supported by the storage, the
     * original vCard data is persisted implicitly.
     *
     * @param inputStream The input stream carrying the vCard
     * @param folderID The target folder identifier
     * @param session The session
     * @return The imported contact
     */
    public static Contact importContactToFolder(InputStream inputStream, String folderID, ServerSession session) throws OXException {
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
        VCardStorageService vCardStorage = getVCardStorageService(session.getContextId());

        boolean keepOriginalVCard = null != vCardStorage && contactService.supports(session, folderID, ContactField.VCARD_ID);
        VCardImport vCardImport = null;
        boolean saved = false;
        String vCardID = null;
        try {
            vCardImport = importContact(inputStream, session, keepOriginalVCard);
            Contact contact = vCardImport.getContact();
            /*
             * store original vCard & remember vCard identifier
             */
            if (vCardStorage != null) {
                IFileHolder originalVCard = vCardImport.getVCard();
                if (null != originalVCard) {
                    try {
                        vCardID = vCardStorage.saveVCard(originalVCard.getStream(), session.getContextId());
                        contact.setVCardId(vCardID);
                    } finally {
                        Streams.close(originalVCard);
                    }
                }
            }
            /*
             * create & return contact
             */
            contactService.createContact(session, folderID, contact);
            saved = true;
            return contact;
        } finally {
            Streams.close(vCardImport);
            if (null != vCardID && false == saved) {
                vCardStorage.deleteVCard(vCardID, session.getContextId());
            }
        }
    }

    public static VCardStorageService getVCardStorageService(int contextId) throws OXException {
        VCardStorageFactory vCardStorageFactory = ServerServiceRegistry.getInstance().getService(VCardStorageFactory.class, false);
        if (vCardStorageFactory != null) {
            return vCardStorageFactory.getVCardStorageService(ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class), contextId);
        }
        return null;
    }

    private VCardUtil() {
        // prevent instantiation
    }

}
