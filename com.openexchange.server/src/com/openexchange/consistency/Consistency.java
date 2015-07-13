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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.management.MBeanException;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.config.ConfigurationService;
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
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
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
 */
public abstract class Consistency implements ConsistencyMBean {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Consistency.class);

    /**
     * Initializes a new {@link Consistency}.
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
            checkOneContext(ctx, recorder, recorder, recorder, recorder, doNothing, recorder, getDatabase(), getAttachments(), getFileStorages(ctx));
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
            checkOneContext(ctx, doNothing, doNothing, doNothing, doNothing, recorder, doNothing, getDatabase(), getAttachments(), getFileStorages(ctx));
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
            for (final String schema : schemaMap.keySet()) {
                List<Integer> ctxs = schemaMap.get(schema);
                Integer poolid = schemaPoolMap.get(schema);
                poolCon = Database.get(poolid.intValue(), schema);
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
                Database.back(poolid.intValue(), poolCon);
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
            checkOneContext(ctx, recorder, recorder, recorder, recorder, doNothing, recorder, getDatabase(), getAttachments(), getFileStorages(ctx));
            retval.put(Integer.valueOf(ctx.getContextId()), recorder.getProblems());
        }
        return retval;
    }

    private Map<Integer, List<String>> listUnassigned(final List<Context> contexts) throws OXException {
        final Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for (final Context ctx : contexts) {
            final RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx, doNothing, doNothing, doNothing, doNothing, recorder, doNothing, getDatabase(), getAttachments(), getFileStorages(ctx));
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
            List<FileStorage> storages = getFileStorages(ctx);

            final ResolverPolicy resolvers = ResolverPolicy.parse(policy, database, attachments, storages, this, ctx);
            checkOneContext(ctx, resolvers.dbsolver, resolvers.attachmentsolver, resolvers.snippetsolver, new DeleteBrokenPreviewReferencesSolver(), resolvers.filesolver, resolvers.vCardSolver, database, attachments, storages);

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
            recalculateUsage(storages, filesToIgnore);
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

    private void checkOneContext(final Context ctx, final ProblemSolver dbSolver, final ProblemSolver attachmentSolver, final ProblemSolver snippetSolver, final ProblemSolver previewSolver, final ProblemSolver fileSolver, final ProblemSolver vCardSolver, final DatabaseImpl database, final AttachmentBase attach, final List<FileStorage> storages) throws OXException {

        // We believe in the worst case, so lets check the storage first, so
        // that the state file is recreated
        LOG.info("Checking context {}. Using solvers db: {} attachments: {} snippets: {} files: {} vcards: {}", ctx.getContextId(), dbSolver.description(), attachmentSolver.description(), snippetSolver.description(), fileSolver.description(), vCardSolver.description());
        for (FileStorage stor : storages) {
            try {
                stor.recreateStateFile();
            } catch (OXException e) {
                if (FileStorageCodes.NO_SUCH_FILE_STORAGE.equals(e)) {
                    // Does not (yet) exist
                    Object[] logArgs = e.getLogArgs();
                    LOG.info("Cannot check files in filestore{} for context {} since associated filestore does not (yet) exist: {}", ((stor instanceof QuotaFileStorage) ? " "+((QuotaFileStorage) stor).getUri() : ""), ctx.getContextId(), null == logArgs || 0 == logArgs.length ? e.getMessage() : logArgs[0].toString());
                    return;
                }

                throw e;
            }
        }

        // Get files residing in file storages
        LOG.info("Listing all files in filestores");
        SortedSet<String> filestoreset = new TreeSet<String>();
        for (FileStorage stor : storages) {
            filestoreset.addAll(stor.getFileList());
        }
        LOG.info("Found {} files in the filestore for this context", filestoreset.size());

        // Get the referenced ones
        SortedSet<String> attachmentset = attach.getAttachmentFileStoreLocationsperContext(ctx);
        LOG.info("Found {} attachments", attachmentset.size());

        SortedSet<String> snippetset = getSnippetFileStoreLocationsperContext(ctx);
        LOG.info("Found {} snippets", snippetset.size());

        SortedSet<String> previewset = getPreviewCacheFileStoreLocationsperContext(ctx);
        LOG.info("Found {} previews", previewset.size());

        SortedSet<String> vcardset = getVCardFileStoreLocationsperContext(ctx);
        LOG.info("Found {} vCards", vcardset.size());

        try {
            LOG.info("Loading all infostore filestore locations");
            SortedSet<String> dbfileset = database.getDocumentFileStoreLocationsperContext(ctx);
            LOG.info("Found {} infostore filepaths", dbfileset.size());

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

            if (diffset(vcardset, filestoreset, "database list of VCard files", "filestore list")) {
                vCardSolver.solve(ctx, vcardset);
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

    private SortedSet<String> getVCardFileStoreLocationsperContext(Context ctx) throws OXException {
        VCardStorageMetadataStore vCardStorageMetadataStore = ConsistencyServiceLookup.getOptionalService(VCardStorageMetadataStore.class);
        if (vCardStorageMetadataStore != null) {
            Set<String> loadRefIds = vCardStorageMetadataStore.loadRefIds(ctx.getContextId());
            return new TreeSet<String>(loadRefIds);
        }
        return new TreeSet<String>();
    }

    private SortedSet<String> getPreviewCacheFileStoreLocationsperContext(Context ctx) throws OXException {
        ResourceCacheMetadataStore metadataStore = ResourceCacheMetadataStore.getInstance();
        Set<String> refIds = metadataStore.loadRefIds(ctx.getContextId());
        return new TreeSet<String>(refIds);
    }

    private SortedSet<String> getSnippetFileStoreLocationsperContext(Context ctx) throws OXException {
        final SortedSet<String> retval = new TreeSet<String>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = Database.get(ctx, false);
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
                Database.back(ctx, false, con);
            }
        }
        return retval;
    }

    private void recalculateUsage(final List<FileStorage> storages, final Set<String> filesToIgnore) {
        for (FileStorage storage : storages) {
            try {
                if (storage instanceof QuotaFileStorage) {
                    output("Recalculating usage...");
                    ((QuotaFileStorage) storages).recalculateUsage(filesToIgnore);
                }
            } catch (final OXException e) {
                erroroutput(e);
            }
        }
    }

    protected abstract Context getContext(int contextId) throws OXException;

    protected abstract DatabaseImpl getDatabase();

    protected abstract AttachmentBase getAttachments();

    protected abstract List<FileStorage> getFileStorages(Context ctx) throws OXException;

    protected abstract List<Context> getContextsForFilestore(int filestoreId) throws OXException;

    protected abstract List<Context> getContextsForDatabase(int datbaseId) throws OXException;

    protected abstract List<Context> getAllContexts() throws OXException;

    protected abstract User getAdmin(Context ctx) throws OXException;

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

        public static ResolverPolicy parse(final String list, final DatabaseImpl database, final AttachmentBase attach, final List<FileStorage> storages, final Consistency consistency, final Context context) throws OXException {
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
                        dbsolver = new CreateDummyFileForInfoitemSolver(database, storages);
                    } else if ("delete".equals(action)) {
                        dbsolver = new DeleteInfoitemSolver(database);
                    } else {
                        dbsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_attachment".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        attachmentsolver = new CreateDummyFileForAttachmentSolver(attach, storages);
                    } else if ("delete".equals(action)) {
                        attachmentsolver = new DeleteAttachmentSolver(attach);
                    } else {
                        attachmentsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_snippet".equals(condition)) {
                    if ("create_dummy".equals(action)) {
                        snippetsolver = new CreateDummyFileForSnippetSolver(storages);
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
                        filesolver = new CreateInfoitemSolver(database, storages, consistency.getAdmin(context));
                    } else if ("delete".equals(action)) {
                        filesolver = new RemoveFileSolver(storages);
                    } else {
                        filesolver = new DoNothingSolver();
                    }
                }
            }

            return new ResolverPolicy(dbsolver, attachmentsolver, snippetsolver, filesolver, vCardSolver);
        }

    }
}
