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

package com.openexchange.dav.internal;

import static com.openexchange.dav.DAVProtocol.CALENDARSERVER_NS;
import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ResourceId;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link ShareHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class ShareHelper {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareHelper.class);

    /**
     * Shares a folder collection according to the supplied <code>DAV:share-resource</code> element.
     *
     * @param folderCollection The folder collection to share
     * @param shareResourceElement The <code>DAV:share-resource</code> element to use
     */
    public static void share(FolderCollection<?> folderCollection, Element shareElement) throws WebdavProtocolException {
        share(folderCollection, parseShare(folderCollection.getFactory(), folderCollection.getPermissions(), shareElement));
    }

    /**
     * Shares a folder collection according to the supplied <code>http://calendarserver.org/ns/:share</code> element.
     *
     * @param folderCollection The folder collection to share
     * @param shareResourceElement The <code>http://calendarserver.org/ns/:share</code> element to use
     */
    public static void shareResource(FolderCollection<?> folderCollection, Element shareResourceElement) throws WebdavProtocolException {
        share(folderCollection, parseShareResource(folderCollection.getFactory(), folderCollection.getPermissions(), shareResourceElement));
    }

    private static void share(FolderCollection<?> folderCollection, List<Permission> updatedPermissions) throws WebdavProtocolException {
        DAVFactory factory = folderCollection.getFactory();
        UserizedFolder folder = folderCollection.getFolder();
        if (null != updatedPermissions) {
            if (false == folder.getOwnPermission().isAdmin()) {
                throw WebdavProtocolException.generalError(folderCollection.getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
            ParameterizedFolder updatableFolder = FolderCollection.prepareUpdatableFolder(folder);
            updatableFolder.setPermissions(updatedPermissions.toArray(new Permission[updatedPermissions.size()]));
            try {
                factory.requireService(FolderService.class).updateFolder(updatableFolder, folder.getLastModifiedUTC(), factory.getSession(), null);
            } catch (OXException e) {
                if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
                    throw WebdavProtocolException.generalError(e, folderCollection.getUrl(), HttpServletResponse.SC_FORBIDDEN);
                }
                throw WebdavProtocolException.generalError(e, folderCollection.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    private static List<Permission> parseShare(DAVFactory factory, Permission[] originalPermissions, Element shareElement) {
        List<Permission> permissions = new ArrayList<Permission>(Arrays.asList(originalPermissions));
        boolean hasChanged = false;
        List<Element> setElements = shareElement.getChildren("set", CALENDARSERVER_NS);
        if (null != setElements && 0 < setElements.size()) {
            for (Element setElement : setElements) {
                PrincipalURL principal = optPrincipal(factory, setElement.getChild("href", DAV_NS));
                if (null != principal) {
                    hasChanged |= removePermission(permissions, principal.getPrincipalID());
                    if (null != setElement.getChild("read", CALENDARSERVER_NS)) {
                        hasChanged |= permissions.add(createReadOnlyPermission(principal));
                    } else if (null != setElement.getChild("read-write", CALENDARSERVER_NS)) {
                        hasChanged |= permissions.add(createReadWritePermission(principal));
                    }
                }
            }
        }
        List<Element> removeElements = shareElement.getChildren("remove", CALENDARSERVER_NS);
        if (null != removeElements && 0 < removeElements.size()) {
            for (Element removeElement : removeElements) {
                PrincipalURL principal = optPrincipal(factory, removeElement.getChild("href", DAV_NS));
                if (null != principal) {
                    hasChanged |= removePermission(permissions, principal.getPrincipalID());
                }
            }
        }
        return hasChanged ? permissions : null;
    }

    private static List<Permission> parseShareResource(DAVFactory factory, Permission[] originalPermissions, Element shareResourceElement) {
        List<Element> shareeElements = shareResourceElement.getChildren("sharee", DAV_NS);
        if (null != shareeElements && 0 < shareeElements.size()) {
            List<Permission> permissions = new ArrayList<Permission>(Arrays.asList(originalPermissions));
            boolean hasChanged = false;
            for (Element shareeElement : shareeElements) {
                PrincipalURL principal = optPrincipal(factory, shareeElement.getChild("href", DAV_NS));
                if (null != principal) {
                    Element shareAccessElement = shareeElement.getChild("share-access", DAV_NS);
                    if (null != shareAccessElement) {
                        hasChanged |= removePermission(permissions, principal.getPrincipalID());
                        if (null != shareAccessElement.getChild("read", DAVProtocol.DAV_NS)) {
                            hasChanged |= permissions.add(createReadOnlyPermission(principal));
                        } else if (null != shareAccessElement.getChild("read-write", DAV_NS)) {
                            hasChanged |= permissions.add(createReadWritePermission(principal));
                        } else {
                            // assume "no-access", otherwise, so leave permission removed
                        }
                    }
                }
            }
            if (hasChanged) {
                return permissions;
            }
        }
        return null;
    }

    private static PrincipalURL optPrincipal(DAVFactory factory, Element hrefElement) {
        if (null != hrefElement) {
            String text = hrefElement.getText();
            if (Strings.isNotEmpty(text)) {
                try {
                    return parsePrincipal(factory, text);
                } catch (OXException | IllegalArgumentException e) {
                    LOG.warn("Error resolving sharee {}", text, e);
                }
            }
        }
        return null;
    }

    private static PrincipalURL parsePrincipal(DAVFactory factory, String href) throws IllegalArgumentException, OXException {
        /*
         * strip a leading "mailto:" from the URI (inserted by some Mac calendar client even for principal URIs)
         */
        if (href.startsWith("mailto:")) {
            href = href.substring(7);
        }
        /*
         * try to interpret as principal URL directly
         */
        ConfigViewFactory configViewFactory = factory.getServiceSafe(ConfigViewFactory.class);
        PrincipalURL principalURL = PrincipalURL.parse(href,configViewFactory);
        if (null != principalURL) {
            return principalURL;
        }
        /*
         * try to interpret as resource id, too
         */
        ResourceId resourceId = ResourceId.parse(href);
        if (null != resourceId) {
            if (CalendarUserType.INDIVIDUAL.equals(resourceId.getCalendarUserType())) {
                return new PrincipalURL(resourceId.getEntity(), CalendarUserType.INDIVIDUAL, configViewFactory);
            } else if (CalendarUserType.GROUP.equals(resourceId.getCalendarUserType())) {
                return new PrincipalURL(resourceId.getEntity(), CalendarUserType.GROUP, configViewFactory);
            } else {
                throw new IllegalArgumentException("Unexpected resource type: " + href);
            }
        }
        /*
         * try to parse as e-mail address & resolve user
         */
        String mail = null;
        try {
            mail = new QuotedInternetAddress(href).getAddress();
        } catch (AddressException e) {
            throw new IllegalArgumentException(e);
        }
        if (Strings.isNotEmpty(mail)) {
            User user = factory.requireService(UserService.class).searchUser(mail, factory.getContext(), true, true, false);
            return new PrincipalURL(user.getId(), CalendarUserType.INDIVIDUAL, configViewFactory);
        }
        return null;
    }

    /**
     * Gets a value indicating whether the supplied permission implies (at least) a simplified WebDAV "read" access level.
     *
     * @param permission The permission to check
     * @return <code>true</code> if "read" permissions can be assumed, <code>false</code>, otherwise
     */
    public static boolean impliesReadPermissions(Permission permission) {
        return null != permission &&
            permission.getFolderPermission() >= Permission.READ_FOLDER &&
            permission.getReadPermission() >= Permission.READ_OWN_OBJECTS
        ;
    }

    /**
     * Gets a value indicating whether the supplied permission implies (at least) a simplified WevDAV "read-write" access level.
     *
     * @param permission The permission to check
     * @return <code>true</code> if "read-write" permissions can be assumed, <code>false</code>, otherwise
     */
    public static boolean impliesReadWritePermissions(Permission permission) {
        return null != permission &&
            permission.getFolderPermission() >= Permission.CREATE_OBJECTS_IN_FOLDER &&
            permission.getWritePermission() >= Permission.WRITE_OWN_OBJECTS &&
            permission.getDeletePermission() >= Permission.DELETE_OWN_OBJECTS &&
            permission.getReadPermission() >= Permission.READ_OWN_OBJECTS
        ;
    }

    /**
     * Creates a new permission representing the simplified WebDAV "read-write" access level, i.e. a combination of
     * {@link Permission#CREATE_OBJECTS_IN_FOLDER}, {@link Permission#READ_ALL_OBJECTS}, {@link Permission#WRITE_ALL_OBJECTS} and
     * {@link Permission#DELETE_ALL_OBJECTS} for folder-, read-, write- and delete-permissions.
     *
     * @param principal The principal to create the permission for
     * @return The permission
     */
    private static Permission createReadWritePermission(PrincipalURL principal) {
        BasicPermission permission = new BasicPermission();
        permission.setEntity(principal.getPrincipalID());
        permission.setGroup(CalendarUserType.GROUP.equals(principal.getType()));
        permission.setAllPermissions(Permission.CREATE_OBJECTS_IN_FOLDER, Permission.READ_ALL_OBJECTS, Permission.WRITE_ALL_OBJECTS, Permission.DELETE_ALL_OBJECTS);
        return permission;
    }

    /**
     * Creates a new permission representing the simplified WebDAV "read" access level, i.e. a combination of
     * {@link Permission#READ_FOLDER}, {@link Permission#READ_ALL_OBJECTS}, {@link Permission#NO_PERMISSIONS} and
     * {@link Permission#NO_PERMISSIONS} for folder-, read-, write- and delete-permissions.
     *
     * @param principal The principal to create the permission for
     * @return The permission
     */
    private static Permission createReadOnlyPermission(PrincipalURL principal) {
        BasicPermission permission = new BasicPermission();
        permission.setEntity(principal.getPrincipalID());
        permission.setGroup(CalendarUserType.GROUP.equals(principal.getType()));
        permission.setAllPermissions(Permission.READ_FOLDER, Permission.READ_ALL_OBJECTS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS);
        return permission;
    }

    private static boolean removePermission(List<Permission> permissions, int entity) {
        for (Iterator<Permission> iterator = permissions.iterator(); iterator.hasNext();) {
            if (entity == iterator.next().getEntity()) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

}
