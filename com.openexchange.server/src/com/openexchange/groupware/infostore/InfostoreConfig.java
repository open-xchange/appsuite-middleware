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

package com.openexchange.groupware.infostore;

import java.io.File;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.conf.AbstractConfig;


/**
 * DEPENDS ON: SystemConfig
 */

public class InfostoreConfig extends AbstractConfig implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreConfig.class);

    /**
     * InfoStore properties.
     */
    public static enum InfoProperty{
		MAX_UPLOAD_SIZE;
	}

    private static volatile InfostoreConfig singleton;

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static InfostoreConfig getInstance() {
        InfostoreConfig ret = singleton;
        if (null == ret) {
            synchronized (InfostoreConfig.class) {
                ret = singleton;
                if (null == ret) {
                    ret = new InfostoreConfig();
                    singleton = ret;
                }
            }
        }
        return ret;
    }

    // ------------------------------------------------------------------- //

    private volatile boolean loaded = false;

    /**
     * Initializes a new {@link InfostoreConfig}.
     */
    private InfostoreConfig() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws OXException {
        final File file = ServerServiceRegistry.getInstance().getService(ConfigurationService.class).getFileByName("infostore.properties");
        final String filename = null == file ? null : file.getPath();
        if (null == filename) {
            throw new RuntimeException("Properties file 'infostore.properties' is not available.");
        }
        return filename;
    }

    /**
     * Gets the value associated with specified property key.
     *
     * @param key The property name
     * @return The associated value or <code>null</code>
     */
    public static String getProperty(String key) {
        InfostoreConfig singleton = InfostoreConfig.singleton;
        if (singleton == null || !singleton.loaded) {
            try {
                singleton = getInstance();
                singleton.start();
            } catch (final OXException e) {
                LOG.error("Can't init config:", e);
            }
        }

        return singleton.getPropertyInternal(key);
    }

    /**
     * Gets the max. upload size for InfoStore module.
     *
     * @return The max. upload size or <code>-1</code> if unlimited
     */
    public static long getMaxUploadSize() {
        final String sizeS = getProperty(InfoProperty.MAX_UPLOAD_SIZE.name());
        if (Strings.isEmpty(sizeS)) {
            return sysconfMaxUpload();
        }
        long size;
        try {
            size = Long.parseLong(sizeS.trim());
        } catch (final NumberFormatException e) {
            LOG.warn("{} is not a number: {}. Fall-back to system upload limitation.", InfoProperty.MAX_UPLOAD_SIZE.name(), sizeS);
            size = -1;
        }
        if (size < 0) {
            return sysconfMaxUpload();
        }
        return size;
    }

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (Strings.isEmpty(sizeS)) {
            return 0;
        }
        try {
            return Long.parseLong(sizeS.trim());
        } catch (final NumberFormatException e) {
            LOG.warn("{} is not a number: {}. Fall-back to no upload limitation for InfoStore module.", com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE.name(), sizeS);
            return 0;
        }
    }

    @Override
    public synchronized void start() throws OXException {
        if (!loaded || singleton == null) {
			getInstance().loadPropertiesInternal();
            loaded = true;
		}
    }

    @Override
    public synchronized void stop() throws OXException {
        singleton = null;
        loaded = false;
    }

}
