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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.mail.categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map.Entry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link CachedMailCategoriesConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class CachedMailCategoriesConfigService implements MailCategoriesConfigService, Reloadable {

    private static final Logger LOG = LoggerFactory.getLogger(CachedMailCategoriesConfigService.class);
    private static final String REGION = "Categories_Config_Cache";
    private MailCategoriesConfigService delegate;

    /**
     * Initializes a new {@link CachedMailCategoriesConfigService}.
     * 
     * @throws OXException
     */
    public CachedMailCategoriesConfigService(CachedMailCategoriesConfigService delegate, Properties properties) throws OXException {
        super();
        this.delegate = delegate;
        initCache(properties);

    }

    @Override
    public List<MailCategoryConfig> getAllCategories(Session session, boolean onlyEnabled) throws OXException {
        Cache cache = getCache();
        MailCategoriesConfigCacheEntry cacheEntry = (MailCategoriesConfigCacheEntry) cache.get(cache.newCacheKey(session.getContextId(), session.getUserId()));
        if (cacheEntry == null) {
            List<MailCategoryConfig> configs = delegate.getAllCategories(session, false);
            if (configs == null || configs.isEmpty()) {
                return Collections.emptyList();
            }
            cache.put(cache.newCacheKey(session.getContextId(), session.getUserId()), new MailCategoriesConfigCacheEntry(configs), true);
            if (onlyEnabled) {
                List<MailCategoryConfig> result = new ArrayList<>(configs.size());
                for (MailCategoryConfig config : configs) {
                    if (config.isActive() || config.isForced()) {
                        result.add(config);
                    }
                }
                return result;
            } else {
                return configs;
            }
        }
        return cacheEntry.getAll();
    }

    @Override
    public MailCategoryConfig getConfigByCategory(Session session, String name) throws OXException {
        Cache cache = getCache();
        MailCategoriesConfigCacheEntry cacheEntry = (MailCategoriesConfigCacheEntry) cache.get(cache.newCacheKey(session.getContextId(), session.getUserId()));
        if (cacheEntry == null) {
            List<MailCategoryConfig> configs = delegate.getAllCategories(session, false);
            if (configs == null || configs.isEmpty()) {
                return null;
            }
            MailCategoriesConfigCacheEntry entry = new MailCategoriesConfigCacheEntry(configs);
            cache.put(cache.newCacheKey(session.getContextId(), session.getUserId()), entry, true);
            return entry.getByName(name);
        }
        return cacheEntry.getByName(name);
    }

    @Override
    public MailCategoryConfig getConfigByFlag(Session session, String flag) throws OXException {
        Cache cache = getCache();
        MailCategoriesConfigCacheEntry cacheEntry = (MailCategoriesConfigCacheEntry) cache.get(cache.newCacheKey(session.getContextId(), session.getUserId()));
        if (cacheEntry == null) {
            List<MailCategoryConfig> configs = delegate.getAllCategories(session, false);
            if (configs == null || configs.isEmpty()) {
                return null;
            }
            MailCategoriesConfigCacheEntry entry = new MailCategoriesConfigCacheEntry(configs);
            cache.put(cache.newCacheKey(session.getContextId(), session.getUserId()), entry, true);
            return entry.getByFlag(flag);
        }
        return cacheEntry.getByName(flag);
    }

    private void initCache(Properties properties) throws OXException {
        Properties customProperties = new Properties();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            if (null != entry.getKey() && String.class.isInstance(entry.getKey())) {
                customProperties.put(((String) entry.getKey()).replace("[REGIONNAME]", REGION), entry.getValue());
            }
        }
        ServerServiceRegistry.getInstance().getService(CacheService.class).loadConfiguration(customProperties);
    }

    private Cache getCache() throws OXException {
        return ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            getCache().clear();
        } catch (OXException e) {
            LOG.error("Unable to clear the cache: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        return null;
    }

}
