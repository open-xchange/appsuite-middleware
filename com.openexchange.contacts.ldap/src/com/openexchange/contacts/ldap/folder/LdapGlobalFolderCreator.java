package com.openexchange.contacts.ldap.folder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;


public class LdapGlobalFolderCreator {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapGlobalFolderCreator.class);
    
    private static String globalLdapFolderName = "LDAPneu9";
    
    private static int testcontextid = 111;
    
    public static int createGlobalFolder() throws OXException, DBPoolingException, SQLException {
        // First search for a folder with the name if is doesn't exist create it
        final Context ctx = new ContextImpl(testcontextid);
        final Connection readCon = DBPool.pickup(ctx);
        int ldapFolderID;
        final int admin_user_id;
        try {
            admin_user_id = OXFolderSQL.getContextAdminID(ctx, readCon);
            ldapFolderID = getLdapFolderID(globalLdapFolderName, ctx, readCon);
        } finally {
            DBPool.closeReaderSilent(ctx, readCon);
        }

        if (-1 == ldapFolderID) {
            final FolderObject fo = createFolderObject(admin_user_id);
            // As we have no possibility right now to access the foldermanager without a session, we have to create
            // a dummy session object here, which provides the needed information
            final Session dummysession = getDummySessionObj(admin_user_id);
            final OXFolderManager instance = OXFolderManager.getInstance(dummysession);
            ldapFolderID = instance.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
            if (LOG.isInfoEnabled()) {
                LOG.info("LDAP folder successfully created");
            }
        }
        return ldapFolderID;
    }

    /**
     * @param globalLdapFolderName2
     * @param ctx
     * @param writeCon
     * @return the id or -1 if not found 
     * @throws SQLException 
     */
    private static int getLdapFolderID(String globalLdapFolderName2, Context ctx, Connection readCon) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = readCon.prepareStatement("SELECT fuid from oxfolder_tree WHERE cid=? AND fname=?");
            ps.setInt(1, ctx.getContextId());
            ps.setString(2, globalLdapFolderName2);
            final ResultSet executeQuery = ps.executeQuery();
            while (executeQuery.next()) {
                return executeQuery.getInt(1);
            }
            return -1;
        } catch (final SQLException e) {
            throw e;
        } finally {
            try {
                if (null != ps) {
                    ps.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static FolderObject createFolderObject(final int admin_user_id) {
        final FolderObject fo = new FolderObject();
        final OCLPermission defaultPerm = new OCLPermission();
        defaultPerm.setEntity(admin_user_id);
        defaultPerm.setGroupPermission(false);
        defaultPerm.setAllPermission(
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION,
                OCLPermission.ADMIN_PERMISSION);
        defaultPerm.setFolderAdmin(true);
         
        final OCLPermission allPerm = new OCLPermission();
        allPerm.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        allPerm.setGroupPermission(true);
        allPerm.setAllPermission(
                OCLPermission.READ_FOLDER,
                OCLPermission.READ_ALL_OBJECTS,
                OCLPermission.NO_PERMISSIONS,
                OCLPermission.NO_PERMISSIONS);
        allPerm.setFolderAdmin(false);
        fo.setPermissionsAsArray(new OCLPermission[] { defaultPerm, allPerm });
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setType(FolderObject.PUBLIC);
        fo.setFolderName(globalLdapFolderName);
        fo.setModule(FolderObject.CONTACT);
        return fo;
    }

    private static Session getDummySessionObj(final int admin_user_id) {
        final Session dummysession = new Session(){

            public int getContextId() {
                return testcontextid;
            }

            public String getLocalIp() {
                return null;
            }

            public String getLogin() {
                return null;
            }

            public String getLoginName() {
                return null;
            }

            public Object getParameter(String name) {
                return null;
            }

            public String getPassword() {
                return null;
            }

            public String getRandomToken() {
                return null;
            }

            public String getSecret() {
                return null;
            }

            public String getSessionID() {
                return null;
            }

            public ManagedUploadFile getUploadedFile(String id) {
                return null;
            }

            public int getUserId() {
                return admin_user_id;
            }

            public String getUserlogin() {
                return null;
            }

            public void putUploadedFile(String id, ManagedUploadFile uploadFile) {
            }

            public void removeRandomToken() {
            }

            public ManagedUploadFile removeUploadedFile(String id) {
                return null;
            }

            public void removeUploadedFileOnly(String id) {
            }

            public void setParameter(String name, Object value) {
            }

            public boolean touchUploadedFile(String id) {
                return false;
            }
            
        };
        return dummysession;
    }
}
