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

package com.openexchange.publish.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.publish.EscapeMode;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.Publications;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link ContactFolderLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactFolderLoader implements PublicationDataLoaderService {

    private final ContactService contactService;

    /**
     * Initializes a new {@link ContactFolderLoader}.
     * @param contacts
     */
    public ContactFolderLoader(final ContactService contactService) {
        super();
        this.contactService = contactService;
    }

    @Override
    public Collection<? extends Object> load(Publication publication, EscapeMode escapeMode) throws OXException {
        PublicationSession session = new PublicationSession(publication);
        SortOptions sortOptions = new SortOptions(ContactField.GIVEN_NAME, Order.ASCENDING);
        SearchIterator<Contact> searchIterator = contactService.getAllContacts(session, publication.getEntityId(), sortOptions);
    	try {
    	    List<Contact> list = new LinkedList<Contact>();
    		while (searchIterator.hasNext()) {
                Contact next = searchIterator.next();
                if (false == next.getMarkAsDistribtuionlist()) {
                    list.add(null != escapeMode && EscapeMode.NONE != escapeMode ? new EscapingContact(next, escapeMode) : next);
                }
    		}
    		// FIXME add sorting
    		return list;
    	} finally {
    	    SearchIterators.close(searchIterator);
    	}
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static class EscapingContact extends Contact {

        private static final long serialVersionUID = 7925325989240435735L;

        private final Contact contact;
        private final EscapeMode escapeMode;

        EscapingContact(Contact contact, EscapeMode escapeMode) {
            super();
            this.contact = contact;
            this.escapeMode = escapeMode;
        }

        @Override
        public int getParentFolderID() {
            return contact.getParentFolderID();
        }

        @Override
        public void setParentFolderID(int parentFolderId) {
            contact.setParentFolderID(parentFolderId);
        }

        @Override
        public void removeParentFolderID() {
            contact.removeParentFolderID();
        }

        @Override
        public void setProperty(String name, Object value) {
            contact.setProperty(name, value);
        }

        @Override
        public boolean containsParentFolderID() {
            return contact.containsParentFolderID();
        }

        @Override
        public int getObjectID() {
            return contact.getObjectID();
        }

        @Override
        public int getCreatedBy() {
            return contact.getCreatedBy();
        }

        @Override
        public int getModifiedBy() {
            return contact.getModifiedBy();
        }

        @Override
        public <V> V getProperty(String name) {
            return contact.getProperty(name);
        }

        @Override
        public Date getCreationDate() {
            return contact.getCreationDate();
        }

        @Override
        public Date getLastModified() {
            return contact.getLastModified();
        }

        @Override
        public String getTopic() {
            return escape(contact.getTopic());
        }

        @Override
        public void setObjectID(int object_id) {
            contact.setObjectID(object_id);
        }

        @Override
        public void setCreatedBy(int created_by) {
            contact.setCreatedBy(created_by);
        }

        @Override
        public <V> V removeProperty(String name) {
            return contact.removeProperty(name);
        }

        @Override
        public void setModifiedBy(int modified_by) {
            contact.setModifiedBy(modified_by);
        }

        @Override
        public void setCreationDate(Date creation_date) {
            contact.setCreationDate(creation_date);
        }

        @Override
        public void setLastModified(Date last_modified) {
            contact.setLastModified(last_modified);
        }

        @Override
        public void setTopic(String topic) {
            contact.setTopic(topic);
        }

        @Override
        public void setMap(Map<String, ? extends Object> map) {
            contact.setMap(map);
        }

        @Override
        public void removeObjectID() {
            contact.removeObjectID();
        }

        @Override
        public Map<String, Object> getExtendedProperties() {
            return contact.getExtendedProperties();
        }

        @Override
        public void removeCreatedBy() {
            contact.removeCreatedBy();
        }

        @Override
        public void removeModifiedBy() {
            contact.removeModifiedBy();
        }

        @Override
        public void removeMap() {
            contact.removeMap();
        }

        @Override
        public void removeCreationDate() {
            contact.removeCreationDate();
        }

        @Override
        public boolean containsMap() {
            return contact.containsMap();
        }

        @Override
        public String getCategories() {
            return escape(contact.getCategories());
        }

        @Override
        public void removeLastModified() {
            contact.removeLastModified();
        }

        @Override
        public Marker getMarker() {
            return contact.getMarker();
        }

        @Override
        public void removeTopic() {
            contact.removeTopic();
        }

        @Override
        public Map<String, Object> getMap() {
            return contact.getMap();
        }

        @Override
        public boolean containsObjectID() {
            return contact.containsObjectID();
        }

        @Override
        public int getPersonalFolderID() {
            return contact.getPersonalFolderID();
        }

        @Override
        public int getNumberOfAttachments() {
            return contact.getNumberOfAttachments();
        }

        @Override
        public boolean containsCreatedBy() {
            return contact.containsCreatedBy();
        }

        @Override
        public boolean containsModifiedBy() {
            return contact.containsModifiedBy();
        }

        @Override
        public Date getLastModifiedOfNewestAttachment() {
            return contact.getLastModifiedOfNewestAttachment();
        }

        @Override
        public boolean containsCreationDate() {
            return contact.containsCreationDate();
        }

        @Override
        public boolean getPrivateFlag() {
            return contact.getPrivateFlag();
        }

        @Override
        public boolean containsLastModified() {
            return contact.containsLastModified();
        }

        @Override
        public int getLabel() {
            return contact.getLabel();
        }

        @Override
        public boolean containsTopic() {
            return contact.containsTopic();
        }

        @Override
        public String getUid() {
            return escape(contact.getUid());
        }

        @Override
        public String getFilename() {
            return escape(contact.getFilename());
        }

        @Override
        public void setExtendedProperties(Map<? extends String, ? extends Serializable> extendedProperties) {
            contact.setExtendedProperties(extendedProperties);
        }

        @Override
        public void addExtendedProperties(Map<? extends String, ? extends Serializable> extendedProperties) {
            contact.addExtendedProperties(extendedProperties);
        }

        @Override
        public void addExtendedProperty(String name, Serializable value) {
            contact.addExtendedProperty(name, value);
        }

        @Override
        public void putExtendedProperty(String name, Serializable value) {
            contact.putExtendedProperty(name, value);
        }

        @Override
        public void setCategories(String categories) {
            contact.setCategories(categories);
        }

        @Override
        public void setMarker(Marker marker) {
            contact.setMarker(marker);
        }

        @Override
        public void setPersonalFolderID(int personal_folder_id) {
            contact.setPersonalFolderID(personal_folder_id);
        }

        @Override
        public void setNumberOfAttachments(int number_of_attachments) {
            contact.setNumberOfAttachments(number_of_attachments);
        }

        @Override
        public void setLastModifiedOfNewestAttachment(Date lastModifiedOfNewestAttachment) {
            contact.setLastModifiedOfNewestAttachment(lastModifiedOfNewestAttachment);
        }

        @Override
        public void setPrivateFlag(boolean privateFlag) {
            contact.setPrivateFlag(privateFlag);
        }

        @Override
        public void setLabel(int label) {
            contact.setLabel(label);
        }

        @Override
        public void setUid(String uid) {
            contact.setUid(uid);
        }

        @Override
        public void setFilename(String filename) {
            contact.setFilename(filename);
        }

        @Override
        public void removeExtendedProperties() {
            contact.removeExtendedProperties();
        }

        @Override
        public void removeCategories() {
            contact.removeCategories();
        }

        @Override
        public void removePersonalFolderID() {
            contact.removePersonalFolderID();
        }

        @Override
        public void removeNumberOfAttachments() {
            contact.removeNumberOfAttachments();
        }

        @Override
        public void removeLastModifiedOfNewestAttachment() {
            contact.removeLastModifiedOfNewestAttachment();
        }

        @Override
        public void removePrivateFlag() {
            contact.removePrivateFlag();
        }

        @Override
        public void removeLabel() {
            contact.removeLabel();
        }

        @Override
        public void removeUid() {
            contact.removeUid();
        }

        @Override
        public void removeFilename() {
            contact.removeFilename();
        }

        @Override
        public boolean containsExtendedProperties() {
            return contact.containsExtendedProperties();
        }

        @Override
        public boolean containsCategories() {
            return contact.containsCategories();
        }

        @Override
        public boolean containsPersonalFolderID() {
            return contact.containsPersonalFolderID();
        }

        @Override
        public boolean containsNumberOfAttachments() {
            return contact.containsNumberOfAttachments();
        }

        @Override
        public boolean containsLastModifiedOfNewestAttachment() {
            return contact.containsLastModifiedOfNewestAttachment();
        }

        @Override
        public boolean containsPrivateFlag() {
            return contact.containsPrivateFlag();
        }

        @Override
        public boolean containsLabel() {
            return contact.containsLabel();
        }

        @Override
        public boolean containsUid() {
            return contact.containsUid();
        }

        @Override
        public boolean containsFilename() {
            return contact.containsFilename();
        }

        @Override
        public void addWarning(OXException warning) {
            contact.addWarning(warning);
        }

        @Override
        public Collection<OXException> getWarnings() {
            return contact.getWarnings();
        }

        @Override
        public String getDisplayName() {
            return escape(contact.getDisplayName());
        }

        @Override
        public String getGivenName() {
            return escape(contact.getGivenName());
        }

        @Override
        public String getSurName() {
            return escape(contact.getSurName());
        }

        @Override
        public String getMiddleName() {
            return escape(contact.getMiddleName());
        }

        @Override
        public String getSuffix() {
            return escape(contact.getSuffix());
        }

        @Override
        public String getTitle() {
            return escape(contact.getTitle());
        }

        @Override
        public String getStreetHome() {
            return escape(contact.getStreetHome());
        }

        @Override
        public String getPostalCodeHome() {
            return escape(contact.getPostalCodeHome());
        }

        @Override
        public String getCityHome() {
            return escape(contact.getCityHome());
        }

        @Override
        public String getStateHome() {
            return escape(contact.getStateHome());
        }

        @Override
        public String getCountryHome() {
            return escape(contact.getCountryHome());
        }

        @Override
        public Date getBirthday() {
            return contact.getBirthday();
        }

        @Override
        public String getMaritalStatus() {
            return escape(contact.getMaritalStatus());
        }

        @Override
        public String getNumberOfChildren() {
            return contact.getNumberOfChildren();
        }

        @Override
        public String getProfession() {
            return escape(contact.getProfession());
        }

        @Override
        public String getNickname() {
            return escape(contact.getNickname());
        }

        @Override
        public String getSpouseName() {
            return escape(contact.getSpouseName());
        }

        @Override
        public Date getAnniversary() {
            return contact.getAnniversary();
        }

        @Override
        public String getNote() {
            return escape(contact.getNote());
        }

        @Override
        public String getDepartment() {
            return escape(contact.getDepartment());
        }

        @Override
        public String getPosition() {
            return escape(contact.getPosition());
        }

        @Override
        public String getEmployeeType() {
            return escape(contact.getEmployeeType());
        }

        @Override
        public String getRoomNumber() {
            return escape(contact.getRoomNumber());
        }

        @Override
        public String getStreetBusiness() {
            return escape(contact.getStreetBusiness());
        }

        @Override
        public String getPostalCodeBusiness() {
            return escape(contact.getPostalCodeBusiness());
        }

        @Override
        public String getCityBusiness() {
            return escape(contact.getCityBusiness());
        }

        @Override
        public String getStateBusiness() {
            return escape(contact.getStateBusiness());
        }

        @Override
        public String getCountryBusiness() {
            return escape(contact.getCountryBusiness());
        }

        @Override
        public String getNumberOfEmployee() {
            return contact.getNumberOfEmployee();
        }

        @Override
        public String getSalesVolume() {
            return escape(contact.getSalesVolume());
        }

        @Override
        public String getTaxID() {
            return escape(contact.getTaxID());
        }

        @Override
        public String getCommercialRegister() {
            return escape(contact.getCommercialRegister());
        }

        @Override
        public String getBranches() {
            return escape(contact.getBranches());
        }

        @Override
        public String getBusinessCategory() {
            return escape(contact.getBusinessCategory());
        }

        @Override
        public String getInfo() {
            return escape(contact.getInfo());
        }

        @Override
        public String getManagerName() {
            return escape(contact.getManagerName());
        }

        @Override
        public String getAssistantName() {
            return escape(contact.getAssistantName());
        }

        @Override
        public String getStreetOther() {
            return escape(contact.getStreetOther());
        }

        @Override
        public String getPostalCodeOther() {
            return escape(contact.getPostalCodeOther());
        }

        @Override
        public String getCityOther() {
            return escape(contact.getCityOther());
        }

        @Override
        public String getStateOther() {
            return escape(contact.getStateOther());
        }

        @Override
        public String getCountryOther() {
            return escape(contact.getCountryOther());
        }

        @Override
        public String getTelephoneBusiness1() {
            return escape(contact.getTelephoneBusiness1());
        }

        @Override
        public String getTelephoneBusiness2() {
            return escape(contact.getTelephoneBusiness2());
        }

        @Override
        public String getFaxBusiness() {
            return escape(contact.getFaxBusiness());
        }

        @Override
        public String getTelephoneCallback() {
            return escape(contact.getTelephoneCallback());
        }

        @Override
        public String getTelephoneCar() {
            return escape(contact.getTelephoneCar());
        }

        @Override
        public String getTelephoneCompany() {
            return escape(contact.getTelephoneCompany());
        }

        @Override
        public String getTelephoneHome1() {
            return escape(contact.getTelephoneHome1());
        }

        @Override
        public String getTelephoneHome2() {
            return escape(contact.getTelephoneHome2());
        }

        @Override
        public String getFaxHome() {
            return escape(contact.getFaxHome());
        }

        @Override
        public String getCellularTelephone1() {
            return escape(contact.getCellularTelephone1());
        }

        @Override
        public String getCellularTelephone2() {
            return escape(contact.getCellularTelephone2());
        }

        @Override
        public String getTelephoneOther() {
            return escape(contact.getTelephoneOther());
        }

        @Override
        public String getFaxOther() {
            return escape(contact.getFaxOther());
        }

        @Override
        public String getEmail1() {
            return escape(contact.getEmail1());
        }

        @Override
        public String getEmail2() {
            return escape(contact.getEmail2());
        }

        @Override
        public String getEmail3() {
            return escape(contact.getEmail3());
        }

        @Override
        public String getURL() {
            return escape(contact.getURL());
        }

        @Override
        public String getTelephoneISDN() {
            return escape(contact.getTelephoneISDN());
        }

        @Override
        public String getTelephonePager() {
            return escape(contact.getTelephonePager());
        }

        @Override
        public String getTelephonePrimary() {
            return escape(contact.getTelephonePrimary());
        }

        @Override
        public String getTelephoneRadio() {
            return escape(contact.getTelephoneRadio());
        }

        @Override
        public String getTelephoneTelex() {
            return escape(contact.getTelephoneTelex());
        }

        @Override
        public String getTelephoneTTYTTD() {
            return escape(contact.getTelephoneTTYTTD());
        }

        @Override
        public String getInstantMessenger1() {
            return escape(contact.getInstantMessenger1());
        }

        @Override
        public String getInstantMessenger2() {
            return escape(contact.getInstantMessenger2());
        }

        @Override
        public String getTelephoneIP() {
            return escape(contact.getTelephoneIP());
        }

        @Override
        public String getTelephoneAssistant() {
            return escape(contact.getTelephoneAssistant());
        }

        @Override
        public int getDefaultAddress() {
            return contact.getDefaultAddress();
        }

        @Override
        public String getCompany() {
            return escape(contact.getCompany());
        }

        @Override
        public byte[] getImage1() {
            return contact.getImage1();
        }

        @Override
        public String getImageContentType() {
            return contact.getImageContentType();
        }

        @Override
        public int getNumberOfImages() {
            return contact.getNumberOfImages();
        }

        @Override
        public String getUserField01() {
            return escape(contact.getUserField01());
        }

        @Override
        public String getUserField02() {
            return escape(contact.getUserField02());
        }

        @Override
        public String getUserField03() {
            return escape(contact.getUserField03());
        }

        @Override
        public String getUserField04() {
            return escape(contact.getUserField04());
        }

        @Override
        public String getUserField05() {
            return escape(contact.getUserField05());
        }

        @Override
        public String getUserField06() {
            return escape(contact.getUserField06());
        }

        @Override
        public String getUserField07() {
            return escape(contact.getUserField07());
        }

        @Override
        public String getUserField08() {
            return escape(contact.getUserField08());
        }

        @Override
        public String getUserField09() {
            return escape(contact.getUserField09());
        }

        @Override
        public String getUserField10() {
            return escape(contact.getUserField10());
        }

        @Override
        public String getUserField11() {
            return escape(contact.getUserField11());
        }

        @Override
        public String getUserField12() {
            return escape(contact.getUserField12());
        }

        @Override
        public String getUserField13() {
            return escape(contact.getUserField13());
        }

        @Override
        public String getUserField14() {
            return escape(contact.getUserField14());
        }

        @Override
        public String getUserField15() {
            return escape(contact.getUserField15());
        }

        @Override
        public String getUserField16() {
            return escape(contact.getUserField16());
        }

        @Override
        public String getUserField17() {
            return escape(contact.getUserField17());
        }

        @Override
        public String getUserField18() {
            return escape(contact.getUserField18());
        }

        @Override
        public String getUserField19() {
            return escape(contact.getUserField19());
        }

        @Override
        public String getUserField20() {
            return escape(contact.getUserField20());
        }

        @Override
        public int getNumberOfDistributionLists() {
            return contact.getNumberOfDistributionLists();
        }

        @Override
        public DistributionListEntryObject[] getDistributionList() {
            return contact.getDistributionList();
        }

        @Override
        public int getContextId() {
            return contact.getContextId();
        }

        @Override
        public int getInternalUserId() {
            return contact.getInternalUserId();
        }

        @Override
        public Date getImageLastModified() {
            return contact.getImageLastModified();
        }

        @Override
        public String getFileAs() {
            return escape(contact.getFileAs());
        }

        @Override
        public boolean getMarkAsDistribtuionlist() {
            return contact.getMarkAsDistribtuionlist();
        }

        @Override
        public int getUseCount() {
            return contact.getUseCount();
        }

        @Override
        public String getYomiFirstName() {
            return escape(contact.getYomiFirstName());
        }

        @Override
        public String getYomiLastName() {
            return escape(contact.getYomiLastName());
        }

        @Override
        public String getYomiCompany() {
            return escape(contact.getYomiCompany());
        }

        @Override
        public String getAddressBusiness() {
            return escape(contact.getAddressBusiness());
        }

        @Override
        public String getAddressHome() {
            return escape(contact.getAddressHome());
        }

        @Override
        public String getAddressOther() {
            return escape(contact.getAddressOther());
        }

        @Override
        public String getVCardId() {
            return escape(contact.getVCardId());
        }

        @Override
        public void setDisplayName(String display_name) {
            contact.setDisplayName(display_name);
        }

        @Override
        public void setGivenName(String given_name) {
            contact.setGivenName(given_name);
        }

        @Override
        public void setSurName(String sur_name) {
            contact.setSurName(sur_name);
        }

        @Override
        public void setMiddleName(String middle_name) {
            contact.setMiddleName(middle_name);
        }

        @Override
        public void setSuffix(String suffix) {
            contact.setSuffix(suffix);
        }

        @Override
        public void setTitle(String title) {
            contact.setTitle(title);
        }

        @Override
        public void setStreetHome(String street) {
            contact.setStreetHome(street);
        }

        @Override
        public void setPostalCodeHome(String postal_code) {
            contact.setPostalCodeHome(postal_code);
        }

        @Override
        public void setCityHome(String city) {
            contact.setCityHome(city);
        }

        @Override
        public void setStateHome(String state) {
            contact.setStateHome(state);
        }

        @Override
        public void setCountryHome(String country) {
            contact.setCountryHome(country);
        }

        @Override
        public void setBirthday(Date birthday) {
            contact.setBirthday(birthday);
        }

        @Override
        public void setMaritalStatus(String marital_status) {
            contact.setMaritalStatus(marital_status);
        }

        @Override
        public void setNumberOfChildren(String number_of_children) {
            contact.setNumberOfChildren(number_of_children);
        }

        @Override
        public void setProfession(String profession) {
            contact.setProfession(profession);
        }

        @Override
        public void setNickname(String nickname) {
            contact.setNickname(nickname);
        }

        @Override
        public void setSpouseName(String spouse_name) {
            contact.setSpouseName(spouse_name);
        }

        @Override
        public void setAnniversary(Date anniversary) {
            contact.setAnniversary(anniversary);
        }

        @Override
        public void setNote(String note) {
            contact.setNote(note);
        }

        @Override
        public void setDepartment(String department) {
            contact.setDepartment(department);
        }

        @Override
        public void setPosition(String position) {
            contact.setPosition(position);
        }

        @Override
        public void setEmployeeType(String employee_type) {
            contact.setEmployeeType(employee_type);
        }

        @Override
        public void setRoomNumber(String room_number) {
            contact.setRoomNumber(room_number);
        }

        @Override
        public void setStreetBusiness(String street_business) {
            contact.setStreetBusiness(street_business);
        }

        @Override
        public void setPostalCodeBusiness(String postal_code_business) {
            contact.setPostalCodeBusiness(postal_code_business);
        }

        @Override
        public void setCityBusiness(String city_business) {
            contact.setCityBusiness(city_business);
        }

        @Override
        public void setStateBusiness(String state_business) {
            contact.setStateBusiness(state_business);
        }

        @Override
        public void setCountryBusiness(String country_business) {
            contact.setCountryBusiness(country_business);
        }

        @Override
        public void setNumberOfEmployee(String number_of_employee) {
            contact.setNumberOfEmployee(number_of_employee);
        }

        @Override
        public void setSalesVolume(String sales_volume) {
            contact.setSalesVolume(sales_volume);
        }

        @Override
        public void setTaxID(String tax_id) {
            contact.setTaxID(tax_id);
        }

        @Override
        public void setCommercialRegister(String commercial_register) {
            contact.setCommercialRegister(commercial_register);
        }

        @Override
        public void setBranches(String branches) {
            contact.setBranches(branches);
        }

        @Override
        public void setBusinessCategory(String business_category) {
            contact.setBusinessCategory(business_category);
        }

        @Override
        public void setInfo(String info) {
            contact.setInfo(info);
        }

        @Override
        public void setManagerName(String manager_name) {
            contact.setManagerName(manager_name);
        }

        @Override
        public void setAssistantName(String assistant_name) {
            contact.setAssistantName(assistant_name);
        }

        @Override
        public void setStreetOther(String street_other) {
            contact.setStreetOther(street_other);
        }

        @Override
        public void setPostalCodeOther(String postal_code_other) {
            contact.setPostalCodeOther(postal_code_other);
        }

        @Override
        public void setCityOther(String city_other) {
            contact.setCityOther(city_other);
        }

        @Override
        public void setStateOther(String state_other) {
            contact.setStateOther(state_other);
        }

        @Override
        public void setCountryOther(String country_other) {
            contact.setCountryOther(country_other);
        }

        @Override
        public void setTelephoneBusiness1(String telephone_business1) {
            contact.setTelephoneBusiness1(telephone_business1);
        }

        @Override
        public void setTelephoneBusiness2(String telephone_business2) {
            contact.setTelephoneBusiness2(telephone_business2);
        }

        @Override
        public void setFaxBusiness(String fax_business) {
            contact.setFaxBusiness(fax_business);
        }

        @Override
        public void setTelephoneCallback(String telephone_callback) {
            contact.setTelephoneCallback(telephone_callback);
        }

        @Override
        public void setTelephoneCar(String telephone_car) {
            contact.setTelephoneCar(telephone_car);
        }

        @Override
        public void setTelephoneCompany(String telephone_company) {
            contact.setTelephoneCompany(telephone_company);
        }

        @Override
        public void setTelephoneHome1(String telephone_home1) {
            contact.setTelephoneHome1(telephone_home1);
        }

        @Override
        public void setTelephoneHome2(String telephone_home2) {
            contact.setTelephoneHome2(telephone_home2);
        }

        @Override
        public void setFaxHome(String fax_home) {
            contact.setFaxHome(fax_home);
        }

        @Override
        public void setCellularTelephone1(String cellular_telephone1) {
            contact.setCellularTelephone1(cellular_telephone1);
        }

        @Override
        public void setCellularTelephone2(String cellular_telephone2) {
            contact.setCellularTelephone2(cellular_telephone2);
        }

        @Override
        public void setTelephoneOther(String telephone_other) {
            contact.setTelephoneOther(telephone_other);
        }

        @Override
        public void setFaxOther(String fax_other) {
            contact.setFaxOther(fax_other);
        }

        @Override
        public void setEmail1(String email1) {
            contact.setEmail1(email1);
        }

        @Override
        public void setEmail2(String email2) {
            contact.setEmail2(email2);
        }

        @Override
        public void setEmail3(String email3) {
            contact.setEmail3(email3);
        }

        @Override
        public void setURL(String url) {
            contact.setURL(url);
        }

        @Override
        public void setTelephoneISDN(String telephone_isdn) {
            contact.setTelephoneISDN(telephone_isdn);
        }

        @Override
        public void setTelephonePager(String telephone_pager) {
            contact.setTelephonePager(telephone_pager);
        }

        @Override
        public void setTelephonePrimary(String telephone_primary) {
            contact.setTelephonePrimary(telephone_primary);
        }

        @Override
        public void setTelephoneRadio(String telephone_radio) {
            contact.setTelephoneRadio(telephone_radio);
        }

        @Override
        public void setTelephoneTelex(String telephone_telex) {
            contact.setTelephoneTelex(telephone_telex);
        }

        @Override
        public void setTelephoneTTYTTD(String telephone_ttyttd) {
            contact.setTelephoneTTYTTD(telephone_ttyttd);
        }

        @Override
        public void setInstantMessenger1(String instant_messenger1) {
            contact.setInstantMessenger1(instant_messenger1);
        }

        @Override
        public void setInstantMessenger2(String instant_messenger2) {
            contact.setInstantMessenger2(instant_messenger2);
        }

        @Override
        public void setTelephoneIP(String phone_ip) {
            contact.setTelephoneIP(phone_ip);
        }

        @Override
        public void setTelephoneAssistant(String telephone_assistant) {
            contact.setTelephoneAssistant(telephone_assistant);
        }

        @Override
        public void setDefaultAddress(int defaultaddress) {
            contact.setDefaultAddress(defaultaddress);
        }

        @Override
        public void setCompany(String company) {
            contact.setCompany(company);
        }

        @Override
        public void setUserField01(String userfield01) {
            contact.setUserField01(userfield01);
        }

        @Override
        public void setUserField02(String userfield02) {
            contact.setUserField02(userfield02);
        }

        @Override
        public void setUserField03(String userfield03) {
            contact.setUserField03(userfield03);
        }

        @Override
        public void setUserField04(String userfield04) {
            contact.setUserField04(userfield04);
        }

        @Override
        public void setUserField05(String userfield05) {
            contact.setUserField05(userfield05);
        }

        @Override
        public void setUserField06(String userfield06) {
            contact.setUserField06(userfield06);
        }

        @Override
        public void setUserField07(String userfield07) {
            contact.setUserField07(userfield07);
        }

        @Override
        public void setUserField08(String userfield08) {
            contact.setUserField08(userfield08);
        }

        @Override
        public void setUserField09(String userfield09) {
            contact.setUserField09(userfield09);
        }

        @Override
        public void setUserField10(String userfield10) {
            contact.setUserField10(userfield10);
        }

        @Override
        public void setUserField11(String userfield11) {
            contact.setUserField11(userfield11);
        }

        @Override
        public void setUserField12(String userfield12) {
            contact.setUserField12(userfield12);
        }

        @Override
        public void setUserField13(String userfield13) {
            contact.setUserField13(userfield13);
        }

        @Override
        public void setUserField14(String userfield14) {
            contact.setUserField14(userfield14);
        }

        @Override
        public void setUserField15(String userfield15) {
            contact.setUserField15(userfield15);
        }

        @Override
        public void setUserField16(String userfield16) {
            contact.setUserField16(userfield16);
        }

        @Override
        public void setUserField17(String userfield17) {
            contact.setUserField17(userfield17);
        }

        @Override
        public void setUserField18(String userfield18) {
            contact.setUserField18(userfield18);
        }

        @Override
        public void setUserField19(String userfield19) {
            contact.setUserField19(userfield19);
        }

        @Override
        public void setUserField20(String userfield20) {
            contact.setUserField20(userfield20);
        }

        @Override
        public void setImage1(byte[] image1) {
            contact.setImage1(image1);
        }

        @Override
        public void setImageContentType(String imageContentType) {
            contact.setImageContentType(imageContentType);
        }

        @Override
        public void setNumberOfImages(int number_of_images) {
            contact.setNumberOfImages(number_of_images);
        }

        @Override
        public void setNumberOfDistributionLists(int listsize) {
            contact.setNumberOfDistributionLists(listsize);
        }

        @Override
        public void setDistributionList(DistributionListEntryObject[] dleo) {
            contact.setDistributionList(dleo);
        }

        @Override
        public void setContextId(int cid) {
            contact.setContextId(cid);
        }

        @Override
        public void setInternalUserId(int internal_userId) {
            contact.setInternalUserId(internal_userId);
        }

        @Override
        public void setImageLastModified(Date image_last_modified) {
            contact.setImageLastModified(image_last_modified);
        }

        @Override
        public void setFileAs(String file_as) {
            contact.setFileAs(file_as);
        }

        @Override
        public void setMarkAsDistributionlist(boolean mark_as_disitributionlist) {
            contact.setMarkAsDistributionlist(mark_as_disitributionlist);
        }

        @Override
        public void setUseCount(int useCount) {
            contact.setUseCount(useCount);
        }

        @Override
        public void setYomiFirstName(String yomiFirstName) {
            contact.setYomiFirstName(yomiFirstName);
        }

        @Override
        public void setYomiLastName(String yomiLastName) {
            contact.setYomiLastName(yomiLastName);
        }

        @Override
        public void setYomiCompany(String yomiCompany) {
            contact.setYomiCompany(yomiCompany);
        }

        @Override
        public void setAddressBusiness(String addressBusiness) {
            contact.setAddressBusiness(addressBusiness);
        }

        @Override
        public void setAddressHome(String addressHome) {
            contact.setAddressHome(addressHome);
        }

        @Override
        public void setAddressOther(String addressOther) {
            contact.setAddressOther(addressOther);
        }

        @Override
        public void setVCardId(String vCardId) {
            contact.setVCardId(vCardId);
        }

        @Override
        public void removeDisplayName() {
            contact.removeDisplayName();
        }

        @Override
        public void removeGivenName() {
            contact.removeGivenName();
        }

        @Override
        public void removeSurName() {
            contact.removeSurName();
        }

        @Override
        public void removeMiddleName() {
            contact.removeMiddleName();
        }

        @Override
        public void removeSuffix() {
            contact.removeSuffix();
        }

        @Override
        public void removeTitle() {
            contact.removeTitle();
        }

        @Override
        public void removeStreetHome() {
            contact.removeStreetHome();
        }

        @Override
        public void removePostalCodeHome() {
            contact.removePostalCodeHome();
        }

        @Override
        public void removeCityHome() {
            contact.removeCityHome();
        }

        @Override
        public void removeStateHome() {
            contact.removeStateHome();
        }

        @Override
        public void removeCountryHome() {
            contact.removeCountryHome();
        }

        @Override
        public void removeBirthday() {
            contact.removeBirthday();
        }

        @Override
        public void removeMaritalStatus() {
            contact.removeMaritalStatus();
        }

        @Override
        public void removeNumberOfChildren() {
            contact.removeNumberOfChildren();
        }

        @Override
        public void removeProfession() {
            contact.removeProfession();
        }

        @Override
        public void removeNickname() {
            contact.removeNickname();
        }

        @Override
        public void removeSpouseName() {
            contact.removeSpouseName();
        }

        @Override
        public void removeAnniversary() {
            contact.removeAnniversary();
        }

        @Override
        public void removeNote() {
            contact.removeNote();
        }

        @Override
        public void removeDepartment() {
            contact.removeDepartment();
        }

        @Override
        public void removePosition() {
            contact.removePosition();
        }

        @Override
        public void removeEmployeeType() {
            contact.removeEmployeeType();
        }

        @Override
        public void removeRoomNumber() {
            contact.removeRoomNumber();
        }

        @Override
        public void removeStreetBusiness() {
            contact.removeStreetBusiness();
        }

        @Override
        public void removePostalCodeBusiness() {
            contact.removePostalCodeBusiness();
        }

        @Override
        public void removeCityBusiness() {
            contact.removeCityBusiness();
        }

        @Override
        public void removeStateBusiness() {
            contact.removeStateBusiness();
        }

        @Override
        public void removeCountryBusiness() {
            contact.removeCountryBusiness();
        }

        @Override
        public void removeNumberOfEmployee() {
            contact.removeNumberOfEmployee();
        }

        @Override
        public void removeSalesVolume() {
            contact.removeSalesVolume();
        }

        @Override
        public void removeTaxID() {
            contact.removeTaxID();
        }

        @Override
        public void removeCommercialRegister() {
            contact.removeCommercialRegister();
        }

        @Override
        public void removeBranches() {
            contact.removeBranches();
        }

        @Override
        public void removeBusinessCategory() {
            contact.removeBusinessCategory();
        }

        @Override
        public void removeInfo() {
            contact.removeInfo();
        }

        @Override
        public void removeManagerName() {
            contact.removeManagerName();
        }

        @Override
        public void removeAssistantName() {
            contact.removeAssistantName();
        }

        @Override
        public void removeStreetOther() {
            contact.removeStreetOther();
        }

        @Override
        public void removePostalCodeOther() {
            contact.removePostalCodeOther();
        }

        @Override
        public void removeCityOther() {
            contact.removeCityOther();
        }

        @Override
        public void removeStateOther() {
            contact.removeStateOther();
        }

        @Override
        public void removeCountryOther() {
            contact.removeCountryOther();
        }

        @Override
        public void removeTelephoneBusiness1() {
            contact.removeTelephoneBusiness1();
        }

        @Override
        public void removeTelephoneBusiness2() {
            contact.removeTelephoneBusiness2();
        }

        @Override
        public void removeFaxBusiness() {
            contact.removeFaxBusiness();
        }

        @Override
        public void removeFileAs() {
            contact.removeFileAs();
        }

        @Override
        public void removeTelephoneCallback() {
            contact.removeTelephoneCallback();
        }

        @Override
        public void removeTelephoneCar() {
            contact.removeTelephoneCar();
        }

        @Override
        public void removeTelephoneCompany() {
            contact.removeTelephoneCompany();
        }

        @Override
        public void removeTelephoneHome1() {
            contact.removeTelephoneHome1();
        }

        @Override
        public void removeTelephoneHome2() {
            contact.removeTelephoneHome2();
        }

        @Override
        public void removeFaxHome() {
            contact.removeFaxHome();
        }

        @Override
        public void removeCellularTelephone1() {
            contact.removeCellularTelephone1();
        }

        @Override
        public void removeCellularTelephone2() {
            contact.removeCellularTelephone2();
        }

        @Override
        public void removeTelephoneOther() {
            contact.removeTelephoneOther();
        }

        @Override
        public void removeFaxOther() {
            contact.removeFaxOther();
        }

        @Override
        public void removeEmail1() {
            contact.removeEmail1();
        }

        @Override
        public void removeEmail2() {
            contact.removeEmail2();
        }

        @Override
        public void removeEmail3() {
            contact.removeEmail3();
        }

        @Override
        public void removeURL() {
            contact.removeURL();
        }

        @Override
        public void removeTelephoneISDN() {
            contact.removeTelephoneISDN();
        }

        @Override
        public void removeTelephonePager() {
            contact.removeTelephonePager();
        }

        @Override
        public void removeTelephonePrimary() {
            contact.removeTelephonePrimary();
        }

        @Override
        public void removeTelephoneRadio() {
            contact.removeTelephoneRadio();
        }

        @Override
        public void removeTelephoneTelex() {
            contact.removeTelephoneTelex();
        }

        @Override
        public void removeTelephoneTTYTTD() {
            contact.removeTelephoneTTYTTD();
        }

        @Override
        public void removeInstantMessenger1() {
            contact.removeInstantMessenger1();
        }

        @Override
        public void removeInstantMessenger2() {
            contact.removeInstantMessenger2();
        }

        @Override
        public void removeImageLastModified() {
            contact.removeImageLastModified();
        }

        @Override
        public void removeTelephoneIP() {
            contact.removeTelephoneIP();
        }

        @Override
        public void removeTelephoneAssistant() {
            contact.removeTelephoneAssistant();
        }

        @Override
        public void removeDefaultAddress() {
            contact.removeDefaultAddress();
        }

        @Override
        public void removeCompany() {
            contact.removeCompany();
        }

        @Override
        public void removeImage1() {
            contact.removeImage1();
        }

        @Override
        public void removeImageContentType() {
            contact.removeImageContentType();
        }

        @Override
        public void removeUserField01() {
            contact.removeUserField01();
        }

        @Override
        public void removeUserField02() {
            contact.removeUserField02();
        }

        @Override
        public void removeUserField03() {
            contact.removeUserField03();
        }

        @Override
        public void removeUserField04() {
            contact.removeUserField04();
        }

        @Override
        public void removeUserField05() {
            contact.removeUserField05();
        }

        @Override
        public void removeUserField06() {
            contact.removeUserField06();
        }

        @Override
        public void removeUserField07() {
            contact.removeUserField07();
        }

        @Override
        public void removeUserField08() {
            contact.removeUserField08();
        }

        @Override
        public void removeUserField09() {
            contact.removeUserField09();
        }

        @Override
        public void removeUserField10() {
            contact.removeUserField10();
        }

        @Override
        public void removeUserField11() {
            contact.removeUserField11();
        }

        @Override
        public void removeUserField12() {
            contact.removeUserField12();
        }

        @Override
        public void removeUserField13() {
            contact.removeUserField13();
        }

        @Override
        public void removeUserField14() {
            contact.removeUserField14();
        }

        @Override
        public void removeUserField15() {
            contact.removeUserField15();
        }

        @Override
        public void removeUserField16() {
            contact.removeUserField16();
        }

        @Override
        public void removeUserField17() {
            contact.removeUserField17();
        }

        @Override
        public void removeUserField18() {
            contact.removeUserField18();
        }

        @Override
        public void removeUserField19() {
            contact.removeUserField19();
        }

        @Override
        public void removeUserField20() {
            contact.removeUserField20();
        }

        @Override
        public void removeNumberOfDistributionLists() {
            contact.removeNumberOfDistributionLists();
        }

        @Override
        public void removeDistributionLists() {
            contact.removeDistributionLists();
        }

        @Override
        public void removeMarkAsDistributionlist() {
            contact.removeMarkAsDistributionlist();
        }

        @Override
        public void removeContextID() {
            contact.removeContextID();
        }

        @Override
        public void removeInternalUserId() {
            contact.removeInternalUserId();
        }

        @Override
        public void removeUseCount() {
            contact.removeUseCount();
        }

        @Override
        public void removeYomiFirstName() {
            contact.removeYomiFirstName();
        }

        @Override
        public void removeYomiLastName() {
            contact.removeYomiLastName();
        }

        @Override
        public void removeYomiCompany() {
            contact.removeYomiCompany();
        }

        @Override
        public void removeAddressHome() {
            contact.removeAddressHome();
        }

        @Override
        public void removeAddressBusiness() {
            contact.removeAddressBusiness();
        }

        @Override
        public void removeAddressOther() {
            contact.removeAddressOther();
        }

        @Override
        public void removeVCardId() {
            contact.removeVCardId();
        }

        @Override
        public boolean containsDisplayName() {
            return contact.containsDisplayName();
        }

        @Override
        public boolean containsGivenName() {
            return contact.containsGivenName();
        }

        @Override
        public boolean containsSurName() {
            return contact.containsSurName();
        }

        @Override
        public boolean containsMiddleName() {
            return contact.containsMiddleName();
        }

        @Override
        public boolean containsSuffix() {
            return contact.containsSuffix();
        }

        @Override
        public boolean containsTitle() {
            return contact.containsTitle();
        }

        @Override
        public boolean containsStreetHome() {
            return contact.containsStreetHome();
        }

        @Override
        public boolean containsPostalCodeHome() {
            return contact.containsPostalCodeHome();
        }

        @Override
        public boolean containsCityHome() {
            return contact.containsCityHome();
        }

        @Override
        public boolean containsStateHome() {
            return contact.containsStateHome();
        }

        @Override
        public boolean containsCountryHome() {
            return contact.containsCountryHome();
        }

        @Override
        public boolean containsBirthday() {
            return contact.containsBirthday();
        }

        @Override
        public boolean containsMaritalStatus() {
            return contact.containsMaritalStatus();
        }

        @Override
        public boolean containsNumberOfChildren() {
            return contact.containsNumberOfChildren();
        }

        @Override
        public boolean containsProfession() {
            return contact.containsProfession();
        }

        @Override
        public boolean containsNickname() {
            return contact.containsNickname();
        }

        @Override
        public boolean containsSpouseName() {
            return contact.containsSpouseName();
        }

        @Override
        public boolean containsAnniversary() {
            return contact.containsAnniversary();
        }

        @Override
        public boolean containsNote() {
            return contact.containsNote();
        }

        @Override
        public boolean containsDepartment() {
            return contact.containsDepartment();
        }

        @Override
        public boolean containsPosition() {
            return contact.containsPosition();
        }

        @Override
        public boolean containsEmployeeType() {
            return contact.containsEmployeeType();
        }

        @Override
        public boolean containsRoomNumber() {
            return contact.containsRoomNumber();
        }

        @Override
        public boolean containsStreetBusiness() {
            return contact.containsStreetBusiness();
        }

        @Override
        public boolean containsPostalCodeBusiness() {
            return contact.containsPostalCodeBusiness();
        }

        @Override
        public boolean containsCityBusiness() {
            return contact.containsCityBusiness();
        }

        @Override
        public boolean containsStateBusiness() {
            return contact.containsStateBusiness();
        }

        @Override
        public boolean containsCountryBusiness() {
            return contact.containsCountryBusiness();
        }

        @Override
        public boolean containsNumberOfEmployee() {
            return contact.containsNumberOfEmployee();
        }

        @Override
        public boolean containsSalesVolume() {
            return contact.containsSalesVolume();
        }

        @Override
        public boolean containsTaxID() {
            return contact.containsTaxID();
        }

        @Override
        public boolean containsCommercialRegister() {
            return contact.containsCommercialRegister();
        }

        @Override
        public boolean containsBranches() {
            return contact.containsBranches();
        }

        @Override
        public boolean containsBusinessCategory() {
            return contact.containsBusinessCategory();
        }

        @Override
        public boolean containsInfo() {
            return contact.containsInfo();
        }

        @Override
        public boolean containsManagerName() {
            return contact.containsManagerName();
        }

        @Override
        public boolean containsAssistantName() {
            return contact.containsAssistantName();
        }

        @Override
        public boolean containsStreetOther() {
            return contact.containsStreetOther();
        }

        @Override
        public boolean containsPostalCodeOther() {
            return contact.containsPostalCodeOther();
        }

        @Override
        public boolean containsCityOther() {
            return contact.containsCityOther();
        }

        @Override
        public boolean containsStateOther() {
            return contact.containsStateOther();
        }

        @Override
        public boolean containsCountryOther() {
            return contact.containsCountryOther();
        }

        @Override
        public boolean containsTelephoneBusiness1() {
            return contact.containsTelephoneBusiness1();
        }

        @Override
        public boolean containsTelephoneBusiness2() {
            return contact.containsTelephoneBusiness2();
        }

        @Override
        public boolean containsFaxBusiness() {
            return contact.containsFaxBusiness();
        }

        @Override
        public boolean containsTelephoneCallback() {
            return contact.containsTelephoneCallback();
        }

        @Override
        public boolean containsTelephoneCar() {
            return contact.containsTelephoneCar();
        }

        @Override
        public boolean containsTelephoneCompany() {
            return contact.containsTelephoneCompany();
        }

        @Override
        public boolean containsTelephoneHome1() {
            return contact.containsTelephoneHome1();
        }

        @Override
        public boolean containsTelephoneHome2() {
            return contact.containsTelephoneHome2();
        }

        @Override
        public boolean containsFaxHome() {
            return contact.containsFaxHome();
        }

        @Override
        public boolean containsCellularTelephone1() {
            return contact.containsCellularTelephone1();
        }

        @Override
        public boolean containsCellularTelephone2() {
            return contact.containsCellularTelephone2();
        }

        @Override
        public boolean containsTelephoneOther() {
            return contact.containsTelephoneOther();
        }

        @Override
        public boolean containsFaxOther() {
            return contact.containsFaxOther();
        }

        @Override
        public boolean containsEmail1() {
            return contact.containsEmail1();
        }

        @Override
        public boolean containsEmail2() {
            return contact.containsEmail2();
        }

        @Override
        public boolean containsEmail3() {
            return contact.containsEmail3();
        }

        @Override
        public boolean containsURL() {
            return contact.containsURL();
        }

        @Override
        public boolean containsTelephoneISDN() {
            return contact.containsTelephoneISDN();
        }

        @Override
        public boolean containsTelephonePager() {
            return contact.containsTelephonePager();
        }

        @Override
        public boolean containsTelephonePrimary() {
            return contact.containsTelephonePrimary();
        }

        @Override
        public boolean containsTelephoneRadio() {
            return contact.containsTelephoneRadio();
        }

        @Override
        public boolean containsTelephoneTelex() {
            return contact.containsTelephoneTelex();
        }

        @Override
        public boolean containsTelephoneTTYTTD() {
            return contact.containsTelephoneTTYTTD();
        }

        @Override
        public boolean containsInstantMessenger1() {
            return contact.containsInstantMessenger1();
        }

        @Override
        public boolean containsInstantMessenger2() {
            return contact.containsInstantMessenger2();
        }

        @Override
        public boolean containsTelephoneIP() {
            return contact.containsTelephoneIP();
        }

        @Override
        public boolean containsTelephoneAssistant() {
            return contact.containsTelephoneAssistant();
        }

        @Override
        public boolean containsDefaultAddress() {
            return contact.containsDefaultAddress();
        }

        @Override
        public boolean containsCompany() {
            return contact.containsCompany();
        }

        @Override
        public boolean containsUserField01() {
            return contact.containsUserField01();
        }

        @Override
        public boolean containsUserField02() {
            return contact.containsUserField02();
        }

        @Override
        public boolean containsUserField03() {
            return contact.containsUserField03();
        }

        @Override
        public boolean containsUserField04() {
            return contact.containsUserField04();
        }

        @Override
        public boolean containsUserField05() {
            return contact.containsUserField05();
        }

        @Override
        public boolean containsUserField06() {
            return contact.containsUserField06();
        }

        @Override
        public boolean containsUserField07() {
            return contact.containsUserField07();
        }

        @Override
        public boolean containsUserField08() {
            return contact.containsUserField08();
        }

        @Override
        public boolean containsUserField09() {
            return contact.containsUserField09();
        }

        @Override
        public boolean containsUserField10() {
            return contact.containsUserField10();
        }

        @Override
        public boolean containsUserField11() {
            return contact.containsUserField11();
        }

        @Override
        public boolean containsUserField12() {
            return contact.containsUserField12();
        }

        @Override
        public boolean containsUserField13() {
            return contact.containsUserField13();
        }

        @Override
        public boolean containsUserField14() {
            return contact.containsUserField14();
        }

        @Override
        public boolean containsUserField15() {
            return contact.containsUserField15();
        }

        @Override
        public boolean containsUserField16() {
            return contact.containsUserField16();
        }

        @Override
        public boolean containsUserField17() {
            return contact.containsUserField17();
        }

        @Override
        public boolean containsUserField18() {
            return contact.containsUserField18();
        }

        @Override
        public boolean containsUserField19() {
            return contact.containsUserField19();
        }

        @Override
        public boolean containsUserField20() {
            return contact.containsUserField20();
        }

        @Override
        public boolean containsImage1() {
            return contact.containsImage1();
        }

        @Override
        public boolean containsImageContentType() {
            return contact.containsImageContentType();
        }

        @Override
        public boolean containsNumberOfDistributionLists() {
            return contact.containsNumberOfDistributionLists();
        }

        @Override
        public boolean containsDistributionLists() {
            return contact.containsDistributionLists();
        }

        @Override
        public int getSizeOfDistributionListArray() {
            return contact.getSizeOfDistributionListArray();
        }

        @Override
        public boolean containsInternalUserId() {
            return contact.containsInternalUserId();
        }

        @Override
        public boolean containsContextId() {
            return contact.containsContextId();
        }

        @Override
        public boolean containsImageLastModified() {
            return contact.containsImageLastModified();
        }

        @Override
        public boolean containsFileAs() {
            return contact.containsFileAs();
        }

        @Override
        public boolean containsMarkAsDistributionlist() {
            return contact.containsMarkAsDistributionlist();
        }

        @Override
        public boolean containsUseCount() {
            return contact.containsUseCount();
        }

        @Override
        public boolean containsYomiFirstName() {
            return contact.containsYomiFirstName();
        }

        @Override
        public boolean containsYomiLastName() {
            return contact.containsYomiLastName();
        }

        @Override
        public boolean containsYomiCompany() {
            return contact.containsYomiCompany();
        }

        @Override
        public boolean containsAddressHome() {
            return contact.containsAddressHome();
        }

        @Override
        public boolean containsAddressBusiness() {
            return contact.containsAddressBusiness();
        }

        @Override
        public boolean containsAddressOther() {
            return contact.containsAddressOther();
        }

        @Override
        public boolean containsVCardId() {
            return contact.containsVCardId();
        }

        @Override
        public void set(int field, Object value) {
            contact.set(field, value);
        }

        @Override
        public Object get(int field) {
            return contact.get(field);
        }

        @Override
        public boolean contains(int field) {
            return contact.contains(field);
        }

        @Override
        public void remove(int field) {
            contact.remove(field);
        }

        @Override
        public boolean canFormDisplayName() {
            return contact.canFormDisplayName();
        }

        @Override
        public String toString() {
            return contact.toString();
        }

        @Override
        public boolean equalsContentwise(Object obj) {
            return contact.equalsContentwise(obj);
        }

        @Override
        public boolean matches(Contact other, int[] fields) {
            return contact.matches(other, fields);
        }

        @Override
        public String getSortName() {
            return escape(contact.getSortName());
        }

        private String escape(String value) {
            return Publications.escape(value, escapeMode);
        }
    }

}
