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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.management.MBeanException;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.config.ConfigurationService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.report.internal.Tools;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;
import com.openexchange.tools.sql.DBUtils;

/**
 * Provides the Business Logic for the consistency tool. Concrete subclasses must provide integration to the environment by implementing the
 * abstract methods.
 *
 * @author Dennis Sieben
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class Consistency implements ConsistencyMBean {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Consistency.class);

    @Override
    public List<String> listMissingFilesInContext(final int contextId) throws MBeanException {
        try {
            LOG.info("Listing missing files in context {}", contextId);
            final DoNothingSolver doNothing = new DoNothingSolver();
            final RecordSolver recorder = new RecordSolver();
            final Context ctx = getContext(contextId);
            checkOneContext(ctx, recorder, recorder, recorder, recorder, doNothing, getDatabase(), getAttachments(), getFileStorage(ctx));
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
    public Map<Integer, List<String>> listMissingFilesInFilestore(final int filestoreId) throws MBeanException {
        try {
            LOG.info("Listing missing files in filestore {}", filestoreId);
            return listMissing(getContextsForFilestore(filestoreId));
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
    public Map<Integer, List<String>> listMissingFilesInDatabase(final int databaseId) throws MBeanException {
        try {
            LOG.info("List missing files in database {}", databaseId);
            return listMissing(getContextsForDatabase(databaseId));
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
    public Map<Integer, List<String>> listAllMissingFiles() throws MBeanException {
        try {
            LOG.info("List all missing files");
            return listMissing(getAllContexts());
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
            checkOneContext(ctx, doNothing, doNothing, doNothing, doNothing, recorder, getDatabase(), getAttachments(), getFileStorage(ctx));
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
    public Map<Integer, List<String>> listUnassignedFilesInFilestore(final int filestoreId) throws MBeanException {
        try {
            LOG.info("List all unassigned files in filestore {}", filestoreId);
            return listUnassigned(getContextsForFilestore(filestoreId));
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
    public Map<Integer, List<String>> listUnassignedFilesInDatabase(final int databaseId) throws MBeanException {
        try {
            LOG.info("List all unassigned files in database {}", databaseId);
            return listUnassigned(getContextsForDatabase(databaseId));
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
    public Map<Integer, List<String>> listAllUnassignedFiles() throws MBeanException {
        try {
            LOG.info("List all unassigned files");
            return listUnassigned(getAllContexts());
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
     *        for now I copied the code from there (except the empty schema deletion)
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
            if( null != stmt ) {
                stmt.close();
            }
        }
    }

    @Override
    public List<String> checkOrRepairConfigDB(final boolean repair) throws MBeanException {
        if( repair ) {
            LOG.info("Repair inconsistent configdb");
        } else {
            LOG.info("List inconsistent configdb");
        }

        Connection confCon = null;
        Connection poolCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> ret = new ArrayList<String>();

        HashMap<String, List<Integer>> schemaMap = new HashMap<String, List<Integer>>();
        try {
            final Map<String, Integer> schemaPoolMap = Tools.getAllSchemata(LOG);
            confCon = Database.get(false);
            stmt = confCon.prepareStatement("SELECT db_schema,cid FROM context_server2db_pool");
            rs = stmt.executeQuery();
            while (rs.next()) {
                final String schema = rs.getString(1);
                final Integer ctx = Integer.valueOf(rs.getInt(2));
                if( schemaMap.containsKey(schema)) {
                    schemaMap.get(schema).add(ctx);
                } else {
                    List<Integer> ctxs = new ArrayList<Integer>();
                    ctxs.add(ctx);
                    schemaMap.put(schema, ctxs);
                }
            }
            DBUtils.closeSQLStuff(rs, stmt);
            stmt = null;
            for(final String schema : schemaMap.keySet()) {
                List<Integer> ctxs = schemaMap.get(schema);
                Integer poolid = schemaPoolMap.get(schema);
                poolCon = Database.get(poolid.intValue(), schema);
                String contextids = "";
                for(final Integer c : ctxs) {
                    contextids += c + ",";
                }
                contextids = contextids.substring(0, contextids.length()-1);
                stmt = poolCon.prepareStatement("SELECT cid FROM login2user WHERE cid IN (" + contextids + ") GROUP BY cid");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    final Integer ctx = Integer.valueOf(rs.getInt(1));
                    ctxs.remove(ctx);
                }
                if( ctxs.size() > 0 ) {
                    LOG.info("Schema {} is broken", schema);
                    for(final Integer ctx : ctxs) {
                        if( repair ) {
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
                Database.back(poolid.intValue(), poolCon);
                poolCon = null;
            }
            if ( ret.size() == 0 && repair ) {
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
            if (null != confCon) {
                Database.back(false, confCon);
            }
            if (null != poolCon) {
                Database.back(false, poolCon);
            }
        }
    }

    private Map<Integer, List<String>> listMissing(final List<Context> contexts) throws OXException {
        final Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for (final Context ctx : contexts) {
            final RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx, recorder, recorder, recorder, recorder, doNothing, getDatabase(), getAttachments(), getFileStorage(ctx));
            retval.put(Integer.valueOf(ctx.getContextId()), recorder.getProblems());
        }
        return retval;
    }

    private Map<Integer, List<String>> listUnassigned(final List<Context> contexts) throws OXException {
        final Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for (final Context ctx : contexts) {
            final RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx, doNothing, doNothing, doNothing, doNothing, recorder, getDatabase(), getAttachments(), getFileStorage(ctx));
            retval.put(Integer.valueOf(ctx.getContextId()), recorder.getProblems());
        }
        return retval;
    }

    // Repair

    @Override
    public void repairFilesInContext(final int contextId, final String resolverPolicy) throws MBeanException {
        try {
            final List<Context> repairMe = new ArrayList<Context>();
            repairMe.add(getContext(contextId));
            repair(repairMe, resolverPolicy);
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
            repair(getContextsForFilestore(filestoreId), resolverPolicy);
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
            repair(getContextsForDatabase(databaseId), resolverPolicy);
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
            repair(getAllContexts(), resolverPolicy);
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

    private void repair(final List<Context> contexts, final String policy) throws OXException {
        final DatabaseImpl database = getDatabase();
        final AttachmentBase attachments = getAttachments();
        for (final Context ctx : contexts) {
            final FileStorage storage = getFileStorage(ctx);

            final ResolverPolicy resolvers = ResolverPolicy.parse(policy, database, attachments, storage, this);
            checkOneContext(ctx, resolvers.dbsolver, resolvers.attachmentsolver, resolvers.snippetsolver, new DeleteBrokenPreviewReferences(), resolvers.filesolver, database, attachments, storage);

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
                filesToIgnore = getPreviewCacheFileStoreLocationsperContext(ctx);
            }
            recalculateUsage(storage, filesToIgnore);
        }
    }

    // Taken from original consistency tool //

    private void output(final String text) {
        LOG.info(text);
    }

    private void erroroutput(final Exception e) {
        LOG.error("", e);
    }

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
        boolean retval = false;
        first.removeAll(second);
        if (!first.isEmpty()) {
            output("Inconsistencies found in " + name + ", the following files aren't in " + name2 + ':');
            outputSet(first);
            retval = true;
        }
        return retval;
    }

    private void checkOneContext(final Context ctx, final ProblemSolver dbSolver, final ProblemSolver attachmentSolver, final ProblemSolver snippetSolver, final ProblemSolver previewSolver, final ProblemSolver fileSolver, final DatabaseImpl database, final AttachmentBase attach, final FileStorage stor) throws OXException {

        // We believe in the worst case, so lets check the storage first, so
        // that the state file is recreated
        LOG.info("Checking context {}. Using solvers db: {} attachments: {} snippets: {} files: {}", ctx.getContextId(), dbSolver.description(), attachmentSolver.description(), snippetSolver.description(), fileSolver.description());
        try {
            stor.recreateStateFile();
        } catch (OXException e) {
            if (FileStorageCodes.NO_SUCH_FILE_STORAGE.equals(e)) {
                // Does not (yet) exist
                Object[] logArgs = e.getLogArgs();
                LOG.info("Cannot check files in filestore for context {} since associated filestore does not (yet) exist: {}", ctx.getContextId(), null == logArgs || 0 == logArgs.length ? e.getMessage() : (String) logArgs[0]);
                return;
            }

            throw e;
        }

        LOG.info("Listing all files in filestore");
        final SortedSet<String> filestoreset = stor.getFileList();
        LOG.info("Found {} files in the filestore for this context", filestoreset.size());
        LOG.info("Loading all attachments");
        final SortedSet<String> attachmentset = attach.getAttachmentFileStoreLocationsperContext(ctx);
        LOG.info("Found {} attachments", attachmentset.size());
        final SortedSet<String> snippetset = getSnippetFileStoreLocationsperContext(ctx);
        LOG.info("Found {} snippets", snippetset.size());
        final SortedSet<String> previewset = getPreviewCacheFileStoreLocationsperContext(ctx);
        LOG.info("Found {} previews", previewset.size());
        SortedSet<String> dbfileset;
        try {
            LOG.info("Loading all infostore filestore locations");
            dbfileset = database.getDocumentFileStoreLocationsperContext(ctx);
            LOG.info("Found {} infostore filepaths", dbfileset.size());
            final SortedSet<String> joineddbfileset = new TreeSet<String>(dbfileset);
            joineddbfileset.addAll(attachmentset);
            joineddbfileset.addAll(snippetset);
            joineddbfileset.addAll(previewset);

            LOG.info("Found {} filestore ids in total. There are {} files in the filespool. A difference of {}", joineddbfileset.size(), filestoreset.size(), Math.abs(joineddbfileset.size() - filestoreset.size()));

            // Build the difference set of the database set, so that the final
            // dbfileset contains all the members that aren't in the filestoreset
            if (diffset(dbfileset, filestoreset, "database list", "filestore list")) {
                // implement the solver for dbfiles here
                dbSolver.solve(ctx, dbfileset);
            }

            // Build the difference set of the attachment database set, so that the
            // final attachmentset contains all the members that aren't in the
            // filestoreset
            if (diffset(attachmentset, filestoreset, "database list of attachment files", "filestore list")) {
                // implement the solver for deleted dbfiles here
                attachmentSolver.solve(ctx, attachmentset);
            }

            // Build the difference set of the attachment database set, so that the
            // final attachmentset contains all the members that aren't in the
            // filestoreset
            if (diffset(snippetset, filestoreset, "database list of snippet files", "filestore list")) {
                // implement the solver for deleted dbfiles here
                snippetSolver.solve(ctx, snippetset);
            }

            if (diffset(previewset, filestoreset, "database list of cached previews", "filestore list")) {
                previewSolver.solve(ctx, previewset);
            }

            // Build the difference set of the filestore set, so that the final
            // filestoreset contains all the members that aren't in the dbfileset or
            // the dbdelfileset
            if (diffset(filestoreset, joineddbfileset, "filestore list", "one of the databases")) {
                // implement the solver for the filestore here
                fileSolver.solve(ctx, filestoreset);
            }

        } catch (final OXException e) {
            erroroutput(e);
        }
    }

    private SortedSet<String> getPreviewCacheFileStoreLocationsperContext(Context ctx) throws OXException {
        ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
        Set<String> refIds = metadataStore.loadRefIds(ctx.getContextId());
        return new TreeSet<String>(refIds);
    }

    private SortedSet<String> getSnippetFileStoreLocationsperContext(Context ctx) throws OXException {
        final SortedSet<String> retval = new TreeSet<String>();
        Connection wcon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            wcon = Database.get(ctx, true);
            if (tableExists(wcon, "snippet")) {
                stmt = wcon.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND refType=1");
                stmt.setInt(1, ctx.getContextId());
                rs = stmt.executeQuery();
                while (rs.next()) {
                    retval.add(rs.getString(1));
                }
                DBUtils.closeSQLStuff(rs, stmt);
                stmt = null;
            }
            if (tableExists(wcon, "snippetAttachment")) {
                stmt = wcon.prepareStatement("SELECT referenceId FROM snippetAttachment WHERE cid=?");
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
            if (null != wcon) {
                Database.back(ctx, true, wcon);
            }
        }
        return retval;
    }

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

    protected abstract Context getContext(int contextId) throws OXException;

    protected abstract DatabaseImpl getDatabase();

    protected abstract AttachmentBase getAttachments();

    protected abstract FileStorage getFileStorage(Context ctx) throws OXException;

    protected abstract List<Context> getContextsForFilestore(int filestoreId) throws OXException;

    protected abstract List<Context> getContextsForDatabase(int datbaseId) throws OXException;

    protected abstract List<Context> getAllContexts() throws OXException;

    protected abstract User getAdmin(Context ctx) throws OXException;

    private static final class ResolverPolicy {

        final ProblemSolver dbsolver;

        final ProblemSolver attachmentsolver;

        final ProblemSolver snippetsolver;

        final ProblemSolver filesolver;

        public ResolverPolicy(final ProblemSolver dbsolver, final ProblemSolver attachmentsolver, final ProblemSolver snippetsolver, final ProblemSolver filesolver) {
            this.dbsolver = dbsolver;
            this.attachmentsolver = attachmentsolver;
            this.snippetsolver = snippetsolver;
            this.filesolver = filesolver;
        }

        public static ResolverPolicy parse(final String list, final DatabaseImpl database, final AttachmentBase attach, final FileStorage stor, final Consistency consistency) throws OXException {
            final String[] options = list.split("\\s*,\\s*");
            ProblemSolver dbsolver = new DoNothingSolver();
            ProblemSolver attachmentsolver = new DoNothingSolver();
            ProblemSolver snippetsolver = new DoNothingSolver();
            ProblemSolver filesolver = new DoNothingSolver();

            for (final String option : options) {
                final String[] tuple = option.split("\\s*:\\s*");
                if (tuple.length != 2) {
                    throw ConsistencyExceptionCodes.MALFORMED_POLICY.create();
                }
                final String condition = tuple[0];
                final String action = tuple[1];
                if ("missing_file_for_infoitem".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        dbsolver = new CreateDummyFileForInfoitem(database, stor);
                    } else if ("delete".equals(action)) {
                        dbsolver = new DeleteInfoitem(database);
                    } else {
                        dbsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_attachment".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        attachmentsolver = new CreateDummyFileForAttachment(attach, stor);
                    } else if ("delete".equals(action)) {
                        attachmentsolver = new DeleteAttachment(attach);
                    } else {
                        attachmentsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_snippet".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        snippetsolver = new CreateDummyFileForSnippet(stor);
                    } else if ("delete".equals(action)) {
                        snippetsolver = new DeleteSnippet();
                    } else {
                        snippetsolver = new DoNothingSolver();
                    }
                } else if ("missing_entry_for_file".equals(condition)) {
                    if ("create_admin_infoitem".equals(action)) {
                        filesolver = new CreateInfoitem(database, stor, consistency);
                    } else if ("delete".equals(action)) {
                        filesolver = new RemoveFile(stor);
                    } else {
                        filesolver = new DoNothingSolver();
                    }
                }
            }

            return new ResolverPolicy(dbsolver, attachmentsolver, snippetsolver, filesolver);
        }

    }

    private static interface ProblemSolver {

        public void solve(Context ctx, Set<String> problems) throws OXException;

        String description();
    }

    private static class DoNothingSolver implements ProblemSolver {

        public DoNothingSolver() {
            super();
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            // Ignore
        }

        @Override
        public String description() {
            return "Do Nothing";
        }
    }

    private static class RecordSolver implements ProblemSolver {

        public RecordSolver() {
            super();
        }

        private final List<String> memory = new ArrayList<String>();

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            memory.addAll(problems);
        }

        @Override
        public String description() {
            return "Remember in List";
        }

        public List<String> getProblems() {
            return memory;
        }
    }

    private static class CreateDummyFile {

        private final FileStorage storage;

        public CreateDummyFile(final FileStorage storage) {
            this.storage = storage;
        }

        /**
         * This method create a dummy file a returns its name
         *
         * @return The name of the dummy file
         * @throws OXException
         */
        protected String createDummyFile() throws OXException {
            final String filetext = "This is just a dummy file";
            final InputStream input = new ByteArrayInputStream(filetext.getBytes());

            return storage.saveNewFile(input);
        }
    }

    private static class CreateDummyFileForInfoitem extends CreateDummyFile implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(CreateDummyFileForInfoitem.class);

        private final DatabaseImpl database;

        public CreateDummyFileForInfoitem(final DatabaseImpl database, final FileStorage storage) {
            super(storage);
            this.database = database;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            /*
             * Here we operate in two stages. First we create a dummy entry in the filestore. Second we update the Entries in the database
             */
            for (final String old_identifier : problems) {
                try {
                    final String identifier = createDummyFile();
                    database.startTransaction();
                    final int changed =
                        database.modifyDocument(old_identifier, identifier, "\nCaution! The file has changed", "text/plain", ctx);
                    database.commit();
                    if (changed == 1) {
                        LOG1.info(MessageFormat.format("Modified entry for identifier {0} in context {1} to new dummy identifier {2}", old_identifier, ctx.getContextId(), identifier));
                    }
                } catch (final OXException e) {
                    LOG1.error("", e);
                    try {
                        database.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.debug("", e1);
                    }
                } catch (final RuntimeException e) {
                    LOG1.error("", e);
                    try {
                        database.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.debug("", e1);
                    }
                } finally {
                    try {
                        database.finish();
                    } catch (final OXException e) {
                        LOG1.debug("", e);
                    }
                }
            }
        }

        @Override
        public String description() {
            return "Create dummy file for infoitem";
        }
    }

    private static class CreateDummyFileForAttachment extends CreateDummyFile implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(CreateDummyFileForAttachment.class);

        private final AttachmentBase attachments;

        public CreateDummyFileForAttachment(final AttachmentBase attachments, final FileStorage storage) {
            super(storage);
            this.attachments = attachments;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            /*
             * Here we operate in two stages. First we create a dummy entry in the filestore. Second we update the Entries in the database
             */
            final int size = problems.size();
            final Iterator<String> it = problems.iterator();
            for (int k = 0; k < size; k++) {
                try {
                    final String identifier = createDummyFile();
                    final String old_identifier = it.next();
                    attachments.setTransactional(true);
                    attachments.startTransaction();
                    final int changed =
                        attachments.modifyAttachment(old_identifier, identifier, "\nCaution! The file has changed", "text/plain", ctx);
                    attachments.commit();
                    if (changed == 1) {
                        LOG1.info(MessageFormat.format("Created dummy entry for: {0}. New identifier is: {1}", old_identifier, identifier));
                    }
                } catch (final OXException e) {
                    LOG1.error("", e);
                    try {
                        attachments.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.error("", e1);
                    }
                } catch (final RuntimeException e) {
                    LOG1.error("", e);
                    try {
                        attachments.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.debug("", e1);
                    }
                } finally {
                    try {
                        attachments.finish();
                    } catch (final OXException e) {
                        LOG1.debug("", e);
                    }
                }
            }
        }

        @Override
        public String description() {
            return "Create dummy file for attachment";
        }

    }

    private static class CreateDummyFileForSnippet extends CreateDummyFile implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(CreateDummyFileForSnippet.class);

        public CreateDummyFileForSnippet(final FileStorage storage) {
            super(storage);
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            /*
             * Here we operate in two stages. First we create a dummy entry in the filestore. Second we update the Entries in the database
             */
            final int size = problems.size();
            final Iterator<String> it = problems.iterator();
            for (int k = 0; k < size; k++) {
                Connection con = null;
                PreparedStatement stmt = null;
                try {
                    con = Database.get(ctx, true);
                    final String old_identifier = it.next();
                    // Not recoverable
                    if (tableExists(con, "snippet")) {
                        stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND refId=? AND refType=1");
                        int pos = 0;
                        stmt.setInt(++pos, ctx.getContextId());
                        stmt.setString(++pos, old_identifier);
                        stmt.executeUpdate();
                        DBUtils.closeSQLStuff(stmt);
                        stmt = null;
                    }
                    // Partly recoverable
                    if (tableExists(con, "snippetAttachment")) {
                        final String identifier = createDummyFile();
                        stmt = con.prepareStatement("UPDATE snippetAttachment SET referenceId=? WHERE cid=? AND referenceId=?");
                        int pos = 0;
                        stmt.setString(++pos, identifier);
                        stmt.setInt(++pos, ctx.getContextId());
                        stmt.setString(++pos, old_identifier);
                        stmt.executeUpdate();
                        DBUtils.closeSQLStuff(stmt);
                        stmt = null;
                    }
                } catch (final SQLException e) {
                    LOG1.error("", e);
                } catch (final OXException e) {
                    LOG1.error("", e);
                } catch (final RuntimeException e) {
                    LOG1.error("", e);
                } finally {
                    DBUtils.closeSQLStuff(stmt);
                    if (null != con) {
                        Database.back(ctx, true, con);
                    }
                }
            }
        }

        @Override
        public String description() {
            return "Create dummy file for snippet";
        }

    }

    private static class RemoveFile implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(RemoveFile.class);

        private final FileStorage storage;

        public RemoveFile(final FileStorage storage) {
            this.storage = storage;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            try {
                for (final String identifier : problems) {
                    if (storage.deleteFile(identifier)) {
                        LOG1.info(MessageFormat.format("Deleted identifier: {0}", identifier));
                    }
                }
                /*
                 * Afterwards we recreate the state file because it could happen that that now new free file slots are available.
                 */
                storage.recreateStateFile();
            } catch (final OXException e) {
                LOG1.error("", e);
            }
        }

        @Override
        public String description() {
            return "delete file";
        }
    }

    private static class DeleteInfoitem implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(DeleteInfoitem.class);

        private final DatabaseImpl database;

        public DeleteInfoitem(final DatabaseImpl database) {
            this.database = database;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            // Now we go through the set an delete each superfluous entry:
            for (final String identifier : problems) {
                try {
                    database.startTransaction();
                    database.startDBTransaction();
                    database.setRequestTransactional(true);
                    final int[] numbers = database.removeDocument(identifier, ctx);
                    database.commit();
                    if (numbers[0] == 1) {
                        LOG1.info(MessageFormat.format("Have to change infostore version number for entry: {0}", identifier));
                    }
                    if (numbers[1] == 1) {
                        LOG1.info(MessageFormat.format("Deleted entry {0} from infostore_documents.", identifier));
                    }
                } catch (final OXException e) {
                    LOG1.error("", e);
                    try {
                        database.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.debug("", e1);
                    }
                } finally {
                    try {
                        database.finish();
                    } catch (final OXException e) {
                        LOG1.debug("", e);
                    }
                }
            }
        }

        @Override
        public String description() {
            return "delete infoitem";
        }
    }

    private static class DeleteAttachment implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(DeleteAttachment.class);

        private final AttachmentBase attachments;

        public DeleteAttachment(final AttachmentBase attachments) {
            this.attachments = attachments;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            // Now we go through the set an delete each superfluous entry:
            final Iterator<String> it = problems.iterator();
            while (it.hasNext()) {
                try {
                    final String identifier = it.next();
                    attachments.setTransactional(true);
                    attachments.startTransaction();
                    final int[] numbers = attachments.removeAttachment(identifier, ctx);
                    attachments.commit();
                    if (numbers[0] == 1) {
                        LOG1.info(MessageFormat.format("Inserted entry for identifier {0} and Context {1} in del_attachments", identifier, ctx.getContextId()));
                    }
                    if (numbers[1] == 1) {
                        LOG1.info(MessageFormat.format("Removed attachment database entry for: {0}", identifier));
                    }
                } catch (final OXException e) {
                    LOG1.debug("", e);
                    try {
                        attachments.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.debug("", e1);
                    }
                    return;
                } catch (final RuntimeException e) {
                    LOG1.error("", e);
                    try {
                        attachments.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG1.debug("", e1);
                    }
                    return;
                } finally {
                    try {
                        attachments.finish();
                    } catch (final OXException e) {
                        LOG1.debug("", e);
                    }
                }
            }
        }

        @Override
        public String description() {
            return "delete attachment";
        }
    }

    private static class DeleteSnippet implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(DeleteSnippet.class);

        public DeleteSnippet() {
            super();
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            // Now we go through the set an delete each superfluous entry:
            final Iterator<String> it = problems.iterator();
            while (it.hasNext()) {
                Connection con = null;
                PreparedStatement stmt = null;
                boolean rollback = false;
                try {
                    con = Database.get(ctx, true);
                    con.setAutoCommit(false);
                    rollback = true;

                    final int contextId = ctx.getContextId();
                    final String old_identifier = it.next();
                    // Not recoverable
                    if (tableExists(con, "snippet")) {
                        stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND refId=? AND refType=1");
                        int pos = 0;
                        stmt.setInt(++pos, contextId);
                        stmt.setString(++pos, old_identifier);
                        stmt.executeUpdate();
                        DBUtils.closeSQLStuff(stmt);
                        stmt = null;
                    }
                    // Partly recoverable
                    if (tableExists(con, "snippetAttachment")) {
                        final List<int[]> pairs = new LinkedList<int[]>();
                        {
                            ResultSet rs = null;
                            try {
                                stmt = con.prepareStatement("SELECT user, id FROM snippetAttachment WHERE cid=? AND referenceId=?");
                                int pos = 0;
                                stmt.setInt(++pos, contextId);
                                stmt.setString(++pos, old_identifier);
                                rs = stmt.executeQuery();
                                while (rs.next()) {
                                    pairs.add(new int[] { rs.getInt(1), rs.getInt(2) });
                                }
                            } finally {
                                DBUtils.closeSQLStuff(rs, stmt);
                            }
                        }
                        for (final int[] pair : pairs) {
                            final int userId=pair[0];
                            final int id=pair[1];
                            deleteSnippet(id, userId, contextId, con);
                        }
                    }
                    con.commit();
                    rollback = false;
                } catch (final SQLException e) {
                    LOG1.error("", e);
                } catch (final OXException e) {
                    LOG1.error("", e);
                } catch (final RuntimeException e) {
                    LOG1.error("", e);
                } finally {
                    if (rollback) {
                        DBUtils.rollback(con);
                    }
                    DBUtils.closeSQLStuff(stmt);
                    if (null != con) {
                        DBUtils.autocommit(con);
                        Database.back(ctx, true, con);
                    }
                }
            }
        }

        private void deleteSnippet(final int id, final int userId, final int contextId, final Connection con) {
            PreparedStatement stmt = null;
            try {
                // Delete attachments
                stmt = con.prepareStatement("DELETE FROM snippetAttachment WHERE cid=? AND user=? AND id=?");
                int pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setLong(++pos, id);
                stmt.executeUpdate();
                closeSQLStuff(stmt);
                stmt = null;
                // Delete content
                stmt = con.prepareStatement("DELETE FROM snippetContent WHERE cid=? AND user=? AND id=?");
                pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setLong(++pos, id);
                stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);
                stmt = null;
                // Delete JSON object
                stmt = con.prepareStatement("DELETE FROM snippetMisc WHERE cid=? AND user=? AND id=?");
                pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setLong(++pos, id);
                stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);
                stmt = null;
                // Delete unnamed properties
                final int confId;
                {
                    ResultSet rs = null;
                    try {
                        stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND user=? AND id=? AND refType=0");
                        pos = 0;
                        stmt.setLong(++pos, contextId);
                        stmt.setLong(++pos, userId);
                        stmt.setString(++pos, Integer.toString(id));
                        rs = stmt.executeQuery();
                        confId = rs.next() ? Integer.parseInt(rs.getString(1)) : -1;
                    } finally {
                        closeSQLStuff(rs, stmt);
                        stmt = null;
                        rs = null;
                    }
                }
                if (confId > 0) {
                    stmt = con.prepareStatement("DELETE FROM genconf_attributes_strings WHERE cid = ? AND id = ?");
                    pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, confId);
                    stmt.executeUpdate();
                    DBUtils.closeSQLStuff(stmt);
                    stmt = con.prepareStatement("DELETE FROM genconf_attributes_bools WHERE cid = ? AND id = ?");
                    pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, confId);
                    stmt.executeUpdate();
                    DBUtils.closeSQLStuff(stmt);
                }
                // Delete snippet
                stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType=0");
                pos = 0;
                stmt.setLong(++pos, contextId);
                stmt.setLong(++pos, userId);
                stmt.setString(++pos, Integer.toString(id));
                stmt.executeUpdate();
                DBUtils.closeSQLStuff(stmt);
                stmt = null;
            } catch (final SQLException e) {
                LOG1.error("", e);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }

        @Override
        public String description() {
            return "delete snippet";
        }
    }

    private static class CreateInfoitem implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(CreateInfoitem.class);

        private static final String description = "This file needs attention";

        private static final String title = "Restoredfile";

        private static final String fileName = "Restoredfile";

        private static final String versioncomment = "";

        private static final String categories = "";

        private final DatabaseImpl database;

        private final FileStorage storage;

        private final Consistency consistency;

        public CreateInfoitem(final DatabaseImpl database, final FileStorage storage, final Consistency consistency) {
            this.database = database;
            this.storage = storage;
            this.consistency = consistency;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            try {
                final User user = consistency.getAdmin(ctx);
                final DocumentMetadata document = new DocumentMetadataImpl();
                document.setDescription(description);
                document.setTitle(title);
                document.setFileName(fileName);
                document.setVersionComment(versioncomment);
                document.setCategories(categories);

                for (final String identifier : problems) {
                    try {
                        document.setFileSize(storage.getFileSize(identifier));
                        document.setFileMIMEType(storage.getMimeType(identifier));
                        database.startTransaction();
                        final int[] numbers = database.saveDocumentMetadata(identifier, document, user, ctx);
                        database.commit();
                        if (numbers[2] == 1) {
                            LOG1.info(MessageFormat.format("Dummy entry for {0} in database created. The admin of this context has now a new document", identifier));
                        }
                    } catch (final OXException e) {
                        LOG1.error("", e);
                        try {
                            database.rollback();
                            return;
                        } catch (final OXException e1) {
                            LOG1.debug("", e1);
                        }
                    } catch (final RuntimeException e) {
                        LOG1.error("", e);
                        try {
                            database.rollback();
                            return;
                        } catch (final OXException e1) {
                            LOG1.debug("", e1);
                        }
                    } finally {
                        try {
                            database.finish();
                        } catch (final OXException e) {
                            LOG1.debug("", e);
                        }
                    }
                }

            } catch (final OXException e) {
                LOG1.error("", e);
            }
        }

        @Override
        public String description() {
            return "create infoitem";
        }
    }

    private static class DeleteBrokenPreviewReferences implements ProblemSolver {

        private static final org.slf4j.Logger LOG1 =
            org.slf4j.LoggerFactory.getLogger(DeleteBrokenPreviewReferences.class);

        @Override
        public void solve(Context ctx, Set<String> problems) throws OXException {
            if (problems.size() > 0) {
                ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
                metadataStore.removeByRefId(ctx.getContextId(), problems);
                LOG1.info("Deleted {} broken preview cache references.", problems.size());
            }
        }

        @Override
        public String description() {
            return "delete broken preview references";
        }

    }

    protected static boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equals(table));
        } finally {
            DBUtils.closeSQLStuff(rs);
        }
        return retval;
    }

}
