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

package com.openexchange.halo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;

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
            if (false == Strings.isEmpty(s)) {
                addresses.add(s);
            }
        }
        if (contact.containsEmail2()) {
            String s = contact.getEmail2();
            if (false == Strings.isEmpty(s)) {
                addresses.add(s);
            }
        }
        if (contact.containsEmail3()) {
            String s = contact.getEmail3();
            if (false == Strings.isEmpty(s)) {
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
