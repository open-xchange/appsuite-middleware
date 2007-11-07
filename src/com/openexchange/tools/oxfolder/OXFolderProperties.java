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

package com.openexchange.tools.oxfolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.cache.FolderCacheManager;
import com.openexchange.cache.FolderQueryCacheManager;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.OCLPermission;

/**
 * <tt>OXFolderProperties</tt> contains both folder properties and folder
 * cache properties
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderProperties implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXFolderProperties.class);

	private static OXFolderProperties instance = new OXFolderProperties();

	private static final String PROPFILE = "FOLDERCACHEPROPERTIES";

	/**
	 * @return The singleton instance of {@link OXFolderProperties}
	 */
	public static OXFolderProperties getInstance() {
		return instance;
	}

	/*
	 * Fields
	 */
	private final AtomicBoolean started = new AtomicBoolean();

	private Properties folderProperties;

	private boolean enableDBGrouping = true;

	private boolean enableFolderCache = true;

	private boolean ignoreSharedAddressbook;

	private boolean enableInternalUsersEdit;

	private OXFolderProperties() {
		super();
	}

	public void start() throws AbstractOXException {
		if (started.get()) {
			LOG.error("Folder properties have already been started", new Throwable());
			return;
		}
		init();
		started.set(true);
	}

	public void stop() throws AbstractOXException {
		if (!started.get()) {
			LOG.error("Folder properties cannot be stopped since they have not been started before", new Throwable());
			return;
		}
		reset();
		FolderCacheManager.releaseInstance();
		FolderQueryCacheManager.releaseInstance();
		started.set(false);
	}

	private void reset() {
		folderProperties = null;
		enableDBGrouping = true;
		enableFolderCache = true;
		ignoreSharedAddressbook = false;
		enableInternalUsersEdit = false;
	}

	private void init() throws ConfigurationException {
		folderProperties = new Properties();
		final String propFileName = SystemConfig.getProperty(PROPFILE);
		if (null == propFileName) {
			LOG.error("Cannot find property \"" + PROPFILE + "\" in system.properties.");
			return;
		}
		try {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(propFileName);
				folderProperties.load(fis);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (final IOException e) {
						LOG.error(e.getLocalizedMessage(), e);
					}
					fis = null;
				}
			}
			final String trueStr = "true";
			/*
			 * ENABLE_DB_GROUPING
			 */
			enableDBGrouping = trueStr.equalsIgnoreCase(folderProperties.getProperty("ENABLE_DB_GROUPING", "true"));
			/*
			 * ENABLE_FOLDER_CACHE
			 */
			enableFolderCache = trueStr.equalsIgnoreCase(folderProperties.getProperty("ENABLE_FOLDER_CACHE", "true"));
			/*
			 * IGNORE_SHARED_ADDRESSBOOK
			 */
			ignoreSharedAddressbook = trueStr.equalsIgnoreCase(folderProperties.getProperty(
					"IGNORE_SHARED_ADDRESSBOOK", "false"));
			/*
			 * ENABLE_INTERNAL_USER_EDIT
			 */
			enableInternalUsersEdit = trueStr.equalsIgnoreCase(folderProperties.getProperty(
					"ENABLE_INTERNAL_USER_EDIT", "false"));
			/*
			 * Log info
			 */
			logInfo();
		} catch (final IOException e) {
			throw new ConfigurationException(ConfigurationException.Code.IO_ERROR, e, e.getLocalizedMessage());
		}
	}

	private void logInfo() {
		if (LOG.isInfoEnabled()) {
			final StringBuilder sb = new StringBuilder(512);
			sb.append("\nFolder Properties & Folder Cache Properties:\n");
			sb.append("\tENABLE_DB_GROUPING=").append(enableDBGrouping).append('\n');
			sb.append("\tENABLE_FOLDER_CACHE=").append(enableFolderCache).append('\n');
			sb.append("\tENABLE_INTERNAL_USER_EDIT=").append(enableInternalUsersEdit).append('\n');
			sb.append("\tIGNORE_SHARED_ADDRESSBOOK=").append(ignoreSharedAddressbook);
			LOG.info(sb.toString());
		}
	}

	private static final String WARN_FOLDER_PROPERTIES_INIT = "Folder properties have not been started.";

	/**
	 * @return <code>true</code> if database grouping is enabled (<code>GROUB BY</code>);
	 *         otherwise <code>false</code>
	 */
	public static boolean isEnableDBGrouping() {
		if (!instance.started.get()) {
			LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
		}
		return instance.enableDBGrouping;
	}

	/**
	 * @return <code>true</code> if folder cache is enabled; otherwise
	 *         <code>false</code>
	 */
	public static boolean isEnableFolderCache() {
		if (!instance.started.get()) {
			LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
		}
		return instance.enableFolderCache;
	}

	/**
	 * @return <code>true</code> if shared address book should be omitted in
	 *         folder tree display; otherwise <code>false</code>
	 */
	public static boolean isIgnoreSharedAddressbook() {
		if (!instance.started.get()) {
			LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
		}
		return instance.ignoreSharedAddressbook;
	}

	/**
	 * Context's system folder "<code>Global address book</code>" is created
	 * with write permission set to {@link OCLPermission#WRITE_OWN_OBJECTS} if
	 * this property is set to <code>true</code>
	 * 
	 * @return <code>true</code> if contacts located in global address book
	 *         may be edited; otherwise <code>false</code>
	 */
	public static boolean isEnableInternalUsersEdit() {
		if (!instance.started.get()) {
			LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
		}
		return instance.enableInternalUsersEdit;
	}

}
