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

package com.openexchange.service.messaging;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MessagingServiceExceptionMessages} - Exception messages for {@link OXException} that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.22
 */
public final class MessagingServiceExceptionMessages implements LocalizableStrings {

    // An error occurred: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // The IP address of host %1$s could not be determined.
    public static final String UNKNOWN_HOST_MSG = "The IP address of host %1$s could not be determined.";

    // Integer value is too big: %1$s
    public static final String INT_TOO_BIG_MSG = "Integer value is too big: %1$s";

    // Missing previous truncated message package(s).
    public static final String MISSING_PREV_PACKAGE_MSG = "Missing previous truncated message package(s).";

    // Conflicting truncated message package(s).
    public static final String CONFLICTING_TRUNCATED_PACKAGES_MSG = "Conflicting truncated message package(s).";

    // Missing or wrong magic bytes: %1$s
    public static final String BROKEN_MAGIC_BYTES_MSG = "Missing or wrong magic bytes: %1$s";

    // Unknown prefix code: %1$s
    public static final String UNKNOWN_PREFIX_CODE_MSG = "Unknown prefix code: %1$s";

    // Invalid message package
    public static final String INVALID_MSG_PACKAGE_MSG = "Invalid message package";

    // Unparseable string.
    public static final String UNPARSEABLE_STRING_MSG = "Unparsable string.";

    // Invalid quoted-printable encoding.
    public static final String INVALID_QUOTED_PRINTABLE_MSG = "Invalid quoted-printable encoding.";

    // Messaging server socket could not be bound to port %1$d. Probably another process is already listening on this port.
    public static final String BIND_ERROR_MSG = "Messaging server socket could not be bound to port %1$d. Probably another process is already listening on this port.";

    /**
     * Initializes a new {@link MessagingServiceExceptionMessages}.
     */
    private MessagingServiceExceptionMessages() {
        super();
    }

}
