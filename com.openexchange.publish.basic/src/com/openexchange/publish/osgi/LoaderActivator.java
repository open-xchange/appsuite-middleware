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

package com.openexchange.publish.osgi;

import com.openexchange.caching.CacheService;
import com.openexchange.contact.ContactService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.impl.CachingLoader;
import com.openexchange.publish.impl.CompositeLoaderService;
import com.openexchange.publish.impl.ContactFolderLoader;
import com.openexchange.publish.impl.InfostoreDocumentLoader;
import com.openexchange.publish.impl.InfostoreFolderLoader;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link LoaderActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class LoaderActivator extends HousekeepingActivator {

    @Override
    public void startBundle() throws Exception {
        final CompositeLoaderService compositeLoader = new CompositeLoaderService();

        final InfostoreFacade infostore = getService(InfostoreFacade.class);
        final UserService users = getService(UserService.class);
        final UserPermissionService userPermissions = getService(UserPermissionService.class);
        compositeLoader.registerLoader("infostore/object", new InfostoreDocumentLoader(infostore, users, userPermissions));
        compositeLoader.registerLoader("infostore", new InfostoreFolderLoader(infostore, users, userPermissions));

        final ContactFolderLoader contactLoader = new ContactFolderLoader(getService(ContactService.class));
        compositeLoader.registerLoader("contacts", contactLoader);

        registerService(PublicationDataLoaderService.class, new CachingLoader(this, compositeLoader), null);
    }

    @Override
    public void stopBundle() throws Exception {
        unregisterServices();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { CacheService.class, InfostoreFacade.class, UserService.class, UserPermissionService.class, ContactService.class };
    }

}
