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

package com.openexchange.imap.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.cache.ListLsubEntry.ChangeState;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * {@link ListLsubCollection}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ListLsubCollection {

    private final ConcurrentMap<String, ListLsubEntryImpl> listMap;

    private final ConcurrentMap<String, ListLsubEntryImpl> lsubMap;

    private long stamp;

    /**
     * Initializes a new {@link ListLsubCollection}.
     * 
     * @param imapFolder The IMAP folder
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MailException If initialization fails
     */
    protected ListLsubCollection(final IMAPFolder imapFolder, final boolean doStatus, final boolean doGetAcl) throws MailException {
        super();
        listMap = new ConcurrentHashMap<String, ListLsubEntryImpl>();
        lsubMap = new ConcurrentHashMap<String, ListLsubEntryImpl>();
        init(imapFolder, doStatus, doGetAcl);
    }

    /**
     * Initializes a new {@link ListLsubCollection}.
     * 
     * @param imapStore The IMAP store
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MailException If initialization fails
     */
    protected ListLsubCollection(final IMAPStore imapStore, final boolean doStatus, final boolean doGetAcl) throws MailException {
        super();
        listMap = new ConcurrentHashMap<String, ListLsubEntryImpl>();
        lsubMap = new ConcurrentHashMap<String, ListLsubEntryImpl>();
        init(imapStore, doStatus, doGetAcl);
    }

    /**
     * Clears this collection and resets its time stamp to force re-initialization.
     */
    public void clear() {
        for (final Iterator<ListLsubEntryImpl> iterator = listMap.values().iterator(); iterator.hasNext();) {
            iterator.next().deprecated();
        }
        listMap.clear();
        for (final Iterator<ListLsubEntryImpl> iterator = lsubMap.values().iterator(); iterator.hasNext();) {
            iterator.next().deprecated();
        }
        lsubMap.clear();
        stamp = 0;
    }

    /**
     * Removes the associated entry.
     * 
     * @param fullName The full name
     */
    public void remove(final String fullName) {
        ListLsubEntryImpl entry = listMap.remove(fullName);
        if (null == entry) {
            return;
        }
        entry.deprecated();
        for (final ListLsubEntry child : entry.getChildren()) {
            remove(child.getFullName());
        }
        entry = lsubMap.remove(fullName);
        if (null != entry) {
            entry.deprecated();
            for (final ListLsubEntry child : entry.getChildren()) {
                remove(child.getFullName());
            }
        }
    }

    /**
     * Re-initializes this collection.
     * 
     * @param imapStore The IMAP store
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MailException If re-initialization fails
     */
    public void reinit(final IMAPStore imapStore, final boolean doStatus, final boolean doGetAcl) throws MailException {
        clear();
        init(imapStore, doStatus, doGetAcl);
    }

    /**
     * Re-initializes this collection.
     * 
     * @param imapFolder The IMAP folder
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MailException If re-initialization fails
     */
    public void reinit(final IMAPFolder imapFolder, final boolean doStatus, final boolean doGetAcl) throws MailException {
        clear();
        init(imapFolder, doStatus, doGetAcl);
    }

    private void init(final IMAPStore imapStore, final boolean doStatus, final boolean doGetAcl) throws MailException {
        try {
            init((IMAPFolder) imapStore.getFolder("INBOX"), doStatus, doGetAcl);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    private void init(final IMAPFolder imapFolder, final boolean doStatus, final boolean doGetAcl) throws MailException {
        try {
            /*
             * Perform LIST "" ""
             */
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doRootListCommand(protocol);
                    return null;
                }

            });
            /*
             * Perform LIST "*" %
             */
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doListLsubCommand(protocol, false);
                    return null;
                }

            });
            /*
             * Perform LSUB "*" %
             */
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doListLsubCommand(protocol, true);
                    return null;
                }

            });
            if (doStatus) {
                /*
                 * Gather STATUS for each entry
                 */
                for (final Iterator<ListLsubEntryImpl> iter = listMap.values().iterator(); iter.hasNext();) {
                    final ListLsubEntryImpl listEntry = iter.next();
                    if (listEntry.canOpen()) {
                        try {
                            final String fullName = listEntry.getFullName();
                            final int[] status = IMAPCommandsCollection.getStatus(fullName, imapFolder);
                            if (null != status) {
                                listEntry.setStatus(status);
                                final ListLsubEntryImpl lsubEntry = lsubMap.get(fullName);
                                if (null != lsubEntry) {
                                    lsubEntry.setStatus(status);
                                }
                            }
                        } catch (final Exception e) {
                            // Swallow failed STATUS command
                            org.apache.commons.logging.LogFactory.getLog(ListLsubCollection.class).debug(
                                "STATUS command failed for " + imapFolder.getStore().toString(),
                                e);
                        }
                    }
                }
            }
            if (doGetAcl && ((IMAPStore) imapFolder.getStore()).hasCapability("ACL")) {
                /*
                 * Perform GETACL command for each entry
                 */
                for (final Iterator<ListLsubEntryImpl> iter = listMap.values().iterator(); iter.hasNext();) {
                    final ListLsubEntryImpl listEntry = iter.next();
                    if (listEntry.canOpen()) {
                        try {
                            final String fullName = listEntry.getFullName();
                            final List<ACL> aclList = IMAPCommandsCollection.getAcl(fullName, imapFolder, false);
                            listEntry.setAcls(aclList);
                            final ListLsubEntryImpl lsubEntry = lsubMap.get(fullName);
                            if (null != lsubEntry) {
                                lsubEntry.setAcls(aclList);
                            }
                        } catch (final Exception e) {
                            // Swallow failed ACL command
                            org.apache.commons.logging.LogFactory.getLog(ListLsubCollection.class).debug(
                                "ACL command failed for " + imapFolder.getStore().toString(),
                                e);
                        }
                    }
                }
            }
            /*
             * Set time stamp
             */
            stamp = System.currentTimeMillis();
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    void doFolderListLsubCommand(final String fullName, final IMAPProtocol protocol, final boolean lsub, final Set<String> fullNames) throws ProtocolException {
        /*
         * Get sub-tree starting at specified full name
         */
        final String command = lsub ? "LSUB" : "LIST";
        final Response[] r = protocol.command(command + " \"" + BASE64MailboxEncoder.encode(fullName) + "\" \"*\"", null);
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            final ConcurrentMap<String, ListLsubEntryImpl> map = lsub ? lsubMap : listMap;
            final ListLsubEntryImpl rootEntry = map.get("");
            for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    final ListLsubEntryImpl listLsubEntry = parseListResponse(ir, lsub ? null : lsubMap);
                    final String fn = listLsubEntry.getFullName();
                    fullNames.add(fn);
                    final ListLsubEntryImpl oldEntry = map.get(fn);
                    final int pos = fn.lastIndexOf(listLsubEntry.getSeparator());
                    if (pos >= 0) {
                        /*
                         * Non-root level
                         */
                        final ListLsubEntryImpl parent = map.get(fn.substring(0, pos));
                        if (null != parent) {
                            listLsubEntry.setParent(parent);
                            parent.replaceChild(listLsubEntry, oldEntry);
                        }
                    } else {
                        /*
                         * Root level
                         */
                        listLsubEntry.setParent(rootEntry);
                        rootEntry.replaceChild(listLsubEntry, oldEntry);
                    }
                    map.put(fn, listLsubEntry);
                    r[i] = null;
                }
            }
        }
        /*
         * Dispatch remaining untagged responses
         */
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
    }

    /**
     * Updates a sub-tree starting at specified full name.
     * 
     * @param fullName The full name of the starting folder node
     * @param imapStore The connected IMAP store
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MailException If update fails
     */
    public void update(final String fullName, final IMAPStore imapStore, final boolean doStatus, final boolean doGetAcl) throws MailException {
        try {
            update(fullName, (IMAPFolder) imapStore.getFolder("INBOX"), doStatus, doGetAcl);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Updates a sub-tree starting at specified full name.
     * 
     * @param fullName The full name of the starting folder node
     * @param imapFolder An IMAP folder providing connected protocol
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MailException If update fails
     */
    public void update(final String fullName, final IMAPFolder imapFolder, final boolean doStatus, final boolean doGetAcl) throws MailException {
        try {
            /*
             * Perform LIST "<full-name>" "*"
             */
            final Set<String> fullNames = new HashSet<String>(8);
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doFolderListLsubCommand(fullName, protocol, false, fullNames);
                    return null;
                }

            });
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doFolderListLsubCommand(fullName, protocol, true, fullNames);
                    return null;
                }

            });
            if (doStatus) {
                /*
                 * Gather STATUS for each entry
                 */
                for (final String fn : fullNames) {
                    final ListLsubEntryImpl listEntry = listMap.get(fn);
                    if (listEntry.canOpen()) {
                        try {
                            final int[] status = IMAPCommandsCollection.getStatus(fn, imapFolder);
                            if (null != status) {
                                listEntry.setStatus(status);
                                final ListLsubEntryImpl lsubEntry = lsubMap.get(fn);
                                if (null != lsubEntry) {
                                    lsubEntry.setStatus(status);
                                }
                            }
                        } catch (final Exception e) {
                            // Swallow failed STATUS command
                            org.apache.commons.logging.LogFactory.getLog(ListLsubCollection.class).debug(
                                "STATUS command failed for " + imapFolder.getStore().toString(),
                                e);
                        }
                    }
                }
            }
            if (doGetAcl && ((IMAPStore) imapFolder.getStore()).hasCapability("ACL")) {
                /*
                 * Perform GETACL command for each entry
                 */
                for (final String fn : fullNames) {
                    final ListLsubEntryImpl listEntry = listMap.get(fn);
                    if (listEntry.canOpen()) {
                        try {
                            final List<ACL> aclList = IMAPCommandsCollection.getAcl(fn, imapFolder, false);
                            listEntry.setAcls(aclList);
                            final ListLsubEntryImpl lsubEntry = lsubMap.get(fn);
                            if (null != lsubEntry) {
                                lsubEntry.setAcls(aclList);
                            }
                        } catch (final Exception e) {
                            // Swallow failed ACL command
                            org.apache.commons.logging.LogFactory.getLog(ListLsubCollection.class).debug(
                                "ACL command failed for " + imapFolder.getStore().toString(),
                                e);
                        }
                    }
                }
            }
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
    }

    /**
     * Performs a LIST/LSUB command with specified IMAP protocol.
     * 
     * @param protocol The IMAP protocol
     * @param lsub <code>true</code> to perform a LSUB command; otherwise <code>false</code> for LIST
     * @throws ProtocolException If a protocol error occurs
     */
    void doListLsubCommand(final IMAPProtocol protocol, final boolean lsub) throws ProtocolException {
        /*
         * Perform command
         */
        final String command = lsub ? "LSUB" : "LIST";
        final Response[] r = protocol.command(command + " \"*\" %", null);
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            final ConcurrentMap<String, ListLsubEntryImpl> map = lsub ? lsubMap : listMap;
            final ListLsubEntryImpl rootEntry = map.get("");
            for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    final ListLsubEntryImpl listLsubEntry = parseListResponse(ir, lsub ? null : lsubMap);
                    final String fullName = listLsubEntry.getFullName();
                    final int pos = fullName.lastIndexOf(listLsubEntry.getSeparator());
                    if (pos >= 0) {
                        /*
                         * Non-root level
                         */
                        final ListLsubEntryImpl parent = map.get(fullName.substring(0, pos));
                        if (null != parent) {
                            listLsubEntry.setParent(parent);
                            parent.addChild(listLsubEntry);
                        }
                    } else {
                        /*
                         * Root level
                         */
                        listLsubEntry.setParent(rootEntry);
                        rootEntry.addChild(listLsubEntry);
                    }
                    map.put(fullName, listLsubEntry);
                    r[i] = null;
                }
            }
        }
        /*
         * Dispatch remaining untagged responses
         */
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
    }

    /**
     * Performs a LIST command for root folder with specified IMAP protocol.
     * 
     * @param protocol The IMAP protocol
     * @throws ProtocolException If a protocol error occurs
     */
    void doRootListCommand(final IMAPProtocol protocol) throws ProtocolException {
        /*
         * Perform command: LIST "" ""
         */
        final Response[] r = protocol.command("LIST \"\" \"\"", null);
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            final String cmd = "LIST";
            for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(cmd)) {
                    final ListLsubEntryImpl listLsubEntry = parseListResponse(ir, null);
                    listMap.put("", listLsubEntry);
                    lsubMap.put("", listLsubEntry);
                    r[i] = null;
                }
            }
        }
        /*
         * Dispatch remaining untagged responses
         */
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
    }

    /**
     * Gets the initialization time stamp.
     * 
     * @return The stamp
     */
    public long getStamp() {
        return stamp;
    }

    /**
     * Gets child LIST entries for specified full name.
     * 
     * @param fullName The full name
     * @return The child LIST entries
     */
    public List<ListLsubEntry> getListChildren(final String fullName) {
        final Iterator<Entry<String, ListLsubEntryImpl>> iter = listMap.entrySet().iterator();
        if (!iter.hasNext()) {
            return Collections.emptyList();
        }
        final List<ListLsubEntry> list = new ArrayList<ListLsubEntry>(4);
        final Entry<String, ListLsubEntryImpl> first = iter.next();
        final char separator = first.getValue().getSeparator();
        if (isChild(fullName, separator, first.getKey())) {
            list.add(first.getValue());
        }
        while (iter.hasNext()) {
            final Entry<String, ListLsubEntryImpl> entry = iter.next();
            if (isChild(fullName, separator, entry.getKey())) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    /**
     * Checks for any subscribed subfolder in IMAP folder tree.
     * 
     * @param fullName The full name
     * @return <code>true</code> if a subscribed subfolder exists; otherwise <code>false</code>
     */
    public boolean hasAnySubscribedSubfolder(final String fullName) {
        for (final Iterator<String> iter = lsubMap.keySet().iterator(); iter.hasNext();) {
            final String fn = iter.next();
            if (fn.startsWith(fullName) && !fn.equals(fullName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets child LSUB entries for specified full name.
     * 
     * @param fullName The full name
     * @return The child LSUB entries
     */
    public List<ListLsubEntry> getLsubChildren(final String fullName) {
        final Iterator<Entry<String, ListLsubEntryImpl>> iter = lsubMap.entrySet().iterator();
        if (!iter.hasNext()) {
            return Collections.emptyList();
        }
        final List<ListLsubEntry> list = new ArrayList<ListLsubEntry>(4);
        final Entry<String, ListLsubEntryImpl> first = iter.next();
        final char separator = first.getValue().getSeparator();
        if (isChild(fullName, separator, first.getKey())) {
            list.add(first.getValue());
        }
        while (iter.hasNext()) {
            final Entry<String, ListLsubEntryImpl> entry = iter.next();
            if (isChild(fullName, separator, entry.getKey())) {
                list.add(entry.getValue());
            }
        }
        return list;
    }

    private static boolean isChild(final String parent, final char separator, final String fn) {
        if (!fn.startsWith(parent) || fn.equals(parent)) {
            return false;
        }
        return fn.lastIndexOf(separator, parent.length() + 1) < 0 ? true : false;
    }

    /**
     * Checks if this collection contains a LIST entry for specified full name.
     * 
     * @param fullName The full name
     * @return <code>true</code> if this collection contains a LIST entry for specified full name; otherwise <code>false</code>
     */
    public boolean containsList(final String fullName) {
        return listMap.containsKey(fullName);
    }

    /**
     * Gets the LIST entry for specified full name.
     * 
     * @param fullName The full name
     * @return The LIST entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getList(final String fullName) {
        return listMap.get(fullName);
    }

    /**
     * Checks if this collection contains a LSUB entry for specified full name.
     * 
     * @param fullName The full name
     * @return <code>true</code> if this collection contains a LSUB entry for specified full name; otherwise <code>false</code>
     */
    public boolean containsLsub(final String fullName) {
        return lsubMap.containsKey(fullName);
    }

    /**
     * Gets the LSUB entry for specified full name.
     * 
     * @param fullName The full name
     * @return The LSUB entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getLsub(final String fullName) {
        return lsubMap.get(fullName);
    }

    private static ListLsubEntryImpl parseListResponse(final IMAPResponse listResponse, final ConcurrentMap<String, ListLsubEntryImpl> lsubMap) {
        /*
         * LIST (\NoInferiors \UnMarked) "/" "Sent Items"
         */
        final String[] s = listResponse.readSimpleList();
        /*
         * Check attributes
         */
        final Set<String> attributes;
        ChangeState changeState = ChangeState.UNDEFINED;
        boolean canOpen = true;
        boolean hasInferiors = true;
        if (s != null) {
            /*
             * Non-empty attribute list
             */
            attributes = new HashSet<String>(s.length);
            for (int i = 0; i < s.length; i++) {
                final String attr = s[i].toLowerCase(Locale.US);
                if ("\\marked".equals(attr)) {
                    changeState = ChangeState.CHANGED;
                } else if ("\\unmarked".equals(attr)) {
                    changeState = ChangeState.UNCHANGED;
                } else if ("\\noselect".equals(attr)) {
                    canOpen = false;
                } else if (attr.equals("\\noinferiors")) {
                    hasInferiors = false;
                }
                attributes.add(attr);
            }
        } else {
            attributes = Collections.emptySet();
        }
        /*
         * Read separator character
         */
        char separator = '/';
        listResponse.skipSpaces();
        if (listResponse.readByte() == '"') {
            if ((separator = (char) listResponse.readByte()) == '\\') {
                /*
                 * Escaped separator character
                 */
                separator = (char) listResponse.readByte();
            }
            listResponse.skip(1);
        } else {
            listResponse.skip(2);
        }
        /*
         * Read full name; decode the name (using RFC2060's modified UTF7)
         */
        listResponse.skipSpaces();
        final String name = BASE64MailboxDecoder.decode(listResponse.readAtomString());
        /*
         * Return
         */
        return new ListLsubEntryImpl(name, attributes, separator, changeState, hasInferiors, canOpen, lsubMap);
    }

    /**
     * Creates an empty {@link ListLsubEntry} for specified full name.
     * 
     * @param fullName The full name
     * @return An empty {@link ListLsubEntry}
     */
    static ListLsubEntry emptyEntryFor(final String fullName) {
        return new EmptyListLsubEntry(fullName);
    }

    private static class EmptyListLsubEntry implements ListLsubEntry {

        private final String fullName;

        public EmptyListLsubEntry(final String fullName) {
            super();
            this.fullName = fullName;
        }

        public String getName() {
            return fullName.substring(fullName.lastIndexOf('/') + 1);
        }

        public boolean exists() {
            return false;
        }

        public ListLsubEntry getParent() {
            return null;
        }

        public List<ListLsubEntry> getChildren() {
            return Collections.emptyList();
        }

        public String getFullName() {
            return fullName;
        }

        public Set<String> getAttributes() {
            return Collections.emptySet();
        }

        public char getSeparator() {
            return '/';
        }

        public ChangeState getChangeState() {
            return ChangeState.UNCHANGED;
        }

        public boolean hasInferiors() {
            return false;
        }

        public boolean canOpen() {
            return false;
        }

        public int getType() {
            return 0;
        }

        public boolean isSubscribed() {
            return false;
        }

        public int getMessageCount() {
            return -1;
        }

        public int getNewMessageCount() {
            return -1;
        }

        public int getUnreadMessageCount() {
            return -1;
        }

        public List<ACL> getACLs() {
            return null;
        }

    }

    /**
     * A LIST/LSUB entry.
     */
    private static final class ListLsubEntryImpl implements ListLsubEntry {

        private ListLsubEntry parent;

        private List<ListLsubEntry> children;

        private int[] status;

        private final String fullName;

        private final Set<String> attributes;

        private final char separator;

        private final ChangeState changeState;

        private final boolean hasInferiors;

        private final boolean canOpen;

        private final int type;

        private final ConcurrentMap<String, ListLsubEntryImpl> lsubMap;

        private List<ACL> acls;

        private boolean deprecated;

        ListLsubEntryImpl(final String fullName, final Set<String> attributes, final char separator, final ChangeState changeState, final boolean hasInferiors, final boolean canOpen, final ConcurrentMap<String, ListLsubEntryImpl> lsubMap) {
            super();
            this.deprecated = false;
            this.fullName = fullName;
            this.attributes = attributes;
            this.separator = separator;
            this.changeState = changeState;
            this.hasInferiors = hasInferiors;
            this.canOpen = canOpen;
            int type = 0;
            if (hasInferiors) {
                type |= Folder.HOLDS_FOLDERS;
            }
            if (canOpen) {
                type |= Folder.HOLDS_MESSAGES;
            }
            this.type = type;
            this.lsubMap = lsubMap;
        }

        /**
         * Marks this entry as deprecated.
         */
        void deprecated() {
            deprecated = true;
        }

        private void checkDeprecated() {
            if (deprecated) {
                throw new ListLsubRuntimeException("LIST/LSUB entry is deprecated.");
            }
        }

        public String getName() {
            checkDeprecated();
            return fullName.substring(fullName.lastIndexOf(separator) + 1);
        }

        /**
         * Sets this LIST/LSUB entry's parent.
         * 
         * @param parent The parent
         */
        void setParent(final ListLsubEntry parent) {
            this.parent = parent;
        }

        public ListLsubEntry getParent() {
            checkDeprecated();
            return parent;
        }

        /**
         * Adds specified LIST/LSUB entry to this LIST/LSUB entry's children
         * 
         * @param child The child LIST/LSUB entry
         */
        void addChild(final ListLsubEntry child) {
            if (null == child) {
                return;
            }
            if (null == children) {
                children = new CopyOnWriteArrayList<ListLsubEntry>();
            }
            children.add(child);
        }

        void replaceChild(final ListLsubEntry newChild, final ListLsubEntry oldChild) {
            if (null == oldChild) {
                if (null == children) {
                    children = new CopyOnWriteArrayList<ListLsubEntry>();
                }
                children.add(newChild);
            } else {
                if (null == children) {
                    children = new CopyOnWriteArrayList<ListLsubEntry>();
                } else {
                    children.remove(oldChild);
                }
                children.add(newChild);
            }
        }

        public List<ListLsubEntry> getChildren() {
            checkDeprecated();
            return null == children ? Collections.<ListLsubEntry> emptyList() : Collections.unmodifiableList(children);
        }

        public String getFullName() {
            checkDeprecated();
            return fullName;
        }

        public Set<String> getAttributes() {
            checkDeprecated();
            return attributes;
        }

        public char getSeparator() {
            checkDeprecated();
            return separator;
        }

        public ChangeState getChangeState() {
            checkDeprecated();
            return changeState;
        }

        public boolean hasInferiors() {
            checkDeprecated();
            return hasInferiors;
        }

        public boolean canOpen() {
            checkDeprecated();
            return canOpen;
        }

        public int getType() {
            checkDeprecated();
            return type;
        }

        public boolean exists() {
            checkDeprecated();
            return true;
        }

        public boolean isSubscribed() {
            checkDeprecated();
            return null == lsubMap ? true : lsubMap.containsKey(fullName);
        }

        /**
         * Sets the status.
         * 
         * @param status The status
         */
        void setStatus(final int[] status) {
            if (null == status) {
                this.status = null;
                return;
            }
            this.status = new int[status.length];
            System.arraycopy(status, 0, this.status, 0, status.length);
        }

        public int getMessageCount() {
            checkDeprecated();
            return null == status ? -1 : status[0];
        }

        public int getNewMessageCount() {
            checkDeprecated();
            return null == status ? -1 : status[1];
        }

        public int getUnreadMessageCount() {
            checkDeprecated();
            return null == status ? -1 : status[2];
        }

        /**
         * Sets the ACLs.
         * 
         * @param acls The ACL list
         */
        void setAcls(final List<ACL> acls) {
            this.acls = acls;
        }

        public List<ACL> getACLs() {
            checkDeprecated();
            return acls == null ? null : new ArrayList<ACL>(acls);
        }

    } // End of class ListLsubEntryImpl

}
