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

package com.openexchange.onboarding;

import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;

/**
 * {@link CommonFormDescription} - An enumeration for common form descriptions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum CommonFormDescription {

    /**
     * The common form description in case the user is not supposed to enter anything.
     */
    NONE(new FormElement[0]),
    /**
     * The common form description in case the user is supposed to enter an E-Mail address.
     */
    EMAIL_ADDRESS(FormElement.input("email", OnboardingStrings.FORM_EMAIL, true, null)),
    /**
     * The common form description in case the user is supposed to enter a phone number.
     */
    PHONE_NUMBER(FormElement.input("number", OnboardingStrings.FORM_EMAIL, true, null)),

    ;

    private final DynamicFormDescription formDescription;
    private final String firstFormElementName;

    private CommonFormDescription(FormElement... elements) {
        if (null != elements && elements.length > 0) {
            DynamicFormDescription formDescription = new DynamicFormDescription();

            // First form element
            {
                FormElement formElement = elements[0];
                firstFormElementName = formElement.toString();
                formDescription.add(formElement);
            }

            // Remaining ones (if any)
            for (int i = 1; i < elements.length; i++) {
                formDescription.add(elements[i]);
            }

            this.formDescription = new ReadOnlyDynamicFormDescription(formDescription);
        } else {
            this.formDescription = null;
            this.firstFormElementName = null;
        }
    }

    /**
     * Gets the (read-only) form description
     *
     * @return The form description or <code>null</code>
     */
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    /**
     * Gets the name of the first form element
     *
     * @return The name of the first form element or <code>null</code>
     */
    public String getFirstFormElementName() {
        return firstFormElementName;
    }

}
