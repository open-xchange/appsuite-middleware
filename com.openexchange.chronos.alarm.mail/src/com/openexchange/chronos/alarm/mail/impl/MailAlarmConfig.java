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

package com.openexchange.chronos.alarm.mail.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.Property;

/**
 * {@link MailAlarmConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailAlarmConfig {

    private static final String PREFIX = "com.openexchange.calendar.alarm.mail.";

    /**
     * Defines the time in milliseconds an alarm mail should be send out before the trigger time.
     * With this property the admin can configure the average time needed by the mail system to send out the mail.
     * This way the mail should usually be send out on time and not a few seconds late.
     */
    public static final Property MAIL_SHIFT = DefaultProperty.valueOf(PREFIX + "time.shift", I(0));

    /**
     * Enabled or disables mail alarms.
     */
    public static final Property MAIL_ENABLED = DefaultProperty.valueOf(PREFIX + "enabled", Boolean.TRUE);

    /**
     * The amount of mails allowed to be sent in a given time-frame (see {@link #MAIL_LIMIT_TIME_FRAME})
     */
    public static final Property MAIL_LIMIT_AMOUNT = DefaultProperty.valueOf(PREFIX + "limit.amount", I(-1));

    /**
     * The timeframe in milliseconds used for the amount limit (see {@link #MAIL_LIMIT_AMOUNT}
     */
    public static final Property MAIL_LIMIT_TIME_FRAME = DefaultProperty.valueOf(PREFIX + "limit.timeframe", L(60000l));

}
