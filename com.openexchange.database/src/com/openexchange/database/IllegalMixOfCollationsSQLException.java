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

package com.openexchange.database;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.Strings;

/**
 * {@link IllegalMixOfCollationsSQLException} - The special SQL exception signaling an illegal mix of collations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class IllegalMixOfCollationsSQLException extends StringLiteralSQLException {

    private static final long serialVersionUID = 3613082500383087281L;

    /** The pattern to match:<pre>Illegal mix of collations (%s,%s) and (%s,%s) for operation '%s'</pre> */
    private static final Pattern PATTERN_ERROR_MESSAGE = Pattern.compile(Pattern.quote("Illegal mix of collations ") + "\\(([a-zA-Z0-9-_]+),([^\\)]+)\\)" + " and " + "\\(([a-zA-Z0-9-_]+),([^\\)]+)\\)" + " for operation " + "'([^']+)'");

    /** The (vendor) error code <code>1267</code> that signals an attempt to pass an incorrect string to database */
    public static final int ERROR_CODE = com.mysql.jdbc.MysqlErrorNumbers.ER_CANT_AGGREGATE_2COLLATIONS;

    public static final char UNKNOWN = '\ufffd';

    /**
     * Attempts to yield an appropriate {@code IncorrectStringSQLException} instance for specified SQL exception.
     *
     * @param e The SQL exception
     * @return The appropriate {@code IncorrectStringSQLException} instance or <code>null</code>
     */
    public static IllegalMixOfCollationsSQLException instanceFor(SQLException e) {
        if (null == e) {
            return null;
        }
        if (ERROR_CODE != e.getErrorCode()) {
            return null;
        }

        // E.g. Illegal mix of collations (utf8_unicode_ci,IMPLICIT) and (utf8mb4_general_ci,COERCIBLE) for operation '='
        Matcher m = PATTERN_ERROR_MESSAGE.matcher(e.getMessage());
        if (!m.matches()) {
            return null;
        }

        CollationInfo firstCollation = new CollationInfo(m.group(1), CollationNature.getCollationNatureFor(m.group(2)));
        CollationInfo secondCollation = new CollationInfo(m.group(3), CollationNature.getCollationNatureFor(m.group(4)));
        String operation = m.group(5);
        return new IllegalMixOfCollationsSQLException(firstCollation, secondCollation, operation, e);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    private final CollationInfo firstCollation;
    private final CollationInfo secondCollation;
    private final String operation;

    /**
     * Initializes a new {@link IllegalMixOfCollationsSQLException}.
     *
     * @param firstCollation The first one of the conflicting collations
     * @param secondCollation The second one of the conflicting collations
     * @param operation The operation
     * @param cause The associated SQL exception
     */
    private IllegalMixOfCollationsSQLException(CollationInfo firstCollation, CollationInfo secondCollation, String operation, SQLException cause) {
        super(cause);
        this.firstCollation = firstCollation;
        this.secondCollation = secondCollation;
        this.operation = operation;
    }

    /**
     * Gets the first one of the conflicting collations.
     *
     * @return The first collation
     */
    public CollationInfo getFirstCollation() {
        return firstCollation;
    }

    /**
     * Gets the second one of the conflicting collations.
     *
     * @return The second collation
     */
    public CollationInfo getSecondCollation() {
        return secondCollation;
    }

    /**
     * Gets the operation that failed
     *
     * @return The operation
     */
    public String getOperation() {
        return operation;
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    /** The collation nature */
    public static enum CollationNature {
        IMPLICIT("IMPLICIT"),
        COERCIBLE("COERCIBLE"),
        ;

        private final String id;

        private CollationNature(String id) {
            this.id = id;
        }

        /**
         * Gets the identifier
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }

        private static final Map<String, CollationNature> MAP = ImmutableMap.of("implicit", IMPLICIT, "coercible", COERCIBLE);

        /**
         * Gets the collation nature for specified identifier
         *
         * @param id The identifier to look-up by
         * @return The collation nature or <code>null</code>
         */
        public static CollationNature getCollationNatureFor(String id) {
            return null == id ? null : MAP.get(Strings.asciiLowerCase(id));
        }
    }

    /** The collation information */
    public static class CollationInfo {

        private final String name;
        private final CollationNature nature;

        CollationInfo(String name, CollationNature nature) {
            super();
            this.name = name;
            this.nature = nature;
        }

        /**
         * Gets the name
         *
         * @return The name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the nature
         *
         * @return The nature
         */
        public CollationNature getNature() {
            return nature;
        }

    }

}
