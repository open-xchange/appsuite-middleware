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
