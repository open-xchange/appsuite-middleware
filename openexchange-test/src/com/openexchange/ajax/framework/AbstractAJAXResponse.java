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

package com.openexchange.ajax.framework;

import java.util.Date;
import java.util.List;
import org.junit.Assert;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;

/**
 * This class implements inheritable methods for AJAX responses.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAJAXResponse extends Assert {

    private final Response response;

    private long requestDuration;

    private long parseDuration;

    private List<ConflictObject> conflicts;

    protected AbstractAJAXResponse(final Response response) {
        super();
        this.response = response;
    }

    public long getRequestDuration() {
        return requestDuration;
    }

    void setRequestDuration(final long duration) {
        this.requestDuration = duration;
    }

    public long getParseDuration() {
        return parseDuration;
    }

    void setParseDuration(final long parseDuration) {
        this.parseDuration = parseDuration;
    }

    public long getTotalDuration() {
        return (requestDuration + parseDuration);
    }

    public Response getResponse() {
        return response;
    }

    public Object getData() {
        return response.getData();
    }

    public Date getTimestamp() {
        return response.getTimestamp();
    }

    public boolean hasError() {
        return response.hasError();
    }

    public boolean hasWarnings() {
        return response.hasWarnings();
    }

    /**
     * Gets the formatted error message.
     *
     * @return The formatted error message or <code>null</code> if no error present
     */
    public String getErrorMessage() {
        return response.getFormattedErrorMessage();
    }

    public OXException getException() {
        return response.getException();
    }

    public ProblematicAttribute[] getProblematics() {
        return response.getException().getProblematics();
    }

    public List<ConflictObject> getConflicts() {
        return conflicts;
    }

    public void setConflicts(final List<ConflictObject> conflicts) {
        this.conflicts = conflicts;
    }

    public boolean hasConflicts() {
        return this.conflicts != null && this.conflicts.size() != 0;
    }
}
