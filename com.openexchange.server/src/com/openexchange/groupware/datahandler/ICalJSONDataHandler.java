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

package com.openexchange.groupware.datahandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DefaultConverter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.ResultConverterRegistry;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataProperties;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ICalJSONDataHandler} - The data handler return JSON appointments and JSON tasks parsed from an ICal input stream.
 * <p>
 * Supported arguments:
 * <ul>
 * <li>com.openexchange.groupware.calendar.timezone<br>
 * (optional; default is session user's time zone)</li>
 * <li>com.openexchange.groupware.calendar.recurrencePosition<br>
 * (optional; default is zero)</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ICalJSONDataHandler implements DataHandler {

    private static final Class<?>[] TYPES = { InputStream.class };

    private static final String[] ARGS = new String[0];

    /**
     * Initializes a new {@link ICalJSONDataHandler}
     */
    public ICalJSONDataHandler() {
        super();
    }

    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

    @Override
    public ConversionResult processData(final Data<? extends Object> data, final DataArguments dataArguments, final Session session) throws OXException {
        final Context ctx = ContextStorage.getStorageContext(session);
        final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
        if (iCalParser == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ICalParser.class.getName());
        }
        final List<Event> events;
        final List<Task> tasks;
        final InputStreamCopy inputStreamCopy;
        {
            long size;
            try {
                size = Long.parseLong(data.getDataProperties().get(DataProperties.PROPERTY_SIZE));
            } catch (final NumberFormatException e) {
                size = 0;
            }
            inputStreamCopy = copyStream((InputStream) data.getData(), size);
        }
        /*
         * Get time zone
         */
        final TimeZone timeZone;
        {
            final String timeZoneId = dataArguments.get("com.openexchange.groupware.calendar.timezone");
            timeZone =
                TimeZoneUtils.getTimeZone(null == timeZoneId ? UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone() : timeZoneId);
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        try {
            /*
             * Errors and warnings
             */
            final List<ConversionError> conversionErrors = new ArrayList<ConversionError>(4);
            final List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(4);
            {
                /*
                 * Start parsing appointments
                 */
                InputStream stream = null;
                try {
                    ICalService iCalService = ServerServiceRegistry.getServize(ICalService.class, true);
                    stream = inputStreamCopy.getInputStream();
                    ImportedCalendar calendar = iCalService.importICal(stream, null);
                    events = calendar.getEvents();
                } finally {
                    if (null != stream) {
                        stream.close();
                    }
                }
                // TODO: Handle errors/warnings
                conversionErrors.clear();
                conversionWarnings.clear();
                /*
                 * Start parsing tasks
                 */
                tasks = parseTaskStream(ctx, iCalParser, inputStreamCopy, conversionErrors, conversionWarnings, timeZone);
            }
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            inputStreamCopy.close();
        }
        /*
         * The JSON array to return
         */
        final JSONArray objects = new JSONArray();
        ResultConverterRegistry converterRegistry = ServerServiceRegistry.getServize(ResultConverterRegistry.class, true);
        AJAXRequestData request = new AJAXRequestData();
        request.setSession(serverSession);
        /*
         * Fill JSON appointments
         */
        if (!events.isEmpty()) {
            /*
             * Insert parsed appointments into denoted calendar folder
             */
            ResultConverter resultConverter = converterRegistry.getResultConverter("eventDocument", "json");
            if (null != resultConverter) {
                for (Event event : events) {
                    AJAXRequestResult result = new AJAXRequestResult(event);
                    resultConverter.convert(request, result, serverSession, new DefaultConverter());
                    Object resultObject = result.getResultObject();
                    objects.put(resultObject);
                }
            }
        }
        /*
         * Fill JSON tasks
         */
        if (!tasks.isEmpty()) {
            /*
             * Insert parsed tasks into denoted task folder
             */
            ResultConverter resultConverter = converterRegistry.getResultConverter("task", "json");
            if (null != resultConverter) {
                for (final Task task : tasks) {
                    AJAXRequestResult result = new AJAXRequestResult(task);
                    resultConverter.convert(request, result, serverSession, new DefaultConverter());
                    Object resultObject = result.getResultObject();
                    objects.put(resultObject);
                }
            }
        }
        ConversionResult result = new ConversionResult();
        result.setData(objects);
        return result;
    }

    private List<Task> parseTaskStream(final Context ctx, final ICalParser iCalParser, final InputStreamCopy inputStreamCopy, final List<ConversionError> conversionErrors, final List<ConversionWarning> conversionWarnings, final TimeZone defaultZone) throws IOException, ConversionError {
        final InputStream inputStream = inputStreamCopy.getInputStream();
        try {
            return iCalParser.parseTasks(inputStream, defaultZone, ctx, conversionErrors, conversionWarnings).getImportedObjects();
        } finally {
            Streams.close(inputStream);
        }
    }

    private static final int LIMIT = 1048576;

    private static InputStreamCopy copyStream(final InputStream orig, final long size) throws OXException {
        try {
            return new InputStreamCopy(orig, (size <= 0 || size > LIMIT));
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static final class InputStreamCopy {

        private static final int DEFAULT_BUF_SIZE = 0x2000;

        private static final String FILE_PREFIX = "openexchange-icaljson-";

        private byte[] bytes;

        private File file;

        private final long size;

        public InputStreamCopy(final InputStream orig, final boolean createFile) throws IOException {
            super();
            if (createFile) {
                size = copy2File(orig);
            } else {
                size = copy2ByteArr(orig);
            }
        }

        public InputStream getInputStream() throws IOException {
            return bytes == null ? (file == null ? null : (new BufferedInputStream(new FileInputStream(file), DEFAULT_BUF_SIZE))) : (new UnsynchronizedByteArrayInputStream(
                bytes));
        }

        public void close() {
            if (file != null) {
                if (file.exists()) {
                    file.delete();
                }
                file = null;
            }
            if (bytes != null) {
                bytes = null;
            }
        }

        private int copy2ByteArr(final InputStream in) throws IOException {
            final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
            final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
            int len;
            while ((len = in.read(bbuf)) > 0) {
                out.write(bbuf, 0, len);
            }
            out.flush();
            this.bytes = out.toByteArray();
            out.close();
            return bytes.length;
        }
        
        @SuppressWarnings("unused")
        public long getSize() {
            return size;
        }

        private long copy2File(final InputStream in) throws IOException {
            long totalBytes = 0;
            {
                final File tmpFile =
                    File.createTempFile(FILE_PREFIX, null, new File(ServerConfig.getProperty(ServerConfig.Property.UploadDirectory)));
                tmpFile.deleteOnExit();
                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUF_SIZE);
                    final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
                    int len;
                    while ((len = in.read(bbuf)) > 0) {
                        out.write(bbuf, 0, len);
                        totalBytes += len;
                    }
                    out.flush();
                } finally {
                    Streams.close(out);
                }
                file = tmpFile;
            }
            return totalBytes;
        }
    }
}
