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

package com.openexchange.dav.principals.users;

import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ResourceId;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.CalendarUserAddressSet;
import com.openexchange.dav.mixins.DisplayName;
import com.openexchange.dav.mixins.EmailAddressSet;
import com.openexchange.dav.mixins.FirstName;
import com.openexchange.dav.mixins.GroupMembership;
import com.openexchange.dav.mixins.LastName;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.RecordType;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.user.User;
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
        ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
        includeProperties(new PrincipalURL(user.getId(), CalendarUserType.INDIVIDUAL, configViewFactory), new AddressbookHomeSet(configViewFactory),
            new CalendarHomeSet(configViewFactory), new EmailAddressSet(user), new PrincipalCollectionSet(configViewFactory),
            new CalendarUserAddressSet(factory.getContext().getContextId(), user, configViewFactory),
            new DisplayName(user.getDisplayName()), new FirstName(user), new LastName(user),
            new com.openexchange.dav.mixins.CalendarUserType(CalendarUserType.INDIVIDUAL), new RecordType(RecordType.RECORD_TYPE_USER),
            new com.openexchange.dav.mixins.ResourceId(ResourceId.forUser(factory.getContext().getContextId(), user.getId())),
            new GroupMembership(user.getGroups(), configViewFactory)
        );
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
