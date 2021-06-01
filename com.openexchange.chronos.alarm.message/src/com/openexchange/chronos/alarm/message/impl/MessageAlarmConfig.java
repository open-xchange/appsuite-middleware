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

package com.openexchange.chronos.alarm.message.impl;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.Property;

/**
 * {@link MessageAlarmConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MessageAlarmConfig {
    private static final String PREFIX = "com.openexchange.calendar.alarm.message.backgroundWorker.";

    /**
     * Defines the time in minutes between executions of the message delivery worker.
     */
    public static final Property PERIOD = DefaultProperty.valueOf(PREFIX + "period", I(30));

    /**
     * Defines the initial delay in minutes after which the message delivery worker runs for the first time.
     */
    public static final Property INITIAL_DELAY = DefaultProperty.valueOf(PREFIX + "initialDelay", I(10));

    /**
     * Defines the time in minutes the delivery worker looks ahead to pick up message alarms. Must not be smaller than {@link #PERIOD}.
     */
    public static final Property LOOK_AHEAD = DefaultProperty.valueOf(PREFIX + "lookAhead", I(35));

    /**
     * Defines the time in minutes that is waited until an alarm that is already in process is picked up. E.g. because the node who originally was going to process the trigger has died.
     */
    public static final Property OVERDUE = DefaultProperty.valueOf(PREFIX + "overdueWaitTime", I(5));

    /**
     * Enables or disables the message alarm delivery worker.
     */
    public static final Property ENABLED = DefaultProperty.valueOf(PREFIX + "enabled", Boolean.TRUE);

}
