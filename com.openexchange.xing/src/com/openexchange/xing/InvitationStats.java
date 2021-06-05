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
        } catch (JSONException e) {
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
