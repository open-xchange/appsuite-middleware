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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ConfigDB;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.consistency.AttachmentProblemSolver;
import com.openexchange.consistency.DBDelProblemSolver;
import com.openexchange.consistency.DBProblemSolver;
import com.openexchange.consistency.FileStoreProblemSolver;
import com.openexchange.consistency.ProblemSolver;
import com.openexchange.database.AssignmentStorage;
import com.openexchange.database.DatabaseInit;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextInit;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.QuotaFileStorage;

/**
 * Consistency
 * 
 * This class provides the main entry to the consistency executable
 * 
 * @author Dennis Sieben
 *
 */
public class Consistency {

	boolean listout;
	boolean dryrun;
	boolean dummydb;
	boolean dummydeldb;
	boolean dummyattachment;
	boolean dummyfile;
	// Stores the database which was rebuild
	int database = -1;
	// Stores the filestore which was rebuild
	int filestore = -1;
	
	private Log LOG = LogFactory.getLog(DBDelProblemSolver.class);
	
	private void output(final String text) {
	    System.out.println(text);
            if (LOG.isInfoEnabled()) {
                LOG.info(text);
            }
	}
	
	private void erroroutput(final Exception e) {
	    System.err.println(e.getMessage());
	    LOG.debug(e.getMessage(), e);
	}

	private void erroroutput(final String text, final Exception e) {
	    System.err.println(text);
	    LOG.debug(text, e);
	}

    private void outputSet(final SortedSet<String> set) {
	    final Iterator<String> itstr = set.iterator();
	    while (itstr.hasNext()) {
	        output(itstr.next());
	    }
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
	
	private void checkOneContext(final Context ctx)
		throws FilestoreException, FileStorageException {
		final DatabaseImpl DATABASE = new DatabaseImpl(new DBPoolProvider());
		final FileStorage stor = FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,
				new DBPoolProvider()); 
		final AttachmentBase attach = Attachments.getInstance();
		// We believe in the worst case, so lets check the storage first, so
		// that the state file is recreated
		stor.recreateStateFile();
		
		final SortedSet<String> filestoreset = stor.getFileList();
		final SortedSet<String> attachmentset =
			attach.getAttachmentFileStoreLocationsperContext(ctx);
		SortedSet<String> dbfileset;
		SortedSet<String> dbdelfileset;
		try {
			dbfileset = DATABASE.getDocumentFileStoreLocationsperContext(ctx);
			dbdelfileset = DATABASE.getDelDocumentFileStoreLocationsperContext(ctx);
			final SortedSet<String> joineddbfileset = new TreeSet<String>(dbfileset); 
			joineddbfileset.addAll(dbdelfileset);
			joineddbfileset.addAll(attachmentset);
			
			if (listout) {
				output("Filestore:");
				outputSet(filestoreset);

				output("DB_del:");
				outputSet(dbdelfileset);

				output("DB:");
				outputSet(dbfileset);
				
				output("Attachments:");
				outputSet(attachmentset);
			}
			
			// Build the difference set of the database set, so that the final
			// dbfileset contains all the members that aren't in the filestoreset
			if (diffset(dbfileset, filestoreset, "database list",
					"filestore list")) {
				// implement the solver for dbfiles here
				runSolver(dummydb, dbfileset, ctx,
						new DBProblemSolver(DATABASE, stor, attach));
			}
			
			// Build the difference set of the deleted database set, so that the
			// final dbdelfileset contains all the members that aren't in the
			// filestoreset
			if (diffset(dbdelfileset, filestoreset, 
					"database list of deleted files", "filestore list")) {
				//implement the solver for deleted dbfiles here
				runSolver(dummydeldb, dbdelfileset, ctx,
						new DBDelProblemSolver(DATABASE, stor, attach));
			}
			
			// Build the difference set of the attachment database set, so that the
			// final attachmentset contains all the members that aren't in the
			// filestoreset
			if (diffset(attachmentset, filestoreset, 
					"database list of attachment files", "filestore list")) {
				//implement the solver for deleted dbfiles here
				runSolver(dummyattachment, attachmentset, ctx,
						new AttachmentProblemSolver(DATABASE, stor, attach));
			}

			// Build the difference set of the filestore set, so that the final
			// filestoreset contains all the members that aren't in the dbfileset or
			// the dbdelfileset
			if (diffset(filestoreset, joineddbfileset, "filestore list",
					"one of the databases")) {
				//implement the solver for the filestore here
				runSolver(dummyfile, filestoreset, ctx,
						new FileStoreProblemSolver(DATABASE, stor, attach));
			}

		} catch (OXException e) {
			erroroutput(e);
		}
	}

