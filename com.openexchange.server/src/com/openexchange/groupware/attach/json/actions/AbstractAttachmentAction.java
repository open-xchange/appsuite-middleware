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

package com.openexchange.groupware.attach.json.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.parser.AttachmentParser;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.groupware.upload.impl.UploadSizeExceededException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractAttachmentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractAttachmentAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAttachmentAction.class);

    protected static final AttachmentParser PARSER = new AttachmentParser();

    protected static final AttachmentBase ATTACHMENT_BASE = Attachment.ATTACHMENT_BASE;

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final AtomicLong maxUploadSize;

    protected final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link AbstractAttachmentAction}.
     */
    protected AbstractAttachmentAction(final ServiceLookup serviceLookup) {
        super();
        maxUploadSize = new AtomicLong(-2L);
        this.serviceLookup = serviceLookup;
    }

    protected int requireNumber(final AJAXRequestData req, final String parameter) throws OXException {
        String value = req.getParameter(parameter);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw AttachmentExceptionCodes.INVALID_REQUEST_PARAMETER.create(nfe, parameter, value);
        }
    }

    protected static void require(final AJAXRequestData req, final String... parameters) throws OXException {
        for (String param : parameters) {
            if (req.getParameter(param) == null) {
                throw UploadException.UploadCode.MISSING_PARAM.create(param);
            }
        }
    }

    private static final String CALLBACK = "callback";

    /**
     * Checks current size of uploaded data against possible quota restrictions.
     *
     * @param size The size
     * @param requestData The associated request data
     * @throws OXException If any quota restrictions are exceeded
     */
    protected void checkSize(final long size, final AJAXRequestData requestData) throws OXException {
        if (maxUploadSize.get() == -2) {
            final long configuredSize = AttachmentConfig.getMaxUploadSize();
            long cur;
            do {
                cur = maxUploadSize.get();
            } while (!maxUploadSize.compareAndSet(cur, configuredSize));
        }

        long maxUploadSize = this.maxUploadSize.get();
        if (maxUploadSize == 0) {
            return;
        }

        if (size > maxUploadSize) {
            if (!requestData.containsParameter(CALLBACK)) {
                requestData.putParameter(CALLBACK, "error");
            }
            throw UploadSizeExceededException.create(size, maxUploadSize, true);
        }
    }

    protected void rollback() {
        try {
            Attachment.ATTACHMENT_BASE.rollback();
        } catch (Exception e) {
            LOG.debug("Rollback failed.", e);
        }
    }

    /**
     * Returns the value of the specified parameter as an integer array
     *
     * @param requestData The request data
     * @param name The name of the parameter
     * @return An integer list or <code>null</code> if the parameter is absent
     * @throws OXException if an invalid value is specified
     */
    protected List<Integer> optIntegerList(AJAXRequestData requestData, String name) throws OXException {
        String value = requestData.getParameter(name);
        if (value == null) {
            return null;
        }
        String[] array = Strings.splitByComma(value);
        List<Integer> retList = new LinkedList<>();
        for (int i = 0; i < array.length; i++) {
            try {
                retList.add(Integer.valueOf(array[i]));
            } catch (NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, name);
            }
        }
        return retList;
    }
}
