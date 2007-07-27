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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.task;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.ajax.task.actions.AbstractTaskRequest;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.InsertRequest;
import com.openexchange.ajax.task.actions.InsertResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AllTest extends AbstractTaskTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AllTest.class);

    private TimeZone timeZone;
    
    /**
     * Default constructor.
     * @param name Name of this test.
     */
    public AllTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final AJAXSession session = getSession();
        timeZone = ConfigTools.getTimeZone(session.getConversation(), AJAXConfig
            .getProperty(AJAXConfig.Property.HOSTNAME), session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        timeZone = null;
        super.tearDown();
    }

    public void testAll() throws Throwable {
        final AbstractTaskRequest[] inserts = new AbstractTaskRequest[10];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(getPrivateTaskFolder());
            inserts[i] = new InsertRequest(task, timeZone);
        }
        final MultipleResponse mInsert = (MultipleResponse) Executor.execute(
            getSession(), new MultipleRequest(inserts));
        for (int i = 0; i < inserts.length; i++) {
            final InsertResponse ins = (InsertResponse) mInsert
                .getResponse(i);
            LOG.info(ins.getId());
        }
        // TODO Get for timestamp
        // TODO List

        final DeleteRequest[] deletes = new DeleteRequest[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            deletes[i] = new DeleteRequest(getPrivateTaskFolder(),
                ((InsertResponse) mInsert.getResponse(i)).getId(),
                new Date());
        }
        final MultipleResponse mDelete = (MultipleResponse) Executor.execute(
            getSession(), new MultipleRequest(deletes)); 
    }
}
