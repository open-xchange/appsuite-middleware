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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.tools.sql.SearchStrings;

/**
 * This class should contain all the search logic for contacts. That logic is still located in {@link RdbContactSQLImpl} but partly it is
 * here.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Search {

    private Search() {
        super();
    }

    public static void checkPatternLength(final ContactSearchObject searchData) throws OXException {
        final int minimumSearchCharacters = getMinimumSearchCharacters();
        if (0 == minimumSearchCharacters) {
            return;
        }
        for (final String pattern : new String[] {
            searchData.getPattern(), searchData.getDisplayName(), searchData.getEmail1(), searchData.getEmail2(), searchData.getEmail3(),
            searchData.getGivenName(), searchData.getSurname() }) {
            checkPatternLength(minimumSearchCharacters, pattern);
        }
    }

    private static int getMinimumSearchCharacters() throws OXException {
        return ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
    }

    public static void checkPatternLength(final String pattern) throws OXException {
        final int minimumSearchCharacters = getMinimumSearchCharacters();
        if (0 == minimumSearchCharacters) {
            return;
        }
        checkPatternLength(minimumSearchCharacters, pattern);
    }

    private static void checkPatternLength(final int minimumSearchCharacters, final String pattern) throws OXException {
        if (null != pattern && SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw ContactExceptionCodes.TOO_FEW_SEARCH_CHARS.create(I(minimumSearchCharacters));
        }
    }
}
