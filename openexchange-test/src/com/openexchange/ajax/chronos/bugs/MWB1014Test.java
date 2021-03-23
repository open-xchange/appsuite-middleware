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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.chronos.bugs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractChronosTest;
import com.openexchange.ajax.chronos.util.DateTimeUtil;
import com.openexchange.java.util.TimeZones;
import com.openexchange.testing.httpclient.models.AlarmTriggerResponse;
import com.openexchange.time.TimeTools;

/**
 * {@link MWB1014Test}
 *
 * UI Error When Birthdays Disabled
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class MWB1014Test extends AbstractChronosTest {

    private Map<String, String> neededConfigurations;

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return neededConfigurations;
    }

    @Override
    protected String getReloadables() {
        return "CapabilityReloadable";
    }

    @Test
    public void testGetTriggersWithDeactivatedBirthdaysCalendar() throws Exception {
        /*
         * lookup birthdays calendar once (to ensure birthdays calendar account gets auto-provisioned)
         */
        assertNotNull("no birthdays calendar folder found", optBirthdayCalendarFolder(foldersApi));
        /*
         * disable birthdays calendar provider for user & ensure that the folder is no longer listed
         */
        neededConfigurations = Collections.singletonMap("com.openexchange.calendar.birthdays.enabled", "false");
        setUpConfiguration();
        assertNull("birthdays calendar folder still found", optBirthdayCalendarFolder(foldersApi));
        /*
         * get alarm triggers & expect no error (but allow a warning)
         */
        Date rangeEnd = TimeTools.D("next week at midnight", TimeZones.UTC);
        AlarmTriggerResponse alarmTriggerResponse = chronosApi.getAlarmTrigger(DateTimeUtil.getZuluDateTime(rangeEnd.getTime()).getValue(), null, null);
        checkResponse(alarmTriggerResponse.getError(), alarmTriggerResponse.getErrorDesc(), alarmTriggerResponse.getCategories(), true, alarmTriggerResponse.getData());
    }
}
