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
package com.openexchange.consistency;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.api2.OXException;
import com.openexchange.server.impl.DBPoolingException;

/**
 * Provides the Business Logic for the consistency tool. Concrete subclasses must provide integration
 * to the environment by implementing the abstract methods.
 *
 * @author Dennis Sieben
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class Consistency implements ConsistencyMBean {

    private static final Log LOG = LogFactory.getLog(Consistency.class);

    public List<String> listMissingFilesInContext(int contextId) throws AbstractOXException {
        DoNothingSolver doNothing = new DoNothingSolver();
        RecordSolver recorder = new RecordSolver();
        Context ctx = getContext(contextId);
        checkOneContext(ctx,recorder,recorder,doNothing,getDatabase(),getAttachments(), getFileStorage(ctx));
        return recorder.getProblems();
    }


    public Map<Integer, List<String>> listMissingFilesInFilestore(int filestoreId) throws AbstractOXException {
        return listMissing(getContextsForFilestore(filestoreId));
    }


    public Map<Integer, List<String>> listMissingFilesInDatabase(int databaseId) throws AbstractOXException {
        return listMissing(getContextsForDatabase(databaseId));
    }

    public Map<Integer, List<String>> listAllMissingFiles() throws AbstractOXException {
        return listMissing(getAllContexts());
    }

    public List<String> listUnassignedFilesInContext(int contextId) throws AbstractOXException {
        DoNothingSolver doNothing = new DoNothingSolver();
        RecordSolver recorder = new RecordSolver();
        Context ctx = getContext(contextId);
        checkOneContext(ctx,doNothing,doNothing,recorder,getDatabase(),getAttachments(), getFileStorage(ctx));
        return recorder.getProblems();
    }

    public Map<Integer, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws AbstractOXException {
        return listUnassigned(getContextsForFilestore(filestoreId));
    }

    public Map<Integer, List<String>> listUnassignedFilesInDatabase(int databaseId) throws AbstractOXException {
        return listUnassigned(getContextsForDatabase(databaseId));
    }

    public Map<Integer, List<String>> listAllUnassignedFiles() throws AbstractOXException {
        return listUnassigned(getAllContexts());
    }

    private Map<Integer, List<String>> listMissing(List<Context> contexts) throws AbstractOXException {
        Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        DoNothingSolver doNothing = new DoNothingSolver();
        for(Context ctx : contexts) {
            RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx,recorder,recorder,doNothing,getDatabase(),getAttachments(), getFileStorage(ctx));
            retval.put(ctx.getContextId(), recorder.getProblems());
        }
        return retval;
    }

    private Map<Integer, List<String>> listUnassigned(List<Context> contexts) throws AbstractOXException {
        Map<Integer, List<String>> retval = new HashMap<Integer, List<String>>();
        DoNothingSolver doNothing = new DoNothingSolver();
        for(Context ctx : contexts) {
            RecordSolver recorder = new RecordSolver();
            checkOneContext(ctx,doNothing,doNothing,recorder,getDatabase(),getAttachments(), getFileStorage(ctx));
            retval.put(ctx.getContextId(), recorder.getProblems());
        }
        return retval;
    }

    //Repair


    public void repairFilesInContext(int contextId, String resolverPolicy) throws AbstractOXException {
        List<Context> repairMe = new ArrayList<Context>();
        repairMe.add(getContext(contextId));
        repair(repairMe, resolverPolicy);
    }

    public void repairFilesInFilestore(int filestoreId, String resolverPolicy) throws AbstractOXException {
        repair(getContextsForFilestore(filestoreId), resolverPolicy);
    }

    public void repairFilesInDatabase(int databaseId, String resolverPolicy) throws AbstractOXException {
        repair(getContextsForDatabase(databaseId), resolverPolicy);
    }

    public void repairAllFiles(String resolverPolicy) throws AbstractOXException {
        repair(getAllContexts(), resolverPolicy);
    }

    private void repair(List<Context> contexts, String policy) throws AbstractOXException {
        DatabaseImpl database = getDatabase();
        AttachmentBase attachments = getAttachments();
        for(Context ctx : contexts) {
            FileStorage storage = getFileStorage(ctx);

            ResolverPolicy resolvers = ResolverPolicy.parse(policy,database,attachments,storage,this);

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
	    LOG.debug(e.getMessage(), e);
	}

	private void outputSet(final SortedSet<String> set) {
	    final Iterator<String> itstr = set.iterator();
	    StringBuilder sb = new StringBuilder();
        while (itstr.hasNext()) {
	        sb.append(itstr.next()).append("\n");
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

	private void checkOneContext(final Context ctx, ProblemSolver dbSolver, ProblemSolver attachmentSolver, ProblemSolver fileSolver, DatabaseImpl database, AttachmentBase attach, FileStorage stor) throws AbstractOXException {
		// We believe in the worst case, so lets check the storage first, so
		// that the state file is recreated
		stor.recreateStateFile();
    
		final SortedSet<String> filestoreset = stor.getFileList();
		final SortedSet<String> attachmentset =
			attach.getAttachmentFileStoreLocationsperContext(ctx);
		SortedSet<String> dbfileset;
		try {
			dbfileset = database.getDocumentFileStoreLocationsperContext(ctx);
			final SortedSet<String> joineddbfileset = new TreeSet<String>(dbfileset);
			joineddbfileset.addAll(attachmentset);


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

		} catch (OXException e) {
			erroroutput(e);
		}
	}

    private void recalculateUsage(FileStorage storage) {
        try {
            if (storage instanceof QuotaFileStorage) {
                output("Recalculating usage...");
                ((QuotaFileStorage)storage).recalculateUsage();
            }
        } catch (FileStorageException e) {
            erroroutput(e);
        }
    }

    protected abstract Context getContext(int contextId) throws ContextException;
    protected abstract DatabaseImpl getDatabase();
    protected abstract AttachmentBase getAttachments();
    protected abstract FileStorage getFileStorage(Context ctx) throws FileStorageException, FilestoreException;
    protected abstract List<Context> getContextsForFilestore(int filestoreId) throws ContextException;
    protected abstract List<Context> getContextsForDatabase(int datbaseId) throws ContextException, DBPoolingException;
    protected abstract List<Context> getAllContexts() throws ContextException;
    protected abstract User getAdmin(Context ctx) throws LdapException;
        


    private static final class ResolverPolicy {
        private ProblemSolver dbsolver;
        private ProblemSolver attachmentsolver;
        private ProblemSolver filesolver;

        public ResolverPolicy(ProblemSolver dbsolver, ProblemSolver attachmentsolver, ProblemSolver filesolver) {
            this.dbsolver = dbsolver;
            this.attachmentsolver = attachmentsolver;
            this.filesolver = filesolver;
        }

        public static ResolverPolicy parse(String list, DatabaseImpl database, AttachmentBase attach, FileStorage stor, Consistency consistency) {
            String[] options = list.split("\\s*,\\s*");
            ProblemSolver dbsolver = new DoNothingSolver();
            ProblemSolver attachmentsolver = new DoNothingSolver();
            ProblemSolver filesolver = new DoNothingSolver();

            for(String option : options) {
                String[] tuple = option.split("\\s*:\\s*");
                String condition = tuple[0];
                String action = tuple[1];
                if(condition.equals("missing_file_for_infoitem")) {
                    if(action.equals("create_dummy")) {
                        dbsolver = new CreateDummyFileForInfoitem(database, stor);
                    } else if (action.equals("delete")) {
                        dbsolver = new DeleteInfoitem(database);
                    } else {
                        dbsolver = new DoNothingSolver();
                    }
                } else if (condition.equals("missing_file_for_attachment")) {
                    if(action.equals("create_dummy")) {
                       attachmentsolver = new CreateDummyFileForAttachment(attach,stor);
                    } else if (action.equals("delete")) {
                       attachmentsolver = new DeleteAttachment(attach);
                    } else {
                        attachmentsolver = new DoNothingSolver();
                    }
                } else if (condition.equals("missing_entry_for_file")) {
                    if(action.equals("create_admin_infoitem")) {
                       filesolver = new CreateInfoitem(database,stor,consistency);
                    } else if (action.equals("delete")) {
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
        public void solve(Context ctx, Set<String> problems) throws OXException ;
    }

    private static class DoNothingSolver implements ProblemSolver {

        public void solve(Context ctx, Set<String> problems) throws OXException {
            // Ignore
        }
    }

    private static class RecordSolver implements ProblemSolver {

        private List<String> memory = new ArrayList<String>();

        public void solve(Context ctx, Set<String> problems) throws OXException {
            memory.addAll(problems);
        }

        public List<String> getProblems() {
            return memory;
        }
    }

    private static class CreateDummyFile {

        private FileStorage storage;
        
        public CreateDummyFile(FileStorage storage) {
            this.storage = storage;
        }

        /**
         * This method create a dummy file a returns its name
         * @return The name of the dummy file
         * @throws FileStorageException
         */
        protected String createDummyFile() throws FileStorageException {
            final String filetext = "This is just a dummy file";
            final InputStream input = new ByteArrayInputStream(filetext.getBytes());

            return storage.saveNewFile(input);
        }
    }

    private static class CreateDummyFileForInfoitem extends CreateDummyFile implements ProblemSolver {
        private DatabaseImpl database;

        public CreateDummyFileForInfoitem(DatabaseImpl database, FileStorage storage) {
            super(storage);
            this.database = database;
        }

        public void solve(Context ctx, Set<String> problems) throws OXException {
            /*
		    * Here we operate in two stages. First we create a dummy entry in the
		    * filestore. Second we update the Entries in the database
		    */
            for (String old_identifier : problems) {
                try {
                    final String identifier = createDummyFile();
                    database.setTransactional(true);
                    database.startTransaction();
                    final int changed = database.modifyDocument(old_identifier,
                            identifier, "\nCaution! The file has changed",
                            "text/plain", ctx);
                    database.commit();
                    if (changed == 1 && LOG.isInfoEnabled()) {
                        LOG.info("Modified entry for identifier " + old_identifier +
                                " in context " + ctx.getContextId() + " to new " +
                                "dummy identifier " + identifier);
                    }
                } catch (FileStorageException e) {
                    LOG.debug("", e);
                } catch (OXException e) {
                    LOG.debug("", e);
                    try {
                        database.rollback();
                        return;
                    } catch (TransactionException e1) {
                        LOG.debug("", e1);
                    }
                } finally {
                    try {
                        database.finish();
                    } catch (TransactionException e) {
                        LOG.debug("", e);
                    }
                }
            }
        }
    }

    private static class CreateDummyFileForAttachment extends CreateDummyFile implements ProblemSolver {
        private AttachmentBase attachments;

        public CreateDummyFileForAttachment(AttachmentBase attachments, FileStorage storage) {
            super(storage);
            this.attachments = attachments;
        }


        public void solve(Context ctx, Set<String> problems) throws OXException {
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
				if (changed == 1 && LOG.isInfoEnabled()) {
					LOG.info("Created dummy entry for: " + old_identifier +
							". New identifier is: " + identifier);
				}
			} catch (FileStorageException e) {
				LOG.debug("", e);
			} catch (TransactionException e) {
				LOG.debug("", e);
				try {
					attachments.rollback();
					return;
				} catch (TransactionException e1) {
					LOG.debug("", e1);
				}
			} catch (OXException e) {
				LOG.debug("", e);
				try {
					attachments.rollback();
					return;
				} catch (TransactionException e1) {
					LOG.debug("", e1);
				}
			} finally {
				try {
					attachments.finish();
				} catch (TransactionException e) {
					LOG.debug("", e);
				}
			}
		}
        }

    }

    private static class RemoveFile implements ProblemSolver {

        private FileStorage storage = null;

        public RemoveFile(FileStorage storage) {
            this.storage = storage;
        }

        public void solve(Context ctx, Set<String> problems) throws OXException {
            try {
                for (String identifier : problems) {
                    if (storage.deleteFile(identifier) == true && LOG.isInfoEnabled()) {
                        LOG.info("Deleted identifier: " + identifier);
                    }
                }
                /* Afterwards we recreate the state file because it could happen that
			 * that now new free file slots are available.
			 */
                storage.recreateStateFile();
            } catch (FileStorageException e) {
                LOG.debug("", e);
            }
        }
    }

    private static class DeleteInfoitem implements ProblemSolver {

        private DatabaseImpl database = null;

        public DeleteInfoitem(DatabaseImpl database) {
            this.database = database;
        }

        public void solve(Context ctx, Set<String> problems) throws OXException {
            // Now we go through the set an delete each superfluous entry:
		for (String identifier : problems) {
			try {
				database.setTransactional(true);
				database.startTransaction();
				final int[] numbers = database.removeDocument(identifier, ctx);
				database.commit();
				if (numbers[0] == 1 && LOG.isInfoEnabled()) {
					LOG.info("Have to change infostore version number " +
							"for entry: " + identifier);
				}
				if (numbers[1] == 1 && LOG.isInfoEnabled()) {
					LOG.info("Deleted entry " + identifier + " from " +
							"infostore_documents.");
				}
			} catch (OXException e) {
				LOG.debug("", e);
				try {
					database.rollback();
					return;
				} catch (TransactionException e1) {
					LOG.debug("", e1);
				}
			} finally {
				try {
					database.finish();
				} catch (TransactionException e) {
					LOG.debug("", e);
				}
			}
		}
        }
    }

    private static class DeleteAttachment implements ProblemSolver {
        private AttachmentBase attachments;
        public DeleteAttachment(AttachmentBase attachments) {
            this.attachments = attachments;
        }
        public void solve(Context ctx, Set<String> problems) throws OXException {
            // Now we go through the set an delete each superfluous entry:
		final Iterator<String> it = problems.iterator();
		while (it.hasNext()) {
			try {
				final String identifier = it.next();
				attachments.setTransactional(true);
				attachments.startTransaction();
				final int[] numbers = attachments.removeAttachment(identifier, ctx);
				attachments.commit();
				if (numbers[0] ==  1 && LOG.isInfoEnabled()) {
					LOG.info("Inserted entry for identifier " + identifier + " and Context " + ctx.getContextId()
							+ " in " + "del_attachments");
				}
				if (numbers[1] == 1 && LOG.isInfoEnabled()) {
					LOG.info("Removed attachment database entry for: " + identifier);
				}
			} catch (TransactionException e) {
				LOG.debug("", e);
				try {
					attachments.rollback();
					return;
				} catch (TransactionException e1) {
					LOG.debug("", e1);
				}
				return;
			} catch (OXException e) {
				LOG.debug("", e);
				try {
					attachments.rollback();
					return;
				} catch (TransactionException e1) {
					LOG.debug("", e1);
				}
				return;
			} finally {
				try {
					attachments.finish();
				} catch (TransactionException e) {
					LOG.debug("", e);
				}
			}
		}
        }
    }

    private static class CreateInfoitem implements ProblemSolver {

        private String description = "This file needs attention";
        private String title = "Restoredfile";
        private String fileName = "Restoredfile";
        private String versioncomment = "";
        private String categories = "";

        private DatabaseImpl database;
        private FileStorage storage;
        private Consistency consistency;

        private CreateInfoitem(DatabaseImpl database, FileStorage storage, Consistency consistency) {
            this.database = database;
            this.storage = storage;
            this.consistency = consistency;
        }

        public void solve(Context ctx, Set<String> problems) throws OXException {
            try {
                final User user = consistency.getAdmin(ctx);
                final DocumentMetadata document = new DocumentMetadataImpl();
                document.setDescription(description);
                document.setTitle(title);
                document.setFileName(fileName);
                document.setVersionComment(versioncomment);
                document.setCategories(categories);

                for (String identifier : problems) {
                    try {
                        document.setFileSize(storage.getFileSize(identifier));
                        document.setFileMIMEType(storage.getMimeType(identifier));
                        database.setTransactional(true);
                        database.startTransaction();
                        final int[] numbers = database.saveDocumentMetadata(identifier, document, user, ctx);
                        database.commit();
                        if (numbers[2] == 1 && LOG.isInfoEnabled()) {
                            LOG.info("Dummy entry for " + identifier + " in database " +
                                    "created. The admin of this context has now " +
                                    "a new document");
                        }
                    } catch (FileStorageException e) {
                        LOG.debug("", e);
                        try {
                            database.rollback();
                            return;
                        } catch (TransactionException e1) {
                            LOG.debug("", e1);
                        }
                    } catch (OXException e) {
                        LOG.debug("", e);
                        try {
                            database.rollback();
                            return;
                        } catch (TransactionException e1) {
                            LOG.debug("", e1);
                        }
                    } finally {
                        try {
                            database.finish();
                        } catch (TransactionException e) {
                            LOG.debug("", e);
                        }
                    }
                }

            } catch (LdapException e) {
                LOG.debug("", e);
            }
        }
    }
}
