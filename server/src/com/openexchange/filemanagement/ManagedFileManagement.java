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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.InputStream;

/**
 * {@link ManagedFileManagement} - The file management designed to keep large content as a temporary file on disk.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ManagedFileManagement {

    /**
     * Clears all files kept by this file management.
     */
    public void clear();

    /**
     * Creates a new managed file from specified input stream.
     * <p>
     * Size attribute is already set in returned managed file.
     * 
     * @param inputStream The input stream whose content is filled into newly created file
     * @return A new managed file
     * @throws ManagedFileException If a new managed file cannot be created from specified content
     */
    public ManagedFile createManagedFile(InputStream inputStream) throws ManagedFileException;

    /**
     * Creates a new managed file from specified bytes.
     * <p>
     * Size attribute is already set in returned managed file.
     * 
     * @param bytes The bytes which are filled into newly created file
     * @return A new managed file
     * @throws ManagedFileException If a new managed file cannot be created from specified content
     */
    public ManagedFile createManagedFile(byte[] bytes) throws ManagedFileException;

    /**
     * Gets an existing managed file by its unique ID and updates its last-accessed time stamp if found.
     * 
     * @param id The managed file's unique ID
     * @return The managed file associated with specified unique ID.
     * @throws ManagedFileException If no such managed file exists or cannot be returned
     */
    public ManagedFile getByID(String id) throws ManagedFileException;

    /**
     * Checks for an existing managed file of which unique ID matches given unique ID. If such a managed file is found, its last-accessed
     * time stamp is updated.
     * 
     * @param id The managed file's unique ID
     * @return <code>true</code> if such a managed file is found; otherwise <code>false</code>
     */
    public boolean contains(String id);

    /**
     * Manually removes an existing managed file by its unique ID.
     * 
     * @param id The managed file's unique ID
     * @throws ManagedFileException If managed file exists and cannot be removed
     */
    public void removeByID(String id) throws ManagedFileException;

    /**
     * Creates a possibly managed input stream from specified bytes with default capacity.
     * <p>
     * Bytes are kept in memory unless they exceed default capacity. If capacity is exceeded, bytes are turned into a managed file to reduce
     * memory consumption.
     * 
     * @param bytes The bytes held by this input stream
     * @throws ManagedFileException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    public InputStream createInputStream(final byte[] bytes) throws ManagedFileException;

    /**
     * Creates a possibly managed input stream from specified bytes.
     * <p>
     * Bytes are kept in memory unless they exceed specified capacity. If capacity is exceeded, bytes are turned into a managed file to
     * reduce memory consumption.
     * 
     * @param bytes The bytes held by this input stream
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @throws ManagedFileException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    public InputStream createInputStream(final byte[] bytes, final int capacity) throws ManagedFileException;

    /**
     * Creates a possibly managed input stream from specified input stream with default capacity and unknown stream size.
     * <p>
     * Stream's data is kept in memory unless it exceeds default capacity. If capacity is exceeded, stream's data is turned into a managed
     * file to reduce memory consumption.
     * 
     * @param in The input stream to manage
     * @throws ManagedFileException If an appropriate managed file cannot be created.
     */
    public InputStream createInputStream(final InputStream in) throws ManagedFileException;

    /**
     * Creates a possibly managed input stream from specified input stream with unknown stream size.
     * <p>
     * Stream's data is kept in memory unless it exceeds specified capacity. If capacity is exceeded, stream's data is turned into a managed
     * file to reduce memory consumption.
     * 
     * @param in The input stream to manage
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @throws ManagedFileException If an appropriate managed file cannot be created.
     */
    public InputStream createInputStream(final InputStream in, final int capacity) throws ManagedFileException;

    /**
     * Creates a possibly managed input stream from specified input stream.
     * <p>
     * OStream's data is kept in memory unless specified size exceeds specified capacity. If capacity is exceeded, stream's data is turned
     * into a managed file to reduce memory consumption.
     * 
     * @param in The input stream to manage
     * @param size The stream's size; leave to <code>-1</code> if unknown
     * @param capacity The number of bytes allowed being kept in memory rather than being spooled to disk.
     * @throws ManagedFileException If size exceeds memory limit and an appropriate managed file cannot be created.
     */
    public InputStream createInputStream(final InputStream in, final int size, final int capacity) throws ManagedFileException;
}
