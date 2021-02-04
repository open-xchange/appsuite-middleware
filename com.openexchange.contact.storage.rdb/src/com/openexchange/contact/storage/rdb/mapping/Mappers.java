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
