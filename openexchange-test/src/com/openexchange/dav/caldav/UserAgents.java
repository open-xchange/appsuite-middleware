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

package com.openexchange.dav.caldav;

/**
 * {@link UserAgents} - Contains user-agent definitions.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class UserAgents {

    public static final String MACOS_10_7_3 = "CalendarStore/5.0.2 (1166); iCal/5.0.2 (1571); Mac OS X/10.7.3 (11D50d)";

    public static final String LIGHTNING_1_7 = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20120907 Thunderbird/15.0.1 Lightning/1.7";

    public static final String LIGHTNING_4_0_3_1 = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:38.0) Gecko/20100101 Thunderbird/38.3.0 Lightning/4.0.3.1";

    public static final String LIGHTNING_4_7_7 = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:45.0) Gecko/20100101 Thunderbird/45.7.0 Lightning/4.7.7";

    public static final String IOS_9_1 = "iOS/9.1 (13B143) dataaccessd/1.0";

    public static final String IOS_12_0 = "iOS/12.0 (16A366) dataaccessd/1.0";

    public static final String EM_CLIENT_6_0 = "eM Client/6.0.24144.0";

    public static final String EM_CLIENT_8_1 = "eMClient/8.1.172.0";

    public static final String[] MACOS_ALL = { MACOS_10_7_3
    };

    private UserAgents() {
        // prevent instantiation
    }
}
