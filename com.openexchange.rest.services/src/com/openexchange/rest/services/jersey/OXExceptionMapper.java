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
package com.openexchange.rest.services.jersey;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * A mapper that catches {@link OXException}s and re-throws appropriate JAX-RS
 * exceptions that result in proper HTTP status codes. Exceptions are additionally
 * logged based on their nature.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
@Provider
public class OXExceptionMapper implements ExceptionMapper<OXException> {

    private static final Logger LOG = LoggerFactory.getLogger(OXExceptionMapper.class);

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private UriInfo uriInfo;


    @Override
    public Response toResponse(OXException e) {
        if (AjaxExceptionCodes.MISSING_PARAMETER.equals(e)) {
            LOG.error(e.getMessage(), e);
            return Response.status(new CustomStatus(Status.BAD_REQUEST.getStatusCode(), e.getMessage())).build();
        } else if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
            LOG.error(e.getMessage(), e);
            return Response.status(new CustomStatus(Status.BAD_REQUEST.getStatusCode(), e.getMessage())).build();
        } else if (AjaxExceptionCodes.HTTP_ERROR.equals(e)) {
            LOG.error(e.getMessage(), e);
            Object[] logArgs = e.getLogArgs();
            Object statusMsg = logArgs.length > 1 ? logArgs[1] : null;
            int sc = ((Integer) logArgs[0]).intValue();
            return Response.status(new CustomStatus(sc, statusMsg)).build();
        } else if (AjaxExceptionCodes.UNEXPECTED_ERROR.equals(e)) {
            LOG.error(e.getMessage(), e);
        } else {
            LOG.error(e.getMessage(), e);
        }

        return Response.status(new CustomStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage())).build();
    }

    private static final class CustomStatus implements StatusType {

        private final int code;

        private final Object msg;

        private final Status defaultStatus;

        public CustomStatus(int code, Object msg) {
            super();
            this.code = code;
            this.msg = msg;
            defaultStatus = Status.fromStatusCode(code);
        }

        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public Family getFamily() {
            if (defaultStatus == null) {
                return Family.familyOf(code);
            }

            return defaultStatus.getFamily();
        }

        @Override
        public String getReasonPhrase() {
            if (msg == null) {
                if (defaultStatus == null) {
                    return "";
                }

                return defaultStatus.getReasonPhrase();
            }

            return msg.toString();
        }

    }

}
