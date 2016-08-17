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


/**
 * {@link Readable} - Represents a readable resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Readable extends Closeable {

    /**
     * Reads up to <code>b.length</code> bytes of data from this file into an array of bytes. This method blocks until at least one byte of input is available.
     *
     * @param b The buffer into which the data is read.
     * @return The total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         this file has been reached.
     * @throws IOException If the first byte cannot be read for any reason
     *             other than end of file, or if the random access file has been closed, or if
     *             some other I/O error occurs.
     */
    int read(byte b[]) throws IOException;

    /**
     * Reads up to <code>len</code> bytes of data from this file into an array of bytes. This method blocks until at least one byte of input is available.
     *
     * @param b The buffer into which the data is read.
     * @param off The start offset in array <code>b</code>
     *            at which the data is written.
     * @param len The maximum number of bytes read.
     * @return The total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         the file has been reached.
     * @throws IOException If the first byte cannot be read for any reason
     *             other than end of file, or if the random access file has been closed, or if
     *             some other I/O error occurs.
     */
    int read(byte b[], int off, int len) throws IOException;

}
