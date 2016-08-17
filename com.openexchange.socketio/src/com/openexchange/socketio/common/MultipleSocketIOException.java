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

package com.openexchange.socketio.common;

import java.util.Iterator;
import java.util.List;

/**
 * {@link MultipleSocketIOException} - An exception wrapping/chaining multiple collected Socket.IO exceptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MultipleSocketIOException extends SocketIOException {

    private static final long serialVersionUID = 2989162173768482334L;

    /**
     * Gets the chained Socket.IO exception for specified collected ones.
     *
     * @param exceptions The collected Socket.IO exceptions
     * @return The chained exception
     */
    public static SocketIOException chainedSocketIOExceptionFor(List<SocketIOException> exceptions) {
        int size;
        if (null == exceptions || (size = exceptions.size()) == 0) {
            throw new IllegalArgumentException("Socket.IO exceptions must not be null or empty");
        }

        return 1 == size ? exceptions.get(0) : new MultipleSocketIOException(exceptions);
    }

    // -----------------------------------------------------------------------------------------------

    private final List<SocketIOException> exceptions;

    /**
     * Initializes a new {@link MultipleSocketIOException}.
     *
     * @param exceptions The collected Socket.IO exceptions
     */
    private MultipleSocketIOException(List<SocketIOException> exceptions) {
        super("Socket.IO errors occurred", createSocketIOExceptionChain(null, exceptions.iterator()));
        this.exceptions = exceptions;
    }

    private static SocketIOException createSocketIOExceptionChain(SocketIOException root, Iterator<SocketIOException> exceptions) {
        if (!exceptions.hasNext()) {
            return root;
        }

        SocketIOException socketIoError = exceptions.next();
        if (null == root) {
            root = socketIoError;
        } else {
            root.initCause(socketIoError);
        }
        createSocketIOExceptionChain(socketIoError, exceptions);
        return root;
    }

    /**
     * Gets the collected exceptions
     *
     * @return The exceptions
     */
    public List<SocketIOException> getExceptions() {
        return exceptions;
    }

}