	private void runSolver(final boolean dummy,
			final SortedSet<String> set, final Context ctx, final ProblemSolver solver) {
		if (!dryrun) {
		        output("Now trying to solve this...");
			if (dummy) {
				solver.dummyEntries(set, ctx);					
			} else {
				solver.deleteEntries(set, ctx);					
			}
			try {
				final FileStorage stor = solver.getStorage();
				if (stor instanceof QuotaFileStorage) {
					output("Recalculating usage...");
					((QuotaFileStorage)stor).recalculateUsage();
				}
			} catch (FileStorageException e) {
				erroroutput(e);
			}
		}
	}
	
	private void completeContextChecking() 
			throws ContextException, FilestoreException, FileStorageException {
		final ContextStorage ctxstor = ContextStorage.getInstance();
		final List<Integer> list = ctxstor.getAllContextIds();
		final Iterator<Integer> it = list.iterator();
		final int size = list.size();
		
		for (int i = 0; i < size; i++) {
			final int cid = it.next().intValue();
			output("-------------------------------------");
			output("Checking Context Nr. " + cid + " ....");
			final Context ctx = ctxstor.getContext(cid);
			checkOneContext(ctx);
		}
	}

	private List<Integer> getContextIdstoFilestore(final int filestore_id) throws ContextException {
		final ContextStorage ctxstor = ContextStorage.getInstance();
		final List<Integer> ids  = ctxstor.getAllContextIds();
		final List<Integer> retval = new ArrayList<Integer>();
		final Iterator<Integer> it = ids.iterator();
		final int size = ids.size();
		
		for (int i = 0; i < size; i++) {
			final int cid = it.next().intValue();
			final Context ctx = ctxstor.getContext(cid);
			if (ctx.getFilestoreId() == filestore_id) {
				retval.add(Integer.valueOf(ctx.getContextId()));
			}

		}
		return retval;
	}
	
	private void databasechecking()  {
		/*
		 * First select the contexts which are stored in that database...
		 */
		final ContextStorage ctxstor = ContextStorage.getInstance();
		List<Integer> list;
		try {
			list = AssignmentStorage.getInstance().listContexts(database);
			if (list.isEmpty()) {
				output("There are no contexts which stored their " + "data in " + database);
			} else {
				final Iterator<Integer> it = list.iterator();
				final int size = list.size();
				
				for (int i = 0; i < size; i++) {
					Context ctx;
					try {
						final int cid = it.next().intValue();
						output("-------------------------------------");
						output("Checking Context Nr. " + cid + " ....");
						ctx = ctxstor.getContext(cid);
						checkOneContext(ctx);
					} catch (ContextException e) {
						erroroutput(e);
					} catch (FilestoreException e) {
                        erroroutput(e);
                    } catch (FileStorageException e) {
                        erroroutput(e);
                    } 

				}
			}

		} catch (DBPoolingException e1) {
			erroroutput(e1);
		} 

	}

	private void filestorechecking() {
		/*
		 * First select the contexts which have stored their data in that
		 * filestore...
		 */
		final ContextStorage ctxstor = ContextStorage.getInstance();
		List<Integer> list;
		try {
			list = getContextIdstoFilestore(filestore);

			if (list.isEmpty()) {
				output("There are no contexts which stored their " + "data in " + filestore);
			} else {
				final Iterator<Integer> it = list.iterator();
				final int size = list.size();
				
				for (int i = 0; i < size; i++) {
					Context ctx;
					try {
						final int cid = it.next().intValue();
						output("-------------------------------------");
						output("Checking Context Nr. " + cid + " ....");
						ctx = ctxstor.getContext(cid);
						checkOneContext(ctx);

					} catch (ContextException e) {
						erroroutput(e);
					} catch (FilestoreException e) {
                        erroroutput(e);
                    } catch (FileStorageException e) {
                        erroroutput(e);
                    }
				}
			}
		} catch (ContextException e1) {
			erroroutput(e1);
		}
		
	}
	
