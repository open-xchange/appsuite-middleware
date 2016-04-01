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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.management.MBeanException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.consistency.Entity.EntityType;
import com.openexchange.consistency.osgi.ConsistencyServiceLookup;
import com.openexchange.consistency.solver.CreateDummyFileForAttachmentSolver;
import com.openexchange.consistency.solver.CreateDummyFileForInfoitemSolver;
import com.openexchange.consistency.solver.CreateDummyFileForSnippetSolver;
import com.openexchange.consistency.solver.CreateInfoitemSolver;
import com.openexchange.consistency.solver.DeleteAttachmentSolver;
import com.openexchange.consistency.solver.DeleteBrokenPreviewReferencesSolver;
import com.openexchange.consistency.solver.DeleteBrokenVCardReferencesSolver;
import com.openexchange.consistency.solver.DeleteInfoitemSolver;
import com.openexchange.consistency.solver.DeleteSnippetSolver;
import com.openexchange.consistency.solver.DoNothingSolver;
import com.openexchange.consistency.solver.ProblemSolver;
import com.openexchange.consistency.solver.RecordSolver;
import com.openexchange.consistency.solver.RemoveFileSolver;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.report.internal.Tools;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * Provides the Business Logic for the consistency tool. Concrete subclasses must provide integration to the environment by implementing the
 * abstract methods.
 *
 * @author Dennis Sieben
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 * @author Ioannis Chouklis <ioannis.chouklis@open-xchange.com>
 */
