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

package com.openexchange.mail.compose.impl.storage.db.mapping;

import java.sql.Types;
import org.json.JSONObject;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;

/**
 * {@link AbstractVarCharJsonObjectMapping}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public abstract class AbstractVarCharJsonObjectMapping<T, O> extends DefaultDbMapping<T, O> {

    /**
     * Initializes a new {@link AbstractVarCharJsonObjectMapping}.
     *
     * @param columnLabel The name of the database column
     * @param readableName The readable name
     */
    protected AbstractVarCharJsonObjectMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.VARCHAR);
    }

    /**
     * Gets the string representation of given <code>String</code> instance or JSON {@link JSONObject#NULL <code>null</code>} if the
     * instance is <code>null</code>.
     *
     * @param string The <code>String</code> instance
     * @return The string representation or JSON {@link JSONObject#NULL <code>null</code>}
     */
    protected static Object getNullable(String string) {
        return string == null ? JSONObject.NULL : string.toString();
    }

    /**
     * Gets the {@link Object#toString() toString()} representation of given instance or JSON {@link JSONObject#NULL <code>null</code>} if
     * the instance is <code>null</code>.
     *
     * @param toString The instance from which the <code>toString()</code> representation is desired
     * @return The instance's <code>toString()</code> representation or JSON {@link JSONObject#NULL <code>null</code>}
     */
    protected static Object getNullable(Object toString) {
        return toString == null ? JSONObject.NULL : toString.toString();
    }

}
