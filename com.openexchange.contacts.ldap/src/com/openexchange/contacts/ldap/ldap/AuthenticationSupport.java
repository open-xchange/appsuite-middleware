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

package com.openexchange.contacts.ldap.ldap;

import javax.naming.NamingException;

/**
 * This interface provides methods to find users in the directory service and
 * to authenticate them against the directory service.
 *
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 */
public interface AuthenticationSupport {

   /**
    * Releases all resources allocated with this interface. This method
    * especially returns the LdapContext to the pool.
    */
   void close();

   /**
    * Finds a user.
    * This method searches through the whole directory service and tries to find
    * a user with the given uid.
    * @param uid Login of the user to find.
    * @return the full DN if a user is found or null.
    * @throws NamingException if an error occurs or more than one user will be
    * found.
    */
   String findUserBaseDN(String uid) throws NamingException;

   /**
    * Authenticates a user against the directory server.
    * @param userDN Identifier of the user. This is the full bind distinguished
    *        name.
    * @param passwd plain text password of the user.
    * @return <code>true</code> if the user can be authenticated.
    * @throws NamingException if authentication fails
    */
   boolean authenticateUser(String userDN, String passwd)
      throws NamingException;

   /**
    * Checks if a user is enabled.
    * A user is enabled if its attribute mailEnabled contains the value true.
    * @param uid the login name of the user.
    * @param passwd Password of the user
    * @return true if the user is enabled.
    * @throws NamingException if an error occurs while reading from ldap.
    */
   boolean isUserEnabled(String uid, String passwd) throws NamingException;

   /**
    * Checks if the password of the user is expired. If the password is expired
    * the user has to change it.
    * @param uid the login name of the user.
    * @param passwd the password of the user.
    * @return true if the password of the user is expired.
    * @throws NamingException if an error occurs while reading from ldap.
    */
   boolean isPasswordExpired(String uid, String passwd)
      throws NamingException;

}
