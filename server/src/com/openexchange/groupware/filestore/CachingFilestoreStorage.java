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

package com.openexchange.groupware.filestore;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.cache.dynamic.impl.CacheProxy;
import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.server.services.ServerServiceRegistry;

public class CachingFilestoreStorage extends FilestoreStorage {

	private static final Log LOG = LogFactory.getLog(CachingFilestoreStorage.class);

	private final FilestoreStorage delegate;
	
	private static Cache cache;

    private static final Lock CACHE_LOCK;

	static {
		try {
			cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache("Filestore");
		} catch (final CacheException e) {
			LOG.error(e);
		}
        CACHE_LOCK = new ReentrantLock();
	}
	
	public CachingFilestoreStorage(final FilestoreStorage fs) {
		this.delegate = fs;
	}
	
	@Override
	public Filestore getFilestore(final int id) throws FilestoreException {
        final FilestoreFactory factory = new FilestoreFactory(id,delegate);
		if(cache == null) {
			throw new IllegalStateException("Cache not initialised! Not caching");
		}
        if (null == cache.get((Serializable) factory.getKey())) {
            factory.load();
        }
		return CacheProxy.getCacheProxy(factory, cache, Filestore.class);
	}
	
	private static final class FilestoreFactory implements
        OXObjectFactory<Filestore> {

		private final int id;
		private final FilestoreStorage delegate;

		public FilestoreFactory(final int id, final FilestoreStorage delegate) {
			this.id = id;
			this.delegate = delegate;
		}
		
		public Object getKey() {
			return id;
		}

		public Filestore load() throws FilestoreException {
		    return delegate.getFilestore(id);
		}

        public Lock getCacheLock() {
            return CACHE_LOCK;
        }
		
	}

}
