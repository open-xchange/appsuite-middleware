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

package com.openexchange.ajax.chat.conversation;

import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link JSONChatUser} - Represents a chat user rendered with JSON.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONChatUser {

    /**
     * Parses given JSON to a {@link JSONChatUser}
     * 
     * @param jsonPresence The JSON chat user data
     * @return The parsed {@link JSONChatUser}
     * @throws JSONException If a JSON error occurs
     */
    public static JSONChatUser valueOf(final JSONObject jsonChatUser, final TimeZone timeZone) throws JSONException {
        final JSONChatUser ret = new JSONChatUser();
        ret.setId(jsonChatUser.getString("id"));
        if (jsonChatUser.hasAndNotNull("name")) {
            ret.setName(jsonChatUser.getString("name"));
        }
        final JSONObject optJsonPresence = jsonChatUser.optJSONObject("presence");
        if (null != optJsonPresence) {
            ret.setPresence(JSONPresence.valueOf(optJsonPresence, timeZone));
        }
        return ret;
    }

    private String id;

    private String name;

    private JSONPresence presence;

    /**
     * Initializes a new {@link JSONChatUser}.
     */
    public JSONChatUser() {
        super();
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
     * Gets the name
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     * 
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the presence
     * 
     * @return The presence
     */
    public JSONPresence getPresence() {
        return presence;
    }

    /**
     * Sets the presence
     * 
     * @param presence The presence to set
     */
    public void setPresence(final JSONPresence presence) {
        this.presence = presence;
    }

}
