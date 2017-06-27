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

package com.openexchange.monitoring.sockets.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import com.openexchange.monitoring.sockets.IOFailure;
import com.openexchange.monitoring.sockets.TimeoutFailure;

/**
 * {@link SocketMonitoringInputStream}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SocketMonitoringInputStream extends InputStream {

    private final Socket socket;
    private final InputStream in;

    /**
     * Initializes a new {@link SocketMonitoringInputStream}.
     *
     * @param socket The associated socket
     * @param in The socket input stream to delegate to
     */
    public SocketMonitoringInputStream(Socket socket, InputStream in) {
        super();
        this.socket = socket;
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        long st = System.currentTimeMillis();
        try {
            int result = in.read();
            if (result < 0) {
                SocketMonitoringSystem.getInstance().eof(socket);
            } else {
                SocketMonitoringSystem.getInstance().read(socket, result);
            }
            return result;
        } catch (java.net.SocketTimeoutException e) {
            // Read timed out
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readTimedOut(socket, new TimeoutFailure(e, millis));
            throw e;
        } catch (IOException e) {
            // Read failed
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readError(socket, new IOFailure(e, millis));
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long st = System.currentTimeMillis();
        try {
            int length = in.read(b, off, len);
            if (length < 0) {
                SocketMonitoringSystem.getInstance().eof(socket);
            } else {
                SocketMonitoringSystem.getInstance().read(socket, b, off, length);
            }
            return length;
        } catch (java.net.SocketTimeoutException e) {
            // Read timed out
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readTimedOut(socket, new TimeoutFailure(e, millis));
            throw e;
        } catch (IOException e) {
            // Read failed
            long millis = System.currentTimeMillis() - st;
            SocketMonitoringSystem.getInstance().readError(socket, new IOFailure(e, millis));
            throw e;
        }
    }

}
