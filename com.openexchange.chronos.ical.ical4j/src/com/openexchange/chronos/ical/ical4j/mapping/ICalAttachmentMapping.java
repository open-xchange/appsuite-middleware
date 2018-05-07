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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.mapping.event.AttachmentMapping;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.parameter.FmtType;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attach;

/**
 * {@link AttachmentMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalAttachmentMapping<T extends CalendarComponent, U> extends AbstractICalMapping<T, U> {

    /**
     * Initializes a new {@link ICalAttendeeMapping}.
     */
    protected ICalAttachmentMapping() {
        super();
    }

    protected abstract List<Attachment> getValue(U object);

    protected abstract void setValue(U object, List<Attachment> value);


    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        List<Attachment> attachments = getValue(object);
        if (null == attachments || 0 == attachments.size()) {
            removeProperties(component, Property.ATTACH);
        } else {
            removeProperties(component, Property.ATTACH); //TODO: merge?
            for (Attachment attachment : attachments) {
                try {
                    component.getProperties().add(exportAttachment(attachment, warnings));
                } catch (URISyntaxException | OXException e) {
                    addConversionWarning(warnings, e, Property.ATTACH, e.getMessage());
                }
            }
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        PropertyList properties = component.getProperties(Property.ATTACH);
        if (null != properties && 0 < properties.size()) {
            List<Attachment> attachments = new ArrayList<Attachment>(properties.size());
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                Attach property = (Attach) iterator.next();
                try {
                    attachments.add(importAttachment(property, warnings));
                } catch (OXException e) {
                    addConversionWarning(warnings, e, Property.ATTACH, e.getMessage());
                }
            }
            setValue(object, attachments);
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            setValue(object, null);
        }
    }

    private Attach exportAttachment(Attachment attachment, List<OXException> warnings) throws URISyntaxException, OXException {
        Attach property = new Attach();
        if (null != attachment.getData()) {
            InputStream inputStream = null;
            try {
                inputStream = attachment.getData().getStream();
                property.setBinary(Streams.stream2bytes(inputStream));
            } catch (IOException e) {
                addConversionWarning(warnings, e, Property.ATTACH, e.getMessage());
            } finally {
                Streams.close(inputStream);
            }
        } else if (null != attachment.getUri()) {
            property.setUri(new URI(attachment.getUri()));
        }
        if (null != attachment.getFilename()) {
            property.getParameters().add(new XParameter("FILENAME", attachment.getFilename()));
        }
        if (null != attachment.getFormatType()) {
            property.getParameters().add(new FmtType(attachment.getFormatType()));
        }
        if (0 < attachment.getSize()) {
            property.getParameters().add(new XParameter("SIZE", String.valueOf(attachment.getSize())));
        }
        if (0 < attachment.getManagedId()) {
            property.getParameters().add(new XParameter("MANAGED-ID", String.valueOf(attachment.getManagedId())));
        }
        return property;
    }

    private Attachment importAttachment(Attach property, List<OXException> warnings) throws OXException {
        Attachment attachment = new Attachment();
        if (null != property.getBinary()) {
            ThresholdFileHolder fileHolder = new ThresholdFileHolder();
            try {
                fileHolder.write(property.getBinary());
                attachment.setData(fileHolder);
                property.setBinary(null);
                fileHolder = null;
            } finally {
                Streams.close(fileHolder);
            }
        } else if (null != property.getUri()) {
            attachment.setUri(property.getUri().toString());
        }
        attachment.setFilename(extractFilename(property));
        Parameter fmtTypeParameter = property.getParameter(Parameter.FMTTYPE);
        attachment.setFormatType(null != fmtTypeParameter ? fmtTypeParameter.getValue() : MimeType2ExtMap.getContentType(attachment.getFilename(), "application/octet-stream"));
        Parameter managedIdParameter = property.getParameter("MANAGED-ID");
        if (null != managedIdParameter) {
            try {
                attachment.setManagedId(Integer.parseInt(managedIdParameter.getValue()));
            } catch (NumberFormatException e) {
                addConversionWarning(warnings, e, Property.ATTACH, "Error parsing managed id");
            }
        }
        Parameter sizeParameter = property.getParameter("SIZE");
        if (null != sizeParameter) {
            try {
                attachment.setSize(Long.parseLong(sizeParameter.getValue()));
            } catch (NumberFormatException e) {
                addConversionWarning(warnings, e, Property.ATTACH, "Error parsing attachment size");
            }
        }
        return attachment;
    }

    private static String extractFilename(net.fortuna.ical4j.model.property.Attach attach) {
        for (String parameterName : new String[] { "FILENAME", "X-FILENAME", "X-ORACLE-FILENAME", "X-APPLE-FILENAME" }) {
            Parameter filenameParameter = attach.getParameter(parameterName);
            if (null != filenameParameter && Strings.isNotEmpty(filenameParameter.getValue())) {
                return filenameParameter.getValue();
            }
        }
        if (null != attach.getUri()) {
            String path = attach.getUri().getPath();
            if (Strings.isNotEmpty(path)) {
                int idx = path.lastIndexOf('/');
                if (-1 != idx) {
                    return path.substring(idx);
                }
            }
        }
        return "attachment";
    }

}
