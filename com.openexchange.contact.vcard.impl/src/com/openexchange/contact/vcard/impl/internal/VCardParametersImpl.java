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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.contact.vcard.DistributionListMode;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardVersion;
import com.openexchange.session.Session;

/**
 * {@link VCardParametersImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class VCardParametersImpl implements VCardParameters {

    private long maxContactImageSize;
    private boolean validateContactEMail;
    private VCardVersion version;
    private Dimension photoScaleDimension;
    private boolean strict;
    private Session session;
    private boolean skipValidation;
    private long maxVCardSize;
    private boolean keepOriginalVCard;
    private boolean removeImageFromKeptVCard;
    private boolean importAttachments;
    private boolean removeAttachmentsFromKeptVCard;
    private DistributionListMode distributionListMode;
    private Map<String, Object> parameters;
    private Set<String> propertyNames;

    /**
     * Initializes a new, empty {@link VCardParametersImpl}.
     */
    public VCardParametersImpl() {
        super();
    }

    @Override
    public VCardVersion getVersion() {
        return version;
    }

    @Override
    public VCardParameters setVersion(VCardVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public Dimension getPhotoScaleDimension() {
        return photoScaleDimension;
    }

    @Override
    public VCardParameters setPhotoScaleDimension(Dimension photoScaleDimension) {
        this.photoScaleDimension = photoScaleDimension;
        return this;
    }

    @Override
    public boolean isStrict() {
        return strict;
    }

    @Override
    public VCardParameters setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public VCardParameters setSession(Session session) {
        this.session = session;
        return this;
    }

    @Override
    public long getMaxContactImageSize() {
        return maxContactImageSize;
    }

    @Override
    public VCardParameters setMaxContactImageSize(long maxContactImageSize) {
        this.maxContactImageSize = maxContactImageSize;
        return this;
    }

    @Override
    public boolean isValidateContactEMail() {
        return validateContactEMail;
    }

    @Override
    public VCardParameters setValidateContactEMail(boolean validateContactEMail) {
        this.validateContactEMail = validateContactEMail;
        return this;
    }

    @Override
    public boolean isSkipValidation() {
        return skipValidation;
    }

    @Override
    public VCardParameters setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
        return this;
    }

    @Override
    public long getMaxVCardSize() {
        return maxVCardSize;
    }

    @Override
    public VCardParameters setMaxVCardSize(long maxVCardSize) {
        this.maxVCardSize = maxVCardSize;
        return this;
    }

    @Override
    public boolean isKeepOriginalVCard() {
        return keepOriginalVCard;
    }

    @Override
    public VCardParameters setKeepOriginalVCard(boolean keepOriginalVCard) {
        this.keepOriginalVCard = keepOriginalVCard;
        return this;
    }

    @Override
    public boolean isRemoveImageFromKeptVCard() {
        return removeImageFromKeptVCard;
    }

    @Override
    public VCardParameters setRemoveImageFromKeptVCard(boolean removeImageFromKeptVCard) {
        this.removeImageFromKeptVCard = removeImageFromKeptVCard;
        return this;
    }

    @Override
    public boolean isImportAttachments() {
        return importAttachments;
    }

    @Override
    public VCardParameters setImportAttachments(boolean importAttachments) {
        this.importAttachments = importAttachments;
        return this;
    }

    @Override
    public boolean isRemoveAttachmentsFromKeptVCard() {
        return removeAttachmentsFromKeptVCard;
    }

    @Override
    public VCardParameters setRemoveAttachmentsFromKeptVCard(boolean removeAttachmentsFromKeptVCard) {
        this.removeAttachmentsFromKeptVCard = removeAttachmentsFromKeptVCard;
        return this;
    }

    @Override
    public DistributionListMode getDistributionListMode() {
        return distributionListMode;
    }

    @Override
    public VCardParameters setDistributionListMode(DistributionListMode mode) {
        this.distributionListMode = mode;
        return this;
    }

    @Override
    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    @Override
    public VCardParameters setPropertyNames(Set<String> propertyNames) {
        this.propertyNames = propertyNames;
        return this;
    }

    @Override
    public <T> T get(String name, Class<T> clazz) {
        if (null == name || null == parameters) {
            return null;
        }
        try {
            return clazz.cast(parameters.get(name));
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> VCardParameters set(String name, T value) {
        if (null != name) {
            if (null == parameters) {
                parameters = new HashMap<String, Object>();
            }
            parameters.put(name, value);
        } else if (null != parameters) {
            parameters.remove(name);
        }
        return this;
    }

}
