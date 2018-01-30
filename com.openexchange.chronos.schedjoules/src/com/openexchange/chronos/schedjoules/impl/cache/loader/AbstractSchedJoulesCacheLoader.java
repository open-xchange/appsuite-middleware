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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.chronos.schedjoules.impl.cache.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesAPICache;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesCachedItemKey;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.timer.TimerService;

/**
 * {@link AbstractSchedJoulesCacheLoader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractSchedJoulesCacheLoader extends CacheLoader<SchedJoulesCachedItemKey, SchedJoulesPage> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSchedJoulesCacheLoader.class);
    final SchedJoulesAPICache apiCache;

    /**
     * Initialises a new {@link AbstractSchedJoulesCacheLoader}.
     * 
     * @param apiCache The {@link SchedJoulesAPICache}
     */
    public AbstractSchedJoulesCacheLoader(SchedJoulesAPICache apiCache) {
        super();
        this.apiCache = apiCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.common.cache.CacheLoader#reload(java.lang.Object, java.lang.Object)
     */
    @Override
    public ListenableFuture<SchedJoulesPage> reload(SchedJoulesCachedItemKey key, SchedJoulesPage oldValue) throws Exception {
        TimerService timerService = Services.getService(TimerService.class);
        ListenableFutureTask<SchedJoulesPage> task = ListenableFutureTask.create(() -> {
            if (isModified(key, oldValue)) {
                LOG.debug("The entry with key: '{}' was modified since last fetch. Reloading...", key.toString());
                return load(key);
            }
            LOG.debug("The entry with key: '{}' was not modified since last fetch.", key.toString());
            return oldValue;
        });
        timerService.getExecutor().execute(task);
        return task;
    }

    /**
     * Checks if the specified {@link SchedJoulesPage} is modified
     * 
     * @param page The {@link SchedJoulesPage}
     * @return <code>true</code> if the page was modified; <code>false</code> otherwise
     */
    abstract boolean isModified(SchedJoulesCachedItemKey key, SchedJoulesPage page) throws OXException;
}
