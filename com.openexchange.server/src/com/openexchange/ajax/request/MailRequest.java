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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.ajax.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.tools.JSONUtil;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailRequest} - A mail request handle via <code>Multiple</code> Servlet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailRequest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailRequest.class);

    static final String PARAMETER_ID = AJAXServlet.PARAMETER_ID;
    static final String FOLDER_ID = FolderChildFields.FOLDER_ID;
    static final String KEY = MailJSONField.FLAGS.getKey();
    static final String COLORLABEL = CommonFields.COLORLABEL;
    static final String DATA = ResponseFields.DATA;
    static final String PARAMETER_FOLDERID = AJAXServlet.PARAMETER_FOLDERID;

    private static enum CollectableOperation {
        MOVE, COPY, STORE_FLAG, COLOR_LABEL, GET;
    }

    /**
     *  The mail servlet instance
     */
    static final Mail MAIL_SERVLET = new Mail();

    private static interface Handler {

        void requestPerformed(ServerSession session, JSONWriter writer, JSONObject jsonObject, MailServletInterface mailInterface) throws OXException, JSONException;
    }

    private static final Handler GET_HANDLER;

    private static final Map<String, Handler> HANDERS_MAP;

    static {
        final Map<String, Handler> m = new HashMap<String, Handler>(24);
        m.put(AJAXServlet.ACTION_ALL, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetAllMails(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_COUNT, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetMailCount(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_UPDATES, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetUpdates(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_REPLY, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                if (jsonObject.has(DATA) && !jsonObject.isNull(DATA)) {
                    MAIL_SERVLET.actionPutReply(session, false, writer, jsonObject, mailInterface);
                } else {
                    MAIL_SERVLET.actionGetReply(session, writer, jsonObject, false, mailInterface);
                }
            }
        });
        m.put(AJAXServlet.ACTION_REPLYALL, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                if (jsonObject.has(DATA) && !jsonObject.isNull(DATA)) {
                    MAIL_SERVLET.actionPutReply(session, true, writer, jsonObject, mailInterface);
                } else {
                    MAIL_SERVLET.actionGetReply(session, writer, jsonObject, true, mailInterface);
                }
            }
        });
        m.put(AJAXServlet.ACTION_FORWARD, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                if (jsonObject.has(DATA) && !jsonObject.isNull(DATA)) {
                    /*
                     * Perform forward with multiple mails
                     */
                    MAIL_SERVLET.actionPutForwardMultiple(session, writer, jsonObject, mailInterface);
                } else {
                    MAIL_SERVLET.actionGetForward(session, writer, jsonObject, mailInterface);
                }
            }
        });
        {
            Handler h = new Handler() {

                @Override
                public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                    if (jsonObject.has(DATA) && !jsonObject.isNull(DATA)) {
                        MAIL_SERVLET.actionPutGet(session, writer, jsonObject, mailInterface);
                    } else {
                        MAIL_SERVLET.actionGetMessage(session, writer, jsonObject, mailInterface);
                    }
                }
            };
            GET_HANDLER = h;
        }
        m.put(AJAXServlet.ACTION_GET_STRUCTURE, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetStructure(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_MATTACH, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetAttachment();
            }
        });
        m.put("attachmenttoken", new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetAttachmentToken(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_NEW_MSGS, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionGetNew(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_LIST, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionPutMailList(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_DELETE, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionPutDeleteMails(session, writer, jsonObject, mailInterface);
            }
        });
        m.put("transport", new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionPutTransportMail(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_MAIL_RECEIPT_ACK, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionPutReceiptAck(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_SEARCH, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionPutMailSearch(session, writer, jsonObject, mailInterface);
            }
        });
        m.put(AJAXServlet.ACTION_CLEAR, new Handler() {

            @Override
            public void requestPerformed(final ServerSession session, final JSONWriter writer, final JSONObject jsonObject, final MailServletInterface mailInterface) throws OXException, JSONException {
                MAIL_SERVLET.actionPutClear(session, writer, jsonObject, mailInterface);
            }
        });
        HANDERS_MAP = m;
    }

    /* -------------- Fields -------------- */

    private final ServerSession session;
    private final OXJSONWriter writer;

    private CollectObject collectObj;
    private boolean contCollecting;

    /**
     * Constructor
     *
     * @param session - the session reference keeping user-specific data
     * @param writer - the instance of <code>{@link OXJSONWriter}</code> to whom response data is written
     */
    public MailRequest(final ServerSession session, final OXJSONWriter writer) {
        super();
        this.session = session;
        this.writer = writer;
    }

    /**
     * Performs the action associated with given <code>action</code> parameter
     *
     * @param action - the action to perform
     * @param jsonObject - the instance of <code>{@link JSONObject}</code> keeping request's data
     * @param mailInterface - the instance of <code>{@link MailServletInterface}</code> to access mail module
     */
    public void action(String action, JSONObject jsonObject, MailServletInterface mailInterface) throws OXException, JSONException {
        if (!session.getUserPermissionBits().hasWebMail()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("mail");
        }
        if (null == action) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("action");
        }

        String act = Strings.asciiLowerCase(action);
        {
            Handler handler = HANDERS_MAP.get(act);
            if (null != handler) {
                handler.requestPerformed(session, writer, jsonObject, mailInterface);
                return;
            }
        }

        if (AJAXServlet.ACTION_UPDATE.equals(act)) {
            if (isMove(jsonObject)) {
                handleMultiple(jsonObject, mailInterface, CollectableOperation.MOVE);
            } else if (isStoreFlags(jsonObject)) {
                handleMultiple(jsonObject, mailInterface, CollectableOperation.STORE_FLAG);
            } else if (isColorLabel(jsonObject)) {
                handleMultiple(jsonObject, mailInterface, CollectableOperation.COLOR_LABEL);
            } else {
                MAIL_SERVLET.actionPutUpdateMail(session, writer, jsonObject, mailInterface);
            }
        } else if (AJAXServlet.ACTION_COPY.equals(act)) {
            handleMultiple(jsonObject, mailInterface, CollectableOperation.COPY);
        } else if (AJAXServlet.ACTION_GET.equals(act)) {
            if (isCollectableGet(jsonObject)) {
                handleMultiple(jsonObject, mailInterface, CollectableOperation.GET);
            } else {
                GET_HANDLER.requestPerformed(session, writer, jsonObject, mailInterface);
            }
        } else {
            throw MailExceptionCode.UNKNOWN_ACTION.create(action);
        }
    }

    private void handleMultiple(final JSONObject jsonObject, final MailServletInterface mailInterface, final CollectableOperation op) throws OXException {
        if (collectObj == null) {
            /*
             * Collect
             */
            collectObj = CollectObject.newInstance(jsonObject, op, MAIL_SERVLET);
            collectObj.addCollectable(jsonObject);
            contCollecting = true;
        } else {
            if (collectObj.collectable(jsonObject, op)) {
                collectObj.addCollectable(jsonObject);
                contCollecting = true;
            } else {
                try {
                    performMultipleInternal(mailInterface);
                    /*
                     * Start new collect
                     */
                    collectObj = CollectObject.newInstance(jsonObject, op, MAIL_SERVLET);
                    collectObj.addCollectable(jsonObject);
                    contCollecting = false;
                } catch (final JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                }
            }
        }
    }

    /**
     * Indicates if this {@link MailRequest mail request} is collecting contiguously
     *
     * @return <code>true</code> if this {@link MailRequest mail request} is collecting contiguously; otherwise <code>false</code>
     */
    public boolean isContiguousCollect() {
        return contCollecting;
    }

    /**
     * Executes gathered actions and writes their response to the instance of <code>{@link OXJSONWriter}</code> given through constructor
     * <code>{@link #MailRequest(Session, Context, OXJSONWriter)}</code>
     *
     * @param mailInterface The mail interface
     * @throws OXException If writing JSON response fails
     */
    public void performMultiple(final MailServletInterface mailInterface) throws OXException {
        if (collectObj != null) {
            try {
                performMultipleInternal(mailInterface);
                collectObj = null;
            } catch (final JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    }

    private void performMultipleInternal(final MailServletInterface mailInterface) throws JSONException {
        collectObj.performOperations(session, writer, mailInterface);
    }

    public static boolean isMove(final JSONObject jsonObject) throws JSONException {
        return jsonObject.has(DATA) && jsonObject.getJSONObject(DATA).has(FOLDER_ID);
    }

    public static boolean isStoreFlags(final JSONObject jsonObject) throws JSONException {
        return jsonObject.has(PARAMETER_ID) && jsonObject.has(DATA) && jsonObject.getJSONObject(DATA).has(KEY);
    }

    public static boolean isColorLabel(final JSONObject jsonObject) throws JSONException {
        return jsonObject.has(PARAMETER_ID) && jsonObject.has(DATA) && jsonObject.getJSONObject(DATA).has(COLORLABEL);
    }

    public static boolean isCollectableGet(final JSONObject jsonObject) {
        if (jsonObject.has(DATA)) {
            return false;
        }
        Object object = jsonObject.opt("format");
        if (null != object && !"json".equalsIgnoreCase(object.toString())) {
            return false;
        }
        object = jsonObject.opt(Mail.PARAMETER_SAVE);
        if (null != object && Integer.parseInt(object.toString()) > 0) {
            return false;
        }
        if (null == jsonObject.opt(PARAMETER_ID)) {
            return false;
        }
        return true;
    }

    private static abstract class CollectObject {

        public static CollectObject newInstance(final JSONObject jsonObject, final CollectableOperation op, final Mail mailServlet) throws OXException {
            switch (op) {
            case COPY:
                return new CopyCollectObject(jsonObject, mailServlet);
            case MOVE:
                return new MoveCollectObject(jsonObject, mailServlet);
            case STORE_FLAG:
                return new FlagsCollectObject(jsonObject, mailServlet);
            case COLOR_LABEL:
                return new ColorCollectObject(jsonObject, mailServlet);
            case GET:
                return new GetCollectObject(jsonObject, mailServlet);
            default:
                /*
                 * Cannot occur since all enums are covered in switch-case-statement
                 */
                throw new InternalError("Unknown collectable operation: " + op);
            }
        }

        protected final Mail mailServlet;

        protected final List<String> mailIDs;

        /**
         * Initializes a new {@link CollectObject}
         *
         * @param mailServlet The mail servlet
         */
        protected CollectObject(final Mail mailServlet) {
            super();
            this.mailIDs = new LinkedList<String>();
            this.mailServlet = mailServlet;
        }

        /**
         * Checks if given collectable operation can be further added to previous collectable operation stored in this {@link CollectObject}
         * and thus needs no direct execution.
         *
         * @param dataObject The JSON object containing request's data and parameters
         * @param op The identified collectable operation
         * @return <code>true</code> f given collectable operation can be further collected; otherwise <code>false</code>
         * @throws OXException If reading from provided JSON object fails
         */
        public abstract boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws OXException;

        /**
         * Performs the collected operations
         *
         * @param session The currently active user session
         * @param writer The JSON writer to write responses to
         * @param mailInterface The mail interface
         * @throws JSONException If a JSON error occurs
         */
        public abstract void performOperations(final ServerSession session, final OXJSONWriter writer, final MailServletInterface mailInterface) throws JSONException;

        /**
         * Gets the collectable operation identifier
         *
         * @return The collectable operation identifier
         */
        public abstract CollectableOperation getOperation();

        /**
         * Adds a collectable operation which has previously been checked by {@link #collectable(JSONObject, CollectableOperation)}
         *
         * @param dataObject
         * @throws OXException If a JSON error occurs
         */
        public void addCollectable(final JSONObject jsonObject) throws  OXException {
            mailIDs.add(JSONUtil.requireString(PARAMETER_ID, jsonObject));
        }

        /**
         * Gets a newly created array of long containing this object's mail IDs.
         *
         * @return A newly created array of long containing this object's mail IDs.
         */
        protected String[] getMailIDs() {
            return mailIDs.toArray(new String[mailIDs.size()]);
        }
    }

    private static final class MoveCollectObject extends CollectObject {

        private final String srcFld;
        private final String destFld;

        public MoveCollectObject(final JSONObject dataObject, final Mail mailServlet) throws OXException {
            super(mailServlet);
            this.srcFld = JSONUtil.requireString(PARAMETER_FOLDERID, dataObject);
            this.destFld = JSONUtil.requireString(FolderChildFields.FOLDER_ID, JSONUtil.requireDataObject(dataObject));
        }

        @Override
        public boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws OXException {
            return (CollectableOperation.MOVE.equals(op) && this.srcFld.equals(JSONUtil.requireString(PARAMETER_FOLDERID, dataObject)) && this.destFld.equals(JSONUtil.requireString(FolderChildFields.FOLDER_ID, JSONUtil.requireDataObject(dataObject))));
        }

        @Override
        public CollectableOperation getOperation() {
            return CollectableOperation.MOVE;
        }

        @Override
        public void performOperations(final ServerSession session, final OXJSONWriter writer, final MailServletInterface mailInterface) throws JSONException {
            mailServlet.actionPutMailMultiple(session, writer, getMailIDs(), srcFld, destFld, true, mailInterface);
        }

    }

    private static final class CopyCollectObject extends CollectObject {

        private final String srcFld;
        private final String destFld;

        public CopyCollectObject(final JSONObject dataObject, final Mail mailServlet) throws OXException {
            super(mailServlet);
            this.srcFld = JSONUtil.requireString(PARAMETER_FOLDERID, dataObject);
            this.destFld = JSONUtil.requireString(FolderChildFields.FOLDER_ID, JSONUtil.requireDataObject(dataObject));
        }

        @Override
        public boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws OXException {
            return (CollectableOperation.COPY.equals(op) && this.srcFld.equals(JSONUtil.requireString(PARAMETER_FOLDERID, dataObject)) && this.destFld.equals(JSONUtil.requireString(FolderChildFields.FOLDER_ID, JSONUtil.requireDataObject(dataObject))));
        }

        @Override
        public CollectableOperation getOperation() {
            return CollectableOperation.COPY;
        }

        @Override
        public void performOperations(final ServerSession session, final OXJSONWriter writer, final MailServletInterface mailInterface) throws JSONException {
            mailServlet.actionPutMailMultiple(session, writer, getMailIDs(), srcFld, destFld, false, mailInterface);
        }

    }

    private static final class FlagsCollectObject extends CollectObject {

        private final String srcFld;
        private final int flagInt;
        private final boolean flagValue;

        public FlagsCollectObject(final JSONObject dataObject, final Mail mailServlet) throws OXException {
            super(mailServlet);
            this.srcFld = JSONUtil.requireString(PARAMETER_FOLDERID, dataObject);
            final JSONObject bodyObj = JSONUtil.requireDataObject(dataObject);
            flagInt = JSONUtil.requireInt(MailJSONField.FLAGS.getKey(), bodyObj);
            flagValue = JSONUtil.requireBoolean(MailJSONField.VALUE.getKey(), bodyObj);
        }

        @Override
        public boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws OXException {
            final JSONObject bodyObj = JSONUtil.requireDataObject(dataObject);
            return (CollectableOperation.STORE_FLAG.equals(op) && this.srcFld.equals(JSONUtil.requireString(PARAMETER_FOLDERID, dataObject)) && flagInt == JSONUtil.requireInt(MailJSONField.FLAGS.getKey(), bodyObj) && flagValue == JSONUtil.requireBoolean(MailJSONField.VALUE.getKey(), bodyObj));
        }

        @Override
        public CollectableOperation getOperation() {
            return CollectableOperation.STORE_FLAG;
        }

        @Override
        public void performOperations(final ServerSession session, final OXJSONWriter writer, final MailServletInterface mailInterface) throws JSONException {
            mailServlet.actionPutStoreFlagsMultiple(session, writer, getMailIDs(), srcFld, flagInt, flagValue, mailInterface);
        }
    }

    private static final class ColorCollectObject extends CollectObject {

        private final String srcFld;
        private final int flagInt;

        public ColorCollectObject(final JSONObject dataObject, final Mail mailServlet) throws OXException {
            super(mailServlet);
            this.srcFld = JSONUtil.requireString(PARAMETER_FOLDERID, dataObject);
            flagInt = JSONUtil.requireInt(MailJSONField.COLOR_LABEL.getKey(), JSONUtil.requireDataObject(dataObject));
        }

        @Override
        public boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws OXException {
            return (CollectableOperation.COLOR_LABEL.equals(op) && srcFld.equals(JSONUtil.requireString(PARAMETER_FOLDERID, dataObject)) && flagInt == JSONUtil.requireInt(MailJSONField.COLOR_LABEL.getKey(), JSONUtil.requireDataObject(dataObject)));
        }

        @Override
        public CollectableOperation getOperation() {
            return CollectableOperation.COLOR_LABEL;
        }

        @Override
        public void performOperations(final ServerSession session, final OXJSONWriter writer, final MailServletInterface mailInterface) throws JSONException {
            mailServlet.actionPutColorLabelMultiple(session, writer, getMailIDs(), srcFld, flagInt, mailInterface);
        }
    }

    private static final class GetCollectObject extends CollectObject {

        private final String folder;
        private final List<ParamContainer> containers;

        public GetCollectObject(final JSONObject dataObject, final Mail mailServlet) throws OXException {
            super(mailServlet);
            this.folder = JSONUtil.requireString(PARAMETER_FOLDERID, dataObject);
            containers = new LinkedList<ParamContainer>();
            ParamContainer container = ParamContainer.getInstance(dataObject);
            containers.add(container);
            mailIDs.add(container.getStringParam(PARAMETER_ID));
        }

        @Override
        public boolean collectable(final JSONObject dataObject, final CollectableOperation op) throws OXException {
            if (!CollectableOperation.GET.equals(op) || !folder.equals(JSONUtil.requireString(PARAMETER_FOLDERID, dataObject))) {
                return false;
            }
            ParamContainer container = ParamContainer.getInstance(dataObject);
            String id = container.getStringParam(PARAMETER_ID);
            if (null == id) {
                return false;
            }
            containers.add(container);
            mailIDs.add(id);
            return true;
        }

        @Override
        public void addCollectable(JSONObject jsonObject) throws OXException {
            // Nothing to do
        }

        @Override
        public CollectableOperation getOperation() {
            return CollectableOperation.GET;
        }

        @Override
        public void performOperations(final ServerSession session, final OXJSONWriter writer, final MailServletInterface mailInterface) throws JSONException {
            mailServlet.actionGetGetMessageMultiple(session, writer, getMailIDs(), containers.toArray(new ParamContainer[containers.size()]), folder, mailInterface);
        }
    }
}
