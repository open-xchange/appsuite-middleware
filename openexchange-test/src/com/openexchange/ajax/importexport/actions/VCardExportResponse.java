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

package com.openexchange.ajax.importexport.actions;

import java.util.Date;
import org.apache.http.HttpResponse;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;

public class VCardExportResponse extends AbstractAJAXResponse {

    private String vCard;
    
    private final HttpResponse response;

    /**
     * @param response
     */
    public VCardExportResponse(HttpResponse response) {
        super(null);
        this.response = response;
    }

    /**
     * @param iCal the iCal to set
     */
    public void setVCard(final String vCard) {
        this.vCard = vCard;
    }

    /**
     * @return the iCal
     */
    public String getVCard() {
        return vCard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getData() {
        return getVCard();
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
     * @return response the http response
     */
    public HttpResponse getHttpResponse() {
        return response;
    }
}
