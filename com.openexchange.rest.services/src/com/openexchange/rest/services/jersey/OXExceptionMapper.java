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
package com.openexchange.rest.services.jersey;

import javax.servlet.http.HttpServletResponse;
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

    @Context
    protected HttpServletResponse servletResponse;

    /**
     * Initializes a new {@link OXExceptionMapper}.
     */
    public OXExceptionMapper() {
        super();
    }

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

        CustomStatus(int code, Object msg) {
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
