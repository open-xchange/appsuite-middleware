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

package com.openexchange.groupware.settings.tree.modules.mail.folder;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.java.Reference;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Archive implements PreferencesItemService {

    /**
     * Default constructor.
     */
    public Archive() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "defaultFolder", "archive" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new ArchiveStandardFolderItemValue();
    }

    private static final class ArchiveStandardFolderItemValue extends AbstractStandardFolderItemValue {

        ArchiveStandardFolderItemValue() {
            super();
        }

        @Override
        protected void getValue(Setting setting, Reference<MailAccessAndStorage> mailAccessReference, Session session) throws OXException {
            MailAccountStorageService mass = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null == mass) {
                setting.setSingleValue(null);
                return;
            }

            MailAccount mailAccount = mass.getMailAccount(MailAccount.DEFAULT_ID, session.getUserId(), session.getContextId());
            String archiveFullname = mailAccount.getArchiveFullname();

            if (Strings.isEmpty(archiveFullname)) {
                setting.setSingleValue(null);
            } else {
                String fn = MailFolderUtility.prepareMailFolderParamOrElseReturn(archiveFullname);
                MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> primaryMailAccess = getConnectedMailAccess(session, mailAccessReference);
                if (primaryMailAccess.getFolderStorage().exists(fn)) {
                    setting.setSingleValue(prepareFullname(MailAccount.DEFAULT_ID, fn));
                } else {
                    setting.setSingleValue(null);
                }
            }
        }
    }

}
