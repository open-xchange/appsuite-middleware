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

package com.openexchange.chronos.provider.schedjoules.osgi;

import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.schedjoules.BasicSchedJoulesCalendarProvider;
import com.openexchange.chronos.provider.schedjoules.SchedJoulesUserServiceInterceptor;
import com.openexchange.chronos.schedjoules.SchedJoulesService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.database.DatabaseService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;
import com.openexchange.user.interceptor.UserServiceInterceptor;

/**
 * {@link SchedJoulesProviderActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesProviderActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link SchedJoulesProviderActivator}.
     */
    public SchedJoulesProviderActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, 
                                AdministrativeCalendarAccountService.class, 
                                SchedJoulesService.class, 
                                DatabaseService.class, 
                                CalendarUtilities.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);

        registerService(CalendarProvider.class, new BasicSchedJoulesCalendarProvider());
        registerService(UserServiceInterceptor.class, new SchedJoulesUserServiceInterceptor(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);

        super.stopBundle();
    }
}
