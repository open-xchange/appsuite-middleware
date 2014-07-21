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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.share.json.osgi;

import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareService;
import com.openexchange.share.json.internal.ShareServiceLookup;
import com.openexchange.share.json.internal.ShareServlet;
import com.openexchange.user.UserService;

/**
 * {@link ServletActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ServletActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletActivator.class);

    private volatile boolean registered;

    /**
     * Initializes a new {@link ServletActivator}.
     */
    public ServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ShareService.class, UserService.class, ContextService.class, DispatcherPrefixService.class,
            HttpService.class, SessiondService.class, CryptoService.class, ConfigurationService.class };
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
        registerServlet();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterServlet();
        cleanUp();
    }

    private void registerServlet() {
        LOG.info("starting bundle: \"com.openexchange.share.json\"");
        /*
         * set references
         */
        ShareServiceLookup.set(this);
        /*
         * register servlet
         */
        registered = true;
        try {
            getService(HttpService.class).registerServlet(getService(DispatcherPrefixService.class).getPrefix() + "share", new ShareServlet(), null, null);
        } catch (final ServletException e) {
            LOG.error("", e);
        } catch (final NamespaceException e) {
            LOG.error("", e);
        }
    }

    private void unregisterServlet() {
        getService(HttpService.class).unregister(getService(DispatcherPrefixService.class).getPrefix() + "/share");
        if (false == registered) {
            return;
        }
        registered = false;
        ShareServiceLookup.set(null);
    }

}
