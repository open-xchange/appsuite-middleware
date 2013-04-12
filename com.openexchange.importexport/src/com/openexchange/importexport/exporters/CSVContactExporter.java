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

package com.openexchange.importexport.exporters;

import static com.openexchange.importexport.formats.csv.CSVLibrary.CELL_DELIMITER;
import static com.openexchange.importexport.formats.csv.CSVLibrary.ROW_DELIMITER;
import static com.openexchange.importexport.formats.csv.CSVLibrary.getFolderId;
import static com.openexchange.importexport.formats.csv.CSVLibrary.getFolderObject;
import static com.openexchange.importexport.formats.csv.CSVLibrary.transformIntArrayToSet;
import static com.openexchange.importexport.formats.csv.CSVLibrary.transformSetToIntArray;
import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactGetter;
import com.openexchange.groupware.contact.helpers.ContactStringGetter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Charsets;
import com.openexchange.log.LogFactory;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class CSVContactExporter implements Exporter {

    protected final static int[] POSSIBLE_FIELDS = {
        DataObject.OBJECT_ID,
        DataObject.CREATED_BY,
        DataObject.CREATION_DATE,
        DataObject.LAST_MODIFIED,
        DataObject.MODIFIED_BY,
        // CommonObject.PRIVATE_FLAG,
        // CommonObject.CATEGORIES,
        Contact.SUR_NAME,
        Contact.ANNIVERSARY,
        Contact.ASSISTANT_NAME,
        Contact.BIRTHDAY,
        Contact.BRANCHES,
        Contact.BUSINESS_CATEGORY,
        Contact.CATEGORIES,
        Contact.CELLULAR_TELEPHONE1,
        Contact.CELLULAR_TELEPHONE2,
        Contact.CITY_BUSINESS,
        Contact.CITY_HOME,
        Contact.CITY_OTHER,
        Contact.COMMERCIAL_REGISTER,
        Contact.COMPANY,
        Contact.COUNTRY_BUSINESS,
        Contact.COUNTRY_HOME,
        Contact.COUNTRY_OTHER,
        Contact.DEPARTMENT,
        Contact.DISPLAY_NAME,
        // ContactObject.DISTRIBUTIONLIST,
        Contact.EMAIL1,
        Contact.EMAIL2,
        Contact.EMAIL3,
        Contact.EMPLOYEE_TYPE,
        Contact.FAX_BUSINESS,
        Contact.FAX_HOME,
        Contact.FAX_OTHER,
        // ContactObject.FILE_AS,
        Contact.FOLDER_ID,
        Contact.GIVEN_NAME,
        // ContactObject.IMAGE1,
        // ContactObject.IMAGE1_CONTENT_TYPE,
        Contact.INFO,
        Contact.INSTANT_MESSENGER1,
        Contact.INSTANT_MESSENGER2,
        // ContactObject.LINKS,
        Contact.MANAGER_NAME, Contact.MARITAL_STATUS,
        Contact.MIDDLE_NAME,
        Contact.NICKNAME,
        Contact.NOTE,
        Contact.NUMBER_OF_CHILDREN,
        Contact.NUMBER_OF_EMPLOYEE,
        Contact.POSITION,
        Contact.POSTAL_CODE_BUSINESS,
        Contact.POSTAL_CODE_HOME,
        Contact.POSTAL_CODE_OTHER,
        // ContactObject.PRIVATE_FLAG,
        Contact.PROFESSION, Contact.ROOM_NUMBER, Contact.SALES_VOLUME, Contact.SPOUSE_NAME, Contact.STATE_BUSINESS, Contact.STATE_HOME,
        Contact.STATE_OTHER, Contact.STREET_BUSINESS, Contact.STREET_HOME, Contact.STREET_OTHER, Contact.SUFFIX, Contact.TAX_ID,
        Contact.TELEPHONE_ASSISTANT, Contact.TELEPHONE_BUSINESS1, Contact.TELEPHONE_BUSINESS2, Contact.TELEPHONE_CALLBACK,
        Contact.TELEPHONE_CAR, Contact.TELEPHONE_COMPANY, Contact.TELEPHONE_HOME1, Contact.TELEPHONE_HOME2, Contact.TELEPHONE_IP,
        Contact.TELEPHONE_ISDN, Contact.TELEPHONE_OTHER, Contact.TELEPHONE_PAGER, Contact.TELEPHONE_PRIMARY, Contact.TELEPHONE_RADIO,
        Contact.TELEPHONE_TELEX, Contact.TELEPHONE_TTYTDD, Contact.TITLE, Contact.URL, Contact.USERFIELD01, Contact.USERFIELD02,
        Contact.USERFIELD03, Contact.USERFIELD04, Contact.USERFIELD05, Contact.USERFIELD06, Contact.USERFIELD07, Contact.USERFIELD08,
        Contact.USERFIELD09, Contact.USERFIELD10, Contact.USERFIELD11, Contact.USERFIELD12, Contact.USERFIELD13, Contact.USERFIELD14,
        Contact.USERFIELD15, Contact.USERFIELD16, Contact.USERFIELD17, Contact.USERFIELD18, Contact.USERFIELD19, Contact.USERFIELD20,
        Contact.DEFAULT_ADDRESS };

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CSVContactExporter.class));

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
        // final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj, sessObj.getContext());
        int[] cols = null;
        if (fieldsToBeExported == null || fieldsToBeExported.length == 0) {
            cols = POSSIBLE_FIELDS;
        } else {
            final Set<Integer> s1 = transformIntArrayToSet(fieldsToBeExported);
            final Set<Integer> s2 = transformIntArrayToSet(POSSIBLE_FIELDS);
            s1.retainAll(s2);
            cols = transformSetToIntArray(s1);
        }
        ContactField[] fields = ContactMapper.getInstance().getFields(cols, null, (ContactField[])null);
        SearchIterator<Contact> conIter;
        try {
            conIter = ImportExportServices.getContactService().getAllContacts(sessObj, Integer.toString(folderId), fields);
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.LOADING_CONTACTS_FAILED.create(e);
        }
        final StringBuilder ret = new StringBuilder();
        ret.append(convertToLine(com.openexchange.importexport.formats.csv.CSVLibrary.convertToList(cols)));

        try {
            while (conIter.hasNext()) {
                Contact current;
                try {
                    current = conIter.next();
                    ret.append(convertToLine(convertToList(current, cols)));
                } catch (final SearchIteratorException e) {
                    LOG.error("Could not retrieve contact from folder " + folder + " using a FolderIterator, exception was: ", e);
                } catch (final OXException e) {
                    LOG.error("Could not retrieve contact from folder " + folder + ", OXException was: ", e);
                }

            }
        } catch (final OXException e) {
            LOG.error("Could not retrieve contact from folder " + folder + " using a FolderIterator, exception was: ", e);
        }
        final byte[] bytes = Charsets.getBytes(ret.toString(), Charsets.UTF_8);
        return new SizedInputStream(new ByteArrayInputStream(bytes), bytes.length, Format.CSV);
    }

    @Override
    public SizedInputStream exportData(final ServerSession sessObj, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, Object> optionalParams) throws OXException {
        if (!canExport(sessObj, format, folder, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_EXPORT.create(folder, format);
        }
        final int folderId = getFolderId(folder);
        // final ContactSQLInterface contactSql = new RdbContactSQLInterface(sessObj, sessObj.getContext());
        int[] cols;
        if (fieldsToBeExported == null || fieldsToBeExported.length == 0) {
            cols = POSSIBLE_FIELDS;
        } else {
            cols = fieldsToBeExported;
        }
        ContactField[] fields = ContactMapper.getInstance().getFields(cols, null, (ContactField[])null);
        final Contact conObj;
        try {
            conObj = ImportExportServices.getContactService().getContact(sessObj, Integer.toString(folderId), Integer.toString(objectId), fields);
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.LOADING_CONTACTS_FAILED.create(e);
        }

        final StringBuilder ret = new StringBuilder();
        ret.append(convertToLine(com.openexchange.importexport.formats.csv.CSVLibrary.convertToList(cols)));
        ret.append(convertToLine(convertToList(conObj, cols)));

        final byte[] bytes = Charsets.getBytes(ret.toString(), Charsets.UTF_8);
        return new SizedInputStream(new ByteArrayInputStream(bytes), bytes.length, Format.CSV);
    }

    protected List<String> convertToList(final Contact conObj, final int[] cols) {
        final List<String> l = new LinkedList<String>();
        final ContactStringGetter getter = new ContactStringGetter();
        getter.setDelegate(new ContactGetter());
        ContactField tempField;
        for (final int col : cols) {
            tempField = ContactField.getByValue(col);
            try {
                l.add((String) tempField.doSwitch(getter, conObj));
            } catch (final OXException e) {
                l.add("");
            }
        }
        return l;
    }

    protected String convertToLine(final List<String> line) {
        final StringBuilder bob = new StringBuilder();
        for (final String str : line) {
            bob.append('"');
            bob.append(str.replace("\"", "\"\""));
            bob.append('"');
            bob.append(CELL_DELIMITER);
        }
        bob.setCharAt(bob.length() - 1, ROW_DELIMITER);
        return bob.toString();
    }

}
