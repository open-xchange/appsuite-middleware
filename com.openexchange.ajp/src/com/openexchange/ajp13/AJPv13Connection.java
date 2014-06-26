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


package com.openexchange.ajp13;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajp13.exception.AJPv13Exception;

public interface AJPv13Connection {

    public static final int IDLE_STATE = 1;

    public static final int ASSIGNED_STATE = 2;

    /**
     * Gets the associated AJP request handler which processes the AJP data sent over this connection
     *
     * @return The associated AJP request handler.
     */
    public AJPv13RequestHandler getAjpRequestHandler();

    /**
     * Gets the input stream from AJP client
     *
     * @return The input stream from AJP client
     * @throws IOException If input stream cannot be returned
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Set whether to enable blocking access to input stream or not.
     *
     * @param block <code>true</code> to enable blocking access; otherwise <code>false</code>
     */
    public void blockInputStream(boolean block);

    /**
     * Gets the output stream to AJP client
     *
     * @return The output stream to AJP client
     * @throws IOException If output stream cannot be returned
     */
    public BlockableBufferedOutputStream getOutputStream() throws IOException;

    /**
     * Set whether to enable blocking access to output stream or not.
     *
     * @param block <code>true</code> to enable blocking access; otherwise <code>false</code>
     */
    public void blockOutputStream(boolean block);

    /**
     * Closes this AJP connection.<br>
     * Closes both input and output stream and associated socket as well.
     */
    public void close();

    /**
     * Sets the SO_TIMEOUT with the specified timeout, in milliseconds.
     *
     * @param millis The timeout in milliseconds
     * @throws AJPv13Exception If there is an error in the underlying protocol, such as a TCP error.
     */
    public void setSoTimeout(final int millis) throws AJPv13Exception;

    /**
     * Gets the number of current AJP package.
     *
     * @return The number of current AJP package.
     */
    public int getPackageNumber();

    /**
     * Gets the current AJP connection's state
     *
     * @return Current AJP connection's state
     */
    public int getState();
}
