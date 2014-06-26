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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote;

/**
 * {@link ActionCode}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ActionCode {

    /**
     * The close action.
     */
    CLOSE(1),
    /**
     * The commit action.
     */
    COMMIT(2),
    /**
     * The client-flush action.
     */
    CLIENT_FLUSH(4),
    /**
     * The custom action.
     */
    CUSTOM(5),
    /**
     * The custom action.
     */
    RESET(6),
    /**
     * The start action.
     */
    START(7),
    /**
     * The stop action.
     */
    STOP(8),
    /**
     * The webapp action.
     */
    WEBAPP(9),
    /**
     * Post request.
     */
    POST_REQUEST(10),
    /**
     * Request host name.
     */
    REQ_HOST_ATTRIBUTE(11),
    /**
     * Request host address.
     */
    REQ_HOST_ADDR_ATTRIBUTE(12),
    /**
     * Extract the SSL-related attributes.
     */
    REQ_REQ_SSL_ATTRIBUTE(13),
    /**
     * Create a new request.
     */
    REQ_NEW_REQUEST(14),
    /**
     * Extract the SSL-certificate (including forcing a re-handshake if necessary)
     */
    REQ_SSL_CERTIFICATE(15),
    /**
     * Socket remote port.
     */
    REQ_REMOTEPORT_ATTRIBUTE(16),
    /**
     * Socket local port.
     */
    REQ_LOCALPORT_ATTRIBUTE(17),
    /**
     * Local host address
     */
    REQ_LOCAL_ADDR_ATTRIBUTE(18),
    /**
     * Local host name.
     */
    REQ_LOCAL_NAME_ATTRIBUTE(19),
    /**
     * Callback for setting FORM auth body replay
     */
    REQ_SET_BODY_REPLAY(20),
    /**
     * Deliver a ping-intended message to client
     */
    CLIENT_PING(21),
    ;


//    /**
//     * Callback for begin Comet processing
//     */
//    public static final ActionCode ACTION_COMET_BEGIN = new ActionCode(21);
//
//    /**
//     * Callback for end Comet processing
//     */
//    public static final ActionCode ACTION_COMET_END = new ActionCode(22);
//
//    /**
//     * Callback for getting the amount of available bytes
//     */
//    public static final ActionCode ACTION_AVAILABLE = new ActionCode(23);
//
//    /**
//     * Callback for an asynchronous close of the Comet event
//     */
//    public static final ActionCode ACTION_COMET_CLOSE = new ActionCode(24);
//
//    /**
//     * Callback for setting the timeout asynchronously
//     */
//    public static final ActionCode ACTION_COMET_SETTIMEOUT = new ActionCode(25);

    private final int code;

    /**
     * Private constructor.
     */
    private ActionCode(final int code) {
        this.code = code;
    }

    /**
     * Action id, useable in switches and table indexes
     */
    public int getCode() {
        return code;
    }

}
