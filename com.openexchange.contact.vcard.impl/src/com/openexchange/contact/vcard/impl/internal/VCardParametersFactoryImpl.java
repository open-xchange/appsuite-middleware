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

package com.openexchange.contact.vcard.impl.internal;

import java.awt.Dimension;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardParametersFactory;
import com.openexchange.contact.vcard.VCardVersion;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link VCardParametersFactoryImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class VCardParametersFactoryImpl implements VCardParametersFactory {

    private Dimension defaultPhotoScaleDimension;
    private int defaultMaxContactImageSize;
    private boolean defaultValidateContactEMail;
    private final boolean defaultStrict;
    private final VCardVersion defaultVersion;
    private final boolean enforceUtf8;
    private long defaultMaxVCardSize;
    private boolean defaultKeepOriginalVCard;
    private boolean defaultRemoveImageFromKeptVCard;

    /**
     * Initializes a new {@link VCardParametersFactoryImpl}.
     */
    public VCardParametersFactoryImpl() {
        super();
        defaultPhotoScaleDimension = new Dimension(200, 200);
        defaultMaxContactImageSize = 4194304;
        enforceUtf8 = true;
        defaultValidateContactEMail = true;
        defaultStrict = false;
        defaultVersion = VCardVersion.VERSION_3_0;
        defaultMaxVCardSize = 4194304;
        defaultKeepOriginalVCard = true;
        defaultRemoveImageFromKeptVCard = true;
    }

    /**
     * (Re-) initializes the default vCard parameter values based on the configuration.
     *
     * @param configService A reference to the configuration service
     * @throws OXException
     */
    public void reinitialize(ConfigurationService configService) throws OXException {
        String value = configService.getProperty("com.openexchange.contact.scaleVCardImages", "");
        if (false == Strings.isEmpty(value)) {
            int idx = value.indexOf('x');
            if (1 > idx) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("com.openexchange.contact.scaleVCardImages");
            }
            try {
                defaultPhotoScaleDimension = new Dimension(Integer.parseInt(value.substring(0, idx)), Integer.parseInt(value.substring(idx + 1)));
            } catch (NumberFormatException e) {
                throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, "com.openexchange.contact.scaleVCardImages");
            }
        } else {
            defaultPhotoScaleDimension = null;
        }
        defaultMaxContactImageSize = configService.getIntProperty("max_image_size", 4194304);
        defaultValidateContactEMail = configService.getBoolProperty("validate_contact_email", true);
        defaultMaxVCardSize = configService.getIntProperty("com.openexchange.contact.maxVCardSize", 4194304);
        defaultKeepOriginalVCard = configService.getBoolProperty("com.openexchange.contact.storeVCards", true);
        defaultRemoveImageFromKeptVCard = configService.getBoolProperty("com.openexchange.contact.removeImageFromStoredVCards", true);
    }

    @Override
    public VCardParameters createParameters(Session session) {
        VCardParametersImpl parameters = new VCardParametersImpl();
        parameters.setSession(session);
        parameters.setStrict(defaultStrict);
        parameters.setVersion(defaultVersion);
        parameters.setPhotoScaleDimension(defaultPhotoScaleDimension);
        parameters.setMaxContactImageSize(defaultMaxContactImageSize);
        parameters.setValidateContactEMail(defaultValidateContactEMail);
        parameters.setMaxVCardSize(defaultMaxVCardSize);
        parameters.setKeepOriginalVCard(defaultKeepOriginalVCard);
        parameters.setRemoveImageFromKeptVCard(defaultRemoveImageFromKeptVCard);
        parameters.setEnforceUtf8(enforceUtf8);
        return parameters;
    }

    @Override
    public VCardParameters createParameters() {
        return createParameters(null);
    }

}
