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

package com.openexchange.search.internal.operands;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;
import com.openexchange.search.Operand;

/**
 * {@link AttachmentOperand}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class AttachmentOperand implements Operand<String> {

    public static enum AttachmentOperandType {
        NAME("name");

        private final String name;

        private AttachmentOperandType(String name) {
            this.name = name;
        }

        private static final Map<String, AttachmentOperandType> TYPES_BY_NAME;
        static {
            ImmutableMap.Builder<String, AttachmentOperandType> typesByName = ImmutableMap.builder();
            for (AttachmentOperandType type : AttachmentOperandType.values()) {
                typesByName.put(type.name, type);
            }
            TYPES_BY_NAME = typesByName.build();
        }

        /**
         * Gets an {@link AttachmentOperandType} by its name.
         *
         * @return The type or <code>null</code>, if the name is invalid or unknown.
         */
        public static AttachmentOperandType getByName(String name) {
            return Strings.isEmpty(name) ? null : TYPES_BY_NAME.get(Strings.asciiLowerCase(name).trim());
        }
    }

    // -----------------------------------------------------------------------------------------------

    private final AttachmentOperandType type;

    /**
     * Initializes a new {@link ColumnOperand}.
     *
     * @param name The attachment
     */
    public AttachmentOperand(final AttachmentOperandType type) {
        super();
        this.type = type;
    }

    @Override
    public com.openexchange.search.Operand.Type getType() {
        return Type.ATTACHMENT;
    }

    @Override
    public String getValue() {
        return type.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder(Type.ATTACHMENT.getType()).append(':').append(type).toString();
    }

}
