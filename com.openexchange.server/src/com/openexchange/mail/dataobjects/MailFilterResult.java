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

package com.openexchange.mail.dataobjects;

/**
 * {@link MailFilterResult} - Indicates whether a filter was successfully applied to a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public abstract class MailFilterResult {

    private static final OkMailFilterResult PLAIN_OK = new OkMailFilterResult(null);

    /**
     * Gets the OK filter result for given optional ID
     *
     * @param id The optional ID or <code>null</code>
     * @return The OK filter result
     */
    public static MailFilterResult okFor(String id) {
        return id == null ? PLAIN_OK : new OkMailFilterResult(id);
    }

    /**
     * Gets the WARNINGS filter result for given optional ID
     *
     * @param id The optional ID or <code>null</code>
     * @param warnings The warnings
     * @return The WARNINGS filter result
     */
    public static MailFilterResult warningsFor(String id, String warnings) {
        return new WarningsMailFilterResult(id, warnings);
    }

    /**
     * Gets the ERRORS filter result for given optional ID
     *
     * @param id The optional ID or <code>null</code>
     * @param errors The errors
     * @return The ERRORS filter result
     */
    public static MailFilterResult errorsFor(String id, String errors) {
        return new ErrorsMailFilterResult(id, errors);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The optional mail ID or <code>null</code> */
    protected final String id;

    /**
     * Initializes a new {@link MailFilterResult}.
     *
     * @param id The optional mail ID or <code>null</code>
     */
    protected MailFilterResult(String id) {
        super();
        this.id = id;
    }

    /**
     * Gets the mail ID.
     *
     * @return The mail ID or <code>null</code>
     */
    public String getId() {
        return id;
    }

    /**
     * Checks if the filter was applied successfully.
     *
     * @return <code>true</code> if filter was applied successfully; otherwise <code>false</code>
     */
    public abstract boolean isOK();

    /**
     * Checks if the filter was applied successfully, but there were one or more warnings produced by the filter.
     *
     * @return <code>true</code> if warnings exist; otherwise <code>false</code>
     */
    public abstract boolean hasWarnings();

    /**
     * Checks if application of the filter failed for some reason.
     *
     * @return <code>true</code> if filter failed; otherwise <code>false</code>
     */
    public abstract boolean hasErrors();

    /**
     * Gets a human-readable descriptive text listing the encountered errors
     *
     * @return The errors
     */
    public abstract String getErrors();

    /**
     * Gets a human-readable descriptive text listing the produced warnings.
     *
     * @return The warnings
     */
    public abstract String getWarnings();

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class OkMailFilterResult extends MailFilterResult {

        OkMailFilterResult(String id) {
            super(id);
        }

        @Override
        public boolean isOK() {
            return true;
        }

        @Override
        public boolean hasWarnings() {
            return false;
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public String getErrors() {
            return null;
        }

        @Override
        public String getWarnings() {
            return null;
        }
    }

    private static class WarningsMailFilterResult extends MailFilterResult {

        private final String warnings;

        WarningsMailFilterResult(String id, String warnings) {
            super(id);
            this.warnings = warnings;
        }

        @Override
        public String getWarnings() {
            return warnings;
        }

        @Override
        public boolean hasWarnings() {
            return true;
        }

        @Override
        public boolean isOK() {
            return false;
        }

        @Override
        public boolean hasErrors() {
            return false;
        }

        @Override
        public String getErrors() {
            return null;
        }
    }

    private static class ErrorsMailFilterResult extends MailFilterResult {

        private final String errors;

        ErrorsMailFilterResult(String id, String errors) {
            super(id);
            this.errors = errors;
        }

        @Override
        public boolean isOK() {
            return false;
        }

        @Override
        public boolean hasWarnings() {
            return false;
        }

        @Override
        public boolean hasErrors() {
            return true;
        }

        @Override
        public String getErrors() {
            return errors;
        }

        @Override
        public String getWarnings() {
            return null;
        }
    }

}
