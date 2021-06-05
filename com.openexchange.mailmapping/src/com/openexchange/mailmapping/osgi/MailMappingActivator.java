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

package com.openexchange.mailmapping.osgi;

import static com.openexchange.osgi.Tools.withRanking;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.MailResolverService;
import com.openexchange.mailmapping.impl.DefaultMailMappingService;
import com.openexchange.mailmapping.impl.MailResolverServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * {@link MailMappingActivator} - The activator for <i>com.openexchange.mailmapping</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class MailMappingActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailMappingActivator}.
     */
    public MailMappingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, ContextService.class, DatabaseService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(MailResolver.class, new DefaultMailMappingService(this), withRanking(Integer.valueOf(Integer.MIN_VALUE)));

        MailResolverServiceImpl osgiMailMappingService = new MailResolverServiceImpl();
        track(MailResolver.class, osgiMailMappingService);
        openTrackers();

        registerService(MailResolverService.class, osgiMailMappingService);
    }

}
