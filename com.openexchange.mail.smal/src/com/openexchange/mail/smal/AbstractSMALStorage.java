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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailAccessWatcher;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractSMALStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractSMALStorage {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AbstractSMALStorage.class));

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
     * Connects delegate mail access instance.
     *
     * @throws OXException If connect attempt fails
     */
    protected void connect() throws OXException {
        delegateMailAccess.connect(false);
    }

    /**
     * Connects delegate mail access instance.
     *
     * @param checkDefaultFolders Whether existence of default folder should be checked
     * @throws OXException If connect attempt fails
     */
    protected void connect(final boolean checkDefaultFolders) throws OXException {
        delegateMailAccess.connect(checkDefaultFolders);
    }

    /**
     * Closes delegate mail access.
     */
    protected void close() {
        if (!delegateMailAccess.isConnectedUnsafe()) {
            return;
        }
        boolean put = true;
        try {
            /*
             * Release all used, non-cachable resources
             */
            delegateMailAccess.invokeReleaseResources();
        } catch (final Throwable t) {
            /*
             * Dropping
             */
            LOG.error("Resources could not be properly released. Dropping mail connection for safety reasons", t);
            put = false;
        }
        try {
            /*
             * Cache connection if desired/possible anymore
             */
            if (put && delegateMailAccess.isCacheable() && SMALMailAccessCache.getInstance().putMailAccess(session, accountId, delegateMailAccess)) {
                /*
                 * Successfully cached: return
                 */
                MailAccessWatcher.removeMailAccess(delegateMailAccess);
                return;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        /*
         * Couldn't be put into cache
         */
        delegateMailAccess.close(false);
    }

    /**
     * Releases all used resources when closing parental {@link MailAccess}
     *
     * @throws OXException If resources cannot be released
     */
    public void releaseResources() throws OXException {
        delegateMailAccess.invokeReleaseResources();
    }

}
