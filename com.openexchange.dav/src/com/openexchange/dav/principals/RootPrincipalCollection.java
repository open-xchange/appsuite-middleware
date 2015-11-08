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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.dav.principals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.Privilege;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.mixins.CurrentUserPrivilegeSet;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.principals.groups.GroupPrincipalCollection;
import com.openexchange.dav.principals.resources.ResourcePrincipalCollection;
import com.openexchange.dav.principals.users.UserPrincipalCollection;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootPrincipalCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class RootPrincipalCollection extends DAVCollection {

    private final PrincipalFactory factory;

    /**
     * Initializes a new {@link RootPrincipalCollection}.
     *
     * @param factory The factory
     */
    public RootPrincipalCollection(PrincipalFactory factory) {
        super(factory, new WebdavPath());
        this.factory = factory;
        includeProperties(new CurrentUserPrincipal(factory), new PrincipalCollectionSet(),
            new CurrentUserPrivilegeSet(Privilege.READ, Privilege.READ_ACL, Privilege.READ_CURRENT_USER_PRIVILEGE_SET));
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return "httpd/unix-directory";
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        List<WebdavResource> children = new ArrayList<WebdavResource>(2);
        children.add(new UserPrincipalCollection(factory));
        children.add(new GroupPrincipalCollection(factory));
        children.add(new ResourcePrincipalCollection(factory));
        return children;
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
    public String getDisplayName() throws WebdavProtocolException {
        return "Principals";
    }

    @Override
    public DAVCollection getChild(String name) throws WebdavProtocolException {
        if (Strings.isEmpty(name)) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        if (UserPrincipalCollection.NAME.equals(name)) {
            return new UserPrincipalCollection(factory);
        }
        if (GroupPrincipalCollection.NAME.equals(name)) {
            return new GroupPrincipalCollection(factory);
        }
        if (ResourcePrincipalCollection.NAME.equals(name)) {
            return new ResourcePrincipalCollection(factory);
        }
        throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_FOUND);
    }

}
