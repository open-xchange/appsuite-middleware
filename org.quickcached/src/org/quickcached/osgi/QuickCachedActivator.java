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

package org.quickcached.osgi;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quickserver.net.server.QuickServer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.version.Version;


/**
 * {@link QuickCachedActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuickCachedActivator extends HousekeepingActivator {

    private volatile QuickServer quickcached;
    private volatile String version;

    /**
     * Initializes a new {@link QuickCachedActivator}.
     */
    public QuickCachedActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Log LOG = LogFactory.getLog(QuickCachedActivator.class);
        LOG.info("Starting bundle: org.quickcached...");
        try {
            // Read configuration
            final ConfigurationService service = getService(ConfigurationService.class);
            final String confFile = service.getFileByName("QuickCached.xml").getCanonicalPath();
            final Object config[] = new Object[] { confFile };
            // Initialize instance
            final QuickServer quickcached = new QuickServer();
            quickcached.initService(config);
            // Memcached version
            final Map<?, ?> configMap = quickcached.getConfig().getApplicationConfiguration();
            String version = (String) configMap.get("MEMCACHED_VERSION_TO_SHOW");
            if (null == version) {
                version = "1.4.6";
            }
            this.version = version;
            // Server configuration
            quickcached.setBindAddr(service.getProperty("org.quickcached.bindAddress", "0.0.0.0"));
            quickcached.setPort(service.getIntProperty("org.quickcached.port", 4455));
            quickcached.setMaxConnection(service.getIntProperty("org.quickcached.maxConnection", 100));
            quickcached.setName("Open-Xchange QuickCached Server " + Version.SINGLETON.getVersionString());
            quickcached.startServer();
            this.quickcached = quickcached;
            LOG.info("Started bundle: org.quickcached");
        } catch (final Exception e) {
            LOG.error("Error starting bundle: org.quickcached", e);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log LOG = LogFactory.getLog(QuickCachedActivator.class);
        LOG.info("Stopping bundle: org.quickcached...");
        try {
            final QuickServer quickcached = this.quickcached;
            if (null != quickcached) {
                quickcached.stopServer();
                this.quickcached = null;
            }
            this.version = null;
            LOG.info("Stopped bundle: org.quickcached");
        } catch (final Exception e) {
            LOG.error("Error stopping bundle: org.quickcached", e);
        }
    }

}
