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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.consistency.internal;

import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.config.ConfigurationService;
import com.openexchange.consistency.ConsistencyExceptionCodes;
import com.openexchange.consistency.ConsistencyService;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.Entity.EntityType;
import com.openexchange.consistency.EntityImpl;
import com.openexchange.consistency.RepairAction;
import com.openexchange.consistency.RepairPolicy;
import com.openexchange.consistency.internal.solver.DeleteBrokenPreviewReferencesSolver;
import com.openexchange.consistency.internal.solver.DoNothingSolver;
import com.openexchange.consistency.internal.solver.PolicyResolver;
import com.openexchange.consistency.internal.solver.ProblemSolver;
import com.openexchange.consistency.internal.solver.RecordSolver;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorage2EntitiesResolver;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.report.internal.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.snippet.QuotaAwareSnippetService;
import com.openexchange.tools.sql.DBUtils;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * {@link ConsistencyServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ConsistencyServiceImpl implements ConsistencyService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsistencyServiceImpl.class);

    private DatabaseImpl database;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link ConsistencyServiceImpl}.
     */
    public ConsistencyServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#checkOrRepairConfigDB(boolean)
     */
    @Override
    public List<String> checkOrRepairConfigDB(boolean repair) throws OXException {
        LOG.info("{} inconsistent configdb", repair ? "Repair" : "List");
        DatabaseService databaseService = null;

        Connection confCon = null;
        Connection poolCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> ret = new ArrayList<String>();

        Map<String, List<Integer>> schemaMap = new HashMap<String, List<Integer>>();
        try {
            databaseService = services.getServiceSafe(DatabaseService.class);

            Map<String, Integer> schemaPoolMap = Tools.getAllSchemata(LOG);
            confCon = databaseService.getReadOnly();

            // Fetch all contexts for each db_schema
            for (String schema : schemaPoolMap.keySet()) {
                stmt = confCon.prepareStatement("SELECT cid FROM context_server2db_pool WHERE db_schema = ?");
                stmt.setString(1, schema);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    Integer ctx = Integer.valueOf(rs.getInt(1));
                    if (schemaMap.containsKey(schema)) {
                        schemaMap.get(schema).add(ctx);
                    } else {
                        List<Integer> ctxs = new ArrayList<Integer>();
                        ctxs.add(ctx);
                        schemaMap.put(schema, ctxs);
                    }
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            }
            for (Entry<String, List<Integer>> schemaEntry : schemaMap.entrySet()) {
                String schema = schemaEntry.getKey();
                List<Integer> ctxs = schemaEntry.getValue();
                Integer poolid = schemaPoolMap.get(schema);
                try {
                    poolCon = databaseService.get(poolid.intValue(), schema);
                } catch (OXException e) {
                    Throwable cause = e.getCause();
                    if (cause.getMessage() != null && cause.getMessage().contains("Unknown database")) {
                        if (repair) {
                            deleteSchemaFromConfigDB(confCon, schema);
                            ret.add("Deleted inconsistent entry(ies) for schema " + schema + " from configdb.");
                        } else {
                            ret.add("The schema '" + schema + "' does not exist.'");
                        }
                        continue;
                    }
                    throw e;
                }
                String contextids = Strings.join(ctxs, ",");
                contextids = contextids.substring(0, contextids.length() - 1);
                // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
                stmt = poolCon.prepareStatement("SELECT cid FROM login2user WHERE cid IN (" + contextids + ") GROUP BY cid");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    Integer ctx = Integer.valueOf(rs.getInt(1));
                    ctxs.remove(ctx);
                }
                if (ctxs.size() > 0) {
                    LOG.info("Schema {} is broken", schema);
                    for (Integer ctx : ctxs) {
                        if (repair) {
                            LOG.info("Deleting inconsistent entry for context {} from configdb", ctx);
                            deleteContextFromConfigDB(confCon, ctx.intValue());
                            ret.add("Deleted inconsistent entry for context " + ctx + " from configdb");
                        } else {
                            LOG.info("Context {} does not exist anymore", ctx);
                            ret.add("Context " + ctx + " does not exist anymore");
                        }
                    }
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
                databaseService.back(poolid.intValue(), poolCon);
                poolCon = null;
            }
            if (ret.size() == 0 && repair) {
                ret.add("there was nothing to repair");
            }
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            return ret;
        } catch (SQLException e) {
            throw ConsistencyExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (databaseService != null) {
                if (null != confCon) {
                    databaseService.backReadOnly(confCon);
                }
                if (null != poolCon) {
                    databaseService.backReadOnly(poolCon);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listMissingFilesInContext(int)
     */
    @Override
    public List<String> listMissingFilesInContext(int contextId) throws OXException {
        LOG.info("Listing missing files in context {}", contextId);
        DoNothingSolver doNothing = new DoNothingSolver();
        RecordSolver recorder = new RecordSolver();
        Context ctx = getContext(contextId);
        checkOneEntity(new EntityImpl(ctx), recorder, recorder, recorder, recorder, doNothing, recorder, getDatabase(), getAttachments(), getFileStorage(ctx));
        return recorder.getProblems();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listMissingFilesInFilestore(int)
     */
    @Override
    public Map<Entity, List<String>> listMissingFilesInFilestore(int filestoreId) throws OXException {
        LOG.info("Listing missing files in filestore {}", filestoreId);
        return listMissing(getEntitiesForFilestore(filestoreId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listMissingFilesInDatabase(int)
     */
    @Override
    public Map<Entity, List<String>> listMissingFilesInDatabase(int databaseId) throws OXException {
        LOG.info("List missing files in database {}", databaseId);
        return listMissing(toEntities(getContextsForDatabase(databaseId)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listAllMissingFiles()
     */
    @Override
    public Map<Entity, List<String>> listAllMissingFiles() throws OXException {
        LOG.info("List all missing files");
        return listMissing(toEntities(getAllContexts()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listUnassignedFilesInContext(int)
     */
    @Override
    public List<String> listUnassignedFilesInContext(int contextId) throws OXException {
        LOG.info("List all unassigned files in context {}", contextId);
        DoNothingSolver doNothing = new DoNothingSolver();
        RecordSolver recorder = new RecordSolver();
        Context ctx = getContext(contextId);
        checkOneEntity(new EntityImpl(ctx), doNothing, doNothing, doNothing, doNothing, recorder, doNothing, getDatabase(), getAttachments(), getFileStorage(ctx));
        return recorder.getProblems();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listUnassignedFilesInFilestore(int)
     */
    @Override
    public Map<Entity, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws OXException {
        LOG.info("List all unassigned files in filestore {}", filestoreId);
        return listUnassigned(getEntitiesForFilestore(filestoreId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listUnassignedFilesInDatabase(int)
     */
    @Override
    public Map<Entity, List<String>> listUnassignedFilesInDatabase(int databaseId) throws OXException {
        LOG.info("List all unassigned files in database {}", databaseId);
        return listUnassigned(toEntities(getContextsForDatabase(databaseId)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#listAllUnassignedFiles()
     */
    @Override
    public Map<Entity, List<String>> listAllUnassignedFiles() throws OXException {
        LOG.info("List all unassigned files");
        return listUnassigned(toEntities(getAllContexts()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#repairFilesInContext(int, com.openexchange.consistency.RepairPolicy, com.openexchange.consistency.RepairAction)
     */
    @Override
    public void repairFilesInContext(int contextId, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException {
        LOG.info("Repair all files in context {} with repair policy {} and repair action {}", contextId, repairPolicy, repairAction);
        List<Context> repairMe = new ArrayList<Context>();
        repairMe.add(getContext(contextId));
        repair(toEntities(repairMe), repairPolicy, repairAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#repairFilesInFilestore(int, com.openexchange.consistency.RepairPolicy, com.openexchange.consistency.RepairAction)
     */
    @Override
    public void repairFilesInFilestore(int filestoreId, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException {
        LOG.info("Repair all files in filestore {} with repair policy {} and repair action {}", filestoreId, repairPolicy, repairAction);
        repair(getEntitiesForFilestore(filestoreId), repairPolicy, repairAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#repairFilesInDatabase(int, com.openexchange.consistency.RepairPolicy, com.openexchange.consistency.RepairAction)
     */
    @Override
    public void repairFilesInDatabase(int databaseId, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException {
        LOG.info("Repair all files in database {} with repair policy {} and repair action {}", databaseId, repairPolicy, repairAction);
        repair(toEntities(getContextsForDatabase(databaseId)), repairPolicy, repairAction);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.ConsistencyService#repairAllFiles(com.openexchange.consistency.RepairPolicy, com.openexchange.consistency.RepairAction)
     */
    @Override
    public void repairAllFiles(RepairPolicy repairPolicy, RepairAction repairAction) throws OXException {
        LOG.info("Repair all files with repair policy {} and repair action {}", repairPolicy, repairAction);
        repair(toEntities(getAllContexts()), repairPolicy, repairAction);
    }

    ////////////////////////////////////////// HELPERS ///////////////////////////////////////

    /**
     * Get the {@link Context} for the specified context identifier from the {@link ContextStore}
     *
     * @param contextId The context identifier
     * @return The {@link Context}
     * @throws OXException if the {@link Context} cannot be returned
     */
    private Context getContext(int contextId) throws OXException {
        ContextStorage ctxstor = ContextStorage.getInstance();
        return ctxstor.getContext(contextId);
    }

    /**
     * Get the DatabaseImpl
     *
     * @return the DatabaseImpl
     */
    private DatabaseImpl getDatabase() {
        if (database == null) {
            database = new DatabaseImpl(new DBPoolProvider());
        }
        return database;
    }

    /**
     * Gets the {@link AttachmentBase} instance
     *
     * @return the {@link AttachmentBase} instance
     */
    private AttachmentBase getAttachments() {
        return Attachments.getInstance();
    }

    /**
     * Gets the {@link FileStorage} for the specified {@link Entity}
     *
     * @param entity The {@link Entity} for which the {@link FileStorage} shall be returned
     * @return the {@link FileStorage} for the specified {@link Entity}
     * @throws OXException if the {@link FileStorage} cannot be returned
     */
    private FileStorage getFileStorage(Entity entity) throws OXException {
        switch (entity.getType()) {
            case Context:
                return getFileStorage(entity.getContext());
            case User:
                return getFileStorage(entity.getContext(), entity.getUser());
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entity.getType());
        }
    }

    /**
     * Gets the {@link FileStorage} for the specified {@link Context}
     *
     * @param ctx The {@link Context} for which the {@link FileStorage} shall be returned
     * @return the {@link FileStorage} for the specified {@link Context}
     * @throws OXException if the {@link FileStorage} cannot be returned
     */
    private FileStorage getFileStorage(Context ctx) throws OXException {
        FileStorage2EntitiesResolver resolver = FileStorages.getFileStorage2EntitiesResolver();
        return resolver.getFileStorageUsedBy(ctx.getContextId(), true);
    }

    /**
     * Gets the {@link FileStorage} for the specified {@link User} in the specified {@link Context}
     *
     * @param context The {@link Context}
     * @param user The {@link User}
     * @return the {@link FileStorage} for the specified {@link User} in the specified {@link Context}
     * @throws OXException if the {@link FileStorage} cannot be returned
     */
    private FileStorage getFileStorage(Context context, User user) throws OXException {
        FileStorage2EntitiesResolver resolver = FileStorages.getFileStorage2EntitiesResolver();
        return resolver.getFileStorageUsedBy(context.getContextId(), user.getId(), true);
    }

    /**
     * Gets a {@link List} with {@link Context}s that are using the {@link FileStorage} with the specified filestore identifier
     *
     * @param filestoreId the filestore identifier
     * @return the {@link List} with {@link Context}s that are using the {@link FileStorage} with the specified filestore identifier
     * @throws OXException If the {@link Context}s cannot be returned
     */
    private List<Context> getContextsForFilestore(int filestoreId) throws OXException {
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
     * Gets a {@link List} with {@link Entity} objects that are using the {@link FileStorage} with the specified filestore identifier
     *
     * @param filestoreId the filestore identifier
     * @return the {@link List} with {@link Entity} objects that are using the {@link FileStorage} with the specified filestore identifier
     * @throws OXException If the {@link Context}s cannot be returned
     */
    private List<Entity> getEntitiesForFilestore(int filestoreId) throws OXException {
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

    /**
     * Converts the list with the specified contexts into entity objects
     *
     * @param contexts the list with contexts
     * @return a list with entity objects
     */
    private List<Entity> toEntities(List<Context> contexts) {
        List<Entity> entities = new ArrayList<Entity>(contexts.size());
        for (Context ctx : contexts) {
            entities.add(new EntityImpl(ctx));
        }
        return entities;
    }

    /**
     * Returns a map with {@link Context} and {@link User} objects that are using the file storage with the specified identifier
     *
     * @param filestoreId the file storage identifier
     * @return a map with {@link Context} and {@link User} objects that are using the file storage with the specified identifier
     * @throws OXException
     */
    private Map<Context, List<User>> getUsersForFilestore(int filestoreId) throws OXException {
        Map<Integer, List<Integer>> users = FileStorages.getFileStorage2EntitiesResolver().getIdsOfUsersUsing(filestoreId);
        return loadUsers(users);
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

    /**
     * Gets a {@link List} with {@link Context}s that are using database with the specified database identifier
     *
     * @param databaseId the database identifier
     * @return the {@link List} with {@link Context}s that are using the database with the specified database identifier
     * @throws OXException If the {@link Context}s cannot be returned
     */
    private List<Context> getContextsForDatabase(int databaseId) throws OXException {
        DatabaseService configDB = ServerServiceRegistry.getInstance().getService(DatabaseService.class, true);

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

        int[] contextIds = configDB.listContexts(databaseId, -1, -1);
        return loadContexts(contextIds);
    }

    /**
     * Gets a {@link List} with all {@link Context}s
     *
     * @return a {@link List} with all {@link Context}s
     * @throws OXException If the {@link Context}s cannot be returned
     */
    private List<Context> getAllContexts() throws OXException {
        ContextStorage ctxstor = ContextStorage.getInstance();
        List<Integer> list = ctxstor.getAllContextIds();

        return loadContexts(list);
    }

    /**
     * Gets the admin {@link User} of the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return the admin {@link User} of the specified {@link Context}
     * @throws OXException if the admin {@link User} cannot be returned
     */
    private User getAdmin(Context ctx) throws OXException {
        return UserStorage.getInstance().getUser(ctx.getMailadmin(), ctx);
    }

    //////////////////////////////////// GET PERFORMERS ///////////////////////////////////

    /**
     * Gets a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     * @throws OXException if the snippet file store locations cannot be returned
     */
    private SortedSet<String> getSnippetFileStoreLocationsPerContext(Context ctx) throws OXException {
        SortedSet<String> retval = new TreeSet<String>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        try {
            con = databaseService.getReadOnly(ctx);
            if (DBUtils.tableExists(con, "snippet")) {
                stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND refType=1");
                stmt.setInt(1, ctx.getContextId());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    retval.add(rs.getString(1));
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                databaseService.backReadOnly(ctx, con);
            }
        }
        return retval;
    }

    /**
     * Gets a {@link SortedSet} with all snippet file store locations for the specified {@link Context} and {@link User}
     *
     * @param ctx the {@link Context}
     * @param user the {@link User}
     * @return a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     * @throws OXException if the snippet file store locations cannot be returned
     */
    private SortedSet<String> getSnippetFileStoreLocationsPerUser(Context ctx, User user) throws OXException {
        SortedSet<String> retval = new TreeSet<String>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        try {
            con = databaseService.getReadOnly(ctx);
            if (DBUtils.tableExists(con, "snippet")) {
                stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND user=? AND refType=1");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, user.getId());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    retval.add(rs.getString(1));
                }
                Databases.closeSQLStuff(rs, stmt);
                stmt = null;
            }
        } catch (SQLException e) {
            throw AttachmentExceptionCodes.SQL_PROBLEM.create(e, getStatement(stmt));
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                databaseService.backReadOnly(ctx, con);
            }
        }
        return retval;
    }

    /**
     * Gets a {@link SortedSet} with all vcard file store locations for the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return a {@link SortedSet} with all vcard file store locations for the specified {@link Context}
     * @throws OXException if the vcard file store locations cannot be returned
     */
    private SortedSet<String> getVCardFileStoreLocationsPerContext(Context ctx) throws OXException {
        VCardStorageMetadataStore vCardStorageMetadataStore = services.getOptionalService(VCardStorageMetadataStore.class);
        if (vCardStorageMetadataStore == null) {
            return new TreeSet<String>();
        }
        Set<String> loadRefIds = vCardStorageMetadataStore.loadRefIds(ctx.getContextId());
        return new TreeSet<String>(loadRefIds);
    }

    /**
     * Gets a {@link SortedSet} with all vcard file store locations for the specified {@link Context} and {@link User}
     *
     * @param ctx the {@link Context}
     * @param user The {@link User}
     * @return a {@link SortedSet} with all vcard file store locations for the specified {@link Context}
     * @throws OXException if the vcard file store locations cannot be returned
     */
    private SortedSet<String> getVCardFileStoreLocationsPerUser(Context ctx, User user) throws OXException {
        VCardStorageMetadataStore vCardStorageMetadataStore = services.getOptionalService(VCardStorageMetadataStore.class);
        if (vCardStorageMetadataStore == null) {
            return new TreeSet<String>();
        }
        Set<String> loadRefIds = vCardStorageMetadataStore.loadRefIds(ctx.getContextId(), user.getId());
        return new TreeSet<String>(loadRefIds);
    }

    /**
     * Gets a {@link SortedSet} with all preview cache file store locations for the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     * @throws OXException if the preview cache file store locations cannot be returned
     */
    private SortedSet<String> getPreviewCacheFileStoreLocationsPerContext(Context ctx) throws OXException {
        ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
        Set<String> refIds = metadataStore.loadRefIds(ctx.getContextId());
        return new TreeSet<String>(refIds);
    }

    /**
     * Gets a {@link SortedSet} with all preview cache file store locations for the specified {@link Context} and {@link User}
     *
     * @param ctx the {@link Context}
     * @param user the {@link User}
     * @return a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     * @throws OXException if the preview cache file store locations cannot be returned
     */
    private SortedSet<String> getPreviewCacheFileStoreLocationsPerUser(Context ctx, User user) throws OXException {
        ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
        Set<String> refIds = metadataStore.loadRefIds(ctx.getContextId(), user.getId());
        return new TreeSet<String>(refIds);
    }

    ///////////////////////////////////////////////////

    /**
     * Deletes the specified schema mapping from the 'context_server2db_pool'
     *
     * @param configCon The configdb writeable {@link Connection}
     * @param schema The schema name
     * @throws SQLException if an SQL error is occurred
     */
    private void deleteSchemaFromConfigDB(Connection configCon, String schema) throws SQLException {
        PreparedStatement stmt = null;
        try {
            LOG.debug("Deleting context_server2db_pool mapping for schema {}", schema);
            stmt = configCon.prepareStatement("DELETE FROM context_server2db_pool WHERE db_schema=?");
            stmt.setString(1, schema);
            stmt.executeUpdate();
            stmt.close();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Deletes the specified context from the configuration database
     * 
     * @param configCon The {@link Connection} to the configuration database
     * @param contextId The context identifier to delete
     * @throws SQLException if an SQL error is occurred
     */
    private void deleteContextFromConfigDB(Connection configCon, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            LOG.debug("Deleting context_server2db_pool mapping for context {}", contextId);
            // delete context from context_server2db_pool
            stmt = configCon.prepareStatement("DELETE FROM context_server2db_pool WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            stmt.close();
            // tell pool, that database has been removed
            try {
                com.openexchange.databaseold.Database.reset(contextId);
            } catch (OXException e) {
                LOG.error("", e);
            }

            LOG.debug("Deleting login2context entries for context {}", contextId);
            stmt = configCon.prepareStatement("DELETE FROM login2context WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            stmt.close();
            LOG.debug("Deleting context entry for context {}", contextId);
            stmt = configCon.prepareStatement("DELETE FROM context WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            stmt.close();
        } finally {
            if (null != stmt) {
                stmt.close();
            }
        }
    }

    /**
     * Performs a consistency check on the specified {@link Entity} object
     *
     * @param entity the entity
     * @param dbSolver The database solver
     * @param attachmentSolver The attachment solver
     * @param snippetSolver The snippet solver
     * @param previewSolver The preview cache solver
     * @param fileSolver The file solver
     * @param vCardSolver The vcard solver
     * @param database The database to use
     * @param attach The attachment base
     * @param fileStorage The file storage for that entity
     * @throws OXException if an error is occurred
     */
    private void checkOneEntity(Entity entity, ProblemSolver dbSolver, ProblemSolver attachmentSolver, ProblemSolver snippetSolver, ProblemSolver previewSolver, ProblemSolver fileSolver, ProblemSolver vCardSolver, DatabaseImpl database, AttachmentBase attach, FileStorage fileStorage) throws OXException {
        // We believe in the worst case, so lets check the storage first, so
        // that the state file is recreated
        LOG.info("Checking entity {}. Using solvers db: {} attachments: {} snippets: {} files: {} vcards: {}", entity, dbSolver.description(), attachmentSolver.description(), snippetSolver.description(), fileSolver.description(), vCardSolver.description());

        try {
            fileStorage.recreateStateFile();
        } catch (OXException e) {
            if (!FileStorageCodes.NO_SUCH_FILE_STORAGE.equals(e)) {
                throw e;
            }
            // Does not (yet) exist
            Object[] logArgs = e.getLogArgs();
            LOG.info("Cannot check files in filestore for entity {} since associated filestore does not (yet) exist: {}", entity, null == logArgs || 0 == logArgs.length ? e.getMessage() : logArgs[0].toString());

            return;
        }

        // Get files residing in file storages
        LOG.info("Listing all files in filestores");
        SortedSet<String> filestoreset = new TreeSet<String>();
        filestoreset = fileStorage.getFileList();
        LOG.info("Found {} files in the filestore for this entity {}", filestoreset.size(), entity);

        try {
            LOG.info("Loading all infostore filestore locations");
            SortedSet<String> dbfileset;

            SortedSet<String> attachmentset;
            SortedSet<String> snippetset;
            SortedSet<String> previewset;
            SortedSet<String> vcardset;
            switch (entity.getType()) {
                case Context:
                    dbfileset = database.getDocumentFileStoreLocationsPerContext(entity.getContext());

                    attachmentset = attach.getAttachmentFileStoreLocationsperContext(entity.getContext());
                    snippetset = getSnippetFileStoreLocationsPerContext(entity.getContext());
                    previewset = getPreviewCacheFileStoreLocationsPerContext(entity.getContext());
                    vcardset = getVCardFileStoreLocationsPerContext(entity.getContext());
                    break;
                case User:
                    dbfileset = database.getDocumentFileStoreLocationsPerUser(entity.getContext(), entity.getUser());

                    attachmentset = attach.getAttachmentFileStoreLocationsPerUser(entity.getContext(), entity.getUser());
                    snippetset = getSnippetFileStoreLocationsPerUser(entity.getContext(), entity.getUser());
                    previewset = getPreviewCacheFileStoreLocationsPerUser(entity.getContext(), entity.getUser());
                    vcardset = getVCardFileStoreLocationsPerUser(entity.getContext(), entity.getUser());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown entity type '" + entity.getType() + "'");

            }
            LOG.info("Found {} infostore filepaths", dbfileset.size());

            LOG.info("Found {} attachments", attachmentset.size());
            LOG.info("Found {} snippets", snippetset.size());
            LOG.info("Found {} previews", previewset.size());
            LOG.info("Found {} vCards", vcardset.size());

            SortedSet<String> joineddbfileset = new TreeSet<String>(dbfileset);
            joineddbfileset.addAll(attachmentset);
            joineddbfileset.addAll(snippetset);
            joineddbfileset.addAll(previewset);
            joineddbfileset.addAll(vcardset);

            LOG.info("Found {} filestore ids in total. There are {} files in the filespool. A difference of {}", joineddbfileset.size(), filestoreset.size(), Math.abs(joineddbfileset.size() - filestoreset.size()));

            // Build the difference set of the database set, so that the final
            // dbfileset contains all the members that aren't in the filestoreset
            if (ConsistencyUtil.diffSet(dbfileset, filestoreset, "database list", "filestore list")) {
                // implement the solver for dbfiles here
                dbSolver.solve(entity, dbfileset);
            }

            // Build the difference set of the attachment database set, so that the
            // attachmentset contains all the members that aren't in the
            // filestoreset
            if (ConsistencyUtil.diffSet(attachmentset, filestoreset, "database list of attachment files", "filestore list")) {
                // implement the solver for deleted dbfiles here
                attachmentSolver.solve(entity, attachmentset);
            }

            // Build the difference set of the attachment database set, so that the
            // attachmentset contains all the members that aren't in the
            // filestoreset
            if (ConsistencyUtil.diffSet(snippetset, filestoreset, "database list of snippet files", "filestore list")) {
                // implement the solver for deleted dbfiles here
                snippetSolver.solve(entity, snippetset);
            }

            if (ConsistencyUtil.diffSet(previewset, filestoreset, "database list of cached previews", "filestore list")) {
                previewSolver.solve(entity, previewset);
            }

            if (ConsistencyUtil.diffSet(vcardset, filestoreset, "database list of VCard files", "filestore list")) {
                vCardSolver.solve(entity, vcardset);
            }

            // Build the difference set of the filestore set, so that the final
            // filestoreset contains all the members that aren't in the dbfileset or
            // the dbdelfileset
            if (ConsistencyUtil.diffSet(filestoreset, joineddbfileset, "filestore list", "one of the databases")) {
                // implement the solver for the filestore here
                fileSolver.solve(entity, filestoreset);
            }
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Repairs the specified entity objects with the specified policy
     *
     * @param entities The entity objects to repair
     * @param policy The policy to use
     * @throws OXException
     */
    private void repair(List<Entity> entities, RepairPolicy repairPolicy, RepairAction repairAction) throws OXException {
        DatabaseImpl database = getDatabase();
        AttachmentBase attachments = getAttachments();
        for (Entity entity : entities) {
            FileStorage storage = getFileStorage(entity);

            PolicyResolver resolvers = PolicyResolver.build(repairPolicy, repairAction, database, attachments, storage, getAdmin(entity.getContext()));
            checkOneEntity(entity, resolvers.getDbSolver(), resolvers.getAttachmentSolver(), resolvers.getSnippetSolver(), new DeleteBrokenPreviewReferencesSolver(), resolvers.getFileSolver(), resolvers.getvCardSolver(), database, attachments, storage);

            /*
             * The ResourceCache might store resources in the filestorage. Depending on its configuration (preview.properties)
             * these files affect the contexts quota or not.
             */
            boolean quotaAware = false;
            ConfigurationService configurationService = ServerServiceRegistry.getServize(ConfigurationService.class);
            if (configurationService != null) {
                quotaAware = configurationService.getBoolProperty("com.openexchange.preview.cache.quotaAware", false);
            }

            Set<String> filesToIgnore;
            if (quotaAware) {
                filesToIgnore = new HashSet<>();
            } else {
                if (entity.getType().equals(EntityType.Context)) {
                    filesToIgnore = getPreviewCacheFileStoreLocationsPerContext(entity.getContext());
                } else {
                    filesToIgnore = new HashSet<>();
                }
            }

            /*
             * Depending on the configuration snippets doesn't count towards the usage too.
             */
            QuotaAwareSnippetService service = ServerServiceRegistry.getInstance().getService(QuotaAwareSnippetService.class);
            if (service != null) {
                if (service.ignoreQuota()) {
                    filesToIgnore.addAll(service.getFilesToIgnore(entity.getContext().getContextId()));
                }
            }

            recalculateUsage(storage, filesToIgnore);
        }
    }

    /**
     * Recalculates the usage of the specified {@link FileStorage} and ignores the specified files
     *
     * @param storage The {@link FileStorage}
     * @param filesToIgnore The files to ignore
     */
    private void recalculateUsage(FileStorage storage, Set<String> filesToIgnore) {
        try {
            if (storage instanceof QuotaFileStorage) {
                ConsistencyUtil.output("Recalculating usage...");
                ((QuotaFileStorage) storage).recalculateUsage(filesToIgnore);
            }
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    /**
     * Returns a map with all missing entries for the specified entity objects
     *
     * @param entities the entity objects
     * @return a map with all missing entries for the specified entity objects
     * @throws OXException if an error is occurred
     */
    private Map<Entity, List<String>> listMissing(List<Entity> entities) throws OXException {
        Map<Entity, List<String>> retval = new HashMap<Entity, List<String>>();
        DoNothingSolver doNothing = new DoNothingSolver();
        for (Entity entity : entities) {
            RecordSolver recorder = new RecordSolver();
            checkOneEntity(entity, recorder, recorder, recorder, recorder, doNothing, recorder, getDatabase(), getAttachments(), getFileStorage(entity));
            retval.put(entity, recorder.getProblems());
        }
        return retval;
    }

    /**
     * Returns a map with all unassigned entries for the specified entity objects
     *
     * @param entities the entity objects
     * @return a map with all unassigned entries for the specified entity objects
     * @throws OXException if an error is occurred
     */
    private Map<Entity, List<String>> listUnassigned(List<Entity> entities) throws OXException {
        Map<Entity, List<String>> retval = new HashMap<Entity, List<String>>();
        DoNothingSolver doNothing = new DoNothingSolver();
        for (Entity entity : entities) {
            RecordSolver recorder = new RecordSolver();
            checkOneEntity(entity, doNothing, doNothing, doNothing, doNothing, recorder, doNothing, getDatabase(), getAttachments(), getFileStorage(entity));
            retval.put(entity, recorder.getProblems());
        }
        return retval;
    }
}
