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

package com.openexchange.share.json.osgi;

import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.quota.QuotaService;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.json.ShareActionFactory;
import com.openexchange.share.json.fields.ExtendedFolderPermissionsField;
import com.openexchange.share.json.fields.ExtendedObjectPermissionsField;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.user.UserService;

/**
 * {@link ShareJsonActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareJsonActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareJsonActivator.class);

    /**
     * Initializes a new {@link ShareJsonActivator}.
     */
    public ShareJsonActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ShareService.class, UserService.class, ContextService.class, GroupService.class, ContactService.class,
            CapabilityService.class, SessiondService.class, ShareNotificationService.class, ModuleSupport.class, QuotaService.class, ContactUserStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: \"com.openexchange.share.json\"");
        trackService(IDBasedFileAccessFactory.class);
        trackService(FolderService.class);
        trackService(TranslatorFactory.class);
        trackService(DispatcherPrefixService.class);
        trackService(HostnameService.class);
        openTrackers();

        registerModule(new ShareActionFactory(this), "share/management");
        registerService(AdditionalFolderField.class, new ExtendedFolderPermissionsField(this));
        registerService(AdditionalFileField.class, new ExtendedObjectPermissionsField(this));
    }

}
