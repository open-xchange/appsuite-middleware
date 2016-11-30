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

package com.openexchange.groupware.update.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.tools.exceptions.ExceptionUtils;

/**
 * The {@link #run()} method of this class is started in a separate thread for
 * the update process.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateProcess implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateProcess.class);

    private final int contextId;
    private final SchemaStore schemaStore = SchemaStore.getInstance();
    private final Queue<TaskInfo> failures;
    private final boolean throwExceptionOnFailure;

    /**
     * Initializes a new {@link UpdateProcess} w/o tracing failures.
     *
     * @param contextId The context identifier
     */
    public UpdateProcess(int contextId) {
        this(contextId, false, false);
    }

    /**
     * Initializes a new {@link UpdateProcess}.
     *
     * @param contextId The context identifier
     * @param traceFailures <code>true</code> to trace failures available via {@link #getFailures()}; otherwise <code>false</code>
     * @param throwExceptionOnFailure Whether to throw an exception if a task failed
     */
    public UpdateProcess(int contextId, boolean traceFailures, boolean throwExceptionOnFailure) {
        super();
        this.contextId = contextId;
        this.failures = traceFailures ? new ConcurrentLinkedQueue<TaskInfo>() : null;
        this.throwExceptionOnFailure = throwExceptionOnFailure;
    }

    /**
     * Gets the optional failures.
     *
     * @return The failures or <code>null</code>
     * @see #UpdateProcess(int, boolean)
     */
    public Queue<TaskInfo> getFailures() {
        return failures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            runUpdate();
        } catch (OXException e) {
            LOG.error("", e);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
        }
    }

    /**
     * Triggers an update run.
     *
     * @return <code>true</code> if context-associated schema was successfully updated; otherwise <code>false</code> if already up-to-date
     * @throws OXException If update attempt fails
     */
    public boolean runUpdate() throws OXException {
        // Load schema
        SchemaUpdateState state = schemaStore.getSchema(contextId);
        if (!UpdateTaskCollection.getInstance().needsUpdate(state)) {
            // Already been updated before by previous thread
            return false;
        }
        new UpdateExecutor(state, contextId, null).execute(failures, throwExceptionOnFailure);
        return true;
    }
}
