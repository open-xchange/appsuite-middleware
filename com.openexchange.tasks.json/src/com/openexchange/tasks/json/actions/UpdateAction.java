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

package com.openexchange.tasks.json.actions;

import java.util.Date;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.TaskParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.tasks.json.TaskRequest;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update a tasks.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Folder Identifier through that the task is accessed. This is necessary for checking the permissions."),
    @Parameter(name = "id", description = "Object ID of the updated task."),
    @Parameter(name = "timestamp", description = "Timestamp of the updated task. If the task was modified after the specified timestamp, then the update must fail.")
}, requestBody = "Task object as described in Common object data, Detailed task and appointment data and Detailed task data. Only modified fields are present. ",
responseDescription = "Nothing, except the standard response object with empty data, the timestamp of the updated task, and maybe errors.")
@OAuthAction(TaskActionFactory.OAUTH_WRITE_SCOPE)
public class UpdateAction extends TaskAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateAction.class);

    /**
     * Initializes a new {@link UpdateAction}.
     * @param services
     */
    public UpdateAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final TaskRequest req) throws OXException {
        final int id = req.checkInt(AJAXServlet.PARAMETER_ID);
        final int inFolder = req.checkInt(AJAXServlet.PARAMETER_INFOLDER);
        Date timestamp = req.checkDate(AJAXServlet.PARAMETER_TIMESTAMP);

        final Task task = new Task();

        final JSONObject jsonobject = (JSONObject) req.getRequest().requireData();
        ServerSession session = req.getSession();
        final TaskParser taskParser = new TaskParser(req.getTimeZone());
        taskParser.parse(task, jsonobject, session.getUser().getLocale());

        task.setObjectID(id);

        convertExternalToInternalUsersIfPossible(task, session.getContext(), LOG);

        final TasksSQLInterface sqlinterface = new TasksSQLImpl(session);
        sqlinterface.updateTaskObject(task, inFolder, timestamp);
        countObjectUse(session, task);
        timestamp = task.getLastModified();

        return new AJAXRequestResult(new JSONObject(0), timestamp, "json");
    }

}
