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

package com.openexchange.mail.compose.json.action;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AllMailComposeAction} - The action serving an <code>"action=all"</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AllMailComposeAction extends AbstractMailComposeAction {

    /**
     * Initializes a new {@link AllMailComposeAction}.
     *
     * @param services The service look-up
     */
    public AllMailComposeAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        CompositionSpaceService compositionSpaceService = getCompositionSpaceService();

        MessageField[] fields;
        String columns = requestData.getParameter("columns");
        boolean hasColumns = Strings.isNotEmpty(columns);
        if (hasColumns) {
            String[] sFields = Strings.splitByComma(columns);
            fields = new MessageField[sFields.length];
            for (int i =  sFields.length; i-- > 0;) {
                fields[i] = MessageField.messageFieldFor(sFields[i]);
            }
        } else {
            fields = null;
        }
        columns = null;

        List<CompositionSpace> compositionSpaces = compositionSpaceService.getCompositionSpaces(fields, session);
        int size = compositionSpaces.size();
        if (size <= 0) {
            return new AJAXRequestResult(JSONArray.EMPTY_ARRAY, "json");
        }

        if (hasColumns) {
            AJAXRequestResult requestResult = new AJAXRequestResult(compositionSpaces, "compositionSpace");
            if (hasColumns) {
                requestResult.setParameter("columns", EnumSet.copyOf(Arrays.asList(fields)));
            }
            return requestResult;
        }

        JSONArray jIds = new JSONArray(size);
        for (CompositionSpace compositionSpace : compositionSpaces) {
            jIds.put(new JSONObject(2).put("id", UUIDs.getUnformattedString(compositionSpace.getId())));
        }
        return new AJAXRequestResult(jIds, "json");
    }

}
