/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.mail.imap.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.ASCIIUtility;

/**
 * This class represents a response obtained from the input stream
 * of an IMAP server.
 *
 * @author  John Mani
 */

public class IMAPResponse extends Response {

    private final String key;
    private final int number;

    public IMAPResponse(Protocol c) throws IOException, ProtocolException {
	super(c);
	// continue parsing if this is an untagged response
	String key = null;
	int number = 0;
	if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
	    key = readAtom();
	    
	    // Is this response of the form "* <number> <command>"
	    int num = parseUnsignedInt(key);
	    if (num >= 0) {
	        number = num;
	        key = readAtom();
	    }
	}
	this.key = javax.mail.util.Interners.internCommandKey(key);
    this.number = number;
    }

    /**
     * Copy constructor.
     *
     * @param	r	the IMAPResponse to copy
     */
    public IMAPResponse(IMAPResponse r) {
	super((Response)r);
	key = r.key;
	number = r.number;
    }

    /**
     * For testing.
     *
     * @param	r	the response string
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     */
    public IMAPResponse(String r) throws IOException, ProtocolException {
	this(r, true);
    }

    /**
     * For testing.
     *
     * @param	r	the response string
     * @param	utf8	UTF-8 allowed?
     * @exception	IOException	for I/O errors
     * @exception	ProtocolException	for protocol failures
     * @since	JavaMail 1.6.0
     */
    public IMAPResponse(String r, boolean utf8)
				throws IOException, ProtocolException {
	super(r, utf8);
    // continue parsing if this is an untagged response
    String key = null;
    int number = 0;
    if (isUnTagged() && !isOK() && !isNO() && !isBAD() && !isBYE()) {
        key = readAtom();
        
        // Is this response of the form "* <number> <command>"
        int num = parseUnsignedInt(key);
        if (num >= 0) {
            number = num;
            key = readAtom();
        }
    }
    this.key = javax.mail.util.Interners.internCommandKey(key);
    this.number = number;
    }

    /**
     * Read a list of space-separated "flag-extension" sequences and 
     * return the list as a array of Strings. An empty list is returned
     * as null.  Each item is expected to be an atom, possibly preceeded
     * by a backslash, but we aren't that strict; we just look for strings
     * separated by spaces and terminated by a right paren.  We assume items
     * are always ASCII.
     *
     * @return	the list items as a String array
     */
    public String[] readSimpleList() {
	skipSpaces();

	if (buffer[index] != '(') // not what we expected
	    return null;
	index++; // skip '('

	List<String> v = new ArrayList<>();
	int start;
	for (start = index; buffer[index] != ')'; index++) {
	    if (buffer[index] == ' ') { // got one item
		v.add(ASCIIUtility.toString(buffer, start, index));
		start = index+1; // index gets incremented at the top
	    }
	}
	if (index > start) // get the last item
	    v.add(ASCIIUtility.toString(buffer, start, index));
	index++; // skip ')'
	
	int size = v.size();
	if (size > 0)
	    return v.toArray(new String[size]);
	else  // empty list
	    return null;
    }

    public String getKey() {
	return key;
    }

    public boolean keyEquals(String k) {
	if (key != null && key.equalsIgnoreCase(k))
	    return true;
	else
	    return false;
    }

    public int getNumber() {
	return number;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------
    
    /** The radix for base <code>10</code>. */
    private static final int RADIX = 10;
    private static final int INT_LIMIT = -Integer.MAX_VALUE;
    private static final int INT_MULTMIN = INT_LIMIT / RADIX;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    private static final int parseUnsignedInt(String s) {
        if (s == null) {
            return -1;
        }

        final int max = s.length();

        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        int result = 0;
        int i = 0;

        int digit = digit(s.charAt(i++));
        if (digit < 0) {
            return -1;
        }
        result = -digit;

        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < INT_MULTMIN) {
                return -1;
            }
            result *= RADIX;
            if (result < INT_LIMIT + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    private static int digit(final char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
    }

}
