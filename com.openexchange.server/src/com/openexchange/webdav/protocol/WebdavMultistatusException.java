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

package com.openexchange.webdav.protocol;

import java.util.Collection;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXExceptionStrings;

public class WebdavMultistatusException extends WebdavProtocolException {

	private static final long serialVersionUID = 1L;

	private static final WebdavProtocolException.Code CODE = WebdavProtocolException.Code.GENERAL_ERROR;

	public static WebdavMultistatusException create(final WebdavPath url, final WebdavProtocolException... exceptions) {
	    final Category category = CODE.getCategory();
        final WebdavMultistatusException ret;
        if (category.getLogLevel().implies(LogLevel.DEBUG)) {
            ret = new WebdavMultistatusException(url, exceptions, CODE.getNumber(), CODE.getMessage(), null);
        } else {
            ret =
                new WebdavMultistatusException(
                    url,
                    exceptions,
                    CODE.getNumber(),
                    Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                    null);
            ret.setLogMessage(CODE.getMessage());
        }
        ret.addCategory(category);
        ret.setPrefix(CODE.getPrefix());
        return ret;

	}

	public static WebdavMultistatusException create(final WebdavPath url, final Collection<WebdavProtocolException> exceptions) {
        final Category category = CODE.getCategory();
        final WebdavMultistatusException ret;
        if (category.getLogLevel().implies(LogLevel.DEBUG)) {
            ret = new WebdavMultistatusException(url, exceptions, CODE.getNumber(), CODE.getMessage(), null);
        } else {
            ret =
                new WebdavMultistatusException(
                    url,
                    exceptions,
                    CODE.getNumber(),
                    Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                    null);
            ret.setLogMessage(CODE.getMessage());
        }
        ret.addCategory(category);
        ret.setPrefix(CODE.getPrefix());
        return ret;

    }

	private final WebdavProtocolException[] exceptions;

    /**
     * No direct instantiation.
     */
    protected WebdavMultistatusException(final WebdavPath url, final WebdavProtocolException[] exceptions, final int code, final String displayMessage, final Throwable cause, final Object... displayArgs) {
        super(207, url, code, displayMessage, cause, displayArgs);
        this.exceptions = exceptions;
    }

    /**
     * No direct instantiation.
     */
    protected WebdavMultistatusException(final WebdavPath url, final Collection<WebdavProtocolException> exceptions, final int code, final String displayMessage, final Throwable cause, final Object... displayArgs) {
        super(207, url, code, displayMessage, cause, displayArgs);
        this.exceptions = exceptions.toArray(new WebdavProtocolException[exceptions.size()]);
    }

    public WebdavProtocolException[] getExceptions(){
		return exceptions;
	}
}
