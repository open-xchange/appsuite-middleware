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

package com.openexchange.ajax.fileholder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link IFileHolder} - The container for binary content.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added {@link #close()} method
 */
public interface IFileHolder extends Closeable {

    /** Creates a new input stream to read from */
    interface InputStreamClosure {

        /**
         * Creates a new input stream to read from.
         *
         * @return The input stream
         * @throws OXException If input stream cannot be returned
         * @throws IOException If input stream cannot be returned
         */
        InputStream newStream() throws OXException, IOException;
    }

    /** Provides random access to a resource */
    interface RandomAccess extends Readable {

        /**
         * Sets the pointer offset, measured from the beginning of associated resource, at which the next read.
         *
         * @param pos The offset position, measured in bytes
         * @exception IOException If <code>pos</code> is less than
         *                <code>0</code> or if an I/O error occurs.
         */
        void seek(long pos) throws IOException;

        /**
         * Gets the length of the resource.
         *
         * @return The length measured in bytes.
         * @exception IOException If an I/O error occurs.
         */
        long length() throws IOException;
    }

    /** Creates a new random access to read from */
    interface RandomAccessClosure {

        /**
         * Creates a new random access to read from.
         *
         * @return The random access
         * @throws OXException If random access cannot be returned
         * @throws IOException If random access cannot be returned
         */
        RandomAccess newRandomAccess() throws OXException, IOException;
    }

    /**
     * Signals if this file holder is repetitive; meaning {@link #getStream()} yields a new {@link InputStream}.
     *
     * @return <code>true</code> if this file holder is repetitive; otherwise <code>false</code>
     */
    boolean repetitive();

    /**
     * Closes this file holder and releases any system resources associated with it. If the file holder is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    void close() throws IOException;

    /**
     * Gets the content's input stream.
     * <p>
     * <b>Note</b>: The {@link #close()} method is supposed being invoked in a wrapping <code>try-finally</code> block.
     *
     * @return The input stream
     * @throws OXException If input stream cannot be returned
     */
    InputStream getStream() throws OXException;

    /**
     * Gets the content's random access representation.
     * <p>
     * <b>Note</b>: The {@link #close()} method is supposed being invoked in a wrapping <code>try-finally</code> block.
     *
     * @return The random access representation or <code>null</code> if random access is not supported
     * @throws OXException If random access representation cannot be returned
     */
    RandomAccess getRandomAccess() throws OXException;

    /**
     * Gets the content's length.
     *
     * @return The content length or <code>-1</code> if unknown
     */
    long getLength();

    /**
     * Gets the content type.
     *
     * @return The content type or <code>null</code> if unknown
     */
    String getContentType();

    /**
     * Gets the name
     *
     * @return The name or <code>null</code> if unknown
     */
    String getName();

    /**
     * Gets the (optional) disposition.
     *
     * @return The disposition or <code>null</code>
     */
    String getDisposition();

    /**
     * Gets the delivery
     *
     * @return The delivery or <code>null</code>
     */
    String getDelivery();

    /**
     * Gets the optional post-processing tasks.
     *
     * @return The tasks
     */
    List<Runnable> getPostProcessingTasks();

    /**
     * Adds the specified post-processing task.
     *
     * @param task The task to add
     */
    void addPostProcessingTask(Runnable task);

}
