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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.dovecot.doveadm.client.osgi;

import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dovecot.doveadm.client.DoveAdmClient;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmClient;
import com.openexchange.dovecot.doveadm.client.internal.HttpDoveAdmEndpointManager;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.endpointpool.EndpointManagerFactory;

/**
 * {@link DoveAdmClientActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DoveAdmClientActivator extends HousekeepingActivator {

    private volatile HttpDoveAdmClient client;

    /**
     * Initializes a new {@link DoveAdmClientActivator}.
     */
    public DoveAdmClientActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, EndpointManagerFactory.class, ConfigViewFactory.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(DoveAdmClientActivator.class);

        // Check API secret
        String apiSecret = getService(ConfigurationService.class).getProperty("com.openexchange.dovecot-doveadm.apiSecret");
        if (Strings.isEmpty(apiSecret)) {
            logger.error("Missing API secret from property \"com.openexchange.dovecot-doveadm.apiSecret\". DoveAdm client will not be initialized.");
            return;
        }

        // Initialize the end-point manager
        EndpointManagerFactory factory = getService(EndpointManagerFactory.class);
        HttpDoveAdmEndpointManager endpointManager = new HttpDoveAdmEndpointManager();
        boolean anyAvailable = endpointManager.init(factory, getService(ConfigurationService.class));
        if (false == anyAvailable) {
            logger.error("Missing end-points for Dovecot DoveAdm REST interface. Bundle {} will not start. DoveAdm client will not be initialized.", context.getBundle().getSymbolicName());
            return;
        }

        // Initialize client to Dovecot REST interface
        HttpDoveAdmClient client = new HttpDoveAdmClient(apiSecret, endpointManager);
        this.client = client;
        registerService(DoveAdmClient.class, client);

        logger.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());
    }

    @Override
    protected void stopBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(DoveAdmClientActivator.class);

        // Clean-up
        super.stopBundle();

        // Shut-down client
        HttpDoveAdmClient client = this.client;
        if (null != client) {
            this.client = null;
            client.shutDown();
        }

        logger.info("Bundle successfully stopped: {}", context.getBundle().getSymbolicName());
    }

}
