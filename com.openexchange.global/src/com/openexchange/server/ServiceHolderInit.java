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
                LOG.error("Missing property file \"system.properties\" in properties path \"{}{}", propDir, '"');
                throw ServiceExceptionCode.SERVICE_INITIALIZATION_FAILED.create();
            }
            try {
                in = new FileInputStream(sysPropFile);
            } catch (final FileNotFoundException e) {
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
                    } catch (final NumberFormatException e) {
                        LOG.error("Invalid property value for \"serviceUsageTimeout\": {}", serviceUsageTimeoutStr);
                        serviceUsageTimeout = Integer.parseInt(DEFAULT_TIMEOUT);
                    }
                    ServiceHolder.enableServiceUsageInspection(serviceUsageTimeout);
                    LOG.info("Service usage inspection successfully enabled");
                } else {
                    LOG.info("Service usage inspection not enabled");
                }
            } catch (final IOException e) {
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
