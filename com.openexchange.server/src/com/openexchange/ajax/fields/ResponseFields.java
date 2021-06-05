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
     * Name of the JSON attribute containing an array with JSON objects describing exception details like sub exceptions.
     */
    public static final String DETAILS = "details";

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
