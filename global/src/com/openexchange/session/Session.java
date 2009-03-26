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

package com.openexchange.session;


/**
 * {@link Session}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Session {

    /**
     * @return the context identifier.
     */
    public int getContextId();

    /**
     * Gets the local IP address
     * 
     * @return The local IP address
     */
    public String getLocalIp();

    /**
     * Gets the login name
     * 
     * @return The login name
     */
    public String getLoginName();

    /**
     * Gets the parameter bound to specified name or <code>null</code> if no such parameter is present
     * 
     * @param name The parameter name
     * @return The parameter or <code>null</code>
     */
    public Object getParameter(String name);

    /**
     * Gets the password
     * 
     * @return The password
     */
    public String getPassword();

    /**
     * Gets the random token
     * 
     * @return The random token
     */
    public String getRandomToken();

    /**
     * Gets the secret
     * 
     * @return
     */
    public String getSecret();

    /**
     * Gets the session ID
     * 
     * @return The session ID
     */
    public String getSessionID();

    /**
     * Gets the user ID
     * 
     * @return The user ID
     */
    public int getUserId();

    /**
     * Gets the user login
     * 
     * @return The user login
     */
    public String getUserlogin();

    /**
     * Gets the full login incl. context information; e.g <code>test@foo</code>
     * 
     * @return The full login
     */
    public String getLogin();

    /**
     * Sets the parameter. Any existing parameters bound to specified name are replaced with given value.
     * <p>
     * <code>Note</code>: To ensure set parameter will reside in session on remote distribution the <code>Serializable</code> interface
     * should be implemented for specified value.
     * 
     * @param name The parameter name
     * @param value The parameter value
     */
    public void setParameter(String name, Object value);

    /**
     * Removes the random token
     */
    public void removeRandomToken();

}
