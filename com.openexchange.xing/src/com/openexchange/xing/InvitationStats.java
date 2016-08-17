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

package com.openexchange.xing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;

/**
 * {@link InvitationStats} - An invitation statistic element.
 * <p>
 *
 * <pre>
 *     "total_addresses": 7,
 *     "invitations_sent": 3,
 *     "already_invited": [
 *       "kven.sever@example.net"
 *     ],
 *     "already_member": [{
 *       "id": "666666_abcdef",
 *       "email": "sark.midt@xing.com",
 *       "display_name": "Sark Midt"
 *     }, {
 *       "id": "12345_abcdef",
 *       "email": "kennart.loopmann@xing.com",
 *       "display_name": "Kennart Loopmann"
 *     }],
 *     "invalid_addresses": [
 *       "@example.f"
 *     ]
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InvitationStats {

    private final int totalAddresses;
    private final int invitationsSent;
    private final List<String> alreadyInvited;
    private final List<User> alreadyMember;
    private final List<String> invalidAddresses;

    /**
     * Initializes a new {@link InvitationStats}.
     *
     * @throws XingException If initialization fails
     */
    protected InvitationStats(final JSONObject jInvitationStats) throws XingException {
        super();
        try {
            totalAddresses = jInvitationStats.optInt("total_addresses", 0);
            invitationsSent = jInvitationStats.optInt("invitations_sent", 0);

            {
                final JSONArray jAlreadyInvited = jInvitationStats.optJSONArray("already_invited");
                if (null == jAlreadyInvited) {
                    alreadyInvited = Collections.emptyList();
                } else {
                    final int length = jAlreadyInvited.length();
                    final List<String> l = new ArrayList<String>(length);
                    for (int i = 0; i < length; i++) {
                        l.add(jAlreadyInvited.getString(i));
                    }
                    alreadyInvited = l;
                }
            }

            {
                final JSONArray jAlreadyMember = jInvitationStats.optJSONArray("already_member");
                if (null == jAlreadyMember) {
                    alreadyMember = Collections.emptyList();
                } else {
                    final int length = jAlreadyMember.length();
                    final List<User> l = new ArrayList<User>(length);
                    for (int i = 0; i < length; i++) {
                        l.add(new User(jAlreadyMember.getJSONObject(i)));
                    }
                    alreadyMember = l;
                }
            }

            {
                final JSONArray jInvalidAddresses = jInvitationStats.optJSONArray("invalid_addresses");
                if (null == jInvalidAddresses) {
                    invalidAddresses = Collections.emptyList();
                } else {
                    final int length = jInvalidAddresses.length();
                    final List<String> l = new ArrayList<String>(length);
                    for (int i = 0; i < length; i++) {
                        l.add(jInvalidAddresses.getString(i));
                    }
                    invalidAddresses = l;
                }
            }
        } catch (final JSONException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the total addresses count
     *
     * @return The total addresses count
     */
    public int getTotalAddresses() {
        return totalAddresses;
    }

    /**
     * Gets the invitations sent count
     *
     * @return The invitations sent count
     */
    public int getInvitationsSent() {
        return invitationsSent;
    }

    /**
     * Gets the already invited addresses
     *
     * @return The already invited addresses
     */
    public List<String> getAlreadyInvited() {
        return alreadyInvited;
    }

    /**
     * Gets the listing of those addresses that are already members
     *
     * @return The listing of those addresses that are already members
     */
    public List<User> getAlreadyMember() {
        return alreadyMember;
    }

    /**
     * Gets the invalid addresses
     *
     * @return The invalid addresses
     */
    public List<String> getInvalidAddresses() {
        return invalidAddresses;
    }

}
