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

package com.openexchange.dav.principals.users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.principals.PrincipalFactory;
import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.DefaultPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link UserPrincipalCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class UserPrincipalCollection extends DAVCollection {

    public static final String NAME = "users";

    private final PrincipalFactory factory;

    /**
     * Initializes a new {@link UserPrincipalCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     */
    public UserPrincipalCollection(PrincipalFactory factory, WebdavPath url) {
        super(factory, url);
        this.factory = factory;
        includeProperties(new CurrentUserPrincipal(factory));
    }

    @Override
    public Permission[] getPermissions() {
        return new Permission[] {
            new DefaultPermission(GroupStorage.GROUP_ZERO_IDENTIFIER, true, Permissions.createPermissionBits(
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
            User[] users = factory.requireService(UserService.class).getUser(factory.getContext());
            List<WebdavResource> children = new ArrayList<WebdavResource>(users.length);
            for (User user : users) {
                children.add(createUserResource(user));
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
    public String getDisplayName() throws WebdavProtocolException {
        return "Users";
    }

    @Override
    public UserPrincipalResource getChild(String name) throws WebdavProtocolException {
        if (Strings.isEmpty(name)) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * lookup by principal id
         */
        int id;
        try {
            id = Integer.parseInt(name);
        } catch (NumberFormatException ignored) {
            // no numerical resource name, lookup by login name for backwards compatibility, too
            try {
                id = factory.requireService(UserService.class).getUserId(name, factory.getContext());
            } catch (OXException e) {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_NOT_FOUND);
            }
        }
        /*
         * get user
         */
        try {
            return createUserResource(factory.requireService(UserService.class).getUser(id, factory.getContext()));
        } catch (OXException e) {
            throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private UserPrincipalResource createUserResource(User user) {
        return new UserPrincipalResource(factory, user, constructPathForChildResource(String.valueOf(user.getId())));
    }

}
