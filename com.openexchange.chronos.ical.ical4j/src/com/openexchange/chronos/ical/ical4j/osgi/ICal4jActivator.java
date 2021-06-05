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

package com.openexchange.chronos.ical.ical4j.osgi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ical4j.handler.Alarm2ICalDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.Event2ICalDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.ICal2AlarmDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.ICal2AlarmsDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.ICal2EventDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.ICal2EventsDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.ICal2TimeZoneDataHandler;
import com.openexchange.chronos.ical.ical4j.handler.TimeZone2ICalDataHandler;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapper;
import com.openexchange.chronos.ical.impl.ICalServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.DataHandler;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.version.VersionService;

/**
 * {@link ICal4jActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ICal4jActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(ICal4jActivator.class);

    /**
     * Initializes a new {@link ICal4jActivator}.
     */
    public ICal4jActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }
    
    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { VersionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("starting bundle {}", context.getBundle());
            /*
             * register services
             */
            Services.setServiceLookup(this);
            ICalMapper mapper = new ICalMapper();
            registerService(ICalService.class, new ICalServiceImpl());
            /*
             * register data handlers
             */
            registerService(DataHandler.class, new Alarm2ICalDataHandler(mapper), singletonDictionary("identifier", DataHandlers.ALARM2ICAL));
            registerService(DataHandler.class, new ICal2AlarmsDataHandler(mapper), singletonDictionary("identifier", DataHandlers.ICAL2ALARMS));
            registerService(DataHandler.class, new ICal2AlarmDataHandler(mapper), singletonDictionary("identifier", DataHandlers.ICAL2ALARM));
            registerService(DataHandler.class, new Event2ICalDataHandler(mapper), singletonDictionary("identifier", DataHandlers.EVENT2ICAL));
            registerService(DataHandler.class, new ICal2EventsDataHandler(mapper), singletonDictionary("identifier", DataHandlers.ICAL2EVENTS));
            registerService(DataHandler.class, new ICal2EventDataHandler(mapper), singletonDictionary("identifier", DataHandlers.ICAL2EVENT));
            registerService(DataHandler.class, new TimeZone2ICalDataHandler(), singletonDictionary("identifier", DataHandlers.TIMEZONE2ICAL));
            registerService(DataHandler.class, new ICal2TimeZoneDataHandler(), singletonDictionary("identifier", DataHandlers.ICAL2TIMEZONE));
        } catch (Exception e) {
            LOG.error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle {}", context.getBundle());
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
