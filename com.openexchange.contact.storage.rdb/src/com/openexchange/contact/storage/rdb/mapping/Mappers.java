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

package com.openexchange.contact.storage.rdb.mapping;

import java.util.List;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.internal.DistListMember;
import com.openexchange.groupware.tools.mappings.common.AbstractCollectionUpdate;

/**
 * {@link Mappers} - Provides static access to mappings.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Mappers {

    /**
     * The mappings for contacts.
     */
    public static final ContactMapper CONTACT = new ContactMapper();

    /**
     * The mappings for distribution list members.
     */
    public static final DistListMapper DISTLIST = new DistListMapper();

    private Mappers() {
        // prevent instantiation
    }

    /**
     * Initializes a new DistListMember collection update based on the supplied original and updated lists.
     *
     * @param original The original DistListMember list
     * @param update The updated DistListMember list
     * @return An collection update
     */
    public static AbstractCollectionUpdate<DistListMember, DistListMemberField> getDistListUpdate(List<DistListMember> original, List<DistListMember> update) {
        return new AbstractCollectionUpdate<DistListMember, DistListMemberField>(Mappers.DISTLIST, original, update) {

            @Override
            protected boolean matches(DistListMember item1, DistListMember item2) {
                if (item1.containsUuid() && item2.containsUuid()) {
                    return item1.getUuid().equals(item2.getUuid());
                }
                if (item1.containsContactUid() && item2.containsContactUid()) {
                    return item1.getContactUid().equals(item2.getContactUid());
                }
                return item1.equals(item2);
            }
        };

    }
}
