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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.importexport.importers;

import static com.openexchange.groupware.importexport.csv.CSVLibrary.getFolderObject;
import static com.openexchange.groupware.importexport.csv.CSVLibrary.transformInputStreamToString;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForTimestamp;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.AbstractImporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Importer for OX own CSV file format - this format is able to represent a
 * contact with all fields that appear in the OX.
 *
 *
 * @see com.openexchange.groupware.importexport.importers.OutlookCSVContactImporter - imports files prduced by Outlook
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
@OXExceptionSource(
    classId=ImportExportExceptionClasses.CSVCONTACTIMPORTER,
    component=EnumComponent.IMPORT_EXPORT)
@OXThrowsMultiple(
    category={
        Category.USER_INPUT,
        Category.CODE_ERROR,
        Category.CODE_ERROR,
        Category.WARNING,
        Category.USER_INPUT,
        Category.USER_INPUT,
        Category.PERMISSION},
    desc={"", "", "", "", "", "", ""},
    exceptionId={0,1,2,3,4,5,6},
    msg={
        "Can only import into one folder at a time.",
        "Cannot import this kind of data. Use method canImport() first.",
        "Cannot read given InputStream.",
        "Could not find the following fields %s",
        "Could not translate a single column title. Is this a valid CSV file?",
        "Could not translate a single field of information, did not insert entry %s.",
        "Module Contacts not enabled for user, cannot import contacts"
        })
public class CSVContactImporter extends AbstractImporter {

    private static final Log LOG = LogFactory.getLog(CSVContactImporter.class);

    private static ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(CSVContactImporter.class);

