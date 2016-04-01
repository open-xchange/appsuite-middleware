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

package com.openexchange.consistency;

import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.consistency.osgi.ConsistencyServiceLookup;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorage2EntitiesResolver;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;
import edu.emory.mathcs.backport.java.util.Collections;
import com.openexchange.filestore.FileStorageCodes;

/**
 * Provides the integration of the consistency tool in the OSGi OX.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
 */
public class OsgiOXConsistency extends Consistency {

    private DatabaseImpl database;

    /**
     * Initialises a new {@link OsgiOXConsistency}.
     */
    public OsgiOXConsistency() {
        super();
    }

    @Override
    protected Context getContext(final int contextId) throws OXException {
        final ContextStorage ctxstor = ContextStorage.getInstance();
        return ctxstor.getContext(contextId);
    }

    @Override
    protected DatabaseImpl getDatabase() {
        if (database == null) {
            database = new DatabaseImpl(new DBPoolProvider());
        }
        return database;
    }

    @Override
    protected AttachmentBase getAttachments() {
        return Attachments.getInstance();
    }

    @Override
    protected FileStorage getFileStorage(Entity entity) throws OXException {
        switch (entity.getType()) {
            case Context:
                return getFileStorage(entity.getContext());
            case User:
                return getFileStorage(entity.getContext(), entity.getUser());
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entity.getType());
        }
    }

    @Override
    protected FileStorage getFileStorage(final Context ctx) throws OXException {
        FileStorage2EntitiesResolver resolver = FileStorages.getFileStorage2EntitiesResolver();
        return resolver.getFileStorageUsedBy(ctx.getContextId(), true);
    }

    @Override
    protected FileStorage getFileStorage(Context context, User user) throws OXException {
        FileStorage2EntitiesResolver resolver = FileStorages.getFileStorage2EntitiesResolver();
        return resolver.getFileStorageUsedBy(context.getContextId(), user.getId(), true);
    }

    @Override
    protected List<Context> getContextsForFilestore(final int filestoreId) throws OXException {
        // Check existence
        {
            DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);
            Connection connection = dbService.getReadOnly();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.prepareStatement("SELECT 1 FROM filestore WHERE id=?");
                stmt.setInt(1, filestoreId);
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    throw FileStorageCodes.NO_SUCH_FILE_STORAGE.create(filestoreId);
                }
            } catch (SQLException e) {
                throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                dbService.backReadOnly(connection);
            }
        }

        int[] ids = FileStorages.getFileStorage2EntitiesResolver().getIdsOfContextsUsing(filestoreId);
        return loadContexts(ids);
    }

    @Override
    protected List<Entity> getEntitiesForFilestore(int filestoreId) throws OXException {
        // Get all contexts that use the specified filestore
        List<Context> ctxs = getContextsForFilestore(filestoreId);

        // Get all users that use the specified filestore
        Map<Context, List<User>> users = getUsersForFilestore(filestoreId);

        // Convert to entities
        List<Entity> entities = new ArrayList<Entity>(ctxs.size() + users.size());
        for (Context ctx : ctxs) {
            entities.add(new EntityImpl(ctx));
        }
        for (Entry<Context, List<User>> ctxEntry : users.entrySet()) {
            for (User user : ctxEntry.getValue()) {
                entities.add(new EntityImpl(ctxEntry.getKey(), user));
            }
        }

        return entities;
    }

    @Override
    protected List<Context> getContextsForDatabase(final int databaseId) throws OXException {
        final DatabaseService configDB = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);

        // Check existence
        {
            Connection connection = configDB.getReadOnly();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.prepareStatement("SELECT 1 FROM db_pool WHERE db_pool_id=?");
                stmt.setInt(1, databaseId);
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    throw DBPoolingExceptionCodes.NO_DBPOOL.create(databaseId);
                }
            } catch (SQLException e) {
                throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                configDB.backReadOnly(connection);
            }
        }

        final int[] contextIds = configDB.listContexts(databaseId);

        return loadContexts(contextIds);
    }

    @Override
    protected List<Context> getAllContexts() throws OXException {
        final ContextStorage ctxstor = ContextStorage.getInstance();
        final List<Integer> list = ctxstor.getAllContextIds();

        return loadContexts(list);
    }

    @Override
    protected User getAdmin(final Context ctx) throws OXException {
        return UserStorage.getInstance().getUser(ctx.getMailadmin(), ctx);
    }

    @Override
    protected SortedSet<String> getSnippetFileStoreLocationsPerContext(Context ctx) throws OXException {
        final SortedSet<String> retval = new TreeSet<String>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabaseService databaseService = ConsistencyServiceLookup.getService(DatabaseService.class, true);
        try {
            con = databaseService.getReadOnly(ctx);
            if (DBUtils.tableExists(con, "snippet")) {
                stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND refType=1");
                stmt.setInt(1, ctx.getContextId());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    retval.add(rs.getString(1));
                }
                DBUtils.closeSQLStuff(rs, stmt);
                stmt = null;
            }
        } catch (final SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            if (null != con) {
                databaseService.backReadOnly(ctx, con);
            }
        }
        return retval;
    }

    @Override
    protected SortedSet<String> getVCardFileStoreLocationsPerContext(Context ctx) throws OXException {
        VCardStorageMetadataStore vCardStorageMetadataStore = ConsistencyServiceLookup.getOptionalService(VCardStorageMetadataStore.class);
        if (vCardStorageMetadataStore != null) {
            Set<String> loadRefIds = vCardStorageMetadataStore.loadRefIds(ctx.getContextId());
            return new TreeSet<String>(loadRefIds);
        }
        return new TreeSet<String>();
    }

    @Override
    protected SortedSet<String> getPreviewCacheFileStoreLocationsPerContext(Context ctx) throws OXException {
        ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
        Set<String> refIds = metadataStore.loadRefIds(ctx.getContextId());
        return new TreeSet<String>(refIds);
    }

    /**
     * Returns a list with {@link Context} objects loaded from the {@link ContextStorage} using the specified context identifiers
     *
     * @param list a list with context identifiers
     * @return a list with {@link Context} objects loaded from the {@link ContextStorage}
     * @throws OXException
     */
    private List<Context> loadContexts(List<Integer> list) throws OXException {
        ContextStorage ctxstor = ContextStorage.getInstance();
        List<Context> contexts = new ArrayList<Context>(list.size());
        for (int id : list) {
            contexts.add(ctxstor.getContext(id));
        }
        return contexts;
    }

    /**
     * Returns a list with {@link Context} objects loaded from the {@link ContextStorage} using the specified context identifiers
     *
     * @param list a list with context identifiers
     * @return a list with {@link Context} objects loaded from the {@link ContextStorage}
     * @throws OXException
     */
    private List<Context> loadContexts(int[] list) throws OXException {
        ContextStorage ctxstor = ContextStorage.getInstance();
        List<Context> contexts = new ArrayList<Context>(list.length);
        for (int id : list) {
            contexts.add(ctxstor.getContext(id));
        }
        return contexts;
    }

    /**
     * Returns a map with {@link Context} and {@link User} objects loaded from the {@link ContextStorage} and {@link UserStorage} using the specified context and user identifiers
     *
     * @param list a list with context and user identifiers
     * @return a list with {@link Context} and {@link User} objects loaded from the {@link ContextStorage} and {@link UserStorage}
     * @throws OXException
     */
    private Map<Context, List<User>> loadUsers(Map<Integer, List<Integer>> users) throws OXException {
        ContextStorage ctxStor = ContextStorage.getInstance();
        UserStorage usrStor = UserStorage.getInstance();

        Map<Context, List<User>> usr = new HashMap<Context, List<User>>();
        for (Entry<Integer, List<Integer>> ctxIdEntry : users.entrySet()) {
            Context context = ctxStor.getContext(ctxIdEntry.getKey().intValue());
            User[] usrArray = usrStor.getUser(context, toArray(ctxIdEntry.getValue()));
            List<User> usrList = new ArrayList<User>(usrArray.length);
            Collections.addAll(usrList, usrArray);
            usr.put(context, usrList);
        }

        return usr;
    }

    /**
     * Returns a map with {@link Context} and {@link User} objects that are using the file storage with the specified identifier
     *
     * @param filestoreId the file storage identifier
     * @return a map with {@link Context} and {@link User} objects that are using the file storage with the specified identifier
     * @throws OXException
     */
    private Map<Context, List<User>> getUsersForFilestore(final int filestoreId) throws OXException {
        Map<Integer, List<Integer>> users = FileStorages.getFileStorage2EntitiesResolver().getIdsOfUsersUsing(filestoreId);
        return loadUsers(users);
    }

    /**
     * Converts the specified list of integers to an array of integers
     *
     * @param list the list of integers
     * @return an array of integers
     */
    private int[] toArray(List<Integer> list) {
        int[] integers = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            integers[i] = list.get(i).intValue();
        }
        return integers;
    }

}
