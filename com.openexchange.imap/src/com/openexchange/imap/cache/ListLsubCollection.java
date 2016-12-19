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

package com.openexchange.imap.cache;

import static com.openexchange.imap.IMAPCommandsCollection.performCommand;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.Folder;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.mime.MimeMailException;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

/**
 * {@link ListLsubCollection}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ListLsubCollection implements Serializable {

    private static final long serialVersionUID = 8327173541031987226L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListLsubCollection.class);

    private static final String ROOT_FULL_NAME = "";

    private static final String INBOX = "INBOX";

    private static enum State {
        DEPRECATED, DEPRECATED_FORCE_NEW, INITIALIZED;
    }

    // -----------------------------------------------------------------------------------------------------------

    final ConcurrentMap<String, ListLsubEntryImpl> listMap;
    final ConcurrentMap<String, ListLsubEntryImpl> lsubMap;
    private final AtomicReference<State> deprecated;
    private final String[] shared;
    private final String[] user;
    private Boolean mbox;
    private long stamp;
    private final ConcurrentMap<String, ListLsubEntry> draftsEntries;
    private final ConcurrentMap<String, ListLsubEntry> junkEntries;
    private final ConcurrentMap<String, ListLsubEntry> sentEntries;
    private final ConcurrentMap<String, ListLsubEntry> trashEntries;
    private final ConcurrentMap<String, ListLsubEntry> archiveEntries;

    /**
     * Initializes a new {@link ListLsubCollection}.
     *
     * @param imapFolder The IMAP folder
     * @param shared The shared namespaces
     * @param user The user namespaces
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws MessagingException If a messaging error occurs
     */
    protected ListLsubCollection(IMAPFolder imapFolder, String[] shared, String[] user,  boolean ignoreSubscriptions) throws MessagingException {
        super();
        listMap = new ConcurrentHashMap<String, ListLsubEntryImpl>();
        lsubMap = new ConcurrentHashMap<String, ListLsubEntryImpl>();
        draftsEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        junkEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        sentEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        trashEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        archiveEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        deprecated = new AtomicReference<State>();
        this.shared = shared == null ? new String[0] : shared;
        this.user = user == null ? new String[0] : user;
        init(false, imapFolder, ignoreSubscriptions, (IMAPStore) imapFolder.getStore());
    }

    /**
     * Initializes a new {@link ListLsubCollection}.
     *
     * @param imapStore The IMAP store
     * @param shared The shared namespaces
     * @param user The user namespaces
     * @param ignoreSubscriptions Whether to ignore subscriptions
     * @throws OXException If initialization fails
     */
    protected ListLsubCollection(IMAPStore imapStore, String[] shared, String[] user, boolean ignoreSubscriptions) throws OXException {
        super();
        listMap = new NonBlockingHashMap<String, ListLsubEntryImpl>();
        lsubMap = new NonBlockingHashMap<String, ListLsubEntryImpl>();
        draftsEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        junkEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        sentEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        trashEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        archiveEntries = new ConcurrentHashMap<String, ListLsubEntry>();
        deprecated = new AtomicReference<State>();
        this.shared = shared == null ? new String[0] : shared;
        this.user = user == null ? new String[0] : user;
        init(false, imapStore, ignoreSubscriptions);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /**
     * Generates a string representation
     *
     * @param lsub <code>true</code> to include LSUB entries; otherwise LIST
     * @return The string
     */
    public String toString(boolean lsub) {
        StringBuilder builder = new StringBuilder(1024);
        String lf = System.getProperty("line.separator");
        builder.append("ListLsubCollection:");

        SortedMap<String, ListLsubEntry> sm = new TreeMap<String, ListLsubEntry>(lsub ? lsubMap : listMap);
        for (ListLsubEntry entry : sm.values()) {
            builder.append(lf).append("    ").append(entry);
        }

        builder.append(lf).append("\\Draft:");
        sm = new TreeMap<String, ListLsubEntry>(draftsEntries);
        for (ListLsubEntry entry : sm.values()) {
            builder.append(lf).append("    ").append(entry);
        }

        builder.append(lf).append("\\Spam:");
        sm = new TreeMap<String, ListLsubEntry>(junkEntries);
        for (ListLsubEntry entry : sm.values()) {
            builder.append(lf).append("    ").append(entry);
        }

        builder.append(lf).append("\\Sent:");
        sm = new TreeMap<String, ListLsubEntry>(sentEntries);
        for (ListLsubEntry entry : sm.values()) {
            builder.append(lf).append("    ").append(entry);
        }

        builder.append(lf).append("\\Trash:");
        sm = new TreeMap<String, ListLsubEntry>(trashEntries);
        for (ListLsubEntry entry : sm.values()) {
            builder.append(lf).append("    ").append(entry);
        }

        builder.append(lf).append("\\Archive:");
        sm = new TreeMap<String, ListLsubEntry>(archiveEntries);
        for (ListLsubEntry entry : sm.values()) {
            builder.append(lf).append("    ").append(entry);
        }

        return builder.toString();
    }

    private void checkDeprecated() {
        if (State.INITIALIZED != deprecated.get()) {
            throw new ListLsubRuntimeException("LIST/LSUB cache is deprecated.");
        }
    }

    /**
     * Checks if specified full name starts with either shared or user namespace prefix.
     *
     * @param fullName The full name to check
     * @return <code>true</code> if full name starts with either shared or user namespace prefix; otherwise <code>false</code>
     */
    protected boolean isNamespace(final String fullName) {
        for (final String sharedNamespace : shared) {
            if (fullName.startsWith(sharedNamespace)) {
                return true;
            }
        }
        for (final String userNamespace : user) {
            if (fullName.startsWith(userNamespace)) {
                return true;
            }
        }
        return false;
    }

    protected boolean equalsNamespace(final String fullName) {
        for (final String sharedNamespace : shared) {
            if (fullName.equals(sharedNamespace)) {
                return true;
            }
        }
        for (final String userNamespace : user) {
            if (fullName.equals(userNamespace)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if associated mailbox is considered as MBox format.
     *
     * @return {@link Boolean#TRUE} for MBox format, {@link Boolean#FALSE} for no MBOX format or <code>null</code> for undetermined
     */
    public Boolean consideredAsMBox() {
        return mbox;
    }

    /**
     * Checks if this collection is marked as deprecated.
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     */
    public boolean isDeprecated() {
        return State.INITIALIZED != deprecated.get();
    }

    /**
     * Clears this collection and resets its time stamp to force re-initialization.
     *
     * @param forceNewConnection <code>true</code> to enforce a new connection; otherwise <code>false</code>
     */
    public void clear(boolean forceNewConnection) {
        if (deprecated.compareAndSet(State.INITIALIZED, forceNewConnection ? State.DEPRECATED_FORCE_NEW : State.DEPRECATED)) {
            stamp = 0;
            LOG.debug("Cleared LIST/LSUB cache.", new Throwable());
        }
    }

    /**
     * Removes the associated entry.
     *
     * @param fullName The full name
     */
    public void remove(final String fullName) {
        /*
         * Cleanse from LIST map
         */
        removeFrom(fullName, listMap);
        /*
         * Cleanse from LSUB map, too
         */
        removeFrom(fullName, lsubMap);
    }

    private static void removeFrom(final String fullName, final ConcurrentMap<String, ListLsubEntryImpl> map) {
        final ListLsubEntryImpl entry = map.remove(fullName);
        if (null != entry) {
            final ListLsubEntryImpl parent = entry.getParentImpl();
            if (null != parent) {
                parent.removeChild(entry);
            }
            for (final ListLsubEntry child : entry.getChildrenSet()) {
                removeFromMap(child.getFullName(), map);
            }
        }
    }

    private static void removeFromMap(final String fullName, final ConcurrentMap<String, ListLsubEntryImpl> map) {
        final ListLsubEntryImpl entry = map.remove(fullName);
        if (null == entry) {
            return;
        }
        for (final ListLsubEntry child : entry.getChildrenSet()) {
            removeFromMap(child.getFullName(), map);
        }
    }

    /**
     * Re-Initializes the SPECIAL-USE folders (only if the IMAP store advertises support for <code>"SPECIAL-USE"</code> capability)
     *
     * @param imapStore The IMAP store
     * @throws OXException If re-initialization fails
     */
    public void reinitSpecialUseFolders(final IMAPStore imapStore) throws OXException {
        try {
            reinitSpecialUseFolders((IMAPFolder) imapStore.getFolder("INBOX"), imapStore);
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Re-Initializes the SPECIAL-USE folders (only if the IMAP store advertises support for <code>"SPECIAL-USE"</code> capability)
     *
     * @param imapFolder The IMAP store
     * @throws OXException If re-initialization fails
     */
    public void reinitSpecialUseFolders(final IMAPFolder imapFolder) throws OXException {
        reinitSpecialUseFolders(imapFolder, (IMAPStore) imapFolder.getStore());
    }

    private void reinitSpecialUseFolders(final IMAPFolder imapFolder, final IMAPStore imapStore) throws OXException {
        try {
            draftsEntries.clear();
            junkEntries.clear();
            sentEntries.clear();
            trashEntries.clear();
            archiveEntries.clear();
            if (imapStore.getCapabilities().containsKey("SPECIAL-USE")) {
                /*
                 * Perform LIST (SPECIAL-USE) "" "*"
                 */
                imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                    @Override
                    public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                        doListSpecialUse(protocol, true);
                        return null;
                    }

                });
            } else {
                /*
                 * Perform LIST "" "*"
                 */
                imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                    @Override
                    public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                        doListSpecialUse(protocol, false);
                        return null;
                    }

                });
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Re-initializes this collection.
     *
     * @param imapStore The IMAP store
     * @param ignoreSubscriptions Whether subscription are supposed to be ignored
     * @throws OXException If re-initialization fails
     */
    public void reinit(final IMAPStore imapStore, boolean ignoreSubscriptions) throws OXException {
        clear(false);
        init(true, imapStore, ignoreSubscriptions);
    }

    /**
     * Re-initializes this collection.
     *
     * @param imapFolder The IMAP folder
     * @param ignoreSubscriptions Whether subscription are supposed to be ignored
     * @throws MessagingException If a messaging error occurs
     */
    public void reinit(IMAPFolder imapFolder, boolean ignoreSubscriptions) throws MessagingException {
        clear(false);
        init(true, imapFolder, ignoreSubscriptions, (IMAPStore) imapFolder.getStore());
    }

    private void init(boolean clearMaps, IMAPStore imapStore, boolean ignoreSubscriptions) throws OXException {
        try {
            init(clearMaps, (IMAPFolder) imapStore.getFolder("INBOX"), ignoreSubscriptions, imapStore);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static volatile Integer initAclThreshold;
    private static int initAclThreshold() {
        Integer tmp = initAclThreshold;
        if (null == tmp) {
            synchronized (ListLsubCollection.class) {
                tmp = initAclThreshold;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 500 : service.getIntProperty("com.openexchange.imap.initAclThreshold", 500));
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Integer initStatusThreshold;
    private static int initStatusThreshold() {
        Integer tmp = initStatusThreshold;
        if (null == tmp) {
            synchronized (ListLsubCollection.class) {
                tmp = initStatusThreshold;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 500 : service.getIntProperty("com.openexchange.imap.initStatusThreshold", 500));
                }
            }
        }
        return tmp.intValue();
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                initAclThreshold = null;
                initStatusThreshold = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.initStatusThreshold", "com.openexchange.imap.initAclThreshold");
            }
        });
    }

    private void init(boolean clearMaps, IMAPFolder imapFolder, final boolean ignoreSubscriptions, IMAPStore imapStore) throws MessagingException {
        if (clearMaps) {
            listMap.clear();
            lsubMap.clear();
            draftsEntries.clear();
            junkEntries.clear();
            sentEntries.clear();
            trashEntries.clear();
            archiveEntries.clear();
        } else if (ignoreSubscriptions) {
            lsubMap.clear();
        }
        final boolean debug = LOG.isDebugEnabled();
        final long st = debug ? System.currentTimeMillis() : 0L;

        // Check for if a new connection is supposed to be used
        IMAPFolder imapFolderToUse = imapFolder;
        boolean forceNewConnection = (State.DEPRECATED_FORCE_NEW == deprecated.get());
        if (forceNewConnection && imapFolderToUse.checkOpen()) {
            // If an IMAPFolder is open it uses its own IMAPProtocol instance
            // A new IMAPFolder falls-back to the connection of the underlying IMAPStore, which is stateless
            imapFolderToUse = (IMAPFolder) imapFolderToUse.getStore().getFolder(imapFolderToUse.getFullName());
        }

        // Query the IMAP server
        imapFolderToUse.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                // Perform LIST "" ""
                doRootListCommand(protocol);

                // Perform LSUB "" "*"
                if (!ignoreSubscriptions) {
                    doListLsubCommand(protocol, true);
                }

                // Perform LIST "" "*"
                doListLsubCommand(protocol, false);

                if (ignoreSubscriptions) {
                    lsubMap.putAll(listMap);
                }

                // Perform LIST (SPECIAL-USE) "" "*"
                if (protocol.hasCapability("SPECIAL-USE")) {
                    doListSpecialUse(protocol, true);
                }

                return null;
            }
        });

        // Debug logging
        if (debug) {
            final StringBuilder sb = new StringBuilder(1024);
            {
                final TreeMap<String, ListLsubEntryImpl> tm = new TreeMap<String, ListLsubEntryImpl>(listMap);
                sb.append("LIST cache contains after (re-)initialization:\n");
                for (final Entry<String, ListLsubEntryImpl> entry : tm.entrySet()) {
                    sb.append('"').append(entry.getKey()).append("\"=").append(entry.getValue()).append('\n');
                }
                LOG.debug(sb.toString());
            }
            {
                final TreeMap<String, ListLsubEntryImpl> tm = new TreeMap<String, ListLsubEntryImpl>(lsubMap);
                sb.setLength(0);
                sb.append("LSUB cache contains after (re-)initialization:\n");
                for (final Entry<String, ListLsubEntryImpl> entry : tm.entrySet()) {
                    sb.append('"').append(entry.getKey()).append("\"=").append(entry.getValue()).append('\n');
                }
                LOG.debug(sb.toString());
            }
        }

        if (!ignoreSubscriptions) {
            // Consistency check
            checkConsistency(imapStore);
        }

        if (debug) {
            long dur = System.currentTimeMillis() - st;
            LOG.debug("LIST/LSUB cache built in {}msec", dur);
        }
        /*
         * Set time stamp
         */
        stamp = System.currentTimeMillis();
        deprecated.set(State.INITIALIZED);
    }

    private void checkConsistency(final IMAPStore imapStore) {
        final ListLsubEntryImpl rootEntry = listMap.get(ROOT_FULL_NAME);
        /*
         * Ensure every LSUB'ed entry occurs in LIST'ed entries
         */
        for (final Entry<String, ListLsubEntryImpl> entry : new TreeMap<String, ListLsubEntryImpl>(lsubMap).entrySet()) {
            final String fullName = entry.getKey();
            if (!listMap.containsKey(fullName)) {
                /*
                 * Distinguish between personal and other namespace
                 */
                if (isNamespace(fullName)) {
                    /*
                     * Either shared or user namespace
                     */
                    if (existsSafe(fullName, imapStore)) {
                        createLISTEntryForNamespaceFolder(fullName, entry.getValue(), rootEntry);
                    } else {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
                        final ListLsubEntryImpl lle = entry.getValue();
                        dropEntryFrom(lle, listMap);
                    }
                } else {
                    /*
                     * A personal full name: Drop LSUB'ed entries which do not occur in LIST'ed entries.
                     */
                    final ListLsubEntryImpl lle = entry.getValue();
                    dropEntryFrom(lle, listMap);
                }
            }
        }
    }

    private ListLsubEntryImpl createLISTEntryForNamespaceFolder(String fullName, ListLsubEntryImpl lsubEntry, ListLsubEntryImpl rootEntry) {
        ListLsubEntryImpl lle = new ListLsubEntryImpl(lsubEntry, true);
        listMap.put(fullName, lle);

        char separator = lle.getSeparator();
        int pos = fullName.lastIndexOf(separator);
        if (pos >= 0) {
            /*
             * Non-root level
             */
            final String parentFullName = fullName.substring(0, pos);
            ListLsubEntryImpl parent = listMap.get(parentFullName);
            if (null != parent) {
                lle.setParent(parent);
                parent.addChild(lle);
            } else {
                // Parent not contained in LIST map
                ListLsubEntryImpl parentEntry = lsubMap.get(parentFullName);
                if (null == parentEntry) {
                    parentEntry = new ListLsubEntryImpl(parentFullName, ATTRIBUTES_NON_EXISTING_NAMESPACE, separator, ListLsubEntry.ChangeState.UNDEFINED, true, false, Boolean.TRUE, null).setNamespace(true);
                    lsubMap.put(parentFullName, parentEntry);
                }
                parent = createLISTEntryForNamespaceFolder(parentFullName, parentEntry, rootEntry);
                lle.setParent(parent);
                parent.addChild(lle);
            }
        } else {
            /*
             * Root level
             */
            lle.setParent(rootEntry);
            rootEntry.addChild(lle);
        }
        return lle;
    }

    private static void dropEntryFrom(final ListLsubEntryImpl lle, final ConcurrentMap<String, ListLsubEntryImpl> map) {
        final Set<ListLsubEntryImpl> tmp = new HashSet<ListLsubEntryImpl>(lle.getChildrenSet());
        for (final ListLsubEntryImpl child : tmp) {
            dropEntryFrom(child, map);
        }
        map.remove(lle.getFullName());
        /*
         * Drop from parent's children
         */
        final ListLsubEntryImpl p = lle.getParentImpl();
        if (null != p) {
            p.removeChild(lle);
            if (p.isDummy() && p.emptyChildren()) {
                // Drop dummy parent, too
                dropEntryFrom(p, map);
            }
        }
    }

    private static boolean existsSafe(final String fullName, final IMAPStore imapStore) {
        try {
            return imapStore.getFolder(fullName).exists();
        } catch (final MessagingException e) {
            // Swallow
            LOG.debug("Failed checking existence for {}", fullName, e);
        }
        return false;
    }

    /**
     * Gets current entry for specified full name.
     *
     * @param fullName The full name of the starting folder node
     * @param imapFolder The connected IMAP folder
     * @throws MailException If update fails
     */
    public ListLsubEntry getActualEntry(final String fullName, final IMAPFolder imapFolder) throws OXException {
        try {
            /*
             * Perform LIST "" <full-name>
             */
            return (ListLsubEntry) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    return doSingleListCommandWithLsub(protocol, fullName);
                }

            });
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Updates a sub-tree starting at specified full name.
     *
     * @param fullName The full name of the starting folder node
     * @param imapStore The connected IMAP store
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws OXException If update fails
     */
    public void update(String fullName, IMAPStore imapStore, boolean ignoreSubscriptions) throws OXException {
        try {
            update(fullName, (IMAPFolder) imapStore.getFolder("INBOX"), ignoreSubscriptions);
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Updates a sub-tree starting at specified full name.
     *
     * @param fullName The full name of the starting folder node
     * @param imapFolder An IMAP folder providing connected protocol
     * @param doStatus Whether STATUS command shall be performed
     * @param doGetAcl Whether ACL command shall be performed
     * @throws MessagingException If a messaging error occurs
     */
    public void update(String fullName, IMAPFolder imapFolder, boolean ignoreSubscriptions) throws MessagingException {
        if (State.INITIALIZED != deprecated.get() || ROOT_FULL_NAME.equals(fullName)) {
            init(true, imapFolder, ignoreSubscriptions, (IMAPStore) imapFolder.getStore());
            return;
        }
        /*
         * Do a full re-build anyway...
         */
        init(true, imapFolder,  ignoreSubscriptions, (IMAPStore) imapFolder.getStore());
    }

    /**
     * Performs a dummy LSUB "" "" which seems to reveal folders which got not displayed before... Please don't ask why.
     *
     * @param protocol The IMAP protocol
     */
    protected void doDummyLsub(final IMAPProtocol protocol) {
        String command = "LSUB \"\" \"\"";
        Response[] r = performCommand(protocol, command);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            /*
             * Dispatch remaining untagged responses
             */
            protocol.notifyResponseHandlers(r);
            return;
        }
        try {
            /*
             * Dispatch remaining untagged responses
             */
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, command);
            protocol.notifyResponseHandlers(r);
            protocol.handleResult(response);
        } catch (final ProtocolException e) {
            LOG.warn("Dummy >>LSUB \"\" \"\"<< command failed.", e);
        }
    }

    private static final Set<String> ATTRIBUTES_NON_EXISTING_PARENT = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "\\noselect",
        "\\haschildren")));

    private static final Set<String> ATTRIBUTES_NON_EXISTING_NAMESPACE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "\\noselect",
        "\\hasnochildren")));

    // private static final Set<String> ATTRIBUTES_NO_SELECT_NAMESPACE = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("\\noselect")));

    private static final String ATTRIBUTE_DRAFTS = "\\drafts";
    private static final String ATTRIBUTE_JUNK = "\\junk";
    private static final String ATTRIBUTE_SENT = "\\sent";
    private static final String ATTRIBUTE_TRASH = "\\trash";
    private static final String ATTRIBUTE_ARCHIVE = "\\archive";

    private static final String[] ATTRIBUTES_SPECIAL_USE = { ATTRIBUTE_DRAFTS, ATTRIBUTE_JUNK, ATTRIBUTE_SENT, ATTRIBUTE_TRASH };

    /**
     * Performs a LIST/LSUB command with specified IMAP protocol.
     *
     * @param protocol The IMAP protocol
     * @param lsub <code>true</code> to perform a LSUB command; otherwise <code>false</code> for LIST
     * @throws ProtocolException If a protocol error occurs
     */
    protected void doListSpecialUse(final IMAPProtocol protocol, final boolean usingSpecualUse) throws ProtocolException {
        String command = "LIST";
        String sCmd = new StringBuilder(command).append(usingSpecualUse ? " (SPECIAL-USE) " : " ").append("\"\" \"*\"").toString();
        Response[] r = performCommand(protocol, sCmd);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            for (int i = 0, len = r.length - 1; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    ListLsubEntryImpl entryImpl = parseListResponse(ir, lsubMap, ATTRIBUTES_SPECIAL_USE);
                    if (null != entryImpl) {
                        Set<String> attrs = entryImpl.getAttributes();
                        if (null != attrs && !attrs.isEmpty()) {
                            if (attrs.contains(ATTRIBUTE_DRAFTS)) {
                                this.draftsEntries.put(entryImpl.getFullName(), entryImpl);
                            } else if (attrs.contains(ATTRIBUTE_JUNK)) {
                                this.junkEntries.put(entryImpl.getFullName(), entryImpl);
                            } else if (attrs.contains(ATTRIBUTE_SENT)) {
                                this.sentEntries.put(entryImpl.getFullName(), entryImpl);
                            } else if (attrs.contains(ATTRIBUTE_TRASH)) {
                                this.trashEntries.put(entryImpl.getFullName(), entryImpl);
                            } else if (attrs.contains(ATTRIBUTE_ARCHIVE)) {
                                this.archiveEntries.put(entryImpl.getFullName(), entryImpl);
                            }
                        }
                    }
                    r[i] = null;
                }
            }
            protocol.notifyResponseHandlers(r);
        } else {
            /*
             * Dispatch remaining untagged responses
             */
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, sCmd);
            protocol.notifyResponseHandlers(r);
            protocol.handleResult(response);
        }
    }

    /**
     * Performs a LIST/LSUB command with specified IMAP protocol.
     *
     * @param protocol The IMAP protocol
     * @param lsub <code>true</code> to perform a LSUB command; otherwise <code>false</code> for LIST
     * @throws ProtocolException If a protocol error occurs
     */
    protected void doListLsubCommand(final IMAPProtocol protocol, final boolean lsub) throws ProtocolException {
        // Perform command
        String command = lsub ? "LSUB" : "LIST";
        String sCmd = new StringBuilder(command).append(" \"\" \"*\"").toString();

        Response[] r = performCommand(protocol, sCmd);
        LOG.debug("{} cache filled with >>{}<< which returned {} response line(s).", (command), sCmd, Integer.valueOf(r.length));

        Response response = r[r.length - 1];
        if (response.isOK()) {
            final ConcurrentMap<String, ListLsubEntryImpl> map = lsub ? lsubMap : listMap;
            final Map<String, List<ListLsubEntryImpl>> parentMap = new HashMap<String, List<ListLsubEntryImpl>>(4);
            final ListLsubEntryImpl rootEntry = map.get(ROOT_FULL_NAME);

            // Get sorted responses
            final List<ListLsubEntryImpl> listResponses = sortedListResponses(r, command, lsub);
            char separator = '\0';
            for (final ListLsubEntryImpl next : listResponses) {
                ListLsubEntryImpl listLsubEntry = next;

                // Check for MBox format while iterating LIST/LSUB responses.
                if (listLsubEntry.hasInferiors() && listLsubEntry.canOpen()) {
                    mbox = Boolean.FALSE;
                }
                final String fullName = listLsubEntry.getFullName();

                // (Re-)Set children
                {
                    final ListLsubEntryImpl oldEntry = map.get(fullName);
                    if (oldEntry == null) {
                        // Wasn't in map before
                        map.put(fullName, listLsubEntry);
                    } else {
                        // Already contained in map
                        oldEntry.clearChildren();
                        oldEntry.copyFrom(listLsubEntry);
                        listLsubEntry = oldEntry;
                    }
                }

                // Determine parent
                final int pos = fullName.lastIndexOf((separator = listLsubEntry.getSeparator()));
                if (pos >= 0) {
                    // Non-root level
                    final String parentFullName = fullName.substring(0, pos);
                    final ListLsubEntryImpl parent = map.get(parentFullName);
                    if (null == parent) {
                        // Parent not (yet) in map
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
                    // Root level
                    listLsubEntry.setParent(rootEntry);
                    rootEntry.addChild(listLsubEntry);
                }

                // Check attributes for marked folder
                if (false == lsub) {
                    Set<String> attrs = listLsubEntry.getAttributes();
                    if (null != attrs && !attrs.isEmpty()) {
                        if (attrs.contains(ATTRIBUTE_DRAFTS)) {
                            this.draftsEntries.put(listLsubEntry.getFullName(), listLsubEntry);
                        } else if (attrs.contains(ATTRIBUTE_JUNK)) {
                            this.junkEntries.put(listLsubEntry.getFullName(), listLsubEntry);
                        } else if (attrs.contains(ATTRIBUTE_SENT)) {
                            this.sentEntries.put(listLsubEntry.getFullName(), listLsubEntry);
                        } else if (attrs.contains(ATTRIBUTE_TRASH)) {
                            this.trashEntries.put(listLsubEntry.getFullName(), listLsubEntry);
                        } else if (attrs.contains(ATTRIBUTE_ARCHIVE)) {
                            this.archiveEntries.put(listLsubEntry.getFullName(), listLsubEntry);
                        }
                    }
                }
            } // End of for loop
            if (!parentMap.isEmpty()) {
                // Handle parent map
                handleParentMap(parentMap, separator, rootEntry, lsub, map);
            }

            // Check namespace folders
            if (!lsub) {
                handleNamespaces(map, rootEntry, separator);
            }

            // Dispatch remaining untagged responses
            protocol.notifyResponseHandlers(r);
        } else {
            // Dispatch remaining untagged responses
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, sCmd);
            protocol.notifyResponseHandlers(r);
            protocol.handleResult(response);
        }
    }

    private void handleNamespaces(final ConcurrentMap<String, ListLsubEntryImpl> map, final ListLsubEntryImpl rootEntry, final char separator) {
        for (final String sharedNamespace : shared) {
            final ListLsubEntryImpl entry = map.get(sharedNamespace);
            if (null == entry) {
                final ListLsubEntryImpl namespaceFolder =
                    new ListLsubEntryImpl(
                        sharedNamespace,
                        ATTRIBUTES_NON_EXISTING_NAMESPACE,
                        separator,
                        ListLsubEntry.ChangeState.UNDEFINED,
                        true,
                        false,
                        Boolean.FALSE,
                        lsubMap).setNamespace(true);
                namespaceFolder.setParent(rootEntry);
                rootEntry.addChildIfAbsent(namespaceFolder);
                map.put(sharedNamespace, namespaceFolder);
            } else {
                entry.setCanOpen(false);
            }
        }
        for (final String userNamespace : user) {
            final ListLsubEntryImpl entry = map.get(userNamespace);
            if (null == entry) {
                final ListLsubEntryImpl namespaceFolder =
                    new ListLsubEntryImpl(
                        userNamespace,
                        ATTRIBUTES_NON_EXISTING_NAMESPACE,
                        separator,
                        ListLsubEntry.ChangeState.UNDEFINED,
                        true,
                        false,
                        Boolean.FALSE,
                        lsubMap).setNamespace(true);
                namespaceFolder.setParent(rootEntry);
                rootEntry.addChildIfAbsent(namespaceFolder);
                map.put(userNamespace, namespaceFolder);
            } else {
                entry.setCanOpen(false);
            }
        }
    }

    private List<ListLsubEntryImpl> sortedListResponses(final Response[] r, final String command, final boolean lsub) {
        List<ListLsubEntryImpl> list = new ArrayList<ListLsubEntryImpl>(r.length);
        for (int i = 0, len = r.length - 1; len-- > 0; i++) {
            if (r[i] instanceof IMAPResponse) {
                IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    list.add(parseListResponse(ir, lsub ? null : lsubMap));
                    r[i] = null;
                }
            }
        }
        Collections.sort(list);
        return list;
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
    private void handleParentMap(final Map<String, List<ListLsubEntryImpl>> parentMap, final char separator, final ListLsubEntryImpl rootEntry, final boolean lsub, final ConcurrentMap<String, ListLsubEntryImpl> map) {
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
                    parent =
                        new ListLsubEntryImpl(
                            parentFullName,
                            ATTRIBUTES_NON_EXISTING_PARENT,
                            separator,
                            ListLsubEntry.ChangeState.UNDEFINED,
                            true,
                            false,
                            Boolean.TRUE,
                            lsub ? null : lsubMap).setDummy(true);
                    if (isNamespace(parentFullName)) {
                        parent.setNamespace(true);
                        if (equalsNamespace(parentFullName)) {
                            parent.setCanOpen(false);
                        }
                    }
                    map.put(parentFullName, parent);
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
    protected void doRootListCommand(final IMAPProtocol protocol) throws ProtocolException {
        /*
         * Perform command: LIST "" ""
         */
        String command = "LIST \"\" \"\"";
        Response[] r = performCommand(protocol, command);

        if (r.length == 1) {
            // No LIST response for root folder. Do dummy LSUB and retry...
            doDummyLsub(protocol);
            r = performCommand(protocol, command);
        }

        Response response = r[r.length - 1];
        if (false == response.isOK()) {
            /*
             * Dispatch remaining untagged responses
             */
            LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, command);
            protocol.notifyResponseHandlers(r);
            protocol.handleResult(response);
            return;
        }

        String cmd = "LIST";
        for (int i = 0, k = r.length; k-- > 0; i++) {
            if (!(r[i] instanceof IMAPResponse)) {
                continue;
            }

            IMAPResponse ir = (IMAPResponse) r[i];
            if (ir.keyEquals(cmd)) {
                final ListLsubEntryImpl listLsubEntry = parseListResponse(ir, null, null, ROOT_FULL_NAME);
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
        /*
         * Dispatch remaining untagged responses
         */
        protocol.notifyResponseHandlers(r);
    }

    /**
     * Performs a LIST command for a single folder with specified IMAP protocol.
     *
     * @param protocol The IMAP protocol
     * @param fullName The full name
     * @throws ProtocolException If a protocol error occurs
     */
    protected ListLsubEntryImpl doSingleListCommandWithLsub(final IMAPProtocol protocol, final String fullName) throws ProtocolException {
        doDummyLsub(protocol);
        /*
         * Perform command: LIST "" <full-name>
         */
        final String mbox = BASE64MailboxEncoder.encode(fullName);
        final Argument args = new Argument();
        args.writeString(mbox);
        final Response[] r = performCommand(protocol, "LIST \"\"", args);
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            ListLsubEntryImpl listLsubEntry = null;
            for (int i = 0, len = r.length; null == listLsubEntry && i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals("LIST")) {
                    listLsubEntry = parseListResponse(ir, null);
                    r[i] = null;
                }
            }
            /*
             * Dispatch remaining untagged responses
             */
            protocol.notifyResponseHandlers(r);
            if (null != listLsubEntry) {
                /*
                 * Check subscription status
                 */
                listLsubEntry.setSubscribed(doSubscriptionCheck(protocol, mbox));
            }
            return listLsubEntry;
        }
        /*
         * Dispatch remaining untagged responses
         */
        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, new StringBuilder("LIST \"\" ").append(args).toString());
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
        return null; // Never reached if response is not OK
    }

    /**
     * Performs a check if denoted folder is subscribed.
     *
     * @param protocol The IMAP protocol
     * @param mbox The encoded full name
     * @return <code>true</code> if subscribed; otherwise <code>false</code>
     * @throws ProtocolException If a protocol error occurs
     */
    private boolean doSubscriptionCheck(final IMAPProtocol protocol, final String mbox) throws ProtocolException {
        /*
         * Perform command: LIST "" <full-name>
         */
        final Argument args = new Argument();
        args.writeString(mbox);
        final Response[] r = performCommand(protocol, "LSUB \"\"", args);
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            boolean ret = false;
            for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals("LSUB")) {
                    ret |= mbox.equals(parseEncodedFullName(ir));
                    r[i] = null;
                }
            }
            /*
             * Dispatch remaining untagged responses
             */
            protocol.notifyResponseHandlers(r);
            return ret;
        }
        /*
         * Dispatch remaining untagged responses
         */
        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, new StringBuilder("LSUB \"\" ").append(args).toString());
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
        return false; // Never reached if response is not OK
    }

    /**
     * Adds single folder to collection.
     *
     * @param imapFolder The IMAP folder
     * @param addIt Whether to add the folder or not
     * @throws OXException If operation fails
     */
    public ListLsubEntry addSingleByFolder(IMAPFolder imapFolder, boolean addIt) throws OXException {
        try {
            ListLsubEntry.ChangeState changeState = ListLsubEntry.ChangeState.UNDEFINED;
            boolean canOpen = true;
            boolean hasInferiors = true;
            Boolean hasChildren = null;
            Set<String> attributes;
            {
                String[] attrs = imapFolder.getAttributes();
                if (null == attrs || 0 == attrs.length) {
                    attributes = Collections.<String> emptySet();
                } else {
                    attributes = new HashSet<String>(attrs.length);
                    for (String attribute : attrs) {
                        String attr = Strings.asciiLowerCase(attribute);
                        switch (POS_MAP.get(attr)) {
                            case 1:
                                changeState = ListLsubEntry.ChangeState.CHANGED;
                                break;
                            case 2:
                                changeState = ListLsubEntry.ChangeState.UNCHANGED;
                                break;
                            case 3:
                                canOpen = false;
                                break;
                            case 4:
                                hasInferiors = false;
                                break;
                            case 5:
                                hasChildren = Boolean.TRUE;
                                break;
                            case 6:
                                hasChildren = Boolean.FALSE;
                                break;
                            default:
                                // Nothing
                                break;
                            }
                        attributes.add(attr);
                    }
                }
            }
            boolean subscribed = addIt ? imapFolder.isSubscribed() : false;
            String fullName = imapFolder.getFullName();
            if (subscribed) {
                ListLsubEntryImpl lsubEntry = new ListLsubEntryImpl(fullName, attributes, imapFolder.getSeparator(), changeState, hasInferiors, canOpen, hasChildren, null);
                ConcurrentMap<String, ListLsubEntryImpl> map = lsubMap;
                {
                    ListLsubEntryImpl oldEntry = map.get(fullName);
                    ListLsubEntryImpl parent;
                    if (null != oldEntry) {
                        for (ListLsubEntryImpl child : oldEntry.getChildrenSet()) {
                            child.setParent(lsubEntry);
                            lsubEntry.addChild(child);
                        }
                        parent = oldEntry.getParentImpl();
                    } else {
                        int pos = fullName.lastIndexOf(lsubEntry.getSeparator());
                        if (pos > 0) {
                            String parentFullName = fullName.substring(0, pos);
                            parent = map.get(parentFullName);
                        } else {
                            parent = map.get(ROOT_FULL_NAME);
                        }
                    }
                    if (null != parent) {
                        lsubEntry.setParent(parent);
                        parent.addChild(lsubEntry);
                    }
                }
                map.put(fullName, lsubEntry);
            }

            ListLsubEntryImpl listEntry = new ListLsubEntryImpl(fullName, attributes, imapFolder.getSeparator(), changeState, hasInferiors, canOpen, hasChildren, lsubMap);
            if (addIt) {
                ConcurrentMap<String, ListLsubEntryImpl> map = listMap;
                {
                    ListLsubEntryImpl oldEntry = map.get(fullName);
                    ListLsubEntryImpl parent;
                    if (null != oldEntry) {
                        for (ListLsubEntryImpl child : oldEntry.getChildrenSet()) {
                            child.setParent(listEntry);
                            listEntry.addChild(child);
                        }
                        parent = oldEntry.getParentImpl();
                    } else {
                        int pos = fullName.lastIndexOf(listEntry.getSeparator());
                        if (pos > 0) {
                            String parentFullName = fullName.substring(0, pos);
                            parent = map.get(parentFullName);
                        } else {
                            parent = map.get(ROOT_FULL_NAME);
                        }
                    }
                    if (null != parent) {
                        listEntry.setParent(parent);
                        parent.addChild(listEntry);
                    }
                }
                map.put(fullName, listEntry);
            }
            return listEntry;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds single entry to collection.
     *
     * @param fullName The full name
     * @param imapStore The IMAP store
     * @throws OXException If operation fails
     */
    public void addSingle(final String fullName, final IMAPStore imapStore) throws OXException {
        try {
            addSingle(fullName, (IMAPFolder) imapStore.getFolder("INBOX"));
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds single entry to collection.
     *
     * @param fullName The full name
     * @param subscribed <code>true</code> if subscribed; otherwise <code>false</code>
     * @param imapFolder The IMAP folder
     * @throws OXException If operation fails
     */
    public void addSingle(IMAPFolder imapFolder, boolean subscribed) throws OXException {
        try {
            ListLsubEntry.ChangeState changeState = ListLsubEntry.ChangeState.UNDEFINED;
            boolean canOpen = true;
            boolean hasInferiors = true;
            Boolean hasChildren = null;
            Set<String> attributes;
            {
                String[] attrs = imapFolder.getAttributes();
                if (null == attrs || 0 == attrs.length) {
                    attributes = Collections.<String> emptySet();
                } else {
                    attributes = new HashSet<String>(attrs.length);
                    for (String attribute : attrs) {
                        String attr = Strings.asciiLowerCase(attribute);
                        switch (POS_MAP.get(attr)) {
                            case 1:
                                changeState = ListLsubEntry.ChangeState.CHANGED;
                                break;
                            case 2:
                                changeState = ListLsubEntry.ChangeState.UNCHANGED;
                                break;
                            case 3:
                                canOpen = false;
                                break;
                            case 4:
                                hasInferiors = false;
                                break;
                            case 5:
                                hasChildren = Boolean.TRUE;
                                break;
                            case 6:
                                hasChildren = Boolean.FALSE;
                                break;
                            default:
                                // Nothing
                                break;
                            }
                        attributes.add(attr);
                    }
                }
            }
            String fullName = imapFolder.getFullName();
            if (subscribed) {
                ListLsubEntryImpl lsubEntry = new ListLsubEntryImpl(fullName, attributes, imapFolder.getSeparator(), changeState, hasInferiors, canOpen, hasChildren, null);
                ConcurrentMap<String, ListLsubEntryImpl> map = lsubMap;
                {
                    ListLsubEntryImpl oldEntry = map.get(fullName);
                    ListLsubEntryImpl parent;
                    if (null != oldEntry) {
                        for (ListLsubEntryImpl child : oldEntry.getChildrenSet()) {
                            child.setParent(lsubEntry);
                            lsubEntry.addChild(child);
                        }
                        parent = oldEntry.getParentImpl();
                    } else {
                        int pos = fullName.lastIndexOf(lsubEntry.getSeparator());
                        if (pos > 0) {
                            String parentFullName = fullName.substring(0, pos);
                            parent = map.get(parentFullName);
                        } else {
                            parent = map.get(ROOT_FULL_NAME);
                        }
                    }
                    if (null != parent) {
                        lsubEntry.setParent(parent);
                        parent.addChild(lsubEntry);
                    }
                }
                map.put(fullName, lsubEntry);
            }

            ListLsubEntryImpl listEntry = new ListLsubEntryImpl(fullName, attributes, imapFolder.getSeparator(), changeState, hasInferiors, canOpen, hasChildren, lsubMap);
            {
                ConcurrentMap<String, ListLsubEntryImpl> map = listMap;
                {
                    ListLsubEntryImpl oldEntry = map.get(fullName);
                    ListLsubEntryImpl parent;
                    if (null != oldEntry) {
                        for (ListLsubEntryImpl child : oldEntry.getChildrenSet()) {
                            child.setParent(listEntry);
                            listEntry.addChild(child);
                        }
                        parent = oldEntry.getParentImpl();
                    } else {
                        int pos = fullName.lastIndexOf(listEntry.getSeparator());
                        if (pos > 0) {
                            String parentFullName = fullName.substring(0, pos);
                            parent = map.get(parentFullName);
                        } else {
                            parent = map.get(ROOT_FULL_NAME);
                        }
                    }
                    if (null != parent) {
                        listEntry.setParent(parent);
                        parent.addChild(listEntry);
                    }
                }
                map.put(fullName, listEntry);
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Adds single entry to collection.
     *
     * @param fullName The full name
     * @param imapFolder The IMAP folder
     * @throws OXException If operation fails
     */
    public void addSingle(final String fullName, final IMAPFolder imapFolder) throws OXException {
        try {
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doSingleListCommand(fullName, protocol, false);
                    return null;
                }

            });

            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                    doSingleListCommand(fullName, protocol, true);
                    return null;
                }

            });
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Performs a LIST/LSUB command for a single folder with specified IMAP protocol.
     *
     * @param fullName The full name
     * @param protocol The IMAP protocol
     * @throws ProtocolException If a protocol error occurs
     */
    protected ListLsubEntryImpl doSingleListCommand(final String fullName, final IMAPProtocol protocol, final boolean lsub) throws ProtocolException {
        if (!lsub) {
            doDummyLsub(protocol);
        }
        /*
         * Perform command: LIST "" "INBOX"
         */
        final String command = lsub ? "LSUB" : "LIST";
        String mbox = BASE64MailboxEncoder.encode(fullName);
        Argument args = new Argument();
        args.writeString(mbox);
        final Response[] r = performCommand(protocol, new StringBuilder(command).append(" \"\"").toString(), args);
        args = null;
        mbox = null;
        final Response response = r[r.length - 1];
        if (response.isOK()) {
            ListLsubEntryImpl retval = null;
            final ConcurrentMap<String, ListLsubEntryImpl> map = lsub ? lsubMap : listMap;
            for (int i = 0, len = r.length; i < len; i++) {
                if (!(r[i] instanceof IMAPResponse)) {
                    continue;
                }
                final IMAPResponse ir = (IMAPResponse) r[i];
                if (ir.keyEquals(command)) {
                    final ListLsubEntryImpl listLsubEntry = parseListResponse(ir, lsub ? null : lsubMap);
                    retval = listLsubEntry;
                    {
                        final ListLsubEntryImpl oldEntry = map.get(fullName);
                        final ListLsubEntryImpl parent;
                        if (null != oldEntry) {
                            for (final ListLsubEntryImpl child : oldEntry.getChildrenSet()) {
                                child.setParent(listLsubEntry);
                                listLsubEntry.addChild(child);
                            }
                            parent = oldEntry.getParentImpl();
                        } else {
                            final int pos = fullName.lastIndexOf(listLsubEntry.getSeparator());
                            if (pos > 0) {
                                final String parentFullName = fullName.substring(0, pos);
                                final ListLsubEntryImpl tmp = map.get(parentFullName);
                                parent = null == tmp ? doSingleListCommand(parentFullName, protocol, lsub) : tmp;
                            } else {
                                parent = map.get(ROOT_FULL_NAME);
                            }
                        }
                        if (null != parent) {
                            listLsubEntry.setParent(parent);
                            parent.addChild(listLsubEntry);
                        }
                    }
                    map.put(fullName, listLsubEntry);
                    r[i] = null;
                }
            }
            /*
             * Dispatch remaining untagged responses
             */
            protocol.notifyResponseHandlers(r);
            LOG.debug("{}", new Object() {

                @Override
                public String toString() {
                    final TreeMap<String, ListLsubEntryImpl> tm = new TreeMap<String, ListLsubEntryImpl>();
                    tm.putAll(map);
                    final StringBuilder sb = new StringBuilder(1024);
                    sb.append((lsub ? "LSUB" : "LIST") + " cache contains after adding single entry \"");
                    sb.append(fullName).append("\":\n");
                    for (final Entry<String, ListLsubEntryImpl> entry : tm.entrySet()) {
                        sb.append('"').append(entry.getKey()).append("\"=").append(entry.getValue()).append('\n');
                    }
                    return sb.toString();
                }
            });
            return retval;
        }
        /*
         * Dispatch remaining untagged responses
         */
        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, new StringBuilder(command).append(" \"\" ").append(args).toString());
        protocol.notifyResponseHandlers(r);
        protocol.handleResult(response);
        /*
         * Never reached...
         */
        return null;
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
        final Iterator<Entry<String, ListLsubEntryImpl>> iter = lsubMap.entrySet().iterator();
        if (!iter.hasNext()) {
            return false;
        }
        final String prefix;
        {
            final Entry<String, ListLsubEntryImpl> entry = iter.next();
            prefix = fullName + entry.getValue().getSeparator();
            if (entry.getKey().startsWith(prefix)) {
                return true;
            }
        }
        while (iter.hasNext()) {
            final Entry<String, ListLsubEntryImpl> entry = iter.next();
            if (entry.getKey().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the LIST entries marked with "\Drafts" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @return The entries
     */
    public Collection<ListLsubEntry> getDraftsEntry() {
        return Collections.unmodifiableCollection(draftsEntries.values());
    }

    /**
     * Gets the LIST entries marked with "\Junk" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @return The entries
     */
    public Collection<ListLsubEntry> getJunkEntry() {
        return Collections.unmodifiableCollection(junkEntries.values());
    }

    /**
     * Gets the LIST entries marked with "\Sent" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @return The entries
     */
    public Collection<ListLsubEntry> getSentEntry() {
        return Collections.unmodifiableCollection(sentEntries.values());
    }

    /**
     * Gets the LIST entries marked with "\Trash" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @return The entries
     */
    public Collection<ListLsubEntry> getTrashEntry() {
        return Collections.unmodifiableCollection(trashEntries.values());
    }

    /**
     * Gets the LIST entries marked with "\Archive" attribute.
     * <p>
     * Needs the <code>"SPECIAL-USE"</code> capability.
     *
     * @return The entries
     */
    public Collection<ListLsubEntry> getArchiveEntry() {
        return Collections.unmodifiableCollection(archiveEntries.values());
    }

    /**
     * Gets the LIST entry for specified full name.
     *
     * @param fullName The full name
     * @return The LIST entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getList(final String fullName) {
        if (null == fullName) {
            return null;
        }
        checkDeprecated();
        return listMap.get(fullName);
    }

    /**
     * Like {@link #getList(String)} but ignores if the collection is deprecated
     *
     * @param fullName The full name
     * @return The LIST entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getListIgnoreDeprecated(final String fullName) {
        if (null == fullName) {
            return null;
        }
        return listMap.get(fullName);
    }

    /**
     * Gets the LSUB entry for specified full name.
     *
     * @param fullName The full name
     * @return The LSUB entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getLsub(final String fullName) {
        if (null == fullName) {
            return null;
        }
        checkDeprecated();
        return lsubMap.get(fullName);
    }

    /**
     * Like {@link #getLsub(String))} but ignores if the collection is deprecated.
     *
     * @param fullName The full name
     * @return The LSUB entry for specified full name or <code>null</code>
     */
    public ListLsubEntry getLsubIgnoreDeprecated(final String fullName) {
        if (null == fullName) {
            return null;
        }
        return lsubMap.get(fullName);
    }

    /**
     * Gets the LSUB entries.
     *
     * @return The LSUB entries
     */
    public List<ListLsubEntry> getLsubs() {
        checkDeprecated();
        return new ArrayList<ListLsubEntry>(lsubMap.values());
    }

    /**
     * Like {@link #getLsubs()} but ignores if the collection is deprecated
     *
     * @return The LSUB entries
     */
    public List<ListLsubEntry> getLsubsIgnoreDeprecated() {
        return new ArrayList<ListLsubEntry>(lsubMap.values());
    }

    /**
     * Gets the LIST entries.
     *
     * @return The LIST entries
     */
    public List<ListLsubEntry> getLists() {
        checkDeprecated();
        return new ArrayList<ListLsubEntry>(listMap.values());
    }

    /**
     * Like {@link #getLists()} but ignores if the collection is deprecated
     *
     * @return The LIST entries
     */
    public List<ListLsubEntry> getListsIgnoreDeprecated() {
        return new ArrayList<ListLsubEntry>(listMap.values());
    }


    private static final TObjectIntHashMap<String> POS_MAP;

    static {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>(6);
        map.put("\\marked", 1);
        map.put("\\unmarked", 2);
        map.put("\\noselect", 3);
        map.put("\\noinferiors", 4);
        map.put("\\haschildren", 5);
        map.put("\\hasnochildren", 6);
        POS_MAP = map;
    }

    private ListLsubEntryImpl parseListResponse(IMAPResponse listResponse, ConcurrentMap<String, ListLsubEntryImpl> lsubMap) {
        return parseListResponse(listResponse, lsubMap, null);
    }

    private ListLsubEntryImpl parseListResponse(IMAPResponse listResponse, ConcurrentMap<String, ListLsubEntryImpl> lsubMap, String[] requiredAttributes) {
        return parseListResponse(listResponse, lsubMap, requiredAttributes, null);
    }

    private ListLsubEntryImpl parseListResponse(IMAPResponse listResponse, ConcurrentMap<String, ListLsubEntryImpl> lsubMap, String[] requiredAttributes, String predefinedName) {
        /*-
         * Parses responses like:
         *
         * LIST (\NoInferiors \UnMarked) "/" "Sent Items"
         */

        // Parse & check attributes
        Set<String> attributes;
        ListLsubEntry.ChangeState changeState = ListLsubEntry.ChangeState.UNDEFINED;
        boolean canOpen = true;
        boolean hasInferiors = true;
        Boolean hasChildren = null;
        {
            String[] s = listResponse.readSimpleList();
            if (s == null) {
                attributes = Collections.emptySet();
                if (null != requiredAttributes) {
                    // Cannot contain any required attribute
                    return null;
                }
            } else {
                // Non-empty attribute list
                if ((s.length <= 0) && (null != requiredAttributes)) {
                    // Cannot contain any required attribute
                    return null;
                }

                attributes = new HashSet<String>(s.length);
                for (int i = s.length; i-- > 0;) {
                    String attr = Strings.asciiLowerCase(s[i]);
                    switch (POS_MAP.get(attr)) {
                        case 1:
                            changeState = ListLsubEntry.ChangeState.CHANGED;
                            break;
                        case 2:
                            changeState = ListLsubEntry.ChangeState.UNCHANGED;
                            break;
                        case 3:
                            canOpen = false;
                            break;
                        case 4:
                            hasInferiors = false;
                            break;
                        case 5:
                            hasChildren = Boolean.TRUE;
                            break;
                        case 6:
                            hasChildren = Boolean.FALSE;
                            break;
                        default:
                            // Nothing
                            break;
                    }
                    attributes.add(attr);
                }
            }
        }

        // Check against required attributes
        if (null != requiredAttributes) {
            boolean containsAny = false;
            for (String requiredAttribute : requiredAttributes) {
                if (attributes.contains(requiredAttribute)) {
                    containsAny = true;
                    break;
                }
            }
            if (!containsAny) {
                return null;
            }
        }

        // Read separator character
        char separator = '/';
        listResponse.skipSpaces();
        if (listResponse.readByte() == '"') {
            if ((separator = (char) listResponse.readByte()) == '\\') {
                // Escaped separator character
                separator = (char) listResponse.readByte();
            }
            listResponse.skip(1);
        } else {
            listResponse.skip(2);
        }

        // Read full name; decode the name (using RFC2060's modified UTF7)
        listResponse.skipSpaces();
        String name = null == predefinedName ? BASE64MailboxDecoder.decode(listResponse.readAtomString()) : predefinedName;

        // Return
        return new ListLsubEntryImpl(name, attributes, separator, changeState, hasInferiors, canOpen, hasChildren, lsubMap).setNamespace(isNamespace(name));
    }

    private String parseEncodedFullName(final IMAPResponse listResponse) {
        /*-
         * LIST (\NoInferiors \UnMarked) "/" "Sent Items"
         *
         * Consume attributes
         */
        listResponse.readSimpleList();
        /*
         * Read separator character
         */
        listResponse.skipSpaces();
        if (listResponse.readByte() == '"') {
            if (((char) listResponse.readByte()) == '\\') {
                /*
                 * Escaped separator character
                 */
                listResponse.readByte();
            }
            listResponse.skip(1);
        } else {
            listResponse.skip(2);
        }
        /*
         * Read full name; decode the name (using RFC2060's modified UTF7)
         */
        listResponse.skipSpaces();
        return listResponse.readAtomString();
    }

    /**
     * Creates an empty {@link ListLsubEntry} for specified full name.
     *
     * @param fullName The full name
     * @return An empty {@link ListLsubEntry}
     */
    protected static ListLsubEntry emptyEntryFor(final String fullName) {
        return new EmptyListLsubEntry(fullName);
    }

    private static class EmptyListLsubEntry implements ListLsubEntry, Serializable {

        private static final long serialVersionUID = -58599804792393870L;

        private final String fullName;

        public EmptyListLsubEntry(final String fullName) {
            super();
            this.fullName = fullName;
        }

        @Override
        public String getName() {
            return fullName.substring(fullName.lastIndexOf('/') + 1);
        }

        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public ListLsubEntry getParent() {
            return null;
        }

        @Override
        public List<ListLsubEntry> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String getFullName() {
            return fullName;
        }

        @Override
        public Set<String> getAttributes() {
            return Collections.emptySet();
        }

        @Override
        public char getSeparator() {
            return '/';
        }

        @Override
        public ListLsubEntry.ChangeState getChangeState() {
            return ListLsubEntry.ChangeState.UNCHANGED;
        }

        @Override
        public boolean hasInferiors() {
            return false;
        }

        @Override
        public boolean canOpen() {
            return false;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public boolean isSubscribed() {
            return false;
        }

        @Override
        public int getMessageCount() {
            return -1;
        }

        @Override
        public int getNewMessageCount() {
            return -1;
        }

        @Override
        public int getUnreadMessageCount() {
            return -1;
        }

        @Override
        public List<ACL> getACLs() {
            return null;
        }

        @Override
        public void rememberACLs(final List<ACL> aclList) {
            // Nothing to do
        }

        @Override
        public void rememberCounts(final int total, final int recent, final int unseen) {
            // Nothing to do
        }

        @Override
        public boolean isNamespace() {
            return false;
        }

        @Override
        public boolean hasChildren() {
            return false;
        }

        @Override
        public Rights getMyRights() {
            return null;
        }

    }

    /**
     * A LIST/LSUB entry.
     */
    private static final class ListLsubEntryImpl implements ListLsubEntry, Comparable<ListLsubEntryImpl>, Serializable {

        private static final long serialVersionUID = 7788296796126971004L;

        private ListLsubEntryImpl parent;

        private Set<ListLsubEntryImpl> children;

        private int[] status;

        private final String fullName;

        private Set<String> attributes;

        private char separator;

        private ChangeState changeState;

        private boolean hasInferiors;

        private boolean canOpen;

        private boolean namespace;

        private int type;

        private final ConcurrentMap<String, ListLsubEntryImpl> lsubMap;

        private transient List<ACL> acls;

        private Boolean hasChildren;

        private Rights myRights;

        private Boolean subscribed;

        private boolean dummy;

        protected ListLsubEntryImpl(final String fullName, final Set<String> attributes, final char separator, final ChangeState changeState, final boolean hasInferiors, final boolean canOpen, final Boolean hasChildren, final ConcurrentMap<String, ListLsubEntryImpl> lsubMap) {
            super();
            this.fullName = checkFullName(fullName, separator);
            this.attributes = attributes;
            this.separator = separator;
            this.changeState = changeState;
            this.hasInferiors = hasInferiors;
            this.canOpen = canOpen;
            this.hasChildren = hasChildren;
            int type = 0;
            if (hasInferiors) {
                type |= Folder.HOLDS_FOLDERS;
            }
            if (canOpen) {
                type |= Folder.HOLDS_MESSAGES;
            }
            this.type = type;
            this.lsubMap = lsubMap;
            dummy = false;
        }

        protected ListLsubEntryImpl(final ListLsubEntryImpl newEntry, final boolean subscribed) {
            super();
            fullName = newEntry.fullName;
            attributes = newEntry.attributes;
            canOpen = newEntry.canOpen;
            changeState = newEntry.changeState;
            hasInferiors = newEntry.hasInferiors;
            separator = newEntry.separator;
            type = newEntry.type;
            namespace = newEntry.namespace;
            hasChildren = newEntry.hasChildren;
            this.subscribed = Boolean.valueOf(subscribed);
            lsubMap = null;
            dummy = false;
        }

        protected void copyFrom(final ListLsubEntryImpl newEntry) {
            if (newEntry == null) {
                return;
            }
            attributes = newEntry.attributes;
            canOpen = newEntry.canOpen;
            changeState = newEntry.changeState;
            hasInferiors = newEntry.hasInferiors;
            separator = newEntry.separator;
            type = newEntry.type;
            namespace = newEntry.namespace;
            hasChildren = newEntry.hasChildren;
            dummy = newEntry.dummy;
        }

        protected void clearChildren() {
            if (children != null) {
                children.clear();
            }
        }

        /**
         * Sets the dummy flag
         *
         * @param dummy The dummy flag to set
         * @return This instance
         */
        protected ListLsubEntryImpl setDummy(boolean dummy) {
            this.dummy = dummy;
            return this;
        }

        /**
         * Gets the dummy flag
         *
         * @return The dummy flag
         */
        protected boolean isDummy() {
            return dummy;
        }

        @Override
        public String getName() {
            return fullName.substring(fullName.lastIndexOf(separator) + 1);
        }

        /**
         * Sets this LIST/LSUB entry's parent.
         *
         * @param parent The parent
         */
        protected void setParent(final ListLsubEntryImpl parent) {
            this.parent = parent;
        }

        @Override
        public ListLsubEntry getParent() {
            return parent;
        }

        /**
         * Gets the parent.
         *
         * @return The parent
         */
        protected ListLsubEntryImpl getParentImpl() {
            return parent;
        }

        /**
         * Adds specified LIST/LSUB entry to this LIST/LSUB entry's children
         *
         * @param child The child LIST/LSUB entry
         */
        protected void addChild(final ListLsubEntryImpl child) {
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
         * Removes specified LIST/LSUB entry from this LIST/LSUB entry's children
         *
         * @param child The child LIST/LSUB entry
         */
        protected void removeChild(final ListLsubEntryImpl child) {
            if (null == child) {
                return;
            }
            if (null == children) {
                return;
            }
            children.remove(child);
        }

        /**
         * Adds (if absent) specified LIST/LSUB entry to this LIST/LSUB entry's children
         *
         * @param child The child LIST/LSUB entry
         */
        protected void addChildIfAbsent(final ListLsubEntryImpl child) {
            if (null == child) {
                return;
            }
            if (null == children) {
                children = new HashSet<ListLsubEntryImpl>(8);
            }
            children.add(child);
        }

        @Override
        public List<ListLsubEntry> getChildren() {
            return null == children ? Collections.<ListLsubEntry> emptyList() : new ArrayList<ListLsubEntry>(children);
        }

        protected Set<ListLsubEntryImpl> getChildrenSet() {
            return null == children ? Collections.<ListLsubEntryImpl> emptySet() : children;
        }

        /**
         * Checks if children listing is empty
         *
         * @return <code>true</code> if there are no children listed; otherwise <code>false</code>
         */
        protected boolean emptyChildren() {
            return null == children ? true : children.isEmpty();
        }

        @Override
        public String getFullName() {
            return fullName;
        }

        @Override
        public Set<String> getAttributes() {
            return attributes;
        }

        @Override
        public char getSeparator() {
            return separator;
        }

        @Override
        public ChangeState getChangeState() {
            return changeState;
        }

        @Override
        public boolean hasInferiors() {
            return hasInferiors;
        }

        protected ListLsubEntryImpl setCanOpen(final boolean canOpen) {
            this.canOpen = canOpen;
            return this;
        }

        @Override
        public boolean canOpen() {
            return canOpen;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public boolean exists() {
            return true;
        }

        protected void setSubscribed(final boolean subscribed) {
            this.subscribed = Boolean.valueOf(subscribed);
        }

        @Override
        public boolean isSubscribed() {
            return null == subscribed ? (null == lsubMap ? true : lsubMap.containsKey(fullName)) : subscribed.booleanValue();
        }

        /**
         * Sets the status.
         *
         * @param status The status
         */
        protected void setStatus(final int[] status) {
            if (null == status) {
                this.status = null;
                return;
            }
            this.status = new int[status.length];
            System.arraycopy(status, 0, this.status, 0, status.length);
        }

        @Override
        public int getMessageCount() {
            return null == status ? -1 : status[0];
        }

        @Override
        public int getNewMessageCount() {
            return null == status ? -1 : status[1];
        }

        @Override
        public int getUnreadMessageCount() {
            return null == status ? -1 : status[2];
        }

        protected void setMyRights(final Rights myRights) {
            this.myRights = myRights;
        }

        /**
         * Gets MYRIGHTS.
         *
         * @return MYRIGHTS or <code>null</code> if absent
         */
        @Override
        public Rights getMyRights() {
            return myRights;
        }

        /**
         * Sets the ACLs.
         *
         * @param acls The ACL list
         */
        protected void setAcls(final List<ACL> acls) {
            this.acls = acls;
        }

        @Override
        public List<ACL> getACLs() {
            return acls == null ? null : new ArrayList<ACL>(acls);
        }

        @Override
        public void rememberACLs(final List<ACL> aclList) {
            this.acls = new ArrayList<ACL>(aclList);
        }

        @Override
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
            StringBuilder sb = new StringBuilder(128).append("{ ").append(lsubMap == null ? "LSUB" : "LIST");
            sb.append(" fullName=\"").append(fullName).append('"');
            sb.append(", subscribed=\"").append(isSubscribed()).append('"');
            sb.append(", parent=");
            if (null == parent) {
                sb.append("null");
            } else {
                sb.append('"').append(parent.getFullName()).append('"');
            }
            sb.append(", canOpen=").append(canOpen);
            sb.append(", attributes=(");
            if (null != attributes && !attributes.isEmpty()) {
                final Iterator<String> iterator = new TreeSet<String>(attributes).iterator();
                sb.append('"').append(iterator.next()).append('"');
                while (iterator.hasNext()) {
                    sb.append(", \"").append(iterator.next()).append('"');
                }
            }
            sb.append(')');
            sb.append(", children=(");
            if (null != children && !children.isEmpty()) {
                final Iterator<ListLsubEntryImpl> iterator = new TreeSet<ListLsubEntryImpl>(children).iterator();
                sb.append('"').append(iterator.next().getFullName()).append('"');
                while (iterator.hasNext()) {
                    sb.append(", \"").append(iterator.next().getFullName()).append('"');
                }
            }
            sb.append(") }");
            return sb.toString();
        }

        @Override
        public int compareTo(final ListLsubEntryImpl anotherEntry) {
            final String anotherFullName = anotherEntry.fullName;
            return fullName == null ? (anotherFullName == null ? 0 : -1) : fullName.compareToIgnoreCase(anotherFullName);
        }

        /**
         * Sets the namespace flag
         *
         * @param namespace The namespace flag
         */
        protected ListLsubEntryImpl setNamespace(final boolean namespace) {
            this.namespace = namespace;
            return this;
        }

        @Override
        public boolean isNamespace() {
            return namespace;
        }

        @Override
        public boolean hasChildren() {
            return null == hasChildren ? (null != children && !children.isEmpty()) : hasChildren.booleanValue();
        }

    } // End of class ListLsubEntryImpl

    private static void appendStackTrace(final StackTraceElement[] trace, final StringBuilder sb, final String lineSeparator) {
        if (null == trace) {
            sb.append("<missing stack trace>\n");
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    /** Checks the full name */
    protected static String checkFullName(final String fullName, final char separator) {
        if (null == fullName) {
            return fullName;
        }
        if (String.valueOf(separator).equals(fullName)) {
            return ROOT_FULL_NAME;
        }
        final String upperCase = Strings.toUpperCase(fullName);
        if (INBOX.equals(upperCase)) {
            return INBOX;
        }
        if (fullName.length() > 5 && upperCase.startsWith("INBOX") && separator == upperCase.charAt(5)) {
            return new StringBuilder(INBOX).append(separator).append(fullName.substring(6)).toString();
        }
        return fullName;
    }
}
