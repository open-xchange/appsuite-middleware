/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.ajax.types;

import java.util.Date;

import org.json.JSONArray;

/**
 * Response data object.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Response {

    private Object data = null;

    private Date timestamp = null;

    private String errorMessage = null;

    private JSONArray errorParams = null;

    public Response() {
        super();
    }

    /**
     * @return Returns the data.
     */
    public Object getData() {
        return data;
    }

    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return Returns the errorParams.
     */
    public JSONArray getErrorParams() {
        return errorParams;
    }

    /**
     * @return Returns the timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param data The data to set.
     */
    public void setData(final Object data) {
        this.data = data;
    }

    /**
     * @param errorMessage The errorMessage to set.
     */
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @param errorParams The errorParams to set.
     */
    public void setErrorParams(final JSONArray errorParams) {
        this.errorParams = errorParams;
    }

    /**
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasError() {
        return errorMessage != null;
    }
}
