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

package com.openexchange.mail;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link MailAttributation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAttributation {

    /** The constant for a non-applicable mail attributation */
    public static final MailAttributation NOT_APPLICABLE = new MailAttributation(false, null, null);

    /**
     * Creates a new builder instance.
     *
     * @param originalFields The originally requested fields
     * @param originalHeaderNames The originally requested header names
     * @return The new builder instance
     */
    public static Builder builder(MailField[] originalFields, String[] originalHeaderNames) {
        return new Builder(originalFields, originalHeaderNames);
    }

    /**
     * A builder for an instance of <code>MailAttributation</code>.
     */
    public static class Builder {

        private final Set<MailField> fields;
        private final Set<String> headerNames;

        Builder(MailField[] originalFields, String[] originalHeaderNames) {
            super();
            fields = new LinkedHashSet<>();
            headerNames = new LinkedHashSet<>();
            if (null != originalFields) {
                for (MailField field : originalFields) {
                    fields.add(field);
                }
            }
            if (null != originalHeaderNames) {
                for (String headerName : originalHeaderNames) {
                    headerNames.add(headerName);
                }
            }
        }

        /**
         * Adds specified field.
         *
         * @param field The field to add
         * @return This instance
         */
        public Builder addField(MailField field) {
            fields.add(field);
            return this;
        }

        /**
         * Adds specified header name.
         *
         * @param headerName The header name to add
         * @return This instance
         */
        public Builder addHeaderName(String headerName) {
            if (null != headerName) {
                headerNames.add(headerName);
            }
            return this;
        }

        /**
         * Builds the resulting instance of {@code MailAttributation}.
         *
         * @return The resulting instance of {@code MailAttributation}
         */
        public MailAttributation build() {
            return new MailAttributation(true, fields, headerNames);
        }
    }

    // --------------------------------------------------------------------------------

    private final MailField[] fields;
    private final String[] headerNames;
    private final boolean applicable;

    /**
     * Initializes a new {@link MailAttributation}.
     */
    MailAttributation(boolean applicable, Set<MailField> fields, Set<String> headerNames) {
        super();
        this.applicable = applicable;
        if (null == fields || fields.isEmpty()) {
            this.fields = null;
        } else {
            this.fields = new MailField[fields.size()];
            int i = 0;
            for (MailField field : fields) {
                this.fields[i++] = field;
            }
        }
        if (null == headerNames || headerNames.isEmpty()) {
            this.headerNames = null;
        } else {
            this.headerNames = new String[headerNames.size()];
            int i = 0;
            for (String headerName : headerNames) {
                this.headerNames[i++] = headerName;
            }
        }
    }

    /**
     * Checks if this mail attribution is applicable to specified arguments passed on {@link MailFetchListener#onBeforeFetch(FullnameArgument, com.openexchange.mail.search.SearchTerm, MailSortField, OrderDirection, MailField[], String[]) onBeforeFetch} call.
     *
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    public boolean isApplicable() {
        return applicable;
    }

    /**
     * Gets the effective fields to request to satisfy invoked {@code MailFetchListener}.
     * <p>
     * The return value already includes previously requested ones.
     *
     * @return The fields to request or <code>null</code>
     */
    public MailField[] getFields() {
        return fields;
    }

    /**
     * Gets the effective header names to request to satisfy invoked {@code MailFetchListener}.
     * <p>
     * The return value already includes previously requested ones.
     *
     * @return The header names to request or <code>null</code>
     */
    public String[] getHeaderNames() {
        return headerNames;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("[");
        if (fields != null) {
            builder2.append("fields=").append(Arrays.toString(fields)).append(", ");
        }
        if (headerNames != null) {
            builder2.append("headerNames=").append(Arrays.toString(headerNames));
        }
        builder2.append("]");
        return builder2.toString();
    }

}
