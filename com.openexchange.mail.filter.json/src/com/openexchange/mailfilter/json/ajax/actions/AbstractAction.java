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
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAction<T, U extends AbstractRequest> {

    /**
     * Default constructor.
     */
    public AbstractAction() {
        super();
    }

    public Object action(final U request) throws OXException {
        Object retval;
        switch (request.getAction()) {
            case CONFIG:
                retval = actionConfig(request);
                break;
            case NEW:
                retval = Integer.valueOf(actionNew(request));
                break;
            case REORDER:
                actionReorder(request);
                retval = JSONObject.NULL;
                break;
            case UPDATE:
                actionUpdate(request);
                retval = JSONObject.NULL;
                break;
            case DELETE:
                actionDelete(request);
                retval = JSONObject.NULL;
                break;
            case LIST:
                retval = actionList(request);
                break;
            case DELETESCRIPT:
                actionDeleteScript(request);
                retval = JSONObject.NULL;
                break;
            case GETSCRIPT:
                retval = actionGetScript(request);
                break;
            default:
                throw MailFilterExceptionCode.PROBLEM.create("Unimplemented action.");
        }
        return retval;
    }

    protected JSONArray createAllArray(final int[] ids) {
        final JSONArray array = new JSONArray();
        for (final int id : ids) {
            final JSONArray user = new JSONArray();
            user.put(id);
            array.put(user);
        }
        return array;
    }

    protected JSONObject actionConfig(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.CONFIG.getAjaxName());
    }

    protected int actionNew(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.NEW.getAjaxName());
    }

    protected void actionReorder(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.NEW.getAjaxName());
    }

    protected void actionUpdate(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.UPDATE.getAjaxName());
    }

    protected void actionDelete(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.DELETE.getAjaxName());
    }

    protected JSONArray actionList(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.LIST.getAjaxName());
    }

    protected void actionDeleteScript(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.NEW.getAjaxName());
    }

    protected String actionGetScript(final U request) throws OXException {
        throw AjaxExceptionCodes.UNKNOWN_ACTION.create(Action.NEW.getAjaxName());
    }

}
