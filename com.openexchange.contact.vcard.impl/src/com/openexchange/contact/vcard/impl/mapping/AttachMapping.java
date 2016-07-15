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

package com.openexchange.contact.vcard.impl.mapping;

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
        super("ATTACH");
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
