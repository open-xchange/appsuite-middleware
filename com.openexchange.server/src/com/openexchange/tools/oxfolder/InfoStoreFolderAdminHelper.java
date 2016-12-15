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

package com.openexchange.tools.oxfolder;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.oxfolder.OXFolderSQL.getNextSerialForAdmin;
import static com.openexchange.tools.oxfolder.OXFolderSQL.insertDefaultFolderSQL;
import static com.openexchange.tools.oxfolder.OXFolderSQL.lookUpFolder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link InfoStoreFolderAdminHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class InfoStoreFolderAdminHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfoStoreFolderAdminHelper.class);

    /**
     * Initializes a new {@link InfoStoreFolderAdminHelper}.
     */
    private InfoStoreFolderAdminHelper() {
        super();
    }

    /**
     * Adds all default infostore folders for a user at their default locations in the folder tree.
     *
     * @param connection A (writable) database connection
     * @param contextID The context identifier
     * @param userID The user identifier
     * @return The identifier of the created folder
     */
    public static void addDefaultFolders(Connection connection, int contextID, int userID) throws OXException {
        int userFolderID = addDefaultFolder(connection, contextID, userID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, FolderObject.PUBLIC, null);
        addDefaultFolder(connection, contextID, userID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.TRASH, null);
        int documentsFolderID = addDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.DOCUMENTS, null);
        addDefaultFolder(connection, contextID, userID, documentsFolderID, FolderObject.TEMPLATES, null);
        addDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.PICTURES, null);
        addDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.MUSIC, null);
        addDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.VIDEOS, null);
    }
    
    /**
     * Adds all default infostore folders for a user at their default locations in the folder tree. Expect the User main folder 
     * and the trash folder, all other folders are not created like default folders and remain deletable.
     *
     * @param connection A (writable) database connection
     * @param contextID The context identifier
     * @param userID The user identifier
     * @param locale 
     * @return The identifier of the created folder
     */
    public static void addDefaultFoldersDeletable(Connection connection, int contextID, int userID, Locale locale) throws OXException {
        int userFolderID = addDefaultFolder(connection, contextID, userID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, FolderObject.PUBLIC, locale);
        addDefaultFolder(connection, contextID, userID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.TRASH, locale);
        int documentsFolderID = addDeletableDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.DOCUMENTS, locale);
        addDeletableDefaultFolder(connection, contextID, userID, documentsFolderID, FolderObject.TEMPLATES, locale);
        addDeletableDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.PICTURES, locale);
        addDeletableDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.MUSIC, locale);
        addDeletableDefaultFolder(connection, contextID, userID, userFolderID, FolderObject.VIDEOS, locale);
    }
    
    /**
     * Adds trash and users main default infostore folders for a user at their default locations in the folder tree.
     *
     * @param connection A (writable) database connection
     * @param contextID The context identifier
     * @param userID The user identifier
     * @return The identifier of the created folder
     */
    public static void addDefaultFoldersNone(Connection connection, int contextID, int userID) throws OXException {
        addDefaultFolder(connection, contextID, userID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, FolderObject.PUBLIC, null);
        addDefaultFolder(connection, contextID, userID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.TRASH, null);
    }

    /**
     * Adds a default infostore folder of a specific type for a user below an existing parent folder.
     * <p/>
     * Currently, the following default folder types are supported:
     * <ul>
     * <li>{@link FolderObject#PUBLIC} - for the user's default infostore 'home' folder</li>
     * <li>{@link FolderObject#TRASH} - for the user's 'Trash' folder</li>
     * <li>{@link FolderObject#DOCUMENTS} - for the user's 'Documents' folder</li>
     * <li>{@link FolderObject#PICTURES} - for the user's 'Pictures' folder</li>
     * <li>{@link FolderObject#MUSIC} - for the user's 'Music' folder</li>
     * <li>{@link FolderObject#VIDEOS} - for the user's 'Videos' folder</li>
     * <li>{@link FolderObject#TEMPLATES} - for the user's 'Templates' folder</li>
     * </ul>
     *
     * @param connection A (writable) database connection
     * @param contextID The context identifier
     * @param userID The user identifier
     * @param parentFolderID The parent folder identifier
     * @param type The folder type
     * @param locale The user's locale for also considering existing localized folder names, or <code>null</code> if not needed
     * @return The identifier of the created folder
     */
    private static int addDefaultFolder(Connection connection, int contextID, int userID, int parentFolderID, int type, Locale locale) throws OXException {
        try {
            String folderName = getDefaultFolderName(connection, contextID, userID, type);
            Context context = new ContextImpl(contextID);
            /*
             * insert new default folder
             */
            FolderObject folder = prepareDefaultFolder(userID, parentFolderID, type, folderName);
            int folderID = getNextSerialForAdmin(context, connection);
            insertDefaultFolderSQL(folderID, userID, folder, System.currentTimeMillis(), true, context, connection);
            LOG.info("Default infostore folder '{}' [type={}, id={}] for user {} in context {} created successfully.",
                folderName, I(type), I(folderID), I(userID), I(contextID));
            return folderID;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }
    
    private static int addDeletableDefaultFolder(Connection connection, int contextID, int userID, int parentFolderID, int type, Locale locale) throws OXException {
        try {
            Context context = new ContextImpl(contextID);
            /*
             * insert new default folder
             */
            String localizedFolderName = getLocalizedDefaultFolderName(connection, locale, contextID, userID, type);
            FolderObject folder = prepareDefaultFolder(userID, parentFolderID, FolderObject.PUBLIC, localizedFolderName);
            int folderID = getNextSerialForAdmin(context, connection);
            insertDefaultFolderSQL(folderID, userID, folder, System.currentTimeMillis(), false, context, connection);
            LOG.info("Default infostore folder '{}' [type={}, id={}] for user {} in context {} created successfully.",
                localizedFolderName, I(FolderObject.PUBLIC), I(folderID), I(userID), I(contextID));
            return folderID;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static String getDefaultFolderName(Connection connection, int contextID, int userID, int type) throws OXException, SQLException {
        switch (type) {
            case FolderObject.PUBLIC:
                Context context = new ContextImpl(contextID);
                String name = OXFolderAdminHelper.getUserDisplayName(userID, contextID, connection);
                if (null == name) {
                    throw LdapExceptionCode.USER_NOT_FOUND.create(userID, contextID).setPrefix("USR");
                }
                int resetLen = name.length();
                int count = 0;
                while (-1 != lookUpFolder(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, name, FolderObject.INFOSTORE, connection, context)) {
                    name = name.substring(0, resetLen) + " (" + String.valueOf(++count) + ')';
                }
                return name;
            case FolderObject.TRASH:
                return FolderStrings.SYSTEM_TRASH_FILES_FOLDER_NAME;
            case FolderObject.DOCUMENTS:
                return FolderStrings.SYSTEM_USER_DOCUMENTS_FOLDER_NAME;
            case FolderObject.PICTURES:
                return FolderStrings.SYSTEM_USER_PICTURES_FOLDER_NAME;
            case FolderObject.MUSIC:
                return FolderStrings.SYSTEM_USER_MUSIC_FOLDER_NAME;
            case FolderObject.VIDEOS:
                return FolderStrings.SYSTEM_USER_VIDEOS_FOLDER_NAME;
            case FolderObject.TEMPLATES:
                return FolderStrings.SYSTEM_USER_TEMPLATES_FOLDER_NAME;
            default:
                throw OXFolderExceptionCode.INVALID_TYPE.create(Integer.valueOf(0), Integer.valueOf(type), Integer.valueOf(contextID));
        }
    }

    private static String getLocalizedDefaultFolderName(Connection connection, Locale locale, int contextID, int userID, int type) throws OXException, SQLException {
        switch (type) {
            case FolderObject.PUBLIC:
                Context context = new ContextImpl(contextID);
                String name = OXFolderAdminHelper.getUserDisplayName(userID, contextID, connection);
                int resetLen = name.length();
                int count = 0;
                while (-1 != lookUpFolder(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, name, FolderObject.INFOSTORE, connection, context)) {
                    name = name.substring(0, resetLen) + " (" + String.valueOf(++count) + ')';
                }
                return name;
            case FolderObject.TRASH:
                return StringHelper.valueOf(locale).getString(FolderStrings.SYSTEM_TRASH_FILES_FOLDER_NAME);
            case FolderObject.DOCUMENTS:
                return StringHelper.valueOf(locale).getString(FolderStrings.SYSTEM_USER_DOCUMENTS_FOLDER_NAME);
            case FolderObject.PICTURES:
                return StringHelper.valueOf(locale).getString(FolderStrings.SYSTEM_USER_PICTURES_FOLDER_NAME);
            case FolderObject.MUSIC:
                return StringHelper.valueOf(locale).getString(FolderStrings.SYSTEM_USER_MUSIC_FOLDER_NAME);
            case FolderObject.VIDEOS:
                return StringHelper.valueOf(locale).getString(FolderStrings.SYSTEM_USER_VIDEOS_FOLDER_NAME);
            case FolderObject.TEMPLATES:
                return StringHelper.valueOf(locale).getString(FolderStrings.SYSTEM_USER_TEMPLATES_FOLDER_NAME);
            default:
                throw OXFolderExceptionCode.INVALID_TYPE.create(Integer.valueOf(0), Integer.valueOf(type), Integer.valueOf(contextID));
        }
    }

    private static FolderObject prepareDefaultFolder(int userID, int parentFolderID, int type, String name) {
        FolderObject folder = new FolderObject();
        folder.setPermissionsAsArray(new OCLPermission[] { getAdminPermissions(userID) });
        folder.setDefaultFolder(true);
        folder.setParentFolderID(parentFolderID);
        folder.setType(type);
        folder.setFolderName(name);
        folder.setModule(FolderObject.INFOSTORE);
        return folder;
    }

    private static OCLPermission getAdminPermissions(int userID) {
        OCLPermission permission = new OCLPermission();
        permission.setEntity(userID);
        permission.setGroupPermission(false);
        permission.setAllPermission(
            OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        permission.setFolderAdmin(true);
        return permission;
    }

}
