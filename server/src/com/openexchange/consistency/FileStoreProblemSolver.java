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

import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;


public class FileStoreProblemSolver extends ProblemSolver {

    private String description = "This file needs attention";
    private String title = "Restoredfile";
    private String fileName = "Restoredfile";
    private String versioncomment = "";
    private String categories = "";
    private Log LOG = LogFactory.getLog(FileStoreProblemSolver.class);
    
	/**
     * Don't allow to create a FileStoreProblemSolver object without specifying a
     * Database object
     */
	private FileStoreProblemSolver() {
		this(null, null, null);
	}
	
	/**
	 * @param database The database on which the solver works
	 */
	public FileStoreProblemSolver(DatabaseImpl database, FileStorage storage,
			AttachmentBase attachments) {
		super(database, storage, attachments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteEntries(final SortedSet<String> set, final Context ctx) {
		try {
			for (String identifier : set) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dummyEntries(final SortedSet<String> set, final Context ctx) {
		try {
			final User user = UserStorage.getInstance().getUser(ctx.getMailadmin(), ctx);
			final DocumentMetadata document = new DocumentMetadataImpl();
			document.setDescription(description);
			document.setTitle(title);
			document.setFileName(fileName);
			document.setVersionComment(versioncomment);
			document.setCategories(categories);
			
			for (String identifier : set) {
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
