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

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.FolderProperties.SearchScope;

/**
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public final class LdapUtility {

   private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapUtility.class);
    
   /**
    * Private constructor prevents instanciation.
    */
   private LdapUtility() {
   }

   /**
    * Sets the username and the password of a context. This is usefull if you
    * want to access contents of the LDAP that is specific for a special user.
    * E.g. the personal address book.
    * @param context the context in which to set the login
    * @param login login name of the user
    * @param pass password of the user
    * @throws NamingException if an error occurs
    */
   public static void setLogin(final LdapContext context, final String login,
      final String pass) throws NamingException {
      context.addToEnvironment(Context.SECURITY_PRINCIPAL, login);
      context.addToEnvironment(Context.SECURITY_CREDENTIALS, pass);
      context.reconnect(null);
   }

   /**
    * Sets the username and the password of a context. This is usefull if you
    * want to access contents of the LDAP that is specific for a special user.
    * E.g. the personal address book.
    * @param context the context in which to set the login
    * @param login login name of the user
    * @param pass password of the user
    * @param auth authentication method to use
    * @throws NamingException if an error occurs
    */
   public static void setLogin(final LdapContext context, final String login,
      final String pass, final String auth) throws NamingException {
      context.addToEnvironment(Context.SECURITY_AUTHENTICATION, auth);
      context.addToEnvironment(Context.SECURITY_PRINCIPAL, login);
      context.addToEnvironment(Context.SECURITY_CREDENTIALS, pass);
      context.reconnect(null);
   }

   public static void removeLogin(final LdapContext context) throws NamingException {
      if (context == null) {
         return;
      }
      context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
      context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
      context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
      context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
   }
   
   public static int getSearchControl(final FolderProperties folderProperties) {
       final SearchScope searchScope = folderProperties.getSearchScope();
       switch (searchScope) {
       case one:
           return SearchControls.ONELEVEL_SCOPE;
       case base:
           return SearchControls.OBJECT_SCOPE;
       case sub:
           return SearchControls.SUBTREE_SCOPE;
       default:
           return -1;
       }
   }

   public static LdapContext createContext(final String username, final String password, final FolderProperties folderProperties) throws NamingException {
       if (LOG.isDebugEnabled()) {
           LOG.debug("Creating new connection.");
       }
       final long start = System.currentTimeMillis();
       final Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
       env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
       // Enable connection pooling
       env.put("com.sun.jndi.ldap.connect.pool", "true");
       String uri = folderProperties.getUri();
       if (uri.startsWith("ldap://") || uri.startsWith("ldaps://")) {
           if (uri.endsWith("/")) {
               uri = uri.substring(0, uri.length() - 1);
           }
           env.put(Context.PROVIDER_URL, uri + "/");
       } else {
           env.put(Context.PROVIDER_URL, "ldap://" + uri + ":389/");
       }
       if (uri.startsWith("ldaps://")) {
           env.put("java.naming.ldap.factory.socket", "com.openexchange.tools.ssl.TrustAllSSLSocketFactory");
       }
       switch (folderProperties.getAuthtype()) {
       case AdminDN:
           env.put(Context.SECURITY_PRINCIPAL, folderProperties.getAdminDN());
           env.put(Context.SECURITY_CREDENTIALS, folderProperties.getAdminBindPW());
           break;
       case user:
           env.put(Context.SECURITY_PRINCIPAL, username);
           env.put(Context.SECURITY_CREDENTIALS, password);
           break;
       case anonymous:
           break;
       default:
           break;
       }
       // TODO Make this configurable
       env.put(Context.SECURITY_AUTHENTICATION, "simple");
       final LdapContext retval = new InitialLdapContext(env, null);
       if (LOG.isDebugEnabled()) {
           LOG.debug("Context creation time: " + (System.currentTimeMillis() - start) + " ms");
       }
       return retval;
   }
   
   /**
    * Appends two directory service names without affecting the given names.
    * E.g. appends <code>uid=testuser</code> and <code>dc=example,dc=org</code>
    * to <code>uid=testuser,dc=example,dc=org</code>
    * @param name1 Name that will be the first part in string representation and
    * downmost part in the directory service tree.
    * @param name2 Name that will be the second part in string representation
    * and topmost part in the directory service tree.
    * @return the appended names
    * @throws InvalidNameException if an error occurs while appending the names.
    */
   public static Name append(final Name name1, final Name name2)
      throws InvalidNameException {
      final Name retval = (Name) name2.clone();
      retval.addAll(name1);
      return retval;
   }

}
