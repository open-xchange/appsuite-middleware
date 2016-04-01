/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.iap;

import java.io.ByteArrayInputStream;

/**
 * A simple wrapper around a byte array, with a start position and
 * count of bytes.
 *
 * @author  John Mani
 */

public class ByteArray {

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private byte[] bytes; // the byte array
    private int start;	  // start position
    private int count;	  // count of bytes

    /**
     * Constructor
     *
     * @param	b	the byte array to wrap
     * @param	start	start position in byte array
     * @param	count	number of bytes in byte array
     */
    public ByteArray(byte[] b, int start, int count) {
	bytes = b;
	this.start = start;
	this.count = count;
    }

    /**
     * Constructor that creates a byte array of the specified size.
     *
     * @param	size	the size of the ByteArray
     * @since	JavaMail 1.4.1
     */
    public ByteArray(int size) {
	this(new byte[size], 0, size);
    }

    /**
     * Returns the internal byte array. Note that this is a live
     * reference to the actual data, not a copy.
     *
     * @return	the wrapped byte array
     */
    public byte[] getBytes() {
	return bytes;
    }

    /**
     * Returns a new byte array that is a copy of the data.
     *
     * @return	a new byte array with the bytes from start for count
     */
    public byte[] getNewBytes() {
	byte[] b = new byte[count];
	System.arraycopy(bytes, start, b, 0, count);
	return b;
    }

    /**
     * Returns the start position
     *
     * @return	the start position
     */
    public int getStart() {
	return start;
    }

    /**
     * Returns the count of bytes
     *
     * @return	the number of bytes
     */
    public int getCount() {
	return count;
    }

    /**
     * Set the count of bytes.
     *
     * @param	count	the number of bytes
     * @since	JavaMail 1.4.1
     */
    public void setCount(int count) {
	this.count = count;
	if (bytes.length - count > 4096) {
	    // Shrink to fitting size
	    byte[] b = new byte[count];
	    System.arraycopy(bytes, start, b, 0, count);
	    bytes = b;
	    start = 0;
    }
    }

    /**
     * Returns a ByteArrayInputStream.
     *
     * @return	the ByteArrayInputStream
     */
    public ByteArrayInputStream toByteArrayInputStream() {
	return new ByteArrayInputStream(bytes, start, count);
    }

    /**
     * Grow the byte array by incr bytes.
     *
     * @param	incr	how much to grow
     * @since	JavaMail 1.4.1
     */
    public void grow(int incr) {
	byte[] nbuf = new byte[bytes.length + incr];
	System.arraycopy(bytes, 0, nbuf, 0, bytes.length);
	bytes = nbuf;
    }

    /**
     * Grow the byte array to have its size least set to specified <code>minCapacity</code>.
     *
     * @param minCapacity The minimum capacity to set
     */
    public void growMin(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            // minCapacity is usually close to size, so this is a win:
            byte[] nbuf = new byte[newCapacity];
            System.arraycopy(bytes, 0, nbuf, 0, bytes.length);
            bytes = nbuf;
        }
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    @Override
    public String toString() {
        return new String(bytes);
    }

}
