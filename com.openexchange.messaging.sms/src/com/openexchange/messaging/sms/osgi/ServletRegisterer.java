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

package com.openexchange.messaging.sms.osgi;

import javax.servlet.ServletException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;

/**
 *
 * @author Benjamin Otterbach
 *
 */
public class ServletRegisterer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletRegisterer.class);

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    // friend to be able to test
    final static String SERVLET_PATH_APPENDIX = "messaging/sms";

    private volatile String alias;

    public ServletRegisterer () {
        super();
    }

    public void registerServlet() {
        final HttpService http_service;
        try {
            http_service = MessagingSMSServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (final OXException e) {
            LOG.error("Error registering messaging sms servlet!", e);
            return;
        }
        try {
            String alias = PREFIX.get().getPrefix()+SERVLET_PATH_APPENDIX;
            http_service.registerServlet(alias, new com.openexchange.messaging.sms.servlet.MessagingSMSServlet(), null, null);
            this.alias = alias;
        } catch (final ServletException e) {
            LOG.error("Error registering messaging sms servlet!", e);
        } catch (final NamespaceException e) {
            LOG.error("Error registering messaging sms servlet!", e);
        }
    }

    public void unregisterServlet() {
        final HttpService http_service;
        try {
            http_service = MessagingSMSServiceRegistry.getServiceRegistry().getService(HttpService.class, true);
        } catch (final OXException e) {
            LOG.error("Error unregistering messaging sms servlet!", e);
            return;
        }
        String alias = this.alias;
        if (null != alias) {
            http_service.unregister(PREFIX.get().getPrefix()+SERVLET_PATH_APPENDIX);
            this.alias = null;
        }
    }
}
