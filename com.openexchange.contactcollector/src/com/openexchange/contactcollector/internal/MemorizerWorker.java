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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contactcollector.osgi.CCServiceRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
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
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link MemorizerWorker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemorizerWorker {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MemorizerWorker.class));

    private static final boolean ALL_ALIASES = true;

    /*-
     * Member stuff
     */

    private final BlockingQueue<MemorizerTask> queue;

    private final AtomicReference<Future<Object>> mainFutureRef;

    private final AtomicBoolean flag;

    private final ReadWriteLock readWriteLock;

    /**
     * Initializes a new {@link MemorizerWorker}.
     *
     * @throws OXException If thread pool service is missing
     */
    public MemorizerWorker() throws OXException {
        super();
        readWriteLock = new ReentrantReadWriteLock();
        this.flag = new AtomicBoolean(true);
        this.queue = new LinkedBlockingQueue<MemorizerTask>();
        final ThreadPoolService tps = ThreadPools.getThreadPool();
        mainFutureRef = new AtomicReference<Future<Object>>();
        mainFutureRef.set(tps.submit(ThreadPools.task(new MemorizerCallable(flag, queue, readWriteLock.writeLock()), "ContactCollector"), CallerRunsBehavior.<Object> getInstance()));
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
     * @throws OXException If thread pool service is missing
     */
    public void submit(final MemorizerTask memorizerTask) throws OXException {
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            if (!flag.get()) {
                /*
                 * Closed
                 */
                return;
            }
            Future<Object> f = mainFutureRef.get();
            if (isDone(f)) {
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
                        /*
                         * Closed
                         */
                        return;
                    }
                    f = mainFutureRef.get();
                    if (isDone(f)) {
                        /*
                         * Check if needed service is present
                         */
                        final ThreadPoolService tps = ThreadPools.getThreadPool();
                        /*
                         * Offer task
                         */
                        queue.offer(memorizerTask);
                        /*
                         * Start new thread for processing tasks from queue
                         */
                        f = tps.submit(ThreadPools.task(new MemorizerCallable(flag, queue, writeLock), "ContactCollector"), CallerRunsBehavior.<Object> getInstance());
                        mainFutureRef.set(f);
                    } else {
                        /*
                         * Thread is running; offer task
                         */
                        queue.offer(memorizerTask);
                    }
                } finally {
                    /*
                     * Downgrade lock
                     */
                    readLock.lock();
                    writeLock.unlock();
                }
            } else {
                /*
                 * Thread is running; offer task
                 */
                queue.offer(memorizerTask);
            }
        } finally {
            readLock.unlock();
        }
    }

    private static boolean isDone(final Future<Object> f) {
        return ((null == f) || f.isDone());
    }

    private static final class MemorizerCallable implements Callable<Object> {

        private final AtomicBoolean flag;

        private final BlockingQueue<MemorizerTask> queue;

        private final Lock writeLock;

        public MemorizerCallable(final AtomicBoolean flag, final BlockingQueue<MemorizerTask> queue, final Lock writeLock) {
            super();
            this.flag = flag;
            this.queue = queue;
            this.writeLock = writeLock;
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
                for (final MemorizerTask task : tasks) {
                    handleTask(task);
                }
            }
            return null;
        }

    }

    /**
     * Handles specified task
     *
     * @param memorizerTask The task
     */
    static void handleTask(final MemorizerTask memorizerTask) {
        final Session session = memorizerTask.getSession();
        if (!isEnabled(session) || getFolderId(session) == 0) {
            return;
        }
        final Context ctx;
        final Set<InternetAddress> aliases;
        final UserConfiguration userConfig;
        try {
            final ServiceLookup serviceRegistry = CCServiceRegistry.getInstance();
            final ContextService contextService = serviceRegistry.getService(ContextService.class);
            if (null == contextService) {
                LOG.warn("Contact collector run aborted: missing context service");
                return;
            }
            ctx = contextService.getContext(session.getContextId());

            final UserService userService = serviceRegistry.getService(UserService.class);
            if (null == userService) {
                LOG.warn("Contact collector run aborted: missing user service");
                return;
            }
            if (ALL_ALIASES) {
                // All context-known users' aliases
                aliases = AliasesProvider.getInstance().getContextAliases(ctx, userService);
            } else {
                // Only aliases of session user
                aliases = AliasesProvider.getInstance().getAliases(userService.getUser(session.getUserId(), ctx));
            }

            final UserConfigurationService userConfigurationService = serviceRegistry.getService(UserConfigurationService.class);
            if (null == userConfigurationService) {
                LOG.warn("Contact collector run aborted: missing user configuration service");
                return;
            }
            userConfig = userConfigurationService.getUserConfiguration(session.getUserId(), ctx);
        } catch (final OXException e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        } catch (final Exception e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        }
        /*
         * Iterate addresses
         */
        for (final InternetAddress address : memorizerTask.getAddresses()) {
            /*
             * Check if address is contained in user's aliases
             */
            if (!aliases.contains(address)) {
                try {
                    memorizeContact(address, session, ctx, userConfig);
                } catch (final OXException e) {
                    LOG.warn("Contact collector run aborted for address: " + address.toUnicodeString(), e);
                }
            }
        }
    }

    private static final ContactField[] FIELDS = {
    	ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.LAST_MODIFIED, ContactField.USE_COUNT };

	private static final ContactField[] SEARCH_FIELDS = { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };

    private static int memorizeContact(final InternetAddress address, final Session session, final Context ctx, final UserConfiguration userConfig) throws OXException {
        /*
         * Convert email address to a contact
         */
        final Contact contact;
        try {
            contact = transformInternetAddress(address, session);
        } catch (final ParseException e) {
            // Decoding failed; ignore contact
            LOG.warn(e.getMessage(), e);
            return -1;
        } catch (final UnsupportedEncodingException e) {
            // Decoding failed; ignore contact
            LOG.warn(e.getMessage(), e);
            return -1;
        }
        /*
         * Check if such a contact already exists
         */
        final ContactService contactService = CCServiceRegistry.getInstance().getService(ContactService.class);
        final Contact foundContact;
        {
        	final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        	for (final ContactField field : SEARCH_FIELDS) {
        		final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
        		term.addOperand(new ContactFieldOperand(field));
        		term.addOperand(new ConstantOperand<String>(contact.getEmail1()));
        		orTerm.addSearchTerm(term);
        	}
        	final SearchIterator<Contact> iterator = contactService.searchContacts(session, orTerm, FIELDS);
            try {
                if (iterator.hasNext()) {
                    foundContact = iterator.next();
                } else {
                    foundContact = null;
                }
            } finally {
                iterator.close();
            }
        }
        /*
         * Either create contact or increment its use count
         */
        final int retval;
        if (null == foundContact) {
            final OXFolderAccess folderAccess = new OXFolderAccess(ctx);
            final int folderId = getFolderId(session);
            if (folderAccess.exists(folderId)) {
                final OCLPermission perm = folderAccess.getFolderPermission(folderId, session.getUserId(), userConfig);
                if (perm.canCreateObjects()) {
                    contact.setUseCount(1);
                    contactService.createContact(session, Integer.toString(contact.getParentFolderID()), contact);
                    retval = contact.getObjectID();
                } else {
                    retval = -1;
                }
            } else {
                retval = -1;
            }
        } else {
            final int currentCount = foundContact.getUseCount();
            final int newCount = currentCount + 1;
            foundContact.setUseCount(newCount);
            final OCLPermission perm =
                new OXFolderAccess(ctx).getFolderPermission(foundContact.getParentFolderID(), session.getUserId(), userConfig);
            if (perm.canWriteAllObjects()) {
                contactService.updateContact(session, Integer.toString(foundContact.getParentFolderID()),
                		Integer.toString(foundContact.getObjectID()), foundContact, foundContact.getLastModified());
            }
            retval = foundContact.getObjectID();
        }
        return retval;
    }

    private static boolean isEnabled(final Session session) {
        Boolean enabled = null;
        boolean enabledRight = false;
        try {
            enabled = ServerUserSetting.getInstance().isContactCollectionEnabled(session.getContextId(), session.getUserId());
            enabledRight = ServerSessionAdapter.valueOf(session).getUserPermissionBits().isCollectEmailAddresses();
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        return enabledRight && enabled != null && enabled.booleanValue();
    }

    private static int getFolderId(final Session session) {
        try {
            final Integer folder = ServerUserSetting.getInstance().getContactCollectionFolder(session.getContextId(), session.getUserId());
            return null == folder ? 0 : folder.intValue();
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            return 0;
        }
    }

    private static Contact transformInternetAddress(final InternetAddress address, final Session session) throws ParseException, UnsupportedEncodingException {
        final Contact retval = new Contact();
        final String addr = decodeMultiEncodedValue(IDNA.toIDN(address.getAddress()));
        retval.setEmail1(addr);
        final String displayName;
        if (address.getPersonal() != null && !"".equals(address.getPersonal().trim())) {
            displayName = decodeMultiEncodedValue(address.getPersonal());
        } else {
            displayName = addr;
        }
        retval.setDisplayName(displayName);
        retval.setParentFolderID(getFolderId(session));
        return retval;
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
            final com.openexchange.java.StringAllocator sa = new com.openexchange.java.StringAllocator(val.length());
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

}
