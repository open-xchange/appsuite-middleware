/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.imap.entity2acl;

import java.util.EnumSet;
import java.util.Set;

/**
 * {@link IMAPServer} - Represents an IMAP server with ACL support.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum IMAPServer {
    /**
     * Dummy unknown value.
     */
    UNKNOWN("Unknown", null, null, null),
    /**
     * Courier
     */
    COURIER("Courier", CourierEntity2ACL.getInstance(), new ArgumentGenerator() {

        @Override
        public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
            return new Object[] { Integer.valueOf(accountId), serverUrl, Integer.valueOf(sessionUser), fullname, Character.valueOf(separator), otherUserNamespaces, publicNamespaces };
        }
    }, new GreetingMatcher() {

        @Override
        public boolean matches(String greeting) {
            return toLowerCase(greeting).indexOf(toLowerCase(COURIER.getName())) >= 0;
        }
    }),
    /**
     * Cyrus
     */
    CYRUS("Cyrus", CyrusEntity2ACL.getInstance(), new ArgumentGenerator() {

        @Override
        public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
            return new Object[] { Integer.valueOf(accountId), serverUrl, Integer.valueOf(sessionUser) };
        }
    }, new GreetingMatcher() {

        @Override
        public boolean matches(String greeting) {
            return toLowerCase(greeting).indexOf(toLowerCase(CYRUS.getName())) >= 0;
        }
    }),
    /**
     * Dovecot
     */
    DOVECOT("Dovecot", DovecotEntity2ACL.getInstance(), new ArgumentGenerator() {

        @Override
        public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
            return new Object[] { Integer.valueOf(accountId), serverUrl, Integer.valueOf(sessionUser), fullname, Character.valueOf(separator), otherUserNamespaces, publicNamespaces };
        }
    }, new GreetingMatcher() {

        @Override
        public boolean matches(String greeting) {
            return toLowerCase(greeting).indexOf(toLowerCase(DOVECOT.getName())) >= 0;
        }
    }),
    /**
     * Sun Java(tm) System Messaging Server
     */
    SUN_MESSAGING_SERVER("Sun", SUNMessagingServerEntity2ACL.getInstance(), new ArgumentGenerator() {

        @Override
        public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
            return new Object[] { Integer.valueOf(accountId), serverUrl, Integer.valueOf(sessionUser) };
        }
    }, new GreetingMatcher() {

        @Override
        public boolean matches(String greeting) {
            return greeting.indexOf("Sun Java(tm) System Messaging Server") >= 0;
        }
    }),
    /**
     * MDaemon
     */
    MDAEMON("MDaemon", MDaemonEntity2ACL.getInstance(), new ArgumentGenerator() {

        @Override
        public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
            return new Object[] { Integer.valueOf(accountId), serverUrl, Integer.valueOf(sessionUser) };
        }
    }, new GreetingMatcher() {

        @Override
        public boolean matches(String greeting) {
            return toLowerCase(greeting).indexOf(toLowerCase(MDAEMON.getName())) >= 0;
        }
    }),

    ;

    private final Entity2ACL impl;
    private final String name;
    private final ArgumentGenerator argumentGenerator;
    private final GreetingMatcher greetingMatcher;

    private IMAPServer(String name, Entity2ACL impl, ArgumentGenerator argumentGenerator, GreetingMatcher greetingMatcher) {
        this.name = name;
        this.impl = impl;
        this.argumentGenerator = argumentGenerator;
        this.greetingMatcher = greetingMatcher;
    }

    /**
     * Gets the {@link Entity2ACL} implementation.
     *
     * @return The {@link Entity2ACL} implementation
     */
    public Entity2ACL getImpl() {
        return impl;
    }

    /**
     * Gets the IMAP server's alias name.
     *
     * @return The IMAP server's alias name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the needed arguments to reliably map an ACL entity to a system user and vice versa.
     *
     * @param accountId The account ID
     * @param serverUrl The server URL; e.g. <code>"mail.company.org:143"</code>
     * @param sessionUser The session user ID
     * @param fullname The IMAP folder's full name
     * @param separator The IMAP folder's separator
     * @param otherUserNamespace The user's shared namespace; e.g. <code>"shared"</code>
     * @param publicNamespaces The public namespaces
     * @return The needed arguments to reliably map an ACL entity to a system user and vice versa
     */
    public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces) {
        return argumentGenerator.getArguments(accountId, serverUrl, sessionUser, fullname, separator, otherUserNamespaces, publicNamespaces);
    }

    /**
     * Checks if specified IMAP server greeting indicates the server to be this IMAP server.
     *
     * @param greeting The IMAP server greeting to check against
     * @return <code>true</code> if specified IMAP server greeting indicates the server to be this IMAP server; otherwise <code>false</code>
     */
    public boolean matches(String greeting) {
        return greetingMatcher.matches(greeting);
    }

    private static final EnumSet<IMAPServer> SET = EnumSet.complementOf(EnumSet.of(IMAPServer.UNKNOWN));

    /**
     * Gets the IMAP servers.
     *
     * @return The IMAP servers
     */
    public static Set<IMAPServer> getIMAPServers() {
        return SET;
    }

    /**
     * Gets the class name of {@link Entity2ACL} implementation that corresponds to specified name.
     *
     * @param name The IMAP server name
     * @return The class name of {@link Entity2ACL} implementation or <code>null</code> if none matches.
     */
    public static final Entity2ACL getIMAPServerImpl(String name) {
        for (IMAPServer imapServer : getIMAPServers()) {
            if (imapServer.getName().equalsIgnoreCase(name)) {
                return imapServer.getImpl();
            }
        }
        return null;
    }

    /*-
     * Helper classes/interfaces
     */

    private static interface ArgumentGenerator {

        /**
         * Gets the arguments needed for a certain IMAP server to map ACL entity to a system user and vice versa.
         *
         * @param accountId The account ID
         * @param serverUrl The IMAP server URL
         * @param sessionUser The session user ID
         * @param fullname The IMAP folder's full name
         * @param separator The IMAP folder's separator character
         * @param otherUserNamespace The user's shared namespace; e.g. <code>"shared"</code>
         * @param publicNamespaces The public namespace
         * @return The arguments needed for a certain IMAP server to map ACL entity to a system user and vice versa
         */
        public Object[] getArguments(int accountId, String serverUrl, int sessionUser, String fullname, char separator, String[] otherUserNamespaces, String[] publicNamespaces);
    }

    private static abstract class GreetingMatcher {

        protected GreetingMatcher() {
            super();
        }

        /**
         * Turns specified {@link String} instance into lower-case.
         *
         * @param str The string
         * @return The lower-case string
         */
        protected final String toLowerCase(String str) {
            if (null == str) {
                return null;
            }
            final int length = str.length();
            final StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(Character.toLowerCase(str.charAt(i)));
            }
            return sb.toString();
        }

        /**
         * Checks if given IMAP server greeting matches a certain IMAP server.
         *
         * @param greeting The IMAP server greeting
         * @return <code>true</code> if given IMAP server greeting matches a certain IMAP server; otherwise <code>false</code>
         */
        abstract boolean matches(String greeting);
    }

}
