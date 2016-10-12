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

import static com.openexchange.importexport.formats.csv.CSVLibrary.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactStringGetter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.actions.exporter.ContactExportAction;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CSVContactExporter implements Exporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSVContactExporter.class);

    /**
     * All possible contact fields as used by the CSV contact exporter
     */
    protected static final EnumSet<ContactField> POSSIBLE_FIELDS = EnumSet.of(
        ContactField.OBJECT_ID, ContactField.CREATED_BY, ContactField.CREATION_DATE, ContactField.LAST_MODIFIED, ContactField.MODIFIED_BY,
        // CommonObject.PRIVATE_FLAG, // CommonObject.CATEGORIES,
        ContactField.CATEGORIES,
        ContactField.SUR_NAME, ContactField.ANNIVERSARY, ContactField.ASSISTANT_NAME, ContactField.BIRTHDAY, ContactField.BRANCHES,
        ContactField.BUSINESS_CATEGORY, ContactField.CATEGORIES, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2,
        ContactField.CITY_BUSINESS, ContactField.CITY_HOME, ContactField.CITY_OTHER, ContactField.COMMERCIAL_REGISTER,
        ContactField.COMPANY, ContactField.COUNTRY_BUSINESS, ContactField.COUNTRY_HOME, ContactField.COUNTRY_OTHER,
        ContactField.DEPARTMENT, ContactField.DISPLAY_NAME, ContactField.DISTRIBUTIONLIST, ContactField.EMAIL1, ContactField.EMAIL2,
        ContactField.EMAIL3, ContactField.EMPLOYEE_TYPE, ContactField.FAX_BUSINESS, ContactField.FAX_HOME, ContactField.FAX_OTHER,
        // ContactFieldObject.FILE_AS,
        ContactField.FOLDER_ID, ContactField.GIVEN_NAME,
        // ContactFieldObject.IMAGE1, // ContactFieldObject.IMAGE1_CONTENT_TYPE,
        ContactField.INFO, ContactField.INSTANT_MESSENGER1, ContactField.INSTANT_MESSENGER2,
        // ContactFieldObject.LINKS,
        ContactField.MANAGER_NAME, ContactField.MARITAL_STATUS, ContactField.MIDDLE_NAME, ContactField.NICKNAME, ContactField.NOTE,
        ContactField.NUMBER_OF_CHILDREN, ContactField.NUMBER_OF_EMPLOYEE, ContactField.POSITION, ContactField.POSTAL_CODE_BUSINESS,
        ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_OTHER,
        // ContactFieldObject.PRIVATE_FLAG,
        ContactField.PROFESSION, ContactField.ROOM_NUMBER, ContactField.SALES_VOLUME, ContactField.SPOUSE_NAME,
        ContactField.STATE_BUSINESS,  ContactField.STATE_HOME, ContactField.STATE_OTHER, ContactField.STREET_BUSINESS,
        ContactField.STREET_HOME, ContactField.STREET_OTHER, ContactField.SUFFIX, ContactField.TAX_ID, ContactField.TELEPHONE_ASSISTANT,
        ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2, ContactField.TELEPHONE_CALLBACK, ContactField.TELEPHONE_CAR,
        ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_HOME1, ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_IP,
        ContactField.TELEPHONE_ISDN, ContactField.TELEPHONE_OTHER, ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_PRIMARY,
        ContactField.TELEPHONE_RADIO, ContactField.TELEPHONE_TELEX, ContactField.TELEPHONE_TTYTDD, ContactField.TITLE, ContactField.URL,
        ContactField.USERFIELD01, ContactField.USERFIELD02, ContactField.USERFIELD03, ContactField.USERFIELD04, ContactField.USERFIELD05,
        ContactField.USERFIELD06, ContactField.USERFIELD07, ContactField.USERFIELD08, ContactField.USERFIELD09, ContactField.USERFIELD10,
        ContactField.USERFIELD11, ContactField.USERFIELD12, ContactField.USERFIELD13, ContactField.USERFIELD14, ContactField.USERFIELD15,
        ContactField.USERFIELD16, ContactField.USERFIELD17, ContactField.USERFIELD18, ContactField.USERFIELD19, ContactField.USERFIELD20,
        ContactField.DEFAULT_ADDRESS
    );

    /**
     * The possible contact fields as array
     */
    protected static final ContactField[] POSSIBLE_FIELDS_ARRAY = POSSIBLE_FIELDS.toArray(new ContactField[POSSIBLE_FIELDS.size()]);


    @Override
    public boolean canExport(final ServerSession sessObj, final Format format, final String folder, final Map<String, Object> optionalParams) {
        if (!format.equals(Format.CSV)) {
            return false;
        }
        FolderObject fo;
        try {
            fo = getFolderObject(sessObj, folder);
        } catch (final OXException e) {
            return false;
        }
        // check format of folder
        if (fo.getModule() != FolderObject.CONTACT) {
            return false;
        }
        // check read access to folder
        EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(sessObj.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(
                sessObj.getUserId(),
                sessObj.getContext()));
        } catch (final OXException e) {
            return false;
        }
        return perm.canReadAllObjects();
    }

    @Override
    public SizedInputStream exportData(final ServerSession sessObj, final Format format, final String folder, final int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        if (!canExport(sessObj, format, folder, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
        }
        final int folderId = getFolderId(folder);

        ContactField[] fields;
        if (fieldsToBeExported == null || fieldsToBeExported.length == 0) {
            fields = POSSIBLE_FIELDS_ARRAY;
        } else {
            EnumSet<ContactField> illegalFields = EnumSet.complementOf(POSSIBLE_FIELDS);
            fields = ContactMapper.getInstance().getFields(fieldsToBeExported, illegalFields, (ContactField[])null);
        }

        final boolean exportDlists;
        if (optionalParams == null) {
            exportDlists = true;
        } else {
            exportDlists = optionalParams.containsKey(ContactExportAction.PARAMETER_EXPORT_DLISTS) ? Boolean.valueOf(optionalParams.get(ContactExportAction.PARAMETER_EXPORT_DLISTS).toString()).booleanValue() : true;
        }

        SearchIterator<Contact> conIter;
        try {
            if (!exportDlists) {
                List<ContactField> fieldList = Arrays.asList(fields.clone());
                if (!fieldList.contains(ContactField.MARK_AS_DISTRIBUTIONLIST)) {
                    fieldList = new ArrayList<>(fieldList);
                    fieldList.add(ContactField.MARK_AS_DISTRIBUTIONLIST);
                }
                conIter = ImportExportServices.getContactService().getAllContacts(sessObj, Integer.toString(folderId), fieldList.toArray(new ContactField[fieldList.size()]));
            } else {
                conIter = ImportExportServices.getContactService().getAllContacts(sessObj, Integer.toString(folderId), fields);
            }
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.LOADING_CONTACTS_FAILED.create(e);
        }

        boolean inMemory = false;
        if (inMemory) {
            final StringBuilder ret = new StringBuilder(65536);
            ret.append(convertToLine(com.openexchange.importexport.formats.csv.CSVLibrary.convertToList(fields)));
            try {
                while (conIter.hasNext()) {
                    Contact current;
                    try {
                        current = conIter.next();
                        if (!exportDlists && current.getMarkAsDistribtuionlist()) {
                            continue;
                        }
                        ret.append(convertToLine(convertToList(current, fields)));
                    } catch (final SearchIteratorException e) {
                        LOG.error("Could not retrieve contact from folder {} using a FolderIterator, exception was: ", folder, e);
                    } catch (final OXException e) {
                        LOG.error("Could not retrieve contact from folder {}, OXException was: ", folder, e);
                    }

                }
            } catch (final OXException e) {
                LOG.error("Could not retrieve contact from folder {} using a FolderIterator, exception was: ", folder, e);
            }
            final byte[] bytes = Charsets.getBytes(ret.toString(), Charsets.UTF_8);
            return new SizedInputStream(new ByteArrayInputStream(bytes), bytes.length, Format.CSV);
        }

        try {
            ThresholdFileHolder sink = new ThresholdFileHolder();
            OutputStreamWriter writer = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);
            writer.write(convertToLine(com.openexchange.importexport.formats.csv.CSVLibrary.convertToList(fields)));
            try {
                while (conIter.hasNext()) {
                    Contact current;
                    try {
                        current = conIter.next();
                        if (!exportDlists && current.getMarkAsDistribtuionlist()) {
                            continue;
                        }
                        writer.write(convertToLine(convertToList(current, fields)));
                    } catch (final SearchIteratorException e) {
                        LOG.error("Could not retrieve contact from folder {} using a FolderIterator, exception was: ", folder, e);
                    } catch (final OXException e) {
                        LOG.error("Could not retrieve contact from folder {}, OXException was: ", folder, e);
                    }

                }
            } catch (final OXException e) {
                LOG.error("Could not retrieve contact from folder {} using a FolderIterator, exception was: ", folder, e);
            }
            writer.flush();
            return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.CSV);
        } catch (IOException e) {
            throw ImportExportExceptionCodes.IOEXCEPTION.create(e, e.getMessage());
        }
    }

    @Override
    public SizedInputStream exportData(final ServerSession sessObj, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        if (!canExport(sessObj, format, folder, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
        }
        final int folderId = getFolderId(folder);
        ContactField[] fields;
        if (fieldsToBeExported == null || fieldsToBeExported.length == 0) {
            fields = POSSIBLE_FIELDS_ARRAY;
        } else {
            EnumSet<ContactField> illegalFields = EnumSet.complementOf(POSSIBLE_FIELDS);
            fields = ContactMapper.getInstance().getFields(fieldsToBeExported, illegalFields, (ContactField[])null);
        }
        final Contact conObj;
        try {
            conObj = ImportExportServices.getContactService().getContact(sessObj, Integer.toString(folderId), Integer.toString(objectId), fields);
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.LOADING_CONTACTS_FAILED.create(e);
        }

        boolean inMemory = false;
        if (inMemory) {
            StringBuilder ret = new StringBuilder(1024);
            ret.append(convertToLine(com.openexchange.importexport.formats.csv.CSVLibrary.convertToList(fields)));
            if (conObj.containsDistributionLists()) {
                ret.append(convertToLine(convertToList(conObj, fields)));
            }

            byte[] bytes = Charsets.getBytes(ret.toString(), Charsets.UTF_8);
            return new SizedInputStream(Streams.newByteArrayInputStream(bytes), bytes.length, Format.CSV);
        }

        try {
            ThresholdFileHolder sink = new ThresholdFileHolder();
            OutputStreamWriter writer = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);
            writer.write(convertToLine(com.openexchange.importexport.formats.csv.CSVLibrary.convertToList(fields)));
            if (conObj.containsDistributionLists()) {
                writer.write(convertToLine(convertToList(conObj, fields)));
            }
            writer.flush();
            return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.CSV);
        } catch (IOException e) {
            throw ImportExportExceptionCodes.IOEXCEPTION.create(e, e.getMessage());
        }
    }

    protected List<String> convertToList(final Contact conObj, final ContactField[] fields) {
        final List<String> l = new LinkedList<String>();
        final ContactStringGetter getter = new ContactStringGetter();
        getter.setDelegate(new ContactGetter());
        for (final ContactField field : fields) {
            try {
                l.add((String) field.doSwitch(getter, conObj));
            } catch (final OXException e) {
                l.add("");
            }
        }
        return l;
    }

    private static final Pattern PATTERN_QUOTE = Pattern.compile("\"", Pattern.LITERAL);

    protected String convertToLine(final List<String> line) {
        final StringBuilder bob = new StringBuilder(1024);
        for (final String str : line) {
            bob.append('"');
            bob.append(PATTERN_QUOTE.matcher(str).replaceAll("\"\""));
            bob.append('"');
            bob.append(CELL_DELIMITER);
        }
        bob.setCharAt(bob.length() - 1, ROW_DELIMITER);
        return bob.toString();
    }

}
