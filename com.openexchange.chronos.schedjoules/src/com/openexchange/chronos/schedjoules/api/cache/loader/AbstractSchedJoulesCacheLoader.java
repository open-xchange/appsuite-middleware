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

package com.openexchange.chronos.schedjoules.api.cache.loader;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.openexchange.chronos.schedjoules.api.cache.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.api.cache.SchedJoulesPage.SchedJoulesPageBuilder;
import com.openexchange.chronos.schedjoules.api.client.HttpMethod;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTBindPoint;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRequest;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesResponse;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.timer.TimerService;

/**
 * {@link AbstractSchedJoulesCacheLoader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractSchedJoulesCacheLoader<T> extends CacheLoader<T, SchedJoulesPage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSchedJoulesCacheLoader.class);

    final SchedJoulesRESTClient client;
    final SchedJoulesRESTBindPoint restBindPoint;

    /**
     * Initialises a new {@link AbstractSchedJoulesCacheLoader}.
     * 
     * @param client The {@link SchedJoulesRESTClient}
     * @param restBindPoint The {@link SchedJoulesRESTBindPoint}
     */
    public AbstractSchedJoulesCacheLoader(SchedJoulesRESTClient client, SchedJoulesRESTBindPoint restBindPoint) {
        super();
        this.client = client;
        this.restBindPoint = restBindPoint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.common.cache.CacheLoader#reload(java.lang.Object, java.lang.Object)
     */
    @Override
    public ListenableFuture<SchedJoulesPage> reload(T key, SchedJoulesPage oldValue) throws Exception {
        TimerService timerService = Services.getService(TimerService.class);
        ListenableFutureTask<SchedJoulesPage> task = ListenableFutureTask.create(() -> {
            SchedJoulesRequest request = new SchedJoulesRequest(restBindPoint.getAbsolutePath());
            SchedJoulesResponse response = client.executeRequest(request, HttpMethod.HEAD, oldValue.getEtag(), oldValue.getLastModified());
            if (response.getStatusCode() == 304) {
                LOGGER.debug("The entry with key: '{}' was not modified since last fetch.", key.toString());
                return oldValue;
            }
            LOGGER.debug("The entry with key: '{}' was modified since last fetch. Reloading...", key.toString());
            return load(key);
        });
        timerService.getExecutor().execute(task);
        return task;
    }

    /**
     * Executes the specified request and returns the {@link JSONObject} payload of the response
     * 
     * @param request the {@link SchedJoulesRequest}
     * @return The {@link JSONObject} of the response payload
     * @throws OXException if an error is occurred
     */
    // FIXME: executor mini framework
    SchedJoulesPage executeRequest(SchedJoulesRequest request) throws OXException {
        SchedJoulesResponse response = client.executeRequest(request);
        return new SchedJoulesPageBuilder().itemData((JSONObject) response.getResponseBody()).etag(response.getETag()).lastModified(response.getLastModified()).build();
    }
}
