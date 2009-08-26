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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.tools.sql.SearchStrings;

/**
 * This class should contain all the search logic for contacts. That logic is still located in {@link RdbContactSQLImpl} but partly it is
 * here.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@OXExceptionSource(
    classId=Classes.COM_OPENEXCHANGE_GROUPWARE_CONTACT_SEARCH,
    component=EnumComponent.CONTACT
)
public class Search {

    private static final ContactExceptionFactory EXCEPTIONS = new ContactExceptionFactory(Search.class);

    private Search() {
        super();
    }

    public static void checkPatternLength(ContactSearchObject searchData) throws ContactException {
        final int minimumSearchCharacters = getMinimumSearchCharacters();
        if (0 == minimumSearchCharacters) {
            return;
        }
        for (String pattern : new String[] { searchData.getPattern(), searchData.getDisplayName() }) {
            checkPatternLength(minimumSearchCharacters, pattern);
        }
    }

    private static int getMinimumSearchCharacters() throws ContactException {
        try {
            return ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        } catch (ConfigurationException e) {
            throw new ContactException(e);
        }
    }

    public static void checkPatternLength(String pattern) throws ContactException {
        final int minimumSearchCharacters = getMinimumSearchCharacters();
        if (0 == minimumSearchCharacters) {
            return;
        }
        checkPatternLength(minimumSearchCharacters, pattern);
    }

    @OXThrows(
        category = Category.USER_INPUT,
        desc = "The administrator configured a minimum length for a search pattern and the users pattern is shorter than this minimum.",
        exceptionId = 1,
        msg = "In order to accomplish the search, %1$d or more characters are required."
    )
    private static void checkPatternLength(int minimumSearchCharacters, String pattern) throws ContactException {
        if (null != pattern && SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw EXCEPTIONS.create(1, I(minimumSearchCharacters));
        }
    }
}
