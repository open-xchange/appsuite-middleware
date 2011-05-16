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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.cache.ListLsubEntry.ChangeState;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.MIMEMailException;
import com.sun.mail.iap.Argument;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ListLsubCollection.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final String ROOT_FULL_NAME = "";

    private final ConcurrentMap<String, ListLsubEntryImpl> listMap;

    private final ConcurrentMap<String, ListLsubEntryImpl> lsubMap;

    private final AtomicBoolean deprecated;

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
        deprecated = new AtomicBoolean();
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
        deprecated = new AtomicBoolean();
        init(imapStore, doStatus, doGetAcl);
    }

    private void checkDeprecated() {
        if (deprecated.get()) {
            throw new ListLsubRuntimeException("LIST/LSUB cache is deprecated.");
        }
    }

    /**
     * Checks if this collection is marked as deprecated.
     * 
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     */
    public boolean isDeprecated() {
        return deprecated.get();
    }

    /**
     * Clears this collection and resets its time stamp to force re-initialization.
     */
    public void clear() {
        deprecated.set(true);
        stamp = 0;
        if (DEBUG) {
            LOG.debug("Cleared LIST/LSUB cache.");
        }
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
        for (final ListLsubEntry child : entry.getChildrenSet()) {
            remove(child.getFullName());
        }
        entry = lsubMap.remove(fullName);
        if (null != entry) {
            for (final ListLsubEntry child : entry.getChildrenSet()) {
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
            // listMap.clear();
            // lsubMap.clear();
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
            deprecated.set(false);
        } catch (final MessagingException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
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
        if (deprecated.get() || ROOT_FULL_NAME.equals(fullName)) {
            init(imapFolder, doStatus, doGetAcl);
            return;
        }
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
                    if (null != listEntry && listEntry.canOpen()) {
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
                    if (null != listEntry && listEntry.canOpen()) {
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

    private static final Set<String> ATTRIBUTES_NON_EXISTING_PARENT = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "\\noselect",
        "\\haschildren")));

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
        final Response[] r;
        if (DEBUG) {
            final String sCmd = new StringBuilder(command).append(" \"\" \"*\"").toString();
            r = protocol.command(sCmd, null);
            LOG.debug((lsub ? "LSUB" : "LIST") + " cache filled with >>" + sCmd + "<< which returned " + r.length + " response line(s).");
        } else {
            r = protocol.command(new StringBuilder(command).append(" \"\" \"*\"").toString(), null);
        }
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            final ConcurrentMap<String, ListLsubEntryImpl> map = lsub ? lsubMap : listMap;
            final Map<String, List<ListLsubEntryImpl>> parentMap = new HashMap<String, List<ListLsubEntryImpl>>(r.length);
            char separator = '\0';
            final Set<String> unsubscribeFullNames = lsub ? new HashSet<String>(4) : Collections.<String> emptySet();
            final Set<String> removeFullNames = new HashSet<String>(map.keySet());
            removeFullNames.remove(ROOT_FULL_NAME);
            final ListLsubEntryImpl rootEntry = map.get(ROOT_FULL_NAME);
            NextResponse: for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    ListLsubEntryImpl listLsubEntry = parseListResponse(ir, lsub ? null : lsubMap);
                    final String fullName = listLsubEntry.getFullName();
                    if (lsub && !listMap.containsKey(fullName)) {
                        /*
                         * Found a subscribed folder that does no more exist
                         */
                        unsubscribeFullNames.add(fullName);
                        r[i] = null;
                        continue NextResponse;
                    }
                    removeFullNames.remove(fullName);
                    {
                        final ListLsubEntryImpl oldEntry = map.get(fullName);
                        if (oldEntry == null) {
                            /*
                             * Wasn't in map before
                             */
                            map.put(fullName, listLsubEntry);
                        } else {
                            /*
                             * Already contained in map
                             */
                            oldEntry.clearChildren();
                            oldEntry.copyFrom(listLsubEntry);
                            listLsubEntry = oldEntry;
                        }
                    }
                    /*
                     * Determine parent
                     */
                    final int pos = fullName.lastIndexOf((separator = listLsubEntry.getSeparator()));
                    if (pos >= 0) {
                        /*
                         * Non-root level
                         */
                        final String parentFullName = fullName.substring(0, pos);
                        final ListLsubEntryImpl parent = map.get(parentFullName);
                        if (null == parent) {
                            /*
                             * Parent not (yet) in map
                             */
                            List<ListLsubEntryImpl> children = parentMap.get(parentFullName);
                            if (null == children) {
                                children = new ArrayList<ListLsubCollection.ListLsubEntryImpl>(8);
                                parentMap.put(parentFullName, children);
                            }
                            children.add(listLsubEntry);
                        } else {
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
                    r[i] = null;
                }
            }
            /*
             * Handle parent map
             */
            handleParentMap(parentMap, separator, rootEntry, lsub, map, removeFullNames, false);
            /*
             * Drop removed folders
             */
            if (!removeFullNames.isEmpty()) {
                for (final String removeFullName : removeFullNames) {
                    map.remove(removeFullName);
                }
            }
            /*
             * Unsubscribe folders
             */
            if (!unsubscribeFullNames.isEmpty()) {
                for (final String unsubscribeFullName : unsubscribeFullNames) {
                    final Argument args = new Argument();
                    args.writeString(BASE64MailboxEncoder.encode(unsubscribeFullName));
                    protocol.command("UNSUBSCRIBE", args);
                }
            }
            /*
             * Debug logs
             */
            if (DEBUG) {
                final TreeMap<String, ListLsubEntryImpl> tm = new TreeMap<String, ListLsubEntryImpl>();
                tm.putAll(map);
                final StringBuilder sb = new StringBuilder(1024);
                sb.append((lsub ? "LSUB" : "LIST") + " cache contains after (re-)initialization:\n");
                for (final Entry<String, ListLsubEntryImpl> entry : tm.entrySet()) {
                    sb.append(entry.getValue()).append('\n');
                }
                LOG.debug(sb.toString());
            }
        }
        /*
         * Dispatch remaining untagged responses
         */
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
    }

    /**
     * @param fullName
     * @param protocol
     * @param lsub
     * @param fullNames
     * @throws ProtocolException
     */
    void doFolderListLsubCommand(final String fullName, final IMAPProtocol protocol, final boolean lsub, final Set<String> fullNames) throws ProtocolException {
        /*
         * Get sub-tree starting at specified full name
         */
        final String command = lsub ? "LSUB" : "LIST";
        final Response[] r;
        if (DEBUG) {
            final String sCmd = new StringBuilder(command).append(" \"").append(BASE64MailboxEncoder.encode(fullName)).append("\" \"*\"").toString();
            r = protocol.command(sCmd, null);
            LOG.debug((lsub ? "LSUB" : "LIST") + " cache will be updated with >>" + sCmd + "<< which returned " + r.length + " response line(s).");
        } else {
            r = protocol.command(
                new StringBuilder(command).append(" \"").append(BASE64MailboxEncoder.encode(fullName)).append("\" \"*\"").toString(),
                null);
        }
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            final ConcurrentMap<String, ListLsubEntryImpl> map = lsub ? lsubMap : listMap;
            final Map<String, List<ListLsubEntryImpl>> parentMap = new HashMap<String, List<ListLsubEntryImpl>>(r.length);
            char separator = '\0';
            final ListLsubEntryImpl rootEntry = map.get(ROOT_FULL_NAME);
            for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    ListLsubEntryImpl listLsubEntry = parseListResponse(ir, lsub ? null : lsubMap);
                    final String fn = listLsubEntry.getFullName();
                    fullNames.add(fn);
                    {
                        final ListLsubEntryImpl oldEntry = map.get(fn);
                        if (null == oldEntry) {
                            map.put(fn, listLsubEntry);
                        } else {
                            oldEntry.clearChildren();
                            oldEntry.copyFrom(listLsubEntry);
                            listLsubEntry = oldEntry;
                        }
                    }
                    /*
                     * Determine parent
                     */
                    final int pos = fn.lastIndexOf((separator = listLsubEntry.getSeparator()));
                    if (pos >= 0) {
                        /*
                         * Non-root level
                         */
                        final String parentFullName = fn.substring(0, pos);
                        final ListLsubEntryImpl parent = map.get(parentFullName);
                        if (null == parent) {
                            /*
                             * Parent not (yet) in map
                             */
                            List<ListLsubEntryImpl> children = parentMap.get(parentFullName);
                            if (null == children) {
                                children = new ArrayList<ListLsubCollection.ListLsubEntryImpl>(8);
                                parentMap.put(parentFullName, children);
                            }
                            children.add(listLsubEntry);
                        } else {
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
                    r[i] = null;
                }
            }
            /*
             * Handle children
             */
            handleParentMap(parentMap, separator, rootEntry, lsub, map, fullNames, true);
            /*
             * Debug logs
             */
            if (DEBUG) {
                final TreeMap<String, ListLsubEntryImpl> tm = new TreeMap<String, ListLsubEntryImpl>();
                tm.putAll(map);
                final StringBuilder sb = new StringBuilder(1024);
                sb.append((lsub ? "LSUB" : "LIST") + " cache contains after update:\n");
                for (final Entry<String, ListLsubEntryImpl> entry : tm.entrySet()) {
                    sb.append(entry.getValue()).append('\n');
                }
                LOG.debug(sb.toString());
            }
        }
        /*
         * Dispatch remaining untagged responses
         */
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
    }

    /**
     * Handles specified parent map.
     * 
     * @param parentMap The parent map
     * @param separator The separator character
     * @param rootEntry The root entry
     * @param lsub <code>true</code> for <code>LSUB</code>; otherwise <code>false</code> for <code>LIST</code>
     * @param map The entry map
     * @param set The set of full names
     * @param add <code>true</code> to add to <code>set</code> parameter; otherwise <code>false</code> to remove from it
     */
    private void handleParentMap(final Map<String, List<ListLsubEntryImpl>> parentMap, final char separator, final ListLsubEntryImpl rootEntry, final boolean lsub, final ConcurrentMap<String, ListLsubEntryImpl> map, final Set<String> set, final boolean add) {
        /*
         * Handle children
         */
        boolean handleChildren = true;
        while (handleChildren) {
            handleChildren = false;
            String grandFullName = null;
            ListLsubEntryImpl newEntry = null;
            Next: for (final Entry<String, List<ListLsubEntryImpl>> entry : parentMap.entrySet()) {
                final String parentFullName = entry.getKey();
                ListLsubEntryImpl parent = map.get(parentFullName);
                if (null == parent) {
                    /*
                     * Add dummy parent
                     */
                    parent = new ListLsubEntryImpl(
                        parentFullName,
                        ATTRIBUTES_NON_EXISTING_PARENT,
                        separator,
                        ChangeState.UNDEFINED,
                        true,
                        false,
                        lsub ? null : lsubMap);
                    map.put(parentFullName, parent);
                    if (add) {
                        set.add(parentFullName);
                    } else {
                        set.remove(parentFullName);
                    }
                    final int pos = parentFullName.lastIndexOf(separator);
                    if (pos >= 0) {
                        grandFullName = parentFullName.substring(0, pos);
                        newEntry = parent;
                        break Next;
                    }
                    /*
                     * Grand parent is root folder
                     */
                    parent.setParent(rootEntry);
                    rootEntry.addChildIfAbsent(parent);
                }
                for (final ListLsubEntryImpl child : entry.getValue()) {
                    child.setParent(parent);
                    parent.addChildIfAbsent(child);
                }
            }
            if (grandFullName != null && newEntry != null) {
                List<ListLsubEntryImpl> children = parentMap.get(grandFullName);
                if (null == children) {
                    children = new ArrayList<ListLsubCollection.ListLsubEntryImpl>(8);
                    parentMap.put(grandFullName, children);
                }
                if (!children.contains(newEntry)) {
                    children.add(newEntry);
                }
                /*
                 * Next loop...
                 */
                handleChildren = true;
            }
        }
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
                    {
                        final ListLsubEntryImpl oldEntry = listMap.get(ROOT_FULL_NAME);
                        if (null == oldEntry) {
                            listMap.put(ROOT_FULL_NAME, listLsubEntry);
                            lsubMap.put(ROOT_FULL_NAME, listLsubEntry);
                        } else {
                            oldEntry.clearChildren();
                            oldEntry.copyFrom(listLsubEntry);
                        }
                    }
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
     * Gets the time stamp when last initialization was performed.
     * 
     * @return The stamp of last initialization
     */
    public long getStamp() {
        return stamp;
    }

    /**
     * Checks for any subscribed subfolder in IMAP folder tree located below denoted folder.
     * 
     * @param fullName The full name
     * @return <code>true</code> if a subscribed subfolder exists; otherwise <code>false</code>
     */
    public boolean hasAnySubscribedSubfolder(final String fullName) {
        checkDeprecated();
        final ListLsubEntryImpl parent = lsubMap.get(fullName);
        if (null != parent && !parent.getChildrenSet().isEmpty()) {
            return true;
        }
        for (final Iterator<String> iter = lsubMap.keySet().iterator(); iter.hasNext();) {
            final String fn = iter.next();
            if (fn.startsWith(fullName) && !fn.equals(fullName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the LIST entry for specified full name.
     * 
     * @param fullName The full name
     * @return The LIST entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getList(final String fullName) {
        checkDeprecated();
        return listMap.get(fullName);
    }

    /**
     * Gets the LSUB entry for specified full name.
     * 
     * @param fullName The full name
     * @return The LSUB entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getLsub(final String fullName) {
        checkDeprecated();
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

        public void rememberACLs(final List<ACL> aclList) {
            // Nothing to do
        }

        public void rememberCounts(final int total, final int recent, final int unseen) {
            // Nothing to do
        }

    }

    /**
     * A LIST/LSUB entry.
     */
    private static final class ListLsubEntryImpl implements ListLsubEntry, Comparable<ListLsubEntryImpl> {

        private ListLsubEntry parent;

        private Set<ListLsubEntryImpl> children;

        private int[] status;

        private final String fullName;

        private Set<String> attributes;

        private char separator;

        private ChangeState changeState;

        private boolean hasInferiors;

        private boolean canOpen;

        private int type;

        private final ConcurrentMap<String, ListLsubEntryImpl> lsubMap;

        private List<ACL> acls;

        ListLsubEntryImpl(final String fullName, final Set<String> attributes, final char separator, final ChangeState changeState, final boolean hasInferiors, final boolean canOpen, final ConcurrentMap<String, ListLsubEntryImpl> lsubMap) {
            super();
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

        void copyFrom(final ListLsubEntryImpl newEntry) {
            if (newEntry == null) {
                return;
            }
            attributes = newEntry.attributes;
            canOpen = newEntry.canOpen;
            changeState = newEntry.changeState;
            hasInferiors = newEntry.hasInferiors;
            separator = newEntry.separator;
            type = newEntry.type;
        }

        void clearChildren() {
            if (children != null) {
                children.clear();
            }
        }

        public String getName() {
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
            return parent;
        }

        /**
         * Adds specified LIST/LSUB entry to this LIST/LSUB entry's children
         * 
         * @param child The child LIST/LSUB entry
         */
        void addChild(final ListLsubEntryImpl child) {
            if (null == child) {
                return;
            }
            if (null == children) {
                children = new HashSet<ListLsubEntryImpl>(8);
                children.add(child);
            } else {
                if (!children.add(child)) {
                    /*
                     * Remove previous entry and add again
                     */
                    children.remove(child);
                    children.add(child);
                }
            }
        }

        /**
         * Adds (if absent) specified LIST/LSUB entry to this LIST/LSUB entry's children
         * 
         * @param child The child LIST/LSUB entry
         */
        void addChildIfAbsent(final ListLsubEntryImpl child) {
            if (null == child) {
                return;
            }
            if (null == children) {
                children = new HashSet<ListLsubEntryImpl>(8);
            }
            children.add(child);
        }

        public List<ListLsubEntry> getChildren() {
            return null == children ? Collections.<ListLsubEntry> emptyList() : new ArrayList<ListLsubEntry>(children);
        }

        Set<? extends ListLsubEntry> getChildrenSet() {
            return null == children ? Collections.<ListLsubEntryImpl> emptySet() : children;
        }

        public String getFullName() {
            return fullName;
        }

        public Set<String> getAttributes() {
            return attributes;
        }

        public char getSeparator() {
            return separator;
        }

        public ChangeState getChangeState() {
            return changeState;
        }

        public boolean hasInferiors() {
            return hasInferiors;
        }

        public boolean canOpen() {
            return canOpen;
        }

        public int getType() {
            return type;
        }

        public boolean exists() {
            return true;
        }

        public boolean isSubscribed() {
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
            return null == status ? -1 : status[0];
        }

        public int getNewMessageCount() {
            return null == status ? -1 : status[1];
        }

        public int getUnreadMessageCount() {
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
            return acls == null ? null : new ArrayList<ACL>(acls);
        }

        public void rememberACLs(final List<ACL> aclList) {
            this.acls = new ArrayList<ACL>(acls);
        }

        public void rememberCounts(final int total, final int recent, final int unseen) {
            if (null == status) {
                status = new int[3];
            }
            status[0] = total;
            status[1] = recent;
            status[2] = unseen;
        }

        @Override
        public int hashCode() {
            return ((fullName == null) ? 0 : fullName.hashCode());
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ListLsubEntryImpl)) {
                return false;
            }
            final ListLsubEntryImpl other = (ListLsubEntryImpl) obj;
            if (fullName == null) {
                if (other.fullName != null) {
                    return false;
                }
            } else if (!fullName.equals(other.fullName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder(128).append("{ ").append(lsubMap == null ? "LSUB" : "LIST");
            sb.append(" fullName=\"").append(fullName).append('"');
            sb.append(", parent=");
            if (null == parent) {
                sb.append("null");
            } else {
                sb.append('"').append(parent.getFullName()).append('"');
            }
            sb.append(", children=(");
            if (null != children && !children.isEmpty()) {
                final Iterator<ListLsubEntryImpl> iterator = new TreeSet<ListLsubEntryImpl>(children).iterator();
                sb.append('"').append(iterator.next().getFullName()).append('"');
                for (int i = 1, size = children.size(); i < size; i++) {
                    sb.append(", \"").append(iterator.next().getFullName()).append('"');
                }
            }
            sb.append(") }");
            return sb.toString();
        }

        public int compareTo(final ListLsubEntryImpl anotherEntry) {
            final String anotherFullName = anotherEntry.fullName;
            return fullName == null ? (anotherFullName == null ? 0 : -1) : fullName.compareToIgnoreCase(anotherFullName);
        }

    } // End of class ListLsubEntryImpl

}
