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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.schedjoules.impl;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.chronos.schedjoules.SchedJoulesCalendar;
import com.openexchange.chronos.schedjoules.SchedJoulesResult;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link SchedJoulesServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesServiceImpl implements SchedJoulesService {

    private static final Logger LOG = LoggerFactory.getLogger(SchedJoulesServiceImpl.class);

    /**
     * Cache for the API clients
     */
    private final Cache<String, SchedJoulesAPI> apiCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(notification -> {
        LOG.debug("Shutting down API for '{}'", notification.getKey());
        SchedJoulesAPI api = (SchedJoulesAPI) notification.getValue();
        api.shutDown();
    }).build();

    /**
     * Initialises a new {@link SchedJoulesServiceImpl}.
     *
     * @throws OXException if the {@link SchedJoulesAPI} cannot be initialised
     */
    public SchedJoulesServiceImpl() throws OXException {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot()
     */
    @Override
    public SchedJoulesResult getRoot(int contextId) throws OXException {
        return new SchedJoulesResult(filterContent((JSONObject) getAPI(contextId).pages().getRootPage().getItemData()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot(java.lang.String, java.lang.String)
     */
    @Override
    public SchedJoulesResult getRoot(int contextId, String locale, String location) throws OXException {
        return new SchedJoulesResult(filterContent((JSONObject) getAPI(contextId).pages().getRootPage(locale, location).getItemData()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.service.SchedJoulesService#getPage(int)
     */
    @Override
    public SchedJoulesResult getPage(int contextId, int pageId) throws OXException {
        return new SchedJoulesResult(filterContent((JSONObject) getAPI(contextId).pages().getPage(pageId).getItemData()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#getPage(int, java.lang.String)
     */
    @Override
    public SchedJoulesResult getPage(int contextId, int pageId, String locale) throws OXException {
        return new SchedJoulesResult(filterContent((JSONObject) getAPI(contextId).pages().getPage(pageId, locale).getItemData()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listCountries(java.lang.String)
     */
    @Override
    public SchedJoulesResult listCountries(int contextId, String locale) throws OXException {
        return new SchedJoulesResult(getAPI(contextId).countries().listCountries(locale).getItemData());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listLanguages()
     */
    @Override
    public SchedJoulesResult listLanguages(int contextId) throws OXException {
        return new SchedJoulesResult(getAPI(contextId).languages().listLanguages().getItemData());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#search(java.lang.String, java.lang.String, int, int, int)
     */
    @Override
    public SchedJoulesResult search(int contextId, String query, String locale, int countryId, int categoryId, int maxRows) throws OXException {
        return new SchedJoulesResult(filterContent((JSONObject) getAPI(contextId).pages().search(query, locale, countryId, categoryId, maxRows).getItemData()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#getCalendar(int, java.net.URL, java.lang.String, long)
     */
    @Override
    public SchedJoulesCalendar getCalendar(int contextId, URL url, String etag, long lastModified) throws OXException {
        return getAPI(contextId).calendar().getCalendar(url, etag, lastModified);
    }

    ///////////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * @param contextId
     * @return
     * @throws OXException
     */
    private SchedJoulesAPI getAPI(int contextId) throws OXException {
        try {
            return apiCache.get(getKey(contextId), () -> {
                return new SchedJoulesAPI(getAPIKey(contextId));
            });
        } catch (ExecutionException e) {
            throw SchedJoulesAPIExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Gets the API key for the specified context and hashes both values to create
     * a unique key for the client
     * 
     * @param contextId The context identifier
     * @return The hash
     * @throws OXException if the API key is not configured for the specified context
     */
    private String getKey(int contextId) throws OXException {
        return DigestUtils.sha256Hex(contextId + ":" + getAPIKey(contextId));

    }

    /**
     * Retrieves the API key for the specified context
     * 
     * @param contextId The context identifier
     * @return The API key
     * @throws OXException if no API key is configured
     */
    private String getAPIKey(int contextId) throws OXException {
        LeanConfigurationService leanConfigService = Services.getService(LeanConfigurationService.class);
        String apiKey = leanConfigService.getProperty(-1, contextId, SchedJoulesProperty.apiKey);
        if (Strings.isEmpty(apiKey)) {
            throw SchedJoulesAPIExceptionCodes.NO_API_KEY_CONFIGURED.create(SchedJoulesProperty.apiKey.getFQPropertyName());
        }
        return apiKey;
    }

    /**
     * Filters the specified {@link JSONObject}
     * 
     * @param content the {@link JSONObject} to filter
     * @return the filtered {@link JSONObject}
     * @throws OXException if a JSON error is occurred
     */
    private JSONObject filterContent(JSONObject content) throws OXException {
        try {
            long startTime = System.currentTimeMillis();
            filterJSONObject(content);
            LOG.trace("Filtered content in {} msec.", System.currentTimeMillis() - startTime);
            return content;
        } catch (JSONException e) {
            throw SchedJoulesAPIExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Filters the specified {@link JSONObject} and removes the 'url' field
     * 
     * @param content the {@link JSONObject} to filter
     * @return the filtered {@link JSONObject}
     * @throws OXException if a JSON error is occurred
     */
    private void filterJSONObject(JSONObject object) throws JSONException, OXException {
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.equals("url")) {
                keys.remove();
            } else {
                filterObject(object.get(key));
            }
        }
    }

    /**
     * Filters the specified {@link JSONArray}
     * 
     * @param array The {@link JSONArray} to filter
     * @throws OXException if a JSON error is occurred
     * @throws JSONException if JSON parsing error is occurred
     */
    private void filterJSONArray(JSONArray array) throws OXException, JSONException {
        for (int index = 0; index < array.length(); index++) {
            filterObject(array.get(index));
        }
    }

    /**
     * Filters the specified {@link JSONObject}
     * 
     * @param array The {@link JSONObject} to filter
     * @throws OXException if a JSON error is occurred
     * @throws JSONException if JSON parsing error is occurred
     */
    private void filterObject(Object obj) throws JSONException, OXException {
        if (obj instanceof JSONObject) {
            filterJSONObject((JSONObject) obj);
        } else if (obj instanceof JSONArray) {
            filterJSONArray((JSONArray) obj);
        }
    }
}
