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

package com.openexchange.realtime.client.user;

import java.util.UUID;
import org.apache.commons.lang.Validate;
import com.openexchange.realtime.client.RTUserState;

/**
 * Represents the user that would like to interact with the various kinds of realtime clients.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class RTUser {

    /**
     * Name of the realtime user
     */
    private String name = null;

    /**
     * Password of the realtime user
     */
    private String password = null;

    /**
     * Resource that is assigned to the realtime user
     */
    private String resource = null;
    
    /**
     * State after login
     */
    private RTUserState userState = null;

    /**
     * Initializes a new {@link RTUser}.
     * 
     * @param name - String with the name of the user
     * @param password - String with the password of the user
     */
    public RTUser(String name, String password) {
        this(name, password, UUID.randomUUID().toString());
    }

    /**
     * Initializes a new {@link RTUser}.
     * 
     * @param name - String with the name of the user
     * @param password - String with the password of the user
     * @param resource - String with the resource assigned to the user
     */
    public RTUser(String name, String password, String resource) {
        Validate.notNull(name);
        Validate.notNull(password);
        Validate.notNull(resource);

        this.name = name;
        this.password = password;
        this.resource = resource;
    }


    /**
     * Gets the name
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the password
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the resource
     * 
     * @return The resource
     */
    public String getResource() {
        return resource;
    }

    
    /**
     * Gets the userState
     *
     * @return The userState
     */
    public RTUserState getUserState() {
        return userState;
    }

    
    /**
     * Sets the userState
     *
     * @param userState The userState to set
     */
    public void setUserState(RTUserState userState) {
        this.userState = userState;
    }
    
    
}
