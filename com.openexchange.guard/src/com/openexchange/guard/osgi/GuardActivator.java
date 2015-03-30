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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.guard.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.slf4j.Logger;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.config.ConfigurationService;
import com.openexchange.guard.AbstractGuardAccess;
import com.openexchange.guard.GuardApi;
import com.openexchange.guard.interceptor.GuardProvisioningContextPlugin;
import com.openexchange.guard.interceptor.GuardUserServiceInterceptor;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserServiceInterceptor;


/**
 * {@link GuardActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GuardActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link GuardActivator}.
     */
    public GuardActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Logger logger = org.slf4j.LoggerFactory.getLogger(GuardActivator.class);
        try {
            // Initialize Guard API instance
            {
                ConfigurationService service = getService(ConfigurationService.class);
                String propName = "com.openexchange.guard.endpoint";
                String endPoint = service.getProperty(propName);
                if (Strings.isEmpty(endPoint)) {
                    // No end-point
                    logger.info("No Guard end-point defined via property {}", propName);
                    return;
                }
                // Ensure ending slash '/' character
                endPoint = endPoint.trim();
                if (!endPoint.endsWith("/")) {
                    endPoint = new StringBuilder(endPoint).append('/').toString();
                }
                logger.info("Starting bundle {} using end point \"{}\"", context.getBundle().getSymbolicName(), endPoint);
                AbstractGuardAccess.setGuardApi(new GuardApi(endPoint, service));
            }
            Services.setServiceLookup(this);
            // Register plugin interfaces
            {
                final Dictionary<String, String> props = new Hashtable<String, String>(2);
                props.put("name", "OXContext");
                registerService(OXContextPluginInterface.class, new GuardProvisioningContextPlugin());
            }

            // Register interceptor
            registerService(UserServiceInterceptor.class, new GuardUserServiceInterceptor(this));
        } catch (Exception e) {
            logger.error("Failed starting bundle {}", context.getBundle().getSymbolicName(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        GuardApi guardApi = AbstractGuardAccess.unsetGuardApi();
        if (null != guardApi) {
            guardApi.shutDown();
        }
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
