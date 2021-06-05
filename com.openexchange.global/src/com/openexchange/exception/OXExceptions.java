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

package com.openexchange.exception;

import static com.openexchange.exception.OXExceptionConstants.CATEGORY_CONFLICT;
import static com.openexchange.exception.OXExceptionConstants.CATEGORY_ERROR;
import static com.openexchange.exception.OXExceptionConstants.CATEGORY_PERMISSION_DENIED;
import static com.openexchange.exception.OXExceptionConstants.CATEGORY_USER_INPUT;
import static com.openexchange.exception.OXExceptionConstants.CODE_DEFAULT;
import static com.openexchange.exception.OXExceptionConstants.PREFIX_GENERAL;
import com.openexchange.exception.OXException.Generic;

/**
 * {@link OXExceptions} - Utility class for {@link OXException}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXExceptions {

    /**
     * Initializes a new {@link OXExceptions}.
     */
    public OXExceptions() {
        super();
    }

    /**
     * Checks if specified <code>OXException</code>'s (first) category matches {@link OXExceptionConstants#CATEGORY_USER_INPUT USER_INPUT category}.
     *
     * @param e The <code>OXException</code> instance to check
     * @return <code>true</code> if category matches; otherwise <code>false</code>
     */
    public static boolean isUserInput(OXException e) {
        return isCategory(CATEGORY_USER_INPUT, e);
    }

    /**
     * Checks if specified <code>OXException</code>'s (first) category matches {@link OXExceptionConstants#CATEGORY_PERMISSION_DENIED PERMISSION_DENIED category}.
     *
     * @param e The <code>OXException</code> instance to check
     * @return <code>true</code> if category matches; otherwise <code>false</code>
     */
    public static boolean isPermissionDenied(OXException e) {
        return isCategory(CATEGORY_PERMISSION_DENIED, e);
    }

    /**
     * Checks if specified <code>OXException</code>'s (first) category matches given category.
     *
     * @param category The category
     * @param e The <code>OXException</code> instance to check
     * @return <code>true</code> if category matches; otherwise <code>false</code>
     */
    public static boolean isCategory(Category category, OXException e) {
        if (null == category || null == e) {
            return false;
        }

        Category cat = e.getCategory();
        return null != cat && category.getType().equals(cat.getType());
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if given {@link OXException} instance contains either a socket or an I/O error in its cause chain.
     *
     * @param e The <tt>OXException</tt> instance to check
     * @return <code>true</code> if <tt>OXException</tt> instance contains either a socket or an I/O error in its cause chain; otherwise
     *         <code>false</code>
     */
    public static boolean containsCommunicationError(OXException e) {
        if (null == e) {
            return false;
        }
        return containsCommunicationError0(e.getCause());
    }

    private static boolean containsCommunicationError0(Throwable t) {
        if (null == t) {
            return false;
        }
        if ((t instanceof java.io.IOError) || (t instanceof java.io.IOException) || (t instanceof java.net.SocketException)) {
            // Whatever... Timeout, bind error, no route to host, connect error, connection reset, ...
            return true;
        }
        return containsCommunicationError0(t.getCause());
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a general exception.
     *
     * @param logMessage The log message
     * @return A general exception.
     */
    public static OXException general(final String logMessage) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.MESSAGE).setLogMessage(logMessage).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL);
    }

    /**
     * Creates a general exception.
     *
     * @param logMessage The log message
     * @param cause The cause
     * @return A general exception.
     */
    public static OXException general(final String logMessage, final Throwable cause) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.MESSAGE, cause).setLogMessage(logMessage).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL);
    }

    /**
     * Creates a not-found exception.
     *
     * @param id The identifier of the missing object
     * @return A not-found exception.
     */
    public static OXException notFound(final String id) {
        return new OXException(1, OXExceptionStrings.MESSAGE_NOT_FOUND, id).setCategory(CATEGORY_USER_INPUT).setPrefix(PREFIX_GENERAL).setGeneric(Generic.NOT_FOUND);
    }

    /**
     * Creates a module-denied exception.
     *
     * @param module The identifier of the module
     * @return A module-denied exception.
     */
    public static OXException noPermissionForModule(final String module) {
        return new OXException(1, OXExceptionStrings.MESSAGE_PERMISSION_MODULE, module).setCategory(CATEGORY_USER_INPUT).setPrefix(PREFIX_GENERAL).setGeneric(Generic.NO_PERMISSION);
    }

    /**
     * Creates a folder-denied exception.
     *
     * @return A folder-denied exception.
     */
    public static OXException noPermissionForFolder() {
        return new OXException(1, OXExceptionStrings.MESSAGE_PERMISSION_FOLDER).setCategory(CATEGORY_PERMISSION_DENIED).setPrefix(PREFIX_GENERAL).setGeneric(Generic.NO_PERMISSION);
    }

    /**
     * Creates a missing-field exception.
     *
     * @param name The field name
     * @return A missing-field exception.
     */
    public static OXException mandatoryField(final String name) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.MESSAGE_MISSING_FIELD, name).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL).setGeneric(
            Generic.MANDATORY_FIELD);
    }

    /**
     * Creates a missing-field exception.
     *
     * @param code The code number
     * @param name The field name
     * @return A missing-field exception.
     */
    public static OXException mandatoryField(final int code, final String name) {
        return new OXException(code, OXExceptionStrings.MESSAGE_MISSING_FIELD, name).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL).setGeneric(Generic.MANDATORY_FIELD);
    }

    /**
     * Creates a general conflict exception.
     *
     * @return A general conflict exception.
     */
    public static OXException conflict() {
        return new OXException(1, OXExceptionStrings.MESSAGE_CONFLICT).setCategory(CATEGORY_CONFLICT).setPrefix(PREFIX_GENERAL).setGeneric(Generic.CONFLICT);
    }

    /**
     * Creates a general database exception.
     *
     * @param cause The cause, usually the underlying SQL exception
     * @return A general database exception
     */
    public static OXException database(Throwable cause) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.SQL_ERROR_MSG, cause)
            .setLogMessage(null != cause ? cause.getMessage() : null)
            .setCategory(CATEGORY_ERROR)
            .setPrefix(PREFIX_GENERAL)
        ;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the code for given arguments.
     *
     * @param code The error code number
     * @param prefix The error code prefix
     * @return The code
     */
    public static Code codeFor(int code, String prefix) {
        return new CodeImpl(prefix, code);
    }

    private static class CodeImpl implements Code {

        private final String prefix;
        private final int number;
        private int hash;

        CodeImpl(String prefix, int number) {
            super();
            this.prefix = prefix;
            this.number = number;
            hash = 0;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public int getNumber() {
            return number;
        }

        @Override
        public int hashCode() {
            int result = hash;
            if (result == 0) {
                int prime = 31;
                result = 1;
                result = prime * result + number;
                result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
                this.hash = result;
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Code)) {
                return false;
            }
            Code other = (Code) obj;
            if (number != other.getNumber()) {
                return false;
            }
            if (prefix == null) {
                if (other.getPrefix() != null) {
                    return false;
                }
            } else if (!prefix.equals(other.getPrefix())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new StringBuilder(getPrefix()).append('-').append(String.format("%04d", Integer.valueOf(number))).toString();
        }
    }



    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates the prefix for given argument.
     *
     * @param prefix The error code prefix
     * @return The prefix
     */
    public static Prefix prefixFor(String prefix) {
        return new PrefixImpl(prefix);
    }

    private static class PrefixImpl implements Prefix {

        private final String prefix;
        private int hash;

        PrefixImpl(String prefix) {
            super();
            this.prefix = prefix;
            hash = 0;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public int hashCode() {
            int result = hash;
            if (result == 0) {
                int prime = 31;
                result = 1;
                result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
                this.hash = result;
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Prefix)) {
                return false;
            }
            Prefix other = (Prefix) obj;
            if (prefix == null) {
                if (other.getPrefix() != null) {
                    return false;
                }
            } else if (!prefix.equals(other.getPrefix())) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return prefix;
        }
    }

}
