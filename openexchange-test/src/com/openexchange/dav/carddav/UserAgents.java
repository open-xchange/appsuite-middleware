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

	public static final String IOS_5_0_1 = "iOS/5.0.1 (9A405) dataaccessd/1.0";

	public static final String IOS_5_0_1_PREF = "iOS/5.0.1 (9A405) Preferences/1.0";

	public static final String IOS_5_1_1 = "iOS/5.1.1 (9B206) dataaccessd/1.0";

    public static final String IOS_5_1_1_PREF = "iOS/5.1.1 (9B206) Preferences/1.0";

    public static final String IOS_6_1_4 = "iOS/6.1.4 (10B350) dataaccessd/1.0";

    public static final String IOS_7_0_0 = "iOS/7.0 (11A465) dataaccessd/1.0";

    public static final String IOS_8_4_0 = "iOS/8.4 (12H143) dataaccessd/1.0";

	public static final String ANDROID_CARDAV_SYNC = "CardDAV-Sync (Android) (like iOS/5.0.1 (9A405) dataaccessd/1.0)";


	public static final String[] MACOS_ALL = {
		MACOS_10_6_7, MACOS_10_6_8, MACOS_10_6_8_DE, MACOS_10_6_8_NL, MACOS_10_7_2, MACOS_10_7_3, MACOS_10_8_1, MACOS_10_8_2
	};

    public static final String[] IOS_ALL_PREF = {
        IOS_5_0_1_PREF, IOS_5_1_1
    };

    public static final String[] IOS_ALL = {
        IOS_5_0_1, IOS_5_1_1, IOS_6_1_4, IOS_7_0_0, IOS_8_4_0
    };

	public static final String[] OTHER_ALL = {
		ANDROID_CARDAV_SYNC
	};

	private UserAgents() {
		// prevent instantiation
	}
}