	private void printusage() {
		output('\n' +
"Usage: --with-list-output		This parameter activates the output of\n" +
			"					the identifier lists on both sides\n" +
			"					(FileStore and DB). Use this with\n" +
			"					caution as it produces a high amount\n" + 
			"					of messages.\n" +
"       --check-db=DATABASE		This parameter is used to check only\n " +
			"					the given database, so only contexts\n" +
			"					stored within this database are\n" +
			"					checked. Per default all contexts\n" +
			"					are checked regardless of the storage\n" +
			"					point. For this parameter the id of\n" +
			"					the database is used.\n" + 
"       --check-filestore=FILESTORE	This parameter is used to check only\n" +
			"					the given filestore, so only contexts\n" +
			"					which have stored their datas there\n" +
			"					are checked. Per default all contexts\n" +
			"					are checked regardless of the\n"+
			"					filestore. For this parameter the id\n" +
			"					of the filestore is used.\n" +
"       --dryrun				The tool only checks without trying to\n" +
			"					solve anything.\n" +
"       --dummydb			The tool creates dummyfiles for each\n" +
			"					superfluous entry in the normal\n" + 
			"					database.\n" +
"       --dummydeldb			The tool creates dummyfiles for each\n" +
			"					superfluous entry in the deleted \n" + 
			"					documents database.\n" +
"       --dummyattachment		The tool creates dummyfiles for each\n" +
			"					superfluous entry in the attachment\n" + 
			"					database.\n" +
"       --dummyfile			The tool creates dummy entries in the \n" +
			"					admin database for superfluous files.\n" + 
			"					");
	}
	
	public void start(final String[] args) {
        try {
            SystemConfig.getInstance().start();
            ConfigDB.getInstance().start();
            DatabaseInit.getInstance().start();
            ContextInit.getInstance().start();
        } catch (AbstractOXException e) {
            erroroutput("Initializing the context system failed.", e);
            System.exit(1);
        }
		if (args.length >= 0) {
			
			// Checking arguments ...
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("--help")) {
					printusage();
					System.exit(0);
				} else if (args[i].startsWith("--with-list-output")) {
					listout = true;
				} else if (args[i].startsWith("--check-db=")) {
					try {
						database = Integer.parseInt(args[i].substring(11));
					} catch (NumberFormatException e) {
						throw new NumberFormatException("The argument given " +
								"for --check-db is no Integer.");
					}
				} else if (args[i].startsWith("--check-filestore=")) {
					try {
						filestore = Integer.parseInt(args[i].substring(18));						
					} catch (NumberFormatException e) {
						throw new NumberFormatException("The argument given " +
								"for --check-filestore is no Integer.");
					}

				} else if (args[i].startsWith("--dryrun")) {
					dryrun = true;
				} else if (args[i].startsWith("--dummydb")) {
					dummydb = true;
				} else if (args[i].startsWith("--dummydeldb")) {
					dummydeldb = true;
				} else if (args[i].startsWith("--dummyattachment")) {
					dummyattachment = true;
				} else if (args[i].startsWith("--dummyfile")) {
					dummyfile = true;
				}
			}
			
			try {
				if (database != -1) {
					/* 
					 * Now we check only the contexts which are stored in that
					 * database 
					 */
					output("Searching for database " + database);
					databasechecking();
				} else if (filestore != -1) {
					/*
					 * Now we check only the contexts which are stored in that
					 * filestore
					 */
					filestorechecking();
				} else {
					completeContextChecking();
				}
			} catch (ContextException e) {
				erroroutput(e);
			} catch (FilestoreException e) {
                erroroutput(e);
            } catch (FileStorageException e) {
                erroroutput(e);
            }
		}
		output("Finished.");
		System.exit(0);
	}

	/**
	 * @param args
	 * @throws ContextException 
	 */
	public static void main(final String[] args) {
		final Consistency consist = new Consistency();
		consist.start(args);
	}
}
