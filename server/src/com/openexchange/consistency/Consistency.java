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

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.api2.OXException;

/**
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


    public void repairFilesInContext(int contextId, String resolverPolicy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void repairFilesInFilestore(int filestoreId, String resolverPolicy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void repairFilesInDatabase(int databaseId, String resolverPolicy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void repairAllFiles(String resolverPolicy) {
        //To change body of implemented methods use File | Settings | File Templates.
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

	private void erroroutput(final String text, final Exception e) {
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

	private void checkOneContext(final Context ctx, ProblemSolver dbSolver, ProblemSolver attachmentSolver, ProblemSolver fileSolver, DatabaseImpl database, AttachmentBase attach, FileStorage stor) throws AbstractOXException {
		//final DatabaseImpl DATABASE = new DatabaseImpl(new DBPoolProvider());
		//final FileStorage stor = FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,
		//		new DBPoolProvider());
		//final AttachmentBase attach = Attachments.getInstance();
		// We believe in the worst case, so lets check the storage first, so
		// that the state file is recreated
		stor.recreateStateFile();

		final SortedSet<String> filestoreset = stor.getFileList();
		final SortedSet<String> attachmentset =
			attach.getAttachmentFileStoreLocationsperContext(ctx);
		SortedSet<String> dbfileset;
		SortedSet<String> dbdelfileset;
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
                //runSolver(dummydb, dbfileset, ctx,
			    //	new DBProblemSolver(DATABASE, stor, attach));
			}


			// Build the difference set of the attachment database set, so that the
			// final attachmentset contains all the members that aren't in the
			// filestoreset
			if (diffset(attachmentset, filestoreset,
					"database list of attachment files", "filestore list")) {
				//implement the solver for deleted dbfiles here
				attachmentSolver.solve(ctx, attachmentset);
                //runSolver(dummyattachment, attachmentset, ctx,
				//		new AttachmentProblemSolver(DATABASE, stor, attach));
			}

			// Build the difference set of the filestore set, so that the final
			// filestoreset contains all the members that aren't in the dbfileset or
			// the dbdelfileset
			if (diffset(filestoreset, joineddbfileset, "filestore list",
					"one of the databases")) {
				//implement the solver for the filestore here
				fileSolver.solve(ctx, filestoreset);
                //runSolver(dummyfile, filestoreset, ctx,
				//		new FileStoreProblemSolver(DATABASE, stor, attach));
			}

		} catch (OXException e) {
			erroroutput(e);
		}
	}

    protected abstract Context getContext(int contextId);
    protected abstract DatabaseImpl getDatabase();
    protected abstract AttachmentBase getAttachments();
    protected abstract FileStorage getFileStorage(Context ctx);
    protected abstract List<Context> getContextsForFilestore(int filestoreId);
    protected abstract List<Context> getContextsForDatabase(int datbaseId);
    protected abstract List<Context> getAllContexts();
        


    private static final class ResolverPolicy {
        private boolean dummydb;
        private boolean dummydeldb;
        private boolean dummyattachment;
        private boolean dummyfile;

        public ResolverPolicy(boolean dummydb, boolean dummydeldb, boolean dummyattachment, boolean dummyfile) {
            this.dummydb = dummydb;
            this.dummydeldb = dummydeldb;
            this.dummyattachment = dummyattachment;
            this.dummyfile = dummyfile;
        }

        public static ResolverPolicy parse(String list) {
            String[] options = list.split("\\s*,\\s*");
            boolean dummydb = false;
            boolean dummydeldb = false;
            boolean dummyattachment = false;
            boolean dummyfile = false;

            for(String option : options) {
                if(option.endsWith("deldb")) {
                    dummydeldb = true;
                } else if (option.endsWith("db")) {
                    dummydb = true;
                } else if (option.endsWith("attachment")) {
                    dummyattachment = true;
                } else if (option.endsWith("file")) {
                    dummyfile = true;
                }
            }

            return new ResolverPolicy(dummydb, dummydeldb, dummyattachment, dummyfile);
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
}
