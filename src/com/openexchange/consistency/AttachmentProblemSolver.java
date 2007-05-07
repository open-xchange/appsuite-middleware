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

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;


/**
 * This class implements the solver for the problem that an entry exists in the
 * prg_attachment database but not in the filestore
 * 
 * @author Dennis Sieben
 *
 */
public class AttachmentProblemSolver extends ProblemSolver {

	private static final Log LOG = LogFactory.getLog(DBDelProblemSolver.class);
	
	/**
     * Don't allow to create a DBDelProblemSolver object without specifying a
     * Database object
     */
	private AttachmentProblemSolver() {
		this(null, null, null);
	}
	
	/**
	 * @param database The database on which the solver works
	 */
	public AttachmentProblemSolver(DatabaseImpl database, FileStorage storage,
			AttachmentBase attachments) {
		super(database, storage, attachments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntries(final SortedSet<String> set, final Context ctx) {
		// Now we go through the set an delete each superfluous entry:
		final Iterator<String> it = set.iterator();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dummyEntries(final SortedSet<String> set, final Context ctx) {
		/* 
		 * Here we operate in two stages. First we create a dummy entry in the
		 * filestore. Second we update the Entries in the database
		 */
		final int size = set.size();
		final Iterator<String> it = set.iterator();
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
