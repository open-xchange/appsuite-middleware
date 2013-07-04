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

package com.openexchange.mail.smal.impl;

import java.util.Collections;
import org.apache.commons.logging.Log;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexProperties;
import com.openexchange.index.solr.ModuleSet;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.smal.impl.index.AccountBlacklist;
import com.openexchange.mail.smal.impl.index.FakeSession;
import com.openexchange.mail.smal.impl.index.jobs.MailFolderJob;
import com.openexchange.mail.smal.impl.index.jobs.MailJobInfo;
import com.openexchange.mail.smal.impl.index.jobs.MailJobInfo.Builder;
import com.openexchange.mail.smal.impl.processor.DefaultProcessorStrategy;
import com.openexchange.mail.smal.impl.processor.DoNothingProcessor;
import com.openexchange.mail.smal.impl.processor.MailFolderInfo;
import com.openexchange.mail.smal.impl.processor.Processor;
import com.openexchange.mail.smal.impl.processor.ProcessorStrategy;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.JobInfo;
import com.openexchange.session.Session;
import com.openexchange.threadpool.CancelableCompletionService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link AbstractSMALStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractSMALStorage {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractSMALStorage.class));
    /**
     * The fields containing only the mail identifier.
     */
    protected static final MailField[] FIELDS_ID = new MailField[] { MailField.ID };

    /**
     * The fields containing only flags.
     */
    protected static final MailField[] FIELDS_FLAGS = new MailField[] { MailField.FLAGS };

    /**
     * The session.
     */
    protected final Session session;

    /**
     * The user identifier obtained from session.
     */
    protected final int userId;

    /**
     * The context identifier obtained from session.
     */
    protected final int contextId;

    /**
     * The account identifier.
     */
    protected final int accountId;

    /**
     * The delegate mail access.
     */
    protected final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess;

    /**
     * The processor strategy to use.
     */
    protected final ProcessorStrategy processorStrategy;

    /**
     * The processor parameterized with <code>processorStrategy</code>.
     */
    protected final Processor processor;

    /**
     * The volatile job info reference.
     */
    private volatile MailJobInfo jobInfo;

    /**
     * Whether denoted account is blacklisted.
     * <p>
     * See {@link #isBlacklisted()}
     */
    protected Boolean blacklisted;

    /**
     * Initializes a new {@link AbstractSMALStorage}.
     */
    protected AbstractSMALStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess) {
        super();
        this.session = session;
        userId = session.getUserId();
        contextId = session.getContextId();
        this.accountId = accountId;
        this.delegateMailAccess = delegateMailAccess;
        processorStrategy = DefaultProcessorStrategy.getInstance();
        // FIXME: revert
        processor = new DoNothingProcessor(processorStrategy);
//        processor = new Processor(processorStrategy);
    }

    /**
     * Checks if denoted account is blacklisted
     *
     * @return <code>true</code> if blacklisted; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean isBlacklisted() throws OXException {
        return accountId != 0 && AccountBlacklist.isServerBlacklisted(delegateMailAccess.getMailConfig().getServer());
//        if (null == blacklisted) {
//            final MailSessionCache sessionCache = MailSessionCache.getInstance(session);
//            final Object param = sessionCache.getParameter(accountId, "com.openexchange.mail.smal.isBlacklisted");
//            if (null == param) {
//                final ConfigView view = getConfigViewFactory().getView(userId, contextId);
//                final String blacklist = view.get("com.openexchange.mail.smal.blacklist", String.class);
//                blacklisted = Boolean.valueOf(contains(delegateMailAccess.getMailConfig().getServer(), blacklist));
//                sessionCache.putParameterIfAbsent(accountId, "com.openexchange.mail.smal.isBlacklisted", blacklisted);
//            } else {
//                try {
//                    blacklisted = (Boolean) param;
//                } catch (final ClassCastException e) {
//                    final ConfigView view = getConfigViewFactory().getView(userId, contextId);
//                    final String blacklist = view.get("com.openexchange.mail.smal.blacklist", String.class);
//                    blacklisted = Boolean.valueOf(contains(delegateMailAccess.getMailConfig().getServer(), blacklist));
//                    sessionCache.putParameterIfAbsent(accountId, "com.openexchange.mail.smal.isBlacklisted", blacklisted);
//                }
//            }
//        }
//        return blacklisted.booleanValue();
    }
//
//    private static final Pattern SPLIT_CSV = Pattern.compile("\\s*,\\s*");
//
//    private static boolean contains(final String host, final String blacklist) {
//        if (isEmpty(host) || isEmpty(blacklist)) {
//            return false;
//        }
//        for (final String blacklistedHost : SPLIT_CSV.split(blacklist, 0)) {
//            if (host.equals(blacklistedHost)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static boolean isEmpty(final String string) {
//        if (null == string) {
//            return true;
//        }
//        final int len = string.length();
//        boolean isWhitespace = true;
//        for (int i = 0; isWhitespace && i < len; i++) {
//            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
//        }
//        return isWhitespace;
//    }

    /**
     * Gets the <tt>IndexFacadeService</tt> service.
     *
     * @return The <tt>IndexFacadeService</tt> service or <code>null</code> if absent or disabled via configuration
     * @throws OXException If user configuration cannot be read
     */
    protected IndexFacadeService getIndexFacadeService() throws OXException {
        final IndexFacadeService facadeService = SmalServiceLookup.getServiceStatic(IndexFacadeService.class);
        return null == facadeService ? null : (isBlacklisted() ? null : facadeService);
    }

    /**
     * Gets the {@link ConfigViewFactory} service.
     *
     * @return The service
     */
    protected ConfigViewFactory getConfigViewFactory() {
        return SmalServiceLookup.getServiceStatic(ConfigViewFactory.class);
    }

    /**
     * Initiates processing of given folder.
     *
     * @param fullName The folder full name
     * @return The processing progress
     * @throws OXException If folder retrieval fails
     * @throws InterruptedException If interrupted
     */
    protected void processFolder(final String fullName) throws OXException, InterruptedException {
        final IMailFolderStorage folderStorage = delegateMailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            final IMailFolderStorageEnhanced storageEnhanced = (IMailFolderStorageEnhanced) folderStorage;
            processor.processFolderAsync(
              new MailFolderInfo(fullName, storageEnhanced.getTotalCounter(fullName)),
              accountId,
              session,
              Collections.<String, Object> emptyMap());

        }

        processFolder(folderStorage.getFolder(fullName));
    }

    /**
     * Initiates processing of given folder.
     *
     * @param mailFolder The folder to process
     * @return The processing progress
     * @throws OXException If processing fails
     * @throws InterruptedException If interrupted
     */
    protected void processFolder(final MailFolder mailFolder) throws OXException, InterruptedException {
        processor.processFolderAsync(mailFolder, delegateMailAccess, Collections.<String, Object> emptyMap());
    }

    /**
     * Initiates processing of given folder.
     *
     * @param mailFolderInfo The information of the folder to process
     * @return The processing progress
     * @throws OXException If processing fails
     * @throws InterruptedException If interrupted
     */
    protected void processFolder(final MailFolderInfo mailFolderInfo) throws OXException, InterruptedException {
        processor.processFolderAsync(mailFolderInfo, accountId, session, Collections.<String, Object> emptyMap());
    }

    protected boolean isIndexingAllowed() throws OXException {
        ConfigViewFactory config = SmalServiceLookup.getServiceStatic(ConfigViewFactory.class);
        ConfigView view = config.getView(userId, contextId);
        String moduleStr = view.get(IndexProperties.ALLOWED_MODULES, String.class);
        ModuleSet modules = new ModuleSet(moduleStr);
        return modules.containsModule(Types.EMAIL);
    }

    /**
     * Submits a folder job for the given full name.
     *
     * @param folder The folders full name.
     * @throws OXException
     */
    protected void submitFolderJob(String folder) throws OXException {
        if (session instanceof FakeSession) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Session is a fake session. Job will not be submitted...");
            }
            // FIXME: This is done to prevent loops here and needs a much better solution!
            return;
        }

        if (!isIndexingAllowed() || isBlacklisted()) {
            return;
        }

        MailConfig mailConfig = delegateMailAccess.getMailConfig();
        Builder builder = MailJobInfo.newBuilder(MailFolderJob.class)
            .login(mailConfig.getLogin())
            .accountId(accountId)
            .contextId(contextId)
            .userId(userId)
            .primaryPassword(session.getPassword())
            .password(mailConfig.getPassword())
            .folder(folder);

        JobInfo jobInfo = builder.build();
        IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
        indexingService.scheduleJob(true, jobInfo, null, -1L, IndexingService.DEFAULT_PRIORITY);
    }

    /**
     * Handles specified {@link RuntimeException} instance.
     *
     * @param e The runtime exception to handle
     * @return An appropriate {@link OXException}
     */
    protected OXException handleRuntimeException(final RuntimeException e) {
        return MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

    /**
     * Creates a new {@link ThreadPoolCompletionService completion service}.
     *
     * @return A new completion service.
     * @throws OXException If completion service cannot be created due to absent {@link ThreadPoolService service}
     */
    protected static <V> CancelableCompletionService<V> newCompletionService() throws OXException {
        final ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ThreadPoolService.class.getName());
        }
        return new ThreadPoolCompletionService<V>(threadPool);
    }

}
