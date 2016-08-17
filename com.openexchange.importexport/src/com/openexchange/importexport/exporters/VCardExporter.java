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

package com.openexchange.importexport.exporters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface)
 */
public class VCardExporter implements Exporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VCardExporter.class);
    protected final static int[] _contactFields = {
        DataObject.OBJECT_ID,
        DataObject.CREATED_BY,
        DataObject.CREATION_DATE,
        DataObject.LAST_MODIFIED,
        DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID,
        CommonObject.CATEGORIES,
        Contact.GIVEN_NAME,
        Contact.SUR_NAME,
        Contact.ANNIVERSARY,
        Contact.ASSISTANT_NAME,
        Contact.BIRTHDAY,
        Contact.BRANCHES,
        Contact.BUSINESS_CATEGORY,
        Contact.CELLULAR_TELEPHONE1,
        Contact.CELLULAR_TELEPHONE2,
        Contact.CITY_BUSINESS,
        Contact.CITY_HOME,
        Contact.CITY_OTHER,
        Contact.COLOR_LABEL,
        Contact.COMMERCIAL_REGISTER,
        Contact.COMPANY,
        Contact.COUNTRY_BUSINESS,
        Contact.COUNTRY_HOME,
        Contact.COUNTRY_OTHER,
        Contact.DEPARTMENT,
        Contact.DISPLAY_NAME,
        Contact.DISTRIBUTIONLIST,
        Contact.EMAIL1,
        Contact.EMAIL2,
        Contact.EMAIL3,
        Contact.EMPLOYEE_TYPE,
        Contact.FAX_BUSINESS,
        Contact.FAX_HOME,
        Contact.FAX_OTHER,
        Contact.INFO,
        Contact.INSTANT_MESSENGER1,
        Contact.INSTANT_MESSENGER2,
        Contact.IMAGE1,
        Contact.IMAGE1_CONTENT_TYPE,
        Contact.MANAGER_NAME,
        Contact.MARITAL_STATUS,
        Contact.MIDDLE_NAME,
        Contact.NICKNAME,
        Contact.NOTE,
        Contact.NUMBER_OF_CHILDREN,
        Contact.NUMBER_OF_EMPLOYEE,
        Contact.POSITION,
        Contact.POSTAL_CODE_BUSINESS,
        Contact.POSTAL_CODE_HOME,
        Contact.POSTAL_CODE_OTHER,
        Contact.PRIVATE_FLAG,
        Contact.PROFESSION,
        Contact.ROOM_NUMBER,
        Contact.SALES_VOLUME,
        Contact.SPOUSE_NAME,
        Contact.STATE_BUSINESS,
        Contact.STATE_HOME,
        Contact.STATE_OTHER,
        Contact.STREET_BUSINESS,
        Contact.STREET_HOME,
        Contact.STREET_OTHER,
        Contact.SUFFIX,
        Contact.TAX_ID,
        Contact.TELEPHONE_ASSISTANT,
        Contact.TELEPHONE_BUSINESS1,
        Contact.TELEPHONE_BUSINESS2,
        Contact.TELEPHONE_CALLBACK,
        Contact.TELEPHONE_CAR,
        Contact.TELEPHONE_COMPANY,
        Contact.TELEPHONE_HOME1,
        Contact.TELEPHONE_HOME2,
        Contact.TELEPHONE_IP,
        Contact.TELEPHONE_ISDN,
        Contact.TELEPHONE_OTHER,
        Contact.TELEPHONE_PAGER,
        Contact.TELEPHONE_PRIMARY,
        Contact.TELEPHONE_RADIO,
        Contact.TELEPHONE_TELEX,
        Contact.TELEPHONE_TTYTDD,
        Contact.TITLE,
        Contact.URL,
        Contact.USERFIELD01,
        Contact.USERFIELD02,
        Contact.USERFIELD03,
        Contact.USERFIELD04,
        Contact.USERFIELD05,
        Contact.USERFIELD06,
        Contact.USERFIELD07,
        Contact.USERFIELD08,
        Contact.USERFIELD09,
        Contact.USERFIELD10,
        Contact.USERFIELD11,
        Contact.USERFIELD12,
        Contact.USERFIELD13,
        Contact.USERFIELD14,
        Contact.USERFIELD15,
        Contact.USERFIELD16,
        Contact.USERFIELD17,
        Contact.USERFIELD18,
        Contact.USERFIELD19,
        Contact.USERFIELD20,
        Contact.DEFAULT_ADDRESS,
        Contact.YOMI_FIRST_NAME,
        Contact.YOMI_LAST_NAME
    };

    @Override
    public boolean canExport(final ServerSession session, final Format format, final String folder, final Map<String, Object> optionalParams) throws OXException {
        if (!format.equals(Format.VCARD)) {
            return false;
        }

        final int folderId = Integer.parseInt(folder);
        final FolderObject fo;
        try {
            fo = new OXFolderAccess(session.getContext()).getFolderObject(folderId);
        } catch (final OXException e) {
            return false;
        }
        //check format of folder
        if (fo.getModule() == FolderObject.CONTACT) {
            if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasContact()) {
                return false;
            }
        } else {
            return false;
        }
        //check read access to folder
        final EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(session.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()));
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.NO_DATABASE_CONNECTION.create(e);
        } catch (final RuntimeException e) {
            throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return perm.canReadAllObjects();
    }

    @Override
    public SizedInputStream exportData(final ServerSession session, final Format format, final String folder, int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        return exportData(session, format, folder, 0, fieldsToBeExported, optionalParams);
    }

    @Override
    public SizedInputStream exportData(final ServerSession session, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        String oId = null;
        try {
            oId = objectId > 0 ? Integer.toString(objectId) : null;
        } catch (NumberFormatException e) {
            throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folder);
        }

        try {
            AJAXRequestData requestData = (AJAXRequestData) (optionalParams == null ? null : optionalParams.get("__requestData"));
            if (null != requestData) {
                // Try to stream
                final OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk(optionalParams) ? "application/octet-stream" : Format.VCARD.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment; filename=" + Format.VCARD.getFullName() + "." + Format.VCARD.getExtension());
                    requestData.removeCachingHeader();
                    export(session, folder, oId, fieldsToBeExported, new OutputStreamWriter(out, DEFAULT_CHARSET));
                    return null;
                }
            }

            // No streaming support possible
            ThresholdFileHolder sink = new ThresholdFileHolder();
            boolean error = true;
            try {
                export(session, folder, oId, fieldsToBeExported, new OutputStreamWriter(sink.asOutputStream(), DEFAULT_CHARSET));
                SizedInputStream sizedIn = new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.VCARD);
                error = false;
                return sizedIn;
            } finally {
                if (error) {
                    Streams.close(sink);
                }
            }
        } catch (final IOException e) {
            throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
        }
    }

    private static final ContactField[] FIELDS_ID = new ContactField[] { ContactField.OBJECT_ID };

    private void export(final ServerSession session, final String folderId, final String objectId, int[] fieldsToBeExported, Writer writer) throws OXException {
        ContactField[] fields;
        if (fieldsToBeExported == null || fieldsToBeExported.length == 0) {
            fields = ContactMapper.getInstance().getFields(_contactFields);
            List<ContactField> tmp = new ArrayList<ContactField>();
            tmp.addAll(Arrays.asList(fields));
            tmp.add(ContactField.VCARD_ID);
            fields = tmp.toArray(new ContactField[tmp.size()]);
        } else {
            // In this case the original vCard must not be merged. Since the ContactMapper does not even map the VCARD_ID column it will not be considered when exporting.
            fields = ContactMapper.getInstance().getFields(fieldsToBeExported);
        }

        // Get required contact service
        ContactService contactService = ImportExportServices.getContactService();

        // Either export a single contact...
        if (objectId != null) {
            Contact contactObj = contactService.getContact(session, folderId, objectId, fields);
            exportContact(session, contactObj, null, null, writer);
            return;
        }

        // ... or export a collection of contacts
        SearchIterator<Contact> searchIterator = contactService.getAllContacts(session, folderId, FIELDS_ID);
        try {
            VCardStorageService vCardStorage = ImportExportServices.getVCardStorageService(session.getContextId());
            VCardService vCardService = ImportExportServices.getVCardService();
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                try {
                    Contact fullContact = contactService.getContact(session, folderId, Integer.toString(contact.getObjectID()), fields);
                    exportContact(session, fullContact, vCardService, vCardStorage, writer);
                } catch (OXException e) {
                    if (!ContactExceptionCodes.CONTACT_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                }
            }
        } finally {
            try {
                writer.flush();
            } catch (IOException e) {
                throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
            }
            SearchIterators.close(searchIterator);
        }
    }

    protected void exportContact(ServerSession session, Contact contactObj, VCardService optVCardService, VCardStorageService optVCardStorageService, Writer writer) throws OXException {
        if (contactObj.containsDistributionLists() || contactObj.getMarkAsDistribtuionlist()) {
            // Ignore distribution list
            return;
        }

        InputStream originalVCard = null;
        Reader vcardReader = null;
        try {
            VCardStorageService vCardStorage = null == optVCardStorageService ? ImportExportServices.getVCardStorageService(session.getContextId()) : optVCardStorageService;
            if (vCardStorage != null && contactObj.getVCardId() != null) {
                originalVCard = vCardStorage.getVCard(contactObj.getVCardId(), session.getContextId());
            }

            VCardExport vCardExport = (null == optVCardService ? ImportExportServices.getVCardService() : optVCardService).exportContact(contactObj, originalVCard, null);
            Streams.close(originalVCard);
            originalVCard = null;

            vcardReader = new InputStreamReader(vCardExport.getClosingStream(), DEFAULT_CHARSET);
            int buflen = 65536;
            char[] cbuf = new char[buflen];
            for (int read; (read = vcardReader.read(cbuf, 0, buflen)) > 0;) {
                writer.write(cbuf, 0, read);
            }
        } catch (IOException e) {
            throw ImportExportExceptionCodes.VCARD_CONVERSION_FAILED.create(e);
        } finally {
            Streams.close(originalVCard, vcardReader);
        }
    }

    private boolean isSaveToDisk(final Map<String, Object> optionalParams) {
        if (null == optionalParams) {
            return false;
        }
        final Object object = optionalParams.get("__saveToDisk");
        if (null == object) {
            return false;
        }
        return (object instanceof Boolean ? ((Boolean) object).booleanValue() : Boolean.parseBoolean(object.toString().trim()));
    }
}
