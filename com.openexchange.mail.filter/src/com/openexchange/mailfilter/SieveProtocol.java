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

package com.openexchange.mailfilter;

import java.util.Optional;
import com.openexchange.exception.OXException;

/**
 * {@link SieveProtocol} - Provides low-level access to mail filter/SIEVE server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public interface SieveProtocol {

    /**
     * Writes given command line to mail filter/SIEVE end-point.
     *
     * @param commandLine The command line
     * @throws OXException If writing command line fails
     */
    default void write(String commandLine) throws OXException {
        write(new String[] {commandLine});
    }

    /**
     * Writes given command lines to mail filter/SIEVE end-point.
     *
     * @param commandLines The command lines
     * @throws OXException If writing command lines fails
     */
    void write(String... commandLines) throws OXException;

    /**
     * Reads response lines from mail filter/SIEVE end-point.
     *
     * @return The response lines
     * @throws OXException If reading fails
     */
    String[] readResponseLines() throws OXException;

    /**
     * Handles the last response line as obtained from {@link #readResponseLines(Optional)} (and {@link #readResponseLines()} respectively).
     *
     * @param responseLine The (last) response line to handle
     * @throws OXException If a SIEVE protocol error happens (e.g. <code>NO</code> response)
     */
    default void handleResponse(String responseLine) throws OXException {
        handleResponse(responseLine, Optional.empty());
    }

    /**
     * Handles the last response line as obtained from {@link #readResponseLines()}.
     * <p>
     * If <code>optionalHandler</code> is absent, a SIEVE <code>NO</code> response is quit with <code>"Command failed"</code> error message.
     *
     * @param responseLine The (last) response line to handle
     * @param The optional <code>NO</code> response handler; if absent a SIEVE <code>NO</code> response is quit with <code>"Command failed"</code> error message.
     * @throws OXException If a SIEVE protocol error happens (e.g. <code>NO</code> response)
     */
    void handleResponse(String responseLine, Optional<NoResponseHandler> optionalHandler) throws OXException;

}
