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

package com.openexchange.importexport.importers;

import static com.openexchange.importexport.formats.csv.CSVLibrary.getFolderObject;
import static com.openexchange.importexport.formats.csv.CSVLibrary.readLines;
import static com.openexchange.importexport.formats.csv.CSVLibrary.transformInputStreamToString;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForEmailAddresses;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForTimestamp;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.generic.FolderUpdaterServiceV2;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.csv.CSVLibrary;
import com.openexchange.importexport.formats.csv.ContactFieldMapper;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.Collections;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.session.ServerSession;

/**
 * Importer for OX own CSV file format - this format is able to represent a contact with all fields that appear in the OX.
 *
 * @see com.openexchange.importexport.importers.OutlookCSVContactImporter - imports files produced by Outlook
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class CSVContactImporter extends AbstractImporter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSVContactImporter.class);

    private LinkedList<ContactFieldMapper> mappers;

    private ContactFieldMapper currentMapper;


    public CSVContactImporter(ServiceLookup services) {
        super(services);
    }

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
            LOG.info("Folder does not exist: {}", folder);
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
        }
        return perm.canCreateObjects();
    }


    @Override
    public List<ImportResult> importData(final ServerSession sessObj, final Format format, final InputStream is, final List<String> folders, final Map<String, String[]> optionalParams) throws OXException {
        final String folder = folders.get(0);
        if (!canImport(sessObj, format, folders, optionalParams)) {
            throw ImportExportExceptionCodes.CANNOT_IMPORT.create(folder, format);
        }
        int limit = getLimit(sessObj);
        List<List<String>> csv = null;
        // get header fields
        List<String> fields;
        {
            InputStream input = null;
            try {
                input = is.markSupported() ? is : Streams.asInputStream(is);
                input.mark(Integer.MAX_VALUE);
                Charset charset = getCharsetFromParameters(optionalParams);
                int maxLines = 0 < limit ? limit + 2 : -1;
                csv = null != charset ? parse(input, charset, maxLines) : parse(input, maxLines);
                if (csv == null) {
                    throw ImportExportExceptionCodes.NO_VALID_CSV_COLUMNS.create();
                }
                if (csv.size() < 2) {
                    throw ImportExportExceptionCodes.NO_CONTENT.create();
                }
                fields = csv.get(0);
                if (!passesSanityTestForDisplayName(fields)) {
                    throw ImportExportExceptionCodes.NO_FIELD_FOR_NAMING.create();
                }

            } catch (final IOException e) {
                throw ImportExportExceptionCodes.IOEXCEPTION.create(e);
            } finally {
                Streams.close(input);
            }
        }

        // reading entries...
        final List<ImportIntention> intentions;
        {
            final int size = csv.size();
            intentions = new ArrayList<ImportIntention>(size);
            final ContactSwitcher conSet = getContactSwitcher();
            for (int lineNumber = 1; lineNumber < size; lineNumber++) {
                // ...and writing them
                final List<String> row = csv.get(lineNumber);
                final ImportIntention intention = createIntention(fields, row, folder, conSet, lineNumber, sessObj);
                intentions.add(intention);
            }
        }

        int count = 0;
        // Build a list of contacts to insert
        final List<Contact> contacts = new ArrayList<Contact>(intentions.size());
        for (final ImportIntention intention : intentions) {
            if (intention.contact != null) {
                if (limit <= 0 || count <= limit) {
                    contacts.add(intention.contact);
                    count++;
                } else {
                    intention.exceedsLimit = true;
                }
            }
        }

        // Insert or update contacts
        final FolderUpdaterRegistry updaterRegistry = ImportExportServices.getUpdaterRegistry();
        final TargetFolderDefinition target = new TargetFolderDefinition(folder, sessObj.getUserId(), sessObj.getContext());
        final List<OXException> errors = new LinkedList<OXException>();
        {
            final FolderUpdaterService<Contact> folderUpdater = updaterRegistry.getFolderUpdater(target);
            if (folderUpdater == null) {
                throw ImportExportExceptionCodes.CANNOT_IMPORT.create();
            }
            SearchIteratorDelegator<Contact> searchIterator = new SearchIteratorDelegator<Contact>(contacts);
            if (folderUpdater instanceof FolderUpdaterServiceV2) {
                ((FolderUpdaterServiceV2<Contact>) folderUpdater).save(searchIterator, target, errors);
            } else {
                folderUpdater.save(searchIterator, target);
            }
        }

        // Build result list
        final List<ImportResult> results = new ArrayList<ImportResult>(intentions.size());

        boolean exceeds = false;
        for (final ImportIntention intention : intentions) {
            final boolean notNull = intention.contact != null;
            final boolean isZero = notNull ? intention.contact.getObjectID() == 0 : true;
            if (intention.exceedsLimit) {
                exceeds = true;
            } else if (notNull && !isZero) {
                final ImportResult result = new ImportResult();
                result.setFolder(folder);
                result.setObjectId(Integer.toString(intention.contact.getObjectID()));
                result.setDate(intention.contact.getLastModified());
                if (intention.result != null) {
                    result.setException(intention.result.getException());
                }
                results.add(result);
            } else if (notNull && isZero) {
                final ImportResult notCreated = new ImportResult();
                if (errors.isEmpty()) {
                    notCreated.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create(intention.contact));
                } else {
                    notCreated.setException(errors.get(0));
                }
                results.add(notCreated);
            } else if (intention.result != null) {
                results.add(intention.result);
            }
        }

        if (exceeds) {
            throw ImportExportExceptionCodes.LIMIT_EXCEEDED.create(limit);
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
    protected ImportIntention createIntention(final List<String> fields, final List<String> entry, final String folder, final ContactSwitcher conSet, final int lineNumber, final ServerSession session) {
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

    public Contact convertCsvToContact(List<String> fields, List<String> entry, ContactSwitcher conSet, int lineNumber, ImportResult result, boolean[] atLeastOneFieldInserted) throws OXException {
        final Contact contactObj = new Contact();
        final Collection<OXException> warnings = new LinkedList<OXException>();
        final List<String> wrongFields = new LinkedList<String>();

        boolean atLeastOneFieldWithWrongName = false;
        boolean markAsDistributionlist = false;
        for (int i = 0, k = fields.size(); k -- > 0; i++) {
            final String fieldName = fields.get(i);
            final String currEntry = entry.get(i);

            boolean isMarkAsDistributionList = CSVLibrary.READABLE_TITLE_MARK_AS_DISTRIBUTION_LIST.equals(fieldName);
            if (isMarkAsDistributionList && "true".equalsIgnoreCase(currEntry)) {
                markAsDistributionlist = true;
            }

            final ContactField currField = getRelevantField(fieldName);
            if (currField == null) {
                final boolean worked = conSet._unknownfield(contactObj, fieldName, currEntry);
                if (worked) {
                    continue;
                }
                if (!isMarkAsDistributionList) {
                    atLeastOneFieldWithWrongName = true;
                    wrongFields.add(fieldName);
                }
            } else {
                if (currEntry.length() > 0) {
                    currField.doSwitch(conSet, contactObj, currEntry);
                    final Collection<OXException> warns = contactObj.getWarnings();
                    if (!warns.isEmpty()) {
                        warnings.add(ImportExportExceptionCodes.IGNORE_FIELD.create(warns.iterator().next(), fieldName, currEntry));
                    }
                }
                atLeastOneFieldInserted[0] = true;
            }
        }

        if (markAsDistributionlist) {
            contactObj.setMarkAsDistributionlist(true);
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

        final SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
        df2.setTimeZone(utc);

        final SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
        df3.setTimeZone(utc);

        dateSwitch.addDateFormat(df1);
        dateSwitch.addDateFormat(df2);
        dateSwitch.addDateFormat(df3);

        final ContactSwitcherForTimestamp timestampSwitch = new ContactSwitcherForTimestamp();
        final ContactSwitcherForBooleans boolSwitch = new ContactSwitcherForBooleans();
        ContactSwitcherForEmailAddresses emailSwitch = new ContactSwitcherForEmailAddresses();
        emailSwitch.setDelegate(boolSwitch);
        boolSwitch.setDelegate(timestampSwitch);
        timestampSwitch.setDelegate(dateSwitch);
        dateSwitch.setDelegate(new ContactSetter());
        return emailSwitch;
    }

    public static final class ImportIntention {
        public boolean exceedsLimit;
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

    /**
     * Parses comma separated values from the supplied input stream and selects the best matching contact mapper based on the input.
     *
     * @param input The input stream to parse
     * @param charset A fixed (client-defined) charset to use
     * @param maxLines The maximum number of lines to parse, or -1 to read all available lines
     * @return The line-wise parsed input, or <code>null</code> if no appropriate mapper was detected
     */
    private List<List<String>> parse(InputStream input, Charset charset, int maxLines) throws OXException, IOException {
        ThresholdFileHolder fileHolder = null;
        try {
            fileHolder = new ThresholdFileHolder(new FileHolder(input, -1, null, null));
            /*
             * parse the first line using a fixed (client-defined) charset
             */
            String firstLine = readLines(fileHolder.getStream(), charset, false, 1);
            CSVParser csvParser = getCSVParser(determineDelimiter(firstLine));
            List<List<String>> parsedFirstLine = csvParser.parse(firstLine);
            if (null == parsedFirstLine || 0 == parsedFirstLine.size() || null == parsedFirstLine.get(0)) {
                throw ImportExportExceptionCodes.NO_CONTENT.create();
            }
            /*
             * remember the best matching mapper & parse the whole file
             */
            currentMapper = chooseMapper(getMappers(), parsedFirstLine.get(0));
            if (null == currentMapper) {
                return null;
            }
            return csvParser.parse(readLines(fileHolder.getStream(), charset, false, maxLines));
        } finally {
            Streams.close(fileHolder);
        }
    }

    /**
     * Parses comma separated values from the supplied input stream and selects the best matching contact mapper based on the input.
     *
     * @param input The input stream to parse
     * @param maxLines The maximum number of lines to parse, or -1 to read all available lines
     * @return The line-wise parsed input, or <code>null</code> if no appropriate mapper was detected
     */
    protected List<List<String>> parse(InputStream input, int maxLines) throws OXException, IOException {
        ThresholdFileHolder fileHolder = null;
        try {
            fileHolder = new ThresholdFileHolder(new FileHolder(input, -1, null, null));
            /*
             * try to parse using the mapper's native charset as well as the auto-detected one
             */
            Charset detectedCharset = null;
            String detectedCharsetName = CharsetDetector.detectCharset(fileHolder.getStream(), null, true);
            if (null != detectedCharsetName) {
                detectedCharset = Charsets.forName(detectedCharsetName);
            }
            /*
             * determine the overall best matching mapper
             */
            OXException parseException = null;
            int maxMappedFields = 0;
            CSVParser bestParser = null;
            ContactFieldMapper bestMapper = null;
            Charset bestCharset = null;
            Map<Charset, List<ContactFieldMapper>> mappersByCharset = getMappersByCharset(getMappers(), detectedCharset);
            for (Entry<Charset, List<ContactFieldMapper>> entry : mappersByCharset.entrySet()) {
                /*
                 * parse the first line & choose an appropriate mapper
                 */
                String firstLine = readLines(fileHolder.getStream(), entry.getKey(), false, 1);
                CSVParser csvParser = getCSVParser(determineDelimiter(firstLine));
                List<List<String>> parsedFirstLine = null;
                try {
                    parsedFirstLine = csvParser.parse(firstLine);
                } catch (OXException e) {
                    parseException = e;
                }
                if (null == parsedFirstLine || 0 == parsedFirstLine.size() || null == parsedFirstLine.get(0)) {
                    continue;
                }
                for (ContactFieldMapper mapper : entry.getValue()) {
                    int mappedFields = getMappedFields(mapper, parsedFirstLine.get(0));
                    if (mappedFields > maxMappedFields) {
                        maxMappedFields = mappedFields;
                        bestMapper = mapper;
                        bestParser = csvParser;
                        bestCharset = entry.getKey();
                    }
                }
            }
            /*
             * remember the best matching mapper & parse the whole file
             */
            currentMapper = bestMapper;
            if (null == currentMapper) {
                if (null != parseException) {
                    throw parseException;
                }
                return null;
            }
            return bestParser.parse(readLines(fileHolder.getStream(), bestCharset, false, maxLines));
        } finally {
            Streams.close(fileHolder);
        }
    }

    public List<List<String>> checkFields(final InputStream input) throws OXException, IOException {
        currentMapper = null;
        int highestAmountOfMappedFields = 0;

        List<List<String>> retval = null;

        Map<String, String> csvByEncodings = new HashMap<String, String>();
        for (ContactFieldMapper mapper : getMappers()) {
            String encoding = mapper.getEncoding();
            String csvStr = csvByEncodings.get(encoding);
            if (null == csvStr) {
                csvStr = transformInputStreamToString(input, encoding, false);
                csvByEncodings.put(encoding, csvStr);
            }
            final CSVParser csvParser = getCSVParser();
            List<List<String>>  csv = csvParser.parse(csvStr);
            int mappedFields = 0;
            for (final String name : csv.get(0)) {
                if (mapper.getFieldByName(name) != null) {
                    mappedFields++;
                }
            }
            if (mappedFields > highestAmountOfMappedFields) {
                currentMapper = mapper;
                highestAmountOfMappedFields = mappedFields;
                retval = csv;
            }
            input.reset();
        }
        return getCurrentMapper() == null ? null : retval;
    }

    public boolean checkFields(final List<String> fields) {
        currentMapper = null;
        int highestAmountOfMappedFields = 0;

        for (ContactFieldMapper mapper : getMappers()) {
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
        return getCurrentMapper() != null;
    }

    protected boolean passesSanityTestForDisplayName(List<String> headers) {
        return Collections.any(
            headers,
            getCurrentMapper().getNameOfField(ContactField.DISPLAY_NAME),
            getCurrentMapper().getNameOfField(ContactField.SUR_NAME),
            getCurrentMapper().getNameOfField(ContactField.GIVEN_NAME),
            getCurrentMapper().getNameOfField(ContactField.EMAIL1),
            getCurrentMapper().getNameOfField(ContactField.EMAIL2),
            getCurrentMapper().getNameOfField(ContactField.EMAIL3),
            getCurrentMapper().getNameOfField(ContactField.COMPANY),
            getCurrentMapper().getNameOfField(ContactField.NICKNAME),
            getCurrentMapper().getNameOfField(ContactField.MIDDLE_NAME));
    }

    public void addFieldMapper(ContactFieldMapper mapper) {
        if (mappers == null) {
            mappers = new LinkedList<ContactFieldMapper>();
        }
        mappers.add(mapper);
    }


    private LinkedList<ContactFieldMapper> getMappers() {
        return mappers;
    }

    protected ContactField getRelevantField(final String name) {
        return getCurrentMapper().getFieldByName(name);
    }


    protected ContactFieldMapper getCurrentMapper() {
        return currentMapper;
    }

    protected static CSVParser getCSVParser() {
        final CSVParser result = new CSVParser();
        result.setTolerant(true);
        return result;
    }

    protected static CSVParser getCSVParser(char delimiter) {
        CSVParser csvParser = getCSVParser();
        csvParser.setCellDelimiter(delimiter);
        return csvParser;
    }

    protected boolean isResponsibleFor(Format f) {
        return Format.CSV == f || Format.OUTLOOK_CSV == f;
    }

    public String getEncoding() {
        return getCurrentMapper().getEncoding();
    }

    /**
     * Gets a map holding contact field mappers per charset they should operate on.
     *
     * @param mappers The mappers
     * @param detectedCharset A static detected charset to include in the mapping, or <code>null</code> if not used
     * @return A map holding the contact field mappers per charset they should operate on.
     */
    private static Map<Charset, List<ContactFieldMapper>> getMappersByCharset(List<ContactFieldMapper> mappers, Charset detectedCharset) {
        Map<Charset, List<ContactFieldMapper>> mappersByCharset = new HashMap<Charset, List<ContactFieldMapper>>();
        if (null != detectedCharset) {
            mappersByCharset.put(detectedCharset, new ArrayList<ContactFieldMapper>(mappers));
        }
        for (ContactFieldMapper mapper : mappers) {
            Charset key = Charsets.forName(mapper.getEncoding());
            if (null != key) {
                List<ContactFieldMapper> value = mappersByCharset.get(key);
                if (null == value) {
                    value = new ArrayList<ContactFieldMapper>();
                    mappersByCharset.put(key, value);
                }
                value.add(mapper);
            }
        }
        return mappersByCharset;
    }

    /**
     * Selects the contact field mapper that supports most of the fields present in the supplied header list from the .csv file.
     *
     * @param mappers The possible mappers
     * @param csvHeaders The header names from the .csv file
     * @return The best matching mapper, or <code>null</code> if not a single mapped field is supported by any mapper
     */
    private static ContactFieldMapper chooseMapper(List<ContactFieldMapper> mappers, List<String> csvHeaders) {
        ContactFieldMapper bestMapper = null;
        int maxMappedFields = 0;
        for (ContactFieldMapper mapper : mappers) {
            int mappedFields = getMappedFields(mapper, csvHeaders);
            if (maxMappedFields < mappedFields) {
                bestMapper = mapper;
                maxMappedFields = mappedFields;
            }
        }
        return bestMapper;
    }

    /**
     * Gets the number of mapped fields the supplied mapper supports from the supplied header list from the .csv file.
     *
     * @param mapper The mapper
     * @param csvHeaders The header names from the .csv file
     * @return The number of fields supported by the mapper
     */
    private static int getMappedFields(ContactFieldMapper mapper, List<String> csvHeaders) {
        int mappedFields = 0;
        for (String header : csvHeaders) {
            if (null != mapper.getFieldByName(header)) {
                mappedFields++;
            }
        }
        return mappedFields;
    }

    /**
     * Checks which delimiter is most likely used to separate the fields in the supplied csv string.
     *
     * @param csv The csv string to determine the delimiter for; only the first line is taken into account
     * @return The delimiter character, default to <code>,</code>
     */
    private static char determineDelimiter(String csv) {
        int firstNewlineIndex = csv.indexOf('\n');
        String probe = -1 == firstNewlineIndex ? csv : csv.substring(0, firstNewlineIndex);
        char delimiter = ',';
        int maxOccurrences = 0;
        for (char c : new char[] { ',', ';', '\t' }) {
            int occurrences = countOccurrences(probe, c);
            if (occurrences > maxOccurrences) {
                maxOccurrences = occurrences;
                delimiter = c;
            }
        }
        return delimiter;
    }

    /**
     * Counts the number of occurrences of a specific character in the supplied input string.
     *
     * @param input The string to count the occurrences in
     * @param c The character to count
     * @return The number of occorrences
     */
    private static int countOccurrences(String input, char c) {
        int count = 0;
        if (null != input) {
            for (int i = 0; i < input.length(); i++) {
                if (c == input.charAt(i)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Extracts a client-defined charset if one is set in the supplied optional parameters.
     *
     * @param optionalParams The optional parameters from the request
     * @return The charset, or <code>null</code> if none is defined
     */
    private static Charset getCharsetFromParameters(Map<String, String[]> optionalParams) throws OXException {
        if (null != optionalParams && 0 < optionalParams.size()) {
            String[] value = optionalParams.get("charset");
            if (null != value && 1 == value.length) {
                String charsetName = value[0];
                if (false == Strings.isEmpty(charsetName) && false == "auto".equalsIgnoreCase(charsetName)) {
                    try {
                        return Charsets.forName(value[0]);
                    } catch (IllegalCharsetNameException e) {
                        throw ImportExportExceptionCodes.UNSUPPORTED_CHARACTER_ENCODING.create(charsetName);
                    } catch (UnsupportedCharsetException e) {
                        throw ImportExportExceptionCodes.UNSUPPORTED_CHARACTER_ENCODING.create(charsetName);
                    }
                }
            }
        }
        return null;
    }

}
