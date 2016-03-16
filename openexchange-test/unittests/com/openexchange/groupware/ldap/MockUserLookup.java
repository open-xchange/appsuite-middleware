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

package com.openexchange.groupware.ldap;

import com.openexchange.exception.OXException;
import java.util.HashMap;
import java.util.Map;

/**
 * MockUserStorage for now contains some testing data relevant to the notification tests.
 * This can be exten_USded for other tests for testing in isolation.
 */
public class MockUserLookup {

    private final Map<Integer, User> users = new HashMap<Integer, User>();


    public User getUser(final int uid) throws OXException {
        if (!users.containsKey(uid)) {
            throw UserExceptionCode.USER_NOT_FOUND.create(uid);
        }
        return users.get(uid);
    }

    public User getUserByMail(final String mail) {
        for(final User user : users.values()) {
            final String cur_mail = user.getMail();
            if(cur_mail != null && cur_mail.equalsIgnoreCase(mail)) {
                return user;
            }
        }
        return null;
    }

    public MockUserLookup() {

        final String tz = "Europe/Berlin";

        int i = 0;
        MockUser user = new MockUser(++i);
        user.setDisplayName("The Mailadmin");
        user.setPreferredLanguage("en_US");
        user.setTimeZone(tz);
        user.setMail("mailadmin@test.invalid");
        user.setGroups(new int[]{1});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 1");
        user.setPreferredLanguage("en_US");
        user.setTimeZone(tz);
        user.setMail("user1@test.invalid");
        user.setGroups(new int[]{1,4});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 2");
        user.setPreferredLanguage("de_DE");
        user.setTimeZone(tz);
        user.setMail("user2@test.invalid");
        user.setGroups(new int[]{1,2});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 3");
        user.setPreferredLanguage("en_US");
        user.setTimeZone("Pacific/Samoa");
        user.setMail("user3@test.invalid");
        user.setGroups(new int[]{1,4});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 4");
        user.setPreferredLanguage("de_DE");
        user.setTimeZone(tz);
        user.setMail("user4@test.invalid");
        user.setGroups(new int[]{1,2,3});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 5");
        user.setPreferredLanguage("en_US");
        user.setTimeZone(tz);
        user.setMail("user5@test.invalid");
        user.setGroups(new int[]{1,3,4});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 6");
        user.setPreferredLanguage("de_DE");
        user.setTimeZone(tz);
        user.setMail("user6@test.invalid");
        user.setGroups(new int[]{1,2});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 7");
        user.setPreferredLanguage("en_US");
        user.setTimeZone(tz);
        user.setMail("user7@test.invalid");
        user.setGroups(new int[]{1,4});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 8");
        user.setPreferredLanguage("de_DE");
        user.setTimeZone(tz);
        user.setMail("user8@test.invalid");
        user.setGroups(new int[]{1,2,3});
        addUser(user);

        user = new MockUser(++i);
        user.setDisplayName("User 9");
        user.setPreferredLanguage("fr");
        user.setTimeZone(tz);
        user.setMail("user9@test.invalid");
        user.setGroups(new int[]{1,4});
        addUser(user);

        user = new MockUser(2000);
        user.setDisplayName("Session User");
        user.setPreferredLanguage("en_US");
        user.setTimeZone(tz);
        user.setMail("primary@test");
        user.setGroups(new int[]{1,4});
        addUser(user);
    }

    private void addUser(final User user) {
        users.put(user.getId(),user);
    }

}
