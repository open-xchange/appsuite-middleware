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

package com.openexchange.mail.uuencode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link UUEncodedMultiPart} - Find possible uuencoded attachments in "normal" text (like Outlook does) and converts them to
 * {@link UUEncodedPart} objects.
 *
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UUEncodedMultiPart {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UUEncodedMultiPart.class);

    private static final Pattern PAT_UUENCODED = Pattern.compile(
        "(^begin |\r?\nbegin )([0-7]{3} )(\\S[\\S\\p{Punct} \t]*\r?\n)(.+?)(\r?\n[ \t]*`?[ \t]*\r?\nend)",
        Pattern.DOTALL);

    /**
     * Check if specified content might be UUEncoded.
     *
     * @param content The content
     * @return <code>true</code> if UUEncoded; otherwise <code>false</code>
     */
    public static boolean isUUEncoded(String content) {
        return possiblyUUEncoded(content) && PAT_UUENCODED.matcher(content).find();
    }

    /**
     * Gets the uuencoded instance for given content
     *
     * @param content The content to parse
     * @return The uuencoded instance or <code>null</code>
     */
    public static UUEncodedMultiPart valueFor(String content) {
        if (false == possiblyUUEncoded(content)) {
            return null;
        }

        UUEncodedMultiPart uuencodedMP = new UUEncodedMultiPart(content);
        if (false == uuencodedMP.isUUEncoded()) {
            return null;
        }

        return uuencodedMP;
    }

    private static boolean possiblyUUEncoded(String content) {
        if (null == content) {
            return false;
        }

        int i = content.indexOf("begin ", 0);
        if (i < 0) {
            return false;
        }

        // Example: "begin 665 myfile.exe..."
        // So there are at least 10 characters between "begin" and "end" if uuencoded
        i += 10;
        return i < content.length() && content.indexOf("end", i) > 0;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private final List<UUEncodedPart> uuencodeParts;
    private String text;
    private int count = -1;

    /**
     * Initializes a new {@link UUEncodedMultiPart}
     */
    UUEncodedMultiPart() {
        super();
        uuencodeParts = new ArrayList<>();
    }

    /**
     * Initializes a new {@link UUEncodedMultiPart}
     *
     * @param content The text content which is possibly uuencoded
     */
    private UUEncodedMultiPart(String content) {
        this();
        setContent(content);
    }

    /**
     * A convenience method for setting this part's content.
     *
     * @param content Set the content of this UUEncodeMultiPart.
     */
    private final void setContent(String content) {
        findUUEncodedAttachmentCount(content);
        count = uuencodeParts.size();
        // now we should separate normal text from attachments
        if (count >= 1) {
            final UUEncodedPart uuencodedPart = uuencodeParts.get(0);
            if (uuencodedPart.getIndexStart() != -1) {
                text = content.substring(0, uuencodedPart.getIndexStart());
            }
        }
    }

    /**
     * Checks if content fed into this {@link UUEncodedMultiPart} instance is uuencoded.
     *
     * @return <code>true</code> if content is uuencoded, <code>false</code> otherwise
     */
    public boolean isUUEncoded() {
        return (count > 0);
    }

    /**
     * Try to find attachments recursive. Must containing the "begin" and "end" parameter, and specified tokens as well. Usually looks like:
     *
     * <pre>
     * begin 600 filename.doc
     * ...many data...
     * `
     * end
     * </pre>
     */
    private final void findUUEncodedAttachmentCount(String sBodyPart) {
        final Matcher m = PAT_UUENCODED.matcher(sBodyPart);
        while (m.find()) {
            try {
                final int skip = examineBeginToken(m.group(1));
                uuencodeParts.add(new UUEncodedPart(m.start(1) + skip, m.start(5), m.group().substring(skip), cleanAtom(m.group(3))));
            } catch (Exception e) {
                LOG.error("", e);
                break;
            }
        }
    }

    private static final int examineBeginToken(String beginToken) {
        int count = 0;
        for (char c = beginToken.charAt(count); Strings.isWhitespace(c);) {
            c = beginToken.charAt(++count);
        }
        return count;
    }

    private static final String cleanAtom(String atom) {
        int length = atom.length();
        if (length <= 0) {
            return atom;
        }
        
        StringBuilder sb = null;
        for (int i = 0; i < length; i++) {
            char c = atom.charAt(i);
            if (c == '\r' || c == '\n') {
                if (null == sb) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(atom, 0, i);
                    }
                }
            } else {
                if (null != sb) {
                    sb.append(c);
                }
            }
        }
        
        return null == sb ? atom : sb.toString();
    }

    /**
     * Return the "cleaned" text, without the content of the uuencoded attachments
     *
     * @return The "cleaned" text
     */
    public String getCleanText() {
        return text;
    }

    /**
     * Return the number of enclosed parts.
     *
     * @return number of parts
     */
    public int getCount() {
        return (count);
    }

    /**
     * Get the specified part. Parts are numbered starting at 0.
     *
     * @param index The index of the desired part
     * @return The part
     */
    public UUEncodedPart getBodyPart(int index) {
        return (uuencodeParts.get(index));
    }

    /**
     * Remove the part at specified location (starting from 0). Shifts all the parts after the removed part down one.
     *
     * @param index The index of the part to remove
     */
    public void removeBodyPart(int index) {
        uuencodeParts.remove(index);
    }

}
