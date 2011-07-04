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

package com.openexchange.mailaccount.json.multiple;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mailaccount.json.actions.MailAccountActionFactory;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailAccountMultipleHandler} - The multiple handler for mail account module.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountMultipleHandler implements MultipleHandler {

    private AJAXRequestResult result;
    
    /**
     * Initializes a new {@link MailAccountMultipleHandler}.
     */
    public MailAccountMultipleHandler() {
        super();
    }

    public Object performRequest(final String action, final JSONObject jsonObject, final ServerSession session, final boolean secure) throws AbstractOXException, JSONException {
        final AJAXActionService actionService = MailAccountActionFactory.getInstance().createActionService(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UnknownAction.create( action);
        }
        final AJAXRequestData request = new AJAXRequestData();
        request.setSecure(secure);
        for (final Entry<String, Object> entry : jsonObject.entrySet()) {
            final String key = entry.getKey();
            if (DATA.equals(key)) {
                request.setData(entry.getValue());
            } else {
                request.putParameter(key, entry.getValue().toString());
            }
        }
        result = actionService.perform(request, session);
        return result.getResultObject();
    }

    public Date getTimestamp() {
        if (null == result) {
            return null;
        }
        final Date timestamp = result.getTimestamp();
        return null == timestamp ? null : new Date(timestamp.getTime());
    }

    public void close() {
        result = null;
    }

    public Collection<AbstractOXException> getWarnings() {
        if (null == result) {
            return Collections.<AbstractOXException> emptySet();
        }
        return result.getWarnings();
    }

}
