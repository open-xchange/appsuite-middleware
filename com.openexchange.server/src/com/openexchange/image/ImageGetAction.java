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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ImageGetAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public class ImageGetAction implements AJAXActionService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ImageGetAction.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

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
        String registrationName = null;
        {
            final String serlvetRequestURI = requestData.getSerlvetRequestURI();
            if (null == serlvetRequestURI) {
                if (DEBUG) {
                    LOG.debug("Missing path information in image URL.");
                }
                throw AjaxExceptionCodes.BAD_REQUEST.create("Unknown image location.");
            }
            for (Entry<String, String> entry : ImageActionFactory.alias2regName.entrySet()) {
                String alias = entry.getKey();
                if (serlvetRequestURI.contains(alias)) {
                    registrationName = entry.getValue();
                    break;
                }
            }
            if (registrationName == null) {
                if (DEBUG) {
                    LOG.debug("Request URI cannot be resolved to an image location: " + serlvetRequestURI);
                }
                throw AjaxExceptionCodes.BAD_REQUEST.create("Unknown image location.");
            }
        }
        // Parse path
        ImageDataSource dataSource = null;
        try {
            ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class, true);
            dataSource = (ImageDataSource) conversionService.getDataSource(registrationName);
        } catch (OXException e) {
            if (DEBUG) {
                LOG.debug("Missing ConversionService reference.", e);
            }
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        if (dataSource == null) {
            if (DEBUG) {
                LOG.debug("Data source cannot be found for: " + registrationName);
            }
            throw AjaxExceptionCodes.BAD_REQUEST.create("Invalid image location.");
        }
        ImageLocation imageLocation = dataSource.parseRequest(requestData);

        /*
         * Output image
         */
        AJAXRequestResult requestResult = new AJAXRequestResult();
        try {
            /*
             * Check for ETag headers
             */
            String eTag = requestData.getHeader("If-None-Match");
            if (null != eTag && dataSource.getETag(imageLocation, session).equals(eTag)) {
                requestResult.setType(AJAXRequestResult.ResultType.ETAG);
                if (requestData.getExpires() > 0) {
                    requestResult.setExpires(requestData.getExpires());
                }
                return requestResult;
            }
            requestResult = new AJAXRequestResult();
            outputImageData(dataSource, imageLocation, session, requestResult);
        } catch (OXException e) {
            if (DEBUG) {
                LOG.debug("Writing image data failed.", e);
            }
            throw AjaxExceptionCodes.BAD_REQUEST.create(e, new Object[0]);
        }
        return requestResult;
    }

    private static void outputImageData(ImageDataSource dataSource, ImageLocation imageLocation, Session session, AJAXRequestResult requestResult) throws OXException {
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
