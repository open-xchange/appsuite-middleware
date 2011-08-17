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

package com.openexchange.mail.smal.jobqueue;

import com.openexchange.threadpool.ThreadRenamer;


/**
 * {@link MailAccountJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJob extends Job {

    private final int contextId;
    
    private final int userId;
    
    private final int accountId;

    

    /**
     * Initializes a new {@link MailAccountJob}.
     * @param accountId
     * @param userId
     * @param contextId
     */
    public MailAccountJob(final int accountId, final int userId, final int contextId) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;
    }

    /* (non-Javadoc)
     * @see com.openexchange.threadpool.Task#setThreadName(com.openexchange.threadpool.ThreadRenamer)
     */
    @Override
    public void setThreadName(final ThreadRenamer threadRenamer) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.threadpool.Task#beforeExecute(java.lang.Thread)
     */
    @Override
    public void beforeExecute(final Thread t) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.threadpool.Task#afterExecute(java.lang.Throwable)
     */
    @Override
    public void afterExecute(final Throwable t) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRanking() {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.mail.smal.jobqueue.Job#perform()
     */
    @Override
    public void perform() {
        // TODO Auto-generated method stub

    }

}
