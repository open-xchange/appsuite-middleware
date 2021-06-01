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

package com.openexchange.groupware.settings.tree.modules.mail;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.AbstractWarningAwareReadOnlyValue;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;
import com.openexchange.user.User;


/**
 * {@link Namespace}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Namespace extends AbstractWarningAwareReadOnlyValue implements PreferencesItemService {

    /**
     * Initializes a new {@link Namespace}.
     */
    public Namespace() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "namespace" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return this;
    }

    @Override
    public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            String namespace = mailAccess.getFolderStorage().getDefaultFolderPrefix();
            setting.setSingleValue(namespace);
        } finally {
            if (null != mailAccess) {
                mailAccess.close();
            }
        }
    }

    @Override
    public boolean isAvailable(UserConfiguration userConfig) {
        return userConfig.hasWebMail();
    }

}
