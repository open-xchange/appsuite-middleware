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

package com.openexchange.find;

import com.openexchange.i18n.LocalizableStrings;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class FindExceptionMessages implements LocalizableStrings {

    private FindExceptionMessages() {
        super();
    }

    // The service you requested is currently not available. Please try again later.
    public static final String SERVICE_NOT_AVAILABLE = "The service you requested is currently not available. Please try again later.";

    // A filter for field '%1$s' is missing but is required to search in module %2$s.
    public static final String MISSING_SEARCH_FILTER = "A filter for field '%1$s' is missing but is required to search in module %2$s.";

    // A search filter did not contain a field to filter on.
    public static final String INVALID_FILTER_NO_FIELDS = "A search filter did not contain a field to filter on.";

    // A search filter did not contain a query to search for.
    public static final String INVALID_FILTER_NO_QUERIES = "A search filter did not contain a query to search for.";

    // A search filter contained an unknown field: '%1$s'.
    public static final String INVALID_FILTER_UNKNOWN_FIELD = "A search filter contained an unknown field: '%1$s'.";

    // In order to accomplish the search, %1$d or more characters are required.
    public static final String QUERY_TOO_SHORT = "In order to accomplish the search, %1$d or more characters are required.";

    // The facet \"%1$s\" is mandatory and has to be set.
    public static final String MISSING_MANDATORY_FACET = "The facet \"%1$s\" is mandatory and has to be set.";

    // Could not parse \"%1$s\".
    public static final String PARSING_ERROR = "Could not parse \"%1$s\".";

    // The folder id \"%1$s\" is invalid for module \"%2$s\".
    public static final String INVALID_FOLDER_ID = "The folder id \"%1$s\" is invalid for module \"%2$s\".";

    // The folder type \"%1$s\" is not supported.
    public static final String INVALID_FOLDER_TYPE = "The folder type \"%1$s\" is not supported.";

    // The facet types \"%1$s\" and \"%2$s\" conflict with each other and must not be used within one request.
    public static final String FACET_CONFLICT = "The facet types \"%1$s\" and \"%2$s\" conflict with each other and must not be used within one request.";

    // You are not allowed to search in module \"%1$s\".
    public static final String MODULE_DISABLED = "You are not allowed to search in module \"%1$s\".";

    // The query yielded too many results to be displayed. Please refine your search options and try again.
    public static final String TOO_MANY_RESULTS = "The query yielded too many results to be displayed. Please refine your search options and try again.";

}
