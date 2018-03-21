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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.openexchange.chronos.schedjoules.SchedJoulesResult;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.schedjoules.api.SchedJoulesAPI;
import com.openexchange.chronos.schedjoules.api.SchedJoulesPageField;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesAPIDefaultValues;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCalendar;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesPage;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesAPICache;
import com.openexchange.chronos.schedjoules.impl.cache.SchedJoulesCachedItemKey;
import com.openexchange.chronos.schedjoules.impl.cache.loader.SchedJoulesCountriesCacheLoader;
import com.openexchange.chronos.schedjoules.impl.cache.loader.SchedJoulesLanguagesCacheLoader;
import com.openexchange.chronos.schedjoules.impl.cache.loader.SchedJoulesPageCacheLoader;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;

/**
 * {@link SchedJoulesServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesServiceImpl implements SchedJoulesService, Reloadable {

    private static final int LANGUAGES_ID = -1;

    private static final int COUNTRIES_ID = -2;

    private static final Logger LOG = LoggerFactory.getLogger(SchedJoulesServiceImpl.class);

    private final SchedJoulesAPICache apiCache = new SchedJoulesAPICache();

    /**
     * Pages cache
     */
    private final LoadingCache<SchedJoulesCachedItemKey, SchedJoulesPage> pagesCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(24, TimeUnit.HOURS).refreshAfterWrite(24, TimeUnit.HOURS).build(new SchedJoulesPageCacheLoader(apiCache));

    /**
     * Countries cache
     */
    private final LoadingCache<SchedJoulesCachedItemKey, SchedJoulesPage> countriesCache = CacheBuilder.newBuilder().refreshAfterWrite(24, TimeUnit.HOURS).build(new SchedJoulesCountriesCacheLoader(apiCache));

    /**
     * Languages cache
     */
    private final LoadingCache<SchedJoulesCachedItemKey, SchedJoulesPage> languagesCache = CacheBuilder.newBuilder().refreshAfterWrite(24, TimeUnit.HOURS).build(new SchedJoulesLanguagesCacheLoader(apiCache));
    /**
     * Caches the root page item ids
     */
    private final Cache<String, Integer> rootItemIdCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(24, TimeUnit.HOURS).build();

    /**
     * Black listed itemids
     */
    private List<Integer> blacklistedItems;

    /**
     * Initialises a new {@link SchedJoulesServiceImpl}.
     *
     * @throws OXException if the {@link SchedJoulesAPI} cannot be initialised
     */
    public SchedJoulesServiceImpl() throws OXException {
        super();
        initialiseBlackListedItems();
    }

    /**
     * Initialises the black-listed items {@link List}
     */
    private void initialiseBlackListedItems() {
        LeanConfigurationService leanConfig = Services.getService(LeanConfigurationService.class);
        String property = leanConfig.getProperty(SchedJoulesProperty.itemBlacklist);
        if (Strings.isEmpty(property)) {
            blacklistedItems = Collections.emptyList();
            return;
        }
        String[] split = Strings.splitByComma(property);
        List<Integer> l = new ArrayList<>(split.length);
        for (String s : split) {
            try {
                l.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                LOG.debug("The black-listed item id '{}' is not an integer. Ignoring", s, e);
            }
        }
        blacklistedItems = Collections.unmodifiableList(l);
        invalidateCaches();
    }

    /**
     * Invalidates the pages and root cache
     */
    private void invalidateCaches() {
        pagesCache.asMap().entrySet().stream().filter(predicate -> false == blacklistedItems.contains(predicate.getKey().getItemId())).forEach(entry -> {
            if (entry.getValue().getItemData().isObject()) {
                removeBlackListedItems(entry.getValue().getItemData().toObject());
            }
        });
        rootItemIdCache.asMap().entrySet().stream().filter(predicate -> false == blacklistedItems.contains(predicate.getValue().intValue()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot()
     */
    @Override
    public SchedJoulesResult getRoot(int contextId, Set<SchedJoulesPageField> filteredFields) throws OXException {
        return getRoot(contextId, SchedJoulesAPIDefaultValues.DEFAULT_LOCALE, SchedJoulesAPIDefaultValues.DEFAULT_LOCATION, filteredFields);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.service.SchedJoulesService#getRoot(java.lang.String, java.lang.String)
     */
    @Override
    public SchedJoulesResult getRoot(int contextId, String locale, String location, Set<SchedJoulesPageField> filteredFields) throws OXException {
        try {
            int itemId = rootItemIdCache.get(location, () -> {
                SchedJoulesAPI api = apiCache.getAPI(contextId);
                SchedJoulesPage rootPage = api.pages().getRootPage(locale, location);

                JSONObject itemData = (JSONObject) rootPage.getItemData();
                int rootPageItemId = itemData.getInt("item_id");

                pagesCache.put(new SchedJoulesCachedItemKey(contextId, rootPageItemId, locale), rootPage);
                return rootPageItemId;
            });
            return getPage(contextId, itemId, locale, filteredFields);
        } catch (ExecutionException e) {
            throw handleExecutionException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.service.SchedJoulesService#getPage(int)
     */
    @Override
    public SchedJoulesResult getPage(int contextId, int pageId, Set<SchedJoulesPageField> filteredFields) throws OXException {
        return getPage(contextId, pageId, SchedJoulesAPIDefaultValues.DEFAULT_LOCALE, filteredFields);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#getPage(int, java.lang.String)
     */
    @Override
    public SchedJoulesResult getPage(int contextId, int pageId, String locale, Set<SchedJoulesPageField> filteredFields) throws OXException {
        if (blacklistedItems.contains(pageId)) {
            throw SchedJoulesAPIExceptionCodes.PAGE_NOT_FOUND.create();
        }
        try {
            JSONObject content = (JSONObject) pagesCache.get(new SchedJoulesCachedItemKey(contextId, pageId, locale)).getItemData();
            filterContent(content, SchedJoulesPageField.toSring(filteredFields));
            return new SchedJoulesResult(content);
        } catch (ExecutionException e) {
            throw handleExecutionException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listCountries(java.lang.String)
     */
    @Override
    public SchedJoulesResult listCountries(int contextId, String locale) throws OXException {
        try {
            return new SchedJoulesResult(countriesCache.get(new SchedJoulesCachedItemKey(contextId, COUNTRIES_ID, locale)).getItemData());
        } catch (ExecutionException e) {
            throw handleExecutionException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#listLanguages()
     */
    @Override
    public SchedJoulesResult listLanguages(int contextId) throws OXException {
        try {
            return new SchedJoulesResult(languagesCache.get(new SchedJoulesCachedItemKey(contextId, LANGUAGES_ID, null)).getItemData());
        } catch (ExecutionException e) {
            throw handleExecutionException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#search(java.lang.String, java.lang.String, int, int, int)
     */
    @Override
    public SchedJoulesResult search(int contextId, String query, String locale, int countryId, int categoryId, int maxRows, Set<SchedJoulesPageField> filteredFields) throws OXException {
        return new SchedJoulesResult(filterContent((JSONObject) apiCache.getAPI(contextId).pages().search(query, locale, countryId, categoryId, maxRows).getItemData(), SchedJoulesPageField.toSring(filteredFields)));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#getCalendar(int, java.net.URL, java.lang.String, long)
     */
    @Override
    public SchedJoulesCalendar getCalendar(int contextId, URL url, String etag, long lastModified) throws OXException {
        return apiCache.getAPI(contextId).calendar().getCalendar(url, etag, lastModified);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.chronos.schedjoules.SchedJoulesService#isAvailable(int)
     */
    @Override
    public boolean isAvailable(int contextId) {
        try {
            apiCache.getAPI(contextId);
            return true;
        } catch (OXException e) {
            LOG.debug("No SchedJoules API available for context {}: {}", Autoboxing.I(contextId), e.getMessage());
            return false;
        }
    }

    ///////////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Filters the specified {@link JSONObject}
     *
     * @param content the {@link JSONObject} to filter
     * @param filteredFields the fields to remove
     * @return the filtered {@link JSONObject}
     * @throws OXException if a JSON error is occurred
     */
    private JSONObject filterContent(JSONObject content, Set<String> filteredFields) throws OXException {
        if (filteredFields == null || filteredFields.isEmpty()) {
            return content;
        }
        try {
            long startTime = System.currentTimeMillis();
            removeBlackListedItems(content);
            filterJSONObject(content, filteredFields);
            LOG.trace("Filtered content in {} msec.", System.currentTimeMillis() - startTime);
            return content;
        } catch (JSONException e) {
            throw SchedJoulesAPIExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Removes any black-listed items from the content
     *
     * @param content The content from which to remove the black-listed items
     */
    private void removeBlackListedItems(JSONObject content) {
        JSONArray pageSections = content.optJSONArray(SchedJoulesPageField.PAGE_SECTIONS.getFieldName());
        if (pageSections == null || pageSections.isEmpty()) {
            return;
        }

        Iterator<Object> iterator = pageSections.iterator();
        while (iterator.hasNext()) {
            JSONObject obj = (JSONObject) iterator.next();
            JSONArray items = obj.optJSONArray(SchedJoulesPageField.ITEMS.getFieldName());
            if (items == null || items.isEmpty()) {
                continue;
            }
            Iterator<Object> itemsIterator = items.iterator();
            while (itemsIterator.hasNext()) {
                JSONObject next = (JSONObject) itemsIterator.next();
                JSONObject item = next.optJSONObject(SchedJoulesPageField.ITEM.getFieldName());
                if (item == null || item.isEmpty()) {
                    continue;
                }
                int itemId = item.optInt(SchedJoulesPageField.ITEM_ID.getFieldName());
                if (blacklistedItems.contains(itemId)) {
                    itemsIterator.remove();
                }
            }
        }
    }

    /**
     * Filters the specified {@link JSONObject} and removes the specified fields
     *
     * @param content the {@link JSONObject} to filter
     * @return the filtered {@link JSONObject}
     * @throws OXException if a JSON error is occurred
     */
    private void filterJSONObject(JSONObject object, Set<String> filteredFields) throws JSONException, OXException {
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (filteredFields.contains(key)) {
                keys.remove();
            } else {
                filterObject(object.get(key), filteredFields);
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
    private void filterJSONArray(JSONArray array, Set<String> filteredFields) throws OXException, JSONException {
        for (int index = 0; index < array.length(); index++) {
            filterObject(array.get(index), filteredFields);
        }
    }

    /**
     * Filters the specified {@link JSONObject}
     *
     * @param array The {@link JSONObject} to filter
     * @throws OXException if a JSON error is occurred
     * @throws JSONException if JSON parsing error is occurred
     */
    private void filterObject(Object obj, Set<String> filteredFields) throws JSONException, OXException {
        if (obj instanceof JSONObject) {
            filterJSONObject((JSONObject) obj, filteredFields);
        } else if (obj instanceof JSONArray) {
            filterJSONArray((JSONArray) obj, filteredFields);
        }
    }

    /**
     * Handles the specified {@link ExecutionException}. If the cause of the exception is
     * an {@link OXException} then the cause is thrown, otherwise the {@link ExecutionException} is
     * wrapped in a the {@link SchedJoulesAPIExceptionCodes#UNEXPECTED_ERROR} exception.
     *
     * @param e the {@link ExecutionException} to handle
     * @return the wrapped {@link OXException}
     */
    private OXException handleExecutionException(ExecutionException e) {
        if (e.getCause() != null && e.getCause() instanceof OXException) {
            return (OXException) e.getCause();
        }
        return SchedJoulesAPIExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.config.Reloadable#reloadConfiguration(com.openexchange.config.ConfigurationService)
     */
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        initialiseBlackListedItems();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.config.Reloadable#getInterests()
     */
    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(SchedJoulesProperty.itemBlacklist.getFQPropertyName()).build();
    }
}
