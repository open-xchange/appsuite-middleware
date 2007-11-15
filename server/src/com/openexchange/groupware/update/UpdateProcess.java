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

package com.openexchange.groupware.update;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.openexchange.database.ConfigDBStorage;
import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.server.DBPoolingException;

/**
 * The {@link #run()} method of this class is started in a seperate thread for
 * the update process.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateProcess implements Runnable {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(UpdateProcess.class);

	private final int contextId;

	private final Lock updateLock;

	private final SchemaStore schemaStore;

	public UpdateProcess(final int contextId) throws SchemaException {
		schemaStore = SchemaStore.getInstance(SchemaStoreImpl.class.getCanonicalName());
		this.contextId = contextId;
		this.updateLock = new ReentrantLock();
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		/*
		 * Obtain lock
		 */
		updateLock.lock();
		try {
			boolean unlock = false;
			try {
				/*
				 * Load schema
				 */
				final Schema schema = schemaStore.getSchema(contextId);
				if (schema.getDBVersion() >= UpdateTaskCollection.getHighestVersion()) {
					/*
					 * Already been updated before by previous thread
					 */
					return;
				}
				try {
					lockSchema(schema);
					/*
					 * Lock successfully obtained, thus remember to unlock
					 */
					unlock = true;
					/*
					 * Remove affected contexts and kick active sessions
					 */
					removeContexts(schema);
					/*
					 * Get filtered & sorted list of update tasks
					 */
					final List<UpdateTask> updateTasks = UpdateTaskCollection.getFilteredAndSortedUpdateTasks(schema
							.getDBVersion());
					/*
					 * Perform updates
					 */
					final int size = updateTasks.size();
					final Iterator<UpdateTask> iter = updateTasks.iterator();
					for (int i = 0; i < size; i++) {
						try {
							iter.next().perform(schema, contextId);
						} catch (AbstractOXException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				} finally {
					if (unlock) {
						unlockSchema(schema);
					}
				}
			} catch (SchemaException e) {
				LOG.error(e.getMessage(), e);
			} catch (DBPoolingException e) {
				LOG.error(e.getMessage(), e);
			} catch (ContextException e) {
				LOG.error(e.getMessage(), e);
			}
		} finally {
			updateLock.unlock();
		}
	}

	private final void lockSchema(final Schema schema) throws SchemaException {
		schemaStore.lockSchema(schema, contextId);
	}

	private final void unlockSchema(final Schema schema) throws SchemaException {
		schemaStore.unlockSchema(schema, contextId);
	}

	private final void removeContexts(final Schema schema) throws DBPoolingException, ContextException {
		final int[] contextIds = ConfigDBStorage.getContextsFromSchema(schema.getSchema(), Database.resolvePool(
				contextId, true));
		final ContextStorage contextStorage = ContextStorage.getInstance();
		for (int cid : contextIds) {
			contextStorage.invalidateContext(cid);
		}
	}

}
