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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.tasks.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tasks.json.actions.AllAction;
import com.openexchange.tasks.json.actions.ConfirmAction;
import com.openexchange.tasks.json.actions.CopyAction;
import com.openexchange.tasks.json.actions.DeleteAction;
import com.openexchange.tasks.json.actions.GetAction;
import com.openexchange.tasks.json.actions.ListAction;
import com.openexchange.tasks.json.actions.NewAction;
import com.openexchange.tasks.json.actions.SearchAction;
import com.openexchange.tasks.json.actions.TaskAction;
import com.openexchange.tasks.json.actions.UpdateAction;
import com.openexchange.tasks.json.actions.UpdatesAction;


/**
 * {@link TaskActionFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TaskActionFactory implements AJAXActionServiceFactory {
    
    private static final Map<String, TaskAction> ACTIONS = new ConcurrentHashMap<String, TaskAction>(10);

    /**
     * Initializes a new {@link TaskActionFactory}.
     */
    public TaskActionFactory(final ServiceLookup serviceLookup) {
        super();
        ACTIONS.put("all", new AllAction(serviceLookup));
        ACTIONS.put("confirm", new ConfirmAction(serviceLookup));
        ACTIONS.put("copy", new CopyAction(serviceLookup));
        ACTIONS.put("delete", new DeleteAction(serviceLookup));
        ACTIONS.put("get", new GetAction(serviceLookup));
        ACTIONS.put("list", new ListAction(serviceLookup));
        ACTIONS.put("new", new NewAction(serviceLookup));
        ACTIONS.put("search", new SearchAction(serviceLookup));
        ACTIONS.put("update", new UpdateAction(serviceLookup));
        ACTIONS.put("updates", new UpdatesAction(serviceLookup));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return ACTIONS.get(action);
    }

}
