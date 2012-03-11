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

package com.openexchange.mail.smal;

import static com.openexchange.mail.smal.SMALServiceLookup.getServiceStatic;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.mail.smal.adapter.IndexService;
import com.openexchange.server.ServiceExceptionCodes;
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
     * Initializes a new {@link AbstractSMALStorage}.
     */
    protected AbstractSMALStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> delegateMailAccess) {
        super();
        this.session = session;
        userId = session.getUserId();
        contextId = session.getContextId();
        this.accountId = accountId;
        this.delegateMailAccess = delegateMailAccess;
    }

    /**
     * Gets the available index adapter.
     *
     * @return The index adapter
     */
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
