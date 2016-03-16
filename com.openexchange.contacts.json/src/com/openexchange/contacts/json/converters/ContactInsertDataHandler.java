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

package com.openexchange.contacts.json.converters;

import java.io.InputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ContactInsertDataHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ContactInsertDataHandler implements DataHandler {

    private static final String[] ARGS = { "com.openexchange.groupware.contact.folder" };

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ContactInsertDataHandler}.
     *
     * @param services A service lookup reference
     */
    public ContactInsertDataHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    @Override
    public Object processData(Data<?> data, DataArguments dataArguments, Session session) throws OXException {
        /*
         * get target folder identifier
         */
        String folderID = dataArguments.get(ARGS[0]);
        if (Strings.isEmpty(folderID)) {
            throw DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[0], dataArguments.get(ARGS[0]));
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        InputStream inputStream = (InputStream) data.getData();
        SearchIterator<VCardImport> searchIterator = null;
        try {
            /*
             * parse vCards
             */
            JSONArray jsonArray = new JSONArray();
            VCardService vCardService = services.getService(VCardService.class);
            ContactService contactService = services.getService(ContactService.class);

            VCardStorageFactory vCardStorageFactory = services.getOptionalService(VCardStorageFactory.class);

            VCardStorageService vCardStorageService = null;
            if (vCardStorageFactory != null) {
                vCardStorageService = contactService.supports(serverSession, folderID, ContactField.VCARD_ID) ?
                    vCardStorageFactory.getVCardStorageService(services.getService(ConfigViewFactory.class), session.getContextId()) : null;
            }

            searchIterator = vCardService.importVCards(inputStream, vCardService.createParameters(session).setKeepOriginalVCard(null != vCardStorageService));
            while (searchIterator.hasNext()) {
                /*
                 * import each contact
                 */
                VCardImport vCardImport = null;
                boolean saved = false;
                String vCardID = null;
                try {
                    vCardImport = searchIterator.next();
                    Contact contact = vCardImport.getContact();
                    if (null != vCardStorageService) {
                        InputStream vCardStream = null;
                        try {
                            vCardStream = vCardImport.getVCard().getStream();
                            vCardID = vCardStorageService.saveVCard(vCardStream, session.getContextId());
                            contact.setVCardId(vCardID);
                        } finally {
                            Streams.close(vCardStream);
                        }
                    }
                    try {
                        contactService.createContact(serverSession, folderID, contact);
                        saved = true;
                    } catch (OXException e) {
                        org.slf4j.LoggerFactory.getLogger(ContactInsertDataHandler.class).debug("error storing contact", e);
                        throw handleDataTruncation(e);
                    } finally {
                        if (false == saved && null != vCardID) {
                            vCardStorageService.deleteVCard(vCardID, session.getContextId());
                        }
                    }
                    /*
                     * include new identifiers in result
                     */
                    jsonArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, folderID).put(DataFields.ID, contact.getObjectID()));
                } catch (JSONException e) {
                    throw DataExceptionCodes.ERROR.create(e, e.getMessage());
                } finally {
                    Streams.close(vCardImport);
                }
            }
            /*
             * return JSON array of imported contacts
             */
            return jsonArray;
        } finally {
            SearchIterators.close(searchIterator);
            Streams.close(inputStream);
        }
    }

    private static OXException handleDataTruncation(OXException e) {
        if (ContactExceptionCodes.DATA_TRUNCATION.equals(e) && null != e.getProblematics()) {
            StringBuilder stringBuilder = new StringBuilder();
            List<MappedTruncation<Contact>> truncations = MappedTruncation.extract(e.getProblematics());
            if (0 < truncations.size()) {
                stringBuilder.append(truncations.get(0).getReadableName());
            }
            for (int i = 1; i < truncations.size(); i++) {
                stringBuilder.append(", ").append(truncations.get(i).getReadableName());
            }
            return DataExceptionCodes.TRUNCATED.create(stringBuilder.toString());
        }
        return e;
    }
}
