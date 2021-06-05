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

package com.openexchange.contact.storage.rdb.internal;

import static com.openexchange.contact.storage.rdb.internal.Deduplicator.calculateHash;
import static com.openexchange.contact.storage.rdb.internal.Deduplicator.getContentFields;
import static com.openexchange.contact.storage.rdb.internal.Deduplicator.getDistListContentFields;
import java.util.Random;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.util.UUIDs;

/**
 * {@link DeduplicatorTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SuppressWarnings("unchecked") 
public class DeduplicatorTest {

    private static final ContactField[] TEXTUAL_FIELDS = {
        ContactField.DISPLAY_NAME,
        ContactField.SUR_NAME,
        ContactField.GIVEN_NAME,
        ContactField.MIDDLE_NAME,
        ContactField.SUFFIX,
        ContactField.TITLE,
        ContactField.STREET_HOME,
        ContactField.POSTAL_CODE_HOME,
        ContactField.CITY_HOME,
        ContactField.STATE_HOME,
        ContactField.COUNTRY_HOME,
        ContactField.MARITAL_STATUS,
        ContactField.NUMBER_OF_CHILDREN,
        ContactField.PROFESSION,
        ContactField.NICKNAME,
        ContactField.SPOUSE_NAME,
        ContactField.NOTE,
        ContactField.COMPANY,
        ContactField.DEPARTMENT,
        ContactField.POSITION,
        ContactField.EMPLOYEE_TYPE,
        ContactField.ROOM_NUMBER,
        ContactField.STREET_BUSINESS,
        ContactField.POSTAL_CODE_BUSINESS,
        ContactField.CITY_BUSINESS,
        ContactField.STATE_BUSINESS,
        ContactField.COUNTRY_BUSINESS,
        ContactField.NUMBER_OF_EMPLOYEE,
        ContactField.SALES_VOLUME,
        ContactField.TAX_ID,
        ContactField.COMMERCIAL_REGISTER,
        ContactField.BRANCHES,
        ContactField.BUSINESS_CATEGORY,
        ContactField.INFO,
        ContactField.MANAGER_NAME,
        ContactField.ASSISTANT_NAME,
        ContactField.STREET_OTHER,
        ContactField.POSTAL_CODE_OTHER,
        ContactField.CITY_OTHER,
        ContactField.STATE_OTHER,
        ContactField.COUNTRY_OTHER,
        ContactField.TELEPHONE_ASSISTANT,
        ContactField.TELEPHONE_BUSINESS1,
        ContactField.TELEPHONE_BUSINESS2,
        ContactField.FAX_BUSINESS,
        ContactField.TELEPHONE_CALLBACK,
        ContactField.TELEPHONE_CAR,
        ContactField.TELEPHONE_COMPANY,
        ContactField.TELEPHONE_HOME1,
        ContactField.TELEPHONE_HOME2,
        ContactField.FAX_HOME,
        ContactField.TELEPHONE_ISDN,
        ContactField.CELLULAR_TELEPHONE1,
        ContactField.CELLULAR_TELEPHONE2,
        ContactField.TELEPHONE_OTHER,
        ContactField.FAX_OTHER,
        ContactField.TELEPHONE_PAGER,
        ContactField.TELEPHONE_PRIMARY,
        ContactField.TELEPHONE_RADIO,
        ContactField.TELEPHONE_TELEX,
        ContactField.TELEPHONE_TTYTDD,
        ContactField.INSTANT_MESSENGER1,
        ContactField.INSTANT_MESSENGER2,
        ContactField.TELEPHONE_IP,
        ContactField.EMAIL1,
        ContactField.EMAIL2,
        ContactField.EMAIL3,
        ContactField.URL,
        ContactField.CATEGORIES,
        ContactField.USERFIELD01,
        ContactField.USERFIELD02,
        ContactField.USERFIELD03,
        ContactField.USERFIELD04,
        ContactField.USERFIELD05,
        ContactField.USERFIELD06,
        ContactField.USERFIELD07,
        ContactField.USERFIELD08,
        ContactField.USERFIELD09,
        ContactField.USERFIELD10,
        ContactField.USERFIELD11,
        ContactField.USERFIELD12,
        ContactField.USERFIELD13,
        ContactField.USERFIELD14,
        ContactField.USERFIELD15,
        ContactField.USERFIELD16,
        ContactField.USERFIELD17,
        ContactField.USERFIELD18,
        ContactField.USERFIELD19,
        ContactField.USERFIELD20,
        ContactField.IMAGE1_CONTENT_TYPE,
        ContactField.FILE_AS,
        ContactField.YOMI_FIRST_NAME,
        ContactField.YOMI_LAST_NAME,
        ContactField.YOMI_COMPANY,
        ContactField.HOME_ADDRESS,
        ContactField.BUSINESS_ADDRESS,
        ContactField.OTHER_ADDRESS,
    };

    private static final ContactField[] NUMERICAL_FIELDS = {
        ContactField.NUMBER_OF_DISTRIBUTIONLIST,
        ContactField.INTERNAL_USERID,
        ContactField.COLOR_LABEL,
        ContactField.DEFAULT_ADDRESS,
        ContactField.NUMBER_OF_ATTACHMENTS,
    };

    private ContactField[] contentFields;
    private DistListMemberField[] distListContentFields;
    private Random random;

    @Before
    public void setUp() {
        random = new Random();
        contentFields = getContentFields();
        distListContentFields = getDistListContentFields();
    }

     @Test
     public void testDetectEmptyDuplicates() {
        assertSameHash(contact1(), contact2());
    }

     @Test
     public void testDetectDuplicatesInTextualFields() throws Exception {
        for (ContactField field : TEXTUAL_FIELDS) {
            String value = randomString();
            VarCharMapping<Contact> mapping = (VarCharMapping<Contact>) Mappers.CONTACT.get(field);
            Contact contact1 = contact1();
            mapping.set(contact1, value);
            Contact contact2 = contact2();
            mapping.set(contact2, value);
            assertSameHash(contact1, contact2, String.valueOf(field));
        }
    }

     @Test
     public void testDontDetectDifferencesInTextualFields() throws Exception {
        for (ContactField field : TEXTUAL_FIELDS) {
            VarCharMapping<Contact> mapping = (VarCharMapping<Contact>) Mappers.CONTACT.get(field);
            Contact contact1 = contact1();
            mapping.set(contact1, randomString());
            Contact contact2 = contact2();
            mapping.set(contact2, randomString());
            assertDifferentHash(contact1, contact2, String.valueOf(field));
        }
    }

     @Test
     public void testDetectDuplicatesInNumericalFields() throws Exception {
        for (ContactField field : NUMERICAL_FIELDS) {
            Integer value = Integer.valueOf(randomNumber());
            IntegerMapping<Contact> mapping = (IntegerMapping<Contact>) Mappers.CONTACT.get(field);
            Contact contact1 = contact1();
            mapping.set(contact1, value);
            Contact contact2 = contact2();
            mapping.set(contact2, value);
            assertSameHash(contact1, contact2, String.valueOf(field));
        }
    }

     @Test
     public void testDontDetectDifferencesInNumericalFields() throws Exception {
        for (ContactField field : NUMERICAL_FIELDS) {
            IntegerMapping<Contact> mapping = (IntegerMapping<Contact>) Mappers.CONTACT.get(field);
            Contact contact1 = contact1();
            mapping.set(contact1, Integer.valueOf(randomNumber()));
            Contact contact2 = contact2();
            mapping.set(contact2, Integer.valueOf(randomNumber()));
            assertDifferentHash(contact1, contact2, String.valueOf(field));
        }
    }

     @Test
     public void testDontDetectSimilars() {
        Contact contact1 = contact1();
        contact1.setEmail1("otto@example.com");
        Contact contact2 = contact2();
        contact2.setEmail2("otto@example.com");
        assertDifferentHash(contact1, contact2);
    }

     @Test
     public void testDontDetectDifferentImages() {
        Contact contact1 = contact1();
        contact1.setImage1(UUIDs.toByteArray(UUID.randomUUID()));
        contact1.setNumberOfImages(1);
        contact1.setImageContentType("image/png");
        Contact contact2 = contact2();
        contact2.setNumberOfImages(1);
        contact2.setImageContentType("image/png");
        contact2.setImage1(UUIDs.toByteArray(UUID.randomUUID()));
        assertDifferentHash(contact1, contact2);
    }

     @Test
     public void testDetectDuplicateImages() {
        UUID uuid = UUID.randomUUID();
        Contact contact1 = contact1();
        contact1.setImage1(UUIDs.toByteArray(uuid));
        contact1.setImageContentType("image/png");
        Contact contact2 = contact2();
        contact2.setImage1(UUIDs.toByteArray(uuid));
        contact2.setImageContentType("image/png");
        assertSameHash(contact1, contact2);
    }

    private void assertSameHash(Contact contact1, Contact contact2) {
        assertSameHash(contact1, contact2, null);
    }

    private void assertDifferentHash(Contact contact1, Contact contact2) {
        assertDifferentHash(contact1, contact2, null);
    }

    private void assertSameHash(Contact contact1, Contact contact2, String msg) {
        int hash1 = calculateHash(contact1, contentFields, distListContentFields);
        int hash2 = calculateHash(contact2, contentFields, distListContentFields);
        Assert.assertEquals(null != msg ? "Different hashes: " + msg : "Different hashes", hash1, hash2);
    }

    private void assertDifferentHash(Contact contact1, Contact contact2, String msg) {
        int hash1 = calculateHash(contact1, contentFields, distListContentFields);
        int hash2 = calculateHash(contact2, contentFields, distListContentFields);
        Assert.assertFalse(null != msg ? "Same hashes: " + msg : "Same hashes", hash1 == hash2);
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    private int randomNumber() {
        return random.nextInt();
    }

    private static Contact contact1() {
        Contact contact1 = new Contact();
        contact1.setContextId(12);
        contact1.setParentFolderID(19);
        contact1.setObjectID(9798);
        contact1.setUid(UUID.randomUUID().toString());
        return contact1;
    }

    private static Contact contact2() {
        Contact contact2 = new Contact();
        contact2.setContextId(12);
        contact2.setParentFolderID(19);
        contact2.setObjectID(9799);
        contact2.setUid(UUID.randomUUID().toString());
        return contact2;
    }

}
