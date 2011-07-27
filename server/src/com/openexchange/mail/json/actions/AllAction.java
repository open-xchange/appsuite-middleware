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

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllAction extends AbstractMailAction {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(AllAction.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * Initializes a new {@link AllAction}.
     * @param services
     */
    public AllAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        
        
        try {
            final AJAXRequestData request = req.getRequest();
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderId = request.checkParameter(Mail.PARAMETER_MAILFOLDER);
            final int[] columns = req.checkIntArray(Mail.PARAMETER_COLUMNS);
            final String sort = request.getParameter(Mail.PARAMETER_SORT);
            final String order = request.getParameter(Mail.PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(Mail.PARAMETER_ORDER);
            }
            final int[] fromToIndices;
            {
                final int leftHandLimit = req.optInt(Mail.LEFT_HAND_LIMIT);
                final int rightHandLimit = req.optInt(Mail.RIGHT_HAND_LIMIT);
                if (leftHandLimit == MailRequest.NOT_FOUND || rightHandLimit == MailRequest.NOT_FOUND) {
                    fromToIndices = null;
                } else {
                    fromToIndices = new int[] { leftHandLimit, rightHandLimit };
                }
            }
            /*
             * Get mail interface
             */
            MailServletInterface mailInterface = request.getState().optProperty(PROPERTY_MAIL_IFACE);
            if (mailInterface == null) {
                mailInterface = MailServletInterface.getInstance(session);
                request.getState().putProperty(PROPERTY_MAIL_IFACE, mailInterface);
            }
            /*
             * Pre-Select field writers
             */
            final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
            final int userId = session.getUserId();
            final int contextId = session.getContextId();
            int orderDir = OrderDirection.ASC.getOrder();
            if (order != null) {
                if (order.equalsIgnoreCase("asc")) {
                    orderDir = OrderDirection.ASC.getOrder();
                } else if (order.equalsIgnoreCase("desc")) {
                    orderDir = OrderDirection.DESC.getOrder();
                } else {
                    throw MailExceptionCode.INVALID_INT_VALUE.create(Mail.PARAMETER_ORDER);
                }
            }
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            /*
             * Start response
             */
            final long start = DEBUG ? System.currentTimeMillis() : 0L;
            jsonWriter.array();
            SearchIterator<MailMessage> it = null;
            try {
                /*
                 * Check for thread-sort
                 */
                if (("thread".equalsIgnoreCase(sort))) {
                    it =
                        mailInterface.getAllThreadedMessages(
                            folderId,
                            MailSortField.RECEIVED_DATE.getField(),
                            orderDir,
                            columns,
                            fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = it.next();
                        final JSONArray ja = new JSONArray();
                        if (mail != null) {
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, mail.getThreadLevel(), false, mailInterface.getAccountID(), userId, contextId);
                            }
   
                        }
                        jsonWriter.value(ja);
                    }
                } else {
                    final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                    /*
                     * Get iterator
                     */
                    it = mailInterface.getAllMessages(folderId, sortCol, orderDir, columns, fromToIndices);
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailMessage mail = it.next();
                        final JSONArray ja = new JSONArray();
                        if (mail != null) {
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId);
                            }
                        }
                        jsonWriter.value(ja);
                    }
                }
            } finally {
                if (null != it) {
                    it.close();
                }
            }
            /*
             * Close response and flush print writer
             */
            jsonWriter.endArray();
            if (DEBUG) {
                final long d = System.currentTimeMillis() - start;
                LOG.debug(new StringBuilder(32).append("/ajax/mail?action=all performed in ").append(d).append("msec"));
            }
            return new AJAXRequestResult(jsonWriter.getObject(), "json");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
