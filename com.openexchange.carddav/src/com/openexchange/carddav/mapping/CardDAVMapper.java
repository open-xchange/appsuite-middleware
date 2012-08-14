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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.carddav.mapping;

import java.util.EnumMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;

/**
 * {@link CardDAVMapper}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CardDAVMapper extends DefaultMapper<Contact, ContactField> {

    private static final CardDAVMapper INSTANCE = new CardDAVMapper();

    /**
     * Gets the ContactMapper instance.
     *
     * @return The ContactMapper instance.
     */
    public static CardDAVMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link CardDAVMapper}.
     */
    private CardDAVMapper() {
        super();
    }

	@Override
	public Contact newInstance() {
		return new Contact();
	}

	@Override
	public ContactField[] newArray(int size) {
		return new ContactField[size];
	}

	@Override
	public EnumMap<ContactField, Mapping<? extends Object, Contact>> getMappings() {
		return mappings;
	}
	
	@Override
	public Mapping<? extends Object, Contact> get(final ContactField field) throws OXException {
		if (null == field) {
			throw new IllegalArgumentException("field");
		}
		final Mapping<? extends Object, Contact> mapping = getMappings().get(field);
		if (null == mapping) {
			throw OXException.notFound(field.toString());
		}
		return mapping;
	}

	/**
	 * Holds all known contact mappings.
	 */
	private static final EnumMap<ContactField, Mapping<? extends Object, Contact>> mappings;	
	static {
		mappings = new EnumMap<ContactField, Mapping<? extends Object, Contact>>(ContactField.class);
		
        mappings.put(ContactField.DISPLAY_NAME, new StringMapping() {

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

        mappings.put(ContactField.SUR_NAME, new StringMapping() {

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

        mappings.put(ContactField.GIVEN_NAME, new StringMapping() {

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

        mappings.put(ContactField.COMPANY, new StringMapping() {

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
        
        mappings.put(ContactField.EMAIL1, new StringMapping() {

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
                return contact.getEmail1();
            }

			@Override
			public void remove(Contact contact) {
				contact.removeEmail1();
			}
        });

        mappings.put(ContactField.EMAIL2, new StringMapping() {

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
                return contact.getEmail2();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail2(); 
            }
        });
        
        mappings.put(ContactField.EMAIL3, new StringMapping() {

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
                return contact.getEmail3();
            }

            @Override
            public void remove(Contact contact) {
                contact.removeEmail3(); 
            }
        });
        
        mappings.put(ContactField.CELLULAR_TELEPHONE1, new StringMapping() {

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

        mappings.put(ContactField.CELLULAR_TELEPHONE2, new StringMapping() {

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

        mappings.put(ContactField.TELEPHONE_HOME1, new StringMapping() {

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
        
        mappings.put(ContactField.TELEPHONE_BUSINESS1, new StringMapping() {

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

        mappings.put(ContactField.TELEPHONE_BUSINESS2, new StringMapping() {

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

        mappings.put(ContactField.FAX_HOME, new StringMapping() {

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

        mappings.put(ContactField.FAX_BUSINESS, new StringMapping() {

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

        mappings.put(ContactField.FAX_OTHER, new StringMapping() {

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

        mappings.put(ContactField.TELEPHONE_PAGER, new StringMapping() {

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

        mappings.put(ContactField.NOTE, new StringMapping() {

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

        mappings.put(ContactField.URL, new StringMapping() {

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

        mappings.put(ContactField.STREET_HOME, new StringMapping() {

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

        mappings.put(ContactField.POSTAL_CODE_HOME, new StringMapping() {

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

        mappings.put(ContactField.CITY_HOME, new StringMapping() {

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

        mappings.put(ContactField.STATE_HOME, new StringMapping() {

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

        mappings.put(ContactField.COUNTRY_HOME, new StringMapping() {

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

        mappings.put(ContactField.STREET_BUSINESS, new StringMapping() {

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

        mappings.put(ContactField.POSTAL_CODE_BUSINESS, new StringMapping() {

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

        mappings.put(ContactField.CITY_BUSINESS, new StringMapping() {

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

        mappings.put(ContactField.STATE_BUSINESS, new StringMapping() {

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

        mappings.put(ContactField.COUNTRY_BUSINESS, new StringMapping() {

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

        mappings.put(ContactField.PROFESSION, new StringMapping() {

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
        
        mappings.put(ContactField.INSTANT_MESSENGER1, new StringMapping() {

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
        
        mappings.put(ContactField.INSTANT_MESSENGER2, new StringMapping() {

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
        
        mappings.put(ContactField.IMAGE1, new DefaultMapping<byte[], Contact>() {

			@Override
			public boolean isSet(Contact contact) {
				return contact.containsImage1();
			}

			@Override
			public void set(Contact contact, byte[] value) throws OXException {
				contact.setImage1(value);
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
			public boolean truncate(Contact contact, int length) throws OXException {
				return false;
			}
		});

	}	
}
 
