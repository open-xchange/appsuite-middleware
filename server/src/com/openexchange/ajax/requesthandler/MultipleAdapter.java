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

package com.openexchange.ajax.requesthandler;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultipleAdapter} maps the {@link MultipleHandler} to several {@link AJAXActionService}s. This class is not thread safe because it
 * has to remember the {@link AJAXRequestResult} between calling {@link #performRequest(String, JSONObject, ServerSession)} and
 * {@link #getTimestamp()} methods.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class MultipleAdapter implements MultipleHandler {

    private final AJAXActionServiceFactory factory;

    private AJAXRequestResult result;
  
    public MultipleAdapter(AJAXActionServiceFactory factory) {
        super();
        this.factory = factory;
    }

    public Object performRequest(String action, JSONObject jsonObject, ServerSession session) throws AbstractOXException, JSONException {
        AJAXActionService actionService = factory.createActionService(action);
        if (null == actionService) {
            throw new AjaxException(AjaxException.Code.UnknownAction, action);
        }
        AJAXRequestData request = new AJAXRequestData(jsonObject);
        result = actionService.perform(request, session);
        return result.getResultObject();
    }

    public Date getTimestamp() {
        return null == result ? null : result.getTimestamp();
    }

    public void close() {
        result = null;
    }
}
