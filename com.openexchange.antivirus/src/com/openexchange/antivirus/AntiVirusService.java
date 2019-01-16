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

package com.openexchange.antivirus;

import java.io.File;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link AntiVirusService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
@SingletonService
public interface AntiVirusService {

    /**
     * Scans the specified {@link InputStreamClosure}
     * 
     * @param stream The {@link InputStreamClosure} which retrieves the InputStream if necessary
     * @param uniqueId an identifier that uniquely identifies the specified {@link InputStream}
     * @param contentLength The length of the {@link InputStream} in bytes
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(InputStreamClosure stream, String uniqueId, long contentLength) throws OXException;

    /**
     * Scans the contents of the specified {@link IFileHolder}
     * 
     * @param fileHolder The {@link IFileHolder}
     * @param uniqueId an identifier that uniquely identifies the {@link InputStream} of the specified {@link IFileHolder}
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(IFileHolder fileHolder, String uniqueId) throws OXException;

    /**
     * Scans the specified {@link File}
     * 
     * @param file The {@link File} to scan
     * @param uniqueId an identifier that uniquely identifies the specified {@link File}
     * @param fileSize The file's size in bytes
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(File file, String uniqueId, long fileSize) throws OXException;

    /**
     * Scans the specified {@link ManagedFile}
     * 
     * @param managedFile The {@link ManagedFile} to scan
     * @param uniqueId an identifier that uniquely identifies the {@link InputStream} of the specified {@link ManagedFile}
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(ManagedFile managedFile, String uniqueId) throws OXException;

    /**
     * Whether the service can stream the data through the ICAP server
     * 
     * @return <code>true</code> if the data can be streamed through
     *         the ICAP server.
     */
    boolean canStream();


    /**
     * Checks whether the capability of the {@link AntiVirusService} is enabled
     * for the derived from the specified {@link Session}
     * 
     * @param session The groupware {@link Session}
     * @return <code>true</code> if the capability is enabled, <code>false</code> otherwise
     * @throws OXException if the capability for the specified user is not set, or if the service or
     *             capability for the specified user is disabled
     */
    boolean isEnabled(Session session) throws OXException;
}
