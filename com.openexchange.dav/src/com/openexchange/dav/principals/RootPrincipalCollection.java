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

package com.openexchange.dav.principals;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.principals.groups.GroupPrincipalCollection;
import com.openexchange.dav.principals.resources.ResourcePrincipalCollection;
import com.openexchange.dav.principals.users.UserPrincipalCollection;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootPrincipalCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class RootPrincipalCollection extends DAVRootCollection {

    private final PrincipalFactory factory;

    /**
     * Initializes a new {@link RootPrincipalCollection}.
     *
     * @param factory The factory
     */
    public RootPrincipalCollection(PrincipalFactory factory) {
        super(factory, "Principals");
        this.factory = factory;
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        List<WebdavResource> children = new ArrayList<WebdavResource>(3);
        children.add(new UserPrincipalCollection(factory, constructPathForChildResource(UserPrincipalCollection.NAME)));
        children.add(new GroupPrincipalCollection(factory, constructPathForChildResource(GroupPrincipalCollection.NAME)));
        children.add(new ResourcePrincipalCollection(factory, constructPathForChildResource(ResourcePrincipalCollection.NAME)));
        return children;
    }

    @Override
    public DAVCollection getChild(String name) throws WebdavProtocolException {
        if (Strings.isEmpty(name)) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        if (UserPrincipalCollection.NAME.equals(name)) {
            return new UserPrincipalCollection(factory, constructPathForChildResource(UserPrincipalCollection.NAME));
        }
        if (GroupPrincipalCollection.NAME.equals(name)) {
            return new GroupPrincipalCollection(factory, constructPathForChildResource(GroupPrincipalCollection.NAME));
        }
        if (ResourcePrincipalCollection.NAME.equals(name)) {
            return new ResourcePrincipalCollection(factory, constructPathForChildResource(ResourcePrincipalCollection.NAME));
        }
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_FOUND);
    }

}
