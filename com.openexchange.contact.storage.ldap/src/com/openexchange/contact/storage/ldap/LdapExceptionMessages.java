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

package com.openexchange.contact.storage.ldap;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link LdapExceptionMessages}
 *
 * Exception messages for {@link OXException} that must be translated.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class LdapExceptionMessages implements LocalizableStrings {

    public final static String DELETE_NOT_POSSIBLE_MSG = "LDAP contacts cannot be deleted.";
    public final static String INSERT_NOT_POSSIBLE_MSG = "Contacts cannot be inserted in LDAP.";
    public final static String INITIAL_LDAP_ERROR_MSG = "Error while trying to create connection to LDAP server.";
    public final static String TOO_MANY_USER_RESULTS_MSG = "The LDAP search for the user contains too many results.";
    public final static String NO_USER_RESULTS_MSG = "The LDAP search for the user object \"%s\" gave no results.";
    public final static String SEARCH_IN_DLISTS_NOT_SUPPORTED = "The LDAP search in distribution lists for specific attributes is not supported."; 
    /**
     * Prevent instantiation.
     */
    private LdapExceptionMessages() {
        super();
    }
}
