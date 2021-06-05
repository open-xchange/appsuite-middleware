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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

public class UUEncodedBodyPart {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UUEncodedBodyPart.class);

    private static final String BEGIN = "begin";

    private static final String END = "end";

    private static final char LINE_SEPARATOR = '\n';

    private final InputStream bodyPartInputStream;

    private final String bodyPart;

    private String fileName = null;

    private int headerIndex = -1, endIndex = -1, fileSize = -1;

    /**
     * Creates a new <code>UUEncodedBodyPart</code> instance with given part content
     */
    public UUEncodedBodyPart(String bodyPart) throws MessagingException {
        this(bodyPart, true);
    }

    private UUEncodedBodyPart(String bodyPart, boolean initialize) throws MessagingException {
        this.bodyPart = bodyPart;
        if (initialize && findUUEncodedAttachmentPosition()) {
            final ByteArrayInputStream bStream = new ByteArrayInputStream(bodyPart.substring(headerIndex, endIndex + 3).getBytes(StandardCharsets.ISO_8859_1));
            bodyPartInputStream = MimeUtility.decode(bStream, "uuencode");
        } else {
            bodyPartInputStream = null;
        }
    }

    public static final boolean findUUEncodedAttachmentPosition(String bodyPart) throws MessagingException {
        return new UUEncodedBodyPart(bodyPart, false).findUUEncodedAttachmentPosition();
    }

    /**
     * @return <code>true</code> if part content is uuencoded, otherwise <code>false</code>
     */
    private boolean findUUEncodedAttachmentPosition() {
        int beginIndex = -1;
        final String sSearch = bodyPart;
        if ((beginIndex = sSearch.lastIndexOf(BEGIN)) != -1) {
            final int eolIndex = sSearch.indexOf(LINE_SEPARATOR, beginIndex);
            final String possibleHeader = sSearch.substring(beginIndex, eolIndex);
            final StringTokenizer st = new StringTokenizer(possibleHeader);
            String possibleFileSize;
            st.nextToken();
            try {
                possibleFileSize = st.nextToken();
                fileSize = Integer.parseInt(possibleFileSize);
                fileName = st.nextToken();
                /*
                 * now we know we have a UUencode header
                 */
                headerIndex = beginIndex;
                endIndex = sSearch.indexOf(END, beginIndex);
                return true;
            } catch (NoSuchElementException nsee) {
                /*
                 * there are no more tokens in this tokenizer's string
                 */
                LOG.error("", nsee);
            } catch (NumberFormatException nfe) {
                /*
                 * possibleFileSize was non-numeric
                 */
                LOG.error("", nfe);
            }
        }
        return false;
    }

    /**
     * Gets the fileName attribute of the UUEncodedBodyPart object
     *
     * @return The fileName value
     */
    public String getFileName() {
        return (fileName);
    }

    /**
     * Gets the inputStream attribute of the UUEncodedBodyPart object
     *
     * @return The inputStream value
     */
    public InputStream getInputStream() {
        return (bodyPartInputStream);
    }

    /**
     * Gets the file size attribute of the UUEncodedBodyPart object
     *
     * @return The file size value
     */
    public int getFileSize() {
        return fileSize;
    }

}
