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

package com.openexchange.recaptcha.osgi;

import java.util.Properties;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.recaptcha.ReCaptchaService;
import com.openexchange.recaptcha.ReCaptchaServlet;
import com.openexchange.recaptcha.impl.ReCaptchaServiceImpl;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private static final String ALIAS_APPENDIX = "recaptcha";

    private volatile ReCaptchaServlet servlet;
    private volatile String alias;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        registerServlet();
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        unregisterServlet();
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceRegistry registry = ReCaptchaServiceRegistry.getInstance();
        registry.clearRegistry();
        final Class<?>[] classes = getNeededServices();
        for (int i = 0; i < classes.length; i++) {
            final Object service = getService(classes[i]);
            if (service != null) {
                registry.addService(classes[i], service);
            }
        }

        final ConfigurationService config = registry.getService(ConfigurationService.class);
        final Properties props = config.getFile("recaptcha.properties");
        final Properties options = config.getFile("recaptcha_options.properties");
        final ReCaptchaServiceImpl reCaptchaService = new ReCaptchaServiceImpl(props, options);
        registerService(ReCaptchaService.class, reCaptchaService, null);
        registry.addService(ReCaptchaService.class, reCaptchaService);

        registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        cleanUp();
        unregisterServlet();
        ReCaptchaServiceRegistry.getInstance().clearRegistry();
    }

    private synchronized void registerServlet() {
        ServiceRegistry registry = ReCaptchaServiceRegistry.getInstance();
        HttpService httpService = registry.getService(HttpService.class);
        if (servlet == null) {
            try {
                String alias = getService(DispatcherPrefixService.class).getPrefix() + ALIAS_APPENDIX;
                ReCaptchaServlet servlet = new ReCaptchaServlet();
                httpService.registerServlet(alias, servlet, null, null);
                this.alias = alias;
                this.servlet = servlet;
                LOG.info("reCAPTCHA Servlet registered.");
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
    }

    private synchronized void unregisterServlet() {
        HttpService httpService = getService(HttpService.class);
        if (httpService != null) {
            String alias = this.alias;
            ReCaptchaServlet servlet = this.servlet;
            if (servlet != null && null != alias) {
                httpService.unregister(alias);
                this.servlet = null;
                this.alias = null;
                LOG.info("reCAPTCHA Servlet unregistered.");
            }
        }
    }

}
