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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    private boolean enforceUtf8;
    private boolean importAttachments;
    private boolean removeAttachmentsFromKeptVCard;
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
    public boolean isEnforceUtf8() {
        return enforceUtf8;
    }

    @Override
    public VCardParameters setEnforceUtf8(boolean enforceUtf8) {
        this.enforceUtf8 = enforceUtf8;
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
