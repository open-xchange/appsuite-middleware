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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.share.impl.groupware;

import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailSharedFolderPathResolver;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;


/**
 * {@link MailModuleAdjuster}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailModuleAdjuster implements ModuleAdjuster {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailModuleAdjuster}.
     *
     * @param services The OSGi service look-up
     */
    public MailModuleAdjuster(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId) throws OXException {
        if (!target.isFolder()) {
            int module = target.getModule();
            String m = services.getService(ModuleSupport.class).getShareModule(module);
            throw ShareExceptionCodes.SHARING_ITEMS_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
        }

        FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(target.getFolder());
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session, fa.getAccountId());
            mailAccess.connect();

            if (!mailAccess.getMailConfig().getCapabilities().hasPermissions()) {
                int module = target.getModule();
                String m = services.getService(ModuleSupport.class).getShareModule(module);
                throw ShareExceptionCodes.SHARING_FOLDERS_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
            }

            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (!IMailSharedFolderPathResolver.class.isInstance(folderStorage)) {
                int module = target.getModule();
                String m = services.getService(ModuleSupport.class).getShareModule(module);
                throw ShareExceptionCodes.SHARING_FOLDERS_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
            }

            IMailSharedFolderPathResolver pathResolver = (IMailSharedFolderPathResolver) folderStorage;
            if (!pathResolver.isResolvingSharedFolderPathSupported(fa.getFullname())) {
                int module = target.getModule();
                String m = services.getService(ModuleSupport.class).getShareModule(module);
                throw ShareExceptionCodes.SHARING_FOLDERS_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
            }

            String resolvedFullName = pathResolver.resolveSharedFolderPath(fa.getFullname(), targetUserId);
            return new ShareTarget(target.getModule(), MailFolderUtility.prepareFullname(fa.getAccountId(), resolvedFullName), target.getFolder(), null);
        } finally {
            MailAccess.closeInstance(mailAccess);
        }
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId) throws OXException {
        if (!target.isFolder()) {
            int module = target.getModule();
            String m = services.getService(ModuleSupport.class).getShareModule(module);
            throw ShareExceptionCodes.SHARING_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
        }

        return null;
    }

}
