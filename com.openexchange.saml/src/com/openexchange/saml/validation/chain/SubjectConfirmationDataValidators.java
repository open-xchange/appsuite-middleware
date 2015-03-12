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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.saml.validation.chain;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.SubjectConfirmationData;
import com.openexchange.saml.SAMLStateManagement;
import com.openexchange.saml.validation.ValidationResult.ErrorReason;


/**
 * {@link SubjectConfirmationDataValidators}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SubjectConfirmationDataValidators {

    /**
     * Validates the recipient attribute of a subject confirmation data element
     */
    public static final class RecipientValidator implements SubjectConfirmationDataValidator {

        private final String expected;

        /**
         * Initializes a new {@link RecipientValidator}.
         * @param expected The expected recipient value
         */
        public RecipientValidator(String expected) {
            super();
            this.expected = expected;
        }

        @Override
        public ValidationError validate(SubjectConfirmationData confirmationData) {
            String recipient = confirmationData.getRecipient();
            if (recipient == null) {
                return new ValidationError(ErrorReason.MISSING_ATTRIBUTE, "'Recipient' is not set in subject confirmation data");
            }

            if (!expected.equals(recipient)) {
                return new ValidationError(ErrorReason.INVALID_ATTRIBUTE, "'Recipient' attribute of subject confirmation data contains an unexpected value: " + recipient);
            }

            return null;
        }

    }

    /**
     * Validates the NotOnOrAfter attribute of a subject confirmation data element
     */
    public static final class NotOnOrAfterValidator implements SubjectConfirmationDataValidator {

        private final DateTime now;

        /**
         * Initializes a new {@link NotOnOrAfterValidator}.
         * @param now The date to check against
         */
        public NotOnOrAfterValidator(DateTime now) {
            super();
            this.now = now;
        }

        @Override
        public ValidationError validate(SubjectConfirmationData confirmationData) {
            DateTime notOnOrAfter = confirmationData.getNotOnOrAfter();
            if (notOnOrAfter == null) {
                return new ValidationError(ErrorReason.MISSING_ATTRIBUTE, "'NotOnOrAfter' is not set in subject confirmation data");
            }

            if (!now.isBefore(notOnOrAfter)) {
                return new ValidationError(ErrorReason.INVALID_ATTRIBUTE, "'NotOnOrAfter' attribute of subject confirmation data contains an unexpected value: " + notOnOrAfter);
            }

            return null;
        }

    }

    /**
     * Validates the InResponseTo attribute of a subject confirmation data element
     */
    public static final class InResponseToValidator implements SubjectConfirmationDataValidator {

        private final SAMLStateManagement stateManagement;

        /**
         * Initializes a new {@link InResponseToValidator}.
         * @param stateManagement
         */
        public InResponseToValidator(SAMLStateManagement stateManagement) {
            super();
            this.stateManagement = stateManagement;
        }

        @Override
        public ValidationError validate(SubjectConfirmationData confirmationData) {
            // TODO: implement
            return null;
        }

    }

}
