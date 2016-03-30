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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link PushRequest}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushRequest {

    public static final int MAGIC = 1337;

    public static final int REGISTER = 1;

    public static final int REGISTER_SYNC = 2;

    public static final int PUSH_SYNC = 3;

    public static final int REMOTE_HOST_REGISTER = 4;

    private int currentLength;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushRequest.class);

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

                    LOG.debug("register package: user id={},host address={},port={}", userId, hostAddress, port);

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

                    LOG.debug("register sync package: {}", registerObj);

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

                    LOG.debug("push sync package: {}", pushObject);

                PushOutputQueue.add(pushObject);
                break;
            case REMOTE_HOST_REGISTER:
                /*
                 * ...HostAddress\1Port
                 */
                final RemoteHostObject remoteHostObject = new RemoteHostObject();

                hostAddress = InetAddress.getByName(parseString(args, pos++));
                if ("localhost".equals(hostAddress.getHostName())) {
                    hostAddress = datagramPacket.getAddress();
                }

                port = parsePort(args, pos++);

                remoteHostObject.setHost(hostAddress);
                remoteHostObject.setPort(port);

                    LOG.debug("remost host register request: {}", remoteHostObject);

                PushOutputQueue.addRemoteHostObject(remoteHostObject);
                break;
            default:
                throw PushUDPExceptionCode.INVALID_TYPE.create(null, Integer.valueOf(type));
            }
        } catch (final OXException e) {
            LOG.error("PushRequest", e);
        } catch (final UnknownHostException e) {
            LOG.error("PushRequest: Remote host registration failed", e);
        } catch (final Exception e) {
            LOG.error("PushRequest", e);
        }
    }

    /**
     * Gets specified datagram packet's arguments and check its magic bytes.
     *
     * @param datagramPacket The datagram packet
     * @return The datagram packet's arguments
     * @throws OXException If datagram packet's magic bytes or its length are invalid
     */
    private String[] getArgsFromPacket(final DatagramPacket datagramPacket) throws OXException {
        final byte[] b = new byte[datagramPacket.getLength()];
        System.arraycopy(datagramPacket.getData(), 0, b, 0, b.length);
        final String data = new String(b);

            LOG.debug("push request data: {}", data);

        /*
         * Split: MAGIC\1Length\1Data
         */
        final String[] s = PATTERN_SPLIT.split(data, 0);
        int pos = 0;

        final int magic = parseMagic(s, pos++);
        if (magic != MAGIC) {
            throw PushUDPExceptionCode.INVALID_MAGIC.create(null, s[pos - 1]);
        }

        final int length = parseLength(s, pos++);


        if (currentLength + length > b.length || length < 0) {
            /*
             * Strange datagram package
             */
            throw PushUDPExceptionCode.MISSING_PAYLOAD.create();
        }
        final byte[] bData = new byte[length];
        System.arraycopy(b, currentLength, bData, 0, length);
        return PATTERN_SPLIT.split(new String(bData), 0);
    }

    private int parseMagic(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.MAGIC_NAN.create(e, s[pos]);
        }
    }

    private int parseType(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.TYPE_NAN.create(e, s[pos]);
        }
    }

    private int parseLength(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.LENGTH_NAN.create(e, s[pos]);
        }
    }

    private int parseUserId(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.USER_ID_NAN.create(e, s[pos]);
        }
    }

    private int parseContextId(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.CONTEXT_ID_NAN.create(e, s[pos]);
        }
    }

    private int parseFolderId(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.FOLDER_ID_NAN.create(e, s[pos]);
        }
    }

    private int parseModule(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.MODULE_NAN.create(e, s[pos]);
        }
    }

    private int parsePort(final String[] s, final int pos) throws OXException {
        try {
            return parseInt(s, pos);
        } catch (final NumberFormatException e) {
            // Not a number...
            throw PushUDPExceptionCode.PORT_NAN.create(e, s[pos]);
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
        final int length = s[pos].length();
        currentLength += length + 1;
        if (length == 0) {
            return null;
        }

        return s[pos];
    }

    private boolean parseBoolean(final String[] s, final int pos) {
        return Boolean.parseBoolean(parseString(s, pos));
    }

    private int[] convertString2UserIDArray(final String s) throws OXException {
        final String tmp[] = Strings.splitByComma(s);
        final int i[] = new int[tmp.length];
        try {
            for (int a = 0; a < i.length; a++) {
                i[a] = Integer.parseInt(tmp[a]);
            }
        } catch (final NumberFormatException e) {
            throw PushUDPExceptionCode.INVALID_USER_IDS.create(e, Arrays.toString(tmp));
        }

        return i;
    }
}
