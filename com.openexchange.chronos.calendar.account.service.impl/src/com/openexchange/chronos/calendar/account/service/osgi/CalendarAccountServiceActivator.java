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

package com.openexchange.chronos.calendar.account.service.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.calendar.account.service.groupware.CalendarAccountDeleteListener;
import com.openexchange.chronos.calendar.account.service.impl.CalendarAccountServiceImpl;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link CalendarAccountServiceActivator}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class CalendarAccountServiceActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarAccountServiceActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ContextService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { CalendarProviderRegistry.class, DatabaseService.class, CalendarStorageFactory.class, CapabilityService.class, ConfigViewFactory.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle {}", context.getBundle());
            CalendarAccountServiceImpl accountService = new CalendarAccountServiceImpl(this);
            registerService(CalendarAccountService.class, accountService);
            registerService(AdministrativeCalendarAccountService.class, accountService);
            registerService(DeleteListener.class, new CalendarAccountDeleteListener(this));
            openTrackers();
        } catch (Exception e) {
            LOG.error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }

}
