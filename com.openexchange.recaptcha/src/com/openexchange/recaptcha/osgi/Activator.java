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

package com.openexchange.recaptcha.osgi;

import java.util.Properties;
import org.osgi.service.http.HttpService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.recaptcha.ReCaptchaService;
import com.openexchange.recaptcha.ReCaptchaServlet;
import com.openexchange.recaptcha.impl.ReCaptchaServiceImpl;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private static final String ALIAS_APPENDIX = "recaptcha";

    private ReCaptchaServlet servlet;
    private String alias;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, HttpService.class, DispatcherPrefixService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        ConfigurationService config = getService(ConfigurationService.class);

        Properties props = config.getFile("recaptcha.properties");
        Properties options = config.getFile("recaptcha_options.properties");
        registerService(ReCaptchaService.class, new ReCaptchaServiceImpl(props, options), null);

        registerServlet();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        unregisterServlet();
        super.stopBundle();
    }

    private void registerServlet() {
        if (servlet == null) {
            HttpService httpService = getService(HttpService.class);
            try {
                String alias = getService(DispatcherPrefixService.class).getPrefix() + ALIAS_APPENDIX;
                ReCaptchaServlet servlet = new ReCaptchaServlet(this);
                httpService.registerServlet(alias, servlet, null, null);
                this.alias = alias;
                this.servlet = servlet;
                LOG.info("reCAPTCHA Servlet registered.");
            } catch (Exception e) {
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
                this.servlet = null;
                this.alias = null;
                HttpServices.unregister(alias, httpService);
                LOG.info("reCAPTCHA Servlet unregistered.");
            }
        }
    }

}
