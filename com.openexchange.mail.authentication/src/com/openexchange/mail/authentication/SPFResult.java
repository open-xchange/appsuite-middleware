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

package com.openexchange.mail.authentication;

/**
 * {@link SPFResult}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SPFResult {

    /**
     * The SPF verifier has no information at all about the authorisation
     * or lack thereof of the client to use the checked identity or identities.
     * The check_host() function completed without errors but was not able to
     * reach any conclusion.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.1">RFC-7208, Section 8.1</a>
     */
    NONE("None"),

    /**
     * <p>Indicates that although a policy for the identity was discovered, there is
     * no definite assertion (positive or negative) about the client.</p>
     * 
     * <p>A "neutral" result MUST be treated exactly like the "none" result;
     * the distinction exists only for informational purposes. Treating
     * "neutral" more harshly than "none" would discourage ADMDs from
     * testing the use of SPF records.</p>
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.2">RFC-7208, Section 8.2</a>
     */
    NEUTRAL("Neutral"),

    /**
     * The client is authorised to inject mail with the given identity. The domain
     * can now, in the sense of reputation, be considered responsible for sending
     * the message. Further policy checks can now proceed with confidence in the
     * legitimate use of the identity.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.3">RFC-7208, Section 8.3</a>
     */
    PASS("Pass"),

    /**
     * An explicit statement that the client is not authorised to use the domain in the
     * given identity. Disposition of SPF fail messages is a matter of local policy.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.4">RFC-7208, Section 8.4</a>
     */
    FAIL("Fail"),

    /**
     * Ought to be treated as somewhere between "fail" and "neutral"/"none". The ADMD
     * believes the host is not authorised but is not willing to make a strong policy
     * statement. Receiving software SHOULD NOT reject the message based solely on this
     * result, but MAY subject the message to closer scrutiny than normal.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.5">RFC-7208, Section 8.5</a>
     */
    SOFTFAIL("Soft Fail"),

    /**
     * The SPF verifier encountered a transient (generally DNS) error while performing the check.
     * Checking software can choose to accept or temporarily reject the message. If the message
     * is rejected during the SMTP transaction for this reason, the software SHOULD use an SMTP
     * reply code of 451 and, if supported, the 4.4.3 enhanced status code (see
     * <a href="https://tools.ietf.org/html/rfc3463#section-3.5">Section 3.5 of [RFC3463]</a>).
     * These errors can be caused by problems in either the sender's or receiver's DNS software.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.6">RFC-7208, Section 8.6</a>
     */
    TEMPERROR("Temporary Error"),

    /**
     * The domain's published records could not be correctly interpreted. This signals an error
     * condition that definitely requires DNS operator intervention to be resolved. If the message
     * is rejected during the SMTP transaction for this reason, the software SHOULD use an SMTP
     * reply code of 550 and, if supported, the 5.5.2 enhanced status code (see
     * <a href="https://tools.ietf.org/html/rfc3463#section-3.6">[RFC3463], Section 3.6</a>).
     * Be aware that if the ADMD uses macros (<a href="https://tools.ietf.org/html/rfc7208#section-7">Section 7</a>),
     * it is possible that this result is due to the checked identities having an unexpected format.
     * It is also possible that this result is generated by certain SPF verifiers due to the input arguments
     * having an unexpected format.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7208#section-8.7">RFC-7208, Section 8.7</a>
     */
    PERMERROR("Permanent Error");

    private final String displayName;

    /**
     * Initialises a new {@link SPFResult}.
     */
    private SPFResult(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the technical name of the SPF result
     * 
     * @return the technical name of the SPF result
     */
    public String getTechnicalName() {
        return name().toLowerCase();
    }
}