    public boolean canImport(final ServerSession sessObj, final Format format, final List<String> folders,
            final Map<String, String[]> optionalParams) throws ImportExportException {

        if(! format.equals(getResponsibleFor()) ){
            return false;
        }

        if(!UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()).hasContact() ){
            throw EXCEPTIONS.create(6, new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "Calendar") );
        }

        String folder;
        if(folders.size() != 1){
            throw EXCEPTIONS.create(0);
        }
        folder = folders.get(0);

        FolderObject fo = null;
        try {
            fo = getFolderObject(sessObj, folder);
        } catch (final ImportExportException e) {
            return false;
        }
        if(fo == null){
            if (LOG.isInfoEnabled()) {
                LOG.info("Folder does not exist: " + folder);
            }
            return false;
        }
        //check format of folder
        if ( fo.getModule() != FolderObject.CONTACT ){
            return false;
        }
        //check read access to folder
        EffectivePermission perm;
        try {
            perm = fo.getEffectiveUserPermission(sessObj.getUserId(), UserConfigurationStorage
                    .getInstance().getUserConfigurationSafe(sessObj.getUserId(), sessObj.getContext()));
        } catch (final DBPoolingException e) {
            return false;
        } catch (final SQLException e) {
            return false;
        }
        return perm.canCreateObjects();
    }


    protected Format getResponsibleFor() {
        return Format.CSV;
    }

    /**
     * The encoding this importers assumes is used for the file.
     *
     * @return UTF-8 since if you write your own format, you may actually specify something useful.
     */
    public String getEncoding(){
        return "UTF-8";
    }


    public List<ImportResult> importData(final ServerSession sessObj, final Format format,
            final InputStream is, final List<String> folders,
            final Map<String, String[]> optionalParams) throws ImportExportException {
        if(! canImport(sessObj, format, folders, optionalParams)){
            throw EXCEPTIONS.create(1);
        }
        final String folder = folders.get(0);
        final String csvStr = transformInputStreamToString(is, getEncoding());
        final CSVParser csvParser = getCSVParser();
        final List<List<String>> csv = csvParser.parse(csvStr);

        final Iterator<List<String>> iter = csv.iterator();
        //get header fields
        final List<String> fields = iter.next();
        if (!checkFields(fields)) {
            throw EXCEPTIONS.create(4);
        }

        //reading entries...
        final List<ImportResult> results = new LinkedList<ImportResult>();
        final ContactSwitcher conSet = getContactSwitcher();
        //final ContactSQLInterface contactsql = new RdbContactSQLInterface(sessObj, sessObj.getContext());
        int lineNumber = 1;
        while (iter.hasNext()) {
            //...and writing them
            results.add(writeEntry(fields, iter.next(), folder, conSet, lineNumber++, sessObj));
        }
        return results;
    }

    protected CSVParser getCSVParser() {
        return new CSVParser();
    }


    protected boolean checkFields(final List<String> fields) {
        for(final String fieldname : fields){
            if(getRelevantField(fieldname) != null){
                return true;
            }
        }
        return false;
    }


    /**
     *
     * @param fields Headers of the table; column title
     * @param entry A list of row cells.
     * @param folder The folder this is line meant to be written into
     * @param conSet The ContactSetter used for translating the given data
     * @param lineNumber Number of the entry ins the CSV file (used for precise error message)
     * @param session The session
     * @return a report containing either the object ID of the entry created OR an error message
     */
    protected ImportResult writeEntry(final List<String> fields, final List<String> entry, final String folder, final ContactSwitcher conSet, final int lineNumber, final ServerSession session){
        final ImportResult result = new ImportResult();
        final Contact contactObj = new Contact();
        result.setFolder(folder);
        try{
            final List<String> wrongFields = new LinkedList<String>();
            boolean atLeastOneFieldWithWrongName = false;
            boolean atLeastOneFieldInserted = false;
            for(int i = 0; i < fields.size(); i++){
                final ContactField currField = getRelevantField(fields.get(i));
                if(currField == null){
                    atLeastOneFieldWithWrongName = true;
                    wrongFields.add(fields.get(i));
                } else {
                    final String currEntry = entry.get(i);
                    if(! currEntry.equals("")){
                        currField.doSwitch(conSet, contactObj, currEntry);
                    }
                    atLeastOneFieldInserted = true;
                }
            }
            if(atLeastOneFieldWithWrongName){
                result.setException(EXCEPTIONS.create(3, wrongFields.toString()));
                addErrorInformation(result, lineNumber , fields);
            }
            contactObj.setParentFolderID(Integer.parseInt( folder.trim() ));
            if(atLeastOneFieldInserted){
                final ContactInterface contactInterface = ServerServiceRegistry.getInstance().getService(
                    ContactInterfaceDiscoveryService.class).newContactInterface(contactObj.getParentFolderID(), session);
                contactInterface.insertContactObject(contactObj);
                result.setObjectId( Integer.toString( contactObj.getObjectID() ) );
                result.setDate( contactObj.getLastModified() );
            } else {
                result.setException(EXCEPTIONS.create(5, Integer.valueOf(lineNumber)));
                result.setDate(new Date());
            }

        } catch (OXException e) {
            e =    handleDataTruncation(e);
            result.setException(e);
            addErrorInformation(result, lineNumber , fields);
        }
        return result;
    }

    /**
     * Adds error information to a given ImportResult
     * @param result ImportResult to be written into.
     * @param lineNumber Number of the buggy line in the CSV script.
     * @param entry CSV line that was buggy.
     */
    protected void addErrorInformation(final ImportResult result, final int lineNumber, final List<String> entry){
        result.setEntryNumber(lineNumber);
    }

    /**
     * Method used to find a ContactField based on the given field name
     * @param name Name of the field
     * @return a ContactField that was identified by the given name
     */
    protected ContactField getRelevantField(final String name){
        return ContactField.getByDisplayName(name);
    }

    protected ContactSwitcher getContactSwitcher(){
        final ContactSwitcherForSimpleDateFormat dateSwitch = new ContactSwitcherForSimpleDateFormat();
        dateSwitch.addDateFormat(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM));
        
        final TimeZone utc = TimeZone.getTimeZone("UTC");
        final SimpleDateFormat df1 = new SimpleDateFormat("dd.MM.yyyy");
        df1.setTimeZone(utc);
        
        final SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
        df2.setTimeZone(utc);
        
        final SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd");
        df3.setTimeZone(utc);
        
        dateSwitch.addDateFormat(df1);
        dateSwitch.addDateFormat(df2);
        dateSwitch.addDateFormat(df3);
        
        
        final ContactSwitcherForTimestamp timestampSwitch  = new ContactSwitcherForTimestamp();
        final ContactSwitcherForBooleans boolSwitch  = new ContactSwitcherForBooleans();
        boolSwitch.setDelegate(timestampSwitch);
        timestampSwitch.setDelegate(dateSwitch);
        dateSwitch.setDelegate(new ContactSetter());
        return boolSwitch;
    }

    @Override
    protected String getNameForFieldInTruncationError(final int id, final OXException unused) {
        final ContactField field = ContactField.getByValue(id);
        if(field == null){
            return String.valueOf( id );
        }
        return field.getReadableName();
    }
}
