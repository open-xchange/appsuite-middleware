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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link LdapExceptionMessage}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class LdapExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link LdapExceptionMessage}.
     */
    private LdapExceptionMessage() {
        super();
    }

    /**
     * A property from the ldap.properties file is missing.
     */
    public final static String PROPERTY_MISSING_MSG = "Cannot find property %s.";

    /**
     * A problem with distinguished names occurred.
     */
    public final static String DN_PROBLEM_MSG = "Cannot build distinguished name from %s.";

    /**
     * Class can not be found.
     */
    public final static String CLASS_NOT_FOUND_MSG = "Class %s can not be loaded.";

    /**
     * An implementation can not be instantiated.
     */
    public final static String INSTANTIATION_PROBLEM_MSG = "Cannot instantiate class %s.";

    /**
     * A database connection Cannot be obtained.
     */
    public final static String NO_CONNECTION_MSG = "Cannot get database connection.";

    /**
     * SQL Problem: "%s".
     */
    public final static String SQL_ERROR_MSG = "SQL Problem: \"%s\"";

    /**
     * Problem putting an object into the cache.
     */
    public final static String CACHE_PROBLEM_MSG = "Problem putting/removing an object into/from the cache.";

    /**
     * Hash algorithm %s isn't found.
     */
    public final static String HASH_ALGORITHM_MSG = "Hash algorithm %s isn't found.";

    /**
     * Encoding %s cannot be used.
     */
    public final static String UNSUPPORTED_ENCODING_MSG = "Encoding %s cannot be used.";

    /**
     * Cannot find resource group with identifier %d.
     */
    public final static String RESOURCEGROUP_NOT_FOUND_MSG = "Cannot find resource group with identifier %d.";

    /**
     * Found resource groups with same identifier %d.
     */
    public final static String RESOURCEGROUP_CONFLICT_MSG = "Found resource groups with same identifier %d.";

    /**
     * Cannot find resource with identifier %d.
     */
    public final static String RESOURCE_NOT_FOUND_MSG = "Cannot find resource with identifier %d.";

    /**
     * Found resources with same identifier %d.
     */
    public final static String RESOURCE_CONFLICT_MSG = "Found resources with same identifier %d.";

    /**
     * Cannot find user with email %s.
     */
    public final static String NO_USER_BY_MAIL_MSG = "Cannot find user with email %s.";

    /**
     * Cannot find user with identifier %1$s in context %2$d.
     */
    public final static String USER_NOT_FOUND_MSG = "Cannot find user with identifier %1$s in context %2$d.";

    /**
     * Cannot find group with identifier %1$s in context %2$d.
     */
    public final static String GROUP_NOT_FOUND_MSG = "Cannot find group with identifier %1$s in context %2$d.";

    /**
     * Unexpected error: %1$s
     */
    public final static String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

}
