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

package com.openexchange.contacts.json.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.tools.mappings.json.ArrayMapping;
import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
import com.openexchange.groupware.tools.mappings.json.DateMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.groupware.tools.mappings.json.TimeMapping;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.util.Pair;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.session.Session;

/**
 * {@link ContactMapper} - JSON mapper for contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactMapper extends DefaultJsonMapper<Contact, ContactField> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactMapper.class);
    private static final ContactMapper INSTANCE = new ContactMapper();

    private ContactField[] allFields = null;

    /**
     * Gets the ContactMapper instance.
     *
     * @return The ContactMapper instance.
     */
    public static ContactMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ContactMapper}.
     */
    private ContactMapper() {
        super();

    }

	public ContactField[] getAssignedFields(final Contact contact, ContactField... mandatoryFields) {
		if (null == contact) {
			throw new IllegalArgumentException("contact");
		}
		Set<ContactField> setFields = new HashSet<ContactField>();
		for (Entry<ContactField, ? extends JsonMapping<? extends Object, Contact>> entry : getMappings().entrySet()) {
			JsonMapping<? extends Object, Contact> mapping = entry.getValue();
			if (mapping.isSet(contact)) {
				ContactField field = entry.getKey();
				setFields.add(field);
				if (ContactField.IMAGE1.equals(field)) {
					setFields.add(ContactField.IMAGE1_URL); // assume virtual IMAGE1_URL is set, too
				} else if (ContactField.LAST_MODIFIED.equals(field)) {
					setFields.add(ContactField.LAST_MODIFIED_UTC); // assume virtual LAST_MODIFIED_UTC is set, too
                }
			}
		}
		if (null != mandatoryFields) {
			setFields.addAll(Arrays.asList(mandatoryFields));
		}
		return setFields.toArray(newArray(setFields.size()));
    }

    @Override
	public Contact newInstance() {
		return new Contact();
	}

    public ContactField[] getAllFields() {
        if (null == allFields) {
            this.allFields = this.mappings.keySet().toArray(newArray(this.mappings.keySet().size()));
        }
        return this.allFields;
    }

    public ContactField[] getAllFields(EnumSet<ContactField> illegalFields) {
        List<ContactField> fields = new ArrayList<ContactField>();
        for (ContactField field : getAllFields()) {
            if (false == illegalFields.contains(field)) {
                fields.add(field);
            }
        }
        return fields.toArray(new ContactField[fields.size()]);
    }

	@Override
	public ContactField[] newArray(int size) {
		return new ContactField[size];
	}

	@Override
	public EnumMap<ContactField, ? extends JsonMapping<? extends Object, Contact>> getMappings() {
		return this.mappings;
	}

	@Override
	protected EnumMap<ContactField, ? extends JsonMapping<? extends Object, Contact>> createMappings() {

		final EnumMap<ContactField, JsonMapping<? extends Object, Contact>> mappings = new
				EnumMap<ContactField, JsonMapping<? extends Object, Contact>>(ContactField.class);

        mappings.put(ContactField.DISPLAY_NAME, new StringMapping<Contact>(ContactFields.DISPLAY_NAME, Contact.DISPLAY_NAME) {

            @Override
            public void set(Contact contact, String value) {
                contact.setDisplayName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDisplayName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getDisplayName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDisplayName();
            }
        });

        mappings.put(ContactField.SUR_NAME, new StringMapping<Contact>(ContactFields.LAST_NAME, Contact.SUR_NAME) {

            @Override
            public void set(Contact contact, String value) {
                contact.setSurName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSurName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSurName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSurName();
            }
        });

        mappings.put(ContactField.GIVEN_NAME, new StringMapping<Contact>(ContactFields.FIRST_NAME, 501) {

            @Override
            public void set(Contact contact, String value) {
                contact.setGivenName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsGivenName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getGivenName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeGivenName();
            }
        });

        mappings.put(ContactField.MIDDLE_NAME, new StringMapping<Contact>(ContactFields.SECOND_NAME, 503) {

            @Override
            public void set(Contact contact, String value) {
                contact.setMiddleName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMiddleName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getMiddleName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeMiddleName();
            }
        });

        mappings.put(ContactField.SUFFIX, new StringMapping<Contact>(ContactFields.SUFFIX, 504) {

            @Override
            public void set(Contact contact, String value) {
                contact.setSuffix(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSuffix();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSuffix();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSuffix();
            }
        });

        mappings.put(ContactField.TITLE, new StringMapping<Contact>(ContactFields.TITLE, 505) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTitle(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTitle();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTitle();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTitle();
            }
        });

        mappings.put(ContactField.STREET_HOME, new StringMapping<Contact>(ContactFields.STREET_HOME, 506) {

            @Override
            public void set(Contact contact, String value) {
                contact.setStreetHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStreetHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStreetHome();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_HOME, new StringMapping<Contact>(ContactFields.POSTAL_CODE_HOME, 507) {

            @Override
            public void set(Contact contact, String value) {
                contact.setPostalCodeHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPostalCodeHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePostalCodeHome();
            }
        });

        mappings.put(ContactField.CITY_HOME, new StringMapping<Contact>(ContactFields.CITY_HOME, 508) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCityHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCityHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCityHome();
            }
        });

        mappings.put(ContactField.STATE_HOME, new StringMapping<Contact>(ContactFields.STATE_HOME, 509) {

            @Override
            public void set(Contact contact, String value) {
                contact.setStateHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStateHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStateHome();
            }
        });

        mappings.put(ContactField.COUNTRY_HOME, new StringMapping<Contact>(ContactFields.COUNTRY_HOME, 510) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCountryHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCountryHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCountryHome();
            }
        });

        mappings.put(ContactField.MARITAL_STATUS, new StringMapping<Contact>(ContactFields.MARITAL_STATUS, 512) {

            @Override
            public void set(Contact contact, String value) {
                contact.setMaritalStatus(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMaritalStatus();
            }

            @Override
            public String get(Contact contact) {
                return contact.getMaritalStatus();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeMaritalStatus();
            }
        });

        mappings.put(ContactField.NUMBER_OF_CHILDREN, new StringMapping<Contact>(ContactFields.NUMBER_OF_CHILDREN, 513) {

            @Override
            public void set(Contact contact, String value) {
                contact.setNumberOfChildren(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfChildren();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNumberOfChildren();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfChildren();
            }
        });

        mappings.put(ContactField.PROFESSION, new StringMapping<Contact>(ContactFields.PROFESSION, 514) {

            @Override
            public void set(Contact contact, String value) {
                contact.setProfession(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsProfession();
            }

            @Override
            public String get(Contact contact) {
                return contact.getProfession();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeProfession();
            }
        });

        mappings.put(ContactField.NICKNAME, new StringMapping<Contact>(ContactFields.NICKNAME, 515) {

            @Override
            public void set(Contact contact, String value) {
                contact.setNickname(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNickname();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNickname();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNickname();
            }
        });

        mappings.put(ContactField.SPOUSE_NAME, new StringMapping<Contact>(ContactFields.SPOUSE_NAME, 516) {

            @Override
            public void set(Contact contact, String value) {
                contact.setSpouseName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSpouseName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSpouseName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSpouseName();
            }
        });

        mappings.put(ContactField.NOTE, new StringMapping<Contact>(ContactFields.NOTE, 518) {

            @Override
            public void set(Contact contact, String value) {
                contact.setNote(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNote();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNote();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNote();
            }
        });

        mappings.put(ContactField.COMPANY, new StringMapping<Contact>(ContactFields.COMPANY, 569) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCompany();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCompany();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCompany();
            }
        });

        mappings.put(ContactField.DEPARTMENT, new StringMapping<Contact>(ContactFields.DEPARTMENT, 519) {

            @Override
            public void set(Contact contact, String value) {
                contact.setDepartment(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDepartment();
            }

            @Override
            public String get(Contact contact) {
                return contact.getDepartment();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDepartment();
            }
        });

        mappings.put(ContactField.POSITION, new StringMapping<Contact>(ContactFields.POSITION, 520) {

            @Override
            public void set(Contact contact, String value) {
                contact.setPosition(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPosition();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPosition();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePosition();
            }
        });

        mappings.put(ContactField.EMPLOYEE_TYPE, new StringMapping<Contact>(ContactFields.EMPLOYEE_TYPE, 521) {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmployeeType(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmployeeType();
            }

            @Override
            public String get(Contact contact) {
                return contact.getEmployeeType();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmployeeType();
            }
        });

        mappings.put(ContactField.ROOM_NUMBER, new StringMapping<Contact>(ContactFields.ROOM_NUMBER, 522) {

            @Override
            public void set(Contact contact, String value) {
                contact.setRoomNumber(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsRoomNumber();
            }

            @Override
            public String get(Contact contact) {
                return contact.getRoomNumber();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeRoomNumber();
            }
        });

        mappings.put(ContactField.STREET_BUSINESS, new StringMapping<Contact>(ContactFields.STREET_BUSINESS, 523) {

            @Override
            public void set(Contact contact, String value) {
                contact.setStreetBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStreetBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStreetBusiness();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_BUSINESS, new StringMapping<Contact>(ContactFields.POSTAL_CODE_BUSINESS, 525) {

            @Override
            public void set(Contact contact, String value) {
                contact.setPostalCodeBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPostalCodeBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePostalCodeBusiness();
            }
        });

        mappings.put(ContactField.CITY_BUSINESS, new StringMapping<Contact>(ContactFields.CITY_BUSINESS, 526) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCityBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCityBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCityBusiness();
            }
        });

        mappings.put(ContactField.STATE_BUSINESS, new StringMapping<Contact>(ContactFields.STATE_BUSINESS, 527) {

            @Override
            public void set(Contact contact, String value) {
                contact.setStateBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStateBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStateBusiness();
            }
        });

        mappings.put(ContactField.COUNTRY_BUSINESS, new StringMapping<Contact>(ContactFields.COUNTRY_BUSINESS, 528) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCountryBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCountryBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCountryBusiness();
            }
        });

        mappings.put(ContactField.NUMBER_OF_EMPLOYEE, new StringMapping<Contact>(ContactFields.NUMBER_OF_EMPLOYEE, 529) {

            @Override
            public void set(Contact contact, String value) {
                contact.setNumberOfEmployee(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfEmployee();
            }

            @Override
            public String get(Contact contact) {
                return contact.getNumberOfEmployee();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfEmployee();
            }
        });

        mappings.put(ContactField.SALES_VOLUME, new StringMapping<Contact>(ContactFields.SALES_VOLUME, 530) {

            @Override
            public void set(Contact contact, String value) {
                contact.setSalesVolume(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsSalesVolume();
            }

            @Override
            public String get(Contact contact) {
                return contact.getSalesVolume();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeSalesVolume();
            }
        });

        mappings.put(ContactField.TAX_ID, new StringMapping<Contact>(ContactFields.TAX_ID, 531) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTaxID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTaxID();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTaxID();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTaxID();
            }
        });

        mappings.put(ContactField.COMMERCIAL_REGISTER, new StringMapping<Contact>(ContactFields.COMMERCIAL_REGISTER, 532) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCommercialRegister(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCommercialRegister();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCommercialRegister();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCommercialRegister();
            }
        });

        mappings.put(ContactField.BRANCHES, new StringMapping<Contact>(ContactFields.BRANCHES, 533) {

            @Override
            public void set(Contact contact, String value) {
                contact.setBranches(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBranches();
            }

            @Override
            public String get(Contact contact) {
                return contact.getBranches();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeBranches();
            }
        });

        mappings.put(ContactField.BUSINESS_CATEGORY, new StringMapping<Contact>(ContactFields.BUSINESS_CATEGORY, 534) {

            @Override
            public void set(Contact contact, String value) {
                contact.setBusinessCategory(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBusinessCategory();
            }

            @Override
            public String get(Contact contact) {
                return contact.getBusinessCategory();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeBusinessCategory();
            }
        });

        mappings.put(ContactField.INFO, new StringMapping<Contact>(ContactFields.INFO, 535) {

            @Override
            public void set(Contact contact, String value) {
                contact.setInfo(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInfo();
            }

            @Override
            public String get(Contact contact) {
                return contact.getInfo();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInfo();
            }
        });

        mappings.put(ContactField.MANAGER_NAME, new StringMapping<Contact>(ContactFields.MANAGER_NAME, 536) {

            @Override
            public void set(Contact contact, String value) {
                contact.setManagerName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsManagerName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getManagerName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeManagerName();
            }
        });

        mappings.put(ContactField.ASSISTANT_NAME, new StringMapping<Contact>(ContactFields.ASSISTANT_NAME, 537) {

            @Override
            public void set(Contact contact, String value) {
                contact.setAssistantName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAssistantName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAssistantName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAssistantName();
            }
        });

        mappings.put(ContactField.STREET_OTHER, new StringMapping<Contact>(ContactFields.STREET_OTHER, 538) {

            @Override
            public void set(Contact contact, String value) {
                contact.setStreetOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStreetOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStreetOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStreetOther();
            }
        });

        mappings.put(ContactField.POSTAL_CODE_OTHER, new StringMapping<Contact>(ContactFields.POSTAL_CODE_OTHER, 540) {

            @Override
            public void set(Contact contact, String value) {
                contact.setPostalCodeOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPostalCodeOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getPostalCodeOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePostalCodeOther();
            }
        });

        mappings.put(ContactField.CITY_OTHER, new StringMapping<Contact>(ContactFields.CITY_OTHER, 539) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCityOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCityOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCityOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCityOther();
            }
        });

        mappings.put(ContactField.STATE_OTHER, new StringMapping<Contact>(ContactFields.STATE_OTHER, 598) {

            @Override
            public void set(Contact contact, String value) {
                contact.setStateOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsStateOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getStateOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeStateOther();
            }
        });

        mappings.put(ContactField.COUNTRY_OTHER, new StringMapping<Contact>(ContactFields.COUNTRY_OTHER, 541) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCountryOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCountryOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCountryOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCountryOther();
            }
        });

        mappings.put(ContactField.TELEPHONE_ASSISTANT, new StringMapping<Contact>(ContactFields.TELEPHONE_ASSISTANT, 568) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneAssistant(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneAssistant();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneAssistant();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneAssistant();
            }
        });

        mappings.put(ContactField.TELEPHONE_BUSINESS1, new StringMapping<Contact>(ContactFields.TELEPHONE_BUSINESS1, 542) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneBusiness1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneBusiness1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneBusiness1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneBusiness1();
            }
        });

        mappings.put(ContactField.TELEPHONE_BUSINESS2, new StringMapping<Contact>(ContactFields.TELEPHONE_BUSINESS2, 543) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneBusiness2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneBusiness2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneBusiness2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneBusiness2();
            }
        });

        mappings.put(ContactField.FAX_BUSINESS, new StringMapping<Contact>(ContactFields.FAX_BUSINESS, 544) {

            @Override
            public void set(Contact contact, String value) {
                contact.setFaxBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFaxBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFaxBusiness();
            }
        });

        mappings.put(ContactField.TELEPHONE_CALLBACK, new StringMapping<Contact>(ContactFields.TELEPHONE_CALLBACK, 545) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneCallback(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCallback();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneCallback();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneCallback();
            }
        });

        mappings.put(ContactField.TELEPHONE_CAR, new StringMapping<Contact>(ContactFields.TELEPHONE_CAR, 546) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneCar(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCar();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneCar();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneCar();
            }
        });

        mappings.put(ContactField.TELEPHONE_COMPANY, new StringMapping<Contact>(ContactFields.TELEPHONE_COMPANY, 547) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneCompany();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneCompany();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneCompany();
            }
        });

        mappings.put(ContactField.TELEPHONE_HOME1, new StringMapping<Contact>(ContactFields.TELEPHONE_HOME1, 548) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneHome1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneHome1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneHome1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneHome1();
            }
        });

        mappings.put(ContactField.TELEPHONE_HOME2, new StringMapping<Contact>(ContactFields.TELEPHONE_HOME2, 549) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneHome2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneHome2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneHome2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneHome2();
            }
        });

        mappings.put(ContactField.FAX_HOME, new StringMapping<Contact>(ContactFields.FAX_HOME, 550) {

            @Override
            public void set(Contact contact, String value) {
                contact.setFaxHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFaxHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFaxHome();
            }
        });

        mappings.put(ContactField.TELEPHONE_ISDN, new StringMapping<Contact>(ContactFields.TELEPHONE_ISDN, 559) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneISDN(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneISDN();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneISDN();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneISDN();
            }
        });

        mappings.put(ContactField.CELLULAR_TELEPHONE1, new StringMapping<Contact>(ContactFields.CELLULAR_TELEPHONE1, 551) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCellularTelephone1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCellularTelephone1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCellularTelephone1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCellularTelephone1();
            }
        });

        mappings.put(ContactField.CELLULAR_TELEPHONE2, new StringMapping<Contact>(ContactFields.CELLULAR_TELEPHONE2, 552) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCellularTelephone2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCellularTelephone2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCellularTelephone2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCellularTelephone2();
            }
        });

        mappings.put(ContactField.TELEPHONE_OTHER, new StringMapping<Contact>(ContactFields.TELEPHONE_OTHER, 553) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneOther();
            }
        });

        mappings.put(ContactField.FAX_OTHER, new StringMapping<Contact>(ContactFields.FAX_OTHER, 554) {

            @Override
            public void set(Contact contact, String value) {
                contact.setFaxOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFaxOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFaxOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFaxOther();
            }
        });

        mappings.put(ContactField.TELEPHONE_PAGER, new StringMapping<Contact>(ContactFields.TELEPHONE_PAGER, 560) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephonePager(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephonePager();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephonePager();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephonePager();
            }
        });

        mappings.put(ContactField.TELEPHONE_PRIMARY, new StringMapping<Contact>(ContactFields.TELEPHONE_PRIMARY, 561) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephonePrimary(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephonePrimary();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephonePrimary();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephonePrimary();
            }
        });

        mappings.put(ContactField.TELEPHONE_RADIO, new StringMapping<Contact>(ContactFields.TELEPHONE_RADIO, 562) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneRadio(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneRadio();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneRadio();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneRadio();
            }
        });

        mappings.put(ContactField.TELEPHONE_TELEX, new StringMapping<Contact>(ContactFields.TELEPHONE_TELEX, 563) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneTelex(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneTelex();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneTelex();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneTelex();
            }
        });

        mappings.put(ContactField.TELEPHONE_TTYTDD, new StringMapping<Contact>(ContactFields.TELEPHONE_TTYTDD, 564) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneTTYTTD(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneTTYTTD();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneTTYTTD();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneTTYTTD();
            }
        });

        mappings.put(ContactField.INSTANT_MESSENGER1, new StringMapping<Contact>(ContactFields.INSTANT_MESSENGER1, 565) {

            @Override
            public void set(Contact contact, String value) {
                contact.setInstantMessenger1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInstantMessenger1();
            }

            @Override
            public String get(Contact contact) {
                return contact.getInstantMessenger1();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInstantMessenger1();
            }
        });

        mappings.put(ContactField.INSTANT_MESSENGER2, new StringMapping<Contact>(ContactFields.INSTANT_MESSENGER2, 566) {

            @Override
            public void set(Contact contact, String value) {
                contact.setInstantMessenger2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInstantMessenger2();
            }

            @Override
            public String get(Contact contact) {
                return contact.getInstantMessenger2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInstantMessenger2();
            }
        });

        mappings.put(ContactField.TELEPHONE_IP, new StringMapping<Contact>(ContactFields.TELEPHONE_IP, 567) {

            @Override
            public void set(Contact contact, String value) {
                contact.setTelephoneIP(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsTelephoneIP();
            }

            @Override
            public String get(Contact contact) {
                return contact.getTelephoneIP();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeTelephoneIP();
            }
        });

        mappings.put(ContactField.EMAIL1, new StringMapping<Contact>(ContactFields.EMAIL1, 555) {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmail1(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail1();
            }

            @Override
            public String get(Contact contact) {
                return addr2String(contact.getEmail1());
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail1();
            }
        });

        mappings.put(ContactField.EMAIL2, new StringMapping<Contact>(ContactFields.EMAIL2, 556) {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmail2(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail2();
            }

            @Override
            public String get(Contact contact) {
                return addr2String(contact.getEmail2());
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail2();
            }
        });

        mappings.put(ContactField.EMAIL3, new StringMapping<Contact>(ContactFields.EMAIL3, 557) {

            @Override
            public void set(Contact contact, String value) {
                contact.setEmail3(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsEmail3();
            }

            @Override
            public String get(Contact contact) {
                return addr2String(contact.getEmail3());
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail3();
            }
        });

        mappings.put(ContactField.URL, new StringMapping<Contact>(ContactFields.URL, 558) {

            @Override
            public void set(Contact contact, String value) {
                contact.setURL(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsURL();
            }

            @Override
            public String get(Contact contact) {
                return contact.getURL();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeURL();
            }
        });

        mappings.put(ContactField.CATEGORIES, new StringMapping<Contact>(CommonFields.CATEGORIES, 100) {

            @Override
            public void set(Contact contact, String value) {
                contact.setCategories(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCategories();
            }

            @Override
            public String get(Contact contact) {
                return contact.getCategories();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCategories();
            }
        });

        mappings.put(ContactField.USERFIELD01, new StringMapping<Contact>(ContactFields.USERFIELD01, 571) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField01(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField01();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField01();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField01();
            }
        });

        mappings.put(ContactField.USERFIELD02, new StringMapping<Contact>(ContactFields.USERFIELD02, 572) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField02(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField02();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField02();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField02();
            }
        });

        mappings.put(ContactField.USERFIELD03, new StringMapping<Contact>(ContactFields.USERFIELD03, 573) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField03(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField03();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField03();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField03();
            }
        });

        mappings.put(ContactField.USERFIELD04, new StringMapping<Contact>(ContactFields.USERFIELD04, 574) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField04(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField04();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField04();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField04();
            }
        });

        mappings.put(ContactField.USERFIELD05, new StringMapping<Contact>(ContactFields.USERFIELD05, 575) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField05(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField05();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField05();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField05();
            }
        });

        mappings.put(ContactField.USERFIELD06, new StringMapping<Contact>(ContactFields.USERFIELD06, 576) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField06(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField06();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField06();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField06();
            }
        });

        mappings.put(ContactField.USERFIELD07, new StringMapping<Contact>(ContactFields.USERFIELD07, 577) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField07(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField07();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField07();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField07();
            }
        });

        mappings.put(ContactField.USERFIELD08, new StringMapping<Contact>(ContactFields.USERFIELD08, 578) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField08(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField08();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField08();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField08();
            }
        });

        mappings.put(ContactField.USERFIELD09, new StringMapping<Contact>(ContactFields.USERFIELD09, 579) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField09(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField09();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField09();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField09();
            }
        });

        mappings.put(ContactField.USERFIELD10, new StringMapping<Contact>(ContactFields.USERFIELD10, 580) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField10(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField10();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField10();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField10();
            }
        });

        mappings.put(ContactField.USERFIELD11, new StringMapping<Contact>(ContactFields.USERFIELD11, 581) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField11(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField11();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField11();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField11();
            }
        });

        mappings.put(ContactField.USERFIELD12, new StringMapping<Contact>(ContactFields.USERFIELD12, 582) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField12(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField12();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField12();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField12();
            }
        });

        mappings.put(ContactField.USERFIELD13, new StringMapping<Contact>(ContactFields.USERFIELD13, 583) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField13(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField13();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField13();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField13();
            }
        });

        mappings.put(ContactField.USERFIELD14, new StringMapping<Contact>(ContactFields.USERFIELD14, 584) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField14(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField14();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField14();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField14();
            }
        });

        mappings.put(ContactField.USERFIELD15, new StringMapping<Contact>(ContactFields.USERFIELD15, 585) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField15(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField15();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField15();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField15();
            }
        });

        mappings.put(ContactField.USERFIELD16, new StringMapping<Contact>(ContactFields.USERFIELD16, 586) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField16(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField16();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField16();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField16();
            }
        });

        mappings.put(ContactField.USERFIELD17, new StringMapping<Contact>(ContactFields.USERFIELD17, 587) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField17(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField17();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField17();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField17();
            }
        });

        mappings.put(ContactField.USERFIELD18, new StringMapping<Contact>(ContactFields.USERFIELD18, 588) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField18(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField18();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField18();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField18();
            }
        });

        mappings.put(ContactField.USERFIELD19, new StringMapping<Contact>(ContactFields.USERFIELD19, 589) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField19(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField19();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField19();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField19();
            }
        });

        mappings.put(ContactField.USERFIELD20, new StringMapping<Contact>(ContactFields.USERFIELD20, 590) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUserField20(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUserField20();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUserField20();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUserField20();
            }
        });

        mappings.put(ContactField.OBJECT_ID, new IntegerMapping<Contact>(DataFields.ID, DataObject.OBJECT_ID) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setObjectID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsObjectID();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getObjectID();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeObjectID();
            }
        });

        mappings.put(ContactField.NUMBER_OF_DISTRIBUTIONLIST, new IntegerMapping<Contact>(ContactFields.NUMBER_OF_DISTRIBUTIONLIST, 594) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setNumberOfDistributionLists(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMarkAsDistributionlist();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getNumberOfDistributionLists();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfDistributionLists();
            }
        });

        mappings.put(ContactField.DISTRIBUTIONLIST, new ArrayMapping<DistributionListEntryObject, Contact>(ContactFields.DISTRIBUTIONLIST, Contact.DISTRIBUTIONLIST) {

			@Override
			public DistributionListEntryObject[] newArray(int size) {
				return new DistributionListEntryObject[size];
			}

			@Override
			public boolean isSet(Contact contact) {
                return contact.containsDistributionLists();
			}

			@Override
			public void set(Contact contact, DistributionListEntryObject[] value) throws OXException {
                contact.setDistributionList(value);
			}

			@Override
			public DistributionListEntryObject[] get(Contact contact) {
                return contact.getDistributionList();
			}

			@Override
			public void remove(Contact contact) {
                contact.removeDistributionLists();
			}

			@Override
			protected DistributionListEntryObject deserialize(JSONArray array, int index) throws JSONException, OXException {
	            JSONObject entry = array.getJSONObject(index);
	            DistributionListEntryObject member = new DistributionListEntryObject();
                //FIXME: ui sends wrong values for "id": ===========> Bug #21894
                // "distribution_list":[{"id":"","mail_field":0,"mail":"otto@example.com","display_name":"otto"},
                //                      {"id":1,"mail_field":1,"mail":"horst@example.com","display_name":"horst"}]
                if (entry.hasAndNotNull(DataFields.ID) && 0 < entry.getString(DataFields.ID).length()) {
                	member.setEntryID(entry.getInt(DataFields.ID));
                }
                if (entry.hasAndNotNull(FolderChildFields.FOLDER_ID)) {
                	member.setFolderID(entry.getInt(FolderChildFields.FOLDER_ID));
                }
                if (entry.hasAndNotNull(ContactFields.FIRST_NAME)) {
                	member.setFirstname(entry.getString(ContactFields.FIRST_NAME));
                }
                if (entry.hasAndNotNull(ContactFields.LAST_NAME)) {
                	member.setFirstname(entry.getString(ContactFields.LAST_NAME));
                }
                if (entry.hasAndNotNull(ContactFields.DISPLAY_NAME)) {
                	member.setDisplayname(entry.getString(ContactFields.DISPLAY_NAME));
                }
                if (entry.hasAndNotNull(DistributionListFields.MAIL)) {
                	member.setEmailaddress(entry.getString(DistributionListFields.MAIL));
                }
                if (entry.hasAndNotNull(DistributionListFields.MAIL_FIELD)) {
                	member.setEmailfield(entry.getInt(DistributionListFields.MAIL_FIELD));
                }
                return member;
			}

			@Override
			public Object serialize(Contact from, TimeZone timeZone, Session session) throws JSONException {
				JSONArray jsonArray = null;
				DistributionListEntryObject[] distributionList = this.get(from);
				if (null != distributionList) {
					jsonArray = new JSONArray();
			        for (int i = 0; i < distributionList.length; i++) {
			            JSONObject entry = new JSONObject();
			            int emailField = distributionList[i].getEmailfield();
			            if (DistributionListEntryObject.INDEPENDENT != emailField) {
			                entry.put(DataFields.ID, distributionList[i].getEntryID());
			                entry.put(FolderChildFields.FOLDER_ID, distributionList[i].getFolderID());
			            }
			            entry.put(DistributionListFields.MAIL, distributionList[i].getEmailaddress());
			            entry.put(ContactFields.DISPLAY_NAME, distributionList[i].getDisplayname());
			            entry.put(DistributionListFields.MAIL_FIELD, emailField);
			            jsonArray.put(entry);
			        }
		        }
		        return jsonArray;
			}
		});

        mappings.put(ContactField.FOLDER_ID, new IntegerMapping<Contact>(FolderChildFields.FOLDER_ID, FolderChildObject.FOLDER_ID) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setParentFolderID(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsParentFolderID();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getParentFolderID();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeParentFolderID();
            }
        });

