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
package com.openexchange.find.json.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.find.DocumentVisitor;
import com.openexchange.find.calendar.CalendarDocument;
import com.openexchange.find.contacts.ContactsDocument;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.json.QueryResult;
import com.openexchange.find.json.osgi.ResultConverterRegistry;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.tasks.TasksDocument;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JSONResponseVisitor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public class JSONResponseVisitor implements DocumentVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(JSONResponseVisitor.class);

    private static final MailFieldWriter[] DEFAULT_MAIL_WRITERS;
    static {
        final MailListField[] listFields = new MailListField[MailField.FIELDS_LOW_COST.length];
        for (int i = 0; i < MailField.FIELDS_LOW_COST.length; i++) {
            final MailField mailField = MailField.FIELDS_LOW_COST[i];
            listFields[i] = mailField.getListField();
        }
        DEFAULT_MAIL_WRITERS = MessageWriter.getMailFieldWriters(listFields);
    }

    // ------------------------------------------------------------------------------------------------- //

    private final ServerSession session;
    private final AJAXRequestData requestData;
    private final List<OXException> errors;
    private final JSONArray json;
    private final ResultConverterRegistry converterRegistry;
    private final QueryResult queryResult;
    private final TimeZone timeZone;

    /**
     * @param session The session; never <code>null</code>.
     * @param requestData The request data; never <code>null</code>.
     * @param converterRegistry The converter registry; never <code>null</code>.
     * @param queryResult The query result; never <code>null</code>.
     */
    public JSONResponseVisitor(ServerSession session, AJAXRequestData requestData, ResultConverterRegistry converterRegistry, QueryResult queryResult) {
        super();
        this.converterRegistry = converterRegistry;
        this.session = session;
        this.requestData = requestData;
        this.queryResult = queryResult;
        errors = new LinkedList<OXException>();
        json = new JSONArray();
        String timeZoneStr = queryResult.getSearchRequest().getOptions().getTimeZone();
        if (timeZoneStr != null) {
            timeZone = TimeZoneUtils.getTimeZone(timeZoneStr);
        } else {
            timeZone = TimeZoneUtils.getTimeZone(session.getUser().getTimeZone());
        }
    }

    private List<MailFieldWriter> mailFieldWriters = null;

    @Override
    public void visit(MailDocument mailDocument) {
        if (mailFieldWriters == null) {
            String[] columns = queryResult.getSearchRequest().getColumns().getOriginalColumns();
            if (columns == null || columns.length == 0) {
                mailFieldWriters = new ArrayList<MailFieldWriter>(Arrays.asList(DEFAULT_MAIL_WRITERS));
            } else {
                mailFieldWriters = new ArrayList<MailFieldWriter>(columns.length);
                for (String c : columns) {
                    try {
                        int ic = Integer.parseInt(c);
                        mailFieldWriters.add(MessageWriter.getMailFieldWriter(MailListField.getField(ic)));
                    } catch (NumberFormatException e) {
                        mailFieldWriters.add(MessageWriter.getHeaderFieldWriter(c));
                    }
                }
            }
        }

        final MailMessage mailMessage = mailDocument.getMailMessage();
        try {
            JSONObject jsonMessage = new JSONObject(mailFieldWriters.size());
            int contextId = session.getContextId();
            int userId = session.getUserId();
            for (MailFieldWriter writer : mailFieldWriters) {
                writer.writeField(
                    jsonMessage,
                    mailMessage,
                    0,
                    true,
                    mailMessage.getAccountId(),
                    userId,
                    contextId,
                    timeZone);
            }
            json.put(jsonMessage);
        } catch (OXException e) {
            LOG.warn("Could not write document to response. It will be ignored.", e);
            errors.add(e);
        }
    }

    @Override
    public void visit(FileDocument fileDocument) {
        try {
            ResultConverter converter = converterRegistry.getConverter("infostore");
            if (null != converter) {
                AJAXRequestData requestData = this.requestData.copyOf();
                if (timeZone != null) {
                    requestData.putParameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID());
                }

                AJAXRequestResult requestResult = new AJAXRequestResult(fileDocument.getFile());
                converter.convert(requestData, requestResult, session, null);
                json.put(requestResult.getResultObject());
            }
        } catch (OXException e) {
            LOG.warn("Could not write document to response. It will be ignored.", e);
            errors.add(e);
        }
    }

    @Override
    public void visit(TasksDocument taskDocument) {
        try {
            ResultConverter converter = converterRegistry.getConverter("task");
            if (null != converter) {
                AJAXRequestData requestData = this.requestData.copyOf();
                if (timeZone != null) {
                    requestData.putParameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID());
                }

                AJAXRequestResult requestResult = new AJAXRequestResult(taskDocument.getTask());
                converter.convert(requestData, requestResult, session, null);
                json.put(requestResult.getResultObject());
            }
        } catch (OXException e) {
            LOG.warn("Could not write document to response. It will be ignored.", e);
            errors.add(e);
        }
    }

    @Override
    public void visit(ContactsDocument contactDocument) {
        try {
            ResultConverter converter = converterRegistry.getConverter("contact");
            if (null != converter) {
                AJAXRequestData requestData = this.requestData.copyOf();
                if (timeZone != null) {
                    requestData.putParameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID());
                }

                AJAXRequestResult requestResult = new AJAXRequestResult(contactDocument.getContact());
                converter.convert(requestData, requestResult, session, null);
                json.put(requestResult.getResultObject());
            }
        } catch (OXException e) {
            LOG.warn("Could not write document to response. It will be ignored.", e);
            errors.add(e);
        }
    }

    @Override
    public void visit(CalendarDocument calendarDocument) {
        try {
            ResultConverter calendarConverter = converterRegistry.getConverter("appointment");
            if (calendarConverter != null) {
                AJAXRequestData requestData = this.requestData.copyOf();
                if (timeZone != null) {
                    requestData.putParameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID());
                }

                AJAXRequestResult requestResult = new AJAXRequestResult(calendarDocument.getAppointment());
                calendarConverter.convert(requestData, requestResult, session, null);
                json.put(requestResult.getResultObject());
            }
        } catch (OXException e) {
            LOG.warn("Could not write document to response. It will be ignored.", e);
            errors.add(e);
        }
    }

    /**
     * Gets the JSON array containing documents.
     *
     * @return The documents' JSON array
     */
    public JSONArray getJSONArray() {
        return json;
    }

    /**
     * Gets possible errors that occurred during conversion.
     *
     * @return The possible errors or an empty list
     */
    public List<OXException> getErrors() {
        return errors;
    }

}
