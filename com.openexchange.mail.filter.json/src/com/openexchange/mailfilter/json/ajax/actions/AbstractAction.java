/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailfilter.json.ajax.actions;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.json.ajax.Action;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractAction} - The abstract action for all mail filter actions
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @param <T> Response type
 * @param <U> Request type
 */
public abstract class AbstractAction<T, U extends AbstractRequest> {

    /**
     * Default constructor.
     */
    public AbstractAction() {
        super();
    }

    /**
     * Performs the action
     *
     * @param request The request
     * @return The response
     * @throws OXException if an error is occurred
     */
    public Object action(U request) throws OXException {
        switch (request.getAction()) {
            case CONFIG:
                return actionConfig(request);
            case NEW:
                return Integer.valueOf(actionNew(request));
            case REORDER:
                actionReorder(request);
                return JSONObject.NULL;
            case UPDATE:
                actionUpdate(request);
                return JSONObject.NULL;
            case DELETE:
                actionDelete(request);
                return JSONObject.NULL;
            case LIST:
                return actionList(request);
            case DELETESCRIPT:
                actionDeleteScript(request);
                return JSONObject.NULL;
            case GETSCRIPT:
                return actionGetScript(request);
            default:
                throw MailFilterExceptionCode.PROBLEM.create("Unimplemented action.");
        }
    }

    /**
     * Creates a {@link JSONArray} with the specified ids
     *
     * @param ids The ids
     * @return The {@link JSONArray}
     */
    protected JSONArray createAllArray(int[] ids) {
        JSONArray array = new JSONArray();
        for (int id : ids) {
            JSONArray user = new JSONArray();
            user.put(id);
            array.put(user);
        }
        return array;
    }

    /**
     * Performs the {@link Action#CONFIG}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected JSONObject actionConfig(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.CONFIG.getAjaxName());
    }

    /**
     * Performs the {@link Action#NEW}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected int actionNew(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.NEW.getAjaxName());
    }

    /**
     * Performs the {@link Action#NEW}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected void actionReorder(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.REORDER.getAjaxName());
    }

    /**
     * Performs the {@link Action#UPDATE}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected void actionUpdate(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.UPDATE.getAjaxName());
    }

    /**
     * Performs the {@link Action#DELETE}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected void actionDelete(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.DELETE.getAjaxName());
    }

    /**
     * Performs the {@link Action#LIST}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected JSONArray actionList(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.LIST.getAjaxName());
    }

    /**
     * Performs the {@link Action#DELETESCRIPT}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected void actionDeleteScript(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.DELETESCRIPT.getAjaxName());
    }

    /**
     * Performs the {@link Action#GETSCRIPT}
     * 
     * @param request The request
     * @return The response as {@link JSONObject}
     */
    protected String actionGetScript(U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.GETSCRIPT.getAjaxName());
    }

}
