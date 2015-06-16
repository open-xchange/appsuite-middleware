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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.contact.vcard.internal;

import java.awt.Dimension;
import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardVersion;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link VCardParametersImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class VCardParametersImpl implements VCardParameters {

    private int maxContactImageSize;
    private boolean validateContactEMail;
    private VCardVersion version;
    private Dimension photoScaleDimension;
    private boolean strict;
    private Session session;
    private List<OXException> warnings;
    private boolean skipValidation;
    private long maxVCardSize;

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
    /**
     * Sets the version
     *
     * @param version The version to set
     */
    public void setVersion(VCardVersion version) {
        this.version = version;
    }

    @Override
    public Dimension getPhotoScaleDimension() {
        return photoScaleDimension;
    }

    /**
     * Sets the photoScaleDimension
     *
     * @param photoScaleDimension The photoScaleDimension to set
     */
    public void setPhotoScaleDimension(Dimension photoScaleDimension) {
        this.photoScaleDimension = photoScaleDimension;
    }

    @Override
    public boolean isStrict() {
        return strict;
    }

    /**
     * Sets the strict
     *
     * @param strict The strict to set
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Sets the session
     *
     * @param session The session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public int getMaxContactImageSize() {
        return maxContactImageSize;
    }

    /**
     * Sets the maxContactImageSize
     *
     * @param maxContactImageSize The maxContactImageSize to set
     */
    public void setMaxContactImageSize(int maxContactImageSize) {
        this.maxContactImageSize = maxContactImageSize;
    }

    @Override
    public boolean isValidateContactEMail() {
        return validateContactEMail;
    }

    /**
     * Sets the validateContactEMail
     *
     * @param validateContactEMail The validateContactEMail to set
     */
    public void setValidateContactEMail(boolean validateContactEMail) {
        this.validateContactEMail = validateContactEMail;
    }

    @Override
    public List<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Sets the warnings
     *
     * @param warnings The warnings to set
     */
    public void setWarnings(List<OXException> warnings) {
        this.warnings = warnings;
    }

    /**
     * Gets the skipValidation
     *
     * @return The skipValidation
     */
    @Override
    public boolean isSkipValidation() {
        return skipValidation;
    }

    /**
     * Sets the skipValidation
     *
     * @param skipValidation The skipValidation to set
     */
    public void setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    @Override
    public long getMaxVCardSize() {
        return maxVCardSize;
    }

    /**
     * Sets the maxVCardSize
     *
     * @param maxVCardSize The maxVCardSize to set
     */
    public void setMaxVCardSize(long maxVCardSize) {
        this.maxVCardSize = maxVCardSize;
    }

}
