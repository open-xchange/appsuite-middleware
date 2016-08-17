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

package com.openexchange.client.onboarding.rmi.impl;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.client.onboarding.Device;
import com.openexchange.client.onboarding.OnboardingProvider;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.rmi.RemoteOnboardingService;
import com.openexchange.client.onboarding.rmi.RemoteOnboardingServiceException;
import com.openexchange.client.onboarding.service.OnboardingService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Collators;


/**
 * {@link RemoteOnboardingServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class RemoteOnboardingServiceImpl implements RemoteOnboardingService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RemoteOnboardingServiceImpl.class);

    private final OnboardingService service;
    private final Comparator<String> comparator;

    /**
     * Initializes a new {@link RemoteOnboardingServiceImpl}.
     */
    public RemoteOnboardingServiceImpl(OnboardingService service) {
        super();
        this.service = service;

        // Generate comparator
        final Collator collator = Collators.getSecondaryInstance(Locale.US);
        this.comparator = new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                return collator.compare(s1, s2);
            }
        };
    }

    @Override
    public List<List<String>> getAllProviders() throws RemoteOnboardingServiceException, RemoteException {
        try {
            // Get providers
            Collection<OnboardingProvider> providers = service.getAllProviders();

            // Build up mapping
            Map<String, String> descMap = new HashMap<String, String>(providers.size());
            Map<String, List<String>> devicesMap = new HashMap<String, List<String>>(providers.size());
            Map<String, List<String>> typesMap = new HashMap<String, List<String>>(providers.size());
            List<String> ids = new ArrayList<String>(providers.size());
            for (OnboardingProvider provider : providers) {
                String id = provider.getId();
                ids.add(id);

                descMap.put(id, provider.getDescription());

                {
                    Set<Device> devices = provider.getSupportedDevices();
                    List<String> sDevices = new ArrayList<String>(devices.size());
                    for (Device device : devices) {
                        sDevices.add(device.getId());
                    }
                    Collections.sort(sDevices, comparator);
                    devicesMap.put(id, sDevices);
                }

                {
                    Set<OnboardingType> types = provider.getSupportedTypes();
                    List<String> sTypes = new ArrayList<String>(types.size());
                    for (OnboardingType type : types) {
                        sTypes.add(type.getId());
                    }
                    Collections.sort(sTypes, comparator);
                    typesMap.put(id, sTypes);
                }
            }

            // Sort identifiers
            Collections.sort(ids, comparator);

            // Compile data
            List<List<String>> data = new ArrayList<List<String>>(ids.size());
            for (String id : ids) {
                data.add(Arrays.asList(id, descMap.get(id), toCsl(typesMap.get(id)), toCsl(devicesMap.get(id))));
            }

            // Return the sorted identifiers
            return data;
        } catch (OXException e) {
            throw convertException(e);
        } catch (RuntimeException e) {
            throw convertException(e);
        }
    }

    private String toCsl(List<String> strings) {
        int size = strings.size();
        if (size == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(size << 2);
        sb.append(strings.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(", ").append(strings.get(i));
        }
        return sb.toString();
    }

    private RemoteOnboardingServiceException convertException(Exception e) {
        LOGGER.error("Error during {} invocation", RemoteOnboardingService.class.getSimpleName(), e);
        RemoteOnboardingServiceException cme = new RemoteOnboardingServiceException(e.getMessage());
        cme.setStackTrace(e.getStackTrace());
        return cme;
    }

}
