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

package com.openexchange.dav.principals.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.principals.PrincipalFactory;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.group.GroupStorage;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link ResourcePrincipalCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class ResourcePrincipalCollection extends DAVCollection {

    public static final String NAME = "resources";

    private final PrincipalFactory factory;

    /**
     * Initializes a new {@link ResourcePrincipalCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     */
    public ResourcePrincipalCollection(PrincipalFactory factory, WebdavPath url) {
        super(factory, url);
        this.factory = factory;
        includeProperties(new CurrentUserPrincipal(factory));
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] {
            new BasicPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, true, Permissions.createPermissionBits(
                Permission.READ_FOLDER, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, Permission.NO_PERMISSIONS, false))
        };
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        try {
            Resource[] resources = factory.requireService(ResourceService.class).searchResources("*", factory.getContext());
            List<WebdavResource> children = new ArrayList<WebdavResource>(resources.length);
            for (Resource resource : resources) {
                children.add(createResourceResource(resource));
            }
            return children;
        } catch (OXException e) {
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return new Date(0L);
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return new Date(0L);
    }

    @Override
    public String getSyncToken() throws WebdavProtocolException {
        return "0";
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return "Resources";
    }

    @Override
    public ResourcePrincipalResource getChild(String name) throws WebdavProtocolException {
        if (Strings.isEmpty(name)) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            int id = Integer.parseInt(name);
            return createResourceResource(factory.requireService(ResourceService.class).getResource(id, factory.getContext()));
        } catch (OXException | NumberFormatException e) {
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private ResourcePrincipalResource createResourceResource(Resource resource) {
        return new ResourcePrincipalResource(factory, resource, constructPathForChildResource(String.valueOf(resource.getIdentifier())));
    }

}
