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

package com.openexchange.dovecot.doveadm.client.internal;

import java.io.InputStream;
import org.json.JSONValue;

/**
 * {@link ResultType} - Specifies the expected type of the returned result.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v1.0.0
 */
public interface ResultType<R> {

    /**
     * Gets the class of the result type
     *
     * @return The class
     */
    Class<? extends R> getType();

    // -----------------------------------------------------------------------------------------------------------

    /** No expected result */
    public static final ResultType<Void> VOID = new ResultType<Void>() {

        @Override
        public Class<? extends Void> getType() {
            return null;
        }
    };

    /** An input stream is the expected result */
    public static final ResultType<InputStream> INPUT_STREAM = new ResultType<InputStream>() {

        @Override
        public Class<? extends InputStream> getType() {
            return InputStream.class;
        }
    };

    /** A JSON array is the expected result */
    public static final ResultType<JSONValue> JSON = new ResultType<JSONValue>() {

        @Override
        public Class<? extends JSONValue> getType() {
            return JSONValue.class;
        }
    };
}