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

package com.openexchange.push.ms;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.tools.StringCollection;

/**
 * {@link PushMsObject} - The push object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsObject extends AbstractPushMsObject implements Serializable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsObject.class);

    private static final long serialVersionUID = -8490584616201401142L;

    public static PushMsObject valueFor(final Map<String, Object> pojo) {
        if (null == pojo || pojo.containsKey("__pure")) {
            return null;
        }
        try {
            final int contextId;
            {
                final Integer i = (Integer) pojo.get("__contextId");
                contextId = null == i ? 0 : i.intValue();
            }
            final boolean remote;
            {
                final Boolean b = (Boolean) pojo.get("__remote");
                remote = null == b ? false : b.booleanValue();
            }
            final int folderId;
            {
                final Integer i = (Integer) pojo.get("__folderId");
                folderId = null == i ? 0 : i.intValue();
            }
            final int module;
            {
                final Integer i = (Integer) pojo.get("__module");
                module = null == i ? 0 : i.intValue();
            }
            final int hash;
            {
                final Integer i = (Integer) pojo.get("__hash");
                hash = null == i ? 0 : i.intValue();
            }
            final int[] users = (int[]) pojo.get("__users");
            final long creationDate;
            {
                final Long l = (Long) pojo.get("__creationDate");
                creationDate = null == l ? 0 : l.longValue();
            }
            final long timestamp;
            {
                final Long l = (Long) pojo.get("__timestamp");
                timestamp = null == l ? 0 : l.longValue();
            }
            final String hostname = (String) pojo.get("__hostname");
            final String topicName = (String) pojo.get("__topicName");
            return new PushMsObject(folderId, module, contextId, users, remote, timestamp, topicName, hostname, hash, new Date(creationDate));
        } catch (final NullPointerException npe) {
            LOG.warn("Missing required attribute.", npe);
            return null;
        }
    }

    private final int folderId;
    private final int module;
    private final int users[];
    private final Date creationDate;
    private final int hash;
    private final long timestamp;
    private final String topicName;
    private final String hostname;

    /**
     * Initializes a new {@link PushMsObject}.
     *
     * @param folderId The folder ID
     * @param module The module
     * @param contextId The context ID
     * @param users The user IDs as an array
     * @param isRemote <code>true</code> to mark this push object as remotely received; otherwise <code>false</code>
     * @param timestamp last modified time of the groupware data object
     * @param topicName the topic on which the data object was received
     */
    public PushMsObject(final int folderId, final int module, final int contextId, final int[] users, final boolean isRemote, final long timestamp, final String topicName) {
        super(contextId, isRemote);
        creationDate = new Date();
        this.folderId = folderId;
        this.module = module;
        this.users = users;
        hash = hashCode0();
        this.timestamp = timestamp;
        this.topicName = topicName;
        String hostname = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            LOG.error("", e);
        }
        this.hostname = hostname;
    }

    /**
     * Initializes a new {@link PushMsObject}.
     *
     * @param folderId The folder ID
     * @param module The module
     * @param contextId The context ID
     * @param users The user IDs as an array
     * @param isRemote <code>true</code> to mark this push object as remotely received; otherwise <code>false</code>
     * @param timestamp last modified time of the groupware data object
     * @param topicName the topic on which the data object was received
     * @param hostname the hostname to use for this PushMsObject
     */
    public PushMsObject(final int folderId, final int module, final int contextId, final int[] users, final boolean isRemote, final long timestamp, final String topicName, String hostname) {
        super(contextId, isRemote);
        creationDate = new Date();
        this.folderId = folderId;
        this.module = module;
        this.users = users;
        hash = hashCode0();
        this.timestamp = timestamp;
        this.topicName = topicName;
        this.hostname = hostname;
    }

    private PushMsObject(final int folderId, final int module, final int contextId, final int[] users, final boolean isRemote, final long timestamp, final String topicName, String hostname, final int hash, final Date creationDate) {
        super(contextId, isRemote);
        this.folderId = folderId;
        this.module = module;
        this.users = users;
        this.hash = hash;
        this.timestamp = timestamp;
        this.topicName = topicName;
        this.hostname = hostname;
        this.creationDate = creationDate;
    }

    private int hashCode0() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + folderId;
        result = prime * result + module;
        result = prime * result + Arrays.hashCode(users);
        return result;
    }

    /**
     * Generates the POJO view for this instance.
     *
     * @return The POJO view
     */
    public Map<String, Object> writePojo() {
        final Map<String, Object> m = new LinkedHashMap<String, Object>(10);
        if (folderId > 0) {
            m.put("__folderId", Integer.valueOf(folderId));
        }
        if (module > 0) {
            m.put("__module", Integer.valueOf(module));
        }
        if (contextId > 0) {
            m.put("__contextId", Integer.valueOf(contextId));
        }
        if (null != users) {
            m.put("__users", users);
        }
        m.put("__remote", Boolean.valueOf(remote));
        if (null != creationDate) {
            m.put("__creationDate", Long.valueOf(creationDate.getTime()));
        }
        m.put("__hash", Integer.valueOf(hash));
        if (timestamp > 0) {
            m.put("__timestamp", Long.valueOf(timestamp));
        }
        if (null != hostname) {
            m.put("__hostname", hostname);
        }
        if (null != topicName) {
            m.put("__topicName", topicName);
        }
        return m;
    }

    /**
     * Gets the folder ID.
     *
     * @return The folder ID
     */
    public int getFolderId() {
        return folderId;
    }

    /**
     * Gets the module.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Gets the user IDs as an array.
     *
     * @return The user IDs as an array
     */
    public int[] getUsers() {
        return users;
    }

    /**
     * Gets the creation date.
     *
     * @return The creation date
     */
    public Date getCreationDate() {
        return new Date(creationDate.getTime());
    }

    /**
     * Gets the time stamp or <code>0</code> if not available.
     *
     * @return The time stamp or <code>0</code> if not available
     */
    public long getTimestamp() {
        return timestamp;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getHostname() {
        return hostname;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + folderId;
        result = prime * result + module;
        result = prime * result + Arrays.hashCode(users);
        return result;
    }

    public int hashCode1() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PushMsObject other = (PushMsObject) obj;
        if (folderId != other.folderId) {
            return false;
        }
        if (module != other.module) {
            return false;
        }
        if (!Arrays.equals(users, other.users)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("FOLDER_ID=").append(folderId).append(",MODULE=").append(module).append(",CONTEXT_ID=").append(
            getContextId()).append(",USERS=").append(StringCollection.convertArray2String(users)).append(",IS_REMOTE=").append(isRemote()).append(
            ",TIMESTAMP=").append(timestamp).append(",TOPIC=").append(topicName).append(",HOSTNAME=").append(hostname).toString();
    }

    public static PushMsObject parseString(String toParse) throws OXException {
        final Pattern regex = Pattern.compile(
            "FOLDER_ID=(.*?),MODULE=(.*?),CONTEXT_ID=(.*?),USERS=(.*?),IS_REMOTE=(.*?),TIMESTAMP=(.*?),TOPIC=(.*?),HOSTNAME=(.*?)",
            Pattern.DOTALL);
        Matcher matcher = regex.matcher(toParse);
        if (!matcher.find()) {
            return null;
        }
        int folderId = Integer.parseInt(matcher.group(1));
        int module = Integer.parseInt(matcher.group(2));
        int contextId = Integer.parseInt(matcher.group(3));
        final Pattern splitter = Pattern.compile(",");
        int[] users = null;
        if (matcher.group(4).indexOf(',') >= 0) {
            String[] user = splitter.split(matcher.group(4), 0);
            users = new int[user.length];
            for (int i = 0; i < user.length; i++) {
                users[i] = Integer.parseInt(user[i]);
            }
        } else {
            users = new int[1];
            users[0] = Integer.parseInt(matcher.group(4));
        }
        boolean isRemote = Boolean.parseBoolean(matcher.group(5));
        long timestamp = Long.parseLong(matcher.group(6));
        String topicName = matcher.group(7);
        String hostname = matcher.group(8);
        return new PushMsObject(folderId, module, contextId, users, isRemote, timestamp, topicName, hostname);
    }
}
