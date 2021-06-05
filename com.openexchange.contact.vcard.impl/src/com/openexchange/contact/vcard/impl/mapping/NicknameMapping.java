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

import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import ezvcard.property.Nickname;

/**
 * {@link NicknameMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class NicknameMapping extends SimpleMapping<Nickname> {

    public NicknameMapping() {
        super(Contact.NICKNAME, Nickname.class, "NICKNAME", ContactField.NICKNAME);
    }

    @Override
    protected void exportProperty(Contact contact, Nickname property, List<OXException> warnings) {
        property.getValues().clear();
        property.getValues().addAll(Arrays.asList(Strings.splitByComma(contact.getNickname())));
    }

    @Override
    protected Nickname exportProperty(Contact contact, List<OXException> warnings) {
        Nickname property = new Nickname();
        exportProperty(contact, property, warnings);
        return property;
    }

    @Override
    protected void importProperty(Nickname property, Contact contact, List<OXException> warnings) {
        contact.setNickname(Strings.join(property.getValues(), ","));
    }

}
