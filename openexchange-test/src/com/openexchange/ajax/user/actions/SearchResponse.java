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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.ajax.user.UserImpl4Test;
import com.openexchange.groupware.container.Contact;

/**
 * Stores the response of searched users.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SearchResponse extends CommonSearchResponse {

    private final int[] userImplAttributes;

    /**
     * @param response
     */
    public SearchResponse(final Response response, final int[] columns) {
        super(response);
        this.userImplAttributes = columns;
    }

    public UserImpl4Test[] getUser() {
        final List<UserImpl4Test> list = new ArrayList<UserImpl4Test>();
        for (final Object[] data : this) {
            final UserImpl4Test user = new UserImpl4Test();
            for (final int attribute : userImplAttributes) {
                final Object value = data[getColumnPos(attribute)];
                switch (attribute) {
                case Contact.INTERNAL_USERID:
                    if(JSONObject.NULL == value) {
                        break;
                    }
                    user.setId(((Integer) value).intValue());
                    break;
                case Contact.EMAIL1:
                    if(JSONObject.NULL == value) {
                        user.setMail(null);
                        break;
                    }
                    user.setMail((String) value);
                    break;
                case Contact.DISPLAY_NAME:
                    user.setDisplayName((value == JSONObject.NULL) ? null : (String) value);
                    break;
                case Contact.GIVEN_NAME:
                    user.setGivenName((value == JSONObject.NULL) ? null : (String) value);
                    break;
                case Contact.SUR_NAME:
                    user.setSurname((value == JSONObject.NULL) ? null : (String) value);
                    break;
                }
            }
            list.add(user);
        }
        return list.toArray(new UserImpl4Test[list.size()]);
    }
}
