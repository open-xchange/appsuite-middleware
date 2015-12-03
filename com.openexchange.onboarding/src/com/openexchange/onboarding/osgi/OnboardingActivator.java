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

package com.openexchange.onboarding.osgi;

import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.onboarding.internal.OnboardingServiceImpl;
import com.openexchange.onboarding.internal.OnboardingInit;
import com.openexchange.onboarding.service.OnboardingService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.uadetector.UserAgentParser;
import com.openexchange.user.UserService;

/**
 * {@link OnboardingActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingActivator extends HousekeepingActivator {

    private volatile OnboardingServiceImpl registry;

    /**
     * Initializes a new {@link OnboardingActivator}.
     */
    public OnboardingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ConfigViewFactory.class, ConfigurationService.class, MimeTypeMap.class, UserAgentParser.class, ContextService.class,
            TranslatorFactory.class, ServerConfigService.class, CapabilityService.class, NotificationMailFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        OnboardingServiceImpl registry = new OnboardingServiceImpl(context, OnboardingInit.initScenarios(getService(ConfigurationService.class)));
        registry.open();
        this.registry = registry;
        addService(OnboardingService.class, registry);

        registerService(OnboardingService.class, registry);
        registerService(Reloadable.class, registry);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();

        OnboardingServiceImpl registry = this.registry;
        if (null != registry) {
            this.registry = null;
            removeService(OnboardingService.class);
            registry.close();
        }

        Services.setServiceLookup(null);
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        return super.removeService(clazz);
    }

}
