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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav;

import com.openexchange.exception.OXException;

/**
 * {@link CardDAVClient}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CardDAVClient extends WebDAVClient {
	
	/**
	 * The user agent as normally used by the Apple Addressbook client in Mac OS X 10.7.2
	 */
	public static final String USER_AGENT_10_7_2 = "AddressBook/6.1 (1062) CardDAVPlugin/196 CFNetwork/520.2.5 Mac_OS_X/10.7.2 (11C74)";
	
	/**
	 * The user agent as normally used by the Apple Addressbook client in a German Mac OS X 10.6.8
	 */
	public static final String USER_AGENT_10_6_8 = "Adressbuch/883 CFNetwork/454.12.4 Darwin/10.8.0 (i386)";

	/**
	 * The user agent as normally used by the Apple Addressbook client in Mac OS X 10.7.3
	 */
	public static final String USER_AGENT_10_7_3 = "AddressBook/6.1 (1083) CardDAVPlugin/200 CFNetwork/520.3.2 Mac_OS_X/10.7.3 (11D50d)";

	/**
	 * The user agent as normally used by the Apple client in iOS 5.1.1
	 */
	public static final String USER_AGENT_IOS_5_1_1 = "iOS/5.1.1 (9B206) dataaccessd/1.0";
	
	/**
	 * The user agent as normally used by the Apple client in iOS 5.1.1 during configuration
	 */
	public static final String USER_AGENT_IOS_5_1_1_Pref = "iOS/5.1.1 (9B206) Preferences/1.0";
	
	/**
	 * The user agent as normally used by the Apple client in iOS 5.0.1
	 */
	public static final String USER_AGENT_IOS_5_0_1 = "iOS/5.0.1 (9A405) dataaccessd/1.0";
	
	// CardDAV-Sync (Android) (like iOS/5.0.1 (9A405) dataaccessd/1.0)
	
	
	public CardDAVClient() throws OXException {
		super();
		super.setUserAgent(USER_AGENT_10_7_2);
	}
	
}
