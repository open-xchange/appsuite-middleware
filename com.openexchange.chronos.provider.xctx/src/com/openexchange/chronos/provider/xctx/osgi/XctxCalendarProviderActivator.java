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

package com.openexchange.chronos.provider.xctx.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.xctx.XctxCalendarProvider;
import com.openexchange.chronos.provider.xctx.XctxFreeBusyProvider;
import com.openexchange.chronos.provider.xctx.XctxShareSubscriptionProvider;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.ShareService;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XctxCalendarProviderActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxCalendarProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link XctxCalendarProviderActivator}.
     */
    public XctxCalendarProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            FolderService.class, CalendarService.class, RecurrenceService.class, UserService.class, ConversionService.class, CalendarAccountService.class,
            CalendarStorageFactory.class, CalendarUtilities.class, LeanConfigurationService.class, XctxSessionManager.class, CapabilityService.class,
            GroupService.class, ShareService.class, DispatcherPrefixService.class, UserPermissionService.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(XctxCalendarProviderActivator.class).info("starting bundle {}", context.getBundle());
            XctxCalendarProvider calendarProvider = new XctxCalendarProvider(this);
            registerService(CalendarProvider.class, calendarProvider);
            registerService(ShareSubscriptionProvider.class, new XctxShareSubscriptionProvider(this, calendarProvider));
            registerService(FreeBusyProvider.class, new XctxFreeBusyProvider(this, calendarProvider));
        } catch (Exception e) {
            getLogger(XctxCalendarProviderActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(XctxCalendarProviderActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
