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

package com.openexchange.file.storage.mail.settings;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.mail.FullName;
import com.openexchange.file.storage.mail.FullName.Type;
import com.openexchange.file.storage.mail.MailDriveConstants;
import com.openexchange.file.storage.mail.MailDriveFileStorageService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link AbstractMailDriveSetting}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class AbstractMailDriveSetting implements PreferencesItemService, ConfigTreeEquivalent {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailDriveSetting.class);

    /** The Mail Drive service */
    final MailDriveFileStorageService mailDriveService;

    /** The name in setting's path; e.g. <code>"allattachments"</code> */
    private final String nameInPath;

    /** The full name type */
    private final Type type;

    /**
     * Initializes a new {@link AbstractMailDriveSetting}.
     */
    protected AbstractMailDriveSetting(String nameInPath, Type type, MailDriveFileStorageService mailDriveService) {
        super();
        this.nameInPath = nameInPath;
        this.type = type;
        this.mailDriveService = mailDriveService;
    }

    /**
     * Gets the associated full name (if available)
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The associated full name or <code>null</code>
     * @throws OXException If full name cannot be returned
     */
    protected FullName getFullName(int userId, int contextId) throws OXException {
        return mailDriveService.getFullNameCollectionFor(userId, contextId).getFullNameFor(type);
    }

    @Override
    public String[] getPath() {
        return new String[] { "folder", "mailattachments", nameInPath };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                String folderId = getFullName(session.getUserId(), session.getContextId()).getFolderId();
                setting.setSingleValue(new FolderID(MailDriveConstants.ID, MailDriveConstants.ACCOUNT_ID, folderId).toUniqueID());
            }

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                try {
                    int contextId = userConfig.getContext().getContextId();
                    int userId = userConfig.getUserId();
                    return userConfig.hasWebMail() &&
                        userConfig.hasInfostore() &&
                        !userConfig.isGuest() &&
                        mailDriveService.isEnabledFor(userId, contextId) &&
                        (null != getFullName(userId, contextId));
                } catch (OXException e) {
                    // Failed to check
                    LOG.error("Failed to check Mail Drive availability", e);
                    return false;
                }
            }

        };
    }

    @Override
    public String getConfigTreePath() {
        return "folder/mailattachments/" + nameInPath;
    }

    @Override
    public String getJslobPath() {
        return "io.ox/core//folder/mailattachments/" + nameInPath;
    }
}
