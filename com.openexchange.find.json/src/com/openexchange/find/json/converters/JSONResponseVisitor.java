/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.find.drive.FolderDocument;
import com.openexchange.find.json.QueryResult;
import com.openexchange.find.json.osgi.ResultConverterRegistry;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.tasks.TasksDocument;
import com.openexchange.java.Strings;
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
        MailMessage mailMessage = mailDocument.getMailMessage();
        if (null == mailMessage) {
            // No mail available...
            return;
        }

        if (mailFieldWriters == null) {
            String[] columns = queryResult.getSearchRequest().getColumns().getOriginalColumns();
            if (columns == null || columns.length == 0) {
                mailFieldWriters = new ArrayList<MailFieldWriter>(Arrays.asList(DEFAULT_MAIL_WRITERS));
            } else {
                mailFieldWriters = new ArrayList<MailFieldWriter>(columns.length);
                for (String c : columns) {
                    if (Strings.isNotEmpty(c)) {
                        MailFieldWriter mailFieldWriter;
                        int field = Strings.getUnsignedInt(c.trim());
                        if (field > 0) {
                            mailFieldWriter = MessageWriter.getMailFieldWriter(MailListField.getField(field));
                        } else {
                            mailFieldWriter = MessageWriter.getHeaderFieldWriter(c.trim());
                        }
                        if (mailFieldWriter != null) {
                            mailFieldWriters.add(mailFieldWriter);
                        }
                    }
                }
            }
        }

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
            ResultConverter calendarConverter = converterRegistry.getConverter(calendarDocument.getFormat());
            if (calendarConverter != null) {
                AJAXRequestData requestData = this.requestData.copyOf();
                if (timeZone != null) {
                    requestData.putParameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID());
                }
                AJAXRequestResult requestResult = new AJAXRequestResult(calendarDocument.getObject());
                calendarConverter.convert(requestData, requestResult, session, null);
                json.put(requestResult.getResultObject());
            }
        } catch (OXException e) {
            LOG.warn("Could not write document to response. It will be ignored.", e);
            errors.add(e);
        }
    }

    @Override
    public void visit(FolderDocument folderDocument) {
        try {
            ResultConverter converter = converterRegistry.getConverter("folder");
            if (null != converter) {
                AJAXRequestData requestData = this.requestData.copyOf();
                if (timeZone != null) {
                    requestData.putParameter(AJAXServlet.PARAMETER_TIMEZONE, timeZone.getID());
                }
                AJAXRequestResult requestResult = new AJAXRequestResult(folderDocument.getFolder());
                converter.convert(requestData, requestResult, session, null);
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
