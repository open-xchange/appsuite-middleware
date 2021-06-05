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

package com.openexchange.file.storage.owncloud.internal.permissions;

import static com.openexchange.java.Autoboxing.I;
import com.google.common.collect.BiMap;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.owncloud.OwnCloudEntityResolver;

/**
 * {@link SimpleOwnCloudEntityResolver}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class SimpleOwnCloudEntityResolver implements OwnCloudEntityResolver {

    private static final String PREFIX = "owncloud";
    private BiMap<String, Integer> groups;
    private BiMap<String, Integer> users;

    /**
     * Initializes a new {@link SimpleOwnCloudEntityResolver}.
     */
    public SimpleOwnCloudEntityResolver(BiMap<String, Integer> users, BiMap<String, Integer> groups) {
        super();
        this.users = users;
        this.groups = groups;
    }

    @Override
    public int ocEntity2OXEntity(String ocEntityId, boolean isGroup) throws OXException {
        if (isGroup) {
            if (groups.containsKey(ocEntityId)) {
                return groups.get(ocEntityId).intValue();
            }
            throw new OXException(1, "No group mapping found for owncloud group %s.", ocEntityId).setPrefix(PREFIX);
        }
        if (users.containsKey(ocEntityId)) {
            return users.get(ocEntityId).intValue();
        }
        throw new OXException(2, "No user mapping found for owncloud user %s.", ocEntityId).setPrefix(PREFIX);
    }

    @Override
    public String oxEntity2OCEntity(int oxEntityId, boolean isGroup) throws OXException {
        if (isGroup) {
            if (groups.containsValue(I(oxEntityId))) {
                return groups.inverse().get(I(oxEntityId));
            }
            throw new OXException(3, "No group mapping found for owncloud group %s.", I(oxEntityId)).setPrefix(PREFIX);
        }
        if (users.containsValue(I(oxEntityId))) {
            return users.inverse().get(I(oxEntityId));
        }
        throw new OXException(4, "No user mapping found for ox user %s.", I(oxEntityId)).setPrefix(PREFIX);
    }

}
