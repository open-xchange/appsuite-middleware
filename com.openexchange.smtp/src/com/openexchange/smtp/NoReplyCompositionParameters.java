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

package com.openexchange.smtp;

import java.util.Locale;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.util.TimeZones;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.filler.CompositionParameters;
import com.openexchange.mail.mime.filler.MimeMessageFiller.ImageProvider;


/**
 * {@link NoReplyCompositionParameters}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class NoReplyCompositionParameters implements CompositionParameters {

    @Override
    public String getOrganization() throws OXException {
        return null;
    }

    @Override
    public String getClient() throws OXException {
        return null;
    }

    @Override
    public String getOriginatingIP() throws OXException {
        return null;
    }

    @Override
    public InternetAddress getSenderAddress(InternetAddress from) throws OXException, AddressException {
        return null;
    }

    @Override
    public String getTimeZoneID() throws OXException {
        return TimeZones.UTC.getID();
    }

    @Override
    public boolean setReplyTo() {
        return false;
    }

    @Override
    public String getReplyToAddress() throws OXException {
        return null;
    }

    @Override
    public String getEnvelopeFrom() throws OXException {
        return "<>";
    }

    @Override
    public Locale getLocale() throws OXException {
        return Locale.US;
    }

    @Override
    public String getUserVCardFileName() throws OXException {
        return null;
    }

    @Override
    public  byte[] getUserVCard() throws OXException {
        return null;
    }

    @Override
    public int getAutoLinebreak() {
        return -1;
    }

    @Override
    public boolean isForwardAsAttachment() {
        return false;
    }

    @Override
    public ImageProvider createImageProvider(ImageDataSource dataSource, ImageLocation imageLocation) throws OXException {
        throw MimeMailExceptionCode.IMAGE_ATTACHMENTS_UNSUPPORTED.create();
    }

}
