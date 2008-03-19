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

package com.openexchange.folder.rdb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link RdbFolderProperties} - Properties for both folder storage and cache
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbFolderProperties implements Initialization {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbFolderProperties.class);

	private static final String PROP_RDB_FLD_PROP_FILE = "com.openexchange.folder.rdb.propfile";

	private static final RdbFolderProperties instance = new RdbFolderProperties();

	/**
	 * Gets the singleton instance of {@link RdbFolderProperties}
	 * 
	 * @return The singleton instance of {@link RdbFolderProperties}
	 */
	public static RdbFolderProperties getInstance() {
		return instance;
	}

	private final AtomicBoolean started;

	private boolean enableDBGrouping = true;

	private boolean enableFolderCache = true;

	private boolean ignoreSharedAddressbook;

	private boolean enableInternalUsersEdit;

	/**
	 * Initializes a new {@link RdbFolderProperties}
	 */
	private RdbFolderProperties() {
		super();
		started = new AtomicBoolean();
	}

	public void start() throws AbstractOXException {
		if (!started.compareAndSet(false, true)) {
			LOG.error("RdbFolderProperties already started", new Throwable());
		}
		init();
	}

	public void stop() throws AbstractOXException {
		if (!started.compareAndSet(true, false)) {
			LOG.error("RdbFolderProperties has not been started before", new Throwable());
		}
		reset();
	}

	private void init() throws RdbFolderException {
		/*
		 * Check RDB folder properties file defined through property
		 */
		final String propFile = ServerServiceRegistry.getInstance().getService(ConfigurationService.class).getProperty(
				PROP_RDB_FLD_PROP_FILE);
		if (propFile == null) {
			throw new RdbFolderException(RdbFolderException.Code.MISSING_PROPERTY, PROP_RDB_FLD_PROP_FILE);
		}
		final Properties folderProperties = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(propFile.trim());
			folderProperties.load(in);
		} catch (final IOException e) {
			throw new RdbFolderException(RdbFolderException.Code.IO_ERROR, e, e.getLocalizedMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
				in = null;
			}
		}
		final String trueStr = "true";
		/*
		 * ENABLE_DB_GROUPING
		 */
		enableDBGrouping = trueStr.equalsIgnoreCase(folderProperties.getProperty("ENABLE_DB_GROUPING", trueStr).trim());
		/*
		 * ENABLE_FOLDER_CACHE
		 */
		enableFolderCache = trueStr.equalsIgnoreCase(folderProperties.getProperty("ENABLE_FOLDER_CACHE", trueStr)
				.trim());
		/*
		 * IGNORE_SHARED_ADDRESSBOOK
		 */
		ignoreSharedAddressbook = trueStr.equalsIgnoreCase(folderProperties.getProperty("IGNORE_SHARED_ADDRESSBOOK",
				"false").trim());
		/*
		 * ENABLE_INTERNAL_USER_EDIT
		 */
		enableInternalUsersEdit = trueStr.equalsIgnoreCase(folderProperties.getProperty("ENABLE_INTERNAL_USER_EDIT",
				"false").trim());
		/*
		 * Log info
		 */
		if (LOG.isInfoEnabled()) {
			logInfo();
		}
	}

	private void reset() {
		enableDBGrouping = true;
		enableFolderCache = true;
		ignoreSharedAddressbook = false;
		enableInternalUsersEdit = false;
	}

	private void logInfo() {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("\nRDB Folder Properties & RDB Folder Cache Properties:\n");
		sb.append("\tENABLE_DB_GROUPING=").append(enableDBGrouping).append('\n');
		sb.append("\tENABLE_FOLDER_CACHE=").append(enableFolderCache).append('\n');
		sb.append("\tENABLE_INTERNAL_USER_EDIT=").append(enableInternalUsersEdit).append('\n');
		sb.append("\tIGNORE_SHARED_ADDRESSBOOK=").append(ignoreSharedAddressbook);
		LOG.info(sb.toString());
	}

	/**
	 * Gets the enableDBGrouping
	 * 
	 * @return the enableDBGrouping
	 */
	public boolean isEnableDBGrouping() {
		return enableDBGrouping;
	}

	/**
	 * Gets the enableFolderCache
	 * 
	 * @return the enableFolderCache
	 */
	public boolean isEnableFolderCache() {
		return enableFolderCache;
	}

	/**
	 * Gets the ignoreSharedAddressbook
	 * 
	 * @return the ignoreSharedAddressbook
	 */
	public boolean isIgnoreSharedAddressbook() {
		return ignoreSharedAddressbook;
	}

	/**
	 * Gets the enableInternalUsersEdit
	 * 
	 * @return the enableInternalUsersEdit
	 */
	public boolean isEnableInternalUsersEdit() {
		return enableInternalUsersEdit;
	}

}
