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

package com.openexchange.ajax.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.junit.Assert;
import org.xml.sax.SAXException;
import com.openexchange.ajax.UserTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.SearchRequest;
import com.openexchange.ajax.user.actions.SearchResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class UserTools extends Assert {

    /**
     * Prevent instantiation.
     */
    public UserTools() {
        super();
    }

    public static UserImpl4Test[] searchUser(AJAXClient client, String searchpattern) throws OXException, IOException, SAXException, JSONException {
        final ContactSearchObject search = new ContactSearchObject();
        search.setPattern(searchpattern);
        search.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        final SearchRequest request = new SearchRequest(search, UserTest.CONTACT_FIELDS);
        final SearchResponse response = Executor.execute(client, request);
        assertNotNull("timestamp", response.getTimestamp());
        return response.getUser();
    }

        public static Contact getUserContact(AJAXClient client, int userId) throws OXException, IOException, SAXException, JSONException {
            GetRequest request = new GetRequest(userId, client.getValues().getTimeZone());
            GetResponse response = client.execute(request);
            return response.getContact();
        }

    public static User getUser(AJAXClient client, int userId) throws OXException, IOException, JSONException {
        return new UserResolver(client).getUser(userId);
    }

    public static User[] listUser(AJAXClient client, int[] ids) throws OXException, IOException, JSONException {
        List<User> users = new ArrayList<>();
        for (int id : ids) {
            users.add(getUser(client, id));
        }
        return users.toArray(new User[users.size()]);
    }
}
