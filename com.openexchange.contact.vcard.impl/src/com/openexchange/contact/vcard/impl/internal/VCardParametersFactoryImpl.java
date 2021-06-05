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

import java.awt.Dimension;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.contact.vcard.DistributionListMode;
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
        String value = configService.getProperty("com.openexchange.contact.scaleVCardImages", "600x800");
        if (Strings.isNotEmpty(value)) {
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
        parameters.setDistributionListMode(DistributionListMode.V4_IN_V3_EXPORT);
        return parameters;
    }

    @Override
    public VCardParameters createParameters() {
        return createParameters(null);
    }
}
