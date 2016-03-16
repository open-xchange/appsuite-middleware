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

package com.openexchange.auth.mbean;

import javax.management.MBeanException;

/**
 * {@link AuthenticatorMBean} - The MBean for administrative authentication
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public interface AuthenticatorMBean {

    /** The MBean's domain */
    public static final String DOMAIN = "com.openexchange.auth";

    /**
     * Authenticates the master administrator
     *
     * @param login The login for master administrator
     * @param password The password for master administrator
     * @throws MBeanException If credentials are invalid
     */
    void doAuthentication(String login, String password) throws MBeanException;

    /**
     * Authenticates the context administrator
     *
     * @param login The login for context administrator
     * @param password The password for context administrator
     * @param contextId The context identifier
     * @throws MBeanException If credentials are invalid
     */
    void doAuthentication(String login, String password, int contextId) throws MBeanException;

    /**
     * Authenticates all users within a context.
     *
     * @param login The login
     * @param password The password
     * @param contextId The context identifier
     * @throws MBeanException If credentials are invalid
     */
    void doUserAuthentication(String login, String password, int contextId) throws MBeanException;

    /**
     * Checks if master authentication has been disabled for associated machine.
     *
     * @return <code>true</code> if master authentication has been disabled; otherwise <code>false</code>
     * @throws MBeanException If operation fails
     */
    boolean isMasterAuthenticationDisabled() throws MBeanException;

    /**
     * Checks if context authentication has been disabled for associated machine.
     *
     * @return <code>true</code> if context authentication has been disabled; otherwise <code>false</code>
     * @throws MBeanException If operation fails
     */
    boolean isContextAuthenticationDisabled() throws MBeanException;

}
