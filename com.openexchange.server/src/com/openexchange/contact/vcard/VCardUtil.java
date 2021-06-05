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

package com.openexchange.contact.vcard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
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
