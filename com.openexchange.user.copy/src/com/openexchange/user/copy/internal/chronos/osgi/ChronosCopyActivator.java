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

package com.openexchange.user.copy.internal.chronos.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.internal.chronos.ChronosCopyTask;

/**
 * {@link ChronosCopyActivator}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ChronosCopyActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(ChronosCopyActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{
            CalendarStorageFactory.class,
            CalendarUtilities.class
        };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle {}", context.getBundle());
            registerService(CopyUserTaskService.class, new ChronosCopyTask(this), null);
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
