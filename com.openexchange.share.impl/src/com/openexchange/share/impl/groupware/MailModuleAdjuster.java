/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.impl.groupware;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
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
import com.openexchange.share.core.ModuleAdjuster;
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
    public Collection<String> getModules() {
        return Collections.singleton(Module.MAIL.getName());
    }

    @Override
    public ShareTarget adjustTarget(ShareTarget target, Session session, int targetUserId, Connection connection) throws OXException {
        if (null != session && session.getUserId() == targetUserId) {
            return target; // same account
        }
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
            IMailSharedFolderPathResolver pathResolver = folderStorage.supports(IMailSharedFolderPathResolver.class);
            if (null == pathResolver) {
                int module = target.getModule();
                String m = services.getService(ModuleSupport.class).getShareModule(module);
                throw ShareExceptionCodes.SHARING_FOLDERS_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
            }

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
    public ShareTarget adjustTarget(ShareTarget target, int contextId, int requestUserId, int targetUserId, Connection connection) throws OXException {
        if (requestUserId == targetUserId) {
            return target; // same account
        }
        if (!target.isFolder()) {
            int module = target.getModule();
            String m = services.getService(ModuleSupport.class).getShareModule(module);
            throw ShareExceptionCodes.SHARING_NOT_SUPPORTED.create(m == null ? Integer.toString(module) : m);
        }

        return null;
    }

}
