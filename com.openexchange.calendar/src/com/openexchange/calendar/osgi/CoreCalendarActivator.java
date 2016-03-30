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

package com.openexchange.calendar.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.caching.CacheService;
import com.openexchange.calendar.CalendarAdministration;
import com.openexchange.calendar.CalendarMySQL;
import com.openexchange.calendar.CalendarQuotaProvider;
import com.openexchange.calendar.CalendarReminderDelete;
import com.openexchange.calendar.api.AppointmentSqlFactory;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.calendar.cache.CalendarVolatileCache;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarAdministrationService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.reminder.TargetService;
import com.openexchange.java.Streams;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.quota.QuotaProvider;

/**
 * {@link CoreCalendarActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CoreCalendarActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link CoreCalendarActivator}.
     */
    public CoreCalendarActivator() {
        super();
    }

    @Override
    protected java.lang.Class<?>[] getNeededServices() {
        return new Class<?>[] { CacheService.class, DatabaseService.class, ConfigViewFactory.class };
    }


    @Override
    protected void startBundle() throws Exception {
        final AppointmentSqlFactory factory = new AppointmentSqlFactory();
        ITipActivator.initFeatures(factory);
        CalendarMySQL.setApppointmentSqlFactory(factory);

        CalendarMySQL.setServiceLookup(this);

        registerService(QuotaProvider.class, new CalendarQuotaProvider(new CalendarMySQL(), this));
        registerService(AppointmentSqlFactoryService.class, factory, null);
        registerService(CalendarCollectionService.class, new CalendarCollection(), null);
        registerService(CalendarAdministrationService.class, new CalendarAdministration(), null);
        final Dictionary<String, Integer> props = new Hashtable<String, Integer>(1, 1);
        props.put(TargetService.MODULE_PROPERTY, I(Types.APPOINTMENT));
        registerService(TargetService.class, new CalendarReminderDelete(), props);
        registerCacheRegion();

        track(ContactCollectorService.class, new ContactCollectorServiceTracker(this.context));
        openTrackers();
    }

    private void registerCacheRegion() throws OXException {
        /*
         * Important cache configuration constants
         */
        final String regionName = CalendarVolatileCache.REGION;
        final int maxObjects = 10000000;
        final int maxLifeSeconds = -1;
        final int idleTimeSeconds = 360;
        final int shrinkerIntervalSeconds = 60;
        /*
         * Compose cache configuration
         */
        final byte[] ccf = ("jcs.region."+regionName+"=LTCP\n" +
                "jcs.region."+regionName+".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
                "jcs.region."+regionName+".cacheattributes.MaxObjects="+maxObjects+"\n" +
                "jcs.region."+regionName+".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
                "jcs.region."+regionName+".cacheattributes.UseMemoryShrinker=true\n" +
                "jcs.region."+regionName+".cacheattributes.MaxMemoryIdleTimeSeconds="+idleTimeSeconds+"\n" +
                "jcs.region."+regionName+".cacheattributes.ShrinkerIntervalSeconds="+shrinkerIntervalSeconds+"\n" +
                "jcs.region."+regionName+".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
                "jcs.region."+regionName+".elementattributes.IsEternal=false\n" +
                "jcs.region."+regionName+".elementattributes.MaxLifeSeconds="+maxLifeSeconds+"\n" +
                "jcs.region."+regionName+".elementattributes.IdleTime="+idleTimeSeconds+"\n" +
                "jcs.region."+regionName+".elementattributes.IsSpool=false\n" +
                "jcs.region."+regionName+".elementattributes.IsRemote=false\n" +
                "jcs.region."+regionName+".elementattributes.IsLateral=false\n").getBytes();
        getService(CacheService.class).loadConfiguration(Streams.newByteArrayInputStream(ccf));
        CalendarVolatileCache.initInstance(context);
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterCacheRegion();
        cleanUp();
        super.stopBundle();
    }

    private void unregisterCacheRegion() {
        CalendarVolatileCache.dropInstance();
        final CacheService cacheService = getService(CacheService.class);
        if (null != cacheService) {
            try {
                cacheService.freeCache(CalendarVolatileCache.REGION);
            } catch (final OXException e) {
                // Ignore
            } catch (final RuntimeException e) {
                // Ignore
            }
        }
    }

}
