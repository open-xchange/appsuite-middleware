/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    private static final String EMPTY_STRING = "";

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
            property.getValues().add(contact.containsCompany() ? contact.getCompany() : EMPTY_STRING);
            property.getValues().add(contact.containsDepartment() ? contact.getDepartment() : EMPTY_STRING);
            if (contact.containsBranches() && Strings.isNotEmpty(contact.getBranches())) {
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
