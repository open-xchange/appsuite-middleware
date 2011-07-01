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

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractPollingAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
// It's usually a good idea to have a super class for all actions. Here we can put stuff all actions have to do in common
// Or implement some control flow, that every invocation of every method has to go through anyway.
public abstract class AbstractPollingAction implements AJAXActionService {

	// The factory, which gives us access to the poll service, among other things
    protected PollingActionFactory factory;
    
    protected AbstractPollingAction(PollingActionFactory factory) {
        super();
        this.factory = factory;
    }

    // This is the method we have to implement as an AJAXActionService
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws AbstractOXException {
    	// We construct a PollingRequest. I've found it useful to wrap request data, along with the session and our factory into a single request object. Everything that has to do with parsing request parameters or bodies
    	// can be put neatly into this class.
    	PollingRequest req = new PollingRequest(request, session, factory);
    	// Call our concrete subclasses perorm method, with our custom request object
    	return perform(req);
    }

    // This is the method our subclasses must implement, where we put the concrete logic for an action.
    protected abstract AJAXRequestResult perform(PollingRequest req) throws AbstractOXException;

}
