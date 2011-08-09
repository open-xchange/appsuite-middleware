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

package com.openexchange.mail.json.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailActionConstants;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailConverter}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailConverter implements ResultConverter, MailActionConstants {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(MailConverter.class));

    /**
     * Initializes a new {@link MailConverter}.
     */
    public MailConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "mail";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(final AJAXRequestData request, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final Object resultObject = result.getResultObject();
        if (null == resultObject) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Result object is null.");
            }
            result.setResultObject(JSONObject.NULL, "json");
            return;
        }
        if (resultObject instanceof MailMessage) {
            convertSingle((MailMessage) resultObject, request, result, session, converter);
        } else {
            // TODO: Convert multiple
            @SuppressWarnings("unchecked")
            final Collection<MailMessage> collection = (Collection<MailMessage>) resultObject;
            
        }
    }

    private void convertSingle(final MailMessage mail, final AJAXRequestData request, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        String tmp = request.getParameter(Mail.PARAMETER_EDIT_DRAFT);
        final boolean editDraft = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
        tmp = request.getParameter(Mail.PARAMETER_VIEW);
        final String view = null == tmp ? null : tmp.toLowerCase(Locale.ENGLISH);
        tmp = request.getParameter(Mail.PARAMETER_UNSEEN);
        final boolean unseen = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = request.getParameter("token");
        final boolean token = (tmp != null && ("1".equals(tmp) || Boolean.parseBoolean(tmp)));
        tmp = request.getParameter("ttlMillis");
        int ttlMillis;
        try {
            ttlMillis = (tmp == null ? -1 : Integer.parseInt(tmp.trim()));
        } catch (final NumberFormatException e) {
            ttlMillis = -1;
        }
        tmp = null;
        final UserSettingMail usmNoSave = (UserSettingMail) session.getUserSettingMail().clone();
        /*
         * Deny saving for this request-specific settings
         */
        usmNoSave.setNoSave(true);
        /*
         * Overwrite settings with request's parameters
         */
        final DisplayMode displayMode = AbstractMailAction.detectDisplayMode(editDraft, view, usmNoSave);
        final String folderPath = request.checkParameter(Mail.PARAMETER_FOLDERID);
        /*
         * Check for possible unseen action
         */
        final boolean wasUnseen = (mail.containsPrevSeen() && !mail.isPrevSeen());
        final boolean doUnseen = (unseen && wasUnseen);
        if (doUnseen) {
            mail.setFlag(MailMessage.FLAG_SEEN, false);
            final int unreadMsgs = mail.getUnreadMessages();
            mail.setUnreadMessages(unreadMsgs < 0 ? 0 : unreadMsgs + 1);
        }
        final MailServletInterface mailInterface = getMailInterface(request, session);
        final List<OXException> warnings = new ArrayList<OXException>(2);
        result.setResultObject(
            MessageWriter.writeMailMessage(mailInterface.getAccountID(), mail, displayMode, session, usmNoSave, warnings, token, ttlMillis), "json");
        if (doUnseen) {
            /*-
             * Leave mail as unseen
             * 
             * Determine mail identifier
             */
            final String uid;
            {
                String tmp2 = request.getParameter(Mail.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = request.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(Mail.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }
            mailInterface.updateMessageFlags(folderPath, new String[] { uid }, MailMessage.FLAG_SEEN, false);
        } else if (wasUnseen) {
            try {
                final ServerUserSetting setting = ServerUserSetting.getInstance();
                final int contextId = session.getContextId();
                final int userId = session.getUserId();
                if (setting.isContactCollectionEnabled(contextId, userId).booleanValue() && setting.isContactCollectOnMailAccess(
                    contextId,
                    userId).booleanValue()) {
                    AbstractMailAction.triggerContactCollector(session, mail);
                }
            } catch (final OXException e) {
                LOG.warn("Contact collector could not be triggered.", e);
            }
        }

    }

    private MailServletInterface getMailInterface(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Get mail interface
         */
        MailServletInterface mailInterface = request.getState().optProperty(PROPERTY_MAIL_IFACE);
        if (mailInterface == null) {
            mailInterface = MailServletInterface.getInstance(session);
            request.getState().putProperty(PROPERTY_MAIL_IFACE, mailInterface);
        }
        return mailInterface;
    }

}
