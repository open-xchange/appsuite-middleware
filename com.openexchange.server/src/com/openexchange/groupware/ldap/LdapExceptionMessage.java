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

package com.openexchange.groupware.ldap;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link LdapExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class LdapExceptionMessage implements LocalizableStrings {
    
    public final static String CACHE_PROBLEM_DISPLAY = "Problem putting/removing an object into/from the cache.";
    
    public final static String HASH_ALGORITHM_DISPLAY = "Hash algorithm \"%s\" could not be found.";
    
    public final static String UNSUPPORTED_ENCODING_DISPLAY = "Encoding \"%s\" cannot be used.";
    
    public final static String RESOURCEGROUP_NOT_FOUND_DISPLAY = "No resource group found for identifier \"%1$d\".";

    public final static String RESOURCEGROUP_CONFLICT_DISPLAY = "Found resource groups with the same identifier \"%1$d\".";

    public final static String RESOURCE_NOT_FOUND_DISPLAY = "No resource found with identifier \"%1$d\".";

    public final static String RESOURCE_CONFLICT_DISPLAY = "Found resource(s) with same identifier \"%1$s.\".";
    
    public final static String NO_USER_BY_MAIL_DISPLAY = "Cannot find user with E-Mail \"%s\".";
    
    public final static String USER_NOT_FOUND_DISPLAY = "Cannot find user with identifier \"%1$s\" in context %2$d.";
    
    public final static String GROUP_NOT_FOUND_DISPLAY = "Cannot find group with identifier \"%1$s\" in context %2$d.";

    /**
     * Initializes a new {@link LdapExceptionMessage}.
     */
    private LdapExceptionMessage() {
        super();
    }

}
