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

package com.openexchange.contacts.json.converters;

import java.io.InputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.conversion.ConversionResult;
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
    public ConversionResult processData(Data<?> data, DataArguments dataArguments, Session session) throws OXException {
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
            IDBasedContactsAccess access = services.getService(IDBasedContactsAccessFactory.class).createAccess(serverSession);
            
            VCardStorageFactory vCardStorageFactory = services.getOptionalService(VCardStorageFactory.class);

            VCardStorageService vCardStorageService = null;
            if (vCardStorageFactory != null) {
                vCardStorageService = access.supports(folderID, ContactField.VCARD_ID) ?
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
                        access.createContact(folderID, contact);
                        saved = true;
                    } catch (OXException e) {
                        org.slf4j.LoggerFactory.getLogger(ContactInsertDataHandler.class).debug("error storing contact", e);
                        throw handleDataTruncation(e);
                    } finally {
                        if (false == saved && null != vCardID && null != vCardStorageService) {
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
            ConversionResult result = new ConversionResult();
            result.setData(jsonArray);
            return result;
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
