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

package com.openexchange.test;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebConversation;
import com.openexchange.admin.ContextTest;
import com.openexchange.admin.UserTest;
import com.openexchange.admin.container.Context;
import com.openexchange.admin.container.User;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.FolderTest;
import com.openexchange.ajax.InfostoreClient;

import com.openexchange.ajax.LoginTest;
import com.openexchange.ajax.links.LinkTools;
import com.openexchange.ajax.task.Create;
import com.openexchange.ajax.task.TaskTools;
import com.openexchange.database.Database;
import com.openexchange.database.DatabaseInit;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkObject;

/**
 * This test case creates a context, some user, some groups and a lot of
 * groupware data. Then everything will be deleted and afterwards the database
 * will be searched for lost entries.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class BigDeleteTest extends TestCase {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(BigDeleteTest.class);

    private int contextId = -1;
    
    private WebConversation conv1;
    private User user1;
    private String session1;
    
    private WebConversation conv2;
    private User user2;
    private String session2;
    
    /**
     * Default constructor.
     * @param name Name of the test case.
     */
    public BigDeleteTest(final String name) {
        super(name);
    }

    public void setUp(){
    	System.setProperty("openexchange.propfile", Init.getTestProperty("openexchange.propfile"));
    }

    /**
     * Tests the delete of contexts.
     * @throws Throwable if an error occurs.
     */
    public void testDelete() throws Throwable {
        final Context ctx = ContextTest.getTestContextObject();
        ContextTest.addContextWithoutDamnStuff(ctx, getRMIHost());
        boolean delete = true;
        try {
            contextId = (int) ctx.getContextId();
            user1 = UserTest.getTestUserObject("user1", "user1", contextId);
            user1.giveAllAccessRights();
            user1.setId(UserTest.addUser(user1, getRMIHost()));
            
            user2 = UserTest.getTestUserObject("user2", "user2", contextId);
            user2.giveAllAccessRights();
            user2.setId(UserTest.addUser(user2, getRMIHost()));
            
            
            // For later database check.
            DatabaseInit.init();
            final int poolId = Database.resolvePool(contextId, false);
            final String catalog = Database.getSchema(contextId);
            final Connection con = Database.get(contextId, false);

            createStuff();

            delete = false;
            ContextTest.deleteContext(ctx, getRMIHost());

            Thread.sleep(2 * 180000);
            final String tables = checkLostEntries(con, catalog, contextId);
            Database.back(poolId, con);
            assertEquals("Found lost entries in tables: ", "", tables);
        } catch (Exception x) {
        	x.printStackTrace();
        } finally {
            if (delete) {
                ContextTest.deleteContext(ctx, getRMIHost());
            }
        }
    }

    private static final String SELECT = "SELECT * FROM <tablename> WHERE "
        + "cid=?";

    private static final String DELETE = "DELETE FROM <tablename> WHERE cid=?";
    
    private static final String getSelect(final String tableName) {
        return SELECT.replace("<tablename>", tableName);
    }

    private static final String getDelete(final String tableName) {
        return DELETE.replace("<tablename>", tableName);
    }
    
    public static String checkLostEntries(final Connection con,
        final String catalog, final int contextId) throws SQLException {
        final List<String> tables = getTables(con, catalog, contextId);
        final StringBuilder output = new StringBuilder();
        for (String table : tables) {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement(getSelect(table));
                stmt.setInt(1, contextId);
                result = stmt.executeQuery();
                if (result.next()) {
                    output.append("Table: ");
                    output.append(table);
                    output.append('\n');
                    final ResultSetMetaData meta = result.getMetaData();
                    final int columnCount = meta.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        output.append(meta.getColumnName(i));
                        output.append(',');
                    }
                    output.setCharAt(output.length() - 1, '\n');
                    do {
                        for (int i = 1; i <= columnCount; i++) {
                            output.append(result.getString(i));
                            output.append(',');
                        }
                        output.setCharAt(output.length() - 1, '\n');
                    } while (result.next());
                    PreparedStatement stmt2 = null;
                    try {
                        stmt2 = con.prepareStatement(getDelete(table));
                        stmt2.setInt(1, contextId);
                        stmt2.execute();
                    } catch (SQLException e) {
                        LOG.error(e.getMessage(), e);
                    } finally {
                        closeSQLStuff(null, stmt2);
                    }
                }
            } finally {
                closeSQLStuff(result, stmt);
            }
        }
        return output.toString();
    }

    public static List<String> getTables(final Connection con,
        final String catalog, final int contextId) throws SQLException {
        final List<String> retval = new ArrayList<String>();
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet result = null;
        try {
            result = metaData.getTables(catalog, null, null,
                new String[] { "TABLE" });
            while (result.next()) {
                final String table = result.getString("TABLE_NAME");
                ResultSet result2 = null;
                try {
                    result2 = metaData.getColumns(catalog, null, table, "cid");
                    if (result2.next()) {
                        retval.add(table);
                    }
                } finally {
                    closeSQLStuff(result2, null);
                }
            }
        } finally {
            closeSQLStuff(result, null);
        }
        return retval;
    }
    
    private static String getRMIHost(){
        return "localhost";
    }

    private static String getHostName() {
        return "localhost";
    }

    private WebConversation getConv1() {
        if (null == conv1) {
            conv1 = new WebConversation();
        }
        return conv1;
    }
    
    private WebConversation getConv2() {
        if (null == conv2) {
            conv2 = new WebConversation();
        }
        return conv2;
    }

    private String getUser1Login() {
        return user1.getUsername() + '@' + contextId;
    }
    
    private String getUser2Login() {
        return user2.getUsername() + '@' + contextId;
    }
    
    private String getSession1() throws IOException, SAXException,
        JSONException {
        if (null == session1) {
            session1 = LoginTest.getSessionId(getConv1(), getHostName(),
                getUser1Login(), user1.getPassword());
        }
        return session1;
    }
    
    private String getSession2() throws IOException, SAXException,
    JSONException {
	    if (null == session2) {
	        session2 = LoginTest.getSessionId(getConv2(), getHostName(),
	            getUser2Login(), user2.getPassword());
	    }
	    return session2;
	}


    public void createStuff() throws Throwable {
        createTaskStuff();
        createLinkStuff();
        createContactStuff();
        createInfoStuff();

    }

    public void createTaskStuff() throws Throwable {
        Create.createPrivateTask(getConv1(), getHostName(), getSession1());
    }

    public void createLinkStuff() throws Throwable {
        final int taskFolderId = TaskTools.getPrivateTaskFolder(getConv1(),
            getHostName(), getSession1());
        final int task1 = Create.createPrivateTask(getConv1(), getHostName(),
            getSession1(), taskFolderId);
        final int task2 = Create.createPrivateTask(getConv1(), getHostName(),
            getSession1(), taskFolderId);
        final LinkObject link = new LinkObject(task1, Types.TASK, taskFolderId,
            task2, Types.TASK, taskFolderId, 0);
        LinkTools.extractInsertId(LinkTools.insertLink(getConv1(),
            getHostName(), getSession1(), link));
    }
    
    public void createContactStuff() throws Throwable {
    	
    	byte[] image = { -119, 80, 78, 71, 13, 10, 26, 10, 0,
    			0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 1, 3, 0, 0, 0,
    			37, -37, 86, -54, 0, 0, 0, 6, 80, 76, 84, 69, -1, -1, -1, -1, -1,
    			-1, 85, 124, -11, 108, 0, 0, 0, 1, 116, 82, 78, 83, 0, 64, -26,
    			-40, 102, 0, 0, 0, 1, 98, 75, 71, 68, 0, -120, 5, 29, 72, 0, 0, 0,
    			9, 112, 72, 89, 115, 0, 0, 11, 18, 0, 0, 11, 18, 1, -46, -35, 126,
    			-4, 0, 0, 0, 10, 73, 68, 65, 84, 120, -38, 99, 96, 0, 0, 0, 2, 0,
    			1, -27, 39, -34, -4, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
    	
		final FolderObject folderObj = FolderTest.getStandardContactFolder(getConv1(), getHostName(), getSession1());
		int contactFolderId = folderObj.getObjectID();
		
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
    	
		ContactObject contactObj = new ContactObject();
		contactObj.setPrivateFlag(true);
		contactObj.setCategories("categories");
		contactObj.setGivenName("given name");
		contactObj.setSurName("surname Contact 1");
		contactObj.setAnniversary(new Date(c.getTimeInMillis()));
		contactObj.setAssistantName("assistant name");
		contactObj.setBirthday(new Date(c.getTimeInMillis()));
		contactObj.setBranches("branches");
		contactObj.setBusinessCategory("business categorie");
		contactObj.setCellularTelephone1("cellular telephone1");
		contactObj.setCellularTelephone2("cellular telephone2");
		contactObj.setCityBusiness("city business");
		contactObj.setCityHome("city home");
		contactObj.setCityOther("city other");
		contactObj.setCommercialRegister("commercial register");
		contactObj.setCompany("company");
		contactObj.setCountryBusiness("country business");
		contactObj.setCountryHome("country home");
		contactObj.setCountryOther("country other");
		contactObj.setDepartment("department");
		contactObj.setDisplayName("display name");
		contactObj.setEmail1("email1@test.de");
		contactObj.setEmail2("email2@test.de");
		contactObj.setEmail3("email3@test.de");
		contactObj.setEmployeeType("employee type");
		contactObj.setFaxBusiness("fax business");
		contactObj.setFaxHome("fax home");
		contactObj.setFaxOther("fax other");
		contactObj.setInfo("info");
		contactObj.setInstantMessenger1("instant messenger1");
		contactObj.setInstantMessenger2("instant messenger2");
		contactObj.setImage1(image);
		contactObj.setImageContentType("image/png");
		contactObj.setManagerName("manager name");
		contactObj.setMaritalStatus("marital status");
		contactObj.setMiddleName("middle name");
		contactObj.setNickname("nickname");
		contactObj.setNote("note");
		contactObj.setNumberOfChildren("number of children");
		contactObj.setNumberOfEmployee("number of employee");
		contactObj.setPosition("position");
		contactObj.setPostalCodeBusiness("postal code business");
		contactObj.setPostalCodeHome("postal code home");
		contactObj.setPostalCodeOther("postal code other");
		contactObj.setProfession("profession");
		contactObj.setRoomNumber("room number");
		contactObj.setSalesVolume("sales volume");
		contactObj.setSpouseName("spouse name");
		contactObj.setStateBusiness("state business");
		contactObj.setStateHome("state home");
		contactObj.setStateOther("state other");
		contactObj.setStreetBusiness("street business");
		contactObj.setStreetHome("street home");
		contactObj.setStreetOther("street other");
		contactObj.setSuffix("suffix");
		contactObj.setTaxID("tax id");
		contactObj.setTelephoneAssistant("telephone assistant");
		contactObj.setTelephoneBusiness1("telephone business1");
		contactObj.setTelephoneBusiness2("telephone business2");
		contactObj.setTelephoneCallback("telephone callback");
		contactObj.setTelephoneCar("telephone car");
		contactObj.setTelephoneCompany("telehpone company");
		contactObj.setTelephoneHome1("telephone home1");
		contactObj.setTelephoneHome2("telephone home2");
		contactObj.setTelephoneIP("telehpone ip");
		contactObj.setTelephoneISDN("telehpone isdn");
		contactObj.setTelephoneOther("telephone other");
		contactObj.setTelephonePager("telephone pager");
		contactObj.setTelephonePrimary("telephone primary");
		contactObj.setTelephoneRadio("telephone radio");
		contactObj.setTelephoneTelex("telephone telex");
		contactObj.setTelephoneTTYTTD("telephone ttytdd");
		contactObj.setTitle("title");
		contactObj.setURL("url");
		contactObj.setUserField01("userfield01");
		contactObj.setUserField02("userfield02");
		contactObj.setUserField03("userfield03");
		contactObj.setUserField04("userfield04");
		contactObj.setUserField05("userfield05");
		contactObj.setUserField06("userfield06");
		contactObj.setUserField07("userfield07");
		contactObj.setUserField08("userfield08");
		contactObj.setUserField09("userfield09");
		contactObj.setUserField10("userfield10");
		contactObj.setUserField11("userfield11");
		contactObj.setUserField12("userfield12");
		contactObj.setUserField13("userfield13");
		contactObj.setUserField14("userfield14");
		contactObj.setUserField15("userfield15");
		contactObj.setUserField16("userfield16");
		contactObj.setUserField17("userfield17");
		contactObj.setUserField18("userfield18");
		contactObj.setUserField19("userfield19");
		contactObj.setUserField20("userfield20");
		contactObj.setDefaultAddress(1);
		contactObj.setParentFolderID(contactFolderId);

		int con1 = ContactTest.insertContact(getConv1(), contactObj, getHostName(), getSession1());
		String dname1 = contactObj.getDisplayName();
		
		contactObj.setSurName("surename Contact 2");
		contactObj.setEmail1("2email1@test.de");
		contactObj.setObjectID(0);
		
		DistributionListEntryObject[] entry = new DistributionListEntryObject[3];
		entry[0] = new DistributionListEntryObject("displayname a", "a@a.de", DistributionListEntryObject.INDEPENDENT);
		entry[1] = new DistributionListEntryObject("displayname b", "b@b.de", DistributionListEntryObject.INDEPENDENT);
		entry[2] = new DistributionListEntryObject(contactObj.getSurName()+", "+contactObj.getGivenName(), contactObj.getEmail1(), DistributionListEntryObject.EMAILFIELD1);
		entry[2].setEntryID(con1);
		contactObj.setDistributionList(entry);
		
		int con2 = ContactTest.insertContact(getConv1(), contactObj, getHostName(), getSession1());
		String dname2 = contactObj.getDisplayName();
		
		LinkObject lo = new LinkObject();
		lo.setFirstFolder(contactFolderId);
		lo.setFirstId(con1);
		lo.setFirstType(com.openexchange.groupware.Types.CONTACT);
		lo.setSecondFolder(contactFolderId);
		lo.setSecondId(con2);
		lo.setSecondType(com.openexchange.groupware.Types.CONTACT);

		
    }
    
    public void createInfoStuff() throws Throwable {
    	FolderObject myInfostore = FolderTest.getMyInfostoreFolder(getConv1(), getHostName(), getSession1(), (int)user1.getId());
    	// We'll examine 2 cases
    	// 1) The standard case where user1 is admin. This should disappear as soon as the user is deleted as part of the context delete
    	// 1) is true for myInfostore
    	// 2) Where another user is involved. These infoitems are transferred to mailadmn and only deleted once mailadmin is deleted
    	int fuid = FolderTest.insertFolder(getConv1(), getHostName(), getSession1(), (int) user1.getId(), false,
				FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, "case2inbigdelete", "infostore", FolderObject.PUBLIC, -1,
				true);
    
    	Map<String,String> newItem = new HashMap<String,String>();
    	newItem.put("folder_id", String.valueOf(myInfostore.getObjectID()));
    	newItem.put("title", "case1item");
    	newItem.put("description", "Case1: user1 is admin. Should disappear on user delete");
    	File upload = new File(Init.getTestProperty("ajaxPropertiesFile"));
		
    	
    	int id = InfostoreClient.createNew(getConv1(),getHostName(),getSession1(),newItem,upload, "text/plain");
    	InfostoreClient.lock(getConv1(), getHostName(), getSession1(),id);
    	
    	newItem.put("folder_id", String.valueOf(fuid));
    	newItem.put("title", "case2item");
    	newItem.put("description", "Case2: Another user is involved. Should disappear on mailadmin delete");
    	id = InfostoreClient.createNew(getConv1(),getHostName(),getSession1(),newItem,upload, "text/plain");
    	
    	
    	InfostoreClient.lock(getConv1(), getHostName(), getSession1(),id);
    	
    	FolderTest.updateFolder(getConv1(), getHostName(), getSession1(), String.valueOf(user2.getId()), String.valueOf(user1.getId()), fuid, System.currentTimeMillis(), false);
    	
    }
}
