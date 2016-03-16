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

package com.openexchange.user.copy.internal;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.TestServiceRegistry;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.connection.ConnectionHolder;
import com.openexchange.user.copy.internal.context.ContextMapping;
import com.openexchange.user.copy.internal.folder.FolderMapping;
import com.openexchange.user.copy.internal.user.UserMapping;


/**
 * {@link AbstractUserCopyTest}
 * 
 * WARNING: SECOND_CID and SECOND_UID must(!) point to a context and user that doesn't exist.
 * Otherwise you will destroy the users profile!
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractUserCopyTest extends TestCase {
    
    private static final String SELECT_ARBITRARY_FILESTORE =
        "SELECT " +
            "id " +
        "FROM " +
            "filestore " +
        "LIMIT 1";
    
    private static final String SELECT_FIDS =
        "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND created_from = ? AND (module = 8 OR type = 1)";
    
    private static final String SELECT_FOLDERS = 
        "SELECT "+ 
            "fuid, parent, fname, module, type, creating_date, " + 
            "changing_date, changed_from, permission_flag, " + 
            "subfolder_flag, default_flag " + 
        "FROM " + 
            "oxfolder_tree " + 
        "WHERE " + 
            "cid = ? AND created_from = ? AND (module = 8 OR type = 1)";
    
    private static final int FIRST_CID = 424242669;
    
    private static final int SECOND_CID = 999;
    
    private static final int SECOND_UID = 111;
    
    private static final int TARGET_FOLDER = 1337;
    
    private static boolean init = false;

    private ContextImpl firstCtx;
    
    private ContextImpl secondCtx;

    private int firstUserId;

    private Connection srcCon;

    private Connection dstCon;

    private DatabaseService dbService;
    
    protected UserService userService;
    
    
    public AbstractUserCopyTest(final String name) {
        super(name);
    }
    
    
    /*
     * Set up methods
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!init) {
            Init.startServer();
        }        

        firstCtx = new ContextImpl(FIRST_CID);
        secondCtx = new ContextImpl(SECOND_CID);
        secondCtx.setMailadmin(2);
        firstUserId = resolveUser(AjaxInit.getAJAXProperty("login"), firstCtx);
        dbService = TestServiceRegistry.getInstance().getService(DatabaseService.class);
        userService = TestServiceRegistry.getInstance().getService(UserService.class);
        
        srcCon = dbService.getWritable(FIRST_CID);
        dstCon = dbService.getWritable(FIRST_CID);
        final int filestoreId = getFilestoreId();
        firstCtx.setFilestoreId(filestoreId);
        secondCtx.setFilestoreId(filestoreId);
        
        // As the target context doesn't exist, we have to insert a sequence entry if there is none.
        createSequences(getSequenceTables());
    }
    
    private void createSequences(final String[] sequenceTables) throws Exception {
        if (sequenceTables != null) {
            for (final String table : sequenceTables) {
                final Statement stmt = dstCon.createStatement();
                final ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE cid = " + SECOND_CID);
                boolean entryExists = false;
                if (rs.next()) {
                    final int count = rs.getInt(1);
                    if (count > 0) {
                        entryExists = true;
                    }
                }
                rs.close();
                
                if (!entryExists) {
                    stmt.executeUpdate("INSERT INTO " + table + " (cid, id) VALUES (" + SECOND_CID + ", 1)");
                }
                
                stmt.close();
            }  
        }
    }
    
    private static int resolveUser(String user, final Context ctx) throws Exception {
        try {
            int pos = -1;
            user = (pos = user.indexOf('@')) > -1 ? user.substring(0, pos) : user;
            final UserStorage uStorage = UserStorage.getInstance();
            return uStorage.getUserId(user, ctx);
        } catch (final Throwable t) {
            t.printStackTrace();
            System.exit(1);
            return -1;
        }
    }
    
    private Map<String, ObjectMapping<?>> createMapping() {
        final Map<String, ObjectMapping<?>> mapping = new HashMap<String, ObjectMapping<?>>();
        final ConnectionHolder conHolder = new ConnectionHolder();
        conHolder.addMapping(FIRST_CID, srcCon, SECOND_CID, dstCon);
        final ContextMapping contextMapping = new ContextMapping();
        contextMapping.addMapping(FIRST_CID, firstCtx, SECOND_CID, secondCtx);
        final UserMapping userMapping = new UserMapping();
        userMapping.addMapping(firstUserId, new MockUser(firstUserId), SECOND_UID, new MockUser(SECOND_UID));
        mapping.put(Connection.class.getName(), conHolder);
        mapping.put(com.openexchange.groupware.ldap.User.class.getName(), userMapping);
        mapping.put(Context.class.getName(), contextMapping);
        mapping.put(Constants.CONTEXT_ID_KEY, new ObjectMapping<Integer>() {
            public Integer getSource(final int id) {
                return I(FIRST_CID);
            }
            public Integer getDestination(final Integer source) {
                return I(SECOND_CID);
            }
            public Set<Integer> getSourceKeys() {
                return null;
            }});
        mapping.put(Constants.USER_ID_KEY, new ObjectMapping<Integer>() {
            public Integer getSource(final int id) {
                return I(firstUserId);
            }
            public Integer getDestination(final Integer source) {
                return null;
            }
            public Set<Integer> getSourceKeys() {
                return null;
            }            
        });
        
        return mapping;
    }
    
    private FolderObject buildFolderFromResultSet(final ResultSet rs) throws SQLException {
        int i = 1;
        final FolderObject folder = new FolderObject(rs.getInt(i++));
        folder.setParentFolderID(rs.getInt(i++));
        folder.setFolderName(rs.getString(i++));
        folder.setModule(rs.getInt(i++));
        folder.setType(rs.getInt(i++));
        folder.setCreationDate(new Date(rs.getLong(i++)));
        folder.setLastModified(new Date(rs.getLong(i++)));
        folder.setModifiedBy(rs.getInt(i++));
        folder.setPermissionFlag(rs.getInt(i++));
        folder.setSubfolderFlag(rs.getBoolean(i++));
        folder.setDefaultFolder(rs.getBoolean(i++));

        return folder;        
    }
    
    private int getFilestoreId() throws SQLException, OXException {
        final Connection con = getDBService().getReadOnly();
        Statement stmt = null;
        ResultSet rs = null;
        try {  
            stmt = con.createStatement();
            rs = stmt.executeQuery(SELECT_ARBITRARY_FILESTORE);
            
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                fail("Did not find a proper filestore id.");
                return 0;
            }
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            getDBService().backReadOnly(con);
        }
    }   

    
    /*
     * Abstract methods
     */
    protected abstract String[] getSequenceTables();
    
    
    /*
     * Getter for inherited tests
     */
    protected int getSourceUserId() {
        return firstUserId;
    }
    
    protected int getDestinationUserId() {
        return SECOND_UID;
    }
    
    protected ContextImpl getSourceContext() {
        return firstCtx;
    }
    
    protected ContextImpl getDestinationContext() {
        return secondCtx;
    }
    
    protected Connection getSourceConnection() {
        return srcCon;
    }
    
    protected Connection getDestinationConnection() {
        return dstCon;
    }
    
    protected int getDestinationFolder() {
        return TARGET_FOLDER;
    }
    
    protected Map<String, ObjectMapping<?>> getBasicObjectMapping() {
        return createMapping();
    }
    
    protected Map<String, ObjectMapping<?>> getObjectMappingWithFolders() throws Exception {
        final Map<String, ObjectMapping<?>> mappingMap = getBasicObjectMapping();
        final FolderMapping folderMapping = new FolderMapping();
        final List<FolderObject> sourceFolders = loadSourceFoldersFromDB();
        for (final FolderObject source : sourceFolders) {
            final FolderObject destination = new FolderObject(TARGET_FOLDER);
            folderMapping.addMapping(source.getObjectID(), source, TARGET_FOLDER, destination);
        }
        mappingMap.put(FolderObject.class.getName(), folderMapping);
        
        return mappingMap;
    }
    
    protected <T> Map<T, T> checkAndGetMatchingObjects(final Collection<T> originCollection, final Collection<T> targetCollection) {
        assertEquals("Collections had different sizes.", originCollection.size(), targetCollection.size());
        
        final Map<T, T> map = new HashMap<T, T>();
        for (final T origin : originCollection) {
            boolean found = false;
            for (final T target : targetCollection) {
                if (origin.equals(target)) {
                    found = true;
                    map.put(origin, target);
                    break;
                }
            }
            
            if (!found) {
                fail("Did not find target object for origin " + origin);
            }
        }
        
        return map;
    }
    
    protected <T> Map<T, T> checkAndGetMatchingObjects(final Collection<T> originCollection, final Collection<T> targetCollection, final Comparator<T> comparator) {
        assertEquals("Collections had different sizes.", originCollection.size(), targetCollection.size());
        
        final Map<T, T> map = new HashMap<T, T>();
        for (final T origin : originCollection) {
            boolean found = false;
            for (final T target : targetCollection) {
                final int compare = comparator.compare(origin, target);
                if (compare == 0) {
                    found = true;
                    map.put(origin, target);
                    break;
                }
            }
            
            if (!found) {
                fail("Did not find target object for origin " + origin);
            }
        }
        
        return map;
    }
    
    public static boolean checkNullOrEquals(final Object obj1, final Object obj2) {
        if (obj1 == null) {
            if (obj2 != null) {
                return false;
            }
            
            return true;                
        } else {
            if (obj2 == null) {
                return false;
            }
            
            return obj1.equals(obj2);
        }
    }
    
    protected List<FolderObject> loadSourceFoldersFromDB() throws Exception {
        final List<FolderObject> folders = new ArrayList<FolderObject>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = srcCon.prepareStatement(SELECT_FOLDERS);
            stmt.setInt(1, firstCtx.getContextId());
            stmt.setInt(2, firstUserId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                final FolderObject folder = buildFolderFromResultSet(rs);
                folders.add(folder);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return folders;
    }
    
    protected List<Integer> loadFolderIdsFromDB(final Connection con, final int cid, final int uid) throws Exception {
        final List<Integer> folderIds = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        stmt = con.prepareStatement(SELECT_FIDS);
        stmt.setInt(1, cid);
        stmt.setInt(2, uid);
        rs = stmt.executeQuery();
        while (rs.next()) {
            final int id = rs.getInt(1);
            folderIds.add(id);
        }
        rs.close();
        stmt.close();        
        
        return folderIds;
    }
    
    protected void deleteAllFromTablesForCid(final int cid, final String cidField, final Connection con, final String... tables) throws SQLException {
        final Statement stmt = con.createStatement();
        for (final String table : tables) {
            disableForeignKeyChecks(con);
            stmt.executeUpdate("DELETE FROM " + table + " WHERE " + cidField + " = " + cid);
            enableForeignKeyChecks(con);
        }
        stmt.close();
    }
    
    protected DatabaseService getDBService() {
        return dbService;
    }
    
    protected void disableForeignKeyChecks(final Connection con) throws SQLException {
        final Statement createStatement = con.createStatement();
        createStatement.execute("SET foreign_key_checks = 0");
        createStatement.close();
    }
    
    protected void enableForeignKeyChecks(final Connection con) throws SQLException {
        final Statement createStatement = con.createStatement();
        createStatement.execute("SET foreign_key_checks = 1");
        createStatement.close();
    }
    
    
    /*
     * Tear down
     */
    @Override
    protected void tearDown() throws Exception {
        dbService.backWritable(FIRST_CID, srcCon);
        dbService.backWritable(FIRST_CID, dstCon);

        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }

}
