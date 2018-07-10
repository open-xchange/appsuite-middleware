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

import java.sql.Connection;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTriggerWrapper;
import com.openexchange.chronos.storage.AdministrativeCalendarStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.timer.TimerService;

/**
 * {@link MailAlarmDeliveryWorker}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailAlarmDeliveryWorker implements Runnable{

    private final AdministrativeCalendarStorage storage;
    private final DatabaseService dbservice;
    private final int calendarType, timeframe;
    private final ContextService ctxService;
    private final TimerService timerService;

    /**
     * Initializes a new {@link MailAlarmDeliveryWorker}.
     */
    public MailAlarmDeliveryWorker(AdministrativeCalendarStorage storage, DatabaseService dbservice, ContextService ctxService, TimerService timerService, int timeframe, int calendarType) {
        this.storage = storage;
        this.dbservice = dbservice;
        this.timeframe = timeframe;
        this.calendarType = calendarType;
        this.ctxService = ctxService;
        this.timerService = timerService;
    }

    @Override
    public void run() {
        Calendar until = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        until.add(calendarType, timeframe);
        try {
            List<Integer> ctxIds = ctxService.getDistinctContextsPerSchema();

            for (Integer ctxId : ctxIds) {

                Connection con = dbservice.getForUpdateTask(ctxId);
                try {
                    List<AlarmTriggerWrapper> lockedTriggers = storage.getAlarmTriggerStorage().getAndLockTriggers(con, until.getTime());
                    storage.getAlarmTriggerStorage().setProcessingStatus(con, lockedTriggers, 1);

                    for(AlarmTriggerWrapper trigger: lockedTriggers) {

                        Alarm alarm = storage.getAlarmStorage().getAlarm(con, trigger.getCtx(), trigger.getAccount(), trigger.getAlarmTrigger().getAlarm());
                        // TODO deliver the alarm
                    }


                } finally {
                    dbservice.backForUpdateTask(ctxId, con);
                }

            }
        } catch (OXException e) {
            //TODO log error message
        }
    }



}
