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

package com.openexchange.groupware.attach;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * DEPENDS ON: SystemConfig
 */
public class AttachmentConfig extends AbstractConfig implements Initialization {

	public static enum AttachmentProperty{
		MAX_UPLOAD_SIZE;
	}

    private static final ReentrantLock INIT_LOCK = new ReentrantLock();

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentConfig.class);

    private static volatile AttachmentConfig _singleton;

    private static boolean loaded = false;

    private AttachmentConfig(){}

    public static AttachmentConfig getInstance(){
        AttachmentConfig ret = _singleton;
        if (ret == null) {
            synchronized (AttachmentConfig.class) {
                ret = _singleton;
                if (ret == null) {
                    ret = new AttachmentConfig();
                    _singleton = ret;
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertyFileName() throws OXException {
        final File file = ServerServiceRegistry.getInstance().getService(ConfigurationService.class).getFileByName("attachment.properties");
        final String filename = null == file ? null : file.getPath();
        if (null == filename) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("attachment.properties");
        }
        return filename;
    }

    public static String getProperty(final String key) {
    	AttachmentConfig singleton = _singleton;
        if (!loaded || singleton == null) {
			try {
			    singleton = getInstance();
			    singleton.start();
			} catch (OXException e) {
				LOG.error("Can't init config",e);
			}
		}
        return singleton.getPropertyInternal(key);
    }

    public static long getMaxUploadSize() {
		final String sizeS = getProperty(AttachmentProperty.MAX_UPLOAD_SIZE.name());
        if (Strings.isEmpty(sizeS)) {
			return sysconfMaxUpload();
		}
        long size;
        try {
            size = Long.parseLong(sizeS.trim());
        } catch (NumberFormatException e) {
            LOG.warn("{} is not a number: {}. Fall-back to system upload limitation.", AttachmentProperty.MAX_UPLOAD_SIZE.name(), sizeS);
            size = -1;
        }
        if (size < 0) {
			return sysconfMaxUpload();
		}
		return size;
	}

	private static long sysconfMaxUpload() {
		final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
		if (null == sizeS) {
			return 0;
		}
		return Long.parseLong(sizeS);
	}

    @Override
    public void start() throws OXException {
        if (!loaded || _singleton == null) {
            INIT_LOCK.lock();
            try {
                getInstance().loadPropertiesInternal();
                loaded = true;
            } finally {
                INIT_LOCK.unlock();
            }
        }
    }

    @Override
    public void stop() throws ConfigurationException {
        _singleton = null;
        loaded = false;
    }
}

