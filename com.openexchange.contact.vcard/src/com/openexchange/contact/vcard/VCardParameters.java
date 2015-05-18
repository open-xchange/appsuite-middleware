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

package com.openexchange.contact.vcard;

import java.awt.Dimension;
import java.nio.charset.Charset;
import com.openexchange.java.Charsets;
import com.openexchange.session.Session;


/**
 * {@link VCardParameters}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class VCardParameters {

    private VCardVersion version;
    private Dimension photoScaleDimension;
    private boolean strict;
    private Session session;
    private Charset charset;

    /**
     * Initializes a new {@link VCardParameters}.
     */
    public VCardParameters() {
        super();
        this.version = VCardVersion.VERSION_3_0;
        this.strict = false;
        this.photoScaleDimension = new Dimension(200, 200);
        this.charset = Charsets.UTF_8;
        this.session = null;
    }

    /**
     * Gets the version
     *
     * @return The version
     */
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

    /**
     * Gets the photoScaleDimension
     *
     * @return The photoScaleDimension
     */
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

    /**
     * Gets the strict
     *
     * @return The strict
     */
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

    /**
     * Gets the session
     *
     * @return The session
     */
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

    /**
     * Gets the charset
     *
     * @return The charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets the charset
     *
     * @param charset The charset to set
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

}
