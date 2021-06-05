/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.advertisement.impl.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.impl.osgi.Services;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link TaxonomyTypesAdvertisementConfigService} is an implementation of the {@link AdvertisementConfigService} based on taxonomy/types.
 *
 * It compares the taxonomy/types of the given context with a configured list of packages (com.openexchange.advertisement.taxonomy.types) and returns the first matching item.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class TaxonomyTypesAdvertisementConfigService extends AbstractAdvertisementConfigService implements Reloadable {

    private static final Logger LOG = LoggerFactory.getLogger(TaxonomyTypesAdvertisementConfigService.class);

    private static final String TAXONOMY_TYPES = "taxonomy/types";
    private static final String TAXONOMY_TYPES_CONFIG_CASCADE = "config/com.openexchange.config.cascade.types";
    private static final String TAXONOMY_TYPE_CONFIGURATION = ".taxonomy.types";

    private static volatile TaxonomyTypesAdvertisementConfigService instance;

    /**
     * Gets the instance of {@code TaxonomyTypesAdvertisementConfigService}.
     *
     * @return The instance
     * @throws OXException
     */
    public static TaxonomyTypesAdvertisementConfigService getInstance() throws OXException {
        TaxonomyTypesAdvertisementConfigService tmp = instance;
        if (null == tmp) {
            synchronized (TaxonomyTypesAdvertisementConfigService.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new TaxonomyTypesAdvertisementConfigService();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    private final Map<String, String[]> reseller2Types = new ConcurrentHashMap<>(1);


    // ------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link TaxonomyTypesAdvertisementConfigService}.
     *
     * @throws OXException
     */
    private TaxonomyTypesAdvertisementConfigService() throws OXException {
        super();
        ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        ResellerService resellerService = Services.getService(ResellerService.class);
        for (ResellerAdmin res : resellerService.getAll()) {
            String typesString = configurationService.getProperty(AdvertisementConfigService.CONFIG_PREFIX + res.getName() + TAXONOMY_TYPE_CONFIGURATION);
            if (typesString != null) {
                String[] possibleTypes = Strings.splitByComma(typesString);
                reseller2Types.put(res.getName(), possibleTypes);
            }
        }
    }

    @Override
    protected String getReseller(int contextId) throws OXException {
        ResellerService resellerService = Services.getService(ResellerService.class);
        ResellerAdmin resellerAdmin = resellerService.getReseller(contextId);
        return resellerAdmin.getName();
    }

    @Override
    protected String getPackage(Session session) throws OXException {
        // Retrieve possible taxonomy types
        String reseller = null;
        try {
            reseller = this.getReseller(session.getContextId());
        } catch (OXException e) {
            reseller = RESELLER_ALL;
        }

        String[] possibleTypes = reseller2Types.get(reseller);

        if (possibleTypes == null || possibleTypes.length == 0) {
            return PACKAGE_ALL;
        }

        // check user taxonomy types
        UserService userService = Services.getService(UserService.class);
        User user = userService.getUser(session.getUserId(), session.getContextId());
        Map<String, String> userAttributes = user.getAttributes();
        List<String> packs = new ArrayList<>();
        {
            String taxonomyType = userAttributes.get(TAXONOMY_TYPES_CONFIG_CASCADE);
            if (null != taxonomyType) {
                packs.addAll(Arrays.asList(Strings.splitByComma(taxonomyType)));
            }
        }

        for (String type : possibleTypes) {
            if (packs.contains(type)) {
                return type;
            }
        }

        //check context taxonomy types
        ContextService ctxService = Services.getService(ContextService.class);
        Context ctx = ctxService.getContext(session.getContextId());
        Map<String, List<String>> attributes = ctx.getAttributes();
        packs.clear();
        {
            List<String> taxonomyTypes = attributes.get(TAXONOMY_TYPES);
            if (null != taxonomyTypes) {
                for (String types : taxonomyTypes) {
                    packs.addAll(Arrays.asList(Strings.splitByComma(types)));
                }
            }
        }

        for (String type : possibleTypes) {
            if (packs.contains(type)) {
                return type;
            }
        }

        return PACKAGE_ALL;
    }

    @Override
    public String getSchemeId() {
        return "TaxonomyTypes";
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        reseller2Types.clear();
        ResellerService resellerService = Services.getService(ResellerService.class);
        try {
            for (ResellerAdmin res : resellerService.getAll()) {
                String typesString = configService.getProperty(AdvertisementConfigService.CONFIG_PREFIX + res.getName() + TAXONOMY_TYPE_CONFIGURATION);
                if (typesString != null) {
                    String[] possibleTypes = Strings.splitByComma(typesString);
                    reseller2Types.put(res.getName(), possibleTypes);
                }
            }
            // add default reseller
            String typesString = configService.getProperty(AdvertisementConfigService.CONFIG_PREFIX + AdvertisementConfigService.RESELLER_ALL + TAXONOMY_TYPE_CONFIGURATION);
            if (typesString != null) {
                String[] possibleTypes = Strings.splitByComma(typesString);
                reseller2Types.put(AdvertisementConfigService.RESELLER_ALL, possibleTypes);
            }
        } catch (OXException e) {
            LOG.error("Unable to reload reseller types", e);
            return;
        }
        CacheService cacheService = Services.getService(CacheService.class);
        if (cacheService != null) {
            Cache cache;
            try {
                cache = cacheService.getCache(AbstractAdvertisementConfigService.CACHING_REGION);
                cache.clear();
            } catch (OXException e) {
                LOG.error("Unable to clear advertisement cache", e);
                return;
            }

        }

    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

}
