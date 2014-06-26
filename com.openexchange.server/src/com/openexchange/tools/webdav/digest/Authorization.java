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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.tools.webdav.digest;

/**
 * {@link Authorization}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Authorization {

    String algorithm;

    String cnonce;

    String nOnce;

    String nc;

    String opaque;

    String qop;

    String realm;

    String response;

    String uri;

    String user;

    /**
     * Initializes a new {@link Authorization}.
     */
    Authorization() {
        super();
    }

    /**
     * Gets the algorithm
     *
     * @return The algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Gets the cnonce
     *
     * @return The cnonce
     */
    public String getCnonce() {
        return cnonce;
    }

    /**
     * Gets the nOnce
     *
     * @return The nOnce
     */
    public String getnOnce() {
        return nOnce;
    }

    /**
     * Gets the nc
     *
     * @return The nc
     */
    public String getNc() {
        return nc;
    }

    /**
     * Gets the opaque
     *
     * @return The opaque
     */
    public String getOpaque() {
        return opaque;
    }

    /**
     * Gets the qop
     *
     * @return The qop
     */
    public String getQop() {
        return qop;
    }

    /**
     * Gets the realm
     *
     * @return The realm
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Gets the response
     *
     * @return The response
     */
    public String getResponse() {
        return response;
    }

    /**
     * Gets the uri
     *
     * @return The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the user
     *
     * @return The user
     */
    public String getUser() {
        return user;
    }

}
