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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.dav.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.contact.ContactService;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.principals.PrincipalPerformer;
import com.openexchange.dav.principals.PrincipalServlet;
import com.openexchange.dav.root.RootPerformer;
import com.openexchange.dav.root.RootServlet;
import com.openexchange.group.GroupService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;

/**
 * {@link DAVActivator}
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 * @since v7.8.1
 */
public class DAVActivator extends HousekeepingActivator {

    private volatile OSGiPropertyMixin mixin;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, HttpService.class, ContactService.class, GroupService.class, ResourceService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        HttpService httpService = getService(HttpService.class);
        /*
         * root
         */
        RootPerformer rootPerformer = new RootPerformer(this);
        RootServlet rootServlet = new RootServlet(rootPerformer);
        httpService.registerServlet("/servlet/dav", rootServlet, null, null);
        /*
         * principals
         */
        PrincipalPerformer principalPerformer = new PrincipalPerformer(this);
        PrincipalServlet principalServlet = new PrincipalServlet(principalPerformer);
        httpService.registerServlet("/servlet/dav/principals", principalServlet, null, null);
        OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, principalPerformer);
        principalPerformer.setGlobalMixins(mixin);
        this.mixin = mixin;
        /*
         * OSGi mixins
         */
        registerService(PropertyMixin.class, new PrincipalCollectionSet());
        registerService(PropertyMixin.class, new CalendarHomeSet());
        registerService(PropertyMixin.class, new AddressbookHomeSet());
        openTrackers();
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
