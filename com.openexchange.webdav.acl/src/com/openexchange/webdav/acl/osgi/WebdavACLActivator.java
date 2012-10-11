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

package com.openexchange.webdav.acl.osgi;

import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;
import com.openexchange.webdav.acl.servlets.WebdavPrincipalPerformer;
import com.openexchange.webdav.acl.servlets.WebdavPrincipalServlet;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;

/**
 * {@link WebdavACLActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WebdavACLActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(WebdavACLActivator.class);

    private volatile OSGiPropertyMixin mixin;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, HttpService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            WebdavPrincipalPerformer.setServices(this);

            getService(HttpService.class).registerServlet("/servlet/dav/principals/users", new WebdavPrincipalServlet(), null, null);

            final WebdavPrincipalPerformer performer = WebdavPrincipalPerformer.getInstance();
            final OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            this.mixin = mixin;
            openTrackers();
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final OSGiPropertyMixin mixin = this.mixin;
        if (null != mixin) {
            mixin.close();
            this.mixin = null;
        }
        super.stopBundle();
    }

}
