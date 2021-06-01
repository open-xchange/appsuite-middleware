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

package com.openexchange.halo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

public abstract class AbstractContactHalo implements HaloContactDataSource {

    /**
     * Initializes a new {@link AbstractContactHalo}.
     */
    protected AbstractContactHalo() {
        super();
    }

    protected boolean isUserThemselves(final User user, final List<String> addresses) {
        final Set<String> ownAddresses = new HashSet<String>(8);
        for (final String alias : user.getAliases()) {
            if (!com.openexchange.java.Strings.isEmpty(alias)) {
                ownAddresses.add(com.openexchange.java.Strings.toLowerCase(alias));
            }
        }
        ownAddresses.add(com.openexchange.java.Strings.toLowerCase(user.getMail()));
        for (final String requested : addresses) {
            if (!ownAddresses.contains(com.openexchange.java.Strings.toLowerCase(requested))) {
                return false;
            }
        }
        return true;
    }

    protected List<String> getEMailAddresses(final Contact contact) {
        final Set<String> addresses = new LinkedHashSet<String>(8);
        if (contact.containsEmail1()) {
            String s = contact.getEmail1();
            if (Strings.isNotEmpty(s)) {
                addresses.add(s);
            }
        }
        if (contact.containsEmail2()) {
            String s = contact.getEmail2();
            if (Strings.isNotEmpty(s)) {
                addresses.add(s);
            }
        }
        if (contact.containsEmail3()) {
            String s = contact.getEmail3();
            if (Strings.isNotEmpty(s)) {
                addresses.add(s);
            }
        }
        return new ArrayList<String>(addresses);
    }

    @Override
    public boolean isAvailable(final ServerSession session) throws OXException {
        return true;
    }
}
