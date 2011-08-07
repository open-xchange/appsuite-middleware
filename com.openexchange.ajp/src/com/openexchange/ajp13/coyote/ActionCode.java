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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
public final class ActionCode {

    // -------------------------------------------------------------- Constants

    public static final ActionCode ACTION_ACK = new ActionCode(1);

    public static final ActionCode ACTION_CLOSE = new ActionCode(2);

    public static final ActionCode ACTION_COMMIT = new ActionCode(3);

    /**
     * A flush() operation originated by the client ( i.e. a flush() on the servlet output stream or writer, called by a servlet ). Argument
     * is the Response.
     */
    public static final ActionCode ACTION_CLIENT_FLUSH = new ActionCode(4);

    public static final ActionCode ACTION_CUSTOM = new ActionCode(5);

    public static final ActionCode ACTION_RESET = new ActionCode(6);

    public static final ActionCode ACTION_START = new ActionCode(7);

    public static final ActionCode ACTION_STOP = new ActionCode(8);

    public static final ActionCode ACTION_WEBAPP = new ActionCode(9);

    /**
     * Hook called after request, but before recycling. Can be used for logging, to update counters, custom cleanup - the request is still
     * visible
     */
    public static final ActionCode ACTION_POST_REQUEST = new ActionCode(10);

    /**
     * Callback for lazy evaluation - extract the remote host address.
     */
    public static final ActionCode ACTION_REQ_HOST_ATTRIBUTE = new ActionCode(11);

    /**
     * Callback for lazy evaluation - extract the remote host infos (address, name, port) and local address.
     */
    public static final ActionCode ACTION_REQ_HOST_ADDR_ATTRIBUTE = new ActionCode(12);

    /**
     * Callback for lazy evaluation - extract the SSL-related attributes.
     */
    public static final ActionCode ACTION_REQ_SSL_ATTRIBUTE = new ActionCode(13);

    /**
     * Chain for request creation. Called each time a new request is created ( requests are recycled ).
     */
    public static final ActionCode ACTION_NEW_REQUEST = new ActionCode(14);

    /**
     * Callback for lazy evaluation - extract the SSL-certificate (including forcing a re-handshake if necessary)
     */
    public static final ActionCode ACTION_REQ_SSL_CERTIFICATE = new ActionCode(15);

    /**
     * Callback for lazy evaluation - socket remote port.
     **/
    public static final ActionCode ACTION_REQ_REMOTEPORT_ATTRIBUTE = new ActionCode(16);

    /**
     * Callback for lazy evaluation - socket local port.
     **/
    public static final ActionCode ACTION_REQ_LOCALPORT_ATTRIBUTE = new ActionCode(17);

    /**
     * Callback for lazy evaluation - local address.
     **/
    public static final ActionCode ACTION_REQ_LOCAL_ADDR_ATTRIBUTE = new ActionCode(18);

    /**
     * Callback for lazy evaluation - local address.
     **/
    public static final ActionCode ACTION_REQ_LOCAL_NAME_ATTRIBUTE = new ActionCode(19);

    /**
     * Callback for setting FORM auth body replay
     */
    public static final ActionCode ACTION_REQ_SET_BODY_REPLAY = new ActionCode(20);

    /**
     * Callback for begin Comet processing
     */
    public static final ActionCode ACTION_COMET_BEGIN = new ActionCode(21);

    /**
     * Callback for end Comet processing
     */
    public static final ActionCode ACTION_COMET_END = new ActionCode(22);

    /**
     * Callback for getting the amount of available bytes
     */
    public static final ActionCode ACTION_AVAILABLE = new ActionCode(23);

    /**
     * Callback for an asynchronous close of the Comet event
     */
    public static final ActionCode ACTION_COMET_CLOSE = new ActionCode(24);

    /**
     * Callback for setting the timeout asynchronously
     */
    public static final ActionCode ACTION_COMET_SETTIMEOUT = new ActionCode(25);

    // ----------------------------------------------------------- Constructors

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
