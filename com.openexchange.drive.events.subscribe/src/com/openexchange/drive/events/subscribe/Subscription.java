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

package com.openexchange.drive.events.subscribe;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;


/**
 * {@link Subscription}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Subscription {

    private String uuid;
    private String serviceID;
    private String token;
    private int contextID;
    private int userID;
    private String rootFolderID;
    private long timestamp;

    public Subscription() {
        super();
    }

    public Subscription(String uuid, int contextID, int userID, String serviceID, String token, String rootFolderID, long timestamp) {
        super();
        this.uuid = uuid;
        this.serviceID = serviceID;
        this.token = token;
        this.contextID = contextID;
        this.userID = userID;
        this.rootFolderID = rootFolderID;
        this.timestamp = timestamp;
    }

    /**
     * Gets the serviceID
     *
     * @return The serviceID
     */
    public String getServiceID() {
        return serviceID;
    }

    /**
     * Sets the serviceID
     *
     * @param serviceID The serviceID to set
     */
    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    /**
     * Gets the registration token
     *
     * @return The registration token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the registration token
     *
     * @param token The registration token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the contextID
     *
     * @return The contextID
     */
    public int getContextID() {
        return contextID;
    }

    /**
     * Sets the contextID
     *
     * @param contextID The contextID to set
     */
    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    /**
     * Gets the rootFolderID
     *
     * @return The rootFolderID
     */
    public String getRootFolderID() {
        return rootFolderID;
    }

    /**
     * Sets the rootFolderID
     *
     * @param rootFolderID The rootFolderID to set
     */
    public void setRootFolderID(String rootFolderID) {
        this.rootFolderID = rootFolderID;
    }

    /**
     * Gets the userID
     *
     * @return The userID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Sets the userID
     *
     * @param userID The userID to set
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * Gets the timestamp
     *
     * @return The timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp
     *
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the uuid
     *
     * @return The uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid
     *
     * @param uuid The uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets a value indicating whether this subscription's push registration token matches the supplied token value, trying to match
     * either the token itself or the md5 checksum of the token.
     *
     * @param tokenRef The push token reference to match
     * @return <code>true</code> if this subscription's token or the md5 checksum of this subscription's token matches,
     *         <code>false</code>, otherwise
     */
    public boolean matches(String tokenRef) {
        return null == tokenRef ? null == token : tokenRef.equals(token) || tokenRef.equals(getMD5(token));
    }

    private static String getMD5(String string) {
        if (Strings.isEmpty(string)) {
            return string;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(string.getBytes(Charsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null; // ignore
        }
    }

    @Override
    public String toString() {
        return "Subscription [uuid=" + uuid + ", serviceID=" + serviceID + ", token=" + token + ", contextID=" + contextID + ", userID=" + userID + ", rootFolderID=" + rootFolderID + ", timestamp=" + timestamp + "]";
    }

}
