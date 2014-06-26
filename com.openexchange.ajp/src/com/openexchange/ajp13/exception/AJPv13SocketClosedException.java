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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.exception;

/**
 * {@link AJPv13SocketClosedException} - Thrown to indicate a closed AJP socket which cause to abort all communication to client. Therefore
 * {@link AJPv13Exception#keepAlive()} always returns <code>false</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com>Thorben Betten</a>
 */
public class AJPv13SocketClosedException extends AJPv13Exception {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 3627750166902286241L;

    /**
     * Initializes a new {@link AJPv13SocketClosedException}
     *
     * @param code The error code
     */
    public AJPv13SocketClosedException(final AJPCode code) {
        this(code, null, EMPTY_ARGS);
    }

    /**
     * Initializes a new {@link AJPv13SocketClosedException}
     *
     * @param code The error code
     * @param cause The init cause
     */
    public AJPv13SocketClosedException(final AJPCode code, final Exception cause) {
        super(code, false, cause, EMPTY_ARGS);
    }

    /**
     * Initializes a new {@link AJPv13SocketClosedException}
     *
     * @param code The error code
     * @param cause The init cause
     * @param messageArgs The message arguments
     */
    public AJPv13SocketClosedException(final AJPCode code, final Exception cause, final Object... messageArgs) {
        super(code, false, cause, messageArgs);
    }

}
