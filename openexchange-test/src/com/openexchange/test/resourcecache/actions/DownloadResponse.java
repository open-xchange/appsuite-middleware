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

package com.openexchange.test.resourcecache.actions;

import java.util.Date;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.ProblematicAttribute;

/**
 * {@link DownloadResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DownloadResponse extends AbstractAJAXResponse {

    private final byte[] bytes;

    public DownloadResponse(byte[] bytes) {
        super(null);
        this.bytes = bytes;
    }

    @Override
    public Object getData() {
        return getBytes();
    }

    @Override
    public OXException getException() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProblematicAttribute[] getProblematics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasError() {
        throw new UnsupportedOperationException();
    }

    public byte[] getBytes() {
        return bytes;
    }

}
