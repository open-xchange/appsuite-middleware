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

package com.openexchange.mail.uuencode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
    public UUEncodedBodyPart(final String bodyPart) throws MessagingException {
        this(bodyPart, true);
    }

    private UUEncodedBodyPart(final String bodyPart, final boolean initialize) throws MessagingException {
        this.bodyPart = bodyPart;
        if (initialize && findUUEncodedAttachmentPosition()) {
            final ByteArrayInputStream bStream = new ByteArrayInputStream(bodyPart.substring(headerIndex, endIndex + 3).getBytes());
            bodyPartInputStream = MimeUtility.decode(bStream, "uuencode");
        } else {
            bodyPartInputStream = null;
        }
    }

    public static final boolean findUUEncodedAttachmentPosition(final String bodyPart) throws MessagingException {
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
            } catch (final NoSuchElementException nsee) {
                /*
                 * there are no more tokens in this tokenizer's string
                 */
                LOG.error("", nsee);
            } catch (final NumberFormatException nfe) {
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
