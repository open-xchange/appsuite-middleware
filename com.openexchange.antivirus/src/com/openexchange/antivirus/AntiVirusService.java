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
     * @param content The optional encapsulated HTTP content
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(InputStreamClosure stream, String uniqueId, long contentLength, AntiVirusEncapsulatedContent content) throws OXException;

    /**
     * Scans the contents of the specified {@link IFileHolder}
     * 
     * @param fileHolder The {@link IFileHolder}
     * @param uniqueId an identifier that uniquely identifies the {@link InputStream} of the specified {@link IFileHolder}
     * @param content The optional encapsulated HTTP content
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(IFileHolder fileHolder, String uniqueId, AntiVirusEncapsulatedContent content) throws OXException;

    /**
     * Scans the specified {@link File}
     * 
     * @param file The {@link File} to scan
     * @param uniqueId an identifier that uniquely identifies the specified {@link File}
     * @param fileSize The file's size in bytes
     * @param content The optional encapsulated HTTP content
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(File file, String uniqueId, long fileSize, AntiVirusEncapsulatedContent content) throws OXException;

    /**
     * Scans the specified {@link ManagedFile}
     * 
     * @param managedFile The {@link ManagedFile} to scan
     * @param uniqueId an identifier that uniquely identifies the {@link InputStream} of the specified {@link ManagedFile}
     * @param content The optional encapsulated HTTP content
     * @return the {@link AntiVirusResult}
     * @throws OXException if an error is occurred
     */
    AntiVirusResult scan(ManagedFile managedFile, String uniqueId, AntiVirusEncapsulatedContent content) throws OXException;

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
