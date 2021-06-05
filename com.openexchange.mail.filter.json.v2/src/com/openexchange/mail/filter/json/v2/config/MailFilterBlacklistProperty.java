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

package com.openexchange.mail.filter.json.v2.config;

import com.openexchange.config.lean.Property;

/**
 *
 * {@link MailFilterBlacklistProperty} defines properties to blacklist mailfilter elements. E.g. actions or comparisons.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MailFilterBlacklistProperty implements Property {

    public enum BasicGroup {
        /**
         * Specifies the actions group.
         */
        actions,

        /**
         * Specifies the tests group
         */
        tests,

        /**
         * Specifies the comparisons group
         */
        comparisons
    }

    public enum Field {

        /**
         * Specifies the comparisons element.
         *
         * Note: This element is similar to the comparisons group but only blacklists comparisons for a single test
         */
        comparisons,

        /**
         * Specifies the headers element
         */
        headers,

        /**
         * Specifies the parts element
         */
        parts;

        public static Field getFieldByName(String name) {
            for (Field ele : Field.values()) {
                if (ele.name().equals(name.toLowerCase())) {
                    return ele;
                }
            }
            return null;
        }
    }

    private static final String DOT = ".";
    private static final String PREFIX = "com.openexchange.mail.filter.blacklist.";
    private final String fqn;

    private final BasicGroup base;
    private final String sub;
    private final Field field;
    private final int hashCode;

    /**
     *
     * Initializes a new {@link MailFilterBlacklistProperty}.
     *
     * @param group
     */
    public MailFilterBlacklistProperty(BasicGroup group) {
        this.fqn = PREFIX + group.name();
        this.base = group;
        this.sub = null;
        this.field = null;
        hashCode = generateHashCode();
    }

    /**
     * Initializes a new {@link MailFilterBlacklistProperty}.
     *
     * @param group The base group (e.g. tests)
     * @param subGroup The sub group (e.g. address)
     * @param field The field (e.g. comparisons)
     */
    public MailFilterBlacklistProperty(BasicGroup group, String subGroup, Field field) {
        StringBuilder builder = new StringBuilder(PREFIX);
        builder.append(group.name()).append(DOT).append(subGroup).append(DOT).append(field);
        this.fqn = builder.toString();
        this.base = group;
        this.sub = subGroup;
        this.field = field;
        hashCode = generateHashCode();
    }

    /**
     * Generates the hash code
     * 
     * @return The generated hash code
     */
    private int generateHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((fqn == null) ? 0 : fqn.hashCode());
        result = prime * result + ((sub == null) ? 0 : sub.hashCode());
        return result;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public <T> T getDefaultValue(Class<T> cls) {
        return null;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MailFilterBlacklistProperty) {
            return this.fqn.equals(((MailFilterBlacklistProperty) obj).getFQPropertyName());
        }
        return false;
    }


    /**
     * Gets the base. See {@link BasicGroup}
     *
     * @return The base
     */
    public BasicGroup getBase() {
        return base;
    }

    /**
     * Gets the sub. E.g. "address"
     *
     * @return The sub
     */
    public String getSub() {
        return sub;
    }

    /**
     * Gets the name. See {@link Field}
     *
     * @return The name
     */
    public Field getField() {
        return field;
    }
}
