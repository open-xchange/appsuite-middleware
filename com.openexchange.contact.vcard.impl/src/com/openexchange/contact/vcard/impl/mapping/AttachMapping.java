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

package com.openexchange.contact.vcard.impl.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import ezvcard.VCard;
import ezvcard.property.RawProperty;

/**
 * {@link AttachMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AttachMapping extends AbstractMapping {

    private static final String PROPERTY_MANAGED_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.managedAttachments";
    private static final String PROPERTY_BINARY_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.binaryAttachments";
    private static final String PROPERTY_LINKED_ATTACHMENTS = "com.openexchange.data.conversion.ical.attach.linkedAttachments";

    /**
     * Initializes a new {@link AttachMapping}.
     */
    public AttachMapping() {
        super("ATTACH", ContactField.NUMBER_OF_ATTACHMENTS, ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        List<Entry<URI, AttachmentMetadata>> managedAttachments = contact.getProperty(PROPERTY_MANAGED_ATTACHMENTS);
        if (null != managedAttachments && 0 < managedAttachments.size()) {
            for (Entry<URI, AttachmentMetadata> managedAttachment : managedAttachments) {
                RawProperty attachProperty = new RawProperty("ATTACH", managedAttachment.getKey().toString());
                AttachmentMetadata metadata = managedAttachment.getValue();
                attachProperty.addParameter("FILENAME", metadata.getFilename());
                attachProperty.addParameter("X-ORACLE-FILENAME", metadata.getFilename());
                attachProperty.addParameter("X-APPLE-FILENAME", metadata.getFilename());
                attachProperty.addParameter("FMTTYPE", metadata.getFileMIMEType());
                attachProperty.addParameter("SIZE", String.valueOf(metadata.getFilesize()));
                attachProperty.addParameter("MANAGED-ID", String.valueOf(metadata.getId()));
                vCard.addProperty(attachProperty);
            }
        }

        List<IFileHolder> binaryAttachments = contact.getProperty(PROPERTY_BINARY_ATTACHMENTS);
        if (null != binaryAttachments && !binaryAttachments.isEmpty()) {
            for (IFileHolder attachment : binaryAttachments) {
                try {
                    // Encode to base64 string
                    String value = java.util.Base64.getEncoder().encodeToString(Streams.stream2bytes(attachment.getStream()));
                    RawProperty attachProperty = new RawProperty("ATTACH", value);

                    // Add other properties
                    attachProperty.addParameter("ENCODING", "BASE64");
                    attachProperty.addParameter("VALUE", "BINARY");
                    String fileName = attachment.getName();
                    if (Strings.isNotEmpty(fileName)) {
                        attachProperty.addParameter("FILENAME", fileName);
                        attachProperty.addParameter("X-ORACLE-FILENAME", fileName);
                        attachProperty.addParameter("X-APPLE-FILENAME", fileName);
                    }
                    String contentType = attachment.getContentType();
                    if (Strings.isNotEmpty(contentType)) {
                        attachProperty.addParameter("FMTTYPE", contentType);
                    }
                    long length = attachment.getLength();
                    if (length >= 0) {
                        attachProperty.addParameter("SIZE", String.valueOf(length));
                    }
                    vCard.addProperty(attachProperty);
                } catch (IOException e) {
                    addConversionWarning(warnings, e, "ATTACH", "Failed to read binary attachment");
                } catch (OXException e) {
                    addConversionWarning(warnings, e, "ATTACH", e.getMessage());
                }
            }
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        if (null == parameters || false == parameters.isImportAttachments()) {
            return;
        }
        List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
        List<IFileHolder> binaryAttachments = new ArrayList<IFileHolder>();
        List<String> linkedAttachments = new ArrayList<String>();
        List<RawProperty> attachProperties = vCard.getExtendedProperties("ATTACH");
        if (null != attachProperties && 0 < attachProperties.size()) {
            for (RawProperty attachProperty : attachProperties) {
                if (parameters.isKeepOriginalVCard() && parameters.isRemoveAttachmentsFromKeptVCard()) {
                    vCard.removeProperty(attachProperty);
                }
                ezvcard.parameter.VCardParameters vCardParameters = attachProperty.getParameters();
                if (null != vCardParameters.getEncoding()) {
                    /*
                     * decode as binary attachment
                     */
                    IFileHolder binaryAttachment = parseBinaryAttachment(attachProperty, warnings);
                    if (null != binaryAttachment) {
                        binaryAttachments.add(binaryAttachment);
                    }
                    continue;
                }
                List<String> managedIdParameters = vCardParameters.get("MANAGED-ID");
                if (null != managedIdParameters && 0 < managedIdParameters.size()) {
                    /*
                     * decode as managed attachment
                     */
                    Entry<URI, AttachmentMetadata> managedAttachment = parseManagedAttachment(attachProperty, warnings);
                    if (null != managedAttachment) {
                        managedAttachments.add(managedAttachment);
                    }
                    continue;
                }
                /*
                 * add as plain attachment link
                 */
                linkedAttachments.add(attachProperty.getValue());
            }
        }
        /*
         * apply properties
         */
        contact.setProperty(PROPERTY_MANAGED_ATTACHMENTS, managedAttachments);
        contact.setProperty(PROPERTY_BINARY_ATTACHMENTS, binaryAttachments);
        contact.setProperty(PROPERTY_LINKED_ATTACHMENTS, linkedAttachments);
    }

    private IFileHolder parseBinaryAttachment(RawProperty attachProperty, List<OXException> warnings) {
        String value = attachProperty.getValue();
        if (null == value) {
            addConversionWarning(warnings, "ATTACH", "No value for binary attachment");
            return null;
        }
        String fmttype = attachProperty.getParameter("FMTTYPE");
        String filename = extractFilename(attachProperty);
        String contentType = null != fmttype ? fmttype : MimeType2ExtMap.getContentType(filename, "application/octet-stream");
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        StringReader reader = null;
        InputStream inputStream = null;
        try {
            reader = new StringReader(value);
            inputStream = BaseEncoding.base64().decodingStream(reader);
            fileHolder.write(inputStream);
        } catch (OXException e) {
            Streams.close(fileHolder);
            addConversionWarning(warnings, e, "ATTACH", e.getMessage());
        } finally {
            Streams.close(inputStream, reader);
        }
        /*
         * store additional metadata in fileholder
         */
        fileHolder.setContentType(contentType);
        fileHolder.setName(filename);
        return fileHolder;
    }

    private Entry<URI, AttachmentMetadata> parseManagedAttachment(RawProperty attachProperty, List<OXException> warnings) {
        AttachmentMetadata metadata = new AttachmentMetadataFactory().newAttachmentMetadata();
        URI uri;
        try {
            uri = new URI(attachProperty.getValue());
        } catch (URISyntaxException e) {
            addConversionWarning(warnings, e, "ATTACH", "Invalid URI");
            return null;
        }
        String managedId = attachProperty.getParameter("MANAGED-ID");
        if (Strings.isNotEmpty(managedId)) {
            try {
                metadata.setId(Integer.parseInt(managedId));
            } catch (NumberFormatException e) {
                addConversionWarning(warnings, e, "ATTACH", "Invalid managed ID");
            }
        }
        metadata.setFileMIMEType(attachProperty.getParameter("FMTTYPE"));
        metadata.setFilename(extractFilename(attachProperty));
        String size = attachProperty.getParameter("SIZE");
        if (null != size) {
            try {
                metadata.setFilesize(Long.parseLong(size));
            } catch (NumberFormatException e) {
                addConversionWarning(warnings, e, "ATTACH", "Invalid size");
            }
        }
        return new AbstractMap.SimpleEntry<URI, AttachmentMetadata>(uri, metadata);
    }

    private String extractFilename(RawProperty attachProperty) {
        for (String parameterName : new String[] { "FILENAME", "X-FILENAME", "X-ORACLE-FILENAME", "X-APPLE-FILENAME" }) {
            String filename = attachProperty.getParameter(parameterName);
            if (Strings.isNotEmpty(filename)) {
                return filename;
            }
        }
        return "attachment";
    }

}
