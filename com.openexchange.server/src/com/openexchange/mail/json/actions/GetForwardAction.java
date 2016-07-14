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

package com.openexchange.mail.json.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetForwardAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "forward", description = "Forward a mail.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the requested Message."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "view", optional=true, description = "(available with SP6) - \"text\" forces the server to deliver a text-only version of the requested mail's body, even if content is HTML. \"html\" to allow a possible HTML mail body being transferred as it is (but white-list filter applied).NOTE: if set, the corresponding gui config setting will be ignored.")
}, responseDescription = "(not IMAP: with timestamp): An object containing all data of the requested mail. The fields of the object are listed in Detailed mail data. The fields id and attachment are not included.")
public final class GetForwardAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetForwardAction.class);

    /**
     * Initializes a new {@link GetForwardAction}.
     *
     * @param services
     */
    public GetForwardAction(final ServiceLookup services) {
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

    private AJAXRequestResult performGet(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            String view = req.getParameter(Mail.PARAMETER_VIEW);
            String csid = req.getParameter(AJAXServlet.PARAMETER_CSID);
            UserSettingMail usmNoSave = session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            detectDisplayMode(true, view, usmNoSave);
            if (AJAXRequestDataTools.parseBoolParameter(req.getParameter("dropPrefix"))) {
                usmNoSave.setDropReplyForwardPrefix(true);
            }
            if (AJAXRequestDataTools.parseBoolParameter(req.getParameter("attachOriginalMessage"))) {
                usmNoSave.setAttachOriginalMessage(true);
            }
            boolean setFrom = AJAXRequestDataTools.parseBoolParameter(req.getParameter("setFrom"));
            /*
             * Get mail interface
             */
            MailServletInterface mailInterface = getMailInterface(req);
            MailMessage mailMessage = mailInterface.getForwardMessageForDisplay(new String[] { folderPath }, new String[] { uid }, usmNoSave, setFrom);
            if (!mailMessage.containsAccountId()) {
                mailMessage.setAccountId(mailInterface.getAccountID());
            }

            if (null != csid) {
                CompositionSpace compositionSpace = CompositionSpace.getCompositionSpace(csid, session);
                compositionSpace.addForwardFor(new MailPath(folderPath, uid));


                AJAXRequestResult result = new AJAXRequestResult(mailMessage, "mail");
                result.setParameter("csid", csid);
                return result;
            }

            return new AJAXRequestResult(mailMessage, "mail");
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private AJAXRequestResult performPut(final MailRequest req, final JSONArray paths) throws OXException {
        try {
            ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            int length = paths.length();
            if (length <= 0) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            List<MailPath> forwardFors = new LinkedList<MailPath>();
            String[] folders = new String[length];
            String[] ids = new String[length];
            for (int i = 0; i < length; i++) {
                JSONObject folderAndID = paths.getJSONObject(i);
                folders[i] = folderAndID.getString(AJAXServlet.PARAMETER_FOLDERID);
                ids[i] = folderAndID.getString(AJAXServlet.PARAMETER_ID);
                forwardFors.add(new MailPath(folders[i], ids[i]));
            }
            String view = req.getParameter(Mail.PARAMETER_VIEW);
            String csid = req.getParameter(AJAXServlet.PARAMETER_CSID);
            UserSettingMail usmNoSave = session.getUserSettingMail().clone();
            /*
             * Deny saving for this request-specific settings
             */
            usmNoSave.setNoSave(true);
            /*
             * Overwrite settings with request's parameters
             */
            if (null != view) {
                if (VIEW_TEXT.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(false);
                } else if (VIEW_HTML.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(true);
                } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
                    usmNoSave.setDisplayHtmlInlineContent(true);
                    usmNoSave.setAllowHTMLImages(false);
                } else {
                    LOG.warn("Unknown value in parameter {}: {}. Using user's mail settings as fallback.", Mail.PARAMETER_VIEW, view);
                }
            }
            boolean setFrom = AJAXRequestDataTools.parseBoolParameter(req.getParameter("setFrom"));
            MailServletInterface mailInterface = getMailInterface(req);
            MailMessage mail = mailInterface.getForwardMessageForDisplay(folders, ids, usmNoSave, setFrom);
            if (!mail.containsAccountId()) {
                mail.setAccountId(mailInterface.getAccountID());
            }

            if (null != csid) {
                CompositionSpace compositionSpace = CompositionSpace.getCompositionSpace(csid, session);
                for (MailPath forwardFor : forwardFors) {
                    compositionSpace.addForwardFor(forwardFor);
                }


                AJAXRequestResult result = new AJAXRequestResult(mail, "mail");
                result.setParameter("csid", csid);
                return result;
            }

            return new AJAXRequestResult(mail, "mail");
        } catch (final OXException e) {
            final String uid = getUidFromException(e);
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e) && "undefined".equalsIgnoreCase(uid)) {
                throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
            }
            throw e;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
