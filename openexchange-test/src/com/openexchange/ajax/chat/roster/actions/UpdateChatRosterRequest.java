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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.chat.roster.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.chat.Presence;
import com.openexchange.chat.json.roster.RosterID;

/**
 * {@link UpdateChatRosterRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateChatRosterRequest extends AbstractChatRosterRequest<UpdateChatRosterResponse> {

    private RosterID rosterId;

    private Presence presence;

    /**
     * Initializes a new {@link UpdateChatRosterRequest}.
     */
    public UpdateChatRosterRequest() {
        super();
    }

    /**
     * Sets the presence
     *
     * @param presence The presence to set
     */
    public void setPresence(final Presence presence) {
        this.presence = presence;
    }

    /**
     * Sets the rosterId
     *
     * @param rosterId The rosterId to set
     */
    public void setRosterId(final RosterID rosterId) {
        this.rosterId = rosterId;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> params = new ArrayList<Parameter>(2);
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "update"));
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, rosterId.toString()));
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends UpdateChatRosterResponse> getParser() {
        return new AbstractAJAXParser<UpdateChatRosterResponse>(isFailOnError()) {

            @Override
            protected UpdateChatRosterResponse createResponse(final Response response) {
                return new UpdateChatRosterResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        final JSONObject json = new JSONObject();
        json.put("mode", presence.getMode().toString());
        json.put("status", presence.getStatus());
        return json;
    }

}
