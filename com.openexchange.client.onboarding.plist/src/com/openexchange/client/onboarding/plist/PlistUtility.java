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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.client.onboarding.plist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.openexchange.client.onboarding.BuiltInProvider;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link PlistUtility} - A utility class for PLIST-related client on-boarding stuff.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class PlistUtility {

    /**
     * Initializes a new {@link PlistUtility}.
     */
    private PlistUtility() {
        super();
    }

    /**
     * Looks-up the available PLIST provider by specified identifier.
     *
     * @param id The provider identifier to look-up by
     * @param onboardingService The client on-boarding service to use
     * @return The optional PLIST provider
     * @throws OXException If PLIST provider cannot be returned
     */
    public static Optional<OnboardingPlistProvider> lookUpPlistProviderById(String id, OnboardingService onboardingService) throws OXException {
        if (Strings.isEmpty(id)) {
            return Optional.empty();
        }

        try {
            OnboardingProvider provider = onboardingService.getProvider(id);
            return provider instanceof OnboardingPlistProvider ? Optional.of((OnboardingPlistProvider) provider) : Optional.empty();
        } catch (OXException e) {
            if (OnboardingExceptionCodes.NOT_FOUND.equals(e)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Gets all available PLIST providers offered by given service.
     *
     * @param onboardingService The client on-boarding service to use
     * @return All available PLIST providers (might be an empty list)
     * @throws OXException If retrieval of available PLIST providers fails
     */
    public static List<OnboardingPlistProvider> getAllAvailablePlistProviders(OnboardingService onboardingService) throws OXException {
        Collection<OnboardingProvider> providers = onboardingService.getAllProviders();
        List<OnboardingPlistProvider> retval = null;
        for (OnboardingProvider provider : providers) {
            if (provider instanceof OnboardingPlistProvider) {
                if (retval == null) {
                    retval = new ArrayList<OnboardingPlistProvider>(providers.size());
                }
                retval.add((OnboardingPlistProvider) provider);
            }
        }
        return retval == null ? Collections.emptyList() : retval;
    }

    /**
     * Puts the referenced built-in PLIST provider into specified map.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Referenced provider is expected to be of type {@link OnboardingPlistProvider}.
     * </div>
     *
     * @param builtInProvider The built-in provider
     * @param providers The map to put into
     * @param service The client on-boarding service to use
     * @throws OXException If there is no such provider associated with given identifier or looked-up provider is not suitable to generate a PLIST dictionary
     * @throws IllegalArgumentException If given provider identifier is <code>null</code> or empty or any other argument is <code>null</code>
     */
    public static void putPlistProviderById(BuiltInProvider builtInProvider, Map<String, OnboardingPlistProvider> providers, OnboardingService service) throws OXException {
        putPlistProviderById(builtInProvider.getId(), providers, service);
    }

    /**
     * Puts the PLIST provider associated with given identifier into specified map.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Referenced provider is expected to be of type {@link OnboardingPlistProvider}.
     * </div>
     *
     * @param id The provider identifier
     * @param providers The map to put into
     * @param service The client on-boarding service to use
     * @throws OXException If there is no such provider associated with given identifier or looked-up provider is not suitable to generate a PLIST dictionary
     * @throws IllegalArgumentException If given provider identifier is <code>null</code> or empty or any other argument is <code>null</code>
     */
    public static void putPlistProviderById(String id, Map<String, OnboardingPlistProvider> providers, OnboardingService service) throws OXException {
        if (Strings.isEmpty(id)) {
            throw new IllegalArgumentException("Provider identifier must not be null or empty");
        }
        if (providers == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (service == null) {
            throw new IllegalArgumentException("Service must not be null");
        }

        OnboardingProvider provider = service.getProvider(id);
        if (!(provider instanceof OnboardingPlistProvider)) {
            throw OnboardingExceptionCodes.NO_PLIST_PROVIDER.create(id);
        }
        providers.put(id, (OnboardingPlistProvider) provider);
    }

}
