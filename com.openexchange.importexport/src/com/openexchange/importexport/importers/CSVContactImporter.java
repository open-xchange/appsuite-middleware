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

package com.openexchange.importexport.importers;

import static com.openexchange.importexport.formats.csv.CSVLibrary.getFolderObject;
import static com.openexchange.importexport.formats.csv.CSVLibrary.transformInputStreamToString;
import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForTimestamp;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.csv.ContactFieldMapper;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.Collections;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * Importer for OX own CSV file format - this format is able to represent a contact with all fields that appear in the OX.
 *
 * @see com.openexchange.importexport.importers.OutlookCSVContactImporter - imports files produced by Outlook
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class CSVContactImporter extends AbstractImporter {

	private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CSVContactImporter.class));

	private LinkedList<ContactFieldMapper> mappers;

	private ContactFieldMapper currentMapper;

	
    @Override
    public boolean canImport(final ServerSession session, final Format format, final List<String> folders, final Map<String, String[]> optionalParams) throws OXException {
        if (!isResponsibleFor(format)) {
            return false;
        }

        if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasContact()) {
            throw ImportExportExceptionCodes.CONTACTS_DISABLED.create().setGeneric(Generic.NO_PERMISSION);
        }

        String folder;
        if (folders.size() != 1) {
            throw ImportExportExceptionCodes.ONLY_ONE_FOLDER.create();
        }
        folder = folders.get(0);

        FolderObject fo = null;
        try {
            fo = getFolderObject(session, folder);
        } catch (final OXException e) {
            return false;
        }
        if (fo == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Folder does not exist: " + folder);
            }
            return false;
        }
        // check format of folder
        if (fo.getModule() != FolderObject.CONTACT) {
            return false;
        }
        // check read access to folder
        EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(
                session.getUserId(),
                UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()));
        } catch (final OXException e) {
            return false;
        } catch (final SQLException e) {
            return false;
        }
        return perm.canCreateObjects();
    }


    @Override
    public List<ImportResult> importData(final ServerSession sessObj, final Format format, final InputStream is, final List<String> folders, final Map<String, String[]> optionalParams) throws OXException {
        final String folder = folders.get(0);
        if (!canImport(sessObj, format, folders, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_IMPORT.create(folder, format);
        }
        String csvStr = transformInputStreamToString(is, "UTF-8");
        CSVParser csvParser = getCSVParser();
        List<List<String>> csv = csvParser.parse(csvStr);

        final Iterator<List<String>> iter = csv.iterator();
        // get header fields
        final List<String> fields = iter.next();
        if (!checkFields(fields)) {
            throw ImportExportExceptionCodes.NO_VALID_CSV_COLUMNS.create();
        }
        if (!passesSanityTestForDisplayName(fields)) {
            throw ImportExportExceptionCodes.NO_FIELD_FOR_NAMING.create();
        }

        // re-read now the we have guessed the format and the encoding
        if (!"UTF-8".equalsIgnoreCase(currentMapper.getEncoding())) {
            csvStr = transformInputStreamToString(is, currentMapper.getEncoding());
            csv = csvParser.parse(csvStr);
        }

        // reading entries...
        final List<ImportIntention> intentions = new LinkedList<ImportIntention>();
        final ContactSwitcher conSet = getContactSwitcher();
        int lineNumber = 1;
        while (iter.hasNext()) {
            // ...and writing them
            intentions.add(writeEntry(fields, iter.next(), folder, conSet, lineNumber++, sessObj));
        }

        // Build a list of contacts to insert

        final List<Contact> contacts = new LinkedList<Contact>();
        for(final ImportIntention intention : intentions) {
            if (intention.contact != null) {
                contacts.add(intention.contact);
            }
        }

        // Insert or update contacts

        final FolderUpdaterRegistry updaterRegistry = ImportExportServices.getUpdaterRegistry();
        final TargetFolderDefinition target = new TargetFolderDefinition(folder, sessObj.getUserId(), sessObj.getContext());

        try {
            final FolderUpdaterService<Contact> folderUpdater = updaterRegistry.getFolderUpdater(target);
            if (folderUpdater == null) {
                throw ImportExportExceptionCodes.CANNOT_IMPORT.create();
            }
            folderUpdater.save(contacts, target);
        } catch (final OXException e) {
            throw new OXException(e);
        }


        // Build result list

        final List<ImportResult> results = new LinkedList<ImportResult>();

        for(final ImportIntention intention : intentions) {
        	if (intention.contact != null && intention.contact.getObjectID() != 0) {
                final ImportResult result = new ImportResult();
                result.setFolder(folder);
                result.setObjectId(Integer.toString(intention.contact.getObjectID()));
                result.setDate(intention.contact.getLastModified());
                if (intention.result != null) {
                    result.setException(intention.result.getException());
                }
                results.add(result);
            } else if (intention.contact != null && intention.contact.getObjectID() == 0) {
            	ImportResult notCreated = new ImportResult();
            	notCreated.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create(intention.contact));
            	results.add(notCreated);
            } else if (intention.result != null) {
                results.add(intention.result);
            }
        }

        return results;
    }

    /**
     * @param fields Headers of the table; column title
     * @param entry A list of row cells.
     * @param folder The folder this is line meant to be written into
     * @param conSet The ContactSetter used for translating the given data
     * @param lineNumber Number of the entry ins the CSV file (used for precise error message)
     * @param session The session
     * @return a report containing either the object ID of the entry created OR an error message
     */
    protected ImportIntention writeEntry(final List<String> fields, final List<String> entry, final String folder, final ContactSwitcher conSet, final int lineNumber, final ServerSession session) {
        final boolean canOverrideInCaseOfTruncation = false;
        final ImportResult result = new ImportResult();
        result.setFolder(folder);
        try {
            final boolean[] atLeastOneFieldInserted = new boolean[] { false };
            final Contact contactObj = convertCsvToContact(fields, entry, conSet, lineNumber, result, atLeastOneFieldInserted);
            if (!contactObj.canFormDisplayName()) {
                result.setException(ImportExportExceptionCodes.NO_FIELD_FOR_NAMING_IN_LINE.create(I(lineNumber)));
                result.setDate(new Date());
                return new ImportIntention(result);
            }

            contactObj.setParentFolderID(Integer.parseInt(folder.trim()));
            if (atLeastOneFieldInserted[0]) {
//                final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
//                    ContactInterfaceDiscoveryService.class).newContactInterface(contactObj.getParentFolderID(), session);
//                if (contactInterface instanceof OverridingContactInterface) {
//                    ((OverridingContactInterface) contactInterface).forceInsertContactObject(contactObj);
//                    canOverrideInCaseOfTruncation = true;
//                } else {
//                    contactInterface.insertContactObject(contactObj);
//                }
//                result.setObjectId(Integer.toString(contactObj.getObjectID()));
//                result.setDate(contactObj.getLastModified());
                if (result.getException() != null) {
                    return new ImportIntention(result, contactObj);
                }
                return new ImportIntention(contactObj);
            } else {
                result.setException(ImportExportExceptionCodes.NO_FIELD_IMPORTED.create(I(lineNumber)));
                result.setDate(new Date());
            }
        } catch (final OXException e) {
            if (e.getCategory() != Category.CATEGORY_TRUNCATED || (e.getCategory() == Category.CATEGORY_TRUNCATED && !canOverrideInCaseOfTruncation)) {
                result.setException(e);
                addErrorInformation(result, lineNumber, fields);
            }
        }
        return new ImportIntention(result);
    }

    public Contact convertCsvToContact(final List<String> fields, final List<String> entry, final ContactSwitcher conSet, final int lineNumber, final ImportResult result, final boolean[] atLeastOneFieldInserted) throws OXException {
        final Contact contactObj = new Contact();
        final Collection<OXException> warnings = new LinkedList<OXException>();
        final List<String> wrongFields = new LinkedList<String>();
        boolean atLeastOneFieldWithWrongName = false;
        for (int i = 0; i < fields.size(); i++) {
            final String fieldName = fields.get(i);
            final String currEntry = entry.get(i);
            final ContactField currField = getRelevantField(fieldName);
            if (currField == null) {
                final boolean worked = conSet._unknownfield(contactObj, fieldName, currEntry);
                if (worked) {
                    continue;
                }
                atLeastOneFieldWithWrongName = true;
                wrongFields.add(fieldName);
            } else {
                if (!currEntry.equals("")) {
                    currField.doSwitch(conSet, contactObj, currEntry);
                    final Collection<OXException> warns = contactObj.getWarnings();
                    if (!warns.isEmpty()) {
                        warnings.add(ImportExportExceptionCodes.IGNORE_FIELD.create(warns.iterator().next(), fieldName, currEntry));
                    }
                }
                atLeastOneFieldInserted[0] = true;
            }
        }
        if (!warnings.isEmpty()) {
            final OXException warning = warnings.iterator().next();
            result.setException(warning);
            addErrorInformation(result, lineNumber, fields);
        } else if (atLeastOneFieldWithWrongName) {
            result.setException(ImportExportExceptionCodes.NOT_FOUND_FIELD.create(wrongFields.toString()));
            addErrorInformation(result, lineNumber, fields);
        }
        return contactObj;
    }

    /**
     * Adds error information to a given ImportResult
     *
     * @param result ImportResult to be written into.
     * @param lineNumber Number of the buggy line in the CSV script.
     * @param entry CSV line that was buggy.
     */
    protected void addErrorInformation(final ImportResult result, final int lineNumber, final List<String> entry) {
        result.setEntryNumber(lineNumber);
    }

    public ContactSwitcher getContactSwitcher() {
        final ContactSwitcherForSimpleDateFormat dateSwitch = new ContactSwitcherForSimpleDateFormat();
        dateSwitch.addDateFormat(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM));

        final TimeZone utc = TimeZoneUtils.getTimeZone("UTC");
        final SimpleDateFormat df1 = new SimpleDateFormat("dd.MM.yyyy");
        df1.setTimeZone(utc);

        final SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
        df2.setTimeZone(utc);

        final SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
        df3.setTimeZone(utc);

        dateSwitch.addDateFormat(df1);
        dateSwitch.addDateFormat(df2);
        dateSwitch.addDateFormat(df3);

        final ContactSwitcherForTimestamp timestampSwitch = new ContactSwitcherForTimestamp();
        final ContactSwitcherForBooleans boolSwitch = new ContactSwitcherForBooleans();
        boolSwitch.setDelegate(timestampSwitch);
        timestampSwitch.setDelegate(dateSwitch);
        dateSwitch.setDelegate(new ContactSetter());
        return boolSwitch;
    }

    @Override
    protected String getNameForFieldInTruncationError(final int id, final OXException unused) {
        final ContactField field = ContactField.getByValue(id);
        if (field == null) {
            return String.valueOf(id);
        }
        return field.getReadableName();
    }

    private static final class ImportIntention {
        public Contact contact;
        public ImportResult result;

        public ImportIntention(final Contact contact) {
            this.contact = contact;
        }

        public ImportIntention(final ImportResult result) {
            this.result = result;
        }

        public ImportIntention(final ImportResult result, final Contact contact) {
            this.result = result;
            this.contact = contact;
        }
    }
    
	public boolean checkFields(final List<String> fields) {
        int highestAmountOfMappedFields = 0;

        for (ContactFieldMapper mapper : mappers) {
            int mappedFields = 0;
            for (final String name : fields) {
                if (mapper.getFieldByName(name) != null) {
                    mappedFields++;
                }
            }
            if (mappedFields > highestAmountOfMappedFields) {
                currentMapper = mapper;
                highestAmountOfMappedFields = mappedFields;
            }

        }
        return currentMapper != null;
    }
    
    protected boolean passesSanityTestForDisplayName(List<String> headers) {
        return Collections.any(
            headers,
            currentMapper.getNameOfField(ContactField.DISPLAY_NAME),
            currentMapper.getNameOfField(ContactField.SUR_NAME),
            currentMapper.getNameOfField(ContactField.GIVEN_NAME),
            currentMapper.getNameOfField(ContactField.EMAIL1),
            currentMapper.getNameOfField(ContactField.EMAIL2),
            currentMapper.getNameOfField(ContactField.EMAIL3),
            currentMapper.getNameOfField(ContactField.COMPANY),
            currentMapper.getNameOfField(ContactField.NICKNAME),
            currentMapper.getNameOfField(ContactField.MIDDLE_NAME));
    }

	public void addFieldMapper(ContactFieldMapper mapper) {
		if (mappers == null) {
			mappers = new LinkedList<ContactFieldMapper>();
		}
		mappers.add(mapper);
	}
	
    protected ContactField getRelevantField(final String name) {
        return currentMapper.getFieldByName(name);
    }

    protected CSVParser getCSVParser() {
        final CSVParser result = new CSVParser();
        result.setTolerant(true);
        return result;
    }

    protected boolean isResponsibleFor(Format f) {
        return Format.CSV == f || Format.OUTLOOK_CSV == f;
    }

    public String getEncoding() {
        return currentMapper.getEncoding();
    }

}
