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

package com.openexchange.chronos.alarm.mail;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class Activator extends HousekeepingActivator{

    private ScheduledTimerTask scheduleAtFixedRate;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {ContextService.class, DatabaseService.class, TimerService.class, CalendarStorageFactory.class, CalendarUtilities.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final AddProcessedColumnUpdateTask task = new AddProcessedColumnUpdateTask();
        registerService(UpdateTaskProviderService.class.getName(), new UpdateTaskProviderService() {
            @Override
            public Collection<UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(((UpdateTaskV2) task));
            }
        });

        TimerService timerService = getService(TimerService.class);
        if(timerService==null) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }
        DatabaseService dbService = getService(DatabaseService.class);
        if(dbService==null) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        CalendarStorageFactory calendarStorageFactory = getService(CalendarStorageFactory.class);
        if(calendarStorageFactory==null) {
            throw ServiceExceptionCode.absentService(CalendarStorageFactory.class);
        }
        ContextService ctxService = getService(ContextService.class);
        if(ctxService==null) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        CalendarUtilities calUtil = getService(CalendarUtilities.class);
        if(calUtil==null) {
            throw ServiceExceptionCode.absentService(CalendarUtilities.class);
        }
        MailAlarmDeliveryWorker worker = new MailAlarmDeliveryWorker(calendarStorageFactory, dbService, ctxService, calUtil, timerService, 60, Calendar.MINUTE);
        scheduleAtFixedRate = timerService.scheduleAtFixedRate(worker, 0, 1, TimeUnit.MINUTES);
        registerService(CalendarHandler.class, new MailAlarmCalendarHandler(worker));
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        scheduleAtFixedRate.cancel(true);
    }



}
