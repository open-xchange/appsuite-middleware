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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.mime;

/**
 * {@link MimeTypes} - Constants for MIME types.
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
}
