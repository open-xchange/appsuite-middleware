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

/**
 * JSON object attribute name definitions.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface DataFields {

    public static final String ID = "id";

    public static final String CREATED_BY = "created_by";

    public static final String MODIFIED_BY = "modified_by";

    public static final String CREATION_DATE = "creation_date";

    public static final String LAST_MODIFIED = "last_modified";

    /**
     * UTC last modified attribute. Synchronization clients use this to
     * implement a proper working synchronization independent from the users
     * time zone.
     */
    public static final String LAST_MODIFIED_UTC = "last_modified_utc";

    /**
     * The meta field for JSON blobs.
     */
    public static final String META = "meta";

}
