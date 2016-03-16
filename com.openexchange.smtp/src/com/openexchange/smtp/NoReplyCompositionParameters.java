/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
