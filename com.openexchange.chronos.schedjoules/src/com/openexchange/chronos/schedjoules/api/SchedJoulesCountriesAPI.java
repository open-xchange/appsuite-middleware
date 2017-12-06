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

package com.openexchange.chronos.schedjoules.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCommonParameter;
import com.openexchange.chronos.schedjoules.api.cache.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.api.cache.SchedJoulesPage.SchedJoulesPageBuilder;
import com.openexchange.chronos.schedjoules.api.client.HttpMethod;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTBindPoint;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRESTClient;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesRequest;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesResponse;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.timer.TimerService;

/**
 * {@link SchedJoulesCountriesAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesCountriesAPI extends AbstractSchedJoulesAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedJoulesCountriesAPI.class);

    private final LoadingCache<String, SchedJoulesPage> countriesCache = CacheBuilder.newBuilder().refreshAfterWrite(24, TimeUnit.HOURS).build(new CacheLoader<String, SchedJoulesPage>() {

        @Override
        public SchedJoulesPage load(String key) throws Exception {
            SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.countries);
            request.setQueryParameter(SchedJoulesCommonParameter.locale.name(), key);

            SchedJoulesResponse response = client.executeRequest(request);
            return new SchedJoulesPageBuilder().itemData((JSONArray) response.getResponseBody()).etag(response.getETag()).lastModified(response.getLastModified()).build();
        }

        @Override
        public ListenableFuture<SchedJoulesPage> reload(String key, SchedJoulesPage oldValue) throws Exception {
            TimerService timerService = Services.getService(TimerService.class);
            ListenableFutureTask<SchedJoulesPage> task = ListenableFutureTask.create(() -> {
                SchedJoulesRequest request = new SchedJoulesRequest(SchedJoulesRESTBindPoint.countries.getAbsolutePath());
                SchedJoulesResponse response = client.executeRequest(request, HttpMethod.HEAD, oldValue.getEtag(), oldValue.getLastModified());
                if (response.getStatusCode() == 304) {
                    LOGGER.debug("The entry with locale: {} was not modified since last fetch.", key);
                    return oldValue;
                }
                LOGGER.debug("The entry with locale was modified since last fetch. Reloading...", key);
                return load(key);
            });
            timerService.getExecutor().execute(task);
            return task;
        }

    });

    /**
     * Initialises a new {@link SchedJoulesCountriesAPI}.
     */
    SchedJoulesCountriesAPI(SchedJoulesRESTClient client) {
        super(client);
    }

    /**
     * Retrieves a list with all available countries
     * 
     * @return A {@link JSONArray} with all available countries
     * @throws OXException if a parsing error is occurred
     */
    public JSONArray listCountries() throws OXException {
        return listCountries(DEFAULT_LOCALE);
    }

    /**
     * Retrieves a list with all available countries
     * 
     * @param locale The locale
     * @return A {@link JSONArray} with all available countries
     * @throws OXException if a parsing error is occurred
     */
    public JSONArray listCountries(String locale) throws OXException {
        try {
            return (JSONArray) countriesCache.get(locale).getItemData();
        } catch (ExecutionException e) {
            throw SchedJoulesAPIExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
    }
}
