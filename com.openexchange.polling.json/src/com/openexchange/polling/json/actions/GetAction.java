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

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.PollService;


/**
 * {@link GetAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
//This action implements the "get" call
//Note that we extend the AbstractPollingAction, which is responsible for constructing a nice PollingRequest
public class GetAction extends AbstractPollingAction {

    protected GetAction(PollingActionFactory factory) {
        super(factory);
    }
    
    // So this is what we do in this action. Note how little must be done in an action. While refactoring or creating new HTTP API interfaces
    // We're aiming for a very slim controller (that's what actions are in an Model-View-Controller architecture) much like this one. Most complicated
    // stuff should happen at the model level (meaning, mostly in the PollService or the Poll class). 
    @Override
    protected AJAXRequestResult perform(PollingRequest req) throws OXException {
        req.require("id");
        int cid = req.getContextId();
        int id = req.getId();
        
        PollService polls = factory.getPollService();
        
        Poll poll = polls.getPoll(id, cid);
        
        // We return a result of type 'poll' This is the type that is used when trying to convert this result into something the client understands
        // When you loo att the PollJSONConverter, it claims it can turn a poll into JSON. This, supplying a Poll Object and saying its format is 'pull' is the
        // other half of that equation.
        return new AJAXRequestResult(poll, "poll");
    }

}
