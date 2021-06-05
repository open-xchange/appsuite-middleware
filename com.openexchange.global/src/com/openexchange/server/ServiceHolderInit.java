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

package com.openexchange.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link ServiceHolderInit} - Initialization for service holder
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServiceHolderInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServiceHolderInit.class);

    private static final String DEFAULT_TIMEOUT = "10000";

    private static final ServiceHolderInit SINGLETON = new ServiceHolderInit();

    /**
     * Gets the singleton instance of {@link ServiceHolderInit}
     *
     * @return The singleton instance of {@link ServiceHolderInit}
     */
    public static ServiceHolderInit getInstance() {
        return SINGLETON;
    }

    private final AtomicBoolean started;

    /**
     * Initializes a new {@link ServiceHolderInit}
     */
    private ServiceHolderInit() {
        super();
        started = new AtomicBoolean();
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("Service holder initialization already started", new Throwable());
            return;
        }
        final InputStream in;
        {
            final String propDir = System.getProperties().getProperty("openexchange.propdir");
            if (null == propDir) {
                LOG.error("Missing property \"openexchange.propdir\"");
                throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create();
            }
            final File sysPropFile = new File(propDir, "system.properties");
            if (!sysPropFile.exists() || !sysPropFile.isFile()) {
                LOG.error("Missing property file \"system.properties\" in properties path \"{}\"", propDir);
                throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create();
            }
            try {
                in = new FileInputStream(sysPropFile);
            } catch (FileNotFoundException e) {
                /*
                 * Cannot occur due to the above check
                 */
                LOG.error("", e);
                throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create();
            }
        }
        try {
            final Properties sysProps = new Properties();
            try {
                sysProps.load(in);
                final boolean serviceUsageInscpection = Boolean.parseBoolean(sysProps.getProperty("serviceUsageInspection", "false").trim());
                if (serviceUsageInscpection) {
                    final String serviceUsageTimeoutStr = sysProps.getProperty("serviceUsageTimeout", DEFAULT_TIMEOUT).trim();
                    int serviceUsageTimeout = -1;
                    try {
                        serviceUsageTimeout = Integer.parseInt(serviceUsageTimeoutStr);
                    } catch (NumberFormatException e) {
                        LOG.error("Invalid property value for \"serviceUsageTimeout\": {}", serviceUsageTimeoutStr);
                        serviceUsageTimeout = Integer.parseInt(DEFAULT_TIMEOUT);
                    }
                    ServiceHolder.enableServiceUsageInspection(serviceUsageTimeout);
                    LOG.info("Service usage inspection successfully enabled");
                } else {
                    LOG.info("Service usage inspection not enabled");
                }
            } catch (IOException e) {
                throw ServiceExceptionCode.IO_ERROR.create();
            }
        } finally {
            Streams.close(in);
        }
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error("Service holder initialization has not been started", new Throwable());
            return;
        }
        ServiceHolder.disableServiceUsageInspection();
    }

}
