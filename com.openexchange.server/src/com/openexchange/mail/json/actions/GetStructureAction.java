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

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetStructureAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetStructureAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GetStructureAction.class);

    /**
     * Initializes a new {@link GetStructureAction}.
     *
     * @param services
     */
    public GetStructureAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            AJAXRequestResult data = getJSONNullResult();
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            // final String uid = paramContainer.checkStringParam(PARAMETER_ID);
            final boolean unseen;
            {
                final String tmp = req.getParameter(Mail.PARAMETER_UNSEEN);
                unseen = ("1".equals(tmp) || Boolean.parseBoolean(tmp));
            }
            final long maxSize;
            {
                final String tmp = req.getParameter("max_size");
                if (null == tmp) {
                    maxSize = -1;
                } else {
                    long l = -1;
                    try {
                        l = Long.parseLong(tmp.trim());
                    } catch (final NumberFormatException e) {
                        l = -1;
                    }
                    maxSize = l;
                }
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            final String uid;
            {
                String tmp2 = req.getParameter(AJAXServlet.PARAMETER_ID);
                if (null == tmp2) {
                    tmp2 = req.getParameter(Mail.PARAMETER_MESSAGE_ID);
                    if (null == tmp2) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
                    }
                    uid = mailInterface.getMailIDByMessageID(folderPath, tmp2);
                } else {
                    uid = tmp2;
                }
            }
            if (isEmpty(uid)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
            }
            /*
             * Get message
             */
            final MailMessage mail = mailInterface.getMessage(folderPath, uid, !unseen);
            if (mail == null) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(uid, folderPath);
            }
            if (!mail.containsAccountId()) {
                mail.setAccountId(mailInterface.getAccountID());
            }
            final boolean wasUnseen = (unseen ? !mail.isSeen() : mail.containsPrevSeen() && !mail.isPrevSeen());
            if (wasUnseen) {
                try {
                    final ServerUserSetting setting = ServerUserSetting.getInstance();
                    final int contextId = session.getContextId();
                    final int userId = session.getUserId();
                    if (setting.isContactCollectOnMailAccess(contextId, userId).booleanValue()) {
                        triggerContactCollector(session, mail, false);
                    }
                } catch (final OXException e) {
                    LOG.warn("Contact collector could not be triggered.", e);
                }
            }
            data = new AJAXRequestResult(MessageWriter.writeStructure(mailInterface.getAccountID(), mail, maxSize), "json");
            return data;
        } catch (final OXException e) {
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e)) {
                LOG.warn("Requested mail could not be found. Most likely this is caused by concurrent access of multiple clients while one performed a delete on affected mail.",
                    e);
                try {
                    final String uid = getUidFromException(e);
                    if ("undefined".equalsIgnoreCase(uid)) {
                        throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
                    }
                } catch (final Exception x) {
                    // ignore
                }
            } else {
                LOG.error("", e);
            }
            throw e;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
