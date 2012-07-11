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

package com.openexchange.push.udp;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link PushUDPExceptionMessage}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class PushUDPExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link PushUDPExceptionMessage}.
     */
    private PushUDPExceptionMessage() {
        super();
    }

    /**
     * Push UDP Exception.
     */
    public final static String PUSH_UDP_EXCEPTION_MSG = "Push UDP Exception.";

    /**
     * Missing Push UDP configuration.
     */
    public final static String MISSING_CONFIG_MSG = "Missing Push UDP configuration.";

    /**
     * User ID is not a number: %1$s.
     */
    public final static String USER_ID_NAN_MSG = "User ID is not a number: %1$s.";

    /**
     * Context ID is not a number: %1$s.
     */
    public final static String CONTEXT_ID_NAN_MSG = "Context ID is not a number: %1$s.";

    /**
     * Magic bytes are not a number: %1$s.
     */
    public final static String MAGIC_NAN_MSG = "Magic bytes are not a number: %1$s.";

    /**
     * Invalid Magic bytes: %1$s.
     */
    public final static String INVALID_MAGIC_MSG = "Invalid Magic bytes: %1$s.";

    /**
     * Folder ID is not a number: %1$s.
     */
    public final static String FOLDER_ID_NAN_MSG = "Folder ID is not a number: %1$s.";

    /**
     * Module is not a number: %1$s.
     */
    public final static String MODULE_NAN_MSG = "Module is not a number: %1$s.";

    /**
     * Port is not a number: %1$s.
     */
    public final static String PORT_NAN_MSG = "Port is not a number: %1$s.";

    /**
     * Request type is not a number: %1$s.
     */
    public final static String TYPE_NAN_MSG = "Request type is not a number: %1$s.";

    /**
     * Length is not a number: %1$s.
     */
    public final static String LENGTH_NAN_MSG = "Length is not a number: %1$s.";

    /**
     * Invalid user IDs: %1$s.
     */
    public final static String INVALID_USER_IDS_MSG = "Invalid user IDs: %1$s.";

    /**
     * Unknown request type: %1$s.
     */
    public final static String INVALID_TYPE_MSG = "Unknown request type: %1$s.";

    /**
     * Missing payload in datagram package.
     */
    public final static String MISSING_PAYLOAD_MSG = "Missing payload in datagram package.";

    /**
     * No UDP channel is configured.
     */
    public final static String NO_CHANNEL_MSG = "No UDP channel is configured.";

}
