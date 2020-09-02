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

package com.openexchange.file.storage.xctx.osgi;

import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.context.ContextService;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.xctx.XctxFileStorageService;
import com.openexchange.file.storage.xctx.subscription.XctxShareSubscriptionProvider;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.guest.GuestService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.share.ShareService;
import com.openexchange.share.subscription.ShareSubscriptionProvider;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XctxFileStorageActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFileStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link XctxFileStorageActivator}.
     */
    public XctxFileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContextService.class, UserService.class, ShareService.class, GuestService.class, PasswordMechRegistry.class, 
            FileStorageAccountManagerLookupService.class, FolderService.class, InfostoreFacade.class, InfostoreSearchEngine.class, 
            CapabilityService.class, UserPermissionService.class 
        };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            getLogger(XctxFileStorageActivator.class).info("starting bundle {}", context.getBundle());
            /*
             * register cross-context file storage service
             */
            XctxFileStorageService fileStorageService = new XctxFileStorageService(this);
            registerService(FileStorageService.class, fileStorageService);
            registerService(ShareSubscriptionProvider.class, new XctxShareSubscriptionProvider(this, fileStorageService));
        } catch (Exception e) {
            getLogger(XctxFileStorageActivator.class).error("error starting {}", context.getBundle(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(XctxFileStorageActivator.class).info("stopping bundle {}", context.getBundle());
        super.stopBundle();
    }


}
