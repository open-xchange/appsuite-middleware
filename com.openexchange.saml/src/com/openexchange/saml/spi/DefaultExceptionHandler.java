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

package com.openexchange.saml.spi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;


/**
 * A default implementation of {@link ExceptionHandler}. It basically responds with a not-so-pretty
 * error page, displaying a technical error message. You will probably use this one during development,
 * but return a pretty error page or redirect the user to a different location in production by implementing
 * your own {@link ExceptionHandler}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public void handleAuthnResponseFailed(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OXException exception) {
        ExceptionHandler.sendErrorPage(httpResponse, exception);
    }

    @Override
    public void handleLogoutResponseFailed(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OXException exception) {
        ExceptionHandler.sendErrorPage(httpResponse, exception);
    }

}
