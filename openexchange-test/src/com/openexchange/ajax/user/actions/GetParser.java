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

package com.openexchange.ajax.user.actions;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.user.UserImpl4Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.parser.UserParser;

public class GetParser extends AbstractAJAXParser<GetResponse> {

    private final TimeZone timeZone;

    private final int userId;

    GetParser(boolean failOnError, int userId, TimeZone timeZone) {
        super(failOnError);
        this.userId = userId;
        this.timeZone = timeZone;
    }

    @Override
    protected GetResponse createResponse(final Response response) {
        return new GetResponse(response);
    }

    @Override
    public GetResponse parse(final String body) throws JSONException {
        final GetResponse retval = super.parse(body);
        if (false == retval.hasError() && null != retval.getData()) {
            try {
                JSONObject data = (JSONObject) retval.getData();
                Contact contact = UserParser.parseUserContact(data, timeZone);
                retval.setContact(contact);
                final UserImpl4Test user = new UserImpl4Test();
                user.setId(userId);
                user.setDisplayName(contact.getDisplayName());
                user.setGivenName(contact.getGivenName());
                user.setSurname(contact.getSurName());
                user.setMail(contact.getEmail1());
                JSONArray jGroups = data.optJSONArray("groups");
                if (jGroups != null) {
                    int[] groups = new int[jGroups.length()];
                    for (int i = 0; i < jGroups.length(); i++) {
                        groups[i] = jGroups.getInt(i);
                    }
                    user.setGroups(groups);
                }
                user.setGuestCreatedBy(data.optInt("guest_created_by", 0));
                if (data.has(UserField.IMAGE1_URL.getName())) {
                    retval.setImageUrl(data.getString(UserField.IMAGE1_URL.getName()));
                }
                retval.setUser(user);
            } catch (OXException e) {
                throw new JSONException(e);
            }
        }
        return retval;
    }
}
