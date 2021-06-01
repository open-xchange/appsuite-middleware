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

package com.openexchange.ajax.drive.updater;

import java.util.Date;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;

/**
 * {@link UpdateXMLResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdateXMLResponse extends AbstractAJAXResponse {

    private final String xml;

    /**
     * Initializes a new {@link UpdateXMLResponse}.
     * 
     * @param response
     */
    protected UpdateXMLResponse(String xml) {
        super(null);
        this.xml = xml;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OXException getException() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProblematicAttribute[] getProblematics() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getTimestamp() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasError() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.openexchange.ajax.framework.AbstractAJAXResponse#getData()
     */
    @Override
    public Object getData() {
        return getXML();
    }

    public String getXML() {
        return xml;
    }

}
