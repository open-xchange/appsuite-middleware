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
        if(!loaded || singleton == null) {
			try {
			    singleton = getInstance();
			    singleton.start();
			} catch (final OXException e) {
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
        } catch (final NumberFormatException e) {
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
		if(null == sizeS) {
			return 0;
		}
		return Long.parseLong(sizeS);
	}

    @Override
    public void start() throws OXException {
        if(!loaded || _singleton == null) {
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

