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

package com.openexchange.chat;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ChatExceptionMessages} - Exception messages for {@link OXException} that must be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ChatExceptionMessages implements LocalizableStrings {

    // An error occurred: %1$s
    public static final String ERROR_MSG = "An error occurred: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // Method is not supported.
    public static final String UNSUPPORTED_OPERATION_MSG = "Method is not supported.";

    // No account found with identifier "%1$s".
    public static final String ACCOUNT_NOT_FOUND_MSG = "No account found with identifier \"%1$s\".";

    // Invalid presence packet. A presence packet provides the status as well as the status message; e.g. "Online" + "At work"
    public static final String INVALID_PRESENCE_PACKET_MSG = "Invalid presence packet.";

    // A chat with identifier "%1$s" already exists.
    public static final String CHAT_ALREADY_EXISTS_MSG = "A chat with identifier \"%1$s\" already exists.";

    // Chat member "%1$s" already exists in chat "%2$s".
    public static final String CHAT_MEMBER_ALREADY_EXISTS_MSG = "Chat member \"%1$s\" already exists in chat \"%2$s\".";

    // No chat found with identifier "%1$s".
    public static final String CHAT_NOT_FOUND_MSG = "No chat found with identifier \"%1$s\".";

    // Unknown chat service: %1$s
    public static final String UNKNOWN_CHAT_SERVICE_MSG = "Unknown chat service: %1$s";

    // No message found with identifier "%1$s" in chat "%2$s".
    public static final String MESSAGE_NOT_FOUND_MSG = "No message found with identifier \"%1$s\" in chat \"%2$s\".";

    // No member found with identifier "%1$s".
    public static final String MEMBER_NOT_FOUND_MSG = "No member found with identifier \"%1$s\".";

    // Invalid conversation identifier: "%1$s"
    public static final String INVALID_CONVERSATION_ID_MSG = "Invalid chat identifier: \"%1$s\"";

    // Invalid roster identifier: "%1$s". A roster is the list of chat buddies/contacts that a chat user knows about and about whom he receives frequent updates about their presence/availability.
    public static final String INVALID_ROSTER_ID_MSG = "Invalid roster identifier: \"%1$s\"";

    // Posted message is too long.
    public static final String MESSAGE_TOO_LONG_MSG = "Posted message is too long.";

    // Subject is too long and is therefore truncated.
    public static final String SUBJECT_TOO_LONG_MSG = "Subject is too long and is therefore truncated.";

    /**
     * Prevent instantiation.
     */
    private ChatExceptionMessages() {
        super();
    }
}
