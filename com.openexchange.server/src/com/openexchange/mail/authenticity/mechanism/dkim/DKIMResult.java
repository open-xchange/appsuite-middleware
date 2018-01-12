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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.mail.authenticity.mechanism.dkim;

import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;

/**
 * {@link DKIMResult} - The evaluation states of the DKIM signature.
 * The ordinal defines the significance of each result.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @see <a href="https://tools.ietf.org/html/rfc7601#section-2.7.1">RFC 7601, Section 2.7.1</a>
 */
public enum DKIMResult implements AuthenticityMechanismResult {

    /**
     * The message was signed, the signature or signatures were
     * acceptable to the ADMD, and the signature(s) passed verification
     * tests.
     */
    PASS("Pass", "pass"),
    /**
     * The message was signed, but the signature or signatures
     * contained syntax errors or were not otherwise able to be
     * processed. This result is also used for other failures not
     * covered elsewhere in this list.
     *
     */
    NEUTRAL("Neutral", "neutral"),
    /**
     * The message was signed, but some aspect of the signature or
     * signatures was not acceptable to the ADMD.
     */
    POLICY("Policy", "policy"),
    /**
     * The message was not signed.
     */
    NONE("None", "none"),
    /**
     * The message could not be verified due to some error that
     * is likely transient in nature, such as a temporary inability to
     * retrieve a public key. A later attempt may produce a final
     * result.
     */
    TEMPERROR("Temporary Error", "temperror"),
    /**
     * The message could not be verified due to some error that
     * is unrecoverable, such as a required header field being absent. A
     * later attempt is unlikely to produce a final result.
     */
    PERMFAIL("Permanent Failure", "permfail"),
    /**
     * The message was signed and the signature or signatures were
     * acceptable to the ADMD, but they failed the verification test(s).
     */
    FAIL("Fail", "fail"),
    ;

    private final String displayName;
    private final String technicalName;

    /**
     * Initialises a new {@link DKIMResult}.
     */
    private DKIMResult(String displayName, String technicalName) {
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
