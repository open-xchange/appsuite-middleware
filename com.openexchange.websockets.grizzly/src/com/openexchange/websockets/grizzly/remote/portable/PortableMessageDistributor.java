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

package com.openexchange.websockets.grizzly.remote.portable;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.Strings;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketApplication;

/**
 * {@link PortableMessageDistributor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableMessageDistributor extends AbstractCustomPortable implements Callable<Void> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PortableMessageDistributor.class);

    private static final String DELIM = "?==?";
    private static final Pattern P_DELIM = Pattern.compile("\\?==\\?");
    private static final Pattern P_ESCAPE = Pattern.compile("\\?=3D=3D\\?");

    /**
     * Joins specified messages
     *
     * @param messages The messages
     * @return The joined message
     */
    private static String joinMessages(Collection<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        int size = messages.size();
        if (1 == size) {
            return escape(messages.iterator().next());
        }

        Iterator<String> it = messages.iterator();
        StringBuilder sb = new StringBuilder(size * 32);
        sb.append(escape(it.next()));
        for (int i = size - 1; i-- > 0;) {
            sb.append(DELIM).append(escape(it.next()));
        }
        return sb.toString();
    }

    /**
     * Splits joined message
     *
     * @param message The joined message
     * @return The split messages
     */
    private static String[] splitMessage(String message) {
        if (Strings.isEmpty(message)) {
            return null;
        }

        if (message.indexOf(DELIM) < 0) {
            return new String[] { unescape(message) };
        }

        String[] split = P_DELIM.split(message);
        for (int i = split.length; i-- > 0;) {
            split[i] = unescape(split[i]);
        }
        return split;
    }

    private static String escape(String toEscape) {
        if (toEscape.indexOf(DELIM) < 0) {
            return toEscape;
        }
        return P_DELIM.matcher(toEscape).replaceAll("?=3D=3D?");
    }

    private static String unescape(String toUnescape) {
        if (toUnescape.indexOf("?=3D=3D?") < 0) {
            return toUnescape;
        }
        return P_ESCAPE.matcher(toUnescape).replaceAll(DELIM);
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    /** The unique portable class ID of the {@link PortableMessageDistributor}: <code>600</code> */
    public static final int CLASS_ID = 600;

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CTX_ID = "contextId";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_FILTER = "filter";
    private static final String FIELD_ASYNC = "async";

    private int userId;
    private int contextId;
    private String message;
    private String filter;
    private boolean async;

    /**
     * Initializes a new {@link PortableMessageDistributor}.
     */
    public PortableMessageDistributor() {
        super();
    }

    /**
     * Initializes a new {@link PortableMessageDistributor}.
     *
     * @param messages The text messages to distribute
     * @param filter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param async Whether to send asynchronously or blocking
     */
    public PortableMessageDistributor(Collection<String> messages, String filter, int userId, int contextId, boolean async) {
        this(joinMessages(messages), filter, userId, contextId, async);
    }

    /**
     * Initializes a new {@link PortableMessageDistributor}.
     *
     * @param message The text message to distribute
     * @param filter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param async Whether to send asynchronously or blocking
     */
    public PortableMessageDistributor(String message, String filter, int userId, int contextId, boolean async) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.message = message;
        this.filter = filter;
        this.async = async;
    }

    @Override
    public Void call() throws Exception {
        GrizzlyWebSocketApplication application = GrizzlyWebSocketApplication.getGrizzlyWebSocketApplication();
        if (null == application) {
            LOG.warn("Found no Web Socket application on cluster member {}", GrizzlyWebSocketApplication.getLocalHost());
            return null;
        }

        String[] messages = splitMessage(message);
        if (messages.length == 0) {
            LOG.info("Received no messages on cluster member {} for user {} in context {}", GrizzlyWebSocketApplication.getLocalHost(), I(userId), I(contextId));
            return null;
        }

        LOG.info("Received {} message(s) on cluster member {} for user {} in context {}", messages.length, GrizzlyWebSocketApplication.getLocalHost(), I(userId), I(contextId));

        for (final String msg : messages) {
            if (async) {
                application.sendToUserAsync(msg, filter, userId, contextId);
            } else {
                application.sendToUser(msg, filter, userId, contextId);
            }
            LOG.info("Transmitted message \"{}\" to Web Socket application using path filter \"{}\" to user {} in context {}", new Object() { @Override public String toString(){ return StringUtils.abbreviate(msg, 24); }}, filter, I(userId), I(contextId));
        }
        return null;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt(FIELD_CTX_ID, contextId);
        writer.writeInt(FIELD_USER_ID, userId);
        writer.writeUTF(FIELD_MESSAGE, message);
        writer.writeUTF(FIELD_FILTER, filter);
        writer.writeBoolean(FIELD_ASYNC, async);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.contextId = reader.readInt(FIELD_CTX_ID);
        this.userId = reader.readInt(FIELD_USER_ID);
        this.message = reader.readUTF(FIELD_MESSAGE);
        this.filter = reader.readUTF(FIELD_FILTER);
        this.async = reader.readBoolean(FIELD_ASYNC);
    }

}
