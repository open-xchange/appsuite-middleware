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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.MailInterface;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.imap.OXMailException;
import com.openexchange.imap.OXMailException.MailCode;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.SearchIteratorException;

public final class MailRequest {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailRequest.class);
	
	private static enum CollectableOperation {
		MOVE("Move"), COPY("Copy"), STORE_FLAG("Store Flag"), COLOR_LABEL("Color Label");
		
		private final String str;
		
		private CollectableOperation(final String str) {
			this.str = str;
		}
		
		/* (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return str;
		}
	}

	private final SessionObject session;

	private final OXJSONWriter writer;

	private CollectObject collectObj;

	private static final String STR_DATA = "data";

	private static final Mail MAIL_SERVLET = new Mail();

	public MailRequest(final SessionObject session, final OXJSONWriter writer) throws JSONException {
		super();
		this.session = session;
		this.writer = writer;
		this.writer.array();
	}

	public void action(final String action, final JSONObject jsonObject, final MailInterface mailInterface)
			throws SearchIteratorException, JSONException, OXMailException, OXPermissionException {
		if (!session.getUserConfiguration().hasCalendar()) {
			throw new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "mail");
		}
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			MAIL_SERVLET.actionGetAllMails(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COUNT)) {
			MAIL_SERVLET.actionGetMailCount(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			MAIL_SERVLET.actionGetUpdates(session, writer, jsonObject, mailInterface);
		} else if (action.regionMatches(true, 0, AJAXServlet.ACTION_REPLY, 0, 5)) {
			MAIL_SERVLET.actionGetReply(session, writer, jsonObject, (action
					.equalsIgnoreCase(AJAXServlet.ACTION_REPLYALL)), mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_FORWARD)) {
			MAIL_SERVLET.actionGetForward(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			MAIL_SERVLET.actionGetMessage(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_MATTACH)) {
			MAIL_SERVLET.actionGetAttachment();
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_NEW_MSGS)) {
			MAIL_SERVLET.actionGetNew(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_LIST)) {
			MAIL_SERVLET.actionPutMailList(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_DELETE)) {
			MAIL_SERVLET.actionPutDeleteMails(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
			if (isMove(jsonObject)) {
				handleMultiple(jsonObject, mailInterface, CollectableOperation.MOVE);
			} else if (isStoreFlags(jsonObject)) {
				handleMultiple(jsonObject, mailInterface, CollectableOperation.STORE_FLAG);
			} else if (isColorLabel(jsonObject)) {
				handleMultiple(jsonObject, mailInterface, CollectableOperation.COLOR_LABEL);
			} else {
				MAIL_SERVLET.actionPutUpdateMail(session, writer, jsonObject, mailInterface);
			}
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
			handleMultiple(jsonObject, mailInterface, CollectableOperation.COPY);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_MATTACH)) {
			MAIL_SERVLET.actionPutAttachment(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_MAIL_RECEIPT_ACK)) {
			MAIL_SERVLET.actionPutReceiptAck(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_SEARCH)) {
			MAIL_SERVLET.actionPutMailSearch(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_CLEAR)) {
			MAIL_SERVLET.actionPutClear(session, writer, jsonObject, mailInterface);
		} else {
			throw new OXMailException(MailCode.UNKNOWN_ACTION, action);
		}
	}

	private void handleMultiple(final JSONObject jsonObject, final MailInterface mailInterface, final CollectableOperation op)
			throws JSONException {
		if (collectObj != null) {
			if (collectObj.collectable(jsonObject, op)) {
				collectObj.addMailID(jsonObject.getString(AJAXServlet.PARAMETER_ID));
			} else {
				performMultipleInternal(mailInterface);
				collectObj = null;
			}
		} else {
			/*
			 * Collect
			 */
			collectObj = new CollectObject(jsonObject, op);
			collectObj.addMailID(jsonObject.getString(AJAXServlet.PARAMETER_ID));
		}
	}

	public boolean hasMultiple() {
		return (collectObj != null);
	}

	public void performMultiple(final MailInterface mailInterface) throws JSONException {
		if (collectObj != null) {
			performMultipleInternal(mailInterface);
			collectObj = null;
		}
	}
	
	private void performMultipleInternal(final MailInterface mailInterface) throws JSONException {
		final long start = System.currentTimeMillis();
		switch (collectObj.getOperation()) {
		case MOVE:
			MAIL_SERVLET.actionPutMailMultiple(session, writer, collectObj.getMailIDs(), collectObj.getSrcFld(),
					collectObj.getDestFld(), true, mailInterface);
			break;
		case COPY:
			MAIL_SERVLET.actionPutMailMultiple(session, writer, collectObj.getMailIDs(), collectObj.getSrcFld(),
					collectObj.getDestFld(), false, mailInterface);
			break;
		case STORE_FLAG:
			MAIL_SERVLET.actionPutStoreFlagsMultiple(session, writer, collectObj.getMailIDs(), collectObj.getSrcFld(),
					collectObj.getFlagInt(), collectObj.getFlagValue(), mailInterface);
			break;
		case COLOR_LABEL:
			MAIL_SERVLET.actionPutColorLabelMultiple(session, writer, collectObj.getMailIDs(), collectObj.getSrcFld(),
					collectObj.getFlagInt(), mailInterface);
			break;
		default:
			/*
			 * Cannot occur since all enums are contained in
			 * switch-case-statement
			 */
			throw new InternalError("Unknown collectable operation: " + collectObj.getOperation());
		}
		if (LOG.isInfoEnabled()) {
			LOG.info(new StringBuilder(100).append("Multiple '").append(collectObj.getOperation().toString()).append(
					"' mail request successfully performed: ").append(System.currentTimeMillis() - start).append("msec")
					.toString());
		}
	}

	public JSONArray getContent() {
		return (JSONArray) (!writer.isEmpty() && writer.isJSONArray() ? writer.getObject() : null);
	}

	private static boolean isMove(final JSONObject jsonObject) throws JSONException {
		return jsonObject.has(STR_DATA) && jsonObject.getJSONObject(STR_DATA).has(FolderFields.FOLDER_ID);
	}
	
	private static boolean isStoreFlags(final JSONObject jsonObject) throws JSONException {
		return jsonObject.has(STR_DATA) && jsonObject.getJSONObject(STR_DATA).has(JSONMessageObject.JSON_FLAGS);
	}
	
	private static boolean isColorLabel(final JSONObject jsonObject) throws JSONException {
		return jsonObject.has(STR_DATA) && jsonObject.getJSONObject(STR_DATA).has(CommonFields.COLORLABEL);
	}

	private static final class CollectObject {

		private final String srcFld;

		private final String destFld;

		private final List<String> mailIDs;

		private final CollectableOperation op;
		
		private final int flagInt;
		
		private final boolean flagValue;

		public CollectObject(final JSONObject jsonObject, final CollectableOperation op) throws JSONException {
			this.srcFld = jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID);
			if (CollectableOperation.MOVE.equals(op) || CollectableOperation.COPY.equals(op)) {
				this.destFld = jsonObject.getJSONObject(STR_DATA).getString(FolderFields.FOLDER_ID);
				flagInt = -1;
				flagValue = false;
			} else if (CollectableOperation.STORE_FLAG.equals(op)) {
				this.destFld = null;
				final JSONObject bodyObj = jsonObject.getJSONObject(STR_DATA);
				flagInt = bodyObj.getInt(JSONMessageObject.JSON_FLAGS);
				flagValue = bodyObj.getBoolean(JSONMessageObject.JSON_VALUE);
			} else if (CollectableOperation.COLOR_LABEL.equals(op)) {
				this.destFld = null;
				flagInt = jsonObject.getJSONObject(STR_DATA).getInt(CommonFields.COLORLABEL);
				flagValue = false;
			} else {
				throw new InternalError("Unknown collectable operation: " + op);
			}
			this.mailIDs = new ArrayList<String>();
			this.op = op;
		}

		public CollectObject(final String srcFld, final String destFld, final CollectableOperation op) {
			this.srcFld = srcFld;
			this.destFld = destFld;
			this.mailIDs = new ArrayList<String>();
			this.op = op;
			flagInt = -1;
			flagValue = false;
		}

		public boolean collectable(final JSONObject jsonObject, final CollectableOperation op) throws JSONException {
			if (CollectableOperation.MOVE.equals(op) || CollectableOperation.COPY.equals(op)) {
				return collectable(jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID), jsonObject.getJSONObject(
						STR_DATA).getString(FolderFields.FOLDER_ID), op);
			} else if (CollectableOperation.STORE_FLAG.equals(op)) {
				final JSONObject bodyObj = jsonObject.getJSONObject(STR_DATA);
				return (this.op.equals(op) && this.srcFld.equals(jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID))
						&& flagInt == bodyObj.getInt(JSONMessageObject.JSON_FLAGS) && flagValue == bodyObj
						.getBoolean(JSONMessageObject.JSON_VALUE));
			} else if (CollectableOperation.COLOR_LABEL.equals(op)) {
				return (this.op.equals(op) && this.srcFld.equals(jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID))
						&& flagInt == jsonObject.getJSONObject(STR_DATA).getInt(CommonFields.COLORLABEL));
			}
			throw new InternalError("Unknown collectable operation: " + op);
		}

		private boolean collectable(final String srcFld, final String destFld, final CollectableOperation op) {
			return (this.op.equals(op) && this.srcFld.equals(srcFld) && this.destFld.equals(destFld));
		}

		public String getDestFld() {
			return destFld;
		}

		public void addMailID(final String mailID) {
			mailIDs.add(mailID);
		}

		public String[] getMailIDs() {
			return mailIDs.toArray(new String[mailIDs.size()]);
		}

		public String getSrcFld() {
			return srcFld;
		}
		
		public int getFlagInt() {
			return flagInt;
		}
		
		public boolean getFlagValue() {
			return flagValue;
		}

		public CollectableOperation getOperation() {
			return op;
		}

	}

}
