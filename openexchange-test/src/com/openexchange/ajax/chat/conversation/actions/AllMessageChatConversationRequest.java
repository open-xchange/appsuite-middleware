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

package com.openexchange.ajax.chat.conversation.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.chat.json.conversation.ConversationID;


/**
 * {@link AllMessageChatConversationRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllMessageChatConversationRequest extends AbstractChatConversationRequest<AllMessageChatConversationResponse> {

    private ConversationID conversationId;
    private Date date;
    private TimeZone timeZone;

    /**
     * Initializes a new {@link AllMessageChatConversationRequest}.
     */
    public AllMessageChatConversationRequest() {
        super();
        setFailOnError(true);
    }

    /**
     * Sets the conversation identifier
     *
     * @param conversationId The conversation identifier to set
     */
    public void setConversationId(final ConversationID conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Sets the since time stamp
     *
     * @param date The time stamp
     * @param timeZoneId The time zone identifier
     */
    public void setSince(final Date date, final String timeZoneId) {
        this.date = date;
        this.timeZone = null == timeZoneId ? TimeZone.getTimeZone("UTC") : TimeZone.getTimeZone(timeZoneId);
    }

    /**
     * Sets the since time stamp
     *
     * @param date The time stamp
     * @param timeZone The time zone
     */
    public void setSince(final Date date, final TimeZone timeZone) {
        this.date = date;
        this.timeZone = timeZone;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> params = new ArrayList<Parameter>(3);
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "allMessages"));
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, conversationId.toString()));
        if (null != date) {
            params.add(new Parameter("since", addTimeZone2Date(date, timeZone).getTime()));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    private static Date addTimeZone2Date(final Date d, final TimeZone tz) {
        return addTimeZone2Date(d.getTime(), tz);
    }

    private static Date addTimeZone2Date(final long timeMillis, final TimeZone tz) {
        return new Date(timeMillis + tz.getOffset(timeMillis));
    }

    @Override
    public AbstractAJAXParser<? extends AllMessageChatConversationResponse> getParser() {
        return new AbstractAJAXParser<AllMessageChatConversationResponse>(isFailOnError()) {

            @Override
            protected AllMessageChatConversationResponse createResponse(final Response response) {
                return new AllMessageChatConversationResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
