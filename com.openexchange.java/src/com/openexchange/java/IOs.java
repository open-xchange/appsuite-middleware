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

}
