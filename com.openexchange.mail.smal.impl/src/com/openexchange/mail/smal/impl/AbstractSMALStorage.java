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

import static com.openexchange.mail.smal.impl.SmalServiceLookup.getServiceStatic;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.smal.impl.adapter.IndexAdapter;
import com.openexchange.mail.smal.impl.adapter.IndexService;
import com.openexchange.mail.smal.impl.processor.DefaultProcessorStrategy;
import com.openexchange.mail.smal.impl.processor.IProcessorStrategy;
import com.openexchange.mail.smal.impl.processor.MailFolderInfo;
import com.openexchange.mail.smal.impl.processor.ProcessingProgress;
import com.openexchange.mail.smal.impl.processor.Processor;
import com.openexchange.server.ServiceExceptionCodes;
import com.openexchange.service.indexing.IndexingJob;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.mail.MailJobInfo;
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

    /**
     * The logger.
     */
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
    protected final IProcessorStrategy processorStrategy;

    /**
     * The processor parameterized with <code>processorStrategy</code>.
     */
    protected final Processor processor;

    /**
     * The volatile job info reference.
     */
    private volatile MailJobInfo jobInfo;

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
        processor = new Processor(processorStrategy);
    }

    /**
     * Initiates processing of given folder.
     * 
     * @param fullName The folder full name
     * @return The processing progress
     * @throws OXException If folder retrieval fails
     * @throws InterruptedException If interrupted
     */
    protected ProcessingProgress processFolder(final String fullName) throws OXException, InterruptedException {
        return processFolder(delegateMailAccess.getFolderStorage().getFolder(fullName));
    }

    /**
     * Initiates processing of given folder.
     * 
     * @param mailFolder The folder to process
     * @return The processing progress
     * @throws InterruptedException If interrupted
     */
    protected ProcessingProgress processFolder(final MailFolder mailFolder) throws InterruptedException {
        return processor.processFolder(mailFolder, accountId, session, Collections.<String, Object> emptyMap());
    }

    /**
     * Initiates processing of given folder.
     * 
     * @param mailFolderInfo The information of the folder to process
     * @return The processing progress
     * @throws InterruptedException If interrupted
     */
    protected ProcessingProgress processFolder(final MailFolderInfo mailFolderInfo) throws InterruptedException {
        return processor.processFolder(mailFolderInfo, accountId, session, Collections.<String, Object> emptyMap());
    }

    /**
     * Submits specified job.
     * 
     * @param job The job
     * @return <code>true</code> if successfully submitted; otherwise <code>false</code>
     */
    protected boolean submitJob(final IndexingJob job) {
        try {
            final IndexingService service = SmalServiceLookup.getServiceStatic(IndexingService.class);
            if (null == service) {
                return false;
            }
            service.addJob(job);
            return true;
        } catch (final OXException e) {
            return false;
        }
    }

    /**
     * Creates the appropriate job info for this storage access.
     * 
     * @return The job info
     * @throws OXException If creation fails
     */
    protected MailJobInfo createJobInfo() throws OXException {
        MailJobInfo jobInfo = this.jobInfo;
        if (null == jobInfo) {
            synchronized (this) {
                jobInfo = this.jobInfo;
                if (null == jobInfo) {
                    final MailConfig config = delegateMailAccess.getMailConfig();
                    jobInfo =
                        new MailJobInfo.Builder(userId, contextId).accountId(accountId).login(config.getLogin()).password(
                            config.getPassword()).port(config.getPort()).server(config.getServer()).secure(config.isSecure()).primaryPassword(
                            session.getPassword()).build();
                    this.jobInfo = jobInfo;
                }
            }
        }
        return jobInfo;
    }

    /**
     * Gets the available index adapter.
     * 
     * @return The index adapter
     * @deprecated Use SmalServiceLookup.getServiceStatic(IndexFacadeService.class)
     */
    @Deprecated
    protected static IndexAdapter getIndexAdapter() {
        final IndexService indexService = getServiceStatic(IndexService.class);
        return null == indexService ? null : indexService.getAdapter();
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
            throw ServiceExceptionCodes.SERVICE_UNAVAILABLE.create(ThreadPoolService.class.getName());
        }
        return new ThreadPoolCompletionService<V>(threadPool);
    }

}
