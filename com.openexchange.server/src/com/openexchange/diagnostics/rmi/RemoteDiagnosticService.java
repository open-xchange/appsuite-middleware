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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.diagnostics.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link RemoteDiagnosticService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public interface RemoteDiagnosticService extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = RemoteDiagnosticService.class.getSimpleName();

    /**
     * <p>Constructs and returns an unmodifiable {@link List} with all available charsets along with
     * their aliases (optional) in this JVM. The charset and the optional aliases will be returned
     * as a comma separated string in the {@link List}, where the first element of the comma separated
     * string will always be the charset followed by any aliases.</p>
     * 
     * <p>The {@link List} returned by this method will have one entry for each charset for which support
     * is available in the current Java virtual machine. If two or more supported charsets have the same
     * canonical name then the resulting {@link List} will contain just one of them; which one it will
     * contain is not specified.</p>
     * 
     * <p>The invocation of this method, and the subsequent use of the resulting {@link List}, may cause
     * time-consuming disk or network I/O operations to occur.</p>
     * 
     * <p>This method may return different results at different times if new charset providers are dynamically
     * made available to the current Java virtual machine. In the absence of such changes, the charsets
     * returned by this method are exactly those that can be retrieved via the <code>forName</code> method.</p>
     * 
     * @param aliases flag to decide whether to include the aliases in the {@link List}
     * @return an unmodifiable {@link List} with all available charsets in this JVM.
     */
    List<String> getCharsets(boolean aliases) throws RemoteException;

    /**
     * Returns an unmodifiable {@link List} containing all the installed providers. The order of the providers
     * in the {@link List} is their alphabetical order.
     * 
     * @return an unmodifiable {@link List} containing all the installed provider
     */
    List<String> getProtocols() throws RemoteException;

    /**
     * Returns an unmodifiable {@link List} of cipher suites which are enabled by default in this JVM. Unless a
     * different list is enabled, handshaking on an SSL connection will use one of these cipher suites. The
     * minimum quality of service for these defaults requires confidentiality protection and server authentication
     * (that is, no anonymous cipher suites).
     * 
     * @return an unmodifiable {@link List} of cipher suites which are enabled by default in this JVM.
     */
    List<String> getCipherSuites() throws RemoteException;

    /**
     * Returns the version string of the server; e.g. "7.8.3-Rev2".
     * 
     * @return the version string of the server
     * @throws IllegalStateException if version instance is not yet initialised
     */
    String getVersion() throws RemoteException;
}
