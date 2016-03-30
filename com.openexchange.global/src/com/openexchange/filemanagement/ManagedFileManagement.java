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

package com.openexchange.filemanagement;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link ManagedFileManagement} - The file management designed to keep large content as a temporary file on disk.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface ManagedFileManagement {

    /**
     * The idle time-to-live for a managed file.
     */
    public static final int TIME_TO_LIVE = 300000;

    /**
     * Clears all files kept by this file management.
     */
    void clear();

    /**
     * Creates a new temporary file.
     * <p>
     * If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and</li>
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.</li>
     * </ol>
     *
     * @return A new temporary file
     * @throws OXException If a temporary file could not be created
     */
    File newTempFile() throws OXException;

    /**
     * Creates a new temporary file.
     * <p>
     * If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and</li>
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.</li>
     * </ol>
     *
     * @param prefix The file prefix; e.g. <code>"open-xchange-"</code>
     * @param suffix The file suffix; e.g. <code>".tmp"</code>
     * @return A new temporary file
     * @throws OXException If a temporary file could not be created
     */
    File newTempFile(String prefix, String suffix) throws OXException;

    /**
     * Creates a new managed file from specified temporary file.
     *
     * @param temporaryFile A temporary file (previously obtained from {@link #newTempFile()}
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified temporary file
     */
    ManagedFile createManagedFile(File temporaryFile) throws OXException;

    /**
     * Creates a new managed file from specified temporary file.
     *
     * @param temporaryFile A temporary file (previously obtained from {@link #newTempFile()}
     * @param ttl The custom time-to-live or <code>-1</code> to use default one
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified temporary file
     */
    ManagedFile createManagedFile(File temporaryFile, int ttl) throws OXException;

    /**
     * Creates a new managed file from specified input stream.
     * <p>
     * Size attribute is already set in returned managed file.
     *
     * @param inputStream The input stream whose content is filled into newly created file
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(InputStream inputStream) throws OXException;

    /**
     * Creates a new managed file from specified input stream.
     * <p>
     * Size attribute is already set in returned managed file.
     *
     * @param inputStream The input stream whose content is filled into newly created file
     * @param distribute <code>true</code> to attempt to have created file remotely accessible for other cluster nodes, too; otherwise <code>false</code> for local-only
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(InputStream inputStream, boolean distribute) throws OXException;

    /**
     * Creates a new managed file from specified input stream.
     * <p>
     * Forces the ManagedFile to have the specified id (if not <code>null</code>).
     *
     * @param id The id or <code>null</code> to use a newly generated one
     * @param inputStream input stream whose content is filled into newly created file
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(String id, InputStream inputStream) throws OXException;

    /**
     * Creates a new managed file from specified input stream.
     * <p>
     * Forces the ManagedFile to have the specified id (if not <code>null</code>).
     *
     * @param id The id or <code>null</code> to use a newly generated one
     * @param inputStream input stream whose content is filled into newly created file
     * @param distribute <code>true</code> to attempt to have created file remotely accessible for other cluster nodes, too; otherwise <code>false</code> for local-only
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(String id, InputStream inputStream, boolean distribute) throws OXException;

    /**
     * Creates a new managed file from specified input stream with custom time-to-live.
     * <p>
     * Forces the ManagedFile to have the specified id (if not <code>null</code>).
     *
     * @param id The id or <code>null</code> to use a newly generated one
     * @param inputStream input stream whose content is filled into newly created file
     * @param ttl The custom time-to-live or <code>-1</code> to use default one
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(String id, InputStream inputStream, int ttl) throws OXException;

    /**
     * Creates a new managed file from specified input stream.
     * <p>
     * Size attribute is already set in returned managed file.
     *
     * @param inputStream The input stream whose content is filled into newly created file
     * @param optExtension An optional file extension; e.g. <code>".txt"</code>
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(InputStream inputStream, String optExtension) throws OXException;

    /**
     * Creates a new managed file from the specified input stream and with the specified TTL
     * 
     * @param inputStream The input stream whose content is filled into newly created file
     * @param optExtension An optional file extension; e.g. <code>".txt"</code>
     * @param @param ttl The custom time-to-live or <code>-1</code> to use default one
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(InputStream inputStream, String optExtension, int ttl) throws OXException;
    
    /**
     * Creates a new managed file from the specified input stream and with the specified TTL
     * 
     * @param inputStream The input stream whose content is filled into newly created file
     * @param @param ttl The custom time-to-live or <code>-1</code> to use default one
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(InputStream inputStream, int ttl) throws OXException;

    /**
     * Creates a new managed file from specified bytes.
     * <p>
     * Size attribute is already set in returned managed file.
     *
     * @param bytes The bytes which are filled into newly created file
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(byte[] bytes) throws OXException;

    /**
     * Creates a new managed file from specified bytes.
     * <p>
     * Size attribute is already set in returned managed file.
     *
     * @param bytes The bytes which are filled into newly created file
     * @param distribute <code>true</code> to attempt to have created file remotely accessible for other cluster nodes, too; otherwise <code>false</code> for local-only
     * @return A new managed file
     * @throws OXException If a new managed file cannot be created from specified content
     */
    ManagedFile createManagedFile(byte[] bytes, boolean distribute) throws OXException;

    /**
     * Gets all currently (<i>locally</i>) stored managed files.
     * <p>
     * Unlike {@link #getByID(String)} the last-accessed time stamp is <b>not</b> touched.
     *
     * @return All managed files
     * @throws OXException If listing all managed files fails
     */
    List<ManagedFile> getManagedFiles() throws OXException;

    /**
     * Gets those currently (<i>locally</i>) stored managed files that satisfy specified filter (if not <code>null</code>)
     * <p>
     * Unlike {@link #getByID(String)} the last-accessed time stamp is <b>not</b> touched.
     *
     * @param filter The optional filter
     * @return The filtered managed files
     * @throws OXException If returning managed files fails
     */
    List<ManagedFile> getManagedFiles(ManagedFileFilter filter) throws OXException;

    /**
     * Gets an existing managed file by its unique ID and updates its last-accessed time stamp if found.
     *
     * @param id The managed file's unique ID
     * @return The managed file associated with specified unique ID.
     * @throws OXException If no such managed file exists or cannot be returned
     */
    ManagedFile getByID(String id) throws OXException;

    /**
     * Gets an existing managed file by its unique ID and updates its last-accessed time stamp if found.
     *
     * @param id The managed file's unique ID
     * @return The managed file associated with specified unique ID or <code>null</code>
     */
    ManagedFile optByID(String id) throws OXException;

    /**
     * Checks for an existing managed file of which unique ID matches given unique ID. If such a managed file is found, its last-accessed
     * time stamp is updated.
     *
     * @param id The managed file's unique ID
     * @return <code>true</code> if such a managed file is found; otherwise <code>false</code>
     */
    boolean contains(String id);

    /**
     * Checks for an existing managed file of which unique ID matches given unique ID. If such a managed file is found, its last-accessed
     * time stamp is updated. The check is limited to this local Node.
     *
     * @param id The managed file's unique ID
     * @return <code>true</code> if such a managed file is found; otherwise <code>false</code>
     */
    boolean containsLocal(String id);

    /**
     * Manually removes an existing managed file by its unique ID.
     *
     * @param id The managed file's unique ID
     * @throws OXException If managed file exists and cannot be removed
     */
    void removeByID(String id) throws OXException;

    /**
     * Creates a possibly managed input stream from specified bytes with default capacity.
     * <p>
     * Bytes are kept in memory unless they exceed default capacity. If capacity is exceeded, bytes are turned into a managed file to reduce
     * memory consumption.
     *
     * @param bytes The bytes held by this input stream
     * @throws OXException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    InputStream createInputStream(final byte[] bytes) throws OXException;

    /**
     * Creates a possibly managed input stream from specified bytes.
     * <p>
     * Bytes are kept in memory unless they exceed specified capacity. If capacity is exceeded, bytes are turned into a managed file to
     * reduce memory consumption.
     *
     * @param bytes The bytes held by this input stream
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @throws OXException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    InputStream createInputStream(final byte[] bytes, final int capacity) throws OXException;

    /**
     * Creates a possibly managed input stream from specified input stream with default capacity and unknown stream size.
     * <p>
     * Stream's data is kept in memory unless it exceeds default capacity. If capacity is exceeded, stream's data is turned into a managed
     * file to reduce memory consumption.
     *
     * @param in The input stream to manage
     * @throws OXException If an appropriate managed file cannot be created.
     */
    InputStream createInputStream(final InputStream in) throws OXException;

    /**
     * Creates a possibly managed input stream from specified input stream with unknown stream size.
     * <p>
     * Stream's data is kept in memory unless it exceeds specified capacity. If capacity is exceeded, stream's data is turned into a managed
     * file to reduce memory consumption.
     *
     * @param in The input stream to manage
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @throws OXException If an appropriate managed file cannot be created.
     */
    InputStream createInputStream(final InputStream in, final int capacity) throws OXException;

    /**
     * Creates a possibly managed input stream from specified input stream.
     * <p>
     * OStream's data is kept in memory unless specified size exceeds specified capacity. If capacity is exceeded, stream's data is turned
     * into a managed file to reduce memory consumption.
     *
     * @param in The input stream to manage
     * @param size The stream's size; leave to <code>-1</code> if unknown
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @throws OXException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    InputStream createInputStream(final InputStream in, final int size, final int capacity) throws OXException;
}
