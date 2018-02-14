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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.authenticity.impl.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import com.openexchange.mail.authenticity.impl.osgi.Services;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link MailAuthenticityFetchListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityFetchListener implements MailFetchListener {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailAuthenticityFetchListener.class);

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

    private boolean isNotApplicableFor(MailFetchArguments fetchArguments, Session session) throws OXException {
        return false == isApplicableFor(fetchArguments, session);
    }

    private boolean isApplicableFor(MailFetchArguments fetchArguments, Session session) throws OXException {
        FullnameArgument folder = fetchArguments.getFolder();
        return null != folder && isAuthResultRequested(fetchArguments) && isAcceptableAccount(folder.getAccountId(), session);
    }

    private boolean isAcceptableAccount(int accountId, Session session) throws OXException {
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

    private boolean isFolderAccepted(String fullName, int accountId, Session session) throws OXException {
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
        if (isNotApplicableFor(fetchArguments, session)) {
            return true;
        }

        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            return true;
        }

        if (false == isFolderAccepted(fetchArguments.getFolder().getFullName(), fetchArguments.getFolder().getAccountId(), session)) {
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
        Session session = mailAccess.getSession();
        if (isNotApplicableFor(fetchArguments, session)) {
            // Special field not contained
            return MailAttributation.NOT_APPLICABLE;
        }

        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            return MailAttributation.NOT_APPLICABLE;
        }

        if (false == isFolderAccepted(fetchArguments.getFolder().getFullName(), mailAccess)) {
            return MailAttributation.NOT_APPLICABLE;
        }

        MailFields fields = null == fetchArguments.getFields() ? new MailFields() : new MailFields(fetchArguments.getFields());
        fields.add(MailField.ID);
        fields.add(MailField.FOLDER_ID);
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

        // int unifiedINBOXAccountId = getUnifiedINBOXAccountId(session);
        Session session = mailAccess.getSession();
        List<FutureAndMail> submittedTasks = new ArrayList<>(mails.length);
        for (final MailMessage mail : mails) {
            if (mail != null) {
                int accId = mail.getAccountId();
                /*-
                if (mail instanceof Delegatized) {
                    int undelegatedAccountId = ((Delegatized) mail).getUndelegatedAccountId();
                    if (undelegatedAccountId >= 0) {
                        accId = undelegatedAccountId;
                    }
                }
                */
                if (accId == MailAccount.DEFAULT_ID /* || accId == unifiedINBOXAccountId */) {
                    Future<Void> future = threadPool.submit(new MailAuthenticityTask(mail, handler, session));
                    submittedTasks.add(new FutureAndMail(future, mail));
                }
            }
        }
        try {
            for (FutureAndMail submitted : submittedTasks) {
                try {
                    submitted.future.get(5, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    MailMessage mail = submitted.mail;
                    LOGGER.warn("Error while verifying mail authenticity for mail {} in folder {}", mail.getMailId(), mail.getFolder(), e.getCause());
                } catch (TimeoutException e) {
                    submitted.future.cancel(true);
                    MailMessage mail = submitted.mail;
                    LOGGER.warn("Timeout while verifying mail authenticity for mail {} in folder {}", mail.getMailId(), mail.getFolder());
                }
            }
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
        return MailFetchListenerResult.neutral(mails, cacheable);
    }

    @Override
    public MailMessage onSingleMailFetch(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        Session session = mailAccess.getSession();
        if (false == isAcceptableAccount(mail.getAccountId(), session)) {
            return mail;
        }

        MailAuthenticityHandler handler = handlerRegistry.getHighestRankedHandlerFor(session);
        if (null == handler) {
            return mail;
        }

        if (false == isApplicableFor(mail, mailAccess)) {
            mail.setAuthenticityResult(MailAuthenticityResult.NOT_ANALYZED_RESULT);
            return mail;
        }

        Future<Void> future = threadPool.submit(new MailAuthenticityTask(mail, handler, session));
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            LOGGER.warn("Error while verifying mail authenticity for mail {} in folder {}", mail.getMailId(), mail.getFolder(), e.getCause());
        } catch (TimeoutException e) {
            future.cancel(true);
            LOGGER.warn("Timeout while verifying mail authenticity for mail {} in folder {}", mail.getMailId(), mail.getFolder());
        } catch (InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }

        return mail;
    }

    /**
     * Determines whether the specified {@link MailMessage} is applicable for mail authenticity
     *
     * @param mail The {@link MailMessage}
     * @param mailAccess The {@link MailAccess}
     * @return <code>true</code> if the message is applicable for mail authenticity; <code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    private boolean isApplicableFor(MailMessage mail, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        if (false == isAcceptableAccount(mail.getAccountId(), mailAccess.getSession())) {
            return false;
        }

        if (false == isFolderAccepted(mail.getFolder(), mailAccess)) {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------------------------------------------

    private static class MailAuthenticityTask extends AbstractTask<Void> {

        private final MailMessage mail;
        private final MailAuthenticityHandler handler;
        private final Session session;

        MailAuthenticityTask(MailMessage mail, MailAuthenticityHandler handler, Session session) {
            super();
            this.mail = mail;
            this.handler = handler;
            this.session = session;
        }

        @Override
        public Void call() throws Exception {
            handler.handle(session, mail);
            return null;
        }
    }

    private static class FutureAndMail {

        final Future<Void> future;
        final MailMessage mail;

        FutureAndMail(Future<Void> future, MailMessage mail) {
            super();
            this.future = future;
            this.mail = mail;
        }
    }

}
