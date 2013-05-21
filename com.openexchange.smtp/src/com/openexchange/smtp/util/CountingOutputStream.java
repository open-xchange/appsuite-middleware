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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.smtp.util;

import java.io.OutputStream;
import org.apache.commons.io.output.ProxyOutputStream;

/**
 * {@link CountingOutputStream} - Aligned to {@link org.apache.commons.io.output.CountingOutputStream} with <code>synchronized</code> removed.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CountingOutputStream extends ProxyOutputStream {

    /** The count of bytes that have passed. */
    private long count = 0;

    /**
     * Constructs a new CountingOutputStream.
     * 
     * @param out the OutputStream to write to
     */
    public CountingOutputStream(OutputStream out) {
        super(out);
    }

    // -----------------------------------------------------------------------

    /**
     * Updates the count with the number of bytes that are being written.
     * 
     * @param n number of bytes to be written to the stream
     */
    @Override
    protected void beforeWrite(int n) {
        count += n;
    }

    // -----------------------------------------------------------------------
    /**
     * The number of bytes that have passed through this stream.
     * 
     * @return the number of bytes accumulated
     * @throws ArithmeticException if the byte count is too large
     */
    public int getCount() {
        long result = getByteCount();
        if (result > Integer.MAX_VALUE) {
            throw new ArithmeticException("The byte count " + result + " is too large to be converted to an int");
        }
        return (int) result;
    }

    /**
     * The number of bytes that have passed through this stream.
     * <p>
     * NOTE: This method is an alternative for <code>getCount()</code>. It was added because that method returns an integer which will
     * result in incorrect count for files over 2GB.
     * 
     * @return the number of bytes accumulated
     */
    public long getByteCount() {
        return this.count;
    }

}
