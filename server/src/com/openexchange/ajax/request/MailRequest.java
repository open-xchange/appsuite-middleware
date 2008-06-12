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

import static com.openexchange.ajax.container.Response.DATA;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api.OXPermissionException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;

public final class MailRequest {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailRequest.class);

	private static enum CollectableOperation {
		MOVE, COPY, STORE_FLAG, COLOR_LABEL;
	}

	private static final Mail MAIL_SERVLET = new Mail();

	/* -------------- Fields -------------- */

	private final Session session;

	private final Context ctx;

	private final OXJSONWriter writer;

	private CollectObject collectObj;

	private boolean contCollecting;

	/**
	 * Constructor
	 * 
	 * @param session
	 *            - the session reference keeping user-specific data
	 * @param writer
	 *            - the instance of <code>{@link OXJSONWriter}</code> to whom
	 *            response data is written
	 */
	public MailRequest(final Session session, final Context ctx, final OXJSONWriter writer) {
		super();
		this.session = session;
		this.ctx = ctx;
		this.writer = writer;
	}

	/**
	 * Performs the action associated with given <code>action</code> parameter
	 * 
	 * @param action
	 *            - the action to perform
	 * @param jsonObject
	 *            - the instance of <code>{@link JSONObject}</code> keeping
	 *            request's data
	 * @param mailInterface
	 *            - the instance of <code>{@link MailServletInterface}</code> to
	 *            access mail module
	 */
	public void action(final String action, final JSONObject jsonObject, final MailServletInterface mailInterface)
			throws SearchIteratorException, JSONException, MailException, OXPermissionException {
		if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx).hasWebMail()) {
			throw new OXPermissionException(OXPermissionException.Code.NoPermissionForModul, "mail");
		}
		if (action.equalsIgnoreCase(AJAXServlet.ACTION_ALL)) {
			MAIL_SERVLET.actionGetAllMails(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COUNT)) {
			MAIL_SERVLET.actionGetMailCount(session, writer, jsonObject, mailInterface);
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATES)) {
			MAIL_SERVLET.actionGetUpdates(session, writer, jsonObject, mailInterface);
		} else if (action.regionMatches(true, 0, AJAXServlet.ACTION_REPLY, 0, 5)) {
			if (jsonObject.has(Response.DATA) && !jsonObject.isNull(Response.DATA)) {
				MAIL_SERVLET.actionPutReply(session, (action.equalsIgnoreCase(AJAXServlet.ACTION_REPLYALL)), writer,
						jsonObject, mailInterface);
			} else {
				MAIL_SERVLET.actionGetReply(session, writer, jsonObject, (action
						.equalsIgnoreCase(AJAXServlet.ACTION_REPLYALL)), mailInterface);
			}
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_FORWARD)) {
			if (jsonObject.has(Response.DATA) && !jsonObject.isNull(Response.DATA)) {
				/*
				 * Perform forward with multiple mails
				 */
				MAIL_SERVLET.actionPutForwardMultiple(session, writer, jsonObject, mailInterface);
			} else {
				MAIL_SERVLET.actionGetForward(session, writer, jsonObject, mailInterface);
			}
		} else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET)) {
			if (jsonObject.has(Response.DATA) && !jsonObject.isNull(Response.DATA)) {
				MAIL_SERVLET.actionPutGet(session, writer, jsonObject, mailInterface);
			} else {
				MAIL_SERVLET.actionGetMessage(session, writer, jsonObject, mailInterface);
			}
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
			throw new MailException(MailException.Code.UNKNOWN_ACTION, action);
		}
	}

	private void handleMultiple(final JSONObject jsonObject, final MailServletInterface mailInterface,
			final CollectableOperation op) throws JSONException {
		if (collectObj == null) {
			/*
			 * Collect
			 */
			collectObj = new CollectObject(jsonObject, op);
			collectObj.addMailID(new MailPath(jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID), jsonObject
					.getLong(AJAXServlet.PARAMETER_ID)));
			contCollecting = true;
		} else {
			if (collectObj.collectable(jsonObject, op)) {
				collectObj.addMailID(new MailPath(jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID), jsonObject
						.getLong(AJAXServlet.PARAMETER_ID)));
				contCollecting = true;
			} else {
				performMultipleInternal(mailInterface);
				/*
				 * Start new collect
				 */
				collectObj = new CollectObject(jsonObject, op);
				collectObj.addMailID(new MailPath(jsonObject.getString(AJAXServlet.PARAMETER_FOLDERID), jsonObject
						.getLong(AJAXServlet.PARAMETER_ID)));
				contCollecting = false;
			}
		}
	}

	/**
	 * Indicates if this {@link MailRequest mail request} is collecting
	 * contiguously
	 * 
	 * @return <code>true</code> if this {@link MailRequest mail request} is
	 *         collecting contiguously; otherwise <code>false</code>
	 */
	public boolean isContiguousCollect() {
		return contCollecting;
	}

	/**
	 * Executes gathered actions and writes their response to the instance of
	 * <code>{@link OXJSONWriter}</code> given through constructor
	 * <code>{@link #MailRequest(Session, Context, OXJSONWriter)}</code>
	 * 
	 * @param mailInterface
	 *            The mail interface
	 * @throws JSONException
	 *             If writing JSON response fails
	 */
	public void performMultiple(final MailServletInterface mailInterface) throws JSONException {
		if (collectObj != null) {
			performMultipleInternal(mailInterface);
			collectObj = null;
		}
	}

	private void performMultipleInternal(final MailServletInterface mailInterface) throws JSONException {
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
			 * Cannot occur since all enums are covered in switch-case-statement
			 */
			throw new InternalError("Unknown collectable operation: " + collectObj.getOperation());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder(100).append("Multiple '").append(getOpName(collectObj.getOperation())).append(
					"' mail request successfully performed: ").append(System.currentTimeMillis() - start)
					.append("msec").toString());
		}
	}

	private static boolean isMove(final JSONObject jsonObject) throws JSONException {
		return jsonObject.has(DATA) && jsonObject.getJSONObject(DATA).has(FolderFields.FOLDER_ID);
	}

	private static boolean isStoreFlags(final JSONObject jsonObject) throws JSONException {
		return jsonObject.has(DATA) && jsonObject.getJSONObject(DATA).has(MailJSONField.FLAGS.getKey());
	}

	private static boolean isColorLabel(final JSONObject jsonObject) throws JSONException {
		return jsonObject.has(DATA) && jsonObject.getJSONObject(DATA).has(CommonFields.COLORLABEL);
	}

	private static final class CollectObject {

		private final String srcFld;

		private final String destFld;

		private final List<MailPath> mailIDs;

		private final CollectableOperation op;

		private final int flagInt;

		private final boolean flagValue;

		/**
		 * Initializes a new {@link CollectObject}
		 * 
		 * @param dataObject
		 *            The JSON object containing request's data and parameters
		 * @param op
		 *            The identified collectable operation
		 * @throws JSONException
		 *             If reading from provided JSON object fails
		 */
		public CollectObject(final JSONObject dataObject, final CollectableOperation op) throws JSONException {
			this.srcFld = dataObject.getString(AJAXServlet.PARAMETER_FOLDERID);
			if (CollectableOperation.MOVE.equals(op) || CollectableOperation.COPY.equals(op)) {
				this.destFld = dataObject.getJSONObject(DATA).getString(FolderFields.FOLDER_ID);
				flagInt = -1;
				flagValue = false;
			} else if (CollectableOperation.STORE_FLAG.equals(op)) {
				this.destFld = null;
				final JSONObject bodyObj = dataObject.getJSONObject(DATA);
				flagInt = bodyObj.getInt(MailJSONField.FLAGS.getKey());
				flagValue = bodyObj.getBoolean(MailJSONField.VALUE.getKey());
			} else if (CollectableOperation.COLOR_LABEL.equals(op)) {
				this.destFld = null;
				flagInt = dataObject.getJSONObject(DATA).getInt(CommonFields.COLORLABEL);
				flagValue = false;
			} else {
				throw new InternalError("Unknown collectable operation: " + op);
			}
			this.mailIDs = new ArrayList<MailPath>();
			this.op = op;
		}

		/**
		 * Checks if given collectable operation can be further added to
		 * previous collectable operation stored in this {@link CollectObject}
		 * and thus needs no direct execution.
		 * 
		 * @param dataObject
		 *            The JSON object containing request's data and parameters
		 * @param op
		 *            The identified collectable operation
		 * @return <code>true</code> f given collectable operation can be
		 *         further collected; otherwise <code>false</code>
		 * @throws JSONException
		 *             If reading from provided JSON object fails
		 */
		public boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws JSONException {
			if (CollectableOperation.MOVE.equals(op) || CollectableOperation.COPY.equals(op)) {
				return (this.op.equals(op) && this.srcFld.equals(dataObject.getString(AJAXServlet.PARAMETER_FOLDERID)) && this.destFld
						.equals(dataObject.getJSONObject(DATA).getString(FolderFields.FOLDER_ID)));
			} else if (CollectableOperation.STORE_FLAG.equals(op)) {
				final JSONObject bodyObj = dataObject.getJSONObject(DATA);
				return (this.op.equals(op) && this.srcFld.equals(dataObject.getString(AJAXServlet.PARAMETER_FOLDERID))
						&& flagInt == bodyObj.getInt(MailJSONField.FLAGS.getKey()) && flagValue == bodyObj
						.getBoolean(MailJSONField.VALUE.getKey()));
			} else if (CollectableOperation.COLOR_LABEL.equals(op)) {
				return (this.op.equals(op) && this.srcFld.equals(dataObject.getString(AJAXServlet.PARAMETER_FOLDERID)) && flagInt == dataObject
						.getJSONObject(DATA).getInt(CommonFields.COLORLABEL));
			}
			throw new InternalError("Unknown collectable operation: " + op);
		}

		public String getDestFld() {
			return destFld;
		}

		public void addMailID(final MailPath mailID) {
			mailIDs.add(mailID);
		}

		public MailPath[] getMailIDs() {
			return mailIDs.toArray(new MailPath[mailIDs.size()]);
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

	private static String getOpName(final CollectableOperation op) {
		switch (op) {
		case MOVE:
			return "Move";
		case COPY:
			return "Copy";
		case STORE_FLAG:
			return "Store Flag";
		case COLOR_LABEL:
			return "Color Label";
		default:
			throw new InternalError("Unknown collectable operation: " + op);
		}

	}
}
