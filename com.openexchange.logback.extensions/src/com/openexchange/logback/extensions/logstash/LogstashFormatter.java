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

package com.openexchange.logback.extensions.logstash;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.FastDateFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * {@link LogstashFormatter}. Formats {@link ILoggingEvent} objects as JSON objects.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class LogstashFormatter implements LifeCycle {

    private boolean isStarted;

    public static final FastDateFormat LOGSTASH_TIMEFORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    private List<CustomField> customFields;

    /**
     * Initialises a new {@link LogstashFormatter}.
     */
    public LogstashFormatter() {
        super();
        customFields = new ArrayList<CustomField>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.spi.LifeCycle#start()
     */
    @Override
    public void start() {
        isStarted = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.spi.LifeCycle#stop()
     */
    @Override
    public void stop() {
        isStarted = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.qos.logback.core.spi.LifeCycle#isStarted()
     */
    @Override
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Write the event as a JSON object to the stream
     *
     * @param event The logging event
     * @param outputStream The output stream
     * @throws IOException
     */
    public void writeToStream(ILoggingEvent event, OutputStream outputStream) throws IOException {
        JsonGenerator generator = new JsonFactory().createGenerator(outputStream);
        generator.configure(Feature.FLUSH_PASSED_TO_STREAM, false);
        generator.writeStartObject();

        generator.writeStringField(LogstashFieldName.timestamp.getLogstashName(), LOGSTASH_TIMEFORMAT.format(event.getTimeStamp()));
        generator.writeNumberField(LogstashFieldName.version.getLogstashName(), 1);

        // Logger
        generator.writeStringField(LogstashFieldName.level.getLogstashName(), event.getLevel().levelStr);
        generator.writeStringField(LogstashFieldName.loggerName.getLogstashName(), event.getLoggerName());

        // App specific
        generator.writeStringField(LogstashFieldName.threadName.getLogstashName(), event.getThreadName());
        generator.writeStringField(LogstashFieldName.message.getLogstashName(), event.getFormattedMessage());
        if (event.getMarker() != null) {
            generator.writeStringField(LogstashFieldName.marker.getLogstashName(), event.getMarker().getName());
        }

        // Stacktraces
        if (event.getThrowableProxy() != null) {
            generator.writeStringField(LogstashFieldName.stacktrace.getLogstashName(), ThrowableProxyUtil.asString(event.getThrowableProxy()));
        }

        // MDC
        Map<String, String> mdc = event.getMDCPropertyMap();
        for (String key : mdc.keySet()) {
            generator.writeFieldName(key);
            generator.writeObject(mdc.get(key));
        }

        for (CustomField customField : customFields) {
            generator.writeFieldName(customField.getKey());
            generator.writeObject(customField.getValue());
        }

        generator.writeEndObject();
        generator.flush();
    }

    /**
     * Adds the specified custom field
     *
     * @param customField the custom field to add
     */
    public void addCustomField(CustomField customField) {
        customFields.add(customField);
    }
}
