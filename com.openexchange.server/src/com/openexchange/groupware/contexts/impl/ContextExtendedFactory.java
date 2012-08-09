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

package com.openexchange.groupware.contexts.impl;

import static com.openexchange.java.Autoboxing.I;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;

import org.apache.commons.logging.Log;

import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.groupware.update.internal.SchemaExceptionCodes;
import com.openexchange.log.LogFactory;

/**
 * {@link ContextExtendedFactory}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 */
public class ContextExtendedFactory implements OXObjectFactory<ContextExtended> {

	private static final long serialVersionUID = 7796462639896914733L;

	private static final Log LOG = LogFactory
			.getLog(ContextExtendedFactory.class);

	private int contextId;
	public static CachingContextStorage parent;

	public ContextExtendedFactory(int contextId) {
		this.contextId = contextId;
	}

	public Serializable getKey() {
		return I(contextId);
	}

	@Override
	public ContextExtended load() throws OXException {
		final ContextExtended retval = parent.getPersistantImpl().loadContext(
				contextId);
		// TODO We should introduce a logic layer above this context storage
		// layer. That layer should then trigger the update tasks.
		// Nearly all accesses to the ContextStorage need then to be replaced
		// with an access to the ContextService.
		final Updater updater = Updater.getInstance();
		try {
			final UpdateStatus status = updater.getStatus(retval);
			retval.setUpdating(status.blockingUpdatesRunning()
					|| status.needsBlockingUpdates());
			if ((status.needsBlockingUpdates() || status
					.needsBackgroundUpdates())
					&& !status.blockingUpdatesRunning()
					&& !status.backgroundUpdatesRunning()) {
				updater.startUpdate(retval);
			}
		} catch (final OXException e) {
			if (SchemaExceptionCodes.DATABASE_DOWN.equals(e)) {
				LOG.warn("Switching to read only mode for context " + contextId
						+ " because master database is down.", e);
				retval.setReadOnly(true);
			}
		}
		return retval;
	}

	@Override
	public Lock getCacheLock() {
		return parent.getCacheLock();
	}
}
