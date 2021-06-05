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

package com.openexchange.dav.carddav;

/**
 * {@link UserAgents} - Contains user-agent definitions.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class UserAgents {

    public static final String MACOS_10_6_7 = "Address%20Book/883 CFNetwork/454.11.12 Darwin/10.7.0 (i386) (MacBookPro4%2C1)";

    public static final String MACOS_10_6_8 = "Address%20Book/883 CFNetwork/454.12.4 Darwin/10.8.0 (i386)";

    public static final String MACOS_10_6_8_DE = "Adressbuch/883 CFNetwork/454.12.4 Darwin/10.8.0 (i386)";

    public static final String MACOS_10_6_8_NL = "Adresboek/883 CFNetwork/454.12.4 Darwin/10.8.0 (i386)";

    public static final String MACOS_10_7_2 = "AddressBook/6.1 (1062) CardDAVPlugin/196 CFNetwork/520.2.5 Mac_OS_X/10.7.2 (11C74)";

    public static final String MACOS_10_7_3 = "AddressBook/6.1 (1083) CardDAVPlugin/200 CFNetwork/520.3.2 Mac_OS_X/10.7.3 (11D50d)";

    public static final String MACOS_10_8_1 = "Mac OS X/10.8.1 (12B19) AddressBook/1143";

    public static final String MACOS_10_8_2 = "Mac OS X/10.8.2 (12C31a) AddressBook/1164";

    public static final String MACOS_10_15 = "Mac OS X/10.15 (19A603) AddressBookCore/1";

    public static final String MACOS_11_1 = "macOS/11.1 (20C69) AddressBookCore/2452.2";

    public static final String IOS_5_0_1 = "iOS/5.0.1 (9A405) dataaccessd/1.0";

    public static final String IOS_5_0_1_PREF = "iOS/5.0.1 (9A405) Preferences/1.0";

    public static final String IOS_5_1_1 = "iOS/5.1.1 (9B206) dataaccessd/1.0";

    public static final String IOS_5_1_1_PREF = "iOS/5.1.1 (9B206) Preferences/1.0";

    public static final String IOS_6_1_4 = "iOS/6.1.4 (10B350) dataaccessd/1.0";

    public static final String IOS_7_0_0 = "iOS/7.0 (11A465) dataaccessd/1.0";

    public static final String IOS_8_4_0 = "iOS/8.4 (12H143) dataaccessd/1.0";

    public static final String ANDROID_CARDAV_SYNC = "CardDAV-Sync (Android) (like iOS/5.0.1 (9A405) dataaccessd/1.0)";

    public static final String EM_CLIENT_FOR_APP_SUITE = "eM Client for OX App Suite/6.0.28376.0";

    public static final String CALDAV_SYNCHRONIZER = "CalDavSynchronizer/3.6";

    public static final String[] MACOS_ALL = { MACOS_10_6_7, MACOS_10_6_8, MACOS_10_6_8_DE, MACOS_10_6_8_NL, MACOS_10_7_2, MACOS_10_7_3, MACOS_10_8_1, MACOS_10_8_2
    };

    public static final String[] IOS_ALL_PREF = { IOS_5_0_1_PREF, IOS_5_1_1
    };

    public static final String[] IOS_ALL = { IOS_5_0_1, IOS_5_1_1, IOS_6_1_4, IOS_7_0_0, IOS_8_4_0
    };

    public static final String[] OTHER_ALL = { ANDROID_CARDAV_SYNC, EM_CLIENT_FOR_APP_SUITE
    };

    private UserAgents() {
        // prevent instantiation
    }
}
