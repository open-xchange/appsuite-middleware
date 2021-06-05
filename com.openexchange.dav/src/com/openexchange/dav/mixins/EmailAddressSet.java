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

package com.openexchange.dav.mixins;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link EmailAddressSet}
 *
 * {http://calendarserver.org/ns/}email-address-set
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EmailAddressSet extends SingleXMLPropertyMixin {

    private final User user;
    private final Resource resource;

    /**
     * Initializes a new {@link EmailAddressSet}.
     *
     * @param user The user
     */
    public EmailAddressSet(User user) {
        super("http://calendarserver.org/ns/", "email-address-set");
        this.user = user;
        this.resource = null;
    }

    /**
     * Initializes a new {@link EmailAddressSet}.
     *
     * @param resource The resource
     */
    public EmailAddressSet(Resource resource) {
        super("http://calendarserver.org/ns/", "email-address-set");
        this.user = null;
        this.resource = resource;
    }

    @Override
    protected String getValue() {
        List<String> addresses = new ArrayList<String>();
        if (null != user) {
            if (Strings.isNotEmpty(user.getMail())) {
                addresses.add(user.getMail());
            }
            if (null != user.getAliases()) {
                for (String alias : user.getAliases()) {
                    if (Strings.isNotEmpty(alias) && false == addresses.contains(alias)) {
                        addresses.add(alias);
                    }
                }
            }
        } else if (null != resource) {
            if (Strings.isNotEmpty(resource.getMail())) {
                addresses.add(resource.getMail());
            }
        }
        if (addresses.isEmpty()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String address : addresses) {
            stringBuilder.append("<D:email-address>").append(address).append("</D:email-address>");
        }
        return stringBuilder.toString();
    }

}
