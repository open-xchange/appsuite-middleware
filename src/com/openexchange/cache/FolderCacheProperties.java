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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.cache;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.openexchange.configuration.SystemConfig;

/**
 * <tt>FolderCacheProperties</tt> contains both folder properties and folder
 * cache properties
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderCacheProperties {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FolderCacheProperties.class);

	private static final String PROPFILE = "FOLDERCACHEPROPERTIES";

	private static final Properties FOLDER_CACHE_PROPS;

	private static boolean enableDBGrouping = true;

	private static boolean enableFolderCache = true;

	private static boolean ignoreSharedAddressbook;

	private static boolean enableInternalUsersEdit;

	private FolderCacheProperties() {
		super();
	}

	static {
		FOLDER_CACHE_PROPS = new Properties();
		final String propFileName = SystemConfig.getProperty(PROPFILE);
		if (null == propFileName) {
			LOG.error("Cannot find property \"" + PROPFILE + "\" in system.properties.");
		}
		try {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(propFileName);
				FOLDER_CACHE_PROPS.load(fis);
			} finally {
				if (fis != null) {
					fis.close();
				}
			}
			final String trueStr = "true";
			/*
			 * ENABLE_DB_GROUPING
			 */
			enableDBGrouping = trueStr.equalsIgnoreCase(FOLDER_CACHE_PROPS.getProperty("ENABLE_DB_GROUPING", "true"));
			/*
			 * ENABLE_FOLDER_CACHE
			 */
			enableFolderCache = trueStr.equalsIgnoreCase(FOLDER_CACHE_PROPS.getProperty("ENABLE_FOLDER_CACHE", "true"));
			/*
			 * IGNORE_SHARED_ADDRESSBOOK
			 */
			ignoreSharedAddressbook = trueStr.equalsIgnoreCase(FOLDER_CACHE_PROPS.getProperty(
					"IGNORE_SHARED_ADDRESSBOOK", "false"));
			/*
			 * ENABLE_INTERNAL_USER_EDIT
			 */
			enableInternalUsersEdit = trueStr.equalsIgnoreCase(FOLDER_CACHE_PROPS.getProperty(
					"ENABLE_INTERNAL_USER_EDIT", "false"));
			/*
			 * Log info
			 */
			logInfo();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private static final void logInfo() {
		if (LOG.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder(500);
			sb.append("\nFolder Properties & Folder Cache Properties:\n");
			sb.append("\tENABLE_DB_GROUPING=").append(enableDBGrouping).append('\n');
			sb.append("\tENABLE_FOLDER_CACHE=").append(enableFolderCache).append('\n');
			sb.append("\tENABLE_INTERNAL_USER_EDIT=").append(enableInternalUsersEdit).append('\n');
			sb.append("\tIGNORE_SHARED_ADDRESSBOOK=").append(ignoreSharedAddressbook);
			LOG.info(sb.toString());
		}
	}

	public static final boolean isEnableDBGrouping() {
		return enableDBGrouping;
	}

	public static final boolean isEnableFolderCache() {
		return enableFolderCache;
	}

	public static boolean isIgnoreSharedAddressbook() {
		return ignoreSharedAddressbook;
	}

	public static boolean isEnableInternalUsersEdit() {
		return enableInternalUsersEdit;
	}
}
