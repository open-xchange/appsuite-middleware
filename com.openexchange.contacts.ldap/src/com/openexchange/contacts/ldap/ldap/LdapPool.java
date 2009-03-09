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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import com.openexchange.contacts.ldap.osgi.LdapActivator;

/**
 * Implements pooling of connections to the LDAP server. Create like the following:
 * 
 * <pre>
 * 
 * LdapPool pool = new LdapPool(&quot;localhost&quot;, &quot;dc=example,dc=org&quot;, &quot;simple&quot;);
 * </pre>
 * 
 * Usage:
 * 
 * <pre>
 * 
 *  LdapContext context = pool.get();
 *  LdapPool.setLogin(context, &quot;uid&quot;, &quot;passwd&quot;);
 *  NamingEnumeration e = context.search(&quot;ou=OxUser,ou=OxObjects&quot;,
 *  &quot;uid=username&quot;);
 *  ...
 *  e.close();
 *  LdapPool.removeLogin(context);
 *  pool.release(context);
 * 
 * </pre>
 * 
 * Shutdown LdapPool at programm termination:
 * 
 * <pre>
 * pool.close();
 * </pre>
 * 
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 */
public class LdapPool implements Runnable {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LdapActivator.class);

    /**
     * Initial connections
     */
    private static final int DEFMINCONS = 0;

    /**
     * Non-used connections will be removed after this time in minutes
     */
    private static final int DEFKEEPALIVE = 2;

    /**
     * Time after which a lookup for no more used database connection is made which can be closed or must be pinged
     */
    private static final int WAIT = 60 * 1000; // Millisekunden

    private int mincons;

    private int keepalive;

    private String uri;

    private String baseDN;

    private String auth;

    private boolean singleMode;

    private ContextContainer cc;

    private Thread rm = null;

    /**
     * Initialises the LDAPPool with the parameters for the connection to the LDAP server
     * 
     * @param uri uri for the LDAP server, e.g. ldap://localhost
     * @param baseDN The base domain name, e.g. dc=netline,dc=de
     * @param auth type of authentication, z.B. simple
     * @param singleMode starts the LdapPool in single context mode.
     */
    public LdapPool(final String uri, final String baseDN, final String auth, final boolean singleMode) {
        this.uri = uri;
        this.baseDN = baseDN;
        this.auth = auth;
        this.singleMode = singleMode;

        mincons = DEFMINCONS;
        keepalive = DEFKEEPALIVE;

        cc = new ContextContainer();

        if (!singleMode) {
            rm = new Thread(this);
            rm.setName("ldap connection ping thread");
            rm.start();
        }
    }

    /**
     * Terminates this LDAPPool and closes all unused connections. Connections which are still in use will be closed on return
     */
    public void close() {
        if (null != rm) {
            rm.interrupt();
            rm = null;
        }
    }

    /**
     * Thread must be stopped if the pool is not needed anymore.
     */
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Return an LdapContext from the pool. This LdapContext must be released with releaseContext so that the pool is able to use is again
     * 
     * @return LdapContext
     */
    public LdapContext get() throws NamingException {
        return get(true);
    }

    /**
     * LdapContext must be released with this method if no more used
     * 
     * @param context
     */
    public void release(LdapContext context) {
        release(context, true);
    }

    /**
     * Sets the username and the password of a context. This is usefull if you want to access contents of the LDAP that is specific for a
     * special user. E.g. the personal address book.
     * 
     * @param context the context in which to set the login
     * @param login login name of the user
     * @param pass password of the user
     * @throws NamingException if an error occurs:
     */
    public static void setLogin(LdapContext context, String login, String pass) throws NamingException {
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, login);
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, pass);
        context.reconnect(null);
    }

    public static void setLogin(LdapContext context, String login, String pass, String auth) throws NamingException {
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, login);
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, pass);
        context.addToEnvironment(Context.SECURITY_AUTHENTICATION, auth);
        context.reconnect(null);
    }

    public static void removeLogin(LdapContext context) throws NamingException {
        if (null == context) {
            return;
        }
        context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
        context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
        context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
        context.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
    }

    /**
     * Return a LdapContext to the pool. If the parameter test is true a check for correct operation is done
     * 
     * @param context the returned LdapContext
     * @param test true, to enable the correctness check
     */
    private void release(LdapContext context, boolean test) {
        if (null == context) {
            return;
        }
        if (!singleMode) {
            try {
                removeLogin(context);
            } catch (NamingException ne) {
                LOG.error("Can't remove login from context: " + ne.getMessage(), ne);
            }
            boolean removed = cc.release(context, new Integer(0));
            if (test && !removed) {
                LOG.error("Got a connection to release that wasn't created by this LdapPool.");
                try {
                    context.close();
                } catch (NamingException ne) {
                }
                return;
            }
        } else if (cc.getUnusedSize() == 0) {
            cc.release(context, new Integer(0));
        }
    }

    /**
     * Get a LdapContext out of the pool. If the parameter test is true a check for correct operation is done
     * 
     * @param test true to enable checks
     * @return einen LdapContext
     */
    private LdapContext get(boolean test) throws NamingException {
        LdapContext retval = null;
        if (singleMode) {
            retval = cc.get(getStackTrace());
        } else {
            retval = cc.get(getStackTrace());
            if (retval != null && isClosed(retval)) {
                retval.close();
                cc.removeUsed(retval);
                retval = null;
            }
            if (retval == null) {
                retval = createContext();
                if (retval != null) {
                    cc.addUsed(retval, getStackTrace());
                }
            }
        }
        return retval;
    }

    /**
     * Returns a stack trace. This method is used to find creations of ldap connections that will not be released.
     * 
     * @return a string containing the stack trace.
     */
    private static String getStackTrace() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Throwable t = new Throwable("Ldap connection used by");
        t.printStackTrace(pw);
        pw.close();
        try {
            sw.close();
        } catch (IOException e) {
        }
        return sw.toString();
    }

    /**
     * Tests if a connection is closed or not
     * 
     * @param retval The connection
     * @return true, if the connection was closed
     */
    private boolean isClosed(LdapContext context) {
        boolean retval;
        try {
            context.reconnect(null);
            retval = false;
        } catch (NamingException e) {
            retval = true;
        }
        return retval;
    }

    /**
     * Creates a new context to the ldap server.
     * 
     * @return a new context to the ldap server.
     */
    private LdapContext createContext() throws NamingException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new connection.");
        }
        long start = System.currentTimeMillis();
        Hashtable<String, String> env = new Hashtable<String, String>(4, 1f);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
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
        env.put(Context.SECURITY_AUTHENTICATION, auth);
        LdapContext retval = new InitialLdapContext(env, null);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Context creation time: " + (System.currentTimeMillis() - start) + " ms");
        }
        return retval;
    }

    /**
     * run() method for the thread. Increments the time counter for each unused connection
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (true) {
            try {
                Thread.sleep(WAIT);
            } catch (InterruptedException ie) {
                LdapContext context = cc.getUnused();
                while (context != null) {
                    try {
                        context.close();
                    } catch (NamingException ne) {
                        if (LOG.isDebugEnabled())
                            LOG.debug("Error while closing ldap connection:" + ne.getMessage());
                    }
                    context = cc.getUnused();
                }
                return;
            }
            cc.increaseTimer();
        }
    }

    /**
     * Executes a small query to keep the connection alive
     */
    private static void makeTraffic(LdapContext context) {
        try {
            context.reconnect(null);
        } catch (NamingException ne) {
        }
    }

    /**
     * This class synchronizes concurrent access to the data structures holding the ldap contexts.
     * 
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein </a>
     */
    private class ContextContainer {

        private Map<LdapContext, Integer> unused;

        private Map<LdapContext, String> used;

        private int minUnusedTime;

        ContextContainer() {
            unused = new HashMap<LdapContext, Integer>();
            used = new HashMap<LdapContext, String>();
            minUnusedTime = 0;
        }

        /**
         * @param retval
         * @return
         */
        public synchronized void addUsed(LdapContext context, String stacktrace) {
            if (used.put(context, stacktrace) != null) {
                System.out.println("Already used context added to used.");
            }
        }

        /**
         * @return
         */
        public synchronized LdapContext getUnused() {
            LdapContext retval = null;
            if (!unused.isEmpty()) {
                Iterator iter = unused.keySet().iterator();
                while (iter.hasNext()) {
                    retval = (LdapContext) (iter.next());
                    if (minUnusedTime == ((Integer) unused.get(retval)).intValue()) {
                        break;
                    } else {
                        retval = null;
                    }
                }
                if (null == retval) {
                    retval = (LdapContext) (unused.keySet().iterator().next());
                }
                if (unused.remove(retval) == null) {
                    System.out.println("Should not happen.");
                }
            }
            return retval;
        }

        /**
         * @param context
         * @return
         */
        public synchronized boolean removeUsed(LdapContext context) {
            return used.remove(context) != null;
        }

        public synchronized LdapContext get(String stacktrace) {
            LdapContext retval = null;
            if (!unused.isEmpty()) {
                Iterator<LdapContext> iter = unused.keySet().iterator();
                while (iter.hasNext()) {
                    retval = (LdapContext) (iter.next());
                    if (minUnusedTime == ((Integer) unused.get(retval)).intValue()) {
                        break;
                    } else {
                        retval = null;
                    }
                }
                if (null == retval) {
                    retval = (LdapContext) (unused.keySet().iterator().next());
                }
                if (!singleMode && (unused.remove(retval) == null)) {
                    System.out.println("Should not happen.");
                }
                if (!singleMode && (used.put(retval, stacktrace) != null)) {
                    System.out.println("Already used context added to used.");
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(System.currentTimeMillis() + " Connection taken. Used: " + used.size() + " Unused: " + unused.size());
            }
            return retval;
        }

        /**
         * @param context
         * @param time
         * @return true if no problems occured.
         */
        public synchronized boolean release(LdapContext context, Integer time) {
            boolean retval = true;
            if (!singleMode) {
                retval = used.remove(context) != null && unused.put(context, time) == null;
                if (retval && minUnusedTime > time.intValue()) {
                    minUnusedTime = time.intValue();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(System.currentTimeMillis() + " Get connection back. Used: " + used.size() + " Unused: " + unused.size());
                }
            } else if (unused.isEmpty()) {
                unused.put(context, time);
            }
            return retval;
        }

        /**
         * @return
         */
        public synchronized int getUnusedSize() {
            return unused.size();
        }

        /**
         * @return
         */
        public synchronized int getUsedSize() {
            return used.size();
        }

        /**
         * Increases all time counters of the unused connections. With this a query on all connections is executed, so that the connections
         * isn't close. Connections which were unused for more than the keepalive will be closed. But the minimum of mincons connections
         * will be kept
         */
        private synchronized void increaseTimer() {
            /*
             * Printing out open connections: Iterator i = used.keySet().iterator(); System.out.println("blubb"); while (i.hasNext()) {
             * System.out.println("used connections: \n"); System.out.println((String) used.get(i.next())); }
             */
            if (LOG.isDebugEnabled())
                LOG.debug("increase timer for ldap started.");
            Iterator<LdapContext> it = unused.keySet().iterator();
            int cons = mincons;
            while (it.hasNext()) {
                LdapContext context = (LdapContext) it.next();
                makeTraffic(context);
                int time = (unused.get(context)).intValue();
                time++;
                unused.put(context, new Integer(time));
                if (time > keepalive) {
                    if (cons <= 0) {
                        if (LOG.isDebugEnabled())
                            LOG.debug("Removing ldap connection.");
                        try {
                            context.close();
                        } catch (NamingException ne) {
                            LOG.error("Error while closing ldap connection: " + ne.getMessage(), ne);
                        }
                        it.remove();
                    }
                    cons--;
                }
            }
            minUnusedTime++;
            if (LOG.isDebugEnabled()) {
                LOG.debug("increase timer for ldap finished.");
                Iterator<String> it2 = used.values().iterator();
                while (it.hasNext()) {
                    String trace = it2.next();
                    LOG.debug(trace);
                }
            }
        }
    }
}
