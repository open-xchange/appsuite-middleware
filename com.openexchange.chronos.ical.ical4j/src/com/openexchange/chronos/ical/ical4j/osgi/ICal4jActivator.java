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
