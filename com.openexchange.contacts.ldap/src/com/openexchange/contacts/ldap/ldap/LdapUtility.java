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
import com.openexchange.contacts.ldap.property.PropertyHandler;
import com.openexchange.contacts.ldap.property.PropertyHandler.AuthType;
import com.openexchange.contacts.ldap.property.PropertyHandler.SearchScope;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein </a>
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

   public static void removeLogin(LdapContext context) throws NamingException {
      if (context == null) {
         return;
      }
      context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
      context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
      context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
      context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
   }
   
   public static int getSearchControl() {
       final PropertyHandler instance = PropertyHandler.getInstance();
       final SearchScope searchScope = instance.getSearchScope();
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

//   /**
//    * Finds the appropriate BaseDN. Patterns can be
//    * <ul>
//    * <li>userBaseDN</li>
//    * <li>groupBaseDN</li>
//    * <li>credentialsBaseDN</li>
//    * </ul>
//    * @param pattern the BaseDN pattern
//    * @return the appropriate BaseDN
//    */
//   static String getSearchBaseDN(Properties props, String classname,
//      String propname, UserLdapValues values) throws NamingException {
//      String retval = findProperty(props, classname, propname);
//      if (retval.length() > 0 && retval.charAt(0) == '['
//         && retval.charAt(retval.length() - 1) == ']') {
//         if (retval.indexOf("[", 1) != -1) {
//            Object[] temp = new Object[4];
//            temp[0] = props;
//            temp[1] = classname;
//            temp[2] = null; // values
//            temp[3] = new HashMap(1);
//            retval = LineParserUtility.parseLine(retval, filler, temp);
//         } else {
//            retval = values.getValue(retval.substring(1, retval.length() - 1));
//         }
//      }
//      return retval;
//   }

   public static LdapContext createContext(String username, String password) throws NamingException {
       if (LOG.isDebugEnabled()) {
           LOG.debug("Creating new connection.");
       }
       long start = System.currentTimeMillis();
       Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
       env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
       // Enable connection pooling
       env.put("com.sun.jndi.ldap.connect.pool", "true");
       final PropertyHandler instance = PropertyHandler.getInstance();
       String uri = instance.getUri();
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
       switch (instance.getAuthtype()) {
       case AdminDN:
           env.put(Context.SECURITY_PRINCIPAL, instance.getAdminDN());
           env.put(Context.SECURITY_CREDENTIALS, instance.getAdminBindPW());
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
       LdapContext retval = new InitialLdapContext(env, null);
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
      Name retval = (Name) name2.clone();
      retval.addAll(name1);
      return retval;
   }

//   /**
//    * Builds a distinguished name.
//    * @param props Properties configuring the directory service.
//    * @param classname Name of the class that needs the property.
//    * @param propname Name of the property that contains the definition for the
//    *        distinguished name to build.
//    * @param replace Map containing the values that fill the dynamic parts of
//    *        the configuration properties.
//    * @param values user specific ldap values.
//    * @return the full qualified distinguished name.
//    * @throws NamingException if a property is missing.
//    * @deprecated The classname is used only for the factory.
//    */
//   public static Name buildDN(final Properties props, final String classname,
//      final String propname, final Map replace, final UserLdapValues values)
//      throws NamingException {
//      return buildDN(props, propname, replace, values);
//   }

//   /**
//    * Builds a distinguished name that contains no user specific parts.
//    * @param props Properties configuring the directory service.
//    * @param classname Name of the class that needs the property.
//    * @param propName Name of the property that contains the definition for the
//    *        distinguished name to build.
//    * @param replace Map containing the values that fill the dynamic parts of
//    *        the configuration properties.
//    * @return the full qualified distinguished name.
//    * @throws NamingException if a property is missing.
//    * @deprecated The classname is used only for the factory.
//    */
//   public static Name buildDN(final Properties props, final String classname,
//      final String propName, final Map replace) throws NamingException {
//      return buildDN(props, propName, replace, null);
//   }

//   /**
//    * Builds a distinguished name that contains no user specific parts.
//    * @param props Properties configuring the directory service.
//    * @param propName Name of the property that contains the definition for the
//    *        distinguished name to build.
//    * @param replace Map containing the values that fill the dynamic parts of
//    *        the configuration properties.
//    * @return the full qualified distinguished name.
//    * @throws NamingException if a property is missing.
//    */
//   public static Name buildDN(final Properties props, final String propName,
//      final Map replace) throws NamingException {
//      return buildDN(props, propName, replace, null);
//   }

//   /**
//    * Builds a distinguished name.
//    * @param props Properties configuring the directory service.
//    * @param propName Name of the property that contains the definition for the
//    *        distinguished name to build.
//    * @param replace Map containing the values that fill the dynamic parts of
//    *        the configuration properties.
//    * @param values user specific ldap values.
//    * @return the full qualified distinguished name.
//    * @throws NamingException if a property is missing.
//    */
//   public static Name buildDN(final Properties props, final String propName,
//      final Map replace, final UserLdapValues values) throws NamingException {
//      String pattern = findProperty(props, propName);
//      Object[] temp = new Object[] { props, null, values, replace };
//      return new LdapName(LineParserUtility.parseLine(pattern, filler, temp));
//   }

//   /**
//    * Builds a distinguished name for binding. The baseDN of the context will be
//    * added.
//    * @param props Properties configuring the directory service.
//    * @param replace Map containing the values that fill the dynamic parts of
//    *        the configuration properties.
//    * @param values user specific ldap values.
//    * @return the full qualified distinguished name.
//    * @throws NamingException if a property is missing.
//    */
//   public static Name buildBindDN(final Properties props, final Map replace,
//      final UserLdapValues values) throws NamingException {
//      Name retval = buildDN(props, Names.USER_DN, replace, values);
//      Name baseDN = new LdapName(PropertyHandler.getInstance().getBaseDN());
//      return append(retval, baseDN);
//   }

//   /**
//    * Builds a distinguished name for binding. The baseDN of the context will be
//    * added.
//    * @param props Properties configuring the directory service.
//    * @param classname Name of the class that needs the property.
//    * @param propname Name of the property that contains the definition for the
//    *        distinguished name to build.
//    * @param replace Map containing the values that fill the dynamic parts of
//    *        the configuration properties.
//    * @param values user specific ldap values.
//    * @return the full qualified distinguished name.
//    * @throws NamingException if a property is missing.
//    * @deprecated The classname is used only for the factory.
//    */
//   public static String buildCredentials(final Properties props,
//      final String classname, final String propname, final Map replace,
//      final UserLdapValues values) throws NamingException {
//      return buildBindDN(props, replace, values).toString();
//   }

//   private static final TagFiller filler = new TagFillerAdapter() {
//      public String replace(String tag, Object data) {
//         Object[] temp = (Object[]) data;
//         Properties props = (Properties) temp[0];
//         String classname = (String) temp[1];
//         UserLdapValues values = (UserLdapValues) temp[2];
//         Map datamap = (Map) temp[3];
//         String retval = tag;
//         if (datamap != null && datamap.containsKey(tag)) {
//            return (String) datamap.get(tag);
//         } else if (values != null && props != null && tag.endsWith("BaseDN")) {
//            try {
//               if (null != classname) {
//                  retval = LdapUtility.getSearchBaseDN(props, classname, tag,
//                     values);
//               } else {
//                  retval = LdapUtility.getSearchBaseDN(props, tag, values);
//               }
//            } catch (NamingException e) {
//               retval = e.getMessage();
//            }
//         } else {
//            try {
//               String property = LdapUtility.findProperty(props, tag);
//               if (!property.equals("[" + tag + "]")) {
//                   retval = LineParserUtility.parseLine(property, this, data);
//               }
//            } catch (NamingException e) {
//            }
//         }
//         return retval;
//      }
//   };

   /*
    * More general helper methods.
    */

   static NamingException propertyNotFound(String[] classnames, String property) {
      StringBuffer message = new StringBuffer("Property ");
      message.append(property);
      message.append(" not found under \"");
      for (int i = 0; i < classnames.length; i++) {
         message.append(classnames[i]);
         message.append("\",\"");
      }
      message.delete(message.length() - 2, message.length());
      message.append(" !");
      return new NamingException(message.toString());
   }

   static NamingException propertyNotFound(String classname, String property) {
      return propertyNotFound(new String[] { classname }, property);
   }

}
