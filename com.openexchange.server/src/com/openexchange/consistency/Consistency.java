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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;

/**
 * Provides the Business Logic for the consistency tool. Concrete subclasses must provide integration
 * to the environment by implementing the abstract methods.
 *
 * @author Dennis Sieben
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class Consistency implements ConsistencyMBean {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Consistency.class));

    @Override
    public List<String> listMissingFilesInContext(final int contextId) throws OXException {
        LOG.info("Listing missing files in context "+contextId);
        final DoNothingSolver doNothing = new DoNothingSolver();
        final RecordSolver recorder = new RecordSolver();
        final Context ctx = getContext(contextId);
        checkOneContext(ctx,recorder,recorder,doNothing,getDatabase(),getAttachments(), getFileStorage(ctx));
        return recorder.getProblems();
    }


    @Override
    public Map<Integer, List<String>> listMissingFilesInFilestore(final int filestoreId) throws OXException {
        LOG.info("Listing missing files in filestore "+filestoreId);
        return listMissing(getContextsForFilestore(filestoreId));
    }


    @Override
    public Map<Integer, List<String>> listMissingFilesInDatabase(final int databaseId) throws OXException {
        LOG.info("List missing files in database "+databaseId);
        return listMissing(getContextsForDatabase(databaseId));
    }

    @Override
    public Map<Integer, List<String>> listAllMissingFiles() throws OXException {
        LOG.info("List all missing files");
        return listMissing(getAllContexts());
    }

    @Override
    public List<String> listUnassignedFilesInContext(final int contextId) throws OXException {
        LOG.info("List all unassigned files in context "+contextId);
        final DoNothingSolver doNothing = new DoNothingSolver();
        final RecordSolver recorder = new RecordSolver();
        final Context ctx = getContext(contextId);
        checkOneContext(ctx,doNothing,doNothing,recorder,getDatabase(),getAttachments(), getFileStorage(ctx));
        return recorder.getProblems();
    }

    @Override
    public Map<Integer, List<String>> listUnassignedFilesInFilestore(final int filestoreId) throws OXException {
        LOG.info("List all unassigned files in filestore "+filestoreId);
        return listUnassigned(getContextsForFilestore(filestoreId));
    }

    @Override
    public Map<Integer, List<String>> listUnassignedFilesInDatabase(final int databaseId) throws OXException {
        LOG.info("List all unassigned files in database "+databaseId);
        return listUnassigned(getContextsForDatabase(databaseId));
    }

    @Override
    public Map<Integer, List<String>> listAllUnassignedFiles() throws OXException {
        LOG.info("List all unassigned files");
        return listUnassigned(getAllContexts());
    }

    private Map<Integer, List<String>> listMissing(final List<Context> contexts) throws OXException {
        final Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for(final Context ctx : contexts) {
            final RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx,recorder,recorder,doNothing,getDatabase(),getAttachments(), getFileStorage(ctx));
            retval.put(Integer.valueOf(ctx.getContextId()), recorder.getProblems());
        }
        return retval;
    }

    private Map<Integer, List<String>> listUnassigned(final List<Context> contexts) throws OXException {
        final Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        final DoNothingSolver doNothing = new DoNothingSolver();
        for(final Context ctx : contexts) {
            final RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx,doNothing,doNothing,recorder,getDatabase(),getAttachments(), getFileStorage(ctx));
            retval.put(Integer.valueOf(ctx.getContextId()), recorder.getProblems());
        }
        return retval;
    }

    //Repair


    @Override
    public void repairFilesInContext(final int contextId, final String resolverPolicy) throws OXException {
        final List<Context> repairMe = new ArrayList<Context>();
        repairMe.add(getContext(contextId));
        repair(repairMe, resolverPolicy);
    }

    @Override
    public void repairFilesInFilestore(final int filestoreId, final String resolverPolicy) throws OXException {
        repair(getContextsForFilestore(filestoreId), resolverPolicy);
    }

    @Override
    public void repairFilesInDatabase(final int databaseId, final String resolverPolicy) throws OXException {
        repair(getContextsForDatabase(databaseId), resolverPolicy);
    }

    @Override
    public void repairAllFiles(final String resolverPolicy) throws OXException {
        repair(getAllContexts(), resolverPolicy);
    }

    private void repair(final List<Context> contexts, final String policy) throws OXException {
        final DatabaseImpl database = getDatabase();
        final AttachmentBase attachments = getAttachments();
        for(final Context ctx : contexts) {
            final FileStorage storage = getFileStorage(ctx);

            final ResolverPolicy resolvers = ResolverPolicy.parse(policy,database,attachments,storage,this);

            checkOneContext(ctx,resolvers.dbsolver, resolvers.attachmentsolver, resolvers.filesolver, database,attachments, storage);

            recalculateUsage(storage);
        }
    }

    // Taken from original consistency tool //

    private void output(final String text) {
        if (LOG.isInfoEnabled()) {
            LOG.info(text);
        }
    }

    private void erroroutput(final Exception e) {
        LOG.error(e.getMessage(), e);
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
     *
     */
    private boolean diffset(final SortedSet<String> first,
            final SortedSet<String> second, final String name, final String name2) {
        boolean retval = false;
        first.removeAll(second);
        if (!first.isEmpty()) {
            output("Inconsistencies found in " + name + ", the following files aren't in " + name2 + ':');
            outputSet(first);
            retval = true;
        }
        return retval;
    }

    private void checkOneContext(final Context ctx, final ProblemSolver dbSolver, final ProblemSolver attachmentSolver, final ProblemSolver fileSolver, final DatabaseImpl database, final AttachmentBase attach, final FileStorage stor) throws OXException {

        // We believe in the worst case, so lets check the storage first, so
        // that the state file is recreated
        LOG.info("Checking context "+ctx.getContextId()+". Using solvers db: "+dbSolver.description()+" attachments: "+attachmentSolver.description()+" files: "+fileSolver.description());
        stor.recreateStateFile();

        LOG.info("Listing all files in filestore");
        final SortedSet<String> filestoreset = stor.getFileList();
        LOG.info("Found "+filestoreset.size()+" files in the filestore for this context");
        LOG.info("Loading all attachments");
        final SortedSet<String> attachmentset =
            attach.getAttachmentFileStoreLocationsperContext(ctx);
        LOG.info("Found "+attachmentset.size()+" attachments");
        SortedSet<String> dbfileset;
        try {
            LOG.info("Loading all infostore filestore locations");
            dbfileset = database.getDocumentFileStoreLocationsperContext(ctx);
            LOG.info("Found "+dbfileset.size()+" infostore filepaths");
            final SortedSet<String> joineddbfileset = new TreeSet<String>(dbfileset);
            joineddbfileset.addAll(attachmentset);

            LOG.info("Found "+joineddbfileset.size()+" filestore ids in total. There are "+filestoreset.size()+" files in the filespool. A difference of "+Math.abs(joineddbfileset.size()-filestoreset.size()));


            // Build the difference set of the database set, so that the final
            // dbfileset contains all the members that aren't in the filestoreset
            if (diffset(dbfileset, filestoreset, "database list",
                    "filestore list")) {
                // implement the solver for dbfiles here
                dbSolver.solve(ctx,dbfileset);
            }


            // Build the difference set of the attachment database set, so that the
            // final attachmentset contains all the members that aren't in the
            // filestoreset
            if (diffset(attachmentset, filestoreset,
                    "database list of attachment files", "filestore list")) {
                //implement the solver for deleted dbfiles here
                attachmentSolver.solve(ctx, attachmentset);
            }

            // Build the difference set of the filestore set, so that the final
            // filestoreset contains all the members that aren't in the dbfileset or
            // the dbdelfileset
            if (diffset(filestoreset, joineddbfileset, "filestore list",
                    "one of the databases")) {
                //implement the solver for the filestore here
                fileSolver.solve(ctx, filestoreset);
            }

        } catch (final OXException e) {
            erroroutput(e);
        }
    }

    private void recalculateUsage(final FileStorage storage) {
        try {
            if (storage instanceof QuotaFileStorage) {
                output("Recalculating usage...");
                ((QuotaFileStorage)storage).recalculateUsage();
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
        final ProblemSolver filesolver;

        public ResolverPolicy(final ProblemSolver dbsolver, final ProblemSolver attachmentsolver, final ProblemSolver filesolver) {
            this.dbsolver = dbsolver;
            this.attachmentsolver = attachmentsolver;
            this.filesolver = filesolver;
        }

        public static ResolverPolicy parse(final String list, final DatabaseImpl database, final AttachmentBase attach, final FileStorage stor, final Consistency consistency) throws OXException {
            final String[] options = list.split("\\s*,\\s*");
            ProblemSolver dbsolver = new DoNothingSolver();
            ProblemSolver attachmentsolver = new DoNothingSolver();
            ProblemSolver filesolver = new DoNothingSolver();

            for(final String option : options) {
                final String[] tuple = option.split("\\s*:\\s*");
                if(tuple.length!=2){
                	throw ConsistencyExceptionCodes.MALFORMED_POLICY.create();
                }
                final String condition = tuple[0];
                final String action = tuple[1];
                if("missing_file_for_infoitem".equals(condition)) {
                    if("create_dummy".equals(action)) {
                        dbsolver = new CreateDummyFileForInfoitem(database, stor);
                    } else if ("delete".equals(action)) {
                        dbsolver = new DeleteInfoitem(database);
                    } else {
                        dbsolver = new DoNothingSolver();
                    }
                } else if ("missing_file_for_attachment".equals(condition)) {
                    if("create_dummy".equals(action)) {
                       attachmentsolver = new CreateDummyFileForAttachment(attach,stor);
                    } else if ("delete".equals(action)) {
                       attachmentsolver = new DeleteAttachment(attach);
                    } else {
                        attachmentsolver = new DoNothingSolver();
                    }
                } else if ("missing_entry_for_file".equals(condition)) {
                    if("create_admin_infoitem".equals(action)) {
                       filesolver = new CreateInfoitem(database,stor,consistency);
                    } else if ("delete".equals(action)) {
                       filesolver = new RemoveFile(stor);
                    } else {
                       filesolver = new DoNothingSolver();
                    }
                }
            }

            return new ResolverPolicy(dbsolver, attachmentsolver, filesolver);
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

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CreateDummyFileForInfoitem.class));

        private final DatabaseImpl database;

        public CreateDummyFileForInfoitem(final DatabaseImpl database, final FileStorage storage) {
            super(storage);
            this.database = database;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            /*
            * Here we operate in two stages. First we create a dummy entry in the
            * filestore. Second we update the Entries in the database
            */
            for (final String old_identifier : problems) {
                try {
                    final String identifier = createDummyFile();
                    database.startTransaction();
                    final int changed = database.modifyDocument(old_identifier,
                            identifier, "\nCaution! The file has changed",
                            "text/plain", ctx);
                    database.commit();
                    if (changed == 1 && LOG1.isInfoEnabled()) {
                        LOG1.info("Modified entry for identifier " + old_identifier +
                                " in context " + ctx.getContextId() + " to new " +
                                "dummy identifier " + identifier);
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

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CreateDummyFileForAttachment.class));

        private final AttachmentBase attachments;

        public CreateDummyFileForAttachment(final AttachmentBase attachments, final FileStorage storage) {
            super(storage);
            this.attachments = attachments;
        }


        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            /*
            * Here we operate in two stages. First we create a dummy entry in the
            * filestore. Second we update the Entries in the database
            */
            final int size = problems.size();
            final Iterator<String> it = problems.iterator();
            for (int k = 0; k < size; k++) {
                try {
                    final String identifier = createDummyFile();
                    final String old_identifier = it.next();
                    attachments.setTransactional(true);
                    attachments.startTransaction();
                    final int changed = attachments.modifyAttachment(old_identifier, identifier,
                            "\nCaution! The file has changed", "text/plain", ctx);
                    attachments.commit();
                    if (changed == 1 && LOG1.isInfoEnabled()) {
                        LOG1.info("Created dummy entry for: " + old_identifier +
                                ". New identifier is: " + identifier);
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

    private static class RemoveFile implements ProblemSolver {

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(RemoveFile.class));

        private final FileStorage storage;

        public RemoveFile(final FileStorage storage) {
            this.storage = storage;
        }

        @Override
        public void solve(final Context ctx, final Set<String> problems) throws OXException {
            try {
                for (final String identifier : problems) {
                    if (storage.deleteFile(identifier) && LOG1.isInfoEnabled()) {
                        LOG1.info("Deleted identifier: " + identifier);
                    }
                }
                /* Afterwards we recreate the state file because it could happen that
             * that now new free file slots are available.
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

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DeleteInfoitem.class));

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
                    if (numbers[0] == 1 && LOG1.isInfoEnabled()) {
                        LOG1.info("Have to change infostore version number " +
                                "for entry: " + identifier);
                    }
                    if (numbers[1] == 1 && LOG1.isInfoEnabled()) {
                        LOG1.info("Deleted entry " + identifier + " from " +
                                "infostore_documents.");
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

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DeleteAttachment.class));

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
                    if (numbers[0] ==  1 && LOG1.isInfoEnabled()) {
                        LOG1.info("Inserted entry for identifier " + identifier + " and Context " + ctx.getContextId()
                                + " in " + "del_attachments");
                    }
                    if (numbers[1] == 1 && LOG1.isInfoEnabled()) {
                        LOG1.info("Removed attachment database entry for: " + identifier);
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

    private static class CreateInfoitem implements ProblemSolver {

        private static final org.apache.commons.logging.Log LOG1 = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CreateInfoitem.class));

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
                        if (numbers[2] == 1 && LOG1.isInfoEnabled()) {
                            LOG1.info("Dummy entry for " + identifier + " in database " +
                                    "created. The admin of this context has now " +
                                    "a new document");
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
}
