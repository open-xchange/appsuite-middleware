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

package com.openexchange.image;

import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ImageGetAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true, publicSessionAuth = true)
@RestrictedAction()
public class ImageGetAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageGetAction.class);

    /**
     * Initializes a new {@link ImageGetAction}.
     *
     * @param services
     */
    public ImageGetAction(ServiceLookup services) {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        // Check registration name
        String registrationName;
        {
            String serlvetRequestURI = requestData.getSerlvetRequestURI();
            if (null == serlvetRequestURI) {
                LOG.debug("Missing path information in image URL.");
                throw AjaxExceptionCodes.BAD_REQUEST.create("Unknown image location.");
            }

            ConcurrentMap<String, String> alias2regname = ImageActionFactory.alias2regName;
            registrationName = alias2regname.get(serlvetRequestURI);

            if (null == registrationName) {
                for (Entry<String, String> entry : alias2regname.entrySet()) {
                    String alias = entry.getKey();
                    if (serlvetRequestURI.contains(alias)) {
                        registrationName = entry.getValue();
                        break;
                    }
                }
            }

            if (registrationName == null) {
                LOG.debug("Request URI cannot be resolved to an image location: {}", serlvetRequestURI);
                throw AjaxExceptionCodes.BAD_REQUEST.create("Unknown image location.");
            }
        }

        // Parse path
        ImageDataSource dataSource = null;
        try {
            ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class, true);
            dataSource = (ImageDataSource) conversionService.getDataSource(registrationName);
        } catch (OXException e) {
            LOG.debug("Missing ConversionService reference.", e);
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        if (dataSource == null) {
            LOG.debug("Data source cannot be found for: {}", registrationName);
            throw AjaxExceptionCodes.BAD_REQUEST.create("Invalid image location.");
        }

        // Output image
        try {
            ImageLocation imageLocation = dataSource.parseRequest(requestData);
            AJAXRequestResult requestResult = new AJAXRequestResult();

            // Check for ETag headers
            String dataETag = dataSource.getETag(imageLocation, session);
            String clientETag = requestData.getHeader("If-None-Match");
            if (null != clientETag && clientETag.equals(dataETag)) {
                requestResult.setType(AJAXRequestResult.ResultType.ETAG);
                requestResult.setFormat("file");
                return requestResult;
            }
            requestResult = new AJAXRequestResult();
            obtainImageData(dataSource, imageLocation, session, requestResult);
            if (null != dataETag) {
                requestResult.setHeader("ETag", dataETag);
                requestResult.setExpires(Tools.getDefaultImageExpiry());
            }
            return requestResult;
        } catch (OXException e) {
            if (signalAsNotFound(e)) {
                throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(HttpServletResponse.SC_NOT_FOUND), e.getSoleMessage());
            }
            if (Category.CATEGORY_PERMISSION_DENIED == e.getCategory()) {
                throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(HttpServletResponse.SC_FORBIDDEN), e.getSoleMessage());
            }
            throw AjaxExceptionCodes.BAD_REQUEST.create(e, new Object[0]);
        } catch (IllegalArgumentException e) {
            LOG.warn("Retrieving image failed.", e);
            throw AjaxExceptionCodes.BAD_REQUEST.create(e, new Object[0]);
        }
    }

    private static final List<OXExceptionCode> NOT_FOUND_CODES = ImmutableList.<OXExceptionCode> builder()
        .add(ContactExceptionCodes.CONTACT_NOT_FOUND)
        .add(MailExceptionCode.IMAGE_ATTACHMENT_NOT_FOUND)
        .add(MailExceptionCode.ATTACHMENT_NOT_FOUND)
        .add(MailExceptionCode.MAIL_NOT_FOUND)
        .add(MailExceptionCode.REFERENCED_MAIL_NOT_FOUND)
        .add(MailExceptionCode.FOLDER_NOT_FOUND)
        .build();

    private static boolean signalAsNotFound(OXException e) {
        for (OXExceptionCode code : NOT_FOUND_CODES) {
            if (code.equals(e)) {
                return true;
            }
        }
        return false;
    }

    private static void obtainImageData(ImageDataSource dataSource, ImageLocation imageLocation, Session session, AJAXRequestResult requestResult) throws OXException {
        Data<InputStream> data = dataSource.getData(InputStream.class, dataSource.generateDataArgumentsFrom(imageLocation), session);

        DataProperties dataProperties = data.getDataProperties();
        String ct = dataProperties.get(DataProperties.PROPERTY_CONTENT_TYPE);
        String fileName = dataProperties.get(DataProperties.PROPERTY_NAME);
        long size = -1L;
        {
            String sSize = dataProperties.get(DataProperties.PROPERTY_SIZE);
            if (Strings.isNotEmpty(sSize)) {
                try {
                    size = Long.parseLong(sSize.trim());
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        InputStream in = data.getData();
        FileHolder fileHolder = new FileHolder(in, size, ct, fileName);
        fileHolder.setDelivery("view");
        requestResult.setResultObject(fileHolder, "file");
    }

}
