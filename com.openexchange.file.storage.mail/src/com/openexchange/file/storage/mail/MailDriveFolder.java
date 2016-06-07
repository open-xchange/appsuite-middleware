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
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return this;
    }

    private static String getLocalizedNameFor(FullName fullName, Session session) throws OXException {
        switch (fullName.getType()) {
            case ALL:
                return StringHelper.valueOf(getSessionUserLocale(session)).getString(MailDriveStrings.NAME_ATTACHMENTS_ALL);
            case RECEIVED:
                return StringHelper.valueOf(getSessionUserLocale(session)).getString(MailDriveStrings.NAME_ATTACHMENTS_RECEIVED);
            case SENT:
                return StringHelper.valueOf(getSessionUserLocale(session)).getString(MailDriveStrings.NAME_ATTACHMENTS_SENT);
            default:
                return null;
        }
    }

    private static Locale getSessionUserLocale(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return Services.getService(UserService.class).getUser(session.getUserId(), session.getContextId()).getLocale();
    }

}
