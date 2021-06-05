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

package com.openexchange.mail.filter.json.v2.mapper;

import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.parser.generated.Token;
import com.openexchange.mail.filter.json.v2.json.fields.ActionField;

/**
 * {@link ArgumentUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class ArgumentUtil {

    /**
     * Creates a {@link TagArgument} from the specified {@link ActionField}
     *
     * @param field The {@link ActionField} from which to create the {@link TagArgument}
     * @return The {@link TagArgument}
     */
    public static final TagArgument createTagArgument(ActionField field) {
        Token token = new Token();
        token.image = field.getTagName();
        return new TagArgument(token);
    }

    /**
     * Creates a {@link NumberArgument} from the specified string value
     *
     * @param value The value of the {@link NumberArgument}
     * @return the {@link NumberArgument}
     */
    public static final NumberArgument createNumberArgument(String value) {
        Token token = new Token();
        token.image = value;
        return new NumberArgument(token);
    }

    /**
     * Creates a {@link TagArgument} from the specified string value
     *
     * @param value The value of the {@link TagArgument}
     * @return the {@link TagArgument}
     */
    public static final TagArgument createTagArgument(String value) {
        Token token = new Token();
        token.image = ":" + value;
        return new TagArgument(token);
    }
}
