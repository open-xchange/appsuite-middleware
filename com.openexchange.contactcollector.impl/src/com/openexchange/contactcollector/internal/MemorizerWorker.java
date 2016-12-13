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

package com.openexchange.contactcollector.internal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.objectusecount.BatchIncrementArguments;
import com.openexchange.objectusecount.BatchIncrementArguments.ObjectAndFolder;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * {@link MemorizerWorker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemorizerWorker {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MemorizerWorker.class);

    private static final boolean ALL_ALIASES = true;

    /*-
     * Member stuff
     */

    final ServiceLookup services;
    final BlockingQueue<MemorizerTask> queue;
    private final AtomicReference<Future<Object>> mainFutureRef;
    final AtomicBoolean flag;
    final ReadWriteLock readWriteLock;

    /**
     * Initializes a new {@link MemorizerWorker}.
     */
    public MemorizerWorker(ServiceLookup services) {
        super();
        this.services = services;
        readWriteLock = new ReentrantReadWriteLock();
        this.flag = new AtomicBoolean(true);
        this.queue = new LinkedBlockingQueue<MemorizerTask>();
        final ThreadPoolService tps = ThreadPools.getThreadPool();
        mainFutureRef = new AtomicReference<Future<Object>>();
        mainFutureRef.set(tps.submit(ThreadPools.task(new MemorizerCallable(), "ContactCollector"), CallerRunsBehavior.<Object> getInstance()));
    }

    /**
     * Closes this worker.
     */
    public void close() {
        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            flag.set(false);
            final Future<Object> mainFuture = mainFutureRef.get();
            if (null != mainFuture && mainFutureRef.compareAndSet(mainFuture, null)) {
                mainFuture.cancel(true);
            }
            queue.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Submits specified task.
     *
     * @param memorizerTask The task
     */
    public void submit(final MemorizerTask memorizerTask) {
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            if (!flag.get()) {
                // Shut-down in the meantime
                return;
            }

            Future<Object> f = mainFutureRef.get();
            if (!isDone(f)) {
                // Worker thread is running; offer task
                queue.offer(memorizerTask);
                return;
            }

            /*-
             * Upgrade lock manually
             *
             * Must unlock first to obtain write lock
             */
            readLock.unlock();
            final Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                if (!flag.get()) {
                    // Shut-down in the meantime
                    return;
                }

                f = mainFutureRef.get();
                if (!isDone(f)) {
                    // Worker thread got initialized meanwhile; offer task
                    queue.offer(memorizerTask);
                    return;
                }

                // Grab thread pool service
                ThreadPoolService tps = ThreadPools.getThreadPool();

                // Offer task
                queue.offer(memorizerTask);

                // Start new thread for processing tasks from queue
                f = tps.submit(ThreadPools.task(new MemorizerCallable(), "ContactCollector"), CallerRunsBehavior.<Object> getInstance());
                mainFutureRef.set(f);
            } finally {
                /*
                 * Downgrade lock
                 */
                readLock.lock();
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
    }

    private static boolean isDone(final Future<Object> f) {
        return ((null == f) || f.isDone());
    }

    private final class MemorizerCallable implements Callable<Object> {

        MemorizerCallable() {
            super();
        }

        private final void waitForTasks(final List<MemorizerTask> tasks) throws InterruptedException {
            waitForTasks(tasks, 10);
        }

        private final void pollForTasks(final List<MemorizerTask> tasks) throws InterruptedException {
            waitForTasks(tasks, 0);
        }

        private final void waitForTasks(final List<MemorizerTask> tasks, final int timeoutSeconds) throws InterruptedException {
            /*
             * Wait for a task to become available
             */
            MemorizerTask task = timeoutSeconds <= 0 ? queue.poll() : queue.poll(timeoutSeconds, TimeUnit.SECONDS);
            if (null == task) {
                return;
            }
            tasks.add(task);
            /*
             * Gather possibly available tasks but don't wait
             */
            while ((task = queue.poll()) != null) {
                tasks.add(task);
            }
        }

        @Override
        public Object call() throws Exception {
            /*
             * Stay active as long as flag is true
             */
            final List<MemorizerTask> tasks = new ArrayList<MemorizerTask>();
            while (flag.get()) {
                /*
                 * Wait for IDs
                 */
                tasks.clear();
                waitForTasks(tasks);
                if (tasks.isEmpty()) {
                    /*
                     * Wait time elapsed and no new tasks were offered
                     */
                    Lock writeLock = readWriteLock.writeLock();
                    writeLock.lock();
                    try {
                        /*
                         * Still no new tasks?
                         */
                        pollForTasks(tasks);
                        if (tasks.isEmpty()) {
                            return null;
                        }
                    } finally {
                        writeLock.unlock();
                    }
                }
                /*
                 * Process tasks
                 */
                for (MemorizerTask task : tasks) {
                    handleTask(task, services);
                }
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Handles specified task
     *
     * @param memorizerTask The task
     * @param services The service look-up
     */
    static void handleTask(MemorizerTask memorizerTask, ServiceLookup services) {
        Session session = memorizerTask.getSession();
        if (!isEnabled(session)) {
            return;
        }

        int folderId = getFolderId(session);
        if (folderId == 0) {
            return;
        }

        // Get associated context
        Context ctx;
        try {
            ContextService contextService = services.getOptionalService(ContextService.class);
            if (null == contextService) {
                LOG.warn("Contact collector run aborted: missing context service");
                return;
            }
            ctx = contextService.getContext(session.getContextId());
        } catch (final Exception e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        }

        // Strip by well-known aliases
        final Set<InternetAddress> addresses = new LinkedHashSet<InternetAddress>(memorizerTask.getAddresses());
        try {
            UserService userService = services.getOptionalService(UserService.class);
            if (null == userService) {
                LOG.warn("Contact collector run aborted: missing user service");
                return;
            }

            UserAliasStorage aliasStorage = services.getOptionalService(UserAliasStorage.class);
            Set<InternetAddress> aliases;
            if (ALL_ALIASES) {
                // All context-known users' aliases
                aliases = AliasesProvider.getInstance().getContextAliases(ctx, userService, aliasStorage);
            } else {
                // Only aliases of session user
                aliases = AliasesProvider.getInstance().getAliases(userService.getUser(session.getUserId(), ctx), ctx, aliasStorage);
            }
            addresses.removeAll(aliases);
        } catch (final Exception e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        }

        if (addresses.isEmpty()) {
            return;
        }

        LOG.debug("Contact collector run for addresses: {}", new Object() {

            @Override
            public String toString() {
                return addresses.toString();
            }
        });

        UserConfiguration userConfig;
        ContactService contactService;
        try {
            UserConfigurationService userConfigurationService = services.getOptionalService(UserConfigurationService.class);
            if (null == userConfigurationService) {
                LOG.warn("Contact collector run aborted: missing user configuration service");
                return;
            }

            contactService = services.getOptionalService(ContactService.class);
            if (null == contactService) {
                LOG.warn("Contact collector run aborted: missing contact service");
                return;
            }

            userConfig = userConfigurationService.getUserConfiguration(session.getUserId(), ctx);
        } catch (final Exception e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        }

        // Iterate addresses and collect use-counts
        TObjectIntMap<BatchIncrementArguments.ObjectAndFolder> useCounts = memorizerTask.isIncrementUseCount() ? new TObjectIntHashMap<BatchIncrementArguments.ObjectAndFolder>(6, 0.9F, 0) : null;
        for (InternetAddress address : addresses) {
            try {
                memorizeContact(address, folderId, session, ctx, userConfig, contactService, useCounts, services);
            } catch (Exception e) {
                LOG.warn("Contact collector run aborted for address: {}", address.toUnicodeString(), e);
            }
        }
        if (null != useCounts && !useCounts.isEmpty()) {
            ObjectUseCountService useCountService = services.getOptionalService(ObjectUseCountService.class);
            batchIncrementUseCount(useCounts, session, useCountService);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private static final ContactField[] FIELDS = { ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.LAST_MODIFIED };

	private static final ContactField[] SEARCH_FIELDS = { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };

    static void memorizeContact(InternetAddress address, int folderId, Session session, Context ctx, UserConfiguration userConfig, ContactService contactService, TObjectIntMap<BatchIncrementArguments.ObjectAndFolder> useCounts, ServiceLookup services) throws OXException {
        // Convert email address to a contact
        Contact contact = transformInternetAddress(address, session);
        if (null == contact) {
            return;
        }

        // Check if such a contact already exists to either create a contact or increment its use count
        List<Contact> foundContacts = null;
        {
        	CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        	for (ContactField field : SEARCH_FIELDS) {
        		SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
        		term.addOperand(new ContactFieldOperand(field));
        		term.addOperand(new ConstantOperand<String>(contact.getEmail1()));
        		orTerm.addSearchTerm(term);
        	}

        	SortOptions sortOptions = new SortOptions(ContactField.USE_COUNT, Order.DESCENDING);
        	sortOptions.setLimit(getSearchLimit(services));

        	SearchIterator<Contact> iterator = contactService.searchContacts(session, orTerm, FIELDS, sortOptions);
            try {
                if (iterator.hasNext()) {
                    // At least one such contact found: If no use-count service was passed, then there is nothing to do.
                    if (null == useCounts) {
                        return;
                    }

                    foundContacts = new LinkedList<Contact>();
                    do {
                        foundContacts.add(iterator.next());
                    } while (iterator.hasNext());
                }
            } finally {
                SearchIterators.close(iterator);
            }
        }

        // No such contacts?
        if (null == foundContacts) {
            OXFolderAccess folderAccess = new OXFolderAccess(ctx);
            if (!folderAccess.exists(folderId)) {
                // Contact collector folder does not exist
                return;
            }

            OCLPermission perm = folderAccess.getFolderPermission(folderId, session.getUserId(), userConfig);
            if (!perm.canCreateObjects()) {
                // Insufficient permissions granted on contact collector folder
                return;
            }

            contactService.createContact(session, Integer.toString(contact.getParentFolderID()), contact);
            int objectId = contact.getObjectID();
            BatchIncrementArguments.ObjectAndFolder key = new BatchIncrementArguments.ObjectAndFolder(objectId, folderId);
            int count = useCounts.get(key);
            useCounts.put(key, count + 1);
            return;
        }

        // Such contacts already exists. Increment their use count
        if (null != useCounts) {
            int numOfFoundContacts = foundContacts.size();
            if (numOfFoundContacts > 0) {
                if (1 == numOfFoundContacts) {
                    Contact foundContact = foundContacts.get(0);
                    OCLPermission perm = new OXFolderAccess(ctx).getFolderPermission(foundContact.getParentFolderID(), session.getUserId(), userConfig);
                    if (perm.canWriteAllObjects()) {
                        BatchIncrementArguments.ObjectAndFolder key = new BatchIncrementArguments.ObjectAndFolder(foundContact.getObjectID(), foundContact.getParentFolderID());
                        int count = useCounts.get(key);
                        useCounts.put(key, count + 1);
                    }
                } else {
                    Set<IntPair> alreadyProcessed = new HashSet<IntPair>(foundContacts.size(), 0.9F);
                    for (Contact foundContact : foundContacts) {
                        int objectID = foundContact.getObjectID();
                        int parentFolderID = foundContact.getParentFolderID();
                        if (alreadyProcessed.add(new IntPair(objectID, parentFolderID))) {
                            OCLPermission perm = new OXFolderAccess(ctx).getFolderPermission(parentFolderID, session.getUserId(), userConfig);
                            if (perm.canWriteAllObjects()) {
                                BatchIncrementArguments.ObjectAndFolder key = new BatchIncrementArguments.ObjectAndFolder(objectID, parentFolderID);
                                int count = useCounts.get(key);
                                useCounts.put(key, count + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    private static volatile Integer searchLimit;
    static int getSearchLimit(ServiceLookup services) {
        Integer tmp = searchLimit;
        if (null == tmp) {
            synchronized (MemorizerWorker.class) {
                tmp = searchLimit;
                if (null == tmp) {
                    int defaultLimit = 5;
                    ConfigurationService configurationService = services.getOptionalService(ConfigurationService.class);
                    if (null == configurationService) {
                        return defaultLimit;
                    }
                    tmp = Integer.valueOf(configurationService.getIntProperty("com.openexchange.contactcollector.searchLimit", defaultLimit));
                    searchLimit = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    static void incrementUseCount(int objectId, int folderId, Session session, ObjectUseCountService useCountService) {
        if (null != useCountService) {
            try {
                useCountService.incrementObjectUseCount(session, new IncrementArguments.Builder(objectId, folderId).build());
            } catch (Exception e) {
                LOG.warn("Failed to increment use count for contact {} inside folder {} for user {} in context {}", objectId, folderId, session.getUserId(), session.getContextId(), e);
            }
        }
    }

    static void batchIncrementUseCount(TObjectIntMap<BatchIncrementArguments.ObjectAndFolder> useCounts, Session session, ObjectUseCountService useCountService) {
        if (null != useCounts && null != useCountService) {
            try {
                BatchIncrementArguments.Builder builder = new BatchIncrementArguments.Builder();

                TObjectIntIterator<ObjectAndFolder> iterator = useCounts.iterator();
                for (int i = useCounts.size(); i-- > 0;) {
                    iterator.advance();
                    ObjectAndFolder key = iterator.key();
                    int count = iterator.value();
                    for (int k = count; k-- > 0;) {
                        builder.add(key.getObjectId(), key.getFolderId());
                    }
                }

                useCountService.incrementObjectUseCount(session, builder.build());
            } catch (Exception e) {
                LOG.warn("Failed to batch increment use count for user {} in context {}", session.getUserId(), session.getContextId(), e);
            }
        }
    }

    static boolean isEnabled(final Session session) {
        try {
            return ServerSessionAdapter.valueOf(session).getUserPermissionBits().isCollectEmailAddresses();
        } catch (final OXException e) {
            LOG.error("", e);
        }
        return false;
    }

    static int getFolderId(final Session session) {
        try {
            final Integer folder = ServerUserSetting.getInstance().getContactCollectionFolder(session.getContextId(), session.getUserId());
            return null == folder ? 0 : folder.intValue();
        } catch (final OXException e) {
            LOG.error("", e);
            return 0;
        }
    }

    private static Contact transformInternetAddress(final InternetAddress address, final Session session) {
        try {
            Contact retval = new Contact();
            retval.setParentFolderID(getFolderId(session));
            final String addr = decodeMultiEncodedValue(IDNA.toIDN(address.getAddress()));
            retval.setEmail1(addr);
            if (false == Strings.isEmpty(address.getPersonal())) {
                String displayName = decodeMultiEncodedValue(address.getPersonal());
                retval.setDisplayName(displayName);
            } else {
                retval.setDisplayName(addr);
            }
            return retval;
        } catch (ParseException e) {
            // Decoding failed; ignore contact
            LOG.warn("", e);
        } catch (UnsupportedEncodingException e) {
            // Decoding failed; ignore contact
            LOG.warn("", e);
        }

        return null;
    }

    private static final Pattern ENC_PATTERN = Pattern.compile("(=\\?\\S+?\\?\\S+?\\?)(.+?)(\\?=)");

    /**
     * Decodes a multi-mime-encoded value using the algorithm specified in RFC 2047, Section 6.1.
     * <p>
     * If the charset-conversion fails for any sequence, an {@link UnsupportedEncodingException} is thrown.
     * <p>
     * If the String is not a RFC 2047 style encoded value, it is returned as-is
     *
     * @param value The possibly encoded value
     * @return The possibly decoded value
     * @throws UnsupportedEncodingException If an unsupported charset encoding occurs
     * @throws ParseException If encoded value cannot be decoded
     */
    private static String decodeMultiEncodedValue(final String value) throws ParseException, UnsupportedEncodingException {
        if (value == null) {
            return null;
        }
        final String val = MimeUtility.unfold(value);
        final Matcher m = ENC_PATTERN.matcher(val);
        if (m.find()) {
            final StringBuilder sa = new StringBuilder(val.length());
            int lastMatch = 0;
            do {
                sa.append(val.substring(lastMatch, m.start()));
                sa.append(com.openexchange.java.Strings.quoteReplacement(MimeUtility.decodeWord(m.group())));
                lastMatch = m.end();
            } while (m.find());
            sa.append(val.substring(lastMatch));
            return sa.toString();
        }
        return val;
    }

    private static final class IntPair {

        private final int i1;
        private final int i2;
        private final int hash;

        IntPair(int i1, int i2) {
            super();
            this.i1 = i1;
            this.i2 = i2;

            int prime = 31;
            int result = prime * 1 + i1;
            result = prime * result + i2;
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            IntPair other = (IntPair) obj;
            if (i1 != other.i1) {
                return false;
            }
            if (i2 != other.i2) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("IntPair [i1=").append(i1).append(", i2=").append(i2).append("]");
            return builder.toString();
        }
    }

}
