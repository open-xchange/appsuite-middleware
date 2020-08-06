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
 *    trademarks of the OX Software GmbH. group of companies.
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
