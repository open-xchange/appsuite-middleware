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

package com.openexchange.contact.vcard.impl.internal;

import java.io.InputStream;
import java.util.List;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link DefaultVCardExport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultVCardExport implements VCardExport {

    private final List<OXException> warnings;
    private final ThresholdFileHolder vCardHolder;

    /**
     * Initializes a new {@link DefaultVCardExport}.
     *
     * @param vCardHolder A file holder storing the vCard
     * @param warnings A list of parser- and conversion warnings
     */
    public DefaultVCardExport(ThresholdFileHolder vCardHolder, List<OXException> warnings) {
        super();
        this.vCardHolder = vCardHolder;
        this.warnings = warnings;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    @Override
    public IFileHolder getVCard() {
        return vCardHolder;
    }

    @Override
    public void close() {
        Streams.close(vCardHolder);
    }

    @Override
    public InputStream getClosingStream() throws OXException {
        return vCardHolder.getClosingStream();
    }

    @Override
    public byte[] toByteArray() throws OXException {
        return vCardHolder.toByteArray();
    }

}
