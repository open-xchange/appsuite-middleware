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

import com.openexchange.dav.CUType;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.CalendarUserAddressSet;
import com.openexchange.dav.mixins.CalendarUserType;
import com.openexchange.dav.mixins.DisplayName;
import com.openexchange.dav.mixins.EmailAddressSet;
import com.openexchange.dav.mixins.FirstName;
import com.openexchange.dav.mixins.GroupMembership;
import com.openexchange.dav.mixins.LastName;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.RecordType;
import com.openexchange.dav.mixins.ResourceId;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link UserPrincipalResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class UserPrincipalResource extends DAVResource {

    private final User user;

    /**
     * Initializes a new {@link UserPrincipalResource}.
     *
     * @param factory The factory
     * @param user The user
     * @param url The WebDAV path of the resource
     */
    public UserPrincipalResource(DAVFactory factory, User user, WebdavPath url) {
        super(factory, url);
        this.user = user;
        includeProperties(new PrincipalURL(user.getId(), CUType.INDIVIDUAL), new AddressbookHomeSet(),
            new CalendarHomeSet(), new EmailAddressSet(user), new PrincipalCollectionSet(),
            new CalendarUserAddressSet(factory.getContext().getContextId(), user),
            new DisplayName(user.getDisplayName()), new FirstName(user), new LastName(user),
            new CalendarUserType(CUType.INDIVIDUAL), new RecordType(RecordType.RECORD_TYPE_USER),
            new ResourceId(factory.getContext().getContextId(), user.getId(), CUType.INDIVIDUAL),
            new GroupMembership(user.getGroups())
        );
    }

    /**
     * Initializes a new {@link UserPrincipalResource}.
     *
     * @param factory The factory
     * @param user The user
     * @param url The WebDAV path of the resource
     */
    public UserPrincipalResource(DAVFactory factory, User user) {
        this(factory, user, new WebdavPath("principals", "users", String.valueOf(user.getId())));
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        return"<D:resourcetype><D:principal /></D:resourcetype>";
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return user.getDisplayName();
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        return "http://www.open-xchange.com/webdav/users/" + user.getId();
    }

}
