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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.data.conversion.ical;

import java.util.Collections;
import java.util.List;


/**
 * {@link DefaultParseResult} - Default parse results.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultParseResult<T> implements ParseResult<T> {

    /**
     * Creates an empty parse result.
     *
     * @return An empty parse result
     */
    public static <T> DefaultParseResult<T> emptyParseResult() {
        return new DefaultParseResult<T>(Collections.<T> emptyList(), null);
    }

    /**
     * Creates a parse result for specified collection.
     *
     * @param objects The objects to wrap
     * @return The parse result for given collection
     */
    public static <T> DefaultParseResult<T> parseResultFor(List<T> objects) {
        return new DefaultParseResult<T>(objects, null);
    }

    /**
     * Creates a new builder instance
     *
     * @return The new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    /** The builder for an instance of <code>DefaultParseResult</code> */
    public static class Builder<T> {

        private List<T> importedObjects;
        private TruncationInfo truncationInfo;

        Builder() {
            super();
        }

        public Builder<T> importedObjects(List<T> importedObjects) {
            this.importedObjects = importedObjects;
            return this;
        }

        public Builder<T> truncationInfo(TruncationInfo truncationInfo) {
            this.truncationInfo = truncationInfo;
            return this;
        }

        /** Builds the <code>DefaultParseResult</code> instance from this builder's arguments */
        public DefaultParseResult<T> build() {
            return new DefaultParseResult<>(importedObjects, truncationInfo);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private final List<T> importedObjects;
    private final TruncationInfo truncationInfo;

    /**
     * Initializes a new {@link DefaultParseResult}.
     */
    DefaultParseResult(List<T> importedObjects, TruncationInfo truncationInfo) {
        super();
        this.importedObjects = null == importedObjects ? Collections.<T> emptyList() : importedObjects;
        this.truncationInfo = truncationInfo;
    }

    @Override
    public List<T> getImportedObjects() {
        return importedObjects;
    }

    @Override
    public TruncationInfo getTruncationInfo() {
        return truncationInfo;
    }

}
