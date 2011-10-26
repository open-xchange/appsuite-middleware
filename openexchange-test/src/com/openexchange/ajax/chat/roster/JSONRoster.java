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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.ajax.chat.roster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.chat.conversation.JSONChatUser;

/**
 * {@link JSONRoster} - Represents a chat roster rendered with JSON.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONRoster {

    /**
     * Parses given JSON to a {@link JSONRoster}
     * 
     * @param jsonPresence The JSON roster data
     * @return The parsed {@link JSONRoster}
     * @throws JSONException If a JSON error occurs
     */
    public static JSONRoster valueOf(final JSONObject jsonRoster, final TimeZone timeZone) throws JSONException {
        final JSONRoster ret = new JSONRoster();
        ret.setId(jsonRoster.getString("id"));
        final JSONArray members = jsonRoster.optJSONArray("members");
        if (null != members) {
            final int length = members.length();
            for (int i = 0; i < length; i++) {
                ret.addMember(JSONChatUser.valueOf(members.getJSONObject(i), timeZone));
            }
        }
        return ret;
    }

    private String id;

    private final List<JSONChatUser> members;

    /**
     * Initializes a new {@link JSONRoster}.
     */
    public JSONRoster() {
        super();
        members = new ArrayList<JSONChatUser>(8);
    }

    /**
     * Gets the id
     * 
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id
     * 
     * @param id The id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the members
     * 
     * @return The members
     */
    public List<JSONChatUser> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * Adds given member.
     * 
     * @param user The member to add
     */
    public void addMember(final JSONChatUser user) {
        members.add(user);
    }

}
