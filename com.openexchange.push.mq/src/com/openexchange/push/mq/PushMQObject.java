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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.push.mq;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.StringCollection;

/**
 * {@link PushMQObject} - The push object.
 * 
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushMQObject extends AbstractPushMQObject implements Serializable {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PushMQObject.class));

    private static final long serialVersionUID = -8490584616201401142L;

    private final int folderId;

    private final int module;

    private final int users[];

    private final Date creationDate = new Date();

    private final int hash;

    private final long timestamp;
    
    private final String topicName;
    
    private String hostname;

    /**
     * Initializes a new {@link PushMQObject}.
     * 
     * @param folderId The folder ID
     * @param module The module
     * @param contextId The context ID
     * @param users The user IDs as an array
     * @param isRemote <code>true</code> to mark this push object as remotely received; otherwise <code>false</code>
     */
    public PushMQObject(final int folderId, final int module, final int contextId, final int[] users, final boolean isRemote, final long timestamp, final String topicName) {
        super(contextId, isRemote);
        this.folderId = folderId;
        this.module = module;
        this.users = users;
        hash = hashCode0();
        this.timestamp = timestamp;
        this.topicName = topicName;
        this.hostname = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            this.hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    public PushMQObject(final int folderId, final int module, final int contextId, final int[] users, final boolean isRemote, final long timestamp, final String topicName, String hostname) {
        super(contextId, isRemote);
        this.folderId = folderId;
        this.module = module;
        this.users = users;
        hash = hashCode0();
        this.timestamp = timestamp;
        this.topicName = topicName;
        this.hostname = hostname;
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
        return creationDate;
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
        PushMQObject other = (PushMQObject) obj;
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

    public static PushMQObject parseString(String toParse) throws OXException {
        final Pattern regex = Pattern.compile(
            "FOLDER_ID=(.*?),MODULE=(.*?),CONTEXT_ID=(.*?),USERS=(.*?),IS_REMOTE=(.*?),TIMESTAMP=(.*?),TOPIC=(*?),HOSTNAME=(.*?)",
            Pattern.DOTALL);
        Matcher matcher = regex.matcher(toParse);
        if (!matcher.find()) {
            return null;
        } else {
            int folderId = Integer.valueOf(matcher.group(1));
            int module = Integer.valueOf(matcher.group(2));
            int contextId = Integer.valueOf(matcher.group(3));
            int[] users = null;
            if (matcher.group(4).contains(",")) {
                String[] user = matcher.group(4).split(",");
                users = new int[user.length];
                for (int i = 0; i < user.length; i++) {
                    users[i] = Integer.valueOf(user[i]);
                }
            } else {
                users = new int[1];
                users[0] = Integer.valueOf(matcher.group(4));
            }
            boolean isRemote = Boolean.valueOf(matcher.group(5));
            long timestamp = Long.valueOf(matcher.group(6));
            String topicName = matcher.group(7);
            String hostname = matcher.group(8);
            return new PushMQObject(folderId, module, contextId, users, isRemote, timestamp, topicName, hostname);
        }
    }
}
