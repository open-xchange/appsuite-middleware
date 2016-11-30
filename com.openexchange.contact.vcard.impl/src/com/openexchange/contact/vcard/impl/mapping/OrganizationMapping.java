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

package com.openexchange.contact.vcard.impl.mapping;

import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import ezvcard.VCard;
import ezvcard.property.Organization;

/**
 * {@link OrganizationMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OrganizationMapping extends AbstractMapping {

    /**
     * Initializes a new {@link OrganizationMapping}.
     */
    public OrganizationMapping() {
        super("ORG", ContactField.COMPANY, ContactField.BRANCHES, ContactField.DEPARTMENT);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        Organization property = vCard.getOrganization();
        if (containsOrganization(contact)) {
            if (null == property) {
                property = new Organization();
                vCard.addOrganization(property);
            } else {
                property.getValues().clear();
            }
            property.getValues().add(contact.getCompany());
            property.getValues().add(contact.getDepartment());
            if (contact.containsBranches() && false == Strings.isEmpty(contact.getBranches())) {
                for (String branch : Strings.splitByComma(contact.getBranches())) {
                    property.getValues().add(branch);
                }
            }
        } else if (null != property) {
            vCard.removeProperty(property);
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        String company = null;
        String department = null;
        String branches = null;
        Organization property = vCard.getOrganization();
        if (null != property) {
            List<String> values = property.getValues();
            if (null != values && 0 < values.size()) {
                company = values.get(0);
                if (1 < values.size()) {
                    department = values.get(1);
                    if (2 < values.size()) {
                        branches = Strings.join(values.subList(2, values.size()), ", ");
                    }
                }
            }
        }
        contact.setCompany(company);
        contact.setDepartment(department);
        contact.setBranches(branches);
    }

    private static boolean containsOrganization(Contact contact) {
        return hasOneOf(contact, Contact.COMPANY, Contact.BRANCHES, Contact.DEPARTMENT);
    }

}
