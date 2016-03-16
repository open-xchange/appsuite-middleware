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

package com.openexchange.groupware.importexport;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.contact.storage.rdb.internal.RdbContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.importers.Importer;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;

/**
 * Basis for folder tests: Creates a folder and deletes it after testing.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class AbstractContactTest {

    public static class TestSession extends SessionObject {

        /**
         * This class is needed to fake permissions for different modules.
         * @param sessionid
         */
        public TestSession(final String sessionid) {
            super(sessionid);
        }

        public SessionObject delegateSessionObject;
        public UserConfiguration delegateUserConfiguration;
        @Override
        public boolean equals(final Object obj) {
            return delegateSessionObject.equals(obj);
        }
        @Override
        public int getContextId() {
            return delegateSessionObject.getContextId();
        }
        @Override
        public Date getCreationtime() {
            return delegateSessionObject.getCreationtime();
        }
        @Override
        public String getHost() {
            return delegateSessionObject.getHost();
        }
        @Override
        public String getLanguage() {
            return delegateSessionObject.getLanguage();
        }
        @Override
        public long getLifetime() {
            return delegateSessionObject.getLifetime();
        }
        @Override
        public String getLocalIp() {
            return delegateSessionObject.getLocalIp();
        }
        @Override
        public String getLoginName() {
            return delegateSessionObject.getLoginName();
        }
        @Override
        public String getPassword() {
            return delegateSessionObject.getPassword();
        }
        @Override
        public String getRandomToken() {
            return delegateSessionObject.getRandomToken();
        }
        @Override
        public String getSecret() {
            return delegateSessionObject.getSecret();
        }
        @Override
        public String getSessionID() {
            return delegateSessionObject.getSessionID();
        }
        @Override
        public Date getTimestamp() {
            return delegateSessionObject.getTimestamp();
        }
        public UserConfiguration getUserConfiguration() {
            return delegateUserConfiguration;
        }
        @Override
        public String getUserlogin() {
            return delegateSessionObject.getUserlogin();
        }
        @Override
        public String getUsername() {
            return delegateSessionObject.getUsername();
        }
        @Override
        public int hashCode() {
            return delegateSessionObject.hashCode();
        }
        @Override
        public void setContextId(final int id) {
            delegateSessionObject.setContextId(id);
        }
        @Override
        public void setCreationtime(final Date creationtime) {
            delegateSessionObject.setCreationtime(creationtime);
        }
        @Override
        public void setHost(final String host) {
            delegateSessionObject.setHost(host);
        }
        @Override
        public void setLanguage(final String language) {
            delegateSessionObject.setLanguage(language);
        }
        @Override
        public void setLifetime(final long lifetime) {
            delegateSessionObject.setLifetime(lifetime);
        }
        @Override
        public void setLocalIp(final String localip) {
            delegateSessionObject.setLocalIp(localip);
        }
        @Override
        public void setLoginName(final String loginName) {
            delegateSessionObject.setLoginName(loginName);
        }
        @Override
        public void setPassword(final String password) {
            delegateSessionObject.setPassword(password);
        }
        @Override
        public void setRandomToken(final String randomToken) {
            delegateSessionObject.setRandomToken(randomToken);
        }
        @Override
        public void setSecret(final String secret) {
            delegateSessionObject.setSecret(secret);
        }
        @Override
        public void setTimestamp(final Date timestamp) {
            delegateSessionObject.setTimestamp(timestamp);
        }
        @Override
        public void setUserlogin(final String userlogin) {
            delegateSessionObject.setUserlogin(userlogin);
        }
        @Override
        public void setUsername(final String username) {
            delegateSessionObject.setUsername(username);
        }
        @Override
        public String toString() {
            return delegateSessionObject.toString();
        }
    }

    protected static final int[] POSSIBLE_FIELDS = {
            DataObject.OBJECT_ID,
            DataObject.CREATED_BY,
            DataObject.CREATION_DATE,
            DataObject.LAST_MODIFIED,
            DataObject.MODIFIED_BY,
            FolderChildObject.FOLDER_ID,
    //        CommonObject.PRIVATE_FLAG,
    //        CommonObject.CATEGORIES,
            Contact.GIVEN_NAME,
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
    //        ContactObject.DISTRIBUTIONLIST,
            Contact.EMAIL1,
            Contact.EMAIL2,
            Contact.EMAIL3,
            Contact.EMPLOYEE_TYPE,
            Contact.FAX_BUSINESS,
            Contact.FAX_HOME,
            Contact.FAX_OTHER,
    //        ContactObject.FILE_AS,
            Contact.FOLDER_ID,
            Contact.GIVEN_NAME,
    //        ContactObject.IMAGE1,
    //        ContactObject.IMAGE1_CONTENT_TYPE,
            Contact.INFO,
            Contact.INSTANT_MESSENGER1,
            Contact.INSTANT_MESSENGER2,
    //        ContactObject.LINKS,
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
    //        ContactObject.PRIVATE_FLAG,
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
            Contact.DEFAULT_ADDRESS};

    public static ServerSession sessObj;
    public static int userId;
    public static int contextId;
    protected static Context ctx;
    public static int folderId;
    public String DISPLAY_NAME1 = ContactTestData.DISPLAY_NAME1;
    public String NAME1 = ContactTestData.NAME1;
    public String EMAIL1 = ContactTestData.EMAIL1;
    public String DISPLAY_NAME2 = ContactTestData.DISPLAY_NAME2;
    public String NAME2 = ContactTestData.NAME2;
    public String EMAIL2 = ContactTestData.EMAIL2;
    public static Importer imp;
    public Format defaultFormat;

    protected final RdbContactStorage contactStorage;

    public static int createTestFolder(final int type, final ServerSession sessObj,final Context ctx, final String folderTitle) throws OXException, OXException {
        final User user = UserStorage.getInstance().getUser(sessObj.getUserId(), ctx);
        final FolderObject fo = new FolderObject();
        fo.setFolderName(folderTitle);
        fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        fo.setModule(type);
        fo.setType(FolderObject.PRIVATE);
        final OCLPermission ocl = new OCLPermission();
        ocl.setEntity(user.getId());
        ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        ocl.setGroupPermission(false);
        ocl.setFolderAdmin(true);
        fo.setPermissionsAsArray(new OCLPermission[] { ocl });
        final OXFolderManager oxfa = OXFolderManager.getInstance(sessObj);
        int tempFolderId = -1;
        //deleting old folder if existing
        if(fo.exists(sessObj.getContext())){
            deleteTestFolder(fo.getObjectID());
        }
        //creating new folder
        tempFolderId = oxfa.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
        return tempFolderId;
    }

    public static void deleteTestFolder(final int fuid) throws OXException {
        if(fuid < 0){
            return;
        }
        final OXFolderManager oxfa = OXFolderManager.getInstance(sessObj, new CalendarSql(sessObj));
        final FolderObject fo = new FolderObject(fuid);
        if(fo.exists(sessObj.getContext())){
            oxfa.deleteFolder(fo, true, System.currentTimeMillis());
        }
    }

    @BeforeClass
    public static void initialize() throws Exception {
        Init.startServer();
        final UserStorage uStorage = UserStorage.getInstance();
        ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId(AjaxInit.getAJAXProperty("contextName")));
        String loginname = AjaxInit.getAJAXProperty("login");
        String namePart = loginname.split("@")[0];
        userId = uStorage.getUserId(namePart, ctx);
        sessObj = ServerSessionFactory.createServerSession(userId, ctx, "csv-tests");
    }

    @After
    public void cleanUpAfterTest() throws OXException {
        deleteTestFolder(folderId);
    }

    @AfterClass
    public static void debrief() throws Exception {
        Init.stopServer();
    }

    public AbstractContactTest() {
        super();
        this.contactStorage = new RdbContactStorage();
    }

    protected List<ImportResult> importStuff(final String csv) throws OXException, UnsupportedEncodingException{
        return importStuff(csv, "UTF-8");
    }

    protected List<ImportResult> importStuff(final String csv, final String encoding) throws OXException, UnsupportedEncodingException{
        final InputStream is = new ByteArrayInputStream( csv.getBytes(encoding) );
        return imp.importData(sessObj, defaultFormat, is, _folders(), null);
    }

    protected boolean existsEntry(final int entryNumber) throws OXException {
        return null != contactStorage.get(sessObj, String.valueOf(folderId), String.valueOf(entryNumber),
            new ContactField[] { ContactField.OBJECT_ID });
    }

    protected Contact getEntry(final int entryNumber) throws OXException, OXException {
        return contactStorage.get(sessObj, String.valueOf(folderId), String.valueOf(entryNumber), ContactField.values());
    }

    /**
     * Gets the number of contacts found in the supplied folder ID, assuming that the user is allowed to read all objects in the folder.
     *
     * @param folderID The ID of the parent folder
     * @return The number of contacts
     * @throws OXException
     */
    protected int getNumberOfContacts(int folderID) throws OXException {
        return contactStorage.count(sessObj, String.valueOf(folderID), true);
    }

    protected List<String> _folders(){
        return Arrays.asList( Integer.toString(folderId) );
    }

    /**
     * This method perform the import of a file with one one result.
     * Kept for backward compatibility, calls <code>performMultipleEntryImport</code>
     *
     * @param file Content of file as string
     * @param format Format of the file
     * @param folderObjectType Type of this folder, usually taken from FolderObject.
     * @param foldername Name of the folder to be used for this tests
     * @param errorExpected Is an error expected?
     * @return
     */
    protected ImportResult performOneEntryCheck(final String file, final Format format, final int folderObjectType, final String foldername,final Context ctx, final boolean errorExpected) throws UnsupportedEncodingException, OXException, OXException {
        return performMultipleEntryImport(file, format, folderObjectType, foldername, ctx, B(errorExpected)).get(0);
    }

    /**
     * This method performs an import of several entries
     *
     * @param file The content of a file as string
     * @param format Format of the file given
     * @param folderObjectType Type of this folder, usually taken from FolderObject.
     * @param foldername Name of the folder to be used for this tests
     * @param expectedErrors Are errors expected? Example: If you expect two ImportResults, which both report failure, write <code>true, true</code>.
     * @return
     */
    protected List<ImportResult> performMultipleEntryImport(final String file, final Format format, final int folderObjectType, final String foldername, final Context ctx, final Boolean... expectedErrors) throws UnsupportedEncodingException, OXException, OXException {
        folderId = createTestFolder(folderObjectType, sessObj,ctx, foldername);

        assertTrue("Can import?" ,  imp.canImport(sessObj, format, _folders(), null));

        final List<ImportResult> results = imp.importData(sessObj, format, new ByteArrayInputStream(file.getBytes(com.openexchange.java.Charsets.UTF_8)), _folders(), null);
        assertEquals("Correct number of results?", Integer.valueOf(expectedErrors.length), Integer.valueOf(results.size())); //ugly, but necessary to bridge JUnit 3 and 4

        for(int i = 0; i < expectedErrors.length; i++){
            assertEquals("Entry " +i+ " is as expected? "+results.get(i).getException() , expectedErrors[i], B(results.get(i).hasError()));
        }
        return results;
    }

    /**
     * Loads a user that is different from the usual user for testing
     *
     * @return the user information of user_participant1 as defined in ajax.properties
     * @throws Exception
     */
    public static User getUserParticipant() throws Exception{
        Init.startServer();
        final UserStorage uStorage = UserStorage.getInstance();
        final Context ctx = ContextStorage.getInstance().getContext(
            ContextStorage.getInstance().getContextId(AjaxInit.getAJAXProperty("contextName")));
        final int uid = uStorage.getUserId(AjaxInit.getAJAXProperty("user_participant1"), ctx);
        return uStorage.getUser(uid, ctx);
    }
}
