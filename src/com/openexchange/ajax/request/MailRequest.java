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

package com.openexchange.ajax.request;

import java.io.Writer;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.api2.MailInterface;
import com.openexchange.groupware.imap.OXMailException;
import com.openexchange.groupware.imap.OXMailException.MailCode;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIteratorException;

public class MailRequest {

	private final SessionObject sessionObj;

	private final Writer pw;

	private static final Mail MAIL_SERVLET = new Mail();

	public MailRequest(final SessionObject sessionObj, final Writer pw) {
		super();
		this.sessionObj = sessionObj;
		this.pw = pw;
	}

	public void action(final String action, final JSONObject jsonObject, final MailInterface mailInterface)
			throws SearchIteratorException, JSONException, OXMailException {
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			MAIL_SERVLET.actionGetAllMails(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COUNT)) {
			MAIL_SERVLET.actionGetMailCount(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			//throw new OXMailException(MailCode.UNKNOWN_ACTION, action);
			MAIL_SERVLET.actionGetUpdates(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.regionMatches(true, 0, AJAXServlet.ACTION_REPLY, 0, 5)) {
			MAIL_SERVLET.actionGetReply(sessionObj, pw, jsonObject, (action
					.equalsIgnoreCase(AJAXServlet.ACTION_REPLYALL)), mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_FORWARD)) {
			MAIL_SERVLET.actionGetForward(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			MAIL_SERVLET.actionGetMessage(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_MATTACH)) {
			MAIL_SERVLET.actionGetAttachment();
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW_MSGS)) {
			MAIL_SERVLET.actionGetNew(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			MAIL_SERVLET.actionPutMailList(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			MAIL_SERVLET.actionPutDeleteMails(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			MAIL_SERVLET.actionPutUpdateMail(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
			MAIL_SERVLET.actionPutCopyMail(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_MATTACH)) {
			MAIL_SERVLET.actionPutAttachment(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_MAIL_RECEIPT_ACK)) {
			MAIL_SERVLET.actionPutReceiptAck(sessionObj, pw, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			MAIL_SERVLET.actionPutMailSearch(sessionObj, pw, jsonObject, mailInterface);
		} else {
			throw new OXMailException(MailCode.UNKNOWN_ACTION, action);
		}
	}

}
