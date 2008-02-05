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

package com.openexchange.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.webdav.OXServlet;
import com.openexchange.webdav.InfostorePerformer.Action;

public class Infostore extends OXServlet {

	private static final long serialVersionUID = -2064098724675986123L;

	
	public Infostore(){
		// Force Loading of InfostorePerformer
		InfostorePerformer.getInstance();
	}
	
	@Override
	protected void doCopy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.COPY);
	}

	@Override
	protected void doLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.LOCK);
	}

	@Override
	protected void doMkCol(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.MKCOL);
	}

	@Override
	protected void doMove(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.MOVE);
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.OPTIONS);
	}

	@Override
	protected void doPropFind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.PROPFIND);
	}

	@Override
	protected void doPropPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.PROPPATCH);
	}

	@Override
	protected void doUnLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp, Action.UNLOCK);
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp, Action.DELETE);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.GET);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.HEAD);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp, Action.PUT);
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doIt(req,resp,Action.TRACE);
	}

	private void doIt(HttpServletRequest req, HttpServletResponse resp, Action action) throws ServletException, IOException {
        final ServerSession session = getSession(req);
		final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext());
		if(!(uc.hasWebDAV() && uc.hasInfostore())){
			resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
		} else {
			InfostorePerformer.getInstance().doIt(req, resp, action, getSession(req));
		}
	}
	
}
