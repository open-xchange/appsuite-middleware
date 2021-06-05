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
            } catch (OXException e) {
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
        } catch (NumberFormatException e) {
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
        } catch (NumberFormatException e) {
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
