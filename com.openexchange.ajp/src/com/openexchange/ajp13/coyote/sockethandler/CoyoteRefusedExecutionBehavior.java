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

package com.openexchange.ajp13.coyote.sockethandler;

import java.util.concurrent.RejectedExecutionException;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajp13.coyote.ActionCode;
import com.openexchange.ajp13.coyote.AjpProcessor;
import com.openexchange.ajp13.watcher.AJPv13TaskWatcher;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link CoyoteRefusedExecutionBehavior} - The behavior for rejected AJP tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class CoyoteRefusedExecutionBehavior implements RefusedExecutionBehavior<Object> {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CoyoteRefusedExecutionBehavior.class));

    private final AJPv13TaskWatcher watcher;

    /**
     * Initializes a new {@link CoyoteRefusedExecutionBehavior}.
     *
     * @param watcher The task watcher to remove excess tasks from
     */
    public CoyoteRefusedExecutionBehavior(final AJPv13TaskWatcher watcher) {
        super();
        this.watcher = watcher;
    }

    @Override
    public Object refusedExecution(final Task<Object> task, final ThreadPoolService threadPoolService) {
        if (threadPoolService.isShutdown()) {
            // Proper logging
            final RejectedExecutionException e = new RejectedExecutionException("Thread pool is shutted down");
            LOG.error("AJP task cannot be executed since thread pool has been shut down. Please restart AJP module.", e);
            throw e;
        }
        // Still running, but task rejected (by synchronous queue if used)
        if (!(task instanceof CoyoteTask)) {
            // Huh..?! No AJP task? Then run in calling thread
            try {
                return task.call();
            } catch (final Exception e) {
                throw new RejectedExecutionException(e);
            }
        }
        // Uncomment this to run in calling thread.
        // r.run();
        final CoyoteTask rejectedTask = (CoyoteTask) task;
        try {
            final AjpProcessor ajpProcessor = rejectedTask.getAjpProcessor();
            ajpProcessor.getResponse().setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            ajpProcessor.action(ActionCode.CLIENT_FLUSH, null);
            ajpProcessor.action(ActionCode.CLOSE, null);
        } finally {
            // TODO:
            // watcher.removeTask(rejectedTask);
        }
        return null;
    }

}
