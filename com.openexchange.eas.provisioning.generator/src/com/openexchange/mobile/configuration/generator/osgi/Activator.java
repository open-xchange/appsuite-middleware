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

package com.openexchange.mobile.configuration.generator.osgi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.HtmlService;
import com.openexchange.mobile.configuration.generator.MobileConfigServlet;
import com.openexchange.mobile.configuration.generator.configuration.ConfigurationException;
import com.openexchange.mobile.configuration.generator.configuration.MobileConfigProperties;
import com.openexchange.mobile.configuration.generator.configuration.Property;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.templating.TemplateService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link Activator}
 */
public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    public static final String ALIAS = "/servlet/mobileconfig";

    private boolean registered;

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, TemplateService.class, ThreadPoolService.class, HttpService.class, HtmlService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
        register();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
        unregister();
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        // Check configuration
        checkConfiguration();
        // Test encoding:
        try {
            URLEncoder.encode("test", "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Stopping mobileconfig bundle because UTF-8 charset encoding is not available: ", e);
            throw e;
        }
        register();
    }

    private void checkConfiguration() throws ConfigurationException {
        MobileConfigProperties.check(Property.values(), "Mobileconfig");
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    /**
     * Registers the servlet.
     */
    private synchronized void register() {
        if (registered) {
            return;
        }

        HttpService service = getService(HttpService.class);
        if (null == service) {
            return;
        }

        try {
            service.registerServlet(ALIAS, new MobileConfigServlet(), null, null);
            registered = true;
        } catch (ServletException e) {
            LOG.error("", e);
        } catch (NamespaceException e) {
            LOG.error("", e);
        }
        LOG.info("MobileConfig servlet registered");
    }

    /**
     * Unregisters the servlet.
     */
    private synchronized void unregister() {
        if (!registered) {
            return;
        }

        HttpService service = getService(HttpService.class);
        if (null != service) {
            boolean registered = this.registered;
            if (registered) {
                service.unregister(ALIAS);
                this.registered = false;
            }
        }
    }

}
