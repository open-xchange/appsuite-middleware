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

import javax.mail.Session;

/**
 * {@link MimeSessionPropertyNames} - Provides string constants to set corresponding properties in an instance of {@link Session}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSessionPropertyNames {

    /**
     * Prevent instantiation
     */
    private MimeSessionPropertyNames() {
        super();
    }

    /**
     * If false, attempts to open a folder read/write will fail if the SELECT command succeeds but indicates that the folder is READ-ONLY.
     * This sometimes indicates that the folder contents can'tbe changed, but the flags are per-user and can be changed, such as might be
     * the case for public shared folders. If true, such open attempts will succeed, allowing the flags to be changed. The getMode method on
     * the Folder object will return Folder.READ_ONLY in this case even though the open method specified Folder.READ_WRITE. Default is
     * false.
     */
    public static final String PROP_ALLOWREADONLYSELECT = "mail.imap.allowreadonlyselect";

    /**
     * The mail.mime.charset System property can be used to specify the default MIME charset to use for encoded words and text parts that
     * don't otherwise specify a charset. Normally, the default MIME charset is derived from the default Java charset, as specified in the
     * file.encoding System property. Most applications will have no need to explicitly set the default MIME charset. In cases where the
     * default MIME charset to be used for mail messages is different than the charset used for files stored on the system, this property
     * should be set.
     */
    public static final String PROP_MAIL_MIME_CHARSET = "mail.mime.charset";

    /**
     * Timeout value in milliseconds for connection pool connections. Default is 45000 (45 seconds).
     */
    public static final String PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT = "mail.imap.connectionpooltimeout";

    /**
     * Maximum number of available connections in the connection pool. Default is 1.
     */
    public static final String PROP_MAIL_IMAP_CONNECTIONPOOLSIZE = "mail.imap.connectionpoolsize";

    /**
     * The mail.mime.decodetext.strict property controls decoding of MIME encoded words. The MIME spec requires that encoded words start at
     * the beginning of a whitespace separated word. Some mailers incorrectly include encoded words in the middle of a word. If the
     * mail.mime.decodetext.strict System property is set to "false", an attempt will be made to decode these illegal encoded words. The
     * default is true.
     */
    public static final String PROP_MAIL_MIME_DECODETEXT_STRICT = "mail.mime.decodetext.strict";

    /**
     * The mail.mime.encodeeol.strict property controls the choice of Content-Transfer-Encoding for MIME parts that are not of type "text".
     * Often such parts will contain textual data for which an encoding that allows normal end of line conventions is appropriate. In rare
     * cases, such a part will appear to contain entirely textual data, but will require an encoding that preserves CR and LF characters
     * without change. If the mail.mime.encodeeol.strict System property is set to "true", such an encoding will be used when necessary. The
     * default is false.
     */
    public static final String PROP_MAIL_MIME_ENCODEEOL_STRICT = "mail.mime.encodeeol.strict";

    /**
     * If set to "true", the BASE64 decoder will ignore errors in the encoded data, returning EOF. This may be useful when dealing with
     * improperly encoded messages that contain extraneous data at the end of the encoded stream. Note however that errors anywhere in the
     * stream will cause the decoder to stop decoding so this should be used with extreme caution. The default is false.
     */
    public static final String PROP_MAIL_MIME_BASE64_IGNOREERRORS = "mail.mime.base64.ignoreerrors";

    /**
     * The initial debug mode. Default is false.
     */
    public static final String PROP_MAIL_DEBUG = "mail.debug";

    /**
     * Normally, when writing out a MimeMultipart that contains no body parts, or when trying to parse a multipart message with no body
     * parts, a MessagingException is thrown. The MIME spec does not allow multipart content with no body parts. This System property may be
     * set to "true" to override this behavior. When writing out such a MimeMultipart, a single empty part will be included. When reading
     * such a multipart, a MimeMultipart will be created with no body parts. The default value of this property is false.
     */
    public static final Object PROP_MAIL_MIME_MULTIPART_ALLOWEMPTY = "mail.mime.multipart.allowempty";

}
