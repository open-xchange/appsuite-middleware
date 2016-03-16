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

package com.openexchange.publish.online.infostore.osgi;

import org.osgi.service.http.HttpService;
import com.openexchange.context.ContextService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.online.infostore.InfostoreDocumentPublicationService;
import com.openexchange.publish.online.infostore.InfostorePublicationServlet;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

public class Activator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Activator.class);

    private static final String ALIAS = InfostoreDocumentPublicationService.PREFIX;
    private InfostorePublicationServlet servlet;


    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {IDBasedFileAccessFactory.class, HttpService.class, PublicationDataLoaderService.class, ContextService.class, InfostoreFacade.class, UserService.class, UserPermissionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final IDBasedFileAccessFactory fileAccessFactory = getService(IDBasedFileAccessFactory.class);

        final InfostoreDocumentPublicationService infostorePublisher = new InfostoreDocumentPublicationService(fileAccessFactory);
        registerService(PublicationService.class, infostorePublisher, null);

        final HttpService httpService = getService(HttpService.class);
        final PublicationDataLoaderService publicationDataLoaderService = getService(PublicationDataLoaderService.class);
        final ContextService contextService = getService(ContextService.class);

        if (servlet == null) {
            try {
                httpService.registerServlet(ALIAS, servlet = new InfostorePublicationServlet(
                    contextService,
                    publicationDataLoaderService,
                    fileAccessFactory,
                    infostorePublisher), null, null);
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();

        final HttpService httpService = getService(HttpService.class);
        if(httpService != null && servlet != null) {
            httpService.unregister(ALIAS);
            servlet = null;
        }
    }
}
