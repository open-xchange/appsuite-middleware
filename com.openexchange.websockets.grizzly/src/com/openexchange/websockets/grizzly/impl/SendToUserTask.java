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

package com.openexchange.websockets.grizzly.impl;

import org.slf4j.Logger;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link SendToUserTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SendToUserTask extends AbstractTask<Void> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SendToUserTask.class);

    private final String message;
    private final String pathFilter;
    private final boolean remote;
    private final int userId;
    private final int contextId;
    private final DefaultGrizzlyWebSocketApplication application;

    /**
     * Initializes a new {@link SendToUserTask}.
     *
     * @param message The text message to send
     * @param pathFilter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param remote Whether the text message was remotely received; otherwise <code>false</code> for local origin
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param application The running application
     */
    public SendToUserTask(String message, String pathFilter, boolean remote, int userId, int contextId, DefaultGrizzlyWebSocketApplication application) {
        super();
        this.message = message;
        this.pathFilter = pathFilter;
        this.remote = remote;
        this.userId = userId;
        this.contextId = contextId;
        this.application = application;
    }

    @Override
    public Void call() throws Exception {
        try {
            application.sendToUser(message, pathFilter, remote, userId, contextId);
        } catch (Exception e) {
            LOG.error("Failed to send message to user {} in context {}", userId, contextId, e);
        }
        return null;
    }

}
