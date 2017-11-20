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

package com.openexchange.mail;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link MailAttributation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAttributation {

    /**
     * Creates a new builder instance.
     *
     * @param originalFields The originally requested fields
     * @param originalHeaderNames The originally requested header names
     * @return The new builder instance
     */
    public static Builder builder(int[] originalFields, String[] originalHeaderNames) {
        return new Builder(originalFields, originalHeaderNames);
    }

    /**
     * A builder for an instance of <code>MailAttributation</code>.
     */
    public static class Builder {

        private final Set<Integer> fields;
        private final Set<String> headerNames;

        Builder(int[] originalFields, String[] originalHeaderNames) {
            super();
            fields = new LinkedHashSet<>();
            headerNames = new LinkedHashSet<>();
            if (null != originalFields) {
                for (int field : originalFields) {
                    fields.add(Integer.valueOf(field));
                }
            }
            if (null != originalHeaderNames) {
                for (String headerName : originalHeaderNames) {
                    headerNames.add(headerName);
                }
            }
        }

        public Builder addField(int field) {
            fields.add(Integer.valueOf(field));
            return this;
        }

        public Builder addHeaderName(String headerName) {
            if (null != headerName) {
                headerNames.add(headerName);
            }
            return this;
        }

        public MailAttributation build() {
            return new MailAttributation(fields, headerNames);
        }
    }

    // --------------------------------------------------------------------------------

    private final int[] fields;
    private final String[] headerNames;

    /**
     * Initializes a new {@link MailAttributation}.
     */
    MailAttributation(Set<Integer> fields, Set<String> headerNames) {
        super();
        if (null == fields || fields.isEmpty()) {
            this.fields = null;
        } else {
            this.fields = new int[fields.size()];
            int i = 0;
            for (Integer field : fields) {
                this.fields[i++] = field.intValue();
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
     * Gets the effective fields to request.
     *
     * @return The fields to request or <code>null</code>
     */
    public int[] getFields() {
        return fields;
    }

    /**
     * Gets the effective header names to request.
     *
     * @return The header names to request or <code>null</code>
     */
    public String[] getHeaderNames() {
        return headerNames;
    }

}
