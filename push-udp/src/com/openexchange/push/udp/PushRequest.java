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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.event.EventException;

/**
 * {@link PushRequest}
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class PushRequest {

    public static final int MAGIC = 1337;

    public static final int REGISTER = 1;

    public static final int REGISTER_SYNC = 2;

    public static final int PUSH_SYNC = 3;

    public static final int REMOTE_HOST_REGISTER = 4;

    private int currentLength;

    private static final Log LOG = LogFactory.getLog(PushRequest.class);

    private static final Pattern PATTERN_SPLIT = Pattern.compile("\1");

    /**
     * Creates a new {@link PushRequest}.
     */
    public PushRequest() {
        super();
    }

    /**
     * Initializes this push request from specified datagram packet.
     * 
     * @param datagramPacket The datagram packet to initializes from
     */
    public void init(final DatagramPacket datagramPacket) {
        try {
            final String[] args = getArgsFromPacket(datagramPacket);
            int pos = 0;

            /*
             * First argument is always the request type
             */
            final int type = parseType(args, pos++);

            int userId = 0;
            InetAddress hostAddress = null;
            int port = 0;
            int folderId = 0;
            int module = 0;
            int contextId = 0;
            long timestamp = 0;

            RegisterObject registerObj = null;

            switch (type) {
            case REGISTER:
                /*
                 * ...UserId\1ContextId
                 */
                userId = parseUserId(args, pos++);
                contextId = parseContextId(args, pos++);

                hostAddress = datagramPacket.getAddress();
                port = datagramPacket.getPort();

                registerObj = new RegisterObject(userId, contextId, hostAddress.getHostAddress(), port, false);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("register package: user id=" + userId + ",host address=" + hostAddress + ",port=" + port);
                }

                RegisterHandler.addRegisterObject(registerObj);
                PushOutputQueue.add(registerObj, true);
                break;
            case REGISTER_SYNC:
                /*
                 * ...UserId\1ContextId\1HostAddress\1Port
                 */
                userId = parseUserId(args, pos++);
                contextId = parseContextId(args, pos++);

                hostAddress = InetAddress.getByName(parseString(args, pos++));

                port = parsePort(args, pos++);

                registerObj = new RegisterObject(userId, contextId, hostAddress.getHostAddress(), port, true);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("register sync package: " + registerObj);
                }

                RegisterHandler.addRegisterObject(registerObj);
                break;
            case PUSH_SYNC:
                /*
                 * ...FolderId\1Module\1ContextId\1Users\1Timestamp
                 */
                folderId = parseFolderId(args, pos++);
                module = parseModule(args, pos++);
                contextId = parseContextId(args, pos++);

                final int[] users = convertString2UserIDArray(parseString(args, pos++));

                timestamp = parseTimestamp(args, pos++);

                final PushObject pushObject = new PushObject(folderId, module, contextId, users, true, timestamp);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("push sync package: " + pushObject);
                }

                PushOutputQueue.add(pushObject);
                break;
            case REMOTE_HOST_REGISTER:
                /*
                 * ...HostAddress\1Port
                 */
                final RemoteHostObject remoteHostObject = new RemoteHostObject();

                hostAddress = InetAddress.getByName(parseString(args, pos++));
                if ("localhost".equals(hostAddress)) {
                    hostAddress = datagramPacket.getAddress();
                }

                port = parsePort(args, pos++);

                remoteHostObject.setHost(hostAddress);
                remoteHostObject.setPort(port);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("remost host register request: " + remoteHostObject);
                }

                PushOutputQueue.addRemoteHostObject(remoteHostObject);
                break;
            default:
                throw new PushUDPException(PushUDPException.Code.INVALID_TYPE, null, Integer.valueOf(type));
            }
        } catch (final PushUDPException e) {
            LOG.error("PushRequest: " + e, e);
        } catch (final UnknownHostException e) {
            LOG.error("PushRequest: Remote host registration failed: " + e.getMessage(), e);
        } catch (final EventException e) {
            LOG.error("PushRequest: Event could not be enqueued: " + e.getMessage(), e);
        } catch (final Exception e) {
            LOG.error("PushRequest: " + e, e);
        }
    }

    /**
     * Gets specified datagram packet's arguments and check its magic bytes.
     * 
     * @param datagramPacket The datagram packet
     * @return The datagram packet's arguments
     * @throws PushUDPException If datagram packet's magic bytes or its length are invalid
     */
    private String[] getArgsFromPacket(final DatagramPacket datagramPacket) throws PushUDPException {
        final byte[] b = new byte[datagramPacket.getLength()];
        System.arraycopy(datagramPacket.getData(), 0, b, 0, b.length);
        final String data = new String(b);

        if (LOG.isDebugEnabled()) {
            LOG.debug("push request data: " + data);
        }

        /*
         * Split: MAGIC\1Length\1Data
         */
        final String[] s = PATTERN_SPLIT.split(data, 0);
        int pos = 0;

        final int magic = parseMagic(s, pos++);
        if (magic != MAGIC) {
            throw new PushUDPException(PushUDPException.Code.INVALID_MAGIC, null, s[pos - 1]);
        }

        final int length = parseLength(s, pos++);

        final byte[] bData = new byte[length];
        System.arraycopy(b, currentLength, bData, 0, length);
        return PATTERN_SPLIT.split(new String(bData), 0);
    }

    private int parseMagic(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.MAGIC_NAN, e, s[pos]);
        }
    }

    private int parseType(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.TYPE_NAN, e, s[pos]);
        }
    }

    private int parseLength(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.LENGTH_NAN, e, s[pos]);
        }
    }

    private int parseUserId(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.USER_ID_NAN, e, s[pos]);
        }
    }

    private int parseContextId(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.CONTEXT_ID_NAN, e, s[pos]);
        }
    }

    private int parseFolderId(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.FOLDER_ID_NAN, e, s[pos]);
        }
    }

    private int parseModule(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.MODULE_NAN, e, s[pos]);
        }
    }

    private int parsePort(final String[] s, final int pos) throws PushUDPException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw new PushUDPException(PushUDPException.Code.PORT_NAN, e, s[pos]);
        }
    }

    private long parseTimestamp(final String[] s, final int pos) {
        if (pos >= s.length) {
            return 0;
        }
        return parseLong(s, pos);
    }

    private long parseLong(final String[] s, final int pos) {
        return Long.parseLong(parseString(s, pos));
    }

    private int parseInt(final String[] s, final int pos) {
        return Integer.parseInt(parseString(s, pos));
    }

    private String parseString(final String[] s, final int pos) {
        currentLength += s[pos].length() + 1;
        if (s[pos].length() == 0) {
            return null;
        }

        return s[pos];
    }

    private boolean parseBoolean(final String[] s, final int pos) {
        return Boolean.parseBoolean(parseString(s, pos));
    }

    private int[] convertString2UserIDArray(final String s) throws PushUDPException {
        final String tmp[] = s.split(",");
        final int i[] = new int[tmp.length];
        try {
            for (int a = 0; a < i.length; a++) {
                i[a] = Integer.parseInt(tmp[a]);
            }
        } catch (final NumberFormatException e) {
            throw new PushUDPException(PushUDPException.Code.INVALID_USER_IDS, e, Arrays.toString(tmp));
        }

        return i;
    }
}
