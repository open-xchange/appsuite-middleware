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

import java.nio.charset.StandardCharsets;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.FilterResult;
import com.sun.mail.util.ASCIIUtility;

/**
 * This class represents a FILTERED response obtained from the input stream
 * of an IMAP server.
 *
 * @author Thorben Bettem
 */

public class FilteredResponse extends IMAPResponse {

    private String tagReference;
    private final FilterResult filterResult;

    /**
     * Construct a FilteredResponse.
     *
     * @param r the IMAP response
     * @exception ProtocolException for protocol failures
     */
    public FilteredResponse(IMAPResponse r) throws ProtocolException {
        super(r);
        filterResult = parse();
        buffer = null; // Not needed anymore as completely parsed by now
    }

    private final static char[] TAG = { 'T', 'A', 'G' };
    private final static char[] FILTER_RESULT_OK = { 'O', 'K' };
    private final static char[] FILTER_RESULT_ERRORS = { 'E', 'R', 'R', 'O', 'R', 'S' };
    private final static char[] FILTER_RESULT_WARNINGS = { 'W', 'A', 'R', 'N', 'I', 'N', 'G', 'S' };

    private FilterResult parse() throws ParsingException {
        if (!isNextNonSpace('(')) {
            throw new ParsingException("error in FILTERED parsing, missing '(' at index " + index);
        }

        skipSpaces();

        if (false == match(TAG)) {
            throw new ParsingException("error in FILTERED parsing, missing 'TAG' at index " + index);
        }
        skipSpaces();
        tagReference = readAtomString();

        if (!isNextNonSpace(')')) {
            throw new ParsingException("error in FILTERED parsing, missing ')' at index " + index);
        }

        UID uid = null;
        do {
            skipSpaces();
            switch (buffer[index]) {
                case 'O': case 'o':
                    if (match(FILTER_RESULT_OK)) {
                        // End
                        return FilterResult.okResult(null == uid ? -1 : uid.uid);
                    }
                    throw new ParsingException("error in FILTERED parsing, unrecognized item at index " + index + ", starts with \"" + next20() + "\"");
                case 'E': case 'e':
                    if (match(FILTER_RESULT_ERRORS)) {
                        String errors = readString();
                        return FilterResult.errorsResult(errors, null == uid ? -1 : uid.uid);
                    }
                    throw new ParsingException("error in FILTERED parsing, unrecognized item at index " + index + ", starts with \"" + next20() + "\"");
                case 'W': case 'w':
                    if (match(FILTER_RESULT_WARNINGS)) {
                        String warnings = readString();
                        return FilterResult.warningsResult(warnings, null == uid ? -1 : uid.uid);
                    }
                    throw new ParsingException("error in FILTERED parsing, unrecognized item at index " + index + ", starts with \"" + next20() + "\"");
                case 'U': case 'u':
                    if (match(UID.name)) {
                        uid = new UID(this);
                    } else {
                        throw new ParsingException("error in FILTERED parsing, unrecognized item at index " + index + ", starts with \"" + next20() + "\"");
                    }
            }
        } while (true);
    }

    /**
     * Return the next 20 characters in the buffer, for exception messages.
     */
    private String next20() {
        if (index + 20 > size) {
            return ASCIIUtility.toString(buffer, index, size);
        } else {
            return ASCIIUtility.toString(buffer, index, index + 20) + "...";
        }
    }

    /**
     * Does the current buffer match the given item name?
     * itemName is the name of the IMAP item to compare against.
     * NOTE that itemName *must* be all uppercase.
     * If the match is successful, the buffer pointer (index)
     * is incremented past the matched item.
     */
    private boolean match(char[] itemName) {
        int len = itemName.length;
        for (int i = 0, j = index; i < len;) {
            // IMAP tokens are case-insensitive. We store itemNames in
            // uppercase, so convert operand to uppercase before comparing.
            if (Character.toUpperCase((char) buffer[j++]) != itemName[i++]) {
                return false;
            }
        }
        index += len;
        return true;
    }

    /**
     * Gets the tag reference
     *
     * @return The tag reference
     */
    public String getTagReference() {
        return tagReference;
    }

    /**
     * Gets the filter result
     *
     * @return The filter result
     */
    public FilterResult getFilterResult() {
        return filterResult;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32).append("* FILTERED ");
        sb.append("(TAG ").append(tagReference).append(") ");
        long uid = filterResult.getUid();
        if (uid >= 0) {
            sb.append("UID ").append(uid).append(' ');
        }

        String errors = filterResult.getErrors();
        if (null != errors) {
            int numBytes = errors.getBytes(StandardCharsets.UTF_8).length;
            sb.append("ERRORS {").append(numBytes).append(numBytes > 0 ? "+" : "").append("}").append("\r\n");
            sb.append(errors);
            return sb.toString();
        }

        String warnings = filterResult.getWarnings();
        if (null != warnings) {
            int numBytes = warnings.getBytes(StandardCharsets.UTF_8).length;
            sb.append("WARNINGS {").append(numBytes).append(numBytes > 0 ? "+" : "").append("}").append("\r\n");
            sb.append(warnings);
            return sb.toString();
        }

        return sb.append("OK").toString();
    }

}
