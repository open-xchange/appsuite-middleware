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

package com.openexchange.file.storage;

import java.net.MalformedURLException;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;


/**
 * {@link FileStorageUtility} - Utility class for file storage module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageUtility {

    /**
     * Initializes a new {@link FileStorageUtility}.
     */
    private FileStorageUtility() {
        super();
    }

    /**
     * Generates the ETag for given file meta data.
     *
     * @param fileMetadata The file meta data
     * @return The Etag
     */
    public static String getETagFor(final File fileMetadata) {
        return getETagFor(fileMetadata.getId(), fileMetadata.getVersion());
    }

    /**
     * Generates the ETag for given file meta data.
     *
     * @param id The file identifier
     * @param version The optional version
     * @return The Etag
     */
    public static String getETagFor(final String id, final String version) {
        final StringAllocator sb = new StringAllocator("http://www.open-xchange.com/infostore");
        if (null != id) {
            sb.append('/').append(id);
        }
        if (null != version) {
            sb.append('/').append(version);
        }
        return sb.toString();
    }

    /**
     * Checks given file's URL string for syntactical correctness.
     *
     * @param file The file whose URL to cehck
     * @throws OXException If URL string is invalid
     */
    public static void checkUrl(final File file) throws OXException {
        checkUrl(file.getURL());
    }

    /**
     * Checks given URL string for syntactical correctness.
     *
     * @param sUrl The URL string
     * @throws OXException If URL string is invalid
     */
    public static void checkUrl(final String sUrl) throws OXException {
        if (isEmpty(sUrl)) {
            // Nothing to check
            return;
        }
        try {
            final java.net.URL url = new java.net.URL(sUrl);
            final String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new MalformedURLException("Only http & https protocols supported.");
            }
        } catch (final MalformedURLException e) {
            throw FileStorageExceptionCodes.INVALID_URL.create(e, sUrl, e.getMessage());
        }
    }

    /** Checks for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
