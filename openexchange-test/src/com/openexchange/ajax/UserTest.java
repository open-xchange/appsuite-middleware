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

package com.openexchange.ajax;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.user.UserTools;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;

public class UserTest extends AbstractAJAXSession {

    public final static int[] CONTACT_FIELDS = { DataObject.OBJECT_ID, Contact.INTERNAL_USERID, Contact.EMAIL1, Contact.GIVEN_NAME, Contact.SUR_NAME, Contact.DISPLAY_NAME
    };

    @Test
    public void testSearch() throws Exception {
        final com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);
    }

    @Test
    public void testList() throws Exception {
        com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);

        final int[] id = new int[users.length];
        for (int a = 0; a < id.length; a++) {
            id[a] = users[a].getId();
        }

        users = UserTools.listUser(getClient(), id);
        assertTrue("user array size > 0", users.length > 0);
    }

    @Test
    public void testSearchUsers() throws Exception {
        final com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);
    }

    @Test
    public void testGet() throws Exception {
        final com.openexchange.groupware.ldap.User users[] = UserTools.searchUser(getClient(), "*");
        assertTrue("user array size > 0", users.length > 0);
        UserTools.getUser(getClient(), users[0].getId());
    }
}
