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

package com.openexchange.http.deferrer.osgi;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.CustomRedirectURLDetermination;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.http.deferrer.impl.DefaultDeferringURLService;
import com.openexchange.http.deferrer.servlet.DeferrerServlet;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link HTTPDeferrerActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HTTPDeferrerActivator extends HousekeepingActivator {

    private volatile String alias;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigViewFactory.class, ConfigurationService.class, HttpService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(HTTPDeferrerActivator.class);
        logger.info("Starting bundle com.openexchange.http.deferrer");

        final DispatcherPrefixService prefixService = getService(DispatcherPrefixService.class);
        DefaultDeferringURLService.PREFIX.set(prefixService);
        String alias = prefixService.getPrefix() + "defer";
        getService(HttpService.class).registerServlet(alias, new DeferrerServlet(), null, null);
        this.alias = alias;

        registerService(DeferringURLService.class, new DefaultDeferringURLService() {

            @Override
            protected String getDeferrerURL(final int userId, final int contextId) {
                if (userId <= 0 || contextId <= 0) {
                    return getService(ConfigurationService.class).getProperty("com.openexchange.http.deferrer.url");
                }
                // Valid user/context identifiers
                try {
                    final ConfigView view = getService(ConfigViewFactory.class).getView(userId, contextId);
                    return view.get("com.openexchange.http.deferrer.url", String.class);
                } catch (final Exception e) {
                    final String url = getService(ConfigurationService.class).getProperty("com.openexchange.http.deferrer.url");
                    logger.error("Failed to retrieve deferrer URL via config-cascade look-up. Using global one instead: {}", null == url ? "null" : url, e);
                    return url;
                }
            }

        });

        track(CustomRedirectURLDetermination.class, new SimpleRegistryListener<CustomRedirectURLDetermination>() {

			@Override
			public void added(
					ServiceReference<CustomRedirectURLDetermination> ref,
					CustomRedirectURLDetermination service) {
				DeferrerServlet.CUSTOM_HANDLERS.add(service);
			}

			@Override
			public void removed(
					ServiceReference<CustomRedirectURLDetermination> ref,
					CustomRedirectURLDetermination service) {
				DeferrerServlet.CUSTOM_HANDLERS.remove(service);
			}
		});

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        HttpService service = getService(HttpService.class);
        if (null != service) {
            String alias = this.alias;
            if (null != alias) {
                service.unregister(alias);
                this.alias = null;
            }
        }
        super.stopBundle();
    }

}
