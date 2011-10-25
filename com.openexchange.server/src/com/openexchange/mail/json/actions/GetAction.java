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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.json.actions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Action;
import com.openexchange.exception.OXException;
import com.openexchange.log.Log;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.utils.CharsetDetector;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(defaultFormat = "mail")
public final class GetAction extends AbstractMailAction {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(org.apache.commons.logging.LogFactory.getLog(GetAction.class));

    /**
     * Initializes a new {@link GetAction}.
     *
     * @param services
     */
    public GetAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        final JSONArray paths = (JSONArray) req.getRequest().getData();
        if (null == paths) {
            return performGet(req);
        }
        return performPut(req, paths);
    }

    private AJAXRequestResult performPut(final MailRequest req, final JSONArray paths) throws OXException {
        try {
            final int length = paths.length();
            if (length != 1) {
                throw new IllegalArgumentException("JSON array's length is not 1");
            }
            final AJAXRequestData requestData = new AJAXRequestData();
            final AJAXRequestData request = req.getRequest();
            for (final Iterator<String> it = request.getParameterNames(); it.hasNext();) {
                final String name = it.next();
                requestData.putParameter(name, request.getParameter(name));
            }
            for (int i = 0; i < length; i++) {
                final JSONObject folderAndID = paths.getJSONObject(i);
                requestData.putParameter(Mail.PARAMETER_FOLDERID, folderAndID.getString(Mail.PARAMETER_FOLDERID));
                requestData.putParameter(Mail.PARAMETER_ID, folderAndID.get(Mail.PARAMETER_ID).toString());
            }
            /*
             * ... and fake a GET request
             */
            return performGet(new MailRequest(requestData, req.getSession()));
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult performGet(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(Mail.PARAMETER_FOLDERID);
            // final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            String tmp = req.getParameter(Mail.PARAMETER_SHOW_SRC);
            final boolean showMessageSource = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = req.getParameter(Mail.PARAMETER_SHOW_HEADER);
            final boolean showMessageHeaders = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            tmp = req.getParameter(Mail.PARAMETER_SAVE);
            final boolean saveToDisk = (tmp != null && tmp.length() > 0 && Integer.parseInt(tmp) > 0);
            tmp = req.getParameter(Mail.PARAMETER_UNSEEN);
            final boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
            tmp = null;
            /*
             * Warnings container
             */
            final List<OXException> warnings = new ArrayList<OXException>(2);
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Determine mail identifier
             */
            final String uid;
            {
                String tmp2 = req.getParameter(Mail.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = req.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(Mail.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }
            AJAXRequestResult data = getJSONNullResult();
            if (showMessageSource) {
                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
                try {
                    mail.writeTo(baos);
                } catch (final OXException e) {
                    if (!MailExceptionCode.NO_CONTENT.equals(e)) {
                        throw e;
                    }
                    LOG.debug(e.getMessage(), e);
                    baos.reset();
                }
                final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                final boolean doUnseen = (unseen && wasUnseen);
                if (doUnseen) {
                    mail.setFlag(MailMessage.FLAG_SEEN, false);
                    final int unreadMsgs = mail.getUnreadMessages();
                    mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                }
                if (doUnseen) {
                    /*
                     * Leave mail as unseen
                     */
                    mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                } else if (wasUnseen) {
                    /*
                     * Trigger contact collector
                     */
                    try {
                        final ServerUserSetting setting = ServerUserSetting.getInstance();
                        final int contextId = session.getContextId();
                        final int userId = session.getUserId();
                        if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
                if (saveToDisk) {
                    /*
                     * Create appropriate file holder
                     */
                    req.getRequest().setFormat("file");
                    final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(baos.toByteArray());
                    fileHolder.setContentType("application/octet-stream");
                    fileHolder.setName(new StringBuilder(mail.getSubject()).append(".eml").toString());
                    return new AJAXRequestResult(fileHolder, "file");
                }
                final ContentType ct = mail.getContentType();
                if (ct.containsCharsetParameter() && CharsetDetector.isValid(ct.getCharsetParameter())) {
                    data = new AJAXRequestResult(new String(baos.toByteArray(), ct.getCharsetParameter()), "string");
                } else {
                    data = new AJAXRequestResult(new String(baos.toByteArray(), "UTF-8"), "string");
                }
            } else if (showMessageHeaders) {
                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
                final boolean doUnseen = (unseen && wasUnseen);
                if (doUnseen) {
                    mail.setFlag(MailMessage.FLAG_SEEN, false);
                    final int unreadMsgs = mail.getUnreadMessages();
                    mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
                }
                data = new AJAXRequestResult(formatMessageHeaders(mail.getHeadersIterator()), "string");
                if (doUnseen) {
                    /*
                     * Leave mail as unseen
                     */
                    mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
                } else if (wasUnseen) {
                    try {
                        final ServerUserSetting setting = ServerUserSetting.getInstance();
                        final int contextId = session.getContextId();
                        final int userId = session.getUserId();
                        if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailAccess(
                            contextId,
                            userId).booleanValue()) {
                            triggerContactCollector(session, mail);
                        }
                    } catch (final OXException e) {
                        LOG.warn("Contact collector could not be triggered.", e);
                    }
                }
            } else {
//                tmp = req.getParameter(Mail.PARAMETER_EDIT_DRAFT);
//                final boolean editDraft = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
//                tmp = req.getParameter(Mail.PARAMETER_VIEW);
//                final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
//                tmp = null;
//                final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
//                /*
//                 * Deny saving for this request-specific settings
//                 */
//                usmNoSave.setNoSave(true);
//                /*
//                 * Overwrite settings with request's parameters
//                 */
//                detectDisplayMode(editDraft, view, usmNoSave);
                /*
                 * Get message
                 */
                final MailMessage mail = mailInterface.getMessage(folderPath, uid);
                if (mail == null) {
                    throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
                }
                mail.setAccountId(mailInterface.getAccountID());
                data = new AJAXRequestResult(mail, "mail");
            }
            data.addWarnings(warnings);
            return data;
        } catch (final OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn(
                    new StringBuilder("Requested mail could not be found. ").append(
                        "Most likely this is caused by concurrent access of multiple clients ").append(
                        "while one performed a delete on affected mail.").toString(),
                    e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            throw e;
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final String formatMessageHeaders(final Iterator<Map.Entry<String, String>> iter) {
        final StringBuilder sb = new StringBuilder(1024);
        final String delim = ": ";
        final String crlf = "\r\n";
        while (iter.hasNext()) {
            final Map.Entry<String, String> entry = iter.next();
            sb.append(entry.getKey()).append(delim).append(entry.getValue()).append(crlf);
        }
        return sb.toString();
    }

}
