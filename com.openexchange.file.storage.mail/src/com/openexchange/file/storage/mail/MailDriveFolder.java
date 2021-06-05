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

package com.openexchange.file.storage.mail;

import java.util.Collections;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.file.storage.mail.FullName.Type;
import com.openexchange.file.storage.mail.osgi.Services;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

/**
 * {@link MailDriveFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFolder extends DefaultFileStorageFolder implements TypeAware {

    /** The constant for full name of an account's root folder. */
    private final FileStorageFolderType type;
    private final DefaultFileStoragePermission permission;

    /**
     * Initializes a new {@link MailDriveFolder}.
     */
    public MailDriveFolder(final int userId) {
        super();
        type = FileStorageFolderType.NONE;
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;

        DefaultFileStoragePermission p = DefaultFileStoragePermission.newInstance();
        p.setEntity(userId);
        p.setAdmin(false);
        p.setAllPermissions(FileStoragePermission.READ_FOLDER, FileStoragePermission.READ_ALL_OBJECTS, FileStoragePermission.NO_PERMISSIONS, FileStoragePermission.NO_PERMISSIONS);
        this.permission = p;
        permissions = Collections.<FileStoragePermission> singletonList(p);
        ownPermission = p;
        createdBy = userId;
        modifiedBy = userId;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified full name.
     *
     * @param fullName The full name
     * @throws OXException If parsing fails
     */
    public MailDriveFolder parseFullName(FullName fullName, Session session) throws OXException {
        try {
            rootFolder = fullName.isDefaultFolder();
            b_rootFolder = true;
            id = fullName.getFolderId();

            if (rootFolder) {
                setParentId(null);
                setName(StringHelper.valueOf(getSessionUserLocale(session)).getString(MailDriveStrings.NAME_ATTACHMENTS_ALL));
                setSubfolders(true);
                setSubscribedSubfolders(true);
                permission.setReadPermission(FileStoragePermission.NO_PERMISSIONS);
            } else {
                setParentId(Type.ALL.getFolderId());
                setName(getLocalizedNameFor(fullName, session));
                setSubfolders(false);
                setSubscribedSubfolders(false);

            }
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return this;
    }

    private static String getLocalizedNameFor(FullName fullName, Session session) throws OXException {
        switch (fullName.getType()) {
            case ALL:
                return StringHelper.valueOf(getSessionUserLocale(session)).getString(MailDriveStrings.NAME_ATTACHMENTS_ALL);
            case RECEIVED:
                {
                    StringHelper stringHelper = StringHelper.valueOf(getSessionUserLocale(session));
                    String translated = stringHelper.getString(MailDriveStrings.NAME_ATTACHMENTS_DEDICATED);
                    return String.format(translated, stringHelper.getString(MailStrings.INBOX));
                }
            case SENT:
                {
                    StringHelper stringHelper = StringHelper.valueOf(getSessionUserLocale(session));
                    String translated = stringHelper.getString(MailDriveStrings.NAME_ATTACHMENTS_DEDICATED);
                    return String.format(translated, stringHelper.getString(MailStrings.SENT));
                }
            default:
                return null;
        }
    }

    /**
     * Extracts the locale from specified session
     *
     * @param session The session
     * @return The locale
     * @throws OXException If extracting the locale fails
     */
    public static Locale getSessionUserLocale(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return Services.getService(UserService.class).getUser(session.getUserId(), session.getContextId()).getLocale();
    }

}
