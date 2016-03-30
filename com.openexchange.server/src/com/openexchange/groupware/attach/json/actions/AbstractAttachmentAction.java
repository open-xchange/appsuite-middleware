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

package com.openexchange.groupware.attach.json.actions;

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
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractAttachmentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractAttachmentAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAttachmentAction.class);

    protected static final AttachmentParser PARSER = new AttachmentParser();

    protected static final AttachmentBase ATTACHMENT_BASE = Attachment.ATTACHMENT_BASE;

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
        final String value = req.getParameter(parameter);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            throw AttachmentExceptionCodes.INVALID_REQUEST_PARAMETER.create(parameter, value);
        }
    }

    protected static void require(final AJAXRequestData req, final String... parameters) throws OXException {
        for (final String param : parameters) {
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
        final long maxUploadSize = this.maxUploadSize.get();
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
        } catch (final OXException e) {
            LOG.debug("Rollback failed.", e);
        }
    }
}
