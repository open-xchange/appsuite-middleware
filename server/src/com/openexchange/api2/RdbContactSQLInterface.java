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

package com.openexchange.api2;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.contact.LdapServer;
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.Classes;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactExceptionFactory;
import com.openexchange.groupware.contact.ContactMySql;
import com.openexchange.groupware.contact.ContactSql;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.contact.Contacts.mapper;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.Arrays;
import com.openexchange.tools.iterator.PrefetchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

@OXExceptionSource(
    classId=Classes.COM_OPENEXCHANGE_API2_DATABASEIMPL_RDBCONTACTSQLINTERFACE,
    component=EnumComponent.CONTACT
)
public class RdbContactSQLInterface implements ContactSQLInterface {

    private static final String ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D = "Unable to load objects. Context %1$d User %2$d";
    private final int userId;
    private final int[] memberInGroups;
    private final Context ctx;
    private final Session session;
    private final UserConfiguration userConfiguration;

    private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(RdbContactSQLInterface.class);

    private static final Log LOG = LogFactory.getLog(RdbContactSQLInterface.class);

    public RdbContactSQLInterface(final Session session) throws ContextException {
        final Context ctx = ContextStorage.getStorageContext(session);
        this.ctx = ctx;
        this.userId = session.getUserId();
        this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
        this.session = session;
        userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
                ctx);
    }

    public RdbContactSQLInterface(final Session session, final Context ctx) {
        this.userId = session.getUserId();
        this.memberInGroups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
        this.ctx = ctx;
        this.session = session;
        userConfiguration = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
                ctx);
    }


    @OXThrows(
            category=Category.CODE_ERROR,
            desc="0",
            exceptionId=0,
            msg= ContactException.EVENT_QUEUE
    )
    public void insertContactObject(final ContactObject co) throws OXException {
        try{
            Contacts.performContactStorageInsert(co,userId,session);
            final EventClient ec = new EventClient(session);
            ec.create(co);
            /*
            ContactObject coo = new ContactObject();
            coo.setSurName("Hoeger");
            ContactSQLInterface csql = new RdbContactSQLInterface(sessionobject);
            csql.insertContactObject(coo);
            */
        }catch (final EventException ise){
            throw EXCEPTIONS.create(0,ise);
        }catch (final ContextException ise){
            throw EXCEPTIONS.create(0,ise);
        }catch (final OXConflictException ce){
            LOG.debug("Unable to insert contact", ce);
            throw ce;
        }catch (final OXException e){
            LOG.debug("Problem while inserting contact.", e);
            throw e;
        }
    }

    @OXThrows(
            category=Category.CODE_ERROR,
            desc="1",
            exceptionId=1,
            msg= ContactException.EVENT_QUEUE
    )
    public void updateContactObject(final ContactObject co, final int fid, final java.util.Date d) throws OXException, OXConcurrentModificationException, ContactException {

        try{
            Contacts.performContactStorageUpdate(co,fid,d,userId,memberInGroups,ctx,userConfiguration);
            final EventClient ec = new EventClient(session);
            ec.modify(co);
        }catch (final ContactException ise){
            throw ise;
        }catch (final EventException ise){
            throw EXCEPTIONS.create(1,ise);
        }catch (final ContextException ise){
            throw EXCEPTIONS.create(1,ise);
        }catch (final OXConcurrentModificationException cme){
            throw cme;
        }catch (final OXConflictException ce){
            throw ce;
        }catch (final OXObjectNotFoundException oonfee){
            throw oonfee;
        }catch (final OXException e){
            throw e;
        }
    }

    @OXThrowsMultiple(
            category={    Category.PERMISSION,
                                    Category.SOCKET_CONNECTION,
                                    Category.PERMISSION,
                                    Category.PERMISSION,
                                    Category.CODE_ERROR
                                },
            desc={"2","3","4","5","6"},
            exceptionId={2,3,4,5,6},
            msg={    ContactException.NON_CONTACT_FOLDER_MSG,
                            ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            ContactException.NO_PERMISSION_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            "Unable fetch the number of elements in this Folder. Context %1$d Folder %2$d User %3$d"
                        }
    )
    public int getNumberOfContacts(final int folderId) throws OXException {
        Connection readCon = null;
        try {
            readCon = DBPool.pickup(ctx);

            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(2,Integer.valueOf(folderId),Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getNumberOfContacts() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }

        } catch (final OXException e) {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
            throw e;
            //throw new OXException("getNumberOfContacts() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
        } catch (final DBPoolingException e) {
            throw EXCEPTIONS.create(3,e);
        }

        try {
            final ContactSql contactSQL = new ContactMySql(session, ctx);
            final EffectivePermission oclPerm = new OXFolderAccess(readCon, ctx).getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw EXCEPTIONS.createOXConflictException(4,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    contactSQL.setReadOnlyOwnFolder(userId);
                } else {
                    throw EXCEPTIONS.createOXConflictException(5,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                    //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
                }
            }
            contactSQL.setSelect(contactSQL.iFgetNumberOfContactsString());
            contactSQL.setFolder(folderId);
            int retval = 0;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = contactSQL.getSqlStatement(readCon);
                rs = ((PreparedStatement) stmt).executeQuery();

                if (rs.next()) {
                    retval = rs.getInt(1);
                }
            } catch (final SQLException e) {
                throw EXCEPTIONS.create(6,e,Integer.valueOf(ctx.getContextId()),Integer.valueOf(folderId), Integer.valueOf(userId));
                //throw new OXException("Exception during getNumberOfContacts() for User "+ userId + " in folder " + folderId + " cid= " +sessionobject.getContext().getContextId()+' ' + "\n:"+ e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
            return retval;
        } catch (final OXException e) {
            throw e;
        }  finally {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
    }

    @OXThrowsMultiple(
            category={    Category.SOCKET_CONNECTION,
                                    Category.CODE_ERROR,
                                    Category.PERMISSION,
                                    Category.PERMISSION,
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR

                                },
            desc={"7","8","9","10","11","12"},
            exceptionId={7,8,9,10,11,12},
            msg={    ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            ContactException.NON_CONTACT_FOLDER_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            "An error occurred during the load of folder objects. Context %1$d Folder %2$d User %3$d",
                            "An error occurred during the load of folder objects. Context %1$d Folder %2$d User %3$d"
                        }
    )
    public SearchIterator<ContactObject> getContactsInFolder(final int folderId, final int from, final int to, final int order_field, final String orderMechanism, final int[] cols) throws OXException {
        boolean error = false;
        String orderDir = orderMechanism;
        int orderBy = order_field;
        if (orderBy == 0){
            orderBy = 502;
        }
        if (orderDir == null || orderDir.length() < 1){
            orderDir = " ASC ";
        }
        Connection readCon = null;
        try{
            readCon = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw EXCEPTIONS.create(7,e);
            //throw new OXException("UNABLE TO GET READ CONNECTION", e);
        }

        final OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);

        try {
            final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(8,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getContactsInFolder() called with a non-Contact-Folder!  (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
        } catch (final OXException e) {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
            throw e;
            //throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
        }

        SearchIterator<ContactObject> si = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            final ContactSql cs = new ContactMySql(session, ctx);
            cs.setFolder(folderId);

            final EffectivePermission oclPerm = folderAccess.getFolderPermission(folderId, userId, userConfiguration);

            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw EXCEPTIONS.createOXConflictException(9,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    cs.setReadOnlyOwnFolder(userId);
                } else {
                    throw EXCEPTIONS.createOXConflictException(10,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                    //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
                }
            }

            //stmt = readCon.createStatement();


            final int dest = to - from;
            String limits = "";
            if (from != 0 || to != 0){
                limits = " LIMIT "    + from + ',' + dest + ' ';
            }

            if (orderBy > 0) {
                final String order = new StringBuilder(32).append(" ORDER BY co.").append(
                        Contacts.mapping[orderBy].getDBFieldName()).append(' ').append(orderDir).append(limits)
                        .toString();
                cs.setOrder(order);
            } else {
                cs.setOrder(limits);
            }

            cs.setSelect(cs.iFgetColsString(cols).toString());

            stmt = cs.getSqlStatement(readCon);
            rs = ((PreparedStatement) stmt).executeQuery();

            si = new ContactObjectIterator(rs, stmt, cols, false, readCon);
            //return new PrefetchIterator(new ContactObjectIterator(rs, stmt, cols, false, readCon));

        } catch (final SearchIteratorException e){
            error = true;
            throw EXCEPTIONS.create(11,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final SQLException e) {
            error = true;
            throw EXCEPTIONS.create(12,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final OXException e) {
            error = true;
            throw e;
            //throw new OXException("Exception during getContactsInFolder() for User " + userId+ " in folder " + folderId +  " cid="+sessionobject.getContext().getContextId()+"\n:" + e.getMessage(),    e);
        } finally {
            if (error){
                try{
                    if (rs != null){
                        rs.close();
                    }
                    if (stmt != null){
                        stmt.close();
                    }
                } catch (final SQLException sxe){
                    LOG.error("Unable to close Statement or ResultSet",sxe);
                }
                try{
                    if (readCon != null) {
                        DBPool.closeReaderSilent(ctx, readCon);
                    }
                } catch (final Exception ex){
                    LOG.error("Unable to return Connection",ex);
                }
            }
        }

        return new PrefetchIterator<ContactObject>(si);
    }

    @OXThrowsMultiple(
        category = { Category.SOCKET_CONNECTION, Category.CODE_ERROR, Category.PERMISSION, Category.PERMISSION, Category.CODE_ERROR,
            Category.CODE_ERROR, Category.SOCKET_CONNECTION },
        desc = { "13", "14", "15", "16", "17", "18", "19" },
        exceptionId = { 13, 14, 15, 16, 17, 18, 19 },
        msg = { ContactException.INIT_CONNECTION_FROM_DBPOOL, ContactException.NON_CONTACT_FOLDER_MSG, ContactException.NO_PERMISSION_MSG,
            ContactException.NO_PERMISSION_MSG,
            "An error occurred during the load of folder objects by an extended search. Context %1$d User %2$d",
            "An error occurred during the load of folder objects by an extended search. Context %1$d User %2$d",
            ContactException.INIT_CONNECTION_FROM_DBPOOL
        }
    )
    public SearchIterator<ContactObject> getContactsByExtendedSearch(final ContactSearchObject searchobject,  final int order_field, final String orderMechanism, final int[] cols) throws OXException {
        final int orderBy;
        if (order_field == 0) {
            orderBy = ContactObject.SUR_NAME;
        } else {
            orderBy = order_field;
        }
        final String orderDir;
        if (orderMechanism == null || orderMechanism.length() < 1) {
            orderDir = " ASC ";
        } else {
            orderDir = orderMechanism;
        }
        final OXFolderAccess oxfs = new OXFolderAccess(ctx);
        final ContactSql cs = new ContactMySql(session, ctx);
        if (searchobject.getEmailAutoComplete()) {
            searchobject.addFolder(oxfs.getDefaultFolder(userId, FolderObject.CONTACT).getObjectID());
            try {
                final Integer contactCollectFolder = ServerUserSetting.getContactCollectionFolder(ctx.getContextId(), userId);
                if (null != contactCollectFolder && oxfs.exists(contactCollectFolder.intValue())) {
                    searchobject.addFolder(contactCollectFolder.intValue());
                }
            } catch (final SettingException e) {
                LOG.error(e.getMessage(), e);
            }
            searchobject.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        }
        if (searchobject.hasFolders()) {
            for (final int folderId : searchobject.getFolders()) {
                final FolderObject contactFolder = new OXFolderAccess(ctx).getFolderObject(folderId);
                if (contactFolder.getModule() != FolderObject.CONTACT) {
                    throw EXCEPTIONS.createOXConflictException(14, I(folderId), I(ctx.getContextId()), I(userId));
                }
                final EffectivePermission oclPerm = oxfs.getFolderPermission(folderId, userId, userConfiguration);
                if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                    throw EXCEPTIONS.createOXConflictException(15, I(folderId), I(ctx.getContextId()), I(userId));
                }
                if (!oclPerm.canReadOwnObjects()) {
                    throw EXCEPTIONS.createOXConflictException(16, I(folderId), I(ctx.getContextId()), I(userId));
                }
            }
            searchobject.setAllFolderSQLINString(cs.buildFolderSearch(userId, memberInGroups, searchobject.getFolders(), session));
        } else {
            try {
                searchobject.setAllFolderSQLINString(cs.buildAllFolderSearchString(userId, memberInGroups, session).toString());
            } catch (final SearchIteratorException e) {
                throw new OXException(e);
            }
        }
        final String order = new StringBuilder(32).append(" ORDER BY co.").append(Contacts.mapping[orderBy].getDBFieldName()).append(' ').append(
            orderDir).append(' ').toString();
        cs.setOrder(order);
        cs.setContactSearchObject(searchobject);
        cs.setSelect(cs.iFgetColsString(cols).toString());
        final Connection con;
        try {
            con = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw EXCEPTIONS.create(13,e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = cs.getSqlStatement(con);
            result = stmt.executeQuery();
        } catch (final SQLException e) {
            DBUtils.closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
            throw EXCEPTIONS.create(18, e, I(ctx.getContextId()), I(userId));
        }
        final SearchIterator<ContactObject> si;
        try {
            si = new ContactObjectIterator(result, stmt, cols, false, con);
        } catch (final SearchIteratorException e) {
            DBUtils.closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
            throw EXCEPTIONS.create(17, e, I(ctx.getContextId()), I(userId));
        } catch (final Exception e) {
            DBUtils.closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(ctx, con);
            final String msg = e.getMessage();
            throw new ContactException(Category.CODE_ERROR, 9999, ContactException.UNEXPECTED_ERROR, e, null == msg ? "not available" : msg);
        }
        return new PrefetchIterator<ContactObject>(si);
    }

    @OXThrowsMultiple(
            category={    Category.SOCKET_CONNECTION,
                                    Category.CODE_ERROR,
                                    Category.PERMISSION,
                                    Category.PERMISSION,
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR
                                },
            desc={"20","21","22","23","24","25"},
            exceptionId={20,21,22,23,24,25},
            msg={    ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            ContactException.NON_CONTACT_FOLDER_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            "An error occurred during the load of folder objects by a simple search. Context %1$d Folder %2$d User %3$d",
                            "An error occurred during the load of folder objects by a simple search. Context %1$d Folder %2$d User %3$d"
                        }
    )
    public SearchIterator<ContactObject> searchContacts(final String searchpattern, final boolean startletter, final int folderId, final int order_field, final String orderMechanism, final int[] cols) throws OXException {
        boolean error = false;
        String orderDir = orderMechanism;
        int orderBy = order_field;
        if (orderBy == 0){
            orderBy = 502;
        }
        if (orderDir == null || orderDir.length() < 1){
            orderDir = " ASC ";
        }
        Connection readcon = null;
        try{
            readcon = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw EXCEPTIONS.create(20,e);
            //throw new OXException("UNABLE TO GET READ CONNECTION", e);
        }

        final OXFolderAccess folderAccess = new OXFolderAccess(readcon, ctx);

        try {
            final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(21,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
        } catch (final OXException e) {
            if (readcon != null) {
                DBPool.closeReaderSilent(ctx, readcon);
            }
            throw e;
            //throw new OXException("getContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
        }

        SearchIterator<ContactObject> si = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(session, ctx);
            cs.setFolder(folderId);
            cs.setSearchHabit(" OR ");
            final String order = new StringBuilder(32).append(" ORDER BY co.").append(Contacts.mapping[orderBy].getDBFieldName()).append(' ').append(orderDir).append(' ').toString();
            cs.setOrder(order);

            final EffectivePermission oclPerm = folderAccess.getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw EXCEPTIONS.createOXConflictException(22,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    cs.setReadOnlyOwnFolder(userId);
                } else {
                    throw EXCEPTIONS.createOXConflictException(23,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                    //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
                }
            }

            final ContactSearchObject cso = new ContactSearchObject();
            if (startletter){
                cs.setStartCharacter(searchpattern);
                cs.setStartCharacterField(ContactConfig.getInstance().getProperty("contact_first_letter_field"));
            }else{
                cso.setDisplayName(searchpattern);
                cso.setGivenName(searchpattern);
                cso.setSurname(searchpattern);
                cso.setEmail1(searchpattern);
                cso.setEmail2(searchpattern);
                cso.setEmail3(searchpattern);
                cso.setCatgories(searchpattern);
            }

            cs.setContactSearchObject(cso);

            cs.setSelect(cs.iFgetColsString(cols).toString());
            stmt = cs.getSqlStatement(readcon);
            rs = ((PreparedStatement) stmt).executeQuery();
            si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
        } catch (final SearchIteratorException e){
            error = true;
            throw EXCEPTIONS.create(24,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final SQLException e) {
            error = true;
            throw EXCEPTIONS.create(25,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final OXException e) {
            error = true;
            throw e;
            //throw new OXException("Exception during getContactsInFolder() for User " + userId    + " in folder " + folderId +  " cid="+sessionobject.getContext().getContextId()+"\n:" + e.getMessage(),    e);
        } finally {
            if (error){
                try{
                    if (rs != null){
                        rs.close();
                    }
                    if (stmt != null){
                        stmt.close();
                    }
                } catch (final SQLException sxe){
                    LOG.error("Unable to close Statement or ResultSet",sxe);
                }
                try{
                    if (readcon != null) {
                        DBPool.closeReaderSilent(ctx, readcon);
                    }
                } catch (final Exception ex){
                    LOG.error("Unable to return Connection",ex);
                }
            }
        }

        return new PrefetchIterator<ContactObject>(si);
    }

    @OXThrowsMultiple(
            category={    Category.TRY_AGAIN,
                                    Category.CODE_ERROR,
                                    Category.PERMISSION,
                                    Category.SOCKET_CONNECTION
                                },
            desc={"26","27","28","29"},
            exceptionId={26,27,28,29},
            msg={    "The object you requested can not be found. Try again. Context %1$d Folder %2$d User %3$d Object %4$d",
                            ContactException.NON_CONTACT_FOLDER_MSG,
                            ContactException.NO_READ_PERMISSION_MSG,
                            ContactException.INIT_CONNECTION_FROM_DBPOOL
                        }
    )
    public ContactObject getObjectById(final int objectId, final int fid) throws OXException {

        Connection readCon = null;
        ContactObject co = null;
        try{
            readCon = DBPool.pickup(ctx);
            if (objectId > 0){
                co = Contacts.getContactById(objectId, userId, memberInGroups, ctx, userConfiguration, readCon);
            }else{
                throw EXCEPTIONS.createOXObjectNotFoundException(26,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fid), Integer.valueOf(userId), Integer.valueOf(objectId));
                //throw new OXObjectNotFoundException("NO CONTACT FOUND! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
            }

            final int folderId = co.getParentFolderID();

            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(27,Integer.valueOf(fid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getObjectById() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
            }

            if (!performSecurityReadCheck(folderId,co.getCreatedBy(), userId, memberInGroups,session, readCon, ctx)){
                throw EXCEPTIONS.createOXConflictException(28,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE OBJECTS");
            }
        } catch (final DBPoolingException e){
            throw EXCEPTIONS.create(29,e);
        } catch (final OXException e){
            throw e;
            //throw new OXException("UNABLE TO LOAD CONTACT BY ID - CHECK RIGHTS (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+objectId+')', e);
        }  finally {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
        return co;
    }

    public ContactObject getUserById(final int userid) throws OXException {
        Connection readCon = null;
        ContactObject co = null;
        final int fid = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        try{
            readCon = DBPool.pickup(ctx);
            if (userid > 0){
                co = Contacts.getUserById(userid, userId, memberInGroups, ctx, userConfiguration, readCon);
            }else{
                throw EXCEPTIONS.createOXObjectNotFoundException(26,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fid), Integer.valueOf(userId), Integer.valueOf(userid));
                //throw new OXObjectNotFoundException("NO CONTACT FOUND! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
            }

            final int folderId = co.getParentFolderID();

            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(27,Integer.valueOf(fid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getObjectById() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
            }

            if (!performSecurityReadCheck(folderId,co.getCreatedBy(), userId, memberInGroups,session, readCon, ctx)){
                throw EXCEPTIONS.createOXConflictException(28,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE OBJECTS");
            }
        } catch (final DBPoolingException e){
            throw EXCEPTIONS.create(29,e);
        } catch (final OXException e){
            throw e;
            //throw new OXException("UNABLE TO LOAD CONTACT BY ID - CHECK RIGHTS (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+objectId+')', e);
        }  finally {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
        return co;
    }

    public ContactObject getUserById(final int userid, final Connection readCon) throws OXException {
        ContactObject co = null;
        final int fid = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        try{
            if (userid > 0){
                co = Contacts.getUserById(userid, userId, memberInGroups, ctx, userConfiguration, readCon);
            }else{
                throw EXCEPTIONS.createOXObjectNotFoundException(26,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fid), Integer.valueOf(userId), Integer.valueOf(userid));
                //throw new OXObjectNotFoundException("NO CONTACT FOUND! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
            }

            final int folderId = co.getParentFolderID();

            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(27,Integer.valueOf(fid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getObjectById() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+')');
            }

            if (!performSecurityReadCheck(folderId,co.getCreatedBy(), userId, memberInGroups,session, readCon, ctx)){
                throw EXCEPTIONS.createOXConflictException(28,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE OBJECTS");
            }
        } catch (final OXException e){
            throw e;
            //throw new OXException("UNABLE TO LOAD CONTACT BY ID - CHECK RIGHTS (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+objectId+')', e);
        }
        return co;
    }

    @OXThrowsMultiple(
            category={    Category.SOCKET_CONNECTION,
                                    Category.CODE_ERROR,
                                    Category.PERMISSION,
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR
                                },
            desc={"30","31","32","33","34","35"},
            exceptionId={30,31,32,33,34,35},
            msg={    ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            ContactException.NON_CONTACT_FOLDER_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            ContactException.NO_PERMISSION_MSG,
                            "An error occurred during the load of modified objects from a folder. Context %1$d Folder %2$d User %3$d",
                            "An error occurred during the load of modified objects from a folder. Context %1$d Folder %2$d User %3$d"
                        }
    )
    public SearchIterator<ContactObject> getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        boolean error = false;
        Connection readCon = null;
        try{
            readCon = DBPool.pickup(ctx);
        } catch (final Exception e) {
            throw EXCEPTIONS.create(30,e);
            //throw new OXException("UNABLE TO GET READ CONNECTION", e);
        }
        final OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);
        try {
            final FolderObject contactFolder = folderAccess.getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(31,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("getModifiedContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
        } catch (final OXException e) {
            if (readCon != null) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
            throw e;
            //throw new OXException("getModifiedContactsInFolder() called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
        }

        SearchIterator<ContactObject> si = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(session, ctx);

            final EffectivePermission oclPerm = folderAccess.getFolderPermission(folderId, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw EXCEPTIONS.createOXConflictException(32,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects()) {
                    cs.setReadOnlyOwnFolder(userId);
                } else {
                    throw EXCEPTIONS.createOXConflictException(33,Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                    //throw new OXConflictException("NOT ALLOWED TO SEE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+folderId+')');
                }
            }

            if (folderId == FolderObject.SYSTEM_LDAP_FOLDER_ID){
                cs.getInternalUsers();
            }else{
                cs.setFolder(folderId);
            }

            //stmt = readCon.createStatement();

            cs.getAllChangedSince(since.getTime());
            cs.setSelect(cs.iFgetColsString(cols).toString());

            stmt = cs.getSqlStatement(readCon);
            rs = ((PreparedStatement) stmt).executeQuery();

            si = new ContactObjectIterator(rs, stmt, cols, false, readCon);
        } catch (final SearchIteratorException e){
            error = true;
            throw EXCEPTIONS.create(34,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final SQLException e){
            error = true;
            throw EXCEPTIONS.create(35,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final OXException e) {
            error = true;
            throw e;
            //throw new OXException(    "Exception during getContactsInFolder() for User " + userId+ " in folder " + folderId+ "(cid="+sessionobject.getContext().getContextId()+')',    e);
        } finally {
            if (error){
                try{
                    if (rs != null){
                        rs.close();
                    }
                    if (stmt != null){
                        stmt.close();
                    }
                } catch (final SQLException sxe){
                    LOG.error("Unable to close Statement or ResultSet",sxe);
                }
                try{
                    if (readCon != null) {
                        DBPool.closeReaderSilent(ctx, readCon);
                    }
                } catch (final Exception ex){
                    LOG.error("Unable to return Connection",ex);
                }
            }
        }
        return new PrefetchIterator<ContactObject>(si);
    }

    @OXThrowsMultiple(
            category={    Category.SOCKET_CONNECTION,
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR
                                },
            desc={"36","37","38"},
            exceptionId={36,37,38},
            msg={    ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            "An error occurred during the load of deleted objects from a folder. Context %1$d Folder %2$d User %3$d",
                            "An error occurred during the load of deleted objects from a folder. Context %1$d Folder %2$d User %3$d"
                        }
    )
    public SearchIterator<ContactObject> getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        boolean error = false;
        SearchIterator<ContactObject> si = null;
        Connection readcon = null;
        Statement stmt = null;
        ResultSet rs = null;
        try{
            readcon = DBPool.pickup(ctx);

            final ContactSql cs = new ContactMySql(session, ctx);
            cs.setFolder(folderId);

            //stmt = readcon.createStatement();

            cs.getAllChangedSince(since.getTime());
            cs.setSelect(cs.iFgetColsStringFromDeleteTable(cols).toString());
            cs.setOrder(" ORDER BY co.field02 ");

            stmt = cs.getSqlStatement(readcon);
            rs = ((PreparedStatement) stmt).executeQuery();

            si = new ContactObjectIterator(rs, stmt, cols, false, readcon);
        } catch (final SearchIteratorException e) {
            error = true;
            throw EXCEPTIONS.create(37,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
        } catch (final DBPoolingException e) {
            error = true;
            throw EXCEPTIONS.create(36,e);
        } catch (final SQLException e) {
            error = true;
            throw EXCEPTIONS.create(38,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(folderId), Integer.valueOf(userId));
            //throw new OXException("Exception during getDeletedContactsInFolder() for User " + userId+ " in folder " + folderId+ "(cid="+sessionobject.getContext().getContextId()+')',    e);
        } finally {
            if (error){
                try{
                    if (rs != null){
                        rs.close();
                    }
                    if (stmt != null){
                        stmt.close();
                    }
                } catch (final SQLException sxe){
                    LOG.error("Unable to close Statement or ResultSet",sxe);
                }
                try{
                    if (readcon != null) {
                        DBPool.closeReaderSilent(ctx, readcon);
                    }
                } catch (final Exception ex){
                    LOG.error("Unable to return Connection",ex);
                }
            }
        }
        return new PrefetchIterator<ContactObject>(si);
    }

    @OXThrowsMultiple(
            category={     Category.CODE_ERROR,
                                    Category.TRY_AGAIN,
                                    Category.CODE_ERROR,
                                    Category.PERMISSION,
                                    Category.PERMISSION,
                                    Category.SOCKET_CONNECTION,
                                    Category.CODE_ERROR,
                                    Category.SOCKET_CONNECTION,
                                    Category.PERMISSION,
                                    Category.CODE_ERROR
                                },
            desc={"39","40","41","42","58","43","44","45","46","56"},
            exceptionId={39,40,41,42,58,43,44,45,46,56},
            msg={    "Unable to delete this contact. Object not found. Context %1$d Folder %2$d User %3$d Object %4$d",
                            ContactException.OBJECT_HAS_CHANGED_MSG+" Context %1$d Folder %2$d User %3$d Object %4$d",
                            ContactException.NON_CONTACT_FOLDER_MSG,
                            ContactException.NO_DELETE_PERMISSION_MSG,
                            ContactException.NO_DELETE_PERMISSION_MSG,
                            ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            "Unable to delete contact object. Context %1$d Folder %2$d User %3$d Object %4$d",
                            ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            ContactException.NO_DELETE_PERMISSION_MSG,
                            ContactException.EVENT_QUEUE
                        }
    )
    public void deleteContactObject(final int oid, final int fuid, final Date client_date) throws OXObjectNotFoundException, OXConflictException, OXException {
        Connection writecon = null;
        Connection readcon = null;
        EffectivePermission oclPerm = null;
        int created_from = 0;
        final ContactObject co = new ContactObject();
        Statement smt = null;
        ResultSet rs = null;
        try{
            readcon = DBPool.pickup(ctx);

            int fid = 0;
            boolean pflag = false;
            Date changing_date = null;
            final ContactSql cs = new ContactMySql(session, ctx);
            smt = readcon.createStatement();
            rs = smt.executeQuery(cs.iFdeleteContactObject(oid,ctx.getContextId()));
            if (rs.next()){
                fid = rs.getInt(1);
                created_from = rs.getInt(2);

                co.setCreatedBy(created_from);
                co.setParentFolderID(fid);
                co.setObjectID(oid);

                final long xx = rs.getLong(3);
                changing_date = new java.util.Date(xx);
                final int pf = rs.getInt(4);
                if (!rs.wasNull() && pf > 0){
                    pflag = true;
                }
            }else{
                throw EXCEPTIONS.createOXObjectNotFoundException(39,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
                //throw new OXObjectNotFoundException();
            }

            //try{
            if ( (client_date != null && client_date.getTime() >= 0) && (client_date.before(changing_date))) {
                throw EXCEPTIONS.createOXConcurrentModificationException(40,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
                //throw new OXConflictException("CONTACT HAS CHANGED ON SERVER SIDE SINCE THE LAST VISIT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')');
            }
                /*
            } catch (Exception np3){
                LOG.error("UNABLE TO PERFORM CONTACT DELETE LAST-MODIFY-TEST", np3);
            }
            */
            final OXFolderAccess folderAccess = new OXFolderAccess(readcon, ctx);
            final FolderObject contactFolder = folderAccess.getFolderObject(fid);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw EXCEPTIONS.createOXConflictException(41,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXException("deleteContactObject called with a non-Contact-Folder! (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
            }

            if ((contactFolder.getType() != FolderObject.PRIVATE) && pflag){
                LOG.debug(new StringBuilder("Here is a contact in a non PRIVATE folder with a set private flag -> (cid=").append(ctx.getContextId()).append(" fid=").append(fid).append(" oid=").append(oid).append(')'));
            } else if ((contactFolder.getType() == FolderObject.PRIVATE) && pflag && created_from != userId){
                throw EXCEPTIONS.createOXPermissionException(42,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS CONTACT CUZ IT IS PRIVATE (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
            }

            oclPerm = folderAccess.getFolderPermission(fid, userId, userConfiguration);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw EXCEPTIONS.createOXPermissionException(58,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+fid+" oid="+oid+')');
            }
        }catch (final DBPoolingException xe){
            throw EXCEPTIONS.create(43,xe);
            //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', xe);
        }catch (final OXObjectNotFoundException xe){
            throw xe;
            //throw new OXObjectNotFoundException("NOT ALLOWED TO DELETE FOLDER OBJECTS CUZ NO OBJECT FOUND (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')',xe);
        }catch (final SQLException e){
            throw EXCEPTIONS.create(44,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(fuid), Integer.valueOf(userId), Integer.valueOf(oid));
            //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);
        }catch (final OXException e){
            throw e;
            //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);
        } finally {
            try{
                if (rs != null){
                    rs.close();
                }
                if (smt != null){
                    smt.close();
                }
            } catch (final SQLException sxe){
                LOG.error("Unable to close Statement or ResultSet",sxe);
            }
            try{
                if (readcon != null) {
                    DBPool.closeReaderSilent(ctx, readcon);
                }
            } catch (final Exception ex){
                LOG.error("Unable to return Connection",ex);
            }
        }

        try{
            writecon = DBPool.pickupWriteable(ctx);

            if (oclPerm.canDeleteAllObjects()) {
                Contacts.deleteContact(oid, ctx.getContextId(), writecon);
            } else {
                if (oclPerm.canDeleteOwnObjects() && created_from == userId){
                    Contacts.deleteContact(oid, ctx.getContextId(), writecon);
                }else{
                    throw EXCEPTIONS.createOXConflictException(46,Integer.valueOf(fuid), Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                    //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECTS (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')');
                }
            }
            final EventClient ec = new EventClient(session);
            ec.delete(co);
        } catch (final EventException ise){
            throw EXCEPTIONS.create(56,ise);
        } catch (final ContextException ise){
            throw EXCEPTIONS.create(56,ise);
        } catch (final DBPoolingException xe){
            throw EXCEPTIONS.create(45,xe);
        } catch (final OXException e){
            throw e;
            //throw new OXConflictException("NOT ALLOWED TO DELETE FOLDER OBJECT (cid="+sessionobject.getContext().getContextId()+" fid="+fuid+" oid="+oid+')', e);
        } finally {
            if (writecon != null) {
                DBPool.closeWriterSilent(ctx, writecon);
            }
        }
    }

    @OXThrowsMultiple(
            category={     Category.SOCKET_CONNECTION,
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR,
                                    Category.TRY_AGAIN
                                },
            desc={"47","48","49","59"},
            exceptionId={47,48,49,59},
            msg={    ContactException.INIT_CONNECTION_FROM_DBPOOL,
                            ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
                            ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
                            "The contact you requested is not valid."
                        }
    )
    public SearchIterator<ContactObject> getObjectsById(final int[][] object_id, final int[] cols) throws OXException {
        final int[] myCols = checkColumns(cols);
        try {
            final List<ContactObject> retval = new ArrayList<ContactObject>(object_id.length);

            int remain = object_id.length;
            int offset = 0;
            final int blockSize = 10;
            while (remain > blockSize) {
                /*
                 * Copy block
                 */
                final int[][] block_object_id = new int[blockSize][];
                System.arraycopy(object_id, offset, block_object_id, 0, block_object_id.length);
                /*
                 * Add contacts
                 */
                addQueriedContacts(myCols, retval, block_object_id);
                remain -= blockSize;
                offset += blockSize;
            }
            if (remain > 0) {
                final int[][] block_object_id = new int[remain][];
                System.arraycopy(object_id, offset, block_object_id, 0, block_object_id.length);
                /*
                 * Add contacts
                 */
                addQueriedContacts(myCols, retval, block_object_id);
            }
            final int size = retval.size();
            if (object_id.length == 1 && size < object_id.length) {
                /*
                 * Throw error if single contact is requested
                 */
                throw EXCEPTIONS.createOXObjectNotFoundException(59);
            }
            return new SearchIteratorDelegator<ContactObject>(retval.iterator(), size);
        } catch (final SearchIteratorException e) {
            throw EXCEPTIONS.create(48, e, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
        } catch (final SQLException e) {
            throw EXCEPTIONS.create(49, e, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
        }
    }

    private int[] checkColumns(final int[] cols) {
        final List<Integer> tmp = new ArrayList<Integer>();
        for (final int col : cols) {
            if (Contacts.mapping[col] != null) {
                tmp.add(Integer.valueOf(col));
            } else if (ContactObject.IMAGE1_URL == col) {
                tmp.add(Integer.valueOf(col));
                final Integer imageId = Integer.valueOf(ContactObject.IMAGE1);
                if (!Arrays.contains(cols, ContactObject.IMAGE1) || !tmp.contains(imageId)) {
                    tmp.add(imageId);
                }
            } else {
                LOG.warn("UNKNOWN FIELD -> " + col);
            }
        }
        final int[] retval = new int[tmp.size()];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = tmp.get(i).intValue();
        }
        return retval;
    }

    private int addQueriedContacts(final int[] cols, final List<ContactObject> retval, final int[][] object_id)
            throws SQLException, SearchIteratorException, OXException {
        final Connection readcon;
        try {
            readcon = DBPool.pickup(ctx);
        } catch (final DBPoolingException e) {
            throw new ContactException(e);
        }
        boolean closeCon = true;
        try {
            /*
             * Create new contact SQL
             */
            final ContactSql contactSQL = new ContactMySql(session, ctx);
            contactSQL.setSelect(contactSQL.iFgetColsString(cols).toString());
            contactSQL.setObjectArray(object_id);
            /*
             * Necessary resources
             */
            PreparedStatement ps = null;
            ResultSet res = null;
            boolean closeStuff = true;
            SearchIterator<ContactObject> searchIterator = null;
            try {
                ps = contactSQL.getSqlStatement(readcon);
                res = ps.executeQuery();
                searchIterator = new ContactObjectIterator(res, ps, cols, true, readcon);
                if (searchIterator.hasSize()) {
                    final int size = searchIterator.size();
                    for (int i = 0; i < size; i++) {
                        retval.add(searchIterator.next());
                    }
                    return size;
                }
                int count = 0;
                while (searchIterator.hasNext()) {
                    retval.add(searchIterator.next());
                    count++;
                }
                return count;
            } finally {
                if (searchIterator != null) {
                    try {
                        searchIterator.close();
                        closeCon = false;
                        closeStuff = false;
                    } catch (final SearchIteratorException e) {
                        LOG.error("Unable to close search iterator", e);
                    }
                }
                if (closeStuff) {
                    DBUtils.closeSQLStuff(res, ps);
                }
            }
        } finally {
            if (closeCon) {
                DBPool.closeReaderSilent(ctx, readcon);
            }
        }
    }

    public static boolean performSecurityReadCheck(final int fid, final int created_from, final int user, final int[] group, final Session so, final Connection readcon, final Context ctx) {
        return Contacts.performContactReadCheck(fid, created_from, user, group, ctx,
                UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx),
                readcon);
    }

    @OXThrowsMultiple(
            category={
                                Category.CODE_ERROR,
                                Category.CODE_ERROR
                                },
            desc={"50","51"},
            exceptionId={50,51},
            msg={    ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
                            ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D
                        }
    )
    protected ContactObject convertResultSet2ContactObject(final ResultSet rs, final int cols[], final boolean check, final Connection readCon) throws OXException {
        final ContactObject co = new ContactObject();

        try {
            co.setContextId(rs.getInt(2));
            co.setCreatedBy(rs.getInt(3));

            final long xx = rs.getLong((4));
            Date mi = new java.util.Date(xx);
            co.setCreationDate(mi);

            co.setModifiedBy(rs.getInt(5));

            final long xx2 = rs.getLong((6));
            mi = new java.util.Date(xx2);
            co.setLastModified(mi);

            co.setObjectID(rs.getInt(7));
            /*
             * Start at row count 8 to pass prefixed fields
             */
            int cnt = 8;
            for (int a = 0; a < cols.length; a++) {
                final mapper m = Contacts.mapping[cols[a]];
                if (m != null) {
                    m.addToContactObject(rs, cnt, co, readCon, userId, memberInGroups, ctx, userConfiguration);
                    cnt++;
                }
            }

            if (!co.containsInternalUserId()){
                co.setParentFolderID(rs.getInt(1));
                if (check && !performSecurityReadCheck(co.getParentFolderID(), co.getCreatedBy(),userId,memberInGroups,session,readCon, ctx)){
                    throw EXCEPTIONS.createOXConflictException(50,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                }
            }
        } catch (final SQLException e) {
            throw EXCEPTIONS.create(51,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
        } catch (final OXException e) {
            throw e;
        }

        return co;
    }
    @OXThrowsMultiple(
                category={
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR
                                    },
                desc={"52","53"},
                exceptionId={52,53},
                msg={    ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D,
                                ERR_UNABLE_TO_LOAD_OBJECTS_CONTEXT_1$D_USER_2$D
                            }
        )
    private class ContactObjectIterator implements SearchIterator<ContactObject> {

        private ContactObject nexto;
        private ContactObject pre;

        private final ResultSet rs;
        private final Statement stmt;
        private final Connection readcon;
        private final int[] cols;
        private boolean first = true;
        private final boolean securecheck;
        private final List<AbstractOXException> warnings;



        public ContactObjectIterator(final ResultSet rs,final Statement stmt, final int[] cols, final boolean securecheck, final Connection readcon) throws SearchIteratorException {
            this.warnings =  new ArrayList<AbstractOXException>(2);
            this.rs = rs;
            this.stmt = stmt;
            this.cols = cols;
            this.readcon = readcon;
            this.securecheck = securecheck;

            try {
                if (rs.next()) {
                    if (securecheck){
                        nexto = convertResultSet2ContactObject(rs, cols, true,  readcon);
                    }else{
                        nexto = convertResultSet2ContactObject(rs, cols, false,  readcon);
                    }
                }
            } catch (final SQLException exc) {
                throw EXCEPTIONS.createSearchIteratorException(52,exc, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
            } catch (final OXException exc) {
                throw EXCEPTIONS.createSearchIteratorException(53,exc, Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
            }
        }

        @OXThrowsMultiple(
                category={
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR
                                    },
                desc={"54","55"},
                exceptionId={54,55},
                msg={    "Unable to close Statement Handling. Context %1$d User %2$d",
                                "Unable to close Statement Handling. Context %1$d User %2$d"
                            }
        )
        public void close() throws SearchIteratorException {
            try{
                rs.close();
            }catch (final SQLException e){
                throw EXCEPTIONS.createSearchIteratorException(54,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new SearchIteratorException("UNABLE TO CLOSE SEARCHITERATOR RESULTSET! (cid="+sessionobject.getContext().getContextId()+')',e);
            }
            try{
                stmt.close();
            }catch (final SQLException e){
                throw EXCEPTIONS.createSearchIteratorException(55,e,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                //throw new SearchIteratorException("UNABLE TO CLOSE SEARCHITERATOR STATEMENT! (cid="+sessionobject.getContext().getContextId()+')',e);
            }
            if (readcon != null) {
                DBPool.closeReaderSilent(ctx, readcon);
            }
        }

        public boolean hasNext() {
            if (!first){
                nexto = pre;
            }
            return nexto != null;
        }

        @OXThrowsMultiple(
                category={
                                    Category.CODE_ERROR,
                                    Category.CODE_ERROR
                                    },
                desc={"56","57"},
                exceptionId={56,57},
                msg={    "Unable to get next Object. Context %1$d User %2$d",
                                "Unable to get next Object. Context %1$d User %2$d"
                            }
        )
        public ContactObject next() throws OXException, SearchIteratorException {
            try {
                if (rs.next()) {
                    try{
                        if (securecheck){
                            pre = convertResultSet2ContactObject(rs, cols, true,  readcon);
                        }else{
                            pre = convertResultSet2ContactObject(rs, cols, false,  readcon);
                        }
                    }catch (final OXException e){
                        throw EXCEPTIONS.create(56,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
                        //throw new OXException("ERROR DURING RIGHTS CHECK IN SEARCHITERATOR NEXT (cid="+sessionobject.getContext().getContextId()+')', e);
                    }
                } else {
                    pre = null;
                }
                if (first) {
                    first = false;
                }

                return nexto;
            } catch (final SQLException exc) {
                throw EXCEPTIONS.create(57,exc,Integer.valueOf(ctx.getContextId()), Integer.valueOf(userId));
            } catch (final OXException exc) {
                throw exc;
                //throw new SearchIteratorException("ERROR OCCURRED ON NEXT (cid="+sessionobject.getContext().getContextId()+')',exc);
            }
        }

        public int size() {
            throw new UnsupportedOperationException("Mehtod size() not implemented");
        }

        public boolean hasSize() {
            return false;
        }

        public void addWarning(final AbstractOXException warning) {
            warnings.add(warning);
        }

        public AbstractOXException[] getWarnings() {
            return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    public int getFolderId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public LdapServer getLdapServer() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSession(final Session s) {
        // TODO Auto-generated method stub

    }

}
