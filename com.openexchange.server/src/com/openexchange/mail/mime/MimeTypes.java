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

package com.openexchange.mail.mime;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link MimeTypes} - Utilities & constants for MIME types.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeTypes {

    /**
     * No instantiation
     */
    private MimeTypes() {
        super();
    }

    /**
     * The default MIME type for rfc822 messages: <code>text/plain; charset=us-ascii</code>
     */
    public static final String MIME_DEFAULT = "text/plain; charset=us-ascii";

    /**
     * text/plain
     */
    public static final String MIME_TEXT_PLAIN = "text/plain";

    /**
     * text/plain; charset=#CS#
     */
    public static final String MIME_TEXT_PLAIN_TEMPL = "text/plain; charset=#CS#";

    /**
     * text/&#42;
     */
    public static final String MIME_TEXT_ALL = "text/*";

    /**
     * text/htm&#42;
     */
    public static final String MIME_TEXT_HTM_ALL = "text/htm*";

    /**
     * text/html
     */
    public static final String MIME_TEXT_HTML = "text/html";

    /**
     * multipart/mixed
     */
    public static final String MIME_MULTIPART_MIXED = "multipart/mixed";

    /**
     * multipart/alternative
     */
    public static final String MIME_MULTIPART_ALTERNATIVE = "multipart/alternative";

    /**
     * multipart/octet-stream
     */
    public static final String MIME_MULTIPART_OCTET = "multipart/octet-stream";

    /**
     * multipart/related
     */
    public static final String MIME_MULTIPART_RELATED = "multipart/related";

    /**
     * multipart/&#42;
     */
    public static final String MIME_MULTIPART_ALL = "multipart/*";

    /**
     * message/rfc822
     */
    public static final String MIME_MESSAGE_RFC822 = "message/rfc822";

    /**
     * text/calendar
     */
    public static final String MIME_TEXT_CALENDAR = "text/calendar";

    /**
     * text/x-vCalendar
     */
    public static final String MIME_TEXT_X_VCALENDAR = "text/x-vcalendar";

    /**
     * text/vcard
     */
    public static final String MIME_TEXT_VCARD = "text/vcard";

    /**
     * text/x-vcard
     */
    public static final String MIME_TEXT_X_VCARD = "text/x-vcard";

    /**
     * application/octet-stream
     */
    public static final String MIME_APPL_OCTET = "application/octet-stream";

    /**
     * application/&#42;
     */
    public static final String MIME_APPL_ALL = "application/*";

    /**
     * text/enriched
     */
    public static final String MIME_TEXT_ENRICHED = "text/enriched";

    /**
     * text/rtf
     */
    public static final String MIME_TEXT_RTF = "text/rtf";

    /**
     * text/richtext
     */
    public static final String MIME_TEXT_RICHTEXT = "text/richtext";

    /**
     * text/rfc822-headers
     */
    public static final String MIME_TEXT_RFC822_HDRS = "text/rfc822-headers";

    /**
     * text/&#42;card
     */
    public static final String MIME_TEXT_ALL_CARD = "text/*card";

    /**
     * text/&#42;calendar
     */
    public static final String MIME_TEXT_ALL_CALENDAR = "text/*calendar";

    /**
     * application/ics
     */
    public static final String MIME_APPLICATION_ICS = "application/ics";

    /**
     * image/&#42;
     */
    public static final String MIME_IMAGE_ALL = "image/*";

    /**
     * message/delivery-status
     */
    public static final String MIME_MESSAGE_DELIVERY_STATUS = "message/delivery-status";

    /**
     * message/disposition-notification
     */
    public static final String MIME_MESSAGE_DISP_NOTIFICATION = "message/disposition-notification";

    /**
     * application/pgp-signature
     */
    public static final String MIME_PGP_SIGN = "application/pgp-signature";

    /**
     * application/octet-stream as unknown
     */
    public static final String MIME_UNKNOWN = MIME_APPL_OCTET;

    /**
     * The set of such MIME types that are considered as invalid (e.g. for file upload attempts)
     */
    public static final Set<String> INVALIDS = ImmutableSet.of(
        "application/octet-stream",
        "application/force-download",
        "application/binary",
        "application/x-download",
        "application/vnd",
        "application/vnd.ms-word.document.12n",
        "application/vnd.ms-word.document.12",
        "application/vnd.ms-word",
        "application/odt",
        "application/x-pdf");

    public static final Set<String> ATTACHMENTS = ImmutableSet.of(
        "message/rfc822");

    // --------------------------------------------------------------------------------------------------------

    /**
     * Extracts the primary type from specified MIME type string.
     *
     * @param contentType The MIME type string
     * @return The primary type or string itself
     */
    public static String getPrimaryType(String contentType) {
        if (isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf('/');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    /**
     * Extracts the file name prefix from specified file name.
     *
     * @param fileName The file name; e.g. <code>"image001.jpg"</code>
     * @return The file extension (e.g. <code>"image001"</code>) or <code>fileName</code> itself
     */
    public static String getFilePrefix(String fileName) {
        if (isEmpty(fileName)) {
            return fileName;
        }
        final int pos = fileName.lastIndexOf('.');
        return pos > 0 && pos < fileName.length() ? fileName.substring(0, pos) : fileName;
    }

    /**
     * Extracts the file name extension from specified file name.
     *
     * @param fileName The file name; e.g. <code>"image001.jpg"</code>
     * @return The file extension (e.g. <code>"jpg"</code>) or <code>fileName</code> itself
     */
    public static String getFileExtension(String fileName) {
        if (isEmpty(fileName)) {
            return fileName;
        }
        final int pos = fileName.lastIndexOf('.');
        return pos > 0 && pos < fileName.length() ? fileName.substring(pos) : fileName;
    }

    /**
     * Extracts the base type from specified MIME type.
     *
     * @param mimeType The MIME type; e.g. <code>"text/plain; name=doc001.txt; charset=us-ascii"</code>
     * @return The base type (e.g. <code>"text/plain"</code>)
     */
    public static String getBaseType(String mimeType) {
        if (isEmpty(mimeType)) {
            return null;
        }
        final int pos = mimeType.indexOf(';');
        if (pos <= 0) {
            return mimeType;
        }
        return pos < mimeType.length() ? mimeType.substring(0, pos).trim() : mimeType;
    }

    /**
     * Extracts the parameter list from specified MIME type.
     *
     * @param mimeType The MIME type; e.g. <code>"text/plain; name=doc001.txt; charset=us-ascii"</code>
     * @return The parameter list (e.g. <code>"; name=doc001.txt; charset=us-ascii"</code>) or <code>null</code>
     */
    public static String getParameterList(String mimeType) {
        if (isEmpty(mimeType)) {
            return null;
        }
        final int pos = mimeType.indexOf(';');
        return pos > 0 && pos < mimeType.length() ? mimeType.substring(pos).trim() : null;
    }

    /**
     * Checks if the primary types of specified MIME type strings are equal;<br>
     * e.g. <code><i>application</i>/...</code>, or <code><i>image</i>/...</code>
     *
     * @param contentType1 The first MIME type string
     * @param contentType2 The second MIME type string
     * @return <code>true</code> if primary types are equal; otherwise <code>false</code>
     */
    public static boolean equalPrimaryTypes(String contentType1, String contentType2) {
        if (null == contentType1 || null == contentType2) {
            return false;
        }
        return toLowerCase(getPrimaryType(contentType1)).startsWith(toLowerCase(getPrimaryType(contentType2)));
    }

    /**
     * Gets the checked MIME type by specified file name.
     * <p>
     * That is the file name should dictate/dominate the considered MIME type for associated file/attachment.
     *
     * @param givenMimeType The given MIME type (as indicated by client)
     * @param fileName The file name
     * @return The checked MIME type
     * @see #checkedMimeType(String, String, Set)
     * @see #INVALIDS
     */
    public static String checkedMimeType(String givenMimeType, String fileName) {
        return checkedMimeType(givenMimeType, fileName, null);
    }

    /**
     * Determines if the given mimetype is considered to be an attachment.
     *
     * @param mimeType, like <type>/<subtype>
     * @return true: if it is an attachment type, false: otherwise
     */
    public static boolean isConsideredAttachment(String mimeType) {
        return ATTACHMENTS.contains(mimeType.toLowerCase());
    }

    /**
     * Gets the checked MIME type by specified file name.
     * <p>
     * That is the file name should dictate/dominate the considered MIME type for associated file/attachment.
     *
     * @param givenMimeType The given MIME type (as indicated by client)
     * @param fileName The file name
     * @param invalids The set of such MIME types that shall be considered as invalid
     * @return The checked MIME type
     * @see #INVALIDS
     */
    public static String checkedMimeType(String givenMimeType, String fileName, Set<String> invalids) {
        if (isEmpty(fileName)) {
            return givenMimeType;
        }
        if (isEmpty(givenMimeType)) {
            return MimeType2ExtMap.getContentType(fileName);
        }
        String contentTypeByFileName = MimeType2ExtMap.getContentType(fileName, null);
        if ((null == contentTypeByFileName || equalPrimaryTypes(givenMimeType, contentTypeByFileName)) && !consideredAsInvalid(givenMimeType, invalids)) {
            // Unknown or MIME types do match
            return givenMimeType;
        }
        final String parameterList = getParameterList(givenMimeType);
        if (contentTypeByFileName == null) {
            // Fallback to application/octet-stream
            contentTypeByFileName = "application/octet-stream";
        }
        return isEmpty(parameterList) ? contentTypeByFileName : new StringBuilder(contentTypeByFileName).append(parameterList).toString();
    }

    private static boolean consideredAsInvalid(String givenMimeType, Set<String> invalids) {
        return null == invalids || invalids.isEmpty() ? false : invalids.contains(toLowerCase(getBaseType(givenMimeType)));
    }

    /**
     * Gets the checked file name by specified base MIME type.
     * <p>
     * That is the MIME type should dictate/dominate the considered file name for associated file/attachment.
     *
     * @param givenFileName The given file name (as indicated by client)
     * @param baseType The base MIME type
     * @return The checked file name
     */
    public static String checkedFileName(String givenFileName, String baseType) {
        if (isEmpty(baseType)) {
            return givenFileName;
        }
        final String contentTypeByFileName = MimeType2ExtMap.getContentType(givenFileName, null);
        if (null == contentTypeByFileName || equalPrimaryTypes(baseType, contentTypeByFileName)) {
            // Unknown or MIME types do match
            return givenFileName;
        }
        final String fileExtension = MimeType2ExtMap.getFileExtension(baseType);
        if ("dat".equals(fileExtension)) {
            // Unknown
            return givenFileName;
        }
        final String filePrefix = getFilePrefix(givenFileName);
        return new StringBuilder(isEmpty(filePrefix) ? "file" : filePrefix).append('.').append(fileExtension).toString();
    }

}
