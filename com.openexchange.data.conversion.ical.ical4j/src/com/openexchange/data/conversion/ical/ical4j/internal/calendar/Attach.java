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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionWarning.Code;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.parameter.XParameter;

/**
 * {@link Attach}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class Attach<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T, U> {

    private static final String PROPERTY_MANAGED_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.managedAttachments";
    private static final String PROPERTY_BINARY_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.binaryAttachments";
    private static final String PROPERTY_LINKED_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.linkedAttachments";

    @Override
    public boolean isSet(U calendarObject) {
        if (calendarObject.containsAttachmentLink() && Strings.isNotEmpty(calendarObject.getAttachmentLink())) {
            return true;
        }
        if (null != calendarObject.getProperty(PROPERTY_MANAGED_ATTACHMENTS) ||
            null != calendarObject.getProperty(PROPERTY_BINARY_ATTACHMENTS) ||
            null != calendarObject.getProperty(PROPERTY_LINKED_ATTACHMENTS)) {
            return true;
        }
        return false;
    }

    @Override
    public void emit(Mode mode, int index, U calendarObject, T calendarComponent, List<ConversionWarning> warnings, Context ctx, Object... args) throws ConversionError {
        /*
         * apply managed attachments
         */
        List<Entry<URI, AttachmentMetadata>> managedAttachments = calendarObject.getProperty(PROPERTY_MANAGED_ATTACHMENTS);
        if (null != managedAttachments && 0 < managedAttachments.size()) {
            for (Entry<URI, AttachmentMetadata> managedAttachment : managedAttachments) {
                net.fortuna.ical4j.model.property.Attach attach = new net.fortuna.ical4j.model.property.Attach(managedAttachment.getKey());
                AttachmentMetadata metadata = managedAttachment.getValue();
                attach.getParameters().add(new XParameter("FILENAME", metadata.getFilename()));
                attach.getParameters().add(new XParameter("X-ORACLE-FILENAME", metadata.getFilename()));
                attach.getParameters().add(new XParameter("X-APPLE-FILENAME", metadata.getFilename()));
                attach.getParameters().add(new XParameter("FMTTYPE", metadata.getFileMIMEType()));
                attach.getParameters().add(new XParameter("SIZE", String.valueOf(metadata.getFilesize())));
                attach.getParameters().add(new XParameter("MANAGED-ID", String.valueOf(metadata.getId())));
                calendarComponent.getProperties().add(attach);
            }
        }
        List<IFileHolder> binaryAttachments = calendarObject.getProperty(PROPERTY_BINARY_ATTACHMENTS);
        if (null != binaryAttachments && !binaryAttachments.isEmpty()) {
            for (IFileHolder attachment : binaryAttachments) {
                try {
                    net.fortuna.ical4j.model.property.Attach attach = new net.fortuna.ical4j.model.property.Attach(Streams.stream2bytes(attachment.getStream()));
                    String fileName = attachment.getName();
                    if (Strings.isNotEmpty(fileName)) {
                        attach.getParameters().add(new XParameter("FILENAME", fileName));
                        attach.getParameters().add(new XParameter("X-ORACLE-FILENAME", fileName));
                        attach.getParameters().add(new XParameter("X-APPLE-FILENAME", fileName));
                    }
                    String contentType = attachment.getContentType();
                    if (Strings.isNotEmpty(contentType)) {
                        attach.getParameters().add(new XParameter("FMTTYPE", contentType));
                    }
                    long length = attachment.getLength();
                    if (length >= 0) {
                        attach.getParameters().add(new XParameter("SIZE", String.valueOf(length)));
                    }
                    calendarComponent.getProperties().add(attach);
                } catch (IOException e) {
                    warnings.add(new ConversionWarning(index, Code.PARSE_EXCEPTION, e, e.getMessage()));
                } catch (OXException e) {
                    warnings.add(new ConversionWarning(index, Code.PARSE_EXCEPTION, e, e.getMessage()));
                }
            }
        }
        /*
         * apply plain attachment link if present
         */
        if (calendarObject.containsAttachmentLink() && Strings.isNotEmpty(calendarObject.getAttachmentLink())) {
            try {
                calendarComponent.getProperties().add(new net.fortuna.ical4j.model.property.Attach(new URI(calendarObject.getAttachmentLink())));
            } catch (URISyntaxException e) {
                throw new ConversionError(index, Code.PARSE_EXCEPTION, e, "Invalid URI Syntax.");
            }
        }
    }

    @Override
    public boolean hasProperty(T calendarComponent) {
        return null != calendarComponent.getProperty(Property.ATTACH);
    }

    @Override
    public void parse(int index, T calendarComponent, U calendarObject, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
        // Parse all kinds of supported attachments
        List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
        List<IFileHolder> binaryAttachments = new ArrayList<IFileHolder>();
        List<String> linkedAttachments = new ArrayList<String>();
        PropertyList attachProperties = calendarComponent.getProperties(Property.ATTACH);
        if (null != attachProperties && 0 < attachProperties.size()) {
            for (Object property : attachProperties) {
                net.fortuna.ical4j.model.property.Attach attach = (net.fortuna.ical4j.model.property.Attach) property;
                Parameter encodingParameter = attach.getParameter(Parameter.ENCODING);
                if (null == encodingParameter) {
                    Parameter managedIdParameter = attach.getParameter("MANAGED-ID");
                    if (null == managedIdParameter) {
                        // Add as plain attachment link
                        linkedAttachments.add(attach.getValue());
                    } else {
                        // Decode as managed attachment
                        managedAttachments.add(parseManagedAttachment(index, attach, warnings));
                    }
                } else {
                    // Decode as binary attachment
                    IFileHolder binaryAttachment = parseBinaryAttachment(index, attach, warnings);
                    if (null != binaryAttachment) {
                        binaryAttachments.add(binaryAttachment);
                    }
                }
            }
        }

        // Apply properties
        calendarObject.setProperty(PROPERTY_MANAGED_ATTACHMENTS, managedAttachments);
        calendarObject.setProperty(PROPERTY_BINARY_ATTACHMENTS, binaryAttachments);
        calendarObject.setProperty(PROPERTY_LINKED_ATTACHMENTS, linkedAttachments);
        if (0 < linkedAttachments.size()) {
            calendarObject.setAttachmentLink(linkedAttachments.get(0));
        }
    }

    private Entry<URI, AttachmentMetadata> parseManagedAttachment(int index, net.fortuna.ical4j.model.property.Attach attach, List<ConversionWarning> warnings) {
        AttachmentMetadata metadata = new AttachmentMetadataFactory().newAttachmentMetadata();
        Parameter managedIdparameter = attach.getParameter("MANAGED-ID");
        if (null != managedIdparameter && Strings.isNotEmpty(managedIdparameter.getValue())) {
            try {
                metadata.setId(Integer.parseInt(managedIdparameter.getValue()));
            } catch (NumberFormatException e) {
                warnings.add(new ConversionWarning(index, Code.PARSE_EXCEPTION, e, "Invalid managed ID"));
            }
        }
        Parameter fmttypeParameter = attach.getParameter("FMTTYPE");
        if (null != fmttypeParameter) {
            metadata.setFileMIMEType(fmttypeParameter.getValue());
        }
        Parameter sizeParameter = attach.getParameter("SIZE");
        if (null != sizeParameter) {
            try {
                metadata.setFilesize(Long.parseLong(sizeParameter.getValue()));
            } catch (NumberFormatException e) {
                warnings.add(new ConversionWarning(index, Code.PARSE_EXCEPTION, e, "Invalid size"));
            }
        }
        return new AbstractMap.SimpleEntry<URI, AttachmentMetadata>(attach.getUri(), metadata);
    }

    private IFileHolder parseBinaryAttachment(int index, net.fortuna.ical4j.model.property.Attach attach, List<ConversionWarning> warnings) {
        /*
         * decode binary attachment
         */
        String value = attach.getValue();
        if (null == value) {
            warnings.add(new ConversionWarning(index, Code.PARSE_EXCEPTION, "No value for binary attachment"));
            return null;
        }
        Parameter fmttypeParameter = attach.getParameter(Parameter.FMTTYPE);
        String filename = extractFilename(attach);
        String contentType = null != fmttypeParameter ? fmttypeParameter.getValue() : MimeType2ExtMap.getContentType(filename, "application/octet-stream");
        ThresholdFileHolder fileHolder = null;
        StringReader reader = null;
        InputStream inputStream = null;
        try {
            reader = new StringReader(value);
            inputStream = BaseEncoding.base64().decodingStream(reader);
            fileHolder = new ThresholdFileHolder();
            fileHolder.write(inputStream);
            /*
             * store additional metadata in fileholder
             */
            fileHolder.setContentType(contentType);
            fileHolder.setName(filename);

            ThresholdFileHolder retval = fileHolder;
            fileHolder = null;
            return retval;
        } catch (OXException e) {
            warnings.add(new ConversionWarning(index, Code.PARSE_EXCEPTION, e, e.getMessage()));
        } finally {
            Streams.close(fileHolder, inputStream, reader);
        }

        return null;
    }

    private String extractFilename(net.fortuna.ical4j.model.property.Attach attach) {
        for (String parameterName : new String[] { "FILENAME", "X-FILENAME", "X-ORACLE-FILENAME", "X-APPLE-FILENAME" }) {
            Parameter filenameParameter = attach.getParameter(parameterName);
            if (null != filenameParameter && Strings.isNotEmpty(filenameParameter.getValue())) {
                return filenameParameter.getValue();
            }
        }
        return "attachment";
    }

}
