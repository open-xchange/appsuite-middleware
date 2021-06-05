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

package com.openexchange.java;

import java.io.IOException;

/**
 * {@link IOs} - A utility class for I/O associated processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class IOs {

    /**
     * Initializes a new {@link IOs}.
     */
    private IOs() {
        super();
    }

    /**
     * Checks whether specified I/O exception can be considered as a connection reset.
     * <p>
     * A <code>"java.io.IOException: Connection reset by peer"</code> is thrown when the other side has abruptly aborted the connection in midst of a transaction.
     * <p>
     * That can have many causes which are not controllable from the Middleware side. E.g. the end-user decided to shutdown the client or change the
     * server abruptly while still interacting with your server, or the client program has crashed, or the enduser's Internet connection went down,
     * or the enduser's machine crashed, etc, etc.
     *
     * @param e The I/O exception to examine
     * @return <code>true</code> for a connection reset; otherwise <code>false</code>
     */
    public static boolean isConnectionReset(IOException e) {
        if (null == e) {
            return false;
        }

        String lcm = com.openexchange.java.Strings.asciiLowerCase(e.getMessage());
        if ("connection reset by peer".equals(lcm) || "broken pipe".equals(lcm)) {
            return true;
        }

        Throwable cause = e.getCause();
        return cause instanceof IOException ? isConnectionReset((IOException) cause) : false;
    }

    /**
     * Checks if cause of specified exception indicates an unexpected end of file or end of stream during reading input.
     *
     * @param e The exception to examine
     * @return <code>true</code> if an EOF problem is indicated; otherwise <code>false</code>
     */
    public static boolean isEOFException(Exception e) {
        if (null == e) {
            return false;
        }

        return isEitherOf(e, java.io.EOFException.class);
    }

    /**
     * Checks if cause of specified exception indicates a connect problem.
     *
     * @param e The exception to examine
     * @return <code>true</code> if a connect problem is indicated; otherwise <code>false</code>
     */
    public static boolean isConnectException(Exception e) {
        if (null == e) {
            return false;
        }

        return isEitherOf(e, java.net.ConnectException.class);
    }

    /**
     * Checks if cause of specified exception indicates a timeout or connect problem.
     *
     * @param e The exception to examine
     * @return <code>true</code> if a timeout or connect problem is indicated; otherwise <code>false</code>
     */
    public static boolean isTimeoutOrConnectException(Exception e) {
        if (null == e) {
            return false;
        }

        return isEitherOf(e, java.net.SocketTimeoutException.class, java.net.ConnectException.class);
    }

    /**
     * Checks if cause of specified exception indicates a timeout problem.
     *
     * @param e The exception to examine
     * @return <code>true</code> if a timeout problem is indicated; otherwise <code>false</code>
     */
    public static boolean isTimeoutException(Exception e) {
        if (null == e) {
            return false;
        }

        return isEitherOf(e, java.net.SocketTimeoutException.class);
    }

    @SafeVarargs
    private static boolean isEitherOf(Throwable e, Class<? extends Exception>... classes) {
        if (null == e || null == classes || 0 == classes.length) {
            return false;
        }

        for (Class<? extends Exception> clazz : classes) {
            if (clazz.isInstance(e)) {
                return true;
            }
        }

        Throwable next = e.getCause();
        return null == next ? false : isEitherOf(next, classes);
    }

}
