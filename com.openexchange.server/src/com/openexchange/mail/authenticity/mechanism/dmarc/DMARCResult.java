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

package com.openexchange.mail.authenticity.mechanism.dmarc;

import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;

/**
 * {@link DMARCResult} - Defines the possible results as defined in
 * <a href="https://tools.ietf.org/html/rfc7489#section-11.2">RFC-7489, Section 11.2</a>.
 * The ordinal defines the significance of each result.
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7489#section-11.2">RFC-7489, Section 11.2</a>
 */
public enum DMARCResult implements AuthenticityMechanismResult {
    /**
     * A DMARC policy record was published for the aligned
     * identifier, and at least one of the authentication mechanisms
     * passed.
     */
    PASS("Pass", "pass"),
    /**
     * No DMARC policy record was published for the aligned
     * identifier, or no aligned identifier could be extracted.
     */
    NONE("None", "none"),
    /**
     * A temporary error occurred during DMARC evaluation. A
     * later attempt might produce a final result.
     */
    TEMPERROR("Temporary Error", "temperror"),
    /**
     * A permanent error occurred during DMARC evaluation, such as
     * encountering a syntactically incorrect DMARC record. A later
     * attempt is unlikely to produce a final result.
     */
    PERMERROR("Permanent Error", "permerror"),
    /**
     * A DMARC policy record was published for the aligned
     * identifier, and none of the authentication mechanisms passed.
     */
    FAIL("Fail", "fail");

    private final String displayName;
    private final String technicalName;

    /**
     * Initialises a new {@link DMARCResult}.
     */
    private DMARCResult(String displayName, String technicalName) {
        this.displayName = displayName;
        this.technicalName = technicalName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getTechnicalName() {
        return technicalName;
    }

    @Override
    public int getCode() {
        return ordinal();
    }
}