public abstract class Consistency implements ConsistencyMBean {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Consistency.class);

    /**
     * Initialises a new {@link Consistency}.
     */
    protected Consistency() {
        super();
    }

    @Override
    public List<String> listMissingFilesInContext(final int contextId) throws MBeanException {
        try {
            LOG.info("Listing missing files in context {}", contextId);
            final DoNothingSolver doNothing = new DoNothingSolver();
            final RecordSolver recorder = new RecordSolver();
            final Context ctx = getContext(contextId);
            checkOneEntity(new EntityImpl(ctx), recorder, recorder, recorder, recorder, doNothing, recorder, getDatabase(), getAttachments(), getFileStorage(ctx));
            return recorder.getProblems();
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<MBeanEntity, List<String>> listMissingFilesInFilestore(final int filestoreId) throws MBeanException {
        try {
            LOG.info("Listing missing files in filestore {}", filestoreId);
            return listMissing(getEntitiesForFilestore(filestoreId));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<MBeanEntity, List<String>> listMissingFilesInDatabase(final int databaseId) throws MBeanException {
        try {
            LOG.info("List missing files in database {}", databaseId);
            return listMissing(toEntities(getContextsForDatabase(databaseId)));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<MBeanEntity, List<String>> listAllMissingFiles() throws MBeanException {
        try {
            LOG.info("List all missing files");
            return listMissing(toEntities(getAllContexts()));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public List<String> listUnassignedFilesInContext(final int contextId) throws MBeanException {
        try {
            LOG.info("List all unassigned files in context {}", contextId);
            final DoNothingSolver doNothing = new DoNothingSolver();
            final RecordSolver recorder = new RecordSolver();
            final Context ctx = getContext(contextId);
            checkOneEntity(new EntityImpl(ctx), doNothing, doNothing, doNothing, doNothing, recorder, doNothing, getDatabase(), getAttachments(), getFileStorage(ctx));
            return recorder.getProblems();
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<MBeanEntity, List<String>> listUnassignedFilesInFilestore(final int filestoreId) throws MBeanException {
        try {
            LOG.info("List all unassigned files in filestore {}", filestoreId);
            return listUnassigned(getEntitiesForFilestore(filestoreId));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<MBeanEntity, List<String>> listUnassignedFilesInDatabase(final int databaseId) throws MBeanException {
        try {
            LOG.info("List all unassigned files in database {}", databaseId);
            return listUnassigned(toEntities(getContextsForDatabase(databaseId)));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public Map<MBeanEntity, List<String>> listAllUnassignedFiles() throws MBeanException {
        try {
            LOG.info("List all unassigned files");
            return listUnassigned(toEntities(getAllContexts()));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * FIXME: Here we should call com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorageCommon.deleteContextFromConfigDB(Connection, int)
     * for now I copied the code from there (except the empty schema deletion)
     */
    private void deleteContextFromConfigDB(final Connection configCon, final int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            LOG.debug("Deleting context_server2dbpool mapping for context {}", contextId);
            // delete context from context_server2db_pool
            stmt = configCon.prepareStatement("DELETE FROM context_server2db_pool WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            stmt.close();
            // tell pool, that database has been removed
            try {
                com.openexchange.databaseold.Database.reset(contextId);
            } catch (final OXException e) {
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

    @Override
    public List<String> checkOrRepairConfigDB(final boolean repair) throws MBeanException {
        if (repair) {
            LOG.info("Repair inconsistent configdb");
        } else {
            LOG.info("List inconsistent configdb");
        }

        DatabaseService databaseService = null;

        Connection confCon = null;
        Connection poolCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> ret = new ArrayList<String>();

        HashMap<String, List<Integer>> schemaMap = new HashMap<String, List<Integer>>();

        try {
            databaseService = ConsistencyServiceLookup.getService(DatabaseService.class, true);

            final Map<String, Integer> schemaPoolMap = Tools.getAllSchemata(LOG);
            confCon = databaseService.getReadOnly();
            stmt = confCon.prepareStatement("SELECT db_schema,cid FROM context_server2db_pool");
            rs = stmt.executeQuery();
            while (rs.next()) {
                final String schema = rs.getString(1);
                final Integer ctx = Integer.valueOf(rs.getInt(2));
                if (schemaMap.containsKey(schema)) {
                    schemaMap.get(schema).add(ctx);
                } else {
                    List<Integer> ctxs = new ArrayList<Integer>();
                    ctxs.add(ctx);
                    schemaMap.put(schema, ctxs);
                }
            }
            DBUtils.closeSQLStuff(rs, stmt);
            stmt = null;
            for (final Entry<String, List<Integer>> schemaEntry : schemaMap.entrySet()) {
                String schema = schemaEntry.getKey();
                List<Integer> ctxs = schemaEntry.getValue();
                Integer poolid = schemaPoolMap.get(schema);
                poolCon = databaseService.get(poolid.intValue(), schema);
                String contextids = "";
                for (final Integer c : ctxs) {
                    contextids += c + ",";
                }
                contextids = contextids.substring(0, contextids.length() - 1);
                stmt = poolCon.prepareStatement("SELECT cid FROM login2user WHERE cid IN (" + contextids + ") GROUP BY cid");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Integer ctx = Integer.valueOf(rs.getInt(1));
                    ctxs.remove(ctx);
                }
                if (ctxs.size() > 0) {
                    LOG.info("Schema {} is broken", schema);
                    for (final Integer ctx : ctxs) {
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
                DBUtils.closeSQLStuff(rs, stmt);
                stmt = null;
                databaseService.back(poolid.intValue(), poolCon);
                poolCon = null;
            }
            if (ret.size() == 0 && repair) {
                ret.add("there was nothing to repair");
            }
            DBUtils.closeSQLStuff(rs, stmt);
            stmt = null;
            return ret;
        } catch (final SQLException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
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

    /**
     * Returns a map with all missing entries for the specified entity objects
     *
     * @param entities the entity objects
     * @return a map with all missing entries for the specified entity objects
     * @throws OXException
     */
    private Map<MBeanEntity, List<String>> listMissing(final List<Entity> entities) throws OXException {
        final Map<MBeanEntity, List<String>> retval = new HashMap<MBeanEntity, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for (final Entity entity : entities) {
            final RecordSolver recorder = new RecordSolver();
            checkOneEntity(entity, recorder, recorder, recorder, recorder, doNothing, recorder, getDatabase(), getAttachments(), getFileStorage(entity));
            retval.put(toMBeanEntity(entity), recorder.getProblems());
        }
        return retval;
    }

    /**
     * Converts an Entity objects to an MBeanEntity objects
     *
     * @param entity The entity object to convert
     * @return the MBeanEntity
     */
    private MBeanEntity toMBeanEntity(Entity entity) {
        switch (entity.getType()) {
            case Context:
                return new MBeanEntity(entity.getContext().getContextId());
            case User:
                return new MBeanEntity(entity.getContext().getContextId(), entity.getUser().getId());
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entity.getType());
        }
    }

    /**
     * Returns a map with all unassigned entries for the specified entity objects
     *
     * @param entities the entity objects
     * @return a map with all unassigned entries for the specified entity objects
     * @throws OXException
     */
    private Map<MBeanEntity, List<String>> listUnassigned(final List<Entity> entities) throws OXException {
        final Map<MBeanEntity, List<String>> retval = new HashMap<MBeanEntity, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for (final Entity entity : entities) {
            final RecordSolver recorder = new RecordSolver();
            checkOneEntity(entity, doNothing, doNothing, doNothing, doNothing, recorder, doNothing, getDatabase(), getAttachments(), getFileStorage(entity));
            retval.put(toMBeanEntity(entity), recorder.getProblems());
        }
        return retval;
    }

    // Repair

    @Override
    public void repairFilesInContext(final int contextId, final String resolverPolicy) throws MBeanException {
        try {
            final List<Context> repairMe = new ArrayList<Context>();
            repairMe.add(getContext(contextId));
            repair(toEntities(repairMe), resolverPolicy);
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void repairFilesInFilestore(final int filestoreId, final String resolverPolicy) throws MBeanException {
        try {
            repair(toEntities(getContextsForFilestore(filestoreId)), resolverPolicy);
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void repairFilesInDatabase(final int databaseId, final String resolverPolicy) throws MBeanException {
        try {
            repair(toEntities(getContextsForDatabase(databaseId)), resolverPolicy);
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void repairAllFiles(final String resolverPolicy) throws MBeanException {
        try {
            repair(toEntities(getAllContexts()), resolverPolicy);
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new MBeanException(wrapMe, e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /**
     * Repairs the specified entity objects with the specified policy
     *
     * @param entities The entity objects to repair
     * @param policy The policy to use
     * @throws OXException
     */
    private void repair(final List<Entity> entities, final String policy) throws OXException {
        final DatabaseImpl database = getDatabase();
        final AttachmentBase attachments = getAttachments();
        for (final Entity entity : entities) {
            FileStorage storage = getFileStorage(entity);

            final ResolverPolicy resolvers = ResolverPolicy.parse(policy, database, attachments, storage, this, entity.getContext());
            checkOneEntity(entity, resolvers.dbsolver, resolvers.attachmentsolver, resolvers.snippetsolver, new DeleteBrokenPreviewReferencesSolver(), resolvers.filesolver, resolvers.vCardSolver, database, attachments, storage);

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
                filesToIgnore = Collections.emptySet();
            } else {
                if (entity.getType().equals(EntityType.Context)) {
                    filesToIgnore = getPreviewCacheFileStoreLocationsPerContext(entity.getContext());
                } else {
                    filesToIgnore = Collections.emptySet();
                }
            }
            recalculateUsage(storage, filesToIgnore);
        }
    }

    // Taken from original consistency tool //

    /**
     * Logs a message with log level INFO
     *
     * @param text the message to log
     */
    private void output(final String text) {
        LOG.info(text);
    }

    /**
     * Logs a message with log level ERROR
     *
     * @param text the message to log
     */
    private void erroroutput(final Exception e) {
        LOG.error("", e);
    }

    /**
     * Logs the specified set with log level INFO
     *
     * @param set the set to log
     */
    private void outputSet(final SortedSet<String> set) {
        final Iterator<String> itstr = set.iterator();
        final StringBuilder sb = new StringBuilder();
        while (itstr.hasNext()) {
            sb.append(itstr.next()).append('\n');
        }
        output(sb.toString());
    }

    /**
     * Makes the difference set between two set, the first one is changed
     */
    private boolean diffset(final SortedSet<String> first, final SortedSet<String> second, final String name, final String name2) {
        first.removeAll(second);
        if (first.isEmpty()) {
            return false;
        }
        output("Inconsistencies found in " + name + ", the following files aren't in " + name2 + ':');
        outputSet(first);
        return true;
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
     * @throws OXException
     */
    private void checkOneEntity(final Entity entity, final ProblemSolver dbSolver, final ProblemSolver attachmentSolver, final ProblemSolver snippetSolver, final ProblemSolver previewSolver, final ProblemSolver fileSolver, final ProblemSolver vCardSolver, final DatabaseImpl database, final AttachmentBase attach, final FileStorage fileStorage) throws OXException {
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
            boolean isContext = entity.getType().equals(EntityType.Context);

            LOG.info("Loading all infostore filestore locations");
            SortedSet<String> dbfileset;
            if (isContext) {
                dbfileset = database.getDocumentFileStoreLocationsperContext(entity.getContext());
            } else {
                dbfileset = database.getDocumentFileStoreLocationsPerUser(entity.getContext(), entity.getUser());
            }
            LOG.info("Found {} infostore filepaths", dbfileset.size());

            if (isContext) {
                // Get the referenced ones
                SortedSet<String> attachmentset = attach.getAttachmentFileStoreLocationsperContext(entity.getContext());
                LOG.info("Found {} attachments", attachmentset.size());

                SortedSet<String> snippetset = getSnippetFileStoreLocationsPerContext(entity.getContext());
                LOG.info("Found {} snippets", snippetset.size());

                SortedSet<String> previewset = getPreviewCacheFileStoreLocationsPerContext(entity.getContext());
                LOG.info("Found {} previews", previewset.size());

                SortedSet<String> vcardset = getVCardFileStoreLocationsPerContext(entity.getContext());
                LOG.info("Found {} vCards", vcardset.size());

                final SortedSet<String> joineddbfileset = new TreeSet<String>(dbfileset);
                joineddbfileset.addAll(attachmentset);
                joineddbfileset.addAll(snippetset);
                joineddbfileset.addAll(previewset);
                joineddbfileset.addAll(vcardset);

                LOG.info("Found {} filestore ids in total. There are {} files in the filespool. A difference of {}", joineddbfileset.size(), filestoreset.size(), Math.abs(joineddbfileset.size() - filestoreset.size()));

                // Build the difference set of the database set, so that the final
                // dbfileset contains all the members that aren't in the filestoreset
                if (diffset(dbfileset, filestoreset, "database list", "filestore list")) {
                    // implement the solver for dbfiles here
                    dbSolver.solve(entity, dbfileset);
                }

                // Build the difference set of the attachment database set, so that the
                // final attachmentset contains all the members that aren't in the
                // filestoreset
                if (diffset(attachmentset, filestoreset, "database list of attachment files", "filestore list")) {
                    // implement the solver for deleted dbfiles here
                    attachmentSolver.solve(entity, attachmentset);
                }

                // Build the difference set of the attachment database set, so that the
                // final attachmentset contains all the members that aren't in the
                // filestoreset
                if (diffset(snippetset, filestoreset, "database list of snippet files", "filestore list")) {
                    // implement the solver for deleted dbfiles here
                    snippetSolver.solve(entity, snippetset);
                }

                if (diffset(previewset, filestoreset, "database list of cached previews", "filestore list")) {
                    previewSolver.solve(entity, previewset);
                }

                if (diffset(vcardset, filestoreset, "database list of VCard files", "filestore list")) {
                    vCardSolver.solve(entity, vcardset);
                }

                // Build the difference set of the filestore set, so that the final
                // filestoreset contains all the members that aren't in the dbfileset or
                // the dbdelfileset
                if (diffset(filestoreset, joineddbfileset, "filestore list", "one of the databases")) {
                    // implement the solver for the filestore here
                    fileSolver.solve(entity, filestoreset);
                }
            }
        } catch (final OXException e) {
            erroroutput(e);
        }
    }

    /**
     * Recalculates the usage of the specified {@link FileStorage} and ignores the specified files
     *
     * @param storage The {@link FileStorage}
     * @param filesToIgnore The files to ignore
     */
    private void recalculateUsage(final FileStorage storage, final Set<String> filesToIgnore) {
        try {
            if (storage instanceof QuotaFileStorage) {
                output("Recalculating usage...");
                ((QuotaFileStorage) storage).recalculateUsage(filesToIgnore);
            }
        } catch (final OXException e) {
            erroroutput(e);
        }
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
     * Get the {@link Context} for the specified context identifier from the {@link ContextStore}
     *
     * @param contextId The context identifier
     * @return The {@link Context}
     * @throws OXException if the {@link Context} cannot be returned
     */
    protected abstract Context getContext(int contextId) throws OXException;

    /**
     * Get the DatabaseImpl
     *
     * @return the DatabaseImpl
     */
    protected abstract DatabaseImpl getDatabase();

    /**
     * Gets the {@link AttachmentBase} instance
     *
     * @return the {@link AttachmentBase} instance
     */
    protected abstract AttachmentBase getAttachments();

    /**
     * Gets the {@link FileStorage} for the specified {@link Context}
     *
     * @param ctx The {@link Context} for which the {@link FileStorage} shall be returned
     * @return the {@link FileStorage} for the specified {@link Context}
     * @throws OXException if the {@link FileStorage} cannot be returned
     */
    protected abstract FileStorage getFileStorage(Context ctx) throws OXException;

    /**
     * Gets the {@link FileStorage} for the specified {@link User} in the specified {@link Context}
     *
     * @param ctx The {@link Context}
     * @param usr The {@link User}
     * @return the {@link FileStorage} for the specified {@link User} in the specified {@link Context}
     * @throws OXException if the {@link FileStorage} cannot be returned
     */
    protected abstract FileStorage getFileStorage(Context ctx, User usr) throws OXException;

    /**
     * Gets the {@link FileStorage} for the specified {@link Entity}
     *
     * @param entity The {@link Entity} for which the {@link FileStorage} shall be returned
     * @return the {@link FileStorage} for the specified {@link Entity}
     * @throws OXException if the {@link FileStorage} cannot be returned
     */
    protected abstract FileStorage getFileStorage(Entity entity) throws OXException;

    /**
     * Gets a {@link List} with {@link Context}s that are using the {@link FileStorage} with the specified filestore identifier
     *
     * @param filestoreId the filestore identifier
     * @return the {@link List} with {@link Context}s that are using the {@link FileStorage} with the specified filestore identifier
     * @throws OXException If the {@link Context}s cannot be returned
     */
    protected abstract List<Context> getContextsForFilestore(int filestoreId) throws OXException;

    /**
     * Gets a {@link List} with {@link Entity} objects that are using the {@link FileStorage} with the specified filestore identifier
     *
     * @param filestoreId the filestore identifier
     * @return the {@link List} with {@link Entity} objects that are using the {@link FileStorage} with the specified filestore identifier
     * @throws OXException If the {@link Context}s cannot be returned
     */
    protected abstract List<Entity> getEntitiesForFilestore(int filestoreId) throws OXException;

    /**
     * Gets a {@link List} with {@link Context}s that are using database with the specified database identifier
     *
     * @param databaseId the database identifier
     * @return the {@link List} with {@link Context}s that are using the database with the specified database identifier
     * @throws OXException If the {@link Context}s cannot be returned
     */
    protected abstract List<Context> getContextsForDatabase(int datbaseId) throws OXException;

    /**
     * Gets a {@link List} with all {@link Context}s
     *
     * @return a {@link List} with all {@link Context}s
     * @throws OXException If the {@link Context}s cannot be returned
     */
    protected abstract List<Context> getAllContexts() throws OXException;

    /**
     * Gets a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     * @throws OXException if the snippet file store locations cannot be returned
     */
    protected abstract SortedSet<String> getSnippetFileStoreLocationsPerContext(Context ctx) throws OXException;

    /**
     * Gets a {@link SortedSet} with all vcard file store locations for the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return a {@link SortedSet} with all vcard file store locations for the specified {@link Context}
     * @throws OXException if the vcard file store locations cannot be returned
     */
    protected abstract SortedSet<String> getVCardFileStoreLocationsPerContext(Context ctx) throws OXException;

    /**
     * Gets a {@link SortedSet} with all preview cache file store locations for the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return a {@link SortedSet} with all snippet file store locations for the specified {@link Context}
     * @throws OXException if the preview cache file store locations cannot be returned
     */
    protected abstract SortedSet<String> getPreviewCacheFileStoreLocationsPerContext(Context ctx) throws OXException;

    /**
     * Gets the admin {@link User} of the specified {@link Context}
     *
     * @param ctx the {@link Context}
     * @return the admin {@link User} of the specified {@link Context}
     * @throws OXException if the admin {@link User} cannot be returned
     */
    protected abstract User getAdmin(Context ctx) throws OXException;

    /**
     * {@link ResolverPolicy}
     */
    private static final class ResolverPolicy {

        final ProblemSolver dbsolver;

        final ProblemSolver attachmentsolver;

        final ProblemSolver snippetsolver;

        final ProblemSolver filesolver;

        final ProblemSolver vCardSolver;

        public ResolverPolicy(final ProblemSolver dbsolver, final ProblemSolver attachmentsolver, final ProblemSolver snippetsolver, final ProblemSolver filesolver, final ProblemSolver vCardSolver) {
            this.dbsolver = dbsolver;
            this.attachmentsolver = attachmentsolver;
            this.snippetsolver = snippetsolver;
            this.filesolver = filesolver;
            this.vCardSolver = vCardSolver;
        }

        public static ResolverPolicy parse(final String list, final DatabaseImpl database, final AttachmentBase attach, final FileStorage storage, final Consistency consistency, final Context context) throws OXException {
            final String[] options = list.split("\\s*,\\s*");
            ProblemSolver dbsolver = new DoNothingSolver();
            ProblemSolver attachmentsolver = new DoNothingSolver();
            ProblemSolver snippetsolver = new DoNothingSolver();
            ProblemSolver filesolver = new DoNothingSolver();
            ProblemSolver vCardSolver = new DoNothingSolver();

            for (final String option : options) {
                final String[] tuple = option.split("\\s*:\\s*");
                if (tuple.length != 2) {
                    throw ConsistencyExceptionCodes.MALFORMED_POLICY.create();
                }
                final String condition = tuple[0];
                final String action = tuple[1];
                if ("missing_file_for_infoitem".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        dbsolver = new CreateDummyFileForInfoitemSolver(database, storage, consistency.getAdmin(context));
                    } else if ("delete".equals(action)) {
                        dbsolver = new DeleteInfoitemSolver(database);
                    } else {
                        dbsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_attachment".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        attachmentsolver = new CreateDummyFileForAttachmentSolver(attach, storage);
                    } else if ("delete".equals(action)) {
                        attachmentsolver = new DeleteAttachmentSolver(attach);
                    } else {
                        attachmentsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_snippet".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        snippetsolver = new CreateDummyFileForSnippetSolver(storage);
                    } else if ("delete".equals(action)) {
                        snippetsolver = new DeleteSnippetSolver();
                    } else {
                        snippetsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_vcard".equals(condition)) {
                    if ("delete".equals(action)) {
                        //TODO hat er alle geloescht?
                        vCardSolver = new DeleteBrokenVCardReferencesSolver();
                    } else {
                        vCardSolver = new DoNothingSolver();
                    }
                } else if ("missing_entry_for_file".equals(condition)) {
                    if ("create_admin_infoitem".equals(action)) {
                        filesolver = new CreateInfoitemSolver(database, storage, consistency.getAdmin(context));
                    } else if ("delete".equals(action)) {
                        filesolver = new RemoveFileSolver(storage);
                    } else {
                        filesolver = new DoNothingSolver();
                    }
                }
            }

            return new ResolverPolicy(dbsolver, attachmentsolver, snippetsolver, filesolver, vCardSolver);
        }

    }
}
