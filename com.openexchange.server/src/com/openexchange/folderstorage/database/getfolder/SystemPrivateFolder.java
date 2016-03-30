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

package com.openexchange.folderstorage.database.getfolder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.database.LocalizedDatabaseFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Collators;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link SystemPrivateFolder} - Gets the system shared folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SystemPrivateFolder {

    /**
     * Initializes a new {@link SystemPrivateFolder}.
     */
    private SystemPrivateFolder() {
        super();
    }

    /**
     * Gets the database folder representing system private folder.
     *
     * @param fo The folder object fetched from database
     * @return The database folder representing system private folder
     */
    public static DatabaseFolder getSystemPrivateFolder(final FolderObject fo) {
        /*
         * The system public folder
         */
        final DatabaseFolder retval = new LocalizedDatabaseFolder(fo);
        retval.setName(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME);
        // Enforce getSubfolders() on storage
        retval.setSubfolderIDs(null);
        retval.setSubscribedSubfolders(true);
        return retval;
    }

    /**
     * Gets the subfolder identifiers of database folder representing system private folder.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system private folder
     * @throws OXException If the database folder cannot be returned
     */
    public static int[] getSystemPrivateFolderSubfoldersAsInt(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        try {
            /*
             * The system private folder
             */
            final List<FolderObject> list =
                ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                    FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                    user.getId(),
                    user.getGroups(),
                    ctx,
                    userPerm,
                    null,
                    con)).asList();
            StringHelper stringHelper = null;
            for (final FolderObject folderObject : list) {
                /*
                 * Check if folder is user's default folder and set locale-sensitive name
                 */
                if (folderObject.isDefaultFolder()) {
                    final int module = folderObject.getModule();
                    if (FolderObject.CALENDAR == module) {
                        {
                            if (null == stringHelper) {
                                stringHelper = StringHelper.valueOf(user.getLocale());
                            }
                            folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME));
                        }
                    } else if (FolderObject.CONTACT == module) {
                        {
                            if (null == stringHelper) {
                                stringHelper = StringHelper.valueOf(user.getLocale());
                            }
                            folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME));
                        }
                    } else if (FolderObject.TASK == module) {
                        {
                            if (null == stringHelper) {
                                stringHelper = StringHelper.valueOf(user.getLocale());
                            }
                            folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME));
                        }
                    }
                }
            }
            /*
             * Extract IDs
             */
            final int[] ret = new int[list.size()];
            int i = 0;
            for (final FolderObject folderObject : list) {
                ret[i++] = folderObject.getObjectID();
            }
            return ret;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the subfolder identifiers of database folder representing system private folder.
     *
     * @param user The user
     * @param userPerm The user permission bits
     * @param ctx The context
     * @param con The connection
     * @return The database folder representing system private folder
     * @throws OXException If the database folder cannot be returned
     */
    public static List<String[]> getSystemPrivateFolderSubfolders(final User user, final UserPermissionBits userPerm, final Context ctx, final Connection con) throws OXException {
        try {
            /*
             * The system private folder
             */
            final List<FolderObject> list =
                ((FolderObjectIterator) OXFolderIteratorSQL.getVisibleSubfoldersIterator(
                    FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                    user.getId(),
                    user.getGroups(),
                    ctx,
                    userPerm,
                    null,
                    con)).asList();
            StringHelper stringHelper = null;
            for (final FolderObject folderObject : list) {
                /*
                 * Check if folder is user's default folder and set locale-sensitive name
                 */
                if (folderObject.isDefaultFolder()) {
                    final int module = folderObject.getModule();
                    if (FolderObject.CALENDAR == module) {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(user.getLocale());
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME));
                    } else if (FolderObject.CONTACT == module) {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(user.getLocale());
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME));
                    } else if (FolderObject.TASK == module) {
                        if (null == stringHelper) {
                            stringHelper = StringHelper.valueOf(user.getLocale());
                        }
                        folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME));
                    }
                }
            }
            /*
             * Extract IDs
             */
            final List<String[]> ret = new ArrayList<String[]>(list.size());
            for (final FolderObject folderObject : list) {
                ret.add(new String[] {String.valueOf(folderObject.getObjectID()),folderObject.getFolderName()});
            }
            return ret;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * {@link NameComparator} - Sorts names with respect to a certain locale
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class NameComparator implements Comparator<FolderObject> {

        private final Collator collator;

        public NameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final FolderObject folder1, final FolderObject folder2) {
            return collator.compare(folder1.getFolderName(), folder2.getFolderName());
        }

    }

}
