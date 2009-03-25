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

package com.openexchange.image.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;

final class ContextBoundImagesCleaner implements Runnable {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ContextBoundImagesCleaner.class);

	private final ConcurrentMap<Integer, ConcurrentMap<String, ImageData>> toIterate;

	/**
	 * Initializes a new {@link ContextBoundImagesCleaner}
	 * 
	 * @param toIterate
	 *            The concurrent map to iterate
	 */
	ContextBoundImagesCleaner(final ConcurrentMap<Integer, ConcurrentMap<String, ImageData>> toIterate) {
		super();
		this.toIterate = toIterate;
	}

	public void run() {
		try {
			if (toIterate.isEmpty()) {
				/*
				 * Nothing to iterate
				 */
				return;
			}
			final ContextStorage storage = ContextStorage.getInstance();
			final long now = System.currentTimeMillis();
			for (final Iterator<Map.Entry<Integer, ConcurrentMap<String, ImageData>>> iterator = toIterate.entrySet()
					.iterator(); iterator.hasNext();) {
				final Map.Entry<Integer, ConcurrentMap<String, ImageData>> entry = iterator.next();
				boolean removed = false;
				try {
					storage.getContext(entry.getKey().intValue());
				} catch (final ContextException e) {
					/*
					 * Context does not exist (anymore)
					 */
					if (LOG.isDebugEnabled()) {
						LOG.debug("Context not available for ID " + entry.getKey()
								+ ". Removing all associated images.");
					}
					iterator.remove();
					removed = true;
				}
				if (!removed) {
                    final ConcurrentMap<String, ImageData> innerMap = entry.getValue();
                    for (final Iterator<ImageData> inner = innerMap.values().iterator(); inner.hasNext();) {
                        final ImageData toCheck = inner.next();
                        final int ttl = toCheck.getTimeToLive();
                        if (ttl > 0 && (now - toCheck.getLastAccessed()) > ttl) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Removing expired context-bound image with UID " + toCheck.getUniqueId());
                            }
                            inner.remove();
                        }
                    }
                    if (innerMap.isEmpty()) {
                        iterator.remove();
                    }
                }
			}
		} catch (final Exception e) {
			/*
			 * Catch every exception to keep parental timer alive
			 */
			LOG.error(e.getMessage(), e);
		}
	} // end of run()

}