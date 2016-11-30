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

package com.openexchange.ajax.fields;

import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ResponseFields {

    /**
     * Name of the JSON attribute containing the response data.
     */
    public static final String DATA = "data";

    /**
     * Name of the JSON attribute containing the response's warnings.
     */
    public static final String WARNINGS = "warnings";

    /**
     * Name of the JSON attribute containing the error message.
     */
    public static final String ERROR = "error";

    /**
     * Name of the JSON attribute containing the error categories.
     */
    public static final String ERROR_CATEGORIES = "categories";

    /**
     * <b>Deprecated</b>: Name of the JSON attribute containing the error category.
     */
    public static final String ERROR_CATEGORY = "category";

    /**
     * Name of the JSON attribute containing the error code.
     */
    public static final String ERROR_CODE = "code";

    /**
     * Name of the JSON attribute containing the unique error identifier.
     */
    public static final String ERROR_ID = "error_id";

    /**
     * Name of the JSON attribute containing the array of the error message attributes.
     */
    public static final String ERROR_PARAMS = "error_params";

    /**
     * Name of the JSON attribute containing the stacks of the error.
     */
    public static final String ERROR_STACK = "error_stack";

    /**
     * Name of the JSON attribute containing the rather technical error description.
     */
    public static final String ERROR_DESC = "error_desc";

    /**
     * Name of the JSON attribute containing the array of actual length of the truncated attributes.
     */
    public static final String LENGTHS = "lengths";

    /**
     * Name of the JSON attribute containing the array of maximum sizes for the truncated attributes.
     */
    public static final String MAX_SIZES = "max_sizes";

    /**
     * Name of the JSON attribute containing an array with JSON objects describing problematic attributes.
     */
    public static final String PROBLEMATIC = "problematic";

    /**
     * Name of the JSON attribute containing the timestamp of the most actual returned object.
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * Name of the JSON attribute containing the array of truncated attribute identifier.
     */
    public static final String TRUNCATED = "truncated";

    /**
     * Name of the JSON attribute containing the arguments' JSON object.
     */
    public static final String ARGUMENTS = "arguments";

    /**
     * Name of the JSON attribute providing the continuation UUID.
     */
    public static final String CONTINUATION = "continuation";

    /**
     * A set of reserved identifiers.
     */
    public static final Set<String> RESERVED_IDENTIFIERS = ImmutableSet.of(
        DATA,
        WARNINGS,
        ERROR,
        ERROR_CATEGORIES,
        ERROR_CODE,
        ERROR_ID,
        ERROR_PARAMS,
        ERROR_STACK,
        ERROR_DESC,
        LENGTHS,
        MAX_SIZES,
        PROBLEMATIC,
        TIMESTAMP,
        TRUNCATED);

    /**
     * Prevent instantiation.
     */
    private ResponseFields() {
        super();
    }

    public static final class TruncatedFields {

        public static final String ID = "id";

        public static final String MAX_SIZE = "max_size";

        public static final String LENGTH = "length";

        /**
         * Prevent instantiation.
         */
        private TruncatedFields() {
            super();
        }
    }

    public static final class ParsingFields {

        public static final String NAME = "name";

        /**
         * Prevent instantiation.
         */
        private ParsingFields() {
            super();
        }
    }
}
