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

package com.openexchange.switchboard.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ZoomExceptionMessages}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class ZoomExceptionMessages implements LocalizableStrings {

    public static final String NO_ALL_DAY_APPOINTMENTS_MSG = "Zoom meetings cannot be used in all-day appointments. Please use a fixed start- and end-time and try again.";

    public static final String NO_FLOATING_APPOINTMENTS_MSG = "Zoom meetings cannot be used in appointments with floating start- or end-times. Please use a fixed start- and end-time and try again.";

    public static final String NO_SERIES_LONGER_THAN_A_YEAR_MSG = "Zoom meetings cannot be used in recurring appointment series spanning over more than one year. Please shorten the recurrence rule and try again.";

    public static final String NO_SWITCH_TO_OR_FROM_SERIES_MSG = "The same Zoom meeting can't be re-used when switching from recurring to single appointments and vice versa. Please generate a new Zoom meeting and try again.";

}
