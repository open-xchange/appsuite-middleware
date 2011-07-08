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

package com.openexchange.polling.json.actions;

import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.json.PollParser;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link PollingRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
// The PollingRequest wraps all request data that is usually handed to us by the framework.
// This is a good place to put everything that has to do with parameter handling and body parsing.
public class PollingRequest {

    private AJAXRequestData request;
    private ServerSession session;
    private PollingActionFactory factory;

    // Out neato PollParser. Turns a JSONObject into a Poll
    private static final PollParser PARSER = new PollParser();
    
    public PollingRequest(AJAXRequestData request, ServerSession session, PollingActionFactory factory) {
        this.request = request;
        this.session = session;
        this.factory = factory;
    }

    // Handle the body. This will typically be used by new and update actions.
    public Poll getPoll() throws OXException {
        JSONObject object = (JSONObject) request.getData();
        return PARSER.parse(object);
    }
    
    // The contextId usually comes from the session
    public int getContextId() {
        return session.getContextId();
    }

    // A helpful delegate method. Add delegate methods as needed by the action implementations.
    // ServerSession#getContext oder ServerSession#getUser are also popular for this.
    public void require(String... mandatoryParameters) throws OXException {
        request.require(mandatoryParameters);
    }

    // Parse the ID as an int
    // Every class for which we have a StringParser (which currently are all basic data types, Date and TimeSpan)
    public int getId() {
        return request.getParameter("id", int.class);
    }
    
    

}
