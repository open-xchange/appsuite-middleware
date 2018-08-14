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

package com.openexchange.chronos.provider.internal.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.internal.InternalCalendarProvider;
import com.openexchange.chronos.provider.internal.InternalFreeBusyProvider;
import com.openexchange.chronos.provider.internal.config.DefaultAlarmDate;
import com.openexchange.chronos.provider.internal.config.DefaultAlarmDateTime;
import com.openexchange.chronos.provider.internal.config.DefaultFolderId;
import com.openexchange.chronos.provider.internal.config.RestrictAllowedAttendeeChanges;
import com.openexchange.chronos.provider.internal.config.RestrictAllowedAttendeeChangesPublic;
import com.openexchange.chronos.provider.internal.share.CalendarFolderHandlerModuleExtension;
import com.openexchange.chronos.provider.internal.share.CalendarModuleAdjuster;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.core.ModuleAdjuster;
import com.openexchange.share.groupware.spi.FolderHandlerModuleExtension;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import com.openexchange.user.UserService;

/**
 * {@link InternalCalendarProviderActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalCalendarProviderActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link InternalCalendarProviderActivator}.
     */
    public InternalCalendarProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            FolderService.class, CalendarService.class, RecurrenceService.class, UserService.class, ConversionService.class, ConfigurationService.class,
            CalendarAccountService.class, CalendarStorageFactory.class, CalendarUtilities.class
        };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { JSlobService.class, JSlobStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(InternalCalendarProviderActivator.class).info("starting bundle {}", context.getBundle());
            trackService(FolderUserPropertyStorage.class);
            openTrackers();
            registerService(CalendarProvider.class, new InternalCalendarProvider(this));
            registerService(FreeBusyProvider.class, new InternalFreeBusyProvider(this));
            registerService(ModuleAdjuster.class, new CalendarModuleAdjuster());
            registerService(FolderHandlerModuleExtension.class, new CalendarFolderHandlerModuleExtension(this));
            /*
             * register JSlob entries
             */
            registerService(JSlobEntry.class, new DefaultFolderId(this));
            registerService(JSlobEntry.class, new RestrictAllowedAttendeeChanges(this));
            registerService(JSlobEntry.class, new RestrictAllowedAttendeeChangesPublic(this));
            registerService(JSlobEntry.class, new DefaultAlarmDate(this));
            registerService(JSlobEntry.class, new DefaultAlarmDateTime(this));
        } catch (Exception e) {
            getLogger(InternalCalendarProviderActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(InternalCalendarProviderActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
