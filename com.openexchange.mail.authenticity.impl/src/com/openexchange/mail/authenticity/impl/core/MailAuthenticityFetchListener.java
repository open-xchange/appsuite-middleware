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

package com.openexchange.mail.authenticity.impl.core;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailAttributation;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailFetchArguments;
import com.openexchange.mail.MailFetchListener;
import com.openexchange.mail.MailFetchListenerResult;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailFields;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.mail.authenticity.impl.helper.NotAnalyzedAuthenticityHandler;
import com.openexchange.mail.authenticity.impl.osgi.Services;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTrackableTask;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link MailAuthenticityFetchListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityFetchListener implements MailFetchListener {

    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailAuthenticityFetchListener.class);

    private static final String STATE_PARAM_HANDLER = "mail.authenticity.handler";

    private final MailAuthenticityHandlerRegistry handlerRegistry;
    private final ThreadPoolService threadPool;

    /**
     * Initializes a new {@link MailAuthenticityFetchListener}.
     *
     * @param threadPool
     */
    public MailAuthenticityFetchListener(MailAuthenticityHandlerRegistry handlerRegistry, ThreadPoolService threadPool) {
        super();
        this.handlerRegistry = handlerRegistry;
        this.threadPool = threadPool;
    }

    private boolean isNotApplicableFor(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        return false == isApplicableFor(fetchArguments, mailAccess);
    }

    private boolean isApplicableFor(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        FullnameArgument folder = fetchArguments.getFolder();
        return null != folder && isAccountAccepted(folder.getAccountId(), mailAccess.getSession()) && isFolderAccepted(folder.getFullName(), mailAccess);
    }

    private boolean isApplicableFor(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        return isAccountAccepted(mail.getAccountId(), mailAccess.getSession()) && isFolderAccepted(mail.getFolder(), mailAccess);
    }

    private boolean isAccountAccepted(int accountId, Session session) throws OXException {
        if (MailAccount.DEFAULT_ID == accountId) {
            // Primary account
            return true;
        }

        // Check for special Unified Mail account
        return getUnifiedINBOXAccountId(session) == accountId;
    }

    private int getUnifiedINBOXAccountId(Session session) throws OXException {
        UnifiedInboxManagement unifiedInboxManagement = Services.optService(UnifiedInboxManagement.class);
        if (null == unifiedInboxManagement) {
            return -1;
        }

        return unifiedInboxManagement.getUnifiedINBOXAccountID(session);
    }

    private boolean isAuthResultRequested(MailFetchArguments fetchArguments) {
        MailFields mailFields = new MailFields(fetchArguments.getFields());
        return mailFields.contains(MailField.AUTHENTICATION_OVERALL_RESULT) || mailFields.contains(MailField.AUTHENTICATION_MECHANISM_RESULTS);
    }

    private boolean isFolderAccepted(String fullName, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        return !fullName.equals(mailAccess.getFolderStorage().getDraftsFolder()) && !fullName.equals(mailAccess.getFolderStorage().getSentFolder());
    }

    private boolean isFolderAccepted(String fullName, int accountId, Session session) {
        MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);

        Boolean b = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderChecked());
        if ((b == null) || !b.booleanValue()) {
            // Don't know better
            return true;
        }

        // Default folder were already checked; ensure client does not request authenticity for standard Drafts or Sent folder
        String[] arr = mailSessionCache.getParameter(accountId, MailSessionParameterNames.getParamDefaultFolderArray());
        String draftsFullName = arr == null ? null : arr[StorageUtility.INDEX_DRAFTS];
        if (null != draftsFullName && fullName.equals(draftsFullName)) {
            return false;
        }

        String sentFullName = arr == null ? null : arr[StorageUtility.INDEX_SENT];
        if (null != sentFullName && fullName.equals(sentFullName)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean accept(MailMessage[] mailsFromCache, MailFetchArguments fetchArguments, Session session) throws OXException {
        if (false == isAuthResultRequested(fetchArguments)) {
            // Not requested by client
            return true;
        }

        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            // Not enabled
            return true;
        }

        if (false == isAccountAccepted(fetchArguments.getFolder().getAccountId(), session)) {
            // Not applicable
            return true;
        }
        if (false == isFolderAccepted(fetchArguments.getFolder().getFullName(), fetchArguments.getFolder().getAccountId(), session)) {
            // Not applicable
            return true;
        }

        long threshold = handlerRegistry.getDateThreshold(session);
        for (MailMessage mail : mailsFromCache) {
            if (false == mail.hasAuthenticityResult() && (threshold <= 0 || mail.getReceivedDate().getTime() >= threshold)) {
                // Should hold an authenticity result
                return false;
            }
        }

        return true;
    }

    @Override
    public MailAttributation onBeforeFetch(MailFetchArguments fetchArguments, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException {
        if (false == isAuthResultRequested(fetchArguments)) {
            // Not requested by client
            return MailAttributation.NOT_APPLICABLE;
        }

        Session session = mailAccess.getSession();
        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            // Not enabled
            return MailAttributation.NOT_APPLICABLE;
        }

        if (isNotApplicableFor(fetchArguments, mailAccess)) {
            // Not applicable
            state.put(STATE_PARAM_HANDLER, NotAnalyzedAuthenticityHandler.getInstance());
            return MailAttributation.builder(fetchArguments.getFields(), fetchArguments.getHeaderNames()).build();
        }

        MailFields fields = null == fetchArguments.getFields() ? new MailFields() : new MailFields(fetchArguments.getFields());
        fields.add(MailField.ID);
        fields.add(MailField.FOLDER_ID); // For folder verification
        fields.add(MailField.RECEIVED_DATE); // For date threshold
        fields.add(MailField.ACCOUNT_NAME); // For account verification
        Set<String> headerNames = null == fetchArguments.getHeaderNames() ? null : new LinkedHashSet<>(Arrays.asList(fetchArguments.getHeaderNames()));
        {
            Collection<MailField> requiredFields = handler.getRequiredFields();
            if (null != requiredFields && !requiredFields.isEmpty()) {
                for (MailField requiredField : requiredFields) {
                    fields.add(requiredField);
                }
            }
            Collection<String> requiredHeaders = handler.getRequiredHeaders();
            if (null != requiredHeaders && !requiredHeaders.isEmpty()) {
                if (null == headerNames) {
                    headerNames = new LinkedHashSet<>();
                }
                for (String requiredHeader : requiredHeaders) {
                    headerNames.add(requiredHeader);
                }
            }
        }

        state.put(STATE_PARAM_HANDLER, handler);
        return MailAttributation.builder(fields.isEmpty() ? null : fields.toArray(), null == headerNames ? null : headerNames.toArray(new String[headerNames.size()])).build();
    }

    @Override
    public MailFetchListenerResult onAfterFetch(MailMessage[] mails, boolean cacheable, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, Map<String, Object> state) throws OXException {
        if (null == mails || mails.length == 0) {
            return MailFetchListenerResult.neutral(mails, cacheable);
        }

        MailAuthenticityHandler handler = (MailAuthenticityHandler) state.get(STATE_PARAM_HANDLER);
        if (null == handler) {
            return MailFetchListenerResult.neutral(mails, cacheable);
        }

        if (handler instanceof NotAnalyzedAuthenticityHandler) {
            for (final MailMessage mail : mails) {
                if (mail != null) {
                    mail.setAuthenticityResult(MailAuthenticityResult.NOT_ANALYZED_RESULT);
                }
            }
            return MailFetchListenerResult.neutral(mails, cacheable);
        }

        Session session = mailAccess.getSession();
        FolderChecker folderChecker = new DenyIfContainedFolderChecker(mailAccess.getFolderStorage().getDraftsFolder(), mailAccess.getFolderStorage().getSentFolder());
        List<MailMessage[]> partitions = com.openexchange.tools.arrays.Arrays.partition(mails, 100);
        Map<Future<Void>, MailAuthenticityTask> submittedTasks = new LinkedHashMap<>(partitions.size());
        for (MailMessage[] partition : partitions) {
            MailAuthenticityTask task = new MailAuthenticityTask(partition, handler, session, folderChecker);
            Future<Void> future = threadPool.submit(task);
            submittedTasks.put(future, task);
        }
        partitions = null; // Help GC

        try {
            for (Map.Entry<Future<Void>, MailAuthenticityTask> submitted : submittedTasks.entrySet()) {
                Future<Void> future = submitted.getKey();
                MailAuthenticityTask task = submitted.getValue();
                long timeoutMillis = getTimeoutMillis(task.getNumberOfMails());
                try {
                    future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                    LOGGER.debug("Verified mail authenticity for mails \"{}\" ({}) in folder {} after {}\u00b5s",
                        task.getMailIds(), I(task.getNumberOfMails()), task.getMailFolder(), L(task.getDurationMicros()));
                } catch (ExecutionException e) {
                    LOGGER.warn("Error while verifying mail authenticity for mails \"{}\" ({}) in folder {} after {}\u00b5s",
                        task.getMailIds(), I(task.getNumberOfMails()), task.getMailFolder(), L(task.getDurationMicros()), e.getCause());
                    task.markAsNeutral();
                } catch (TimeoutException e) {
                    task.interrupt();
                    future.cancel(true);
                    long durationMicros = task.getDurationMicros();
                    if (durationMicros == 0) {
                        // Task not yet in execution; still enqueued in thread pool
                        LOGGER.warn("Timeout while verifying mail authenticity for mails \"{}\" ({}) in folder {} after {}ms. Task has not been executed.",
                            task.getMailIds(), I(task.getNumberOfMails()), task.getMailFolder(), L(timeoutMillis));
                    } else {
                        LOGGER.warn("Timeout while verifying mail authenticity for mails \"{}\" ({}) in folder {} after {}ms",
                            task.getMailIds(), I(task.getNumberOfMails()), task.getMailFolder(), L(timeoutMillis));
                    }
                    task.markAsNeutral();
                }
            }
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();

            // Cancel submitted tasks in reverse order
            List<Map.Entry<Future<Void>, MailAuthenticityTask>> pending = new ArrayList<>(submittedTasks.size());
            for (Map.Entry<Future<Void>, MailAuthenticityTask> submitted : submittedTasks.entrySet()) {
                pending.add(submitted);
            }
            for (int i = pending.size(); i-- > 0;) {
                Map.Entry<Future<Void>, MailAuthenticityTask> submitted = pending.get(i);
                submitted.getValue().interrupt();
                submitted.getKey().cancel(true);
            }

            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
        return MailFetchListenerResult.neutral(mails, cacheable);
    }

    @Override
    public MailMessage onSingleMailFetch(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        Session session = mailAccess.getSession();
        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            // Not enabled
            return mail;
        }

        if (false == isApplicableFor(mail, mailAccess)) {
            // Not applicable
            mail.setAuthenticityResult(MailAuthenticityResult.NOT_ANALYZED_RESULT);
            return mail;
        }

        MailAuthenticityTask task = new MailAuthenticityTask(mail, handler, session, ALWAYS_ACCEPT_FOLDER_CHECKER);
        Future<Void> future = threadPool.submit(task);
        long timeoutMillis = getTimeoutMillis(1);
        try {
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            LOGGER.debug("Verified mail authenticity for mail \"{}\" in folder {} after {}\u00b5s", mail.getMailId(), mail.getFolder(), Long.valueOf(task.getDurationMicros()));
        } catch (ExecutionException e) {
            LOGGER.warn("Error while verifying mail authenticity for mail \"{}\" in folder {} after {}\u00b5s", mail.getMailId(), mail.getFolder(), Long.valueOf(task.getDurationMicros()), e.getCause());
            mail.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
        } catch (TimeoutException e) {
            task.interrupt();
            future.cancel(true);
            long durationMicros = task.getDurationMicros();
            if (durationMicros == 0) {
                // Task not yet in execution; still enqueued in thread pool
                LOGGER.warn("Timeout while verifying mail authenticity for mail \"{}\" in folder {} after {}ms. Task has not been executed.", mail.getMailId(), mail.getFolder(), Long.valueOf(timeoutMillis));
            } else {
                LOGGER.warn("Timeout while verifying mail authenticity for mail \"{}\" in folder {} after {}ms", mail.getMailId(), mail.getFolder(), Long.valueOf(timeoutMillis));
            }
            mail.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            task.interrupt();
            future.cancel(true);
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }

        return mail;
    }

    /**
     * Gets the timeout in milliseconds for <code>num</code> mails.
     *
     * @param num Number of mails
     * @return The milliseconds
     */
    private long getTimeoutMillis(int num) {
        long min = Math.max(5000L, num * 100L);
        return Math.min(min, 30000L);
    }

    // --------------------------------------------------------------------------------------------------------------

    private static class MailAuthenticityTask extends AbstractTrackableTask<Void> {

        private final MailMessage[] mails;
        private final MailAuthenticityHandler handler;
        private final Session session;
        private final FolderChecker folderChecker;
        private final AtomicLong durationMicros;
        private final AtomicReference<Thread> runnerReference;

        MailAuthenticityTask(MailMessage mail, MailAuthenticityHandler handler, Session session, FolderChecker folderChecker) {
            this(new MailMessage[] { mail }, handler, session, folderChecker);
        }

        MailAuthenticityTask(MailMessage[] mails, MailAuthenticityHandler handler, Session session, FolderChecker folderChecker) {
            super();
            this.mails = mails;
            this.handler = handler;
            this.session = session;
            this.folderChecker = folderChecker;
            durationMicros = new AtomicLong(0);
            runnerReference = new AtomicReference<>(null);
        }

        /**
         * Gets the duration of this task in micro seconds.
         * <p>
         * Should only be called when this task has been completed.
         *
         * @return The duration or <code>0</code> if not yet in execution
         */
        public long getDurationMicros() {
            return durationMicros.get();
        }

        /**
         * Gets the number of mails processed by this task
         *
         * @return The number of mails
         */
        public int getNumberOfMails() {
            return mails.length;
        }

        /**
         * Gets an object whose <code>toString()</code> method provides the comma-separated string containing the identifiers of the mails
         * processed by this task.
         * <p>
         * This method is for logging reasons.
         *
         * @return An object providing the mail identifiers as a string
         */
        public Object getMailIds() {
            MailMessage[] mails = this.mails;
            return new Object() {

                @Override
                public String toString() {
                    StringBuilder sb = new StringBuilder(mails.length << 2);
                    boolean first = true;
                    for (MailMessage mail : mails) {
                        if (null != mail) {
                            if (first) {
                                first = false;
                            } else {
                                sb.append(", ");
                            }
                            sb.append(mail.getMailId());
                        }
                    }
                    return sb.toString();
                }
            };
        }

        /**
         * Gets the identifier of the mail folder
         *
         * @return The mail folder, or <code>null</code> if there are no mails
         */
        public String getMailFolder() {
            return mails.length == 0 || mails[0] == null ? null : mails[0].getFolder();
        }

        /**
         * Marks the mails processed by this task as <code>neutral</code> by applying {@link MailAuthenticityResult#NEUTRAL_RESULT the
         * special authenticity result} to all of them.
         */
        public void markAsNeutral() {
            for (MailMessage mail : mails) {
                if (null != mail) {
                    mail.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
                }
            }
        }

        @Override
        public void beforeExecute(Thread thread) {
            durationMicros.set(System.nanoTime());
            super.beforeExecute(thread);
        }

        @Override
        public void afterExecute(Throwable throwable) {
            long taskStartNanos = durationMicros.get();
            durationMicros.set(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - taskStartNanos));
            super.afterExecute(throwable);
        }

        /**
         * Interrupts the thread (if any) currently executing this task.
         */
        public void interrupt() {
            Thread runner = runnerReference.get();
            if (runner != null) {
                runner.interrupt();
            }
        }

        @Override
        public Void call() {
            runnerReference.set(Thread.currentThread());
            try {
                // int unifiedINBOXAccountId = getUnifiedINBOXAccountId(session);
                // Handle first mail
                {
                    MailMessage mail = mails[0];
                    if (null != mail) {
                        verifyAuthenticityFrom(mail);
                    }
                }

                // Handle rest while paying respect to processing thread's interrupted status
                if (mails.length > 1) {
                    Thread t = Thread.currentThread();
                    for (int i = 1, length = mails.length; i < length; i++) {
                        MailMessage mail = mails[i];
                        if (null != mail) {
                            if (t.isInterrupted()) {
                                // Processing thread was interrupted
                                return null;
                            }
                            verifyAuthenticityFrom(mail);
                        }
                    }
                }
                return null;
            } finally {
                runnerReference.set(null);
            }
        }

        private void verifyAuthenticityFrom(MailMessage mail) {
            // Check account
            int accId = mail.getAccountId();
            /*-
            if (mail instanceof Delegatized) {
                int undelegatedAccountId = ((Delegatized) mail).getUndelegatedAccountId();
                if (undelegatedAccountId >= 0) {
                    accId = undelegatedAccountId;
                }
            }
            */
            if ((accId != MailAccount.DEFAULT_ID /* && accId != unifiedINBOXAccountId */) || (false == folderChecker.isFolderAcceptedFrom(mail))) {
                // Not located in primary account or located in a denied folder
                mail.setAuthenticityResult(MailAuthenticityResult.NOT_ANALYZED_RESULT);
            } else {
                // Verify mail authenticity...
                try {
                    handler.handle(session, mail);
                } catch (Exception e) {
                    // Verifying mail authenticity failed
                    LOGGER.warn("Error while verifying mail authenticity for mail \"{}\" in folder {}", mail.getMailId(), mail.getFolder(), e);
                    mail.setAuthenticityResult(MailAuthenticityResult.NEUTRAL_RESULT);
                }
            }
        }

    }

    private static interface FolderChecker {

        boolean isFolderAcceptedFrom(MailMessage mail);
    }

    private static final FolderChecker ALWAYS_ACCEPT_FOLDER_CHECKER = new FolderChecker() {

        @Override
        public boolean isFolderAcceptedFrom(MailMessage mail) {
            return true;
        }
    };

    private static class DenyIfContainedFolderChecker implements FolderChecker {

        private final Set<String> foldersToDeny;

        DenyIfContainedFolderChecker(String... foldersToDeny) {
            super();
            this.foldersToDeny = null == foldersToDeny || foldersToDeny.length <= 0 ? Collections.emptySet() : ImmutableSet.copyOf(foldersToDeny);
        }

        @Override
        public boolean isFolderAcceptedFrom(MailMessage mail) {
            String folder = mail.getFolder();
            return folder == null || !foldersToDeny.contains(folder);
        }
    }

}
