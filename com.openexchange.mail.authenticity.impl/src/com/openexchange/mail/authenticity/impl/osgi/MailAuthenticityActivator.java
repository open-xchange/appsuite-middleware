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

package com.openexchange.mail.authenticity.impl.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.mail.MailFetchListener;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.mail.authenticity.impl.MailAuthenticityFetchListener;
import com.openexchange.mail.authenticity.impl.MailAuthenticityHandlerImpl;
import com.openexchange.mail.authenticity.impl.MailAuthenticityHandlerRegistryImpl;
import com.openexchange.mail.authenticity.impl.MailAuthenticityProperty;
import com.openexchange.mail.authenticity.impl.TrustedDomainAuthenticityHandler;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MailAuthenticityActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailAuthenticityActivator}.
     */
    public MailAuthenticityActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class, ConfigurationService.class, ManagedFileManagement.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // It is OK to pass service references since 'stopOnServiceUnavailability' returns 'true'
        final MailAuthenticityHandlerRegistryImpl registry = new MailAuthenticityHandlerRegistryImpl(getService(LeanConfigurationService.class), context);
        registerService(MailAuthenticityHandlerRegistry.class, registry);
        track(MailAuthenticityHandler.class, registry);
        trackService(TrustedDomainAuthenticityHandler.class);
        openTrackers();

        ConfigurationService configurationService = getService(ConfigurationService.class);
        ManagedFileManagement managedFileManagement = getService(ManagedFileManagement.class);
        TrustedDomainAuthenticityHandler authenticationHandler = new TrustedDomainAuthenticityHandler(configurationService, managedFileManagement);
        registerService(ForcedReloadable.class, authenticationHandler);
        registerService(TrustedDomainAuthenticityHandler.class, authenticationHandler);


        final MailAuthenticityHandlerImpl handlerImpl = new MailAuthenticityHandlerImpl(this);
        registerService(MailAuthenticityHandler.class, handlerImpl);

        registerService(Reloadable.class, new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                registry.invalidateCache();
                handlerImpl.invalidateAuthServIdsCache();
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties(
                    MailAuthenticityProperty.enabled.getFQPropertyName(),
                    MailAuthenticityProperty.threshold.getFQPropertyName(),
                    MailAuthenticityProperty.authServId.getFQPropertyName()
                );
            }
        });

        MailAuthenticityFetchListener fetchListener = new MailAuthenticityFetchListener(registry);
        registerService(MailFetchListener.class, fetchListener);
    }

}
