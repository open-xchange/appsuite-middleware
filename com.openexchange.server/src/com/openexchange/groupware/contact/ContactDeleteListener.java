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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ContactDeleteListener} - The delete listener for contact module
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ContactDeleteListener implements DeleteListener {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactDeleteListener.class);

    /**
     * Initializes a new {@link ContactDeleteListener}
     */
    public ContactDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon) throws OXException {
        if (deleteEvent.getType() == DeleteEvent.TYPE_USER) {

            Integer destUser = deleteEvent.getDestinationUserID();
            if(destUser == null){
                destUser = deleteEvent.getContext().getMailadmin();
            }
            /*
             * Drop affected distribution list entries
             */
            dropDListEntries("prg_dlist", "prg_contacts", deleteEvent.getId(), deleteEvent.getContext().getContextId(), writeCon);
            dropDListEntries("del_dlist", "del_contacts", deleteEvent.getId(), deleteEvent.getContext().getContextId(), writeCon);
            /*
             * Proceed
             */
            trashAllUserContacts(deleteEvent.getContext(), deleteEvent.getId(), deleteEvent.getSession(), destUser, readCon, writeCon);
        }
    }

    private static void dropDListEntries(final String dlistTable, final String contactTable, final int userId, final int contextId, final Connection writeCon) throws OXException {
        String sql = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Get those distribution lists which carry the user as an entry
             */
            sql =
                "SELECT d.intfield01, d.intfield02 FROM " + dlistTable + " AS d JOIN " + contactTable + " AS c ON d.cid = ? AND c.cid = ? AND d.intfield02 = c.intfield01 WHERE c.userId IS NOT NULL AND c.userId = ?";
            stmt = writeCon.prepareStatement(sql);
            stmt.setInt(1, contextId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            rs = stmt.executeQuery();
            final List<int[]> l = new ArrayList<int[]>();
            while (rs.next()) {
                l.add(new int[] { rs.getInt(1), rs.getInt(2) }); // distribution-list-id, contact-id
            }
            DBUtils.closeSQLStuff(rs, stmt);
            /*
             * Delete the entries which refer to the user which should be deleted
             */
            sql = "DELETE FROM " + dlistTable + " WHERE cid = ? AND intfield01 = ? AND intfield02 = ?";
            stmt = writeCon.prepareStatement(sql);
            for (final int[] arr : l) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, arr[0]);
                stmt.setInt(3, arr[1]);
                stmt.addBatch();
            }
            stmt.executeBatch();
            DBUtils.closeSQLStuff(rs, stmt);
            /*
             * Check if any distribution list has no entry after deleting user's entries
             */
            final TIntList toDelete = new TIntArrayList();
            sql = "SELECT COUNT(intfield02) FROM " + dlistTable + " WHERE cid = ? AND intfield01 = ?";
            for (final int[] arr : l) {
                final int dlistId = arr[0];
                stmt = writeCon.prepareStatement(sql);
                stmt.setInt(1, contextId);
                stmt.setInt(2, dlistId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    final int count = rs.getInt(1);
                    if (0 == count) {
                        /*
                         * The distribution list is now empty
                         */
                        toDelete.add(dlistId);
                    }
                }
                DBUtils.closeSQLStuff(rs, stmt);
            }
            /*
             * Delete empty distribution lists
             */
            if (!toDelete.isEmpty()) {
                sql = "DELETE FROM " + contactTable + " WHERE cid = ? AND intfield01 = ?";
                stmt = writeCon.prepareStatement(sql);
                for (final int id : toDelete.toArray()) {
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                DBUtils.closeSQLStuff(rs, stmt);
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static void trashAllUserContacts(Context context, int userID, Session session, final int destUser, Connection readConnection, Connection writeConnection) throws OXException {
        /*
         * get all contacts created by the user being deleted
         */
        List<Contact> contacts = getContactsCreatedBy(readConnection, context.getContextId(), userID);
        if (null == contacts || 0 == contacts.size()) {
            return; // nothing to do
        }
        /*
         * process contacts by their parent folder
         */
        Map<Integer, FolderObject> parentFolders = getParentFolders(contacts, context, readConnection);
        List<Contact> toUser = new ArrayList<Contact>();
        List<Contact> toUsersFolder = new ArrayList<Contact>();
        List<Contact> toDelete = new ArrayList<Contact>();
        for (Map.Entry<FolderObject, List<Contact>> entry : mapToParentFolders(contacts, parentFolders).entrySet()) {
            orderContacts(entry.getKey(), entry.getValue(), userID, toUser, toUsersFolder, toDelete);
        }
        if (0 < toUsersFolder.size()) {
            if (userID == context.getMailadmin()) {
                toDelete.addAll(toUsersFolder);
            }
            /*
             * try to move stale contacts to destUser's default folder
             */
            try {
                int moved = moveToUsersFolder(writeConnection, context, toUsersFolder, destUser);
                LOG.debug("Moved {} entries originally owned by user {} in context {} to default folder of user {}.",
                    I(moved), I(userID), I(context.getContextId()), I(destUser));
            } catch (OXException e) {
                LOG.error("Error moving stale contacts to user's default folder, deleting affected contacts.", e);
                toDelete.addAll(toUsersFolder);
            }
        }
        if (0 < toUser.size()) {
            /*
             * transfer ownership to user
             */
            int updated = reassignContacts(writeConnection, "prg_contacts", context.getContextId(), userID, destUser);
            LOG.debug("Reassigned {} entries in 'prg_contacts' table to user {} originally owned by user {} in context {}.",
                I(updated), I(destUser), I(userID), I(context.getContextId()));
        }
        if (0 < toDelete.size()) {
            /*
             * delete contacts
             */
            int deleted = deleteContacts(writeConnection, context.getContextId(), toDelete);
            LOG.debug("Deleted {} entries from 'prg_contacts' table originally owned by user {} in context {}.",
                I(deleted), I(userID), I(context.getContextId()));
            /*
             * trigger delete events
             */
            EventClient eventClient = new EventClient(session);
            for (Contact contact : toDelete) {
                try {
                    FolderObject parentFolder = parentFolders.get(contact.getParentFolderID());
                    if (null != parentFolder) {
                        eventClient.delete(contact, parentFolder);
                    } else {
                        eventClient.delete(contact);
                    }
                } catch (Exception e) {
                    LOG.error("Error triggering delete event for contact delete: id={} cid={}",
                        I(contact.getObjectID()), I(contact.getContextId()), e);
                }
            }
        }
        /*
         * cleanup any leftovers in the del_contacts table
         */
        if (userID == context.getMailadmin()) {
            int deleted = deleteFromDelContacts(writeConnection, context.getContextId(), userID);
            LOG.debug("Deleted {} entries from 'del_contacts' table originally owned by user {} in context {}.",
                I(deleted), I(userID), I(context.getContextId()));
        } else {
            int updated = reassignContacts(writeConnection, "del_contacts", context.getContextId(), userID, destUser);
            LOG.debug("Reassigned {} entries in 'del_contacts' table to user {} originally owned by user {} in context {}.",
                I(updated), I(destUser), I(userID), I(context.getContextId()));
        }
    }

    /**
     * Decides for contacts in a folder that were created by the deleted user what to do and orders the contact into one of the supplied
     * lists.
     *
     * @param folder The parent folder of the contacts
     * @param contacts The contacts to order
     * @param userID The ID of the deleted user
     * @param toUser The list to contain contacts where ownership should be transferred to the user
     * @param toUsersFolder The list to contain contacts that should be moved to the user's default folder
     * @param toDelete The list to contain contacts that should be deleted
     * @throws OXException
     */
    private static void orderContacts(FolderObject folder, List<Contact> contacts, int userID, List<Contact> toUser, List<Contact> toUsersFolder, List<Contact> toDelete) throws OXException {
        for (Contact contact : contacts) {
            if (contact.getPrivateFlag()) {
                LOG.debug("Contact marked as 'private' will be deleted [Context={} Folder={} User={} Contact={}].",
                    I(contact.getContextId()), I(contact.getParentFolderID()), I(userID), I(contact.getObjectID()));
                toDelete.add(contact);
            } else if (null == folder) {
                LOG.warn("Contact with no valid parent folder will be moved to user's address book. " +
                    "[Context={} Folder={} User={} Contact={}]",
                    I(contact.getContextId()), I(contact.getParentFolderID()), I(userID), I(contact.getObjectID()));
                toUsersFolder.add(contact);
            } else if (FolderObject.CONTACT != folder.getModule()) {
                throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(
                    I(folder.getObjectID()), I(contact.getContextId()), I(userID));
            } else if (FolderObject.PRIVATE == folder.getType(userID)) {
                LOG.debug("Contact in 'private' folder will be deleted [Context={} Folder={} User={} Contact={}].",
                    I(contact.getContextId()), I(contact.getParentFolderID()), I(userID), I(contact.getObjectID()));
                toDelete.add(contact);
            } else {
                LOG.debug("Contact in non-'private' folder will be transferred to user " +
                    "[Context={} Folder={} User={} Contact={}].",
                    I(contact.getContextId()), I(contact.getParentFolderID()), I(userID), I(contact.getObjectID()));
                toUser.add(contact);
            }
        }
    }

    /**
     * Maps the supplied list of contacts to their parent folders, or to the <code>null</code>-key if no parent folder could be retrieved.
     *
     * @param contacts The contacts to map
     * @param knownFolders The parent folders
     * @return The contacts mapped to their parent folders
     */
    private static java.util.Map<FolderObject, List<Contact>> mapToParentFolders(List<Contact> contacts, Map<Integer, FolderObject> knownFolders) {
        Map<FolderObject, List<Contact>> contactsByFolder = new HashMap<FolderObject, List<Contact>>();
        for (Contact contact : contacts) {
            Integer folderID = Integer.valueOf(contact.getParentFolderID());
            FolderObject folder = knownFolders.get(folderID);
            List<Contact> contactsInFolder = contactsByFolder.get(folder);
            if (null == contactsInFolder) {
                contactsInFolder = new ArrayList<Contact>();
                contactsByFolder.put(folder, contactsInFolder);
            }
            contactsInFolder.add(contact);
        }
        return contactsByFolder;
    }

    /**
     * Gets all parent folders of the supplied contacts, mapped to the folder identifiers. If no folder could be retrieved, the identifier
     * will be mapped to <code>null</code>.
     *
     * @param contacts The contacts to map
     * @param context The context
     * @param readConnection A database connection
     * @return The contacts mapped to their parent folders
     */
    private static java.util.Map<Integer, FolderObject> getParentFolders(List<Contact> contacts, Context context, Connection readConnection) {
        Map<Integer, FolderObject> knownFolders = new HashMap<Integer, FolderObject>();
        for (Contact contact : contacts) {
            Integer folderID = Integer.valueOf(contact.getParentFolderID());
            if (false == knownFolders.containsKey(folderID)) {
                knownFolders.put(folderID, getFolder(context, readConnection, folderID));
            }
        }
        return knownFolders;
    }

    /**
     * Gets all contacts that were created from a specific user.
     *
     * @param connection The database connection to use
     * @param contextID The context ID
     * @param createdBy The user ID
     * @return The contacts
     */
    private static List<Contact> getContactsCreatedBy(Connection connection, int contextID, int createdBy) throws OXException {
        String sql =
            "SELECT intfield01,fid,pflag " +
            "FROM prg_contacts " +
            "WHERE cid=? AND created_from=?;"
        ;
        List<Contact> contacts = new ArrayList<Contact>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, contextID);
            stmt.setInt(2, createdBy);
            result = stmt.executeQuery();
            while (result.next()) {
                Contact contact = new Contact();
                contact.setContextId(contextID);
                contact.setCreatedBy(createdBy);
                contact.setObjectID(result.getInt(1));
                contact.setParentFolderID(result.getInt(2));
                int pflag = result.getInt(3);
                if (result.wasNull()) {
                    pflag = 0;
                }
                contact.setPrivateFlag(0 != pflag);
                contacts.add(contact);
            }
            return contacts;
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
        }
    }

    /**
     * Deletes all entries from the 'del_contacts' table that were created by a specific user.
     *
     * @param writeConnection The connection to use
     * @param contextID The context ID
     * @param createdBy The matching created by ID
     * @return The number of deleted rows
     * @throws OXException
     */
    private static int deleteFromDelContacts(Connection writeConnection, int contextID, int createdBy) throws OXException {
        String sql =
            "DELETE FROM del_contacts " +
            "WHERE cid=? AND created_from=?;"
        ;
        PreparedStatement stmt = null;
        try {
            stmt = writeConnection.prepareStatement(sql);
            stmt.setInt(1, contextID);
            stmt.setInt(2, createdBy);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * Updates the "created by" and "modified by" information of all entries from a contacts table that were created by a specific user.
     * The last modification timestamp is updated as well.
     *
     * @param writeConnection The connection to use
     * @param table The table name
     * @param contextID The context ID
     * @param oldCreatedBy The matching created by ID
     * @param newCreatedBy The new created by ID
     * @return The number of updated rows
     * @throws OXException
     */
    private static int reassignContacts(Connection writeConnection, String table, int contextID, int oldCreatedBy, int newCreatedBy) throws OXException {
        String sql =
            "UPDATE " + table + ' ' +
            "SET created_from=?,changed_from=?,changing_date=? " +
            "WHERE cid=? AND created_from=?;"
        ;
        PreparedStatement stmt = null;
        try {
            stmt = writeConnection.prepareStatement(sql);
            stmt.setInt(1, newCreatedBy);
            stmt.setInt(2, newCreatedBy);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setInt(4, contextID);
            stmt.setInt(5, oldCreatedBy);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * Moves the supplied contacts to the default contacts folder of the given user. The "created by" and "modified by" information,
     * as well as the last modification timestamp is updated accordingly.
     *
     * @param writeConnection The connection to use
     * @param context The context
     * @param contacts The contacts to move
     * @param newCreatedBy The new created by ID
     * @param destUserId The id of the user
     * @return The number of updated rows
     * @throws OXException
     */
    private static int moveToUsersFolder(Connection writeConnection, Context context, List<Contact> contacts, int destUser) throws OXException {
        int targetFolderID = new OXFolderAccess(writeConnection, context).getDefaultFolderID(destUser, FolderObject.CONTACT);
        StringBuilder stringBuilder = new StringBuilder()
            .append("UPDATE prg_contacts ")
            .append("SET fid=?,created_from=?,changed_from=?,changing_date=? ")
            .append("WHERE cid=? AND intfield01")
        ;
        if (1 == contacts.size()) {
            stringBuilder.append("=?;");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < contacts.size(); i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(");");
        }
        String sql = stringBuilder.toString();
        PreparedStatement stmt = null;
        try {
            stmt = writeConnection.prepareStatement(sql);
            stmt.setInt(1, targetFolderID);
            stmt.setInt(2, destUser);
            stmt.setInt(3, destUser);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setInt(5, context.getContextId());
            for (int i = 0; i < contacts.size(); i++) {
                stmt.setInt(i + 6, contacts.get(i).getObjectID());
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * Deletes the supplied contacts. This includes all affected entries in tables 'prg_dlist', 'prg_contacts_linkage',
     * 'prg_contacts_image' and 'prg_contacts'.
     *
     * @param writeConnection The connection to use
     * @param contextID The context ID
     * @param contacts The contacts to delete
     * @param newCreatedBy The new created by ID
     * @return The number of updated rows
     * @throws OXException
     */
    private static int deleteContacts(Connection writeConnection, int contextID, List<Contact> contacts) throws OXException {
        StringBuilder stringBuilder = new StringBuilder();
        if (1 == contacts.size()) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < contacts.size(); i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        String inClause = stringBuilder.toString();
        try {
            PreparedStatement stmt = null;
            /*
             * prg_dlist
             */
            try {
                stmt = writeConnection.prepareStatement("DELETE FROM prg_dlist WHERE cid=? AND intfield01" + inClause + ';');
                stmt.setInt(1, contextID);
                for (int i = 0; i < contacts.size(); i++) {
                    stmt.setInt(i + 2, contacts.get(i).getObjectID());
                }
                stmt.executeUpdate();
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * prg_contacts_linkage (obsoloete?)
             */
            try {
                stmt = writeConnection.prepareStatement(
                    "DELETE FROM prg_contacts_linkage WHERE cid=? AND (intfield01" + inClause + " OR intfield02" + inClause + ");");
                stmt.setInt(1, contextID);
                for (int i = 0; i < contacts.size(); i++) {
                    stmt.setInt(i + 2, contacts.get(i).getObjectID());
                    stmt.setInt(i + 2 + contacts.size(), contacts.get(i).getObjectID());
                }
                stmt.executeUpdate();
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * prg_contacts_image
             */
            try {
                stmt = writeConnection.prepareStatement("DELETE FROM prg_contacts_image WHERE cid=? AND intfield01" + inClause + ';');
                stmt.setInt(1, contextID);
                for (int i = 0; i < contacts.size(); i++) {
                    stmt.setInt(i + 2, contacts.get(i).getObjectID());
                }
                stmt.executeUpdate();
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * prg_contacts
             */
            try {
                stmt = writeConnection.prepareStatement("DELETE FROM prg_contacts WHERE cid=? AND intfield01" + inClause + ';');
                stmt.setInt(1, contextID);
                for (int i = 0; i < contacts.size(); i++) {
                    stmt.setInt(i + 2, contacts.get(i).getObjectID());
                }
                return stmt.executeUpdate();
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        } catch (SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

    /**
     * Gets a folder by it's identifier.
     *
     * @param context The context
     * @param readConnection A database connection
     * @param folderID The ID of the folder
     * @return The folder, or <code>null</code> if the folder could not be loaded
     */
    private static FolderObject getFolder(Context context, Connection readConnection, int folderID) {
        try {
            if (FolderCacheManager.isEnabled()) {
                return FolderCacheManager.getInstance().getFolderObject(folderID, true, context, readConnection);
            } else {
                return FolderObject.loadFolderObjectFromDB(folderID, context, readConnection);
            }
        } catch (OXException e) {
            LOG.warn("No folder found for id {} in context {}.", Integer.valueOf(folderID), Integer.valueOf(context.getContextId()));
            return null;
        }
    }

}
