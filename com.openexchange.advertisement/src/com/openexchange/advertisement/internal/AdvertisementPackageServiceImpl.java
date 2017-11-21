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

package com.openexchange.advertisement.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.advertisement.AdvertisementConfigService;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.ResellerExceptionCodes;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;

/**
 * {@link AdvertisementPackageServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class AdvertisementPackageServiceImpl implements AdvertisementPackageService {

    private static final Logger LOG = LoggerFactory.getLogger(AdvertisementConfigService.class);
    private static final String CONFIG_SUFFIX = ".packageScheme";

    private final ResellerService resellerService;
    private final AtomicReference<AdvertisementConfigService> globalReference;
    private final AtomicReference<Map<String, String>> reseller2Scheme;
    private final ConcurrentMap<String, AdvertisementConfigService> configServices;

    /**
     * Initializes a new {@link AdvertisementPackageServiceImpl}.
     *
     * @param resellerService The reseller service to use
     * @param configService The configuration service to use
     * @throws OXException If initialization fails
     */
    public AdvertisementPackageServiceImpl(ResellerService resellerService, ConfigurationService configService) throws OXException {
        super();
        this.resellerService = resellerService;
        reseller2Scheme = new AtomicReference<Map<String,String>>(computeReseller2SchemeMapping(configService));
        configServices = new ConcurrentHashMap<>(8, 0.9F, 1);
        globalReference = new AtomicReference<AdvertisementConfigService>(null);
    }

    private Map<String, String> computeReseller2SchemeMapping(ConfigurationService configService) throws OXException {
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        StringBuilder propNameBuilder = new StringBuilder(AdvertisementConfigService.CONFIG_PREFIX);
        int reslen = propNameBuilder.length();
        boolean containsDefault = false;
        for (ResellerAdmin res: resellerService.getAll()){
            String packageScheme = configService.getProperty(AdvertisementConfigService.CONFIG_PREFIX + res.getName() + CONFIG_SUFFIX);
            if (packageScheme == null) {
                // Fall-back to reseller identifier
                packageScheme = configService.getProperty(AdvertisementConfigService.CONFIG_PREFIX + res.getId() + CONFIG_SUFFIX);

                if (packageScheme == null) {
                    // Fall-back to global as last resort
                    packageScheme = DEFAULT_SCHEME_ID;
                }
            }
            if(res.getName().equals(DEFAULT_RESELLER)){
                containsDefault=true;
            }
            map.put(res.getName(), packageScheme);
        }

        if (!containsDefault) {
            // Add 'default' as a default reseller
            String oxall = DEFAULT_RESELLER;

            propNameBuilder.setLength(reslen);
            String packageScheme = configService.getProperty(propNameBuilder.append(oxall).append(CONFIG_SUFFIX).toString());
            if (packageScheme == null) {
                //fallback to global
                packageScheme = DEFAULT_SCHEME_ID;
            }
            map.put(oxall, packageScheme);
        }
        return map.build();
    }

    @Override
    public AdvertisementConfigService getScheme(int contextId) {
        // Resolve context tom its reseller name
        String reseller;
        try {
            ResellerAdmin admin = resellerService.getReseller(contextId);
            reseller = admin.getName();
        } catch (OXException e) {
            if (ResellerExceptionCodes.NO_RESELLER_FOUND.equals(e) || ResellerExceptionCodes.NO_RESELLER_FOUND_FOR_CTX.equals(e)) {
                reseller = DEFAULT_RESELLER;
            } else {
                return globalReference.get();
            }
        }

        String scheme = reseller2Scheme.get().get(reseller);
        if (null == scheme) {
            // No such reseller known... Assume global
            return globalReference.get();
        }

        AdvertisementConfigService result = configServices.get(scheme);
        if (result == null) {
            result = globalReference.get();
        }
        return result;
    }

    @Override
    public AdvertisementConfigService getDefaultScheme() {
        return globalReference.get();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            reseller2Scheme.set(computeReseller2SchemeMapping(configService));
        } catch (OXException e) {
            LOG.error("Error while reloading configuration: " + e.getMessage());
        }
    }

    /**
     * Adds newly appeared advertisement configuration and reloads this advertisement package service.
     *
     * @param advertisementConfig The advertisement configuration to add
     * @return <code>true</code> if successfully added; otherwise <code>false</code>
     */
    public boolean addServiceAndReload(AdvertisementConfigService advertisementConfig) {
        if (null == advertisementConfig || (null != configServices.putIfAbsent(advertisementConfig.getSchemeId(), advertisementConfig))) {
            // Either null or there is already such an AdvertisementConfigService
            return false;
        }

        if (DEFAULT_SCHEME_ID.equals(advertisementConfig.getSchemeId())) {
            globalReference.set(advertisementConfig);
        }
        return true;
    }

    /**
     * Removes disappeared advertisement configuration and reloads this advertisement package service.
     *
     * @param advertisementConfig The advertisement configuration to remove
     */
    public void removeServiceAndReload(AdvertisementConfigService advertisementConfig) {
        if (null != advertisementConfig) {
            AdvertisementConfigService removed = configServices.remove(advertisementConfig.getSchemeId());
            if (null != removed && DEFAULT_SCHEME_ID.equals(removed.getSchemeId())) {
                globalReference.set(null);
            }
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

}