//        mappings.put(ContactField.CONTEXTID, new IntegerMapping<Contact>(null, 593) {
//
//            @Override
//            public void set(Contact contact, Integer value) {
//                contact.setContextId(value);
//            }
//
//            @Override
//            public boolean isSet(Contact contact) {
//                return contact.containsContextId();
//            }
//
//            @Override
//            public Integer get(Contact contact) {
//                return contact.getContextId();
//            }
//
//            @Override
//            public void remove(Contact contact) {
//                contact.removeContextID();
//            }
//        });

        mappings.put(ContactField.PRIVATE_FLAG, new BooleanMapping<Contact>(CommonFields.PRIVATE_FLAG, CommonObject.PRIVATE_FLAG) {

            @Override
            public void set(Contact contact, Boolean value) {
                contact.setPrivateFlag(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsPrivateFlag();
            }

            @Override
            public Boolean get(Contact contact) {
                return contact.getPrivateFlag();
            }

            @Override
            public void remove(Contact contact) {
                contact.removePrivateFlag();
            }
        });

        mappings.put(ContactField.CREATED_BY, new IntegerMapping<Contact>(DataFields.CREATED_BY, 2) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setCreatedBy(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCreatedBy();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getCreatedBy();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCreatedBy();
            }
        });

        mappings.put(ContactField.MODIFIED_BY, new IntegerMapping<Contact>(DataFields.MODIFIED_BY, 3) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setModifiedBy(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsModifiedBy();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getModifiedBy();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeModifiedBy();
            }
        });

        mappings.put(ContactField.CREATION_DATE, new TimeMapping<Contact>(DataFields.CREATION_DATE, 4) {

            @Override
            public void set(Contact contact, Date value) {
                contact.setCreationDate(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsCreationDate();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getCreationDate();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeCreationDate();
            }
        });

        mappings.put(ContactField.LAST_MODIFIED, new TimeMapping<Contact>(DataFields.LAST_MODIFIED, DataObject.LAST_MODIFIED) {

            @Override
            public void set(Contact contact, Date value) {
                contact.setLastModified(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLastModified();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getLastModified();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeLastModified();
            }
        });

        mappings.put(ContactField.BIRTHDAY, new DateMapping<Contact>(ContactFields.BIRTHDAY, 511) {

            @Override
            public void set(Contact contact, Date value) {
                contact.setBirthday(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsBirthday();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getBirthday();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeBirthday();
            }
        });

        mappings.put(ContactField.ANNIVERSARY, new DateMapping<Contact>(ContactFields.ANNIVERSARY, 517) {

            @Override
            public void set(Contact contact, Date value) {
                contact.setAnniversary(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAnniversary();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getAnniversary();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAnniversary();
            }
        });

        mappings.put(ContactField.IMAGE1, new DefaultJsonMapping<byte[], Contact>(ContactFields.IMAGE1, Contact.IMAGE1) {

			@Override
			public void set(Contact contact, byte[] value) {
			    contact.setImage1(value);
			}

			@Override
			public boolean isSet(Contact contact) {
			    return contact.containsImage1();
			}

			@Override
			public byte[] get(Contact contact) {
			    return contact.getImage1();
			}

			@Override
			public void remove(Contact contact) {
			    contact.removeImage1();
			}

			@Override
			public void deserialize(JSONObject from, Contact to) throws JSONException, OXException {
				Object value = from.get(getAjaxName());
				if (null == value || JSONObject.NULL.equals(value) || 0 == value.toString().length()) {
					to.setImage1(null);
				} else if (byte[].class.isInstance(value)) {
					to.setImage1((byte[])value);
				} else {
					throw new JSONException("unable to deserialize image data");
				}
			}

			@Override
			public void serialize(Contact from, JSONObject to) throws JSONException {
				// always serialize as URL
				try {
					ContactMapper.getInstance().get(ContactField.IMAGE1_URL).serialize(from, to);
				} catch (OXException e) {
					throw new JSONException(e);
				}
			}

			@Override
			public void serialize(Contact from, JSONObject to, TimeZone timeZone) throws JSONException {
				// always serialize as URL
				try {
					ContactMapper.getInstance().get(ContactField.IMAGE1_URL).serialize(from, to, timeZone);
				} catch (OXException e) {
					throw new JSONException(e);
				}
			}

			@Override
			public void serialize(Contact from, JSONObject to, TimeZone timeZone, Session session) throws JSONException {
				// always serialize as URL
				try {
					ContactMapper.getInstance().get(ContactField.IMAGE1_URL).serialize(from, to, timeZone, session);
				} catch (OXException e) {
					throw new JSONException(e);
				}
			}

			@Override
			public Object serialize(Contact from, TimeZone timeZone, Session session) throws JSONException {
				// always serialize as URL
				try {
					return ContactMapper.getInstance().get(ContactField.IMAGE1_URL).serialize(from, timeZone, session);
				} catch (OXException e) {
					throw new JSONException(e);
				}
			}
        });

        mappings.put(ContactField.IMAGE1_URL, new StringMapping<Contact>(ContactFields.IMAGE1_URL, Contact.IMAGE1_URL) {

            @Override
            public void set(Contact contact, String value) {
                LOG.debug("Ignoring request to set 'image_url' in contact to '{}'.", value);
            }

            @Override
            public boolean isSet(Contact contact) {
            	return 0 < contact.getNumberOfImages() || contact.containsImage1() && null != contact.getImage1();
            }

            @Override
        	public Object serialize(Contact from, TimeZone timeZone, Session session) throws JSONException {
                Pair<ImageDataSource, ImageLocation> imageData = ContactUtil.prepareImageData(from);
                if (imageData == null) {
                    return JSONObject.NULL;
                } else {
                    try {
                        return imageData.getFirst().generateUrl(imageData.getSecond(), session);
                    } catch (OXException e) {
                        throw new JSONException(e);
                    }
                }
        	}

            @Override
            public String get(Contact contact) {
            	return null;
            }

            @Override
            public void remove(Contact contact) {
                LOG.debug("Ignoring request to remove 'image_url' from contact.");
            }
        });

        mappings.put(ContactField.IMAGE_LAST_MODIFIED, new DateMapping<Contact>("image_last_modified", 597) {

            @Override
            public void set(Contact contact, Date value) {
                contact.setImageLastModified(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImageLastModified();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getImageLastModified();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeImageLastModified();
            }
        });

        mappings.put(ContactField.INTERNAL_USERID, new IntegerMapping<Contact>(ContactFields.USER_ID, 524) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setInternalUserId(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsInternalUserId();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getInternalUserId();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeInternalUserId();
            }
        });

        mappings.put(ContactField.COLOR_LABEL, new IntegerMapping<Contact>(CommonFields.COLORLABEL, 102) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setLabel(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLabel();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getLabel();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeLabel();
            }
        });

        mappings.put(ContactField.FILE_AS, new StringMapping<Contact>(ContactFields.FILE_AS, Contact.FILE_AS) {

            @Override
            public void set(Contact contact, String value) {
                contact.setFileAs(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsFileAs();
            }

            @Override
            public String get(Contact contact) {
                return contact.getFileAs();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeFileAs();
            }

        	@Override
        	public void serialize(Contact from, JSONObject to) throws JSONException {
        		if (this.isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			super.serialize(from, to);
        		}
        	}

        	@Override
        	public void serialize(Contact from, JSONObject to, TimeZone timeZone) throws JSONException {
        		if (this.isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			super.serialize(from, to, timeZone);
        		}
        	}

        	@Override
        	public void serialize(Contact from, JSONObject to, TimeZone timeZone, Session session) throws JSONException {
        		if (isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			super.serialize(from, to, timeZone, session);
        		}
        	}

        	@Override
        	public Object serialize(Contact from, TimeZone timeZone, Session session) throws JSONException {
        		if (isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			return super.serialize(from, timeZone, session);
        		} else {
        			return null;
        		}
        	}

        });

        mappings.put(ContactField.DEFAULT_ADDRESS, new IntegerMapping<Contact>(ContactFields.DEFAULT_ADDRESS, Contact.DEFAULT_ADDRESS) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setDefaultAddress(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsDefaultAddress();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getDefaultAddress();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeDefaultAddress();
            }

        	@Override
        	public void serialize(Contact from, JSONObject to) throws JSONException {
        		if (this.isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			super.serialize(from, to);
        		}
        	}

        	@Override
        	public void serialize(Contact from, JSONObject to, TimeZone timeZone) throws JSONException {
        		if (this.isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			super.serialize(from, to, timeZone);
        		}
        	}

        	@Override
        	public void serialize(Contact from, JSONObject to, TimeZone timeZone, Session session) throws JSONException {
        		if (isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			super.serialize(from, to, timeZone, session);
        		}
        	}

        	@Override
        	public Object serialize(Contact from, TimeZone timeZone, Session session) throws JSONException {
        		if (isSet(from)) {
        			// only serialize if set; workaround for bug #13960
        			return super.serialize(from, timeZone, session);
        		} else {
        			return null;
        		}
        	}

        });

        mappings.put(ContactField.MARK_AS_DISTRIBUTIONLIST, new BooleanMapping<Contact>(ContactFields.MARK_AS_DISTRIBUTIONLIST, 602) {

            @Override
            public void set(Contact contact, Boolean value) {
                contact.setMarkAsDistributionlist(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsMarkAsDistributionlist();
            }

            @Override
            public Boolean get(Contact contact) {
                return contact.getMarkAsDistribtuionlist();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeMarkAsDistributionlist();
            }
        });

        mappings.put(ContactField.NUMBER_OF_ATTACHMENTS, new IntegerMapping<Contact>(CommonFields.NUMBER_OF_ATTACHMENTS, 104) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setNumberOfAttachments(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsNumberOfAttachments();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getNumberOfAttachments();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeNumberOfAttachments();
            }
        });

        mappings.put(ContactField.YOMI_FIRST_NAME, new StringMapping<Contact>(ContactFields.YOMI_FIRST_NAME, Contact.YOMI_FIRST_NAME) {

            @Override
            public void set(Contact contact, String value) {
                contact.setYomiFirstName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiFirstName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getYomiFirstName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeYomiFirstName();
            }
        });

        mappings.put(ContactField.YOMI_LAST_NAME, new StringMapping<Contact>(ContactFields.YOMI_LAST_NAME, Contact.YOMI_LAST_NAME) {

            @Override
            public void set(Contact contact, String value) {
                contact.setYomiLastName(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiLastName();
            }

            @Override
            public String get(Contact contact) {
                return contact.getYomiLastName();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeYomiLastName();
            }
        });

        mappings.put(ContactField.YOMI_COMPANY, new StringMapping<Contact>(ContactFields.YOMI_COMPANY, Contact.YOMI_COMPANY) {

            @Override
            public void set(Contact contact, String value) {
                contact.setYomiCompany(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsYomiCompany();
            }

            @Override
            public String get(Contact contact) {
                return contact.getYomiCompany();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeYomiCompany();
            }
        });

        mappings.put(ContactField.NUMBER_OF_IMAGES, new IntegerMapping<Contact>(ContactFields.NUMBER_OF_IMAGES, 596) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setNumberOfImages(value);
            }

            @Override
            public boolean isSet(Contact contact) {
            	//TODO: create Contact.containsNumberOfImages() method
                return contact.containsImage1();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getNumberOfImages();
            }

            @Override
            public void remove(Contact contact) {
            	//TODO: create Contact.containsNumberOfImages() method
            }
        });

        mappings.put(ContactField.IMAGE1_CONTENT_TYPE, new StringMapping<Contact>("image1_content_type", Contact.IMAGE1_CONTENT_TYPE) {

            @Override
            public void set(Contact contact, String value) {
                contact.setImageContentType(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsImageContentType();
            }

            @Override
            public String get(Contact contact) {
                return contact.getImageContentType();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeImageContentType();
            }
        });

        mappings.put(ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, new DateMapping<Contact>(CommonFields.LAST_MODIFIED_OF_NEWEST_ATTACHMENT_UTC, 105) {

            @Override
            public void set(Contact contact, Date value) {
                contact.setLastModifiedOfNewestAttachment(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsLastModifiedOfNewestAttachment();
            }

            @Override
            public Date get(Contact contact) {
                return contact.getLastModifiedOfNewestAttachment();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeLastModifiedOfNewestAttachment();
            }
        });

        mappings.put(ContactField.USE_COUNT, new IntegerMapping<Contact>(ContactFields.USE_COUNT, 608) {

            @Override
            public void set(Contact contact, Integer value) {
                contact.setUseCount(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUseCount();
            }

            @Override
            public Integer get(Contact contact) {
                return contact.getUseCount();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUseCount();
            }
        });

        mappings.put(ContactField.LAST_MODIFIED_UTC, new DateMapping<Contact>(DataFields.LAST_MODIFIED_UTC, DataObject.LAST_MODIFIED_UTC) {

			@Override
			public boolean isSet(Contact contact) {
				return contact.containsLastModified();
			}

			@Override
			public void set(Contact contact, Date value) throws OXException {
				contact.setLastModified(value);
			}

			@Override
			public Date get(Contact contact) {
				return contact.getLastModified();
			}

			@Override
			public void remove(Contact contact) {
				contact.removeLastModified();
			}
        });

        mappings.put(ContactField.HOME_ADDRESS, new StringMapping<Contact>(ContactFields.ADDRESS_HOME, Contact.ADDRESS_HOME) {

            @Override
            public void set(Contact contact, String value) {
                contact.setAddressHome(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressHome();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAddressHome();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAddressHome();
            }
        });

        mappings.put(ContactField.BUSINESS_ADDRESS, new StringMapping<Contact>(ContactFields.ADDRESS_BUSINESS, Contact.ADDRESS_BUSINESS) {

            @Override
            public void set(Contact contact, String value) {
                contact.setAddressBusiness(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressBusiness();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAddressBusiness();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAddressBusiness();
            }
        });

        mappings.put(ContactField.OTHER_ADDRESS, new StringMapping<Contact>(ContactFields.ADDRESS_OTHER, Contact.ADDRESS_OTHER) {

            @Override
            public void set(Contact contact, String value) {
                contact.setAddressOther(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsAddressOther();
            }

            @Override
            public String get(Contact contact) {
                return contact.getAddressOther();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeAddressOther();
            }
        });

        mappings.put(ContactField.UID, new StringMapping<Contact>(CommonFields.UID, CommonObject.UID) {

            @Override
            public void set(Contact contact, String value) {
                contact.setUid(value);
            }

            @Override
            public boolean isSet(Contact contact) {
                return contact.containsUid();
            }

            @Override
            public String get(Contact contact) {
                return contact.getUid();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeUid();
            }
        });

        mappings.put(ContactField.SORT_NAME, new StringMapping<Contact>(ContactFields.SORT_NAME, Contact.SPECIAL_SORTING) {

            @Override
            public void set(Contact contact, String value) {
                // no
            }

            @Override
            public boolean isSet(Contact contact) {
                return true;
            }

            @Override
            public String get(Contact contact) {
                return contact.getSortName();
            }

            @Override
            public void remove(Contact contact) {
                // no
            }
        });

		return mappings;
	}

	static String addr2String(final String primaryAddress) {
        if (null == primaryAddress) {
            return primaryAddress;
        }
        try {
            final QuotedInternetAddress addr = new QuotedInternetAddress(primaryAddress);
            final String sAddress = addr.getAddress();
            final int pos = null == sAddress ? 0 : sAddress.indexOf('/');
            if (pos <= 0) {
                // No slash character present
                return addr.toUnicodeString();
            }
            final StringBuilder sb = new StringBuilder(32);
            final String personal = addr.getPersonal();
            if (null == personal) {
                sb.append(MimeMessageUtility.prepareAddress(sAddress.substring(0, pos)));
            } else {
                sb.append(preparePersonal(personal));
                sb.append(" <").append(MimeMessageUtility.prepareAddress(sAddress.substring(0, pos))).append('>');
            }
            return sb.toString();
        } catch (final Exception e) {
            return primaryAddress;
        }
    }

    /**
     * Prepares specified personal string by surrounding it with quotes if needed.
     *
     * @param personal The personal
     * @return The prepared personal
     */
    static String preparePersonal(final String personal) {
        return MimeMessageUtility.quotePhrase(personal, false);
    }

}
