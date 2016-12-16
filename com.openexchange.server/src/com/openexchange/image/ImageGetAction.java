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
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.groupware.contact.ContactExceptionCodes;
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

        InputStream in = data.getData();
        FileHolder fileHolder = new FileHolder(in, -1, ct, fileName);
        fileHolder.setDelivery("view");
        requestResult.setResultObject(fileHolder, "file");
    }

}
