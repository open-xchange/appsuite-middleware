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

package com.openexchange.user.copy.internal.folder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.AbstractUserCopyTest;
import com.openexchange.user.copy.internal.folder.util.FolderEqualsWrapper;


/**
 * {@link FolderCopyTest}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderCopyTest extends AbstractUserCopyTest {   

    private int srcUsrId;
    
    private int srcCtxId;
    
    private int dstCtxId;

    private Connection srcCon;

    private Connection dstCon;
    
    private List<FolderObject> createdFolders;
    

    /**
     * Initializes a new {@link FolderCopyTest}.
     * @param name
     */
    public FolderCopyTest(final String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        super.setUp();        
        createdFolders = new ArrayList<FolderObject>();
        srcUsrId = getSourceUserId();
        srcCon = getSourceConnection();
        dstCon = getDestinationConnection();
        srcCtxId = getSourceContext().getContextId();
        dstCtxId = getDestinationContext().getContextId();  
        createFolderHierarchy();
    }
    
    /*
     * Test cases
     */
    public void testFolderCopy() throws Exception {  
        final FolderCopyTask copyTask = new FolderCopyTask();
        final SortedMap<Integer, FolderEqualsWrapper> originFolders = copyTask.loadFoldersFromDB(srcCon, srcCtxId, srcUsrId);
        ObjectMapping<FolderObject> folderMapping = null;
        try {
            DBUtils.startTransaction(dstCon);
            disableForeignKeyChecks(dstCon);
            dstCon.commit();
            folderMapping = copyTask.copyUser(getBasicObjectMapping());
            dstCon.commit();
            
            for (final int folderId : originFolders.keySet()) {
                final FolderEqualsWrapper originWrapper = originFolders.get(folderId);
                final FolderObject origin = originWrapper.getFolder();
                if (origin.getModule() == 8 && origin.getType() == 2) {
                    if (folderMapping.getSourceKeys().contains(origin.getObjectID())) {
                        checkFolders(folderMapping, folderId);
                    }
                } else {
                    checkFolders(folderMapping, folderId);
                }
            }            
        } catch (final OXException e) {
            DBUtils.rollback(dstCon);
            e.printStackTrace();
            fail("A UserCopyException occurred.");
        } finally {            
            enableForeignKeyChecks(dstCon);
            dstCon.commit();
        }
    }
    
    private void createFolderHierarchy() throws Exception {
        final SortedMap<Integer,FolderEqualsWrapper> folders = new FolderCopyTask().loadFoldersFromDB(srcCon, srcCtxId, srcUsrId);
        FolderObject contactFolder = null;
        FolderObject calendarFolder = null;
        FolderObject taskFolder = null;
        for (final FolderEqualsWrapper folderWrapper : folders.values()) {
            final FolderObject folder = folderWrapper.getFolder();
            switch(folder.getModule()) {
            
            case 1:
                if (folder.isDefaultFolder()) {
                    taskFolder = folder;
                }
                break;
           
            case 2:
                if (folder.isDefaultFolder()) {
                    calendarFolder = folder;
                }
                break;
                
            case 3:
                if (folder.isDefaultFolder()) {
                    contactFolder = folder;
                }
                break;        
                
            }
        }
        
        if (contactFolder == null) {
            fail("Did not find default contact folder.");
        }
        if (calendarFolder == null) {
            fail("Did not find default calendar folder.");
        }
        if (taskFolder == null) {
            fail("Did not find default task folder.");
        }
        
        final Random random = new Random();
        int deep = random.nextInt(5);
        if (deep == 0) {
            deep = 1;
        }
        final List<FolderObject> lastFolders = new ArrayList<FolderObject>();
        lastFolders.add(taskFolder);
        lastFolders.add(calendarFolder);
        lastFolders.add(contactFolder);
        for (int i = 0; i < deep; i++) {
            int num = random.nextInt(deep - i);
            if (num == 0) {
                num = 1;
            }
            final List<FolderObject> newFolders = new ArrayList<FolderObject>();
            for (final FolderObject folder: lastFolders) {
                for (int j = 0; j < num; j++) {
                    final FolderObject created = createFolder(srcCon, srcCtxId, folder.getObjectID(), "TestFolder" + i + "_" + j, folder.getModule(), srcUsrId);
                    newFolders.add(created);
                }
            }
            lastFolders.clear();
            lastFolders.addAll(newFolders);
            createdFolders.addAll(newFolders);
        }
        
    }
    
    private FolderObject createFolder(final Connection con, final int cid, final int parent, final String name, final int module, final int userId) throws Exception {
        PreparedStatement stmt = null;
        int newId = 0;
        try {
            final String sql = 
                "INSERT INTO " + 
                    "oxfolder_tree " + 
                    "(fuid, cid, parent, fname, module, type, creating_date, " + 
                    "created_from, changing_date, changed_from, permission_flag, " + 
                    "subfolder_flag, default_flag) " + 
                "VALUES " + 
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            DBUtils.startTransaction(con);
            newId = IDGenerator.getId(cid, com.openexchange.groupware.Types.FOLDER, con);
            con.commit();
            DBUtils.autocommit(con);
            stmt = con.prepareStatement(sql);
            int i = 1;
            final Date date = new Date();
            stmt.setInt(i++, newId);
            stmt.setInt(i++, cid);
            stmt.setInt(i++, parent);
            stmt.setString(i++, name);
            stmt.setInt(i++, module);
            stmt.setInt(i++, 1);
            stmt.setLong(i++, date.getTime());
            stmt.setInt(i++, userId); 
            stmt.setLong(i++, date.getTime());
            stmt.setInt(i++, userId); 
            stmt.setInt(i++, 1);
            stmt.setInt(i++, 0);
            stmt.setInt(i++, 0);
            
            stmt.executeUpdate();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        
        final FolderObject folder =  new FolderObject(newId);
        folder.setParentFolderID(parent);
        folder.setFolderName(name);
        folder.setModule(module);
        folder.setType(1);
        folder.setDefaultFolder(false);
        
        return folder;
    }
    
    private void checkFolders(final ObjectMapping<FolderObject> folderMapping, final int folderId) {
        final FolderObject source = folderMapping.getSource(folderId);
        if (source == null) {
            fail("Mapping did not contain source folder " + folderId);
        }
        
        final FolderObject destination = folderMapping.getDestination(source);
        if (destination == null) {
            fail("Mapping did not contain destination folder for source " + folderId);
        }
        
        if (source.getParentFolderID() == 1 && destination.getParentFolderID() != 1) {
            fail("Source folder had parent 1 but target folder did not.");
        } else if (source.getParentFolderID() == 10 && destination.getParentFolderID() != 10) {
            fail("Source folder had parent 10 but target folder did not.");
        } else if (source.getParentFolderID() == 15 && destination.getParentFolderID() != 15) {
            fail("Source folder had parent 15 but target folder did not.");
        } else if (source.getParentFolderID() == 9 && destination.getParentFolderID() != 9) {
            fail("Source folder had parent 9 but target folder did not.");
        } else if (source.getParentFolderID() != 1 && source.getParentFolderID() != 9 && source.getParentFolderID() != 10 && source.getParentFolderID() != 15 && source.getType() != 5) {
            final FolderObject sourceParent = folderMapping.getSource(source.getParentFolderID());
            final FolderObject destinationParent = folderMapping.getDestination(sourceParent);
            assertEquals("Destination folder has wrong parent.", destinationParent.getObjectID(), destination.getParentFolderID());
        }  
        
        if (destination.getParentFolderID() == 15 && destination.getType() == 2 && destination.getModule() == 8) {
            /*
             * Ignore mail attachment folder.
             */
            return;
        }
        
        assertEquals("Folder name was not equal. Source: " + folderId, source.getFolderName(), destination.getFolderName());
        assertEquals("Module was not equal. Source: " + folderId, source.getModule(), destination.getModule());
        assertEquals("Type was not equal. Source: " + folderId, source.getType(), destination.getType());
        assertEquals("Creating date was not equal. Source: " + folderId, source.getCreationDate(), destination.getCreationDate());
        assertEquals("Changing date was not equal. Source: " + folderId, source.getLastModified(), destination.getLastModified());
        assertEquals("Permission flag was not equal. Source: " + folderId, source.getPermissionFlag(), destination.getPermissionFlag());
        assertEquals("Subfolder flag was not equal. Source: " + folderId, source.hasSubfolders(), destination.hasSubfolders());
        assertEquals("Default flag was not equal. Source: " + folderId, source.isDefaultFolder(), destination.isDefaultFolder());
    }
    
    /*
     * Clean up methods
     */
    private void cleanDatabase() throws Exception {
        DBUtils.autocommit(srcCon);
        DBUtils.autocommit(dstCon);
        
        /*
         * Delete created test folders
         */
        final StringBuilder sb = new StringBuilder("DELETE FROM oxfolder_tree WHERE cid = ");
        sb.append(srcCtxId);
        sb.append(" AND fuid IN (");
        boolean first = true;
        for (final FolderObject folder : createdFolders) {
            if (first) {
                sb.append(folder.getObjectID());
                first = false;
            } else {
                sb.append(", ");
                sb.append(folder.getObjectID());
            }
        }        
        sb.append(')');
        
        final Statement stmt = srcCon.createStatement();
        stmt.executeUpdate(sb.toString());
        stmt.close();        
        
        
        /*
         * Delete copied folders
         */
        deleteAllFromTablesForCid(dstCtxId, "cid", dstCon, "oxfolder_tree", "oxfolder_permissions", "virtualTree");
    }
    
    @Override
    protected void tearDown() throws Exception {
        cleanDatabase();
        super.tearDown();
    }

    /**
     * @see com.openexchange.user.copy.internal.AbstractUserCopyTest#getSequenceTables()
     */
    @Override
    protected String[] getSequenceTables() {
        return new String[] {"sequence_folder"};
    }

}
