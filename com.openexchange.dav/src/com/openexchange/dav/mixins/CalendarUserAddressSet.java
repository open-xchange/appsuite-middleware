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
import java.util.Arrays;
import java.util.List;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.group.Group;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarUserAddressSet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarUserAddressSet extends SingleXMLPropertyMixin {

    private final List<String> addresses;

    /**
     * Initializes a new {@link CalendarUserAddressSet}.
     *
     * @param contextID The context identifier
     * @param user The user
     * @param configViewFactory The configuration view
     */
    public CalendarUserAddressSet(int contextID, User user, ConfigViewFactory configViewFactory) {
        this(getAddresses(contextID, user, configViewFactory));
    }

    /**
     * Initializes a new {@link CalendarUserAddressSet}.
     *
     * @param contextID The context identifier
     * @param group The group
     * @param configViewFactory The configuration view
     */
    public CalendarUserAddressSet(int contextID, Group group, ConfigViewFactory configViewFactory) {
        this(Arrays.asList(PrincipalURL.forGroup(group.getIdentifier(), configViewFactory), ResourceId.forGroup(contextID, group.getIdentifier())));
    }

    /**
     * Initializes a new {@link CalendarUserAddressSet}.
     *
     * @param contextID The context identifier
     * @param resource The resource
     * @param configViewFactory The configuration view
     */
    public CalendarUserAddressSet(int contextID, Resource resource, ConfigViewFactory configViewFactory) {
        this(getAddresses(contextID, resource, configViewFactory));
    }

    private static List<String> getAddresses(int contextID, Resource resource, ConfigViewFactory configViewFactory) {
        List<String> addresses = new ArrayList<String>(3);
        if (Strings.isNotEmpty(resource.getMail())) {
            addresses.add(CalendarUtils.getURI(resource.getMail()));
        }
        addresses.add(PrincipalURL.forResource(resource.getIdentifier(), configViewFactory));
        addresses.add(ResourceId.forResource(contextID, resource.getIdentifier()));
        return addresses;
    }

    private static List<String> getAddresses(int contextID, User user, ConfigViewFactory configViewFactory) {
        List<String> addresses = new ArrayList<String>(3);
        if (Strings.isNotEmpty(user.getMail())) {
            addresses.add(CalendarUtils.getURI(user.getMail()));
        }
        if (null != user.getAliases()) {
            for (String alias : user.getAliases()) {
                if (Strings.isNotEmpty(alias)) {
                    String address = CalendarUtils.getURI(alias);
                    if (false == addresses.contains(address)) {
                        addresses.add(address);
                    }
                }
            }
        }
        addresses.add(PrincipalURL.forUser(user.getId(), configViewFactory));
        addresses.add(ResourceId.forUser(contextID, user.getId()));
        return addresses;
    }

    /**
     * Initializes a new {@link CalendarUserAddressSet}.
     *
     * @param addresses The possible calendar user address URLs
     */
    private CalendarUserAddressSet(List<String> addresses) {
        super(DAVProtocol.CAL_NS.getURI(), "calendar-user-address-set");
        this.addresses = addresses;
    }

    @Override
    protected String getValue() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String address : addresses) {
            stringBuilder.append("<D:href>").append(address).append("</D:href>");
        }
        return stringBuilder.toString();
    }

}
