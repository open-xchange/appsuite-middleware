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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.service.OnboardingConfigurationService;
import com.openexchange.onboarding.service.OnboardingView;
import com.openexchange.session.Session;

/**
 * {@link OnboardingConfigurationRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingConfigurationRegistry extends ServiceTracker<OnboardingConfiguration, OnboardingConfiguration> implements OnboardingConfigurationService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OnboardingConfigurationRegistry.class);

    private final ConcurrentMap<String, OnboardingConfiguration> configurations;

    /**
     * Initializes a new {@link OnboardingConfigurationRegistry}.
     *
     * @param context The bundle context
     */
    public OnboardingConfigurationRegistry(BundleContext context) {
        super(context, OnboardingConfiguration.class, null);
        configurations = new ConcurrentHashMap<String, OnboardingConfiguration>(32, 0.9F, 1);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    public Collection<OnboardingConfiguration> getAllConfigurations() throws OXException {
        return Collections.unmodifiableCollection(configurations.values());
    }

    @Override
    public OnboardingConfiguration getConfiguration(String id) throws OXException {
        OnboardingConfiguration configuration = null == id ? null : configurations.get(id);
        if (null == configuration) {
            throw OnboardingExceptionCodes.NOT_FOUND.create(null == id ? "null" : id);
        }
        return configuration;
    }

    @Override
    public Collection<OnboardingConfiguration> getAvailableConfigurationsFor(Session session) throws OXException {
        return Collections.unmodifiableCollection(configurations.values());
    }

    @Override
    public OnboardingView getViewFor(Session session) throws OXException {
        OnboardingViewImpl view = new OnboardingViewImpl();
        view.add(configurations.values(), session);
        return view;
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    @Override
    public OnboardingConfiguration addingService(ServiceReference<OnboardingConfiguration> reference) {
        OnboardingConfiguration configuration = context.getService(reference);
        if (null == configurations.putIfAbsent(configuration.getId(), configuration)) {
            return configuration;
        }

        LOG.warn("An on-boarding configuration already exists with identifier {}. Ignoring {}", configuration.getId(), configuration.getClass().getName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(ServiceReference<OnboardingConfiguration> reference, OnboardingConfiguration configuration) {
        configurations.remove(configuration.getId());
        context.ungetService(reference);
    }

}
