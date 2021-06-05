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

package com.openexchange.groupware.container;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link DelegatingContact}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DelegatingContact extends Contact {

    private static final long serialVersionUID = -7891106847748588411L;
    protected final Contact delegate;

    /**
     * Initializes a new {@link DelegatingContact}.
     *
     * @param delegate The contact delegate
     * @throws IllegalArgumentException if the delegate is <code>null</code>
     */
    public DelegatingContact(Contact delegate) {
        super(false);
        if (null == delegate) {
            throw new IllegalArgumentException("The contact delegate cannot be 'null'");
        }
        this.delegate = delegate;
    }

    @Override
    public int getParentFolderID() {
        return delegate.getParentFolderID();
    }

    @Override
    public void setParentFolderID(int parentFolderId) {
        delegate.setParentFolderID(parentFolderId);
    }

    @Override
    public void removeParentFolderID() {
        delegate.removeParentFolderID();
    }

    @Override
    public void setProperty(String name, Object value) {
        delegate.setProperty(name, value);
    }

    @Override
    public boolean containsParentFolderID() {
        return delegate.containsParentFolderID();
    }

    @Override
    public int getObjectID() {
        return delegate.getObjectID();
    }

    @Override
    public int getCreatedBy() {
        return delegate.getCreatedBy();
    }

    @Override
    public int getModifiedBy() {
        return delegate.getModifiedBy();
    }

    @Override
    public <V> V getProperty(String name) {
        return delegate.getProperty(name);
    }

    @Override
    public Date getCreationDate() {
        return delegate.getCreationDate();
    }

    @Override
    public Date getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public String getTopic() {
        return delegate.getTopic();
    }

    @Override
    public void setObjectID(int object_id) {
        delegate.setObjectID(object_id);
    }

    @Override
    public void setCreatedBy(int created_by) {
        delegate.setCreatedBy(created_by);
    }

    @Override
    public <V> V removeProperty(String name) {
        return delegate.removeProperty(name);
    }

    @Override
    public void setModifiedBy(int modified_by) {
        delegate.setModifiedBy(modified_by);
    }

    @Override
    public void setCreationDate(Date creation_date) {
        delegate.setCreationDate(creation_date);
    }

    @Override
    public void setLastModified(Date last_modified) {
        delegate.setLastModified(last_modified);
    }

    @Override
    public void setTopic(String topic) {
        delegate.setTopic(topic);
    }

    @Override
    public void setMap(Map<String, ? extends Object> map) {
        delegate.setMap(map);
    }

    @Override
    public void removeObjectID() {
        delegate.removeObjectID();
    }

    @Override
    public Map<String, Object> getExtendedProperties() {
        return delegate.getExtendedProperties();
    }

    @Override
    public void removeCreatedBy() {
        delegate.removeCreatedBy();
    }

    @Override
    public void removeMap() {
        delegate.removeMap();
    }

    @Override
    public void removeModifiedBy() {
        delegate.removeModifiedBy();
    }

    @Override
    public void removeCreationDate() {
        delegate.removeCreationDate();
    }

    @Override
    public boolean containsMap() {
        return delegate.containsMap();
    }

    @Override
    public String getCategories() {
        return delegate.getCategories();
    }

    @Override
    public void removeLastModified() {
        delegate.removeLastModified();
    }

    @Override
    public Marker getMarker() {
        return delegate.getMarker();
    }

    @Override
    public void removeTopic() {
        delegate.removeTopic();
    }

    @Override
    public Map<String, Object> getMap() {
        return delegate.getMap();
    }

    @Override
    public boolean containsObjectID() {
        return delegate.containsObjectID();
    }

    @Override
    public int getPersonalFolderID() {
        return delegate.getPersonalFolderID();
    }

    @Override
    public int getNumberOfAttachments() {
        return delegate.getNumberOfAttachments();
    }

    @Override
    public boolean containsCreatedBy() {
        return delegate.containsCreatedBy();
    }

    @Override
    public boolean containsModifiedBy() {
        return delegate.containsModifiedBy();
    }

    @Override
    public Date getLastModifiedOfNewestAttachment() {
        return delegate.getLastModifiedOfNewestAttachment();
    }

    @Override
    public boolean containsCreationDate() {
        return delegate.containsCreationDate();
    }

    @Override
    public boolean getPrivateFlag() {
        return delegate.getPrivateFlag();
    }

    @Override
    public boolean containsLastModified() {
        return delegate.containsLastModified();
    }

    @Override
    public int getLabel() {
        return delegate.getLabel();
    }

    @Override
    public boolean containsTopic() {
        return delegate.containsTopic();
    }

    @Override
    public String getUid() {
        return delegate.getUid();
    }

    @Override
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public void setExtendedProperties(Map<? extends String, ? extends Serializable> extendedProperties) {
        delegate.setExtendedProperties(extendedProperties);
    }

    @Override
    public void addExtendedProperties(Map<? extends String, ? extends Serializable> extendedProperties) {
        delegate.addExtendedProperties(extendedProperties);
    }

    @Override
    public void addExtendedProperty(String name, Serializable value) {
        delegate.addExtendedProperty(name, value);
    }

    @Override
    public void putExtendedProperty(String name, Serializable value) {
        delegate.putExtendedProperty(name, value);
    }

    @Override
    public void setCategories(String categories) {
        delegate.setCategories(categories);
    }

    @Override
    public void setMarker(Marker marker) {
        delegate.setMarker(marker);
    }

    @Override
    public void setPersonalFolderID(int personal_folder_id) {
        delegate.setPersonalFolderID(personal_folder_id);
    }

    @Override
    public void setNumberOfAttachments(int number_of_attachments) {
        delegate.setNumberOfAttachments(number_of_attachments);
    }

    @Override
    public void setLastModifiedOfNewestAttachment(Date lastModifiedOfNewestAttachment) {
        delegate.setLastModifiedOfNewestAttachment(lastModifiedOfNewestAttachment);
    }

    @Override
    public void setPrivateFlag(boolean privateFlag) {
        delegate.setPrivateFlag(privateFlag);
    }

    @Override
    public void setLabel(int label) {
        delegate.setLabel(label);
    }

    @Override
    public void setUid(String uid) {
        delegate.setUid(uid);
    }

    @Override
    public void setFilename(String filename) {
        delegate.setFilename(filename);
    }

    @Override
    public void removeExtendedProperties() {
        delegate.removeExtendedProperties();
    }

    @Override
    public void removeCategories() {
        delegate.removeCategories();
    }

    @Override
    public void removePersonalFolderID() {
        delegate.removePersonalFolderID();
    }

    @Override
    public void removeNumberOfAttachments() {
        delegate.removeNumberOfAttachments();
    }

    @Override
    public void removeLastModifiedOfNewestAttachment() {
        delegate.removeLastModifiedOfNewestAttachment();
    }

    @Override
    public void removePrivateFlag() {
        delegate.removePrivateFlag();
    }

    @Override
    public void removeLabel() {
        delegate.removeLabel();
    }

    @Override
    public void removeUid() {
        delegate.removeUid();
    }

    @Override
    public void removeFilename() {
        delegate.removeFilename();
    }

    @Override
    public boolean containsExtendedProperties() {
        return delegate.containsExtendedProperties();
    }

    @Override
    public boolean containsCategories() {
        return delegate.containsCategories();
    }

    @Override
    public boolean containsPersonalFolderID() {
        return delegate.containsPersonalFolderID();
    }

    @Override
    public boolean containsNumberOfAttachments() {
        return delegate.containsNumberOfAttachments();
    }

    @Override
    public boolean containsLastModifiedOfNewestAttachment() {
        return delegate.containsLastModifiedOfNewestAttachment();
    }

    @Override
    public boolean containsPrivateFlag() {
        return delegate.containsPrivateFlag();
    }

    @Override
    public boolean containsLabel() {
        return delegate.containsLabel();
    }

    @Override
    public boolean containsUid() {
        return delegate.containsUid();
    }

    @Override
    public boolean containsFilename() {
        return delegate.containsFilename();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void addWarning(final OXException warning) {
        delegate.addWarning(warning);
    }

    @Override
    public Collection<OXException> getWarnings() {
        return delegate.getWarnings();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public String getGivenName() {
        return delegate.getGivenName();
    }

    @Override
    public String getSurName() {
        return delegate.getSurName();
    }

    @Override
    public String getMiddleName() {
        return delegate.getMiddleName();
    }

    @Override
    public String getSuffix() {
        return delegate.getSuffix();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public String getStreetHome() {
        return delegate.getStreetHome();
    }

    @Override
    public String getPostalCodeHome() {
        return delegate.getPostalCodeHome();
    }

    @Override
    public String getCityHome() {
        return delegate.getCityHome();
    }

    @Override
    public String getStateHome() {
        return delegate.getStateHome();
    }

    @Override
    public String getCountryHome() {
        return delegate.getCountryHome();
    }

    @Override
    public Date getBirthday() {
        return delegate.getBirthday();
    }

    @Override
    public String getMaritalStatus() {
        return delegate.getMaritalStatus();
    }

    @Override
    public String getNumberOfChildren() {
        return delegate.getNumberOfChildren();
    }

    @Override
    public String getProfession() {
        return delegate.getProfession();
    }

    @Override
    public String getNickname() {
        return delegate.getNickname();
    }

    @Override
    public String getSpouseName() {
        return delegate.getSpouseName();
    }

    @Override
    public Date getAnniversary() {
        return delegate.getAnniversary();
    }

    @Override
    public String getNote() {
        return delegate.getNote();
    }

    @Override
    public String getDepartment() {
        return delegate.getDepartment();
    }

    @Override
    public String getPosition() {
        return delegate.getPosition();
    }

    @Override
    public String getEmployeeType() {
        return delegate.getEmployeeType();
    }

    @Override
    public String getRoomNumber() {
        return delegate.getRoomNumber();
    }

    @Override
    public String getStreetBusiness() {
        return delegate.getStreetBusiness();
    }

    @Override
    public String getPostalCodeBusiness() {
        return delegate.getPostalCodeBusiness();
    }

    @Override
    public String getCityBusiness() {
        return delegate.getCityBusiness();
    }

    @Override
    public String getStateBusiness() {
        return delegate.getStateBusiness();
    }

    @Override
    public String getCountryBusiness() {
        return delegate.getCountryBusiness();
    }

    @Override
    public String getNumberOfEmployee() {
        return delegate.getNumberOfEmployee();
    }

    @Override
    public String getSalesVolume() {
        return delegate.getSalesVolume();
    }

    @Override
    public String getTaxID() {
        return delegate.getTaxID();
    }

    @Override
    public String getCommercialRegister() {
        return delegate.getCommercialRegister();
    }

    @Override
    public String getBranches() {
        return delegate.getBranches();
    }

    @Override
    public String getBusinessCategory() {
        return delegate.getBusinessCategory();
    }

    @Override
    public String getInfo() {
        return delegate.getInfo();
    }

    @Override
    public String getManagerName() {
        return delegate.getManagerName();
    }

    @Override
    public String getAssistantName() {
        return delegate.getAssistantName();
    }

    @Override
    public String getStreetOther() {
        return delegate.getStreetOther();
    }

    @Override
    public String getPostalCodeOther() {
        return delegate.getPostalCodeOther();
    }

    @Override
    public String getCityOther() {
        return delegate.getCityOther();
    }

    @Override
    public String getStateOther() {
        return delegate.getStateOther();
    }

    @Override
    public String getCountryOther() {
        return delegate.getCountryOther();
    }

    @Override
    public String getTelephoneBusiness1() {
        return delegate.getTelephoneBusiness1();
    }

    @Override
    public String getTelephoneBusiness2() {
        return delegate.getTelephoneBusiness2();
    }

    @Override
    public String getFaxBusiness() {
        return delegate.getFaxBusiness();
    }

    @Override
    public String getTelephoneCallback() {
        return delegate.getTelephoneCallback();
    }

    @Override
    public String getTelephoneCar() {
        return delegate.getTelephoneCar();
    }

    @Override
    public String getTelephoneCompany() {
        return delegate.getTelephoneCompany();
    }

    @Override
    public String getTelephoneHome1() {
        return delegate.getTelephoneHome1();
    }

    @Override
    public String getTelephoneHome2() {
        return delegate.getTelephoneHome2();
    }

    @Override
    public String getFaxHome() {
        return delegate.getFaxHome();
    }

    @Override
    public String getCellularTelephone1() {
        return delegate.getCellularTelephone1();
    }

    @Override
    public String getCellularTelephone2() {
        return delegate.getCellularTelephone2();
    }

    @Override
    public String getTelephoneOther() {
        return delegate.getTelephoneOther();
    }

    @Override
    public String getFaxOther() {
        return delegate.getFaxOther();
    }

    @Override
    public String getEmail1() {
        return delegate.getEmail1();
    }

    @Override
    public String getEmail2() {
        return delegate.getEmail2();
    }

    @Override
    public String getEmail3() {
        return delegate.getEmail3();
    }

    @Override
    public String getURL() {
        return delegate.getURL();
    }

    @Override
    public String getTelephoneISDN() {
        return delegate.getTelephoneISDN();
    }

    @Override
    public String getTelephonePager() {
        return delegate.getTelephonePager();
    }

    @Override
    public String getTelephonePrimary() {
        return delegate.getTelephonePrimary();
    }

    @Override
    public String getTelephoneRadio() {
        return delegate.getTelephoneRadio();
    }

    @Override
    public String getTelephoneTelex() {
        return delegate.getTelephoneTelex();
    }

    @Override
    public String getTelephoneTTYTTD() {
        return delegate.getTelephoneTTYTTD();
    }

    @Override
    public String getInstantMessenger1() {
        return delegate.getInstantMessenger1();
    }

    @Override
    public String getInstantMessenger2() {
        return delegate.getInstantMessenger2();
    }

    @Override
    public String getTelephoneIP() {
        return delegate.getTelephoneIP();
    }

    @Override
    public String getTelephoneAssistant() {
        return delegate.getTelephoneAssistant();
    }

    @Override
    public int getDefaultAddress() {
        return delegate.getDefaultAddress();
    }

    @Override
    public String getCompany() {
        return delegate.getCompany();
    }

    @Override
    public byte[] getImage1() {
        return delegate.getImage1();
    }

    @Override
    public String getImageContentType() {
        return delegate.getImageContentType();
    }

    @Override
    public int getNumberOfImages() {
        return delegate.getNumberOfImages();
    }

    @Override
    public String getUserField01() {
        return delegate.getUserField01();
    }

    @Override
    public String getUserField02() {
        return delegate.getUserField02();
    }

    @Override
    public String getUserField03() {
        return delegate.getUserField03();
    }

    @Override
    public String getUserField04() {
        return delegate.getUserField04();
    }

    @Override
    public String getUserField05() {
        return delegate.getUserField05();
    }

    @Override
    public String getUserField06() {
        return delegate.getUserField06();
    }

    @Override
    public String getUserField07() {
        return delegate.getUserField07();
    }

    @Override
    public String getUserField08() {
        return delegate.getUserField08();
    }

    @Override
    public String getUserField09() {
        return delegate.getUserField09();
    }

    @Override
    public String getUserField10() {
        return delegate.getUserField10();
    }

    @Override
    public String getUserField11() {
        return delegate.getUserField11();
    }

    @Override
    public String getUserField12() {
        return delegate.getUserField12();
    }

    @Override
    public String getUserField13() {
        return delegate.getUserField13();
    }

    @Override
    public String getUserField14() {
        return delegate.getUserField14();
    }

    @Override
    public String getUserField15() {
        return delegate.getUserField15();
    }

    @Override
    public String getUserField16() {
        return delegate.getUserField16();
    }

    @Override
    public String getUserField17() {
        return delegate.getUserField17();
    }

    @Override
    public String getUserField18() {
        return delegate.getUserField18();
    }

    @Override
    public String getUserField19() {
        return delegate.getUserField19();
    }

    @Override
    public String getUserField20() {
        return delegate.getUserField20();
    }

    @Override
    public int getNumberOfDistributionLists() {
        return delegate.getNumberOfDistributionLists();
    }

    @Override
    public DistributionListEntryObject[] getDistributionList() {
        return delegate.getDistributionList();
    }

    @Override
    public int getContextId() {
        return delegate.getContextId();
    }

    @Override
    public int getInternalUserId() {
        return delegate.getInternalUserId();
    }

    @Override
    public Date getImageLastModified() {
        return delegate.getImageLastModified();
    }

    @Override
    public String getFileAs() {
        return delegate.getFileAs();
    }

    @Override
    public boolean getMarkAsDistribtuionlist() {
        return delegate.getMarkAsDistribtuionlist();
    }

    @Override
    public int getUseCount() {
        return delegate.getUseCount();
    }

    @Override
    public String getYomiFirstName() {
        return delegate.getYomiFirstName();
    }

    @Override
    public String getYomiLastName() {
        return delegate.getYomiLastName();
    }

    @Override
    public String getYomiCompany() {
        return delegate.getYomiCompany();
    }

    @Override
    public String getAddressBusiness() {
        return delegate.getAddressBusiness();
    }

    @Override
    public String getAddressHome() {
        return delegate.getAddressHome();
    }

    @Override
    public String getAddressOther() {
        return delegate.getAddressOther();
    }

    @Override
    public String getVCardId() {
        return delegate.getVCardId();
    }

    @Override
    public void setDisplayName(String display_name) {
        delegate.setDisplayName(display_name);
    }

    @Override
    public void setGivenName(String given_name) {
        delegate.setGivenName(given_name);
    }

    @Override
    public void setSurName(String sur_name) {
        delegate.setSurName(sur_name);
    }

    @Override
    public void setMiddleName(String middle_name) {
        delegate.setMiddleName(middle_name);
    }

    @Override
    public void setSuffix(String suffix) {
        delegate.setSuffix(suffix);
    }

    @Override
    public void setTitle(String title) {
        delegate.setTitle(title);
    }

    @Override
    public void setStreetHome(String street) {
        delegate.setStreetHome(street);
    }

    @Override
    public void setPostalCodeHome(String postal_code) {
        delegate.setPostalCodeHome(postal_code);
    }

    @Override
    public void setCityHome(String city) {
        delegate.setCityHome(city);
    }

    @Override
    public void setStateHome(String state) {
        delegate.setStateHome(state);
    }

    @Override
    public void setCountryHome(String country) {
        delegate.setCountryHome(country);
    }

    @Override
    public void setBirthday(Date birthday) {
        delegate.setBirthday(birthday);
    }

    @Override
    public void setMaritalStatus(String marital_status) {
        delegate.setMaritalStatus(marital_status);
    }

    @Override
    public void setNumberOfChildren(String number_of_children) {
        delegate.setNumberOfChildren(number_of_children);
    }

    @Override
    public void setProfession(String profession) {
        delegate.setProfession(profession);
    }

    @Override
    public void setNickname(String nickname) {
        delegate.setNickname(nickname);
    }

    @Override
    public void setSpouseName(String spouse_name) {
        delegate.setSpouseName(spouse_name);
    }

    @Override
    public void setAnniversary(Date anniversary) {
        delegate.setAnniversary(anniversary);
    }

    @Override
    public void setNote(String note) {
        delegate.setNote(note);
    }

    @Override
    public void setDepartment(String department) {
        delegate.setDepartment(department);
    }

    @Override
    public void setPosition(String position) {
        delegate.setPosition(position);
    }

    @Override
    public void setEmployeeType(String employee_type) {
        delegate.setEmployeeType(employee_type);
    }

    @Override
    public void setRoomNumber(String room_number) {
        delegate.setRoomNumber(room_number);
    }

    @Override
    public void setStreetBusiness(String street_business) {
        delegate.setStreetBusiness(street_business);
    }

    @Override
    public void setPostalCodeBusiness(String postal_code_business) {
        delegate.setPostalCodeBusiness(postal_code_business);
    }

    @Override
    public void setCityBusiness(String city_business) {
        delegate.setCityBusiness(city_business);
    }

    @Override
    public void setStateBusiness(String state_business) {
        delegate.setStateBusiness(state_business);
    }

    @Override
    public void setCountryBusiness(String country_business) {
        delegate.setCountryBusiness(country_business);
    }

    @Override
    public void setNumberOfEmployee(String number_of_employee) {
        delegate.setNumberOfEmployee(number_of_employee);
    }

    @Override
    public void setSalesVolume(String sales_volume) {
        delegate.setSalesVolume(sales_volume);
    }

    @Override
    public void setTaxID(String tax_id) {
        delegate.setTaxID(tax_id);
    }

    @Override
    public void setCommercialRegister(String commercial_register) {
        delegate.setCommercialRegister(commercial_register);
    }

    @Override
    public void setBranches(String branches) {
        delegate.setBranches(branches);
    }

    @Override
    public void setBusinessCategory(String business_category) {
        delegate.setBusinessCategory(business_category);
    }

    @Override
    public void setInfo(String info) {
        delegate.setInfo(info);
    }

    @Override
    public void setManagerName(String manager_name) {
        delegate.setManagerName(manager_name);
    }

    @Override
    public void setAssistantName(String assistant_name) {
        delegate.setAssistantName(assistant_name);
    }

    @Override
    public void setStreetOther(String street_other) {
        delegate.setStreetOther(street_other);
    }

    @Override
    public void setPostalCodeOther(String postal_code_other) {
        delegate.setPostalCodeOther(postal_code_other);
    }

    @Override
    public void setCityOther(String city_other) {
        delegate.setCityOther(city_other);
    }

    @Override
    public void setStateOther(String state_other) {
        delegate.setStateOther(state_other);
    }

    @Override
    public void setCountryOther(String country_other) {
        delegate.setCountryOther(country_other);
    }

    @Override
    public void setTelephoneBusiness1(String telephone_business1) {
        delegate.setTelephoneBusiness1(telephone_business1);
    }

    @Override
    public void setTelephoneBusiness2(String telephone_business2) {
        delegate.setTelephoneBusiness2(telephone_business2);
    }

    @Override
    public void setFaxBusiness(String fax_business) {
        delegate.setFaxBusiness(fax_business);
    }

    @Override
    public void setTelephoneCallback(String telephone_callback) {
        delegate.setTelephoneCallback(telephone_callback);
    }

    @Override
    public void setTelephoneCar(String telephone_car) {
        delegate.setTelephoneCar(telephone_car);
    }

    @Override
    public void setTelephoneCompany(String telephone_company) {
        delegate.setTelephoneCompany(telephone_company);
    }

    @Override
    public void setTelephoneHome1(String telephone_home1) {
        delegate.setTelephoneHome1(telephone_home1);
    }

    @Override
    public void setTelephoneHome2(String telephone_home2) {
        delegate.setTelephoneHome2(telephone_home2);
    }

    @Override
    public void setFaxHome(String fax_home) {
        delegate.setFaxHome(fax_home);
    }

    @Override
    public void setCellularTelephone1(String cellular_telephone1) {
        delegate.setCellularTelephone1(cellular_telephone1);
    }

    @Override
    public void setCellularTelephone2(String cellular_telephone2) {
        delegate.setCellularTelephone2(cellular_telephone2);
    }

    @Override
    public void setTelephoneOther(String telephone_other) {
        delegate.setTelephoneOther(telephone_other);
    }

    @Override
    public void setFaxOther(String fax_other) {
        delegate.setFaxOther(fax_other);
    }

    @Override
    public void setEmail1(String email1) {
        delegate.setEmail1(email1);
    }

    @Override
    public void setEmail2(String email2) {
        delegate.setEmail2(email2);
    }

    @Override
    public void setEmail3(String email3) {
        delegate.setEmail3(email3);
    }

    @Override
    public void setURL(String url) {
        delegate.setURL(url);
    }

    @Override
    public void setTelephoneISDN(String telephone_isdn) {
        delegate.setTelephoneISDN(telephone_isdn);
    }

    @Override
    public void setTelephonePager(String telephone_pager) {
        delegate.setTelephonePager(telephone_pager);
    }

    @Override
    public void setTelephonePrimary(String telephone_primary) {
        delegate.setTelephonePrimary(telephone_primary);
    }

    @Override
    public void setTelephoneRadio(String telephone_radio) {
        delegate.setTelephoneRadio(telephone_radio);
    }

    @Override
    public void setTelephoneTelex(String telephone_telex) {
        delegate.setTelephoneTelex(telephone_telex);
    }

    @Override
    public void setTelephoneTTYTTD(String telephone_ttyttd) {
        delegate.setTelephoneTTYTTD(telephone_ttyttd);
    }

    @Override
    public void setInstantMessenger1(String instant_messenger1) {
        delegate.setInstantMessenger1(instant_messenger1);
    }

    @Override
    public void setInstantMessenger2(String instant_messenger2) {
        delegate.setInstantMessenger2(instant_messenger2);
    }

    @Override
    public void setTelephoneIP(String phone_ip) {
        delegate.setTelephoneIP(phone_ip);
    }

    @Override
    public void setTelephoneAssistant(String telephone_assistant) {
        delegate.setTelephoneAssistant(telephone_assistant);
    }

    @Override
    public void setDefaultAddress(int defaultaddress) {
        delegate.setDefaultAddress(defaultaddress);
    }

    @Override
    public void setCompany(String company) {
        delegate.setCompany(company);
    }

    @Override
    public void setUserField01(String userfield01) {
        delegate.setUserField01(userfield01);
    }

    @Override
    public void setUserField02(String userfield02) {
        delegate.setUserField02(userfield02);
    }

    @Override
    public void setUserField03(String userfield03) {
        delegate.setUserField03(userfield03);
    }

    @Override
    public void setUserField04(String userfield04) {
        delegate.setUserField04(userfield04);
    }

    @Override
    public void setUserField05(String userfield05) {
        delegate.setUserField05(userfield05);
    }

    @Override
    public void setUserField06(String userfield06) {
        delegate.setUserField06(userfield06);
    }

    @Override
    public void setUserField07(String userfield07) {
        delegate.setUserField07(userfield07);
    }

    @Override
    public void setUserField08(String userfield08) {
        delegate.setUserField08(userfield08);
    }

    @Override
    public void setUserField09(String userfield09) {
        delegate.setUserField09(userfield09);
    }

    @Override
    public void setUserField10(String userfield10) {
        delegate.setUserField10(userfield10);
    }

    @Override
    public void setUserField11(String userfield11) {
        delegate.setUserField11(userfield11);
    }

    @Override
    public void setUserField12(String userfield12) {
        delegate.setUserField12(userfield12);
    }

    @Override
    public void setUserField13(String userfield13) {
        delegate.setUserField13(userfield13);
    }

    @Override
    public void setUserField14(String userfield14) {
        delegate.setUserField14(userfield14);
    }

    @Override
    public void setUserField15(String userfield15) {
        delegate.setUserField15(userfield15);
    }

    @Override
    public void setUserField16(String userfield16) {
        delegate.setUserField16(userfield16);
    }

    @Override
    public void setUserField17(String userfield17) {
        delegate.setUserField17(userfield17);
    }

    @Override
    public void setUserField18(String userfield18) {
        delegate.setUserField18(userfield18);
    }

    @Override
    public void setUserField19(String userfield19) {
        delegate.setUserField19(userfield19);
    }

    @Override
    public void setUserField20(String userfield20) {
        delegate.setUserField20(userfield20);
    }

    @Override
    public void setImage1(byte[] image1) {
        delegate.setImage1(image1);
    }

    @Override
    public void setImageContentType(String imageContentType) {
        delegate.setImageContentType(imageContentType);
    }

    @Override
    public void setNumberOfImages(int number_of_images) {
        delegate.setNumberOfImages(number_of_images);
    }

    @Override
    public void setNumberOfDistributionLists(int listsize) {
        delegate.setNumberOfDistributionLists(listsize);
    }

    @Override
    public void setDistributionList(DistributionListEntryObject[] dleo) {
        delegate.setDistributionList(dleo);
    }

    @Override
    public void setContextId(int cid) {
        delegate.setContextId(cid);
    }

    @Override
    public void setInternalUserId(int internal_userId) {
        delegate.setInternalUserId(internal_userId);
    }

    @Override
    public void setImageLastModified(Date image_last_modified) {
        delegate.setImageLastModified(image_last_modified);
    }

    @Override
    public void setFileAs(String file_as) {
        delegate.setFileAs(file_as);
    }

    @Override
    public void setMarkAsDistributionlist(boolean mark_as_disitributionlist) {
        delegate.setMarkAsDistributionlist(mark_as_disitributionlist);
    }

    @Override
    public void setUseCount(int useCount) {
        delegate.setUseCount(useCount);
    }

    @Override
    public void setYomiFirstName(String yomiFirstName) {
        delegate.setYomiFirstName(yomiFirstName);
    }

    @Override
    public void setYomiLastName(String yomiLastName) {
        delegate.setYomiLastName(yomiLastName);
    }

    @Override
    public void setYomiCompany(String yomiCompany) {
        delegate.setYomiCompany(yomiCompany);
    }

    @Override
    public void setAddressBusiness(String addressBusiness) {
        delegate.setAddressBusiness(addressBusiness);
    }

    @Override
    public void setAddressHome(String addressHome) {
        delegate.setAddressHome(addressHome);
    }

    @Override
    public void setAddressOther(String addressOther) {
        delegate.setAddressOther(addressOther);
    }

    @Override
    public void setVCardId(String vCardId) {
        delegate.setVCardId(vCardId);
    }

    @Override
    public void removeDisplayName() {
        delegate.removeDisplayName();
    }

    @Override
    public void removeGivenName() {
        delegate.removeGivenName();
    }

    @Override
    public void removeSurName() {
        delegate.removeSurName();
    }

    @Override
    public void removeMiddleName() {
        delegate.removeMiddleName();
    }

    @Override
    public void removeSuffix() {
        delegate.removeSuffix();
    }

    @Override
    public void removeTitle() {
        delegate.removeTitle();
    }

    @Override
    public void removeStreetHome() {
        delegate.removeStreetHome();
    }

    @Override
    public void removePostalCodeHome() {
        delegate.removePostalCodeHome();
    }

    @Override
    public void removeCityHome() {
        delegate.removeCityHome();
    }

    @Override
    public void removeStateHome() {
        delegate.removeStateHome();
    }

    @Override
    public void removeCountryHome() {
        delegate.removeCountryHome();
    }

    @Override
    public void removeBirthday() {
        delegate.removeBirthday();
    }

    @Override
    public void removeMaritalStatus() {
        delegate.removeMaritalStatus();
    }

    @Override
    public void removeNumberOfChildren() {
        delegate.removeNumberOfChildren();
    }

    @Override
    public void removeProfession() {
        delegate.removeProfession();
    }

    @Override
    public void removeNickname() {
        delegate.removeNickname();
    }

    @Override
    public void removeSpouseName() {
        delegate.removeSpouseName();
    }

    @Override
    public void removeAnniversary() {
        delegate.removeAnniversary();
    }

    @Override
    public void removeNote() {
        delegate.removeNote();
    }

    @Override
    public void removeDepartment() {
        delegate.removeDepartment();
    }

    @Override
    public void removePosition() {
        delegate.removePosition();
    }

    @Override
    public void removeEmployeeType() {
        delegate.removeEmployeeType();
    }

    @Override
    public void removeRoomNumber() {
        delegate.removeRoomNumber();
    }

    @Override
    public void removeStreetBusiness() {
        delegate.removeStreetBusiness();
    }

    @Override
    public void removePostalCodeBusiness() {
        delegate.removePostalCodeBusiness();
    }

    @Override
    public void removeCityBusiness() {
        delegate.removeCityBusiness();
    }

    @Override
    public void removeStateBusiness() {
        delegate.removeStateBusiness();
    }

    @Override
    public void removeCountryBusiness() {
        delegate.removeCountryBusiness();
    }

    @Override
    public void removeNumberOfEmployee() {
        delegate.removeNumberOfEmployee();
    }

    @Override
    public void removeSalesVolume() {
        delegate.removeSalesVolume();
    }

    @Override
    public void removeTaxID() {
        delegate.removeTaxID();
    }

    @Override
    public void removeCommercialRegister() {
        delegate.removeCommercialRegister();
    }

    @Override
    public void removeBranches() {
        delegate.removeBranches();
    }

    @Override
    public void removeBusinessCategory() {
        delegate.removeBusinessCategory();
    }

    @Override
    public void removeInfo() {
        delegate.removeInfo();
    }

    @Override
    public void removeManagerName() {
        delegate.removeManagerName();
    }

    @Override
    public void removeAssistantName() {
        delegate.removeAssistantName();
    }

    @Override
    public void removeStreetOther() {
        delegate.removeStreetOther();
    }

    @Override
    public void removePostalCodeOther() {
        delegate.removePostalCodeOther();
    }

    @Override
    public void removeCityOther() {
        delegate.removeCityOther();
    }

    @Override
    public void removeStateOther() {
        delegate.removeStateOther();
    }

    @Override
    public void removeCountryOther() {
        delegate.removeCountryOther();
    }

    @Override
    public void removeTelephoneBusiness1() {
        delegate.removeTelephoneBusiness1();
    }

    @Override
    public void removeTelephoneBusiness2() {
        delegate.removeTelephoneBusiness2();
    }

    @Override
    public void removeFaxBusiness() {
        delegate.removeFaxBusiness();
    }

    @Override
    public void removeFileAs() {
        delegate.removeFileAs();
    }

    @Override
    public void removeTelephoneCallback() {
        delegate.removeTelephoneCallback();
    }

    @Override
    public void removeTelephoneCar() {
        delegate.removeTelephoneCar();
    }

    @Override
    public void removeTelephoneCompany() {
        delegate.removeTelephoneCompany();
    }

    @Override
    public void removeTelephoneHome1() {
        delegate.removeTelephoneHome1();
    }

    @Override
    public void removeTelephoneHome2() {
        delegate.removeTelephoneHome2();
    }

    @Override
    public void removeFaxHome() {
        delegate.removeFaxHome();
    }

    @Override
    public void removeCellularTelephone1() {
        delegate.removeCellularTelephone1();
    }

    @Override
    public void removeCellularTelephone2() {
        delegate.removeCellularTelephone2();
    }

    @Override
    public void removeTelephoneOther() {
        delegate.removeTelephoneOther();
    }

    @Override
    public void removeFaxOther() {
        delegate.removeFaxOther();
    }

    @Override
    public void removeEmail1() {
        delegate.removeEmail1();
    }

    @Override
    public void removeEmail2() {
        delegate.removeEmail2();
    }

    @Override
    public void removeEmail3() {
        delegate.removeEmail3();
    }

    @Override
    public void removeURL() {
        delegate.removeURL();
    }

    @Override
    public void removeTelephoneISDN() {
        delegate.removeTelephoneISDN();
    }

    @Override
    public void removeTelephonePager() {
        delegate.removeTelephonePager();
    }

    @Override
    public void removeTelephonePrimary() {
        delegate.removeTelephonePrimary();
    }

    @Override
    public void removeTelephoneRadio() {
        delegate.removeTelephoneRadio();
    }

    @Override
    public void removeTelephoneTelex() {
        delegate.removeTelephoneTelex();
    }

    @Override
    public void removeTelephoneTTYTTD() {
        delegate.removeTelephoneTTYTTD();
    }

    @Override
    public void removeInstantMessenger1() {
        delegate.removeInstantMessenger1();
    }

    @Override
    public void removeInstantMessenger2() {
        delegate.removeInstantMessenger2();
    }

    @Override
    public void removeImageLastModified() {
        delegate.removeImageLastModified();
    }

    @Override
    public void removeTelephoneIP() {
        delegate.removeTelephoneIP();
    }

    @Override
    public void removeTelephoneAssistant() {
        delegate.removeTelephoneAssistant();
    }

    @Override
    public void removeDefaultAddress() {
        delegate.removeDefaultAddress();
    }

    @Override
    public void removeCompany() {
        delegate.removeCompany();
    }

    @Override
    public void removeImage1() {
        delegate.removeImage1();
    }

    @Override
    public void removeImageContentType() {
        delegate.removeImageContentType();
    }

    @Override
    public void removeUserField01() {
        delegate.removeUserField01();
    }

    @Override
    public void removeUserField02() {
        delegate.removeUserField02();
    }

    @Override
    public void removeUserField03() {
        delegate.removeUserField03();
    }

    @Override
    public void removeUserField04() {
        delegate.removeUserField04();
    }

    @Override
    public void removeUserField05() {
        delegate.removeUserField05();
    }

    @Override
    public void removeUserField06() {
        delegate.removeUserField06();
    }

    @Override
    public void removeUserField07() {
        delegate.removeUserField07();
    }

    @Override
    public void removeUserField08() {
        delegate.removeUserField08();
    }

    @Override
    public void removeUserField09() {
        delegate.removeUserField09();
    }

    @Override
    public void removeUserField10() {
        delegate.removeUserField10();
    }

    @Override
    public void removeUserField11() {
        delegate.removeUserField11();
    }

    @Override
    public void removeUserField12() {
        delegate.removeUserField12();
    }

    @Override
    public void removeUserField13() {
        delegate.removeUserField13();
    }

    @Override
    public void removeUserField14() {
        delegate.removeUserField14();
    }

    @Override
    public void removeUserField15() {
        delegate.removeUserField15();
    }

    @Override
    public void removeUserField16() {
        delegate.removeUserField16();
    }

    @Override
    public void removeUserField17() {
        delegate.removeUserField17();
    }

    @Override
    public void removeUserField18() {
        delegate.removeUserField18();
    }

    @Override
    public void removeUserField19() {
        delegate.removeUserField19();
    }

    @Override
    public void removeUserField20() {
        delegate.removeUserField20();
    }

    @Override
    public void removeNumberOfDistributionLists() {
        delegate.removeNumberOfDistributionLists();
    }

    @Override
    public void removeDistributionLists() {
        delegate.removeDistributionLists();
    }

    @Override
    public void removeMarkAsDistributionlist() {
        delegate.removeMarkAsDistributionlist();
    }

    @Override
    public void removeContextID() {
        delegate.removeContextID();
    }

    @Override
    public void removeInternalUserId() {
        delegate.removeInternalUserId();
    }

    @Override
    public void removeUseCount() {
        delegate.removeUseCount();
    }

    @Override
    public void removeYomiFirstName() {
        delegate.removeYomiFirstName();
    }

    @Override
    public void removeYomiLastName() {
        delegate.removeYomiLastName();
    }

    @Override
    public void removeYomiCompany() {
        delegate.removeYomiCompany();
    }

    @Override
    public void removeAddressHome() {
        delegate.removeAddressHome();
    }

    @Override
    public void removeAddressBusiness() {
        delegate.removeAddressBusiness();
    }

    @Override
    public void removeAddressOther() {
        delegate.removeAddressOther();
    }

    @Override
    public void removeVCardId() {
        delegate.removeVCardId();
    }

    @Override
    public boolean containsDisplayName() {
        return delegate.containsDisplayName();
    }

    @Override
    public boolean containsGivenName() {
        return delegate.containsGivenName();
    }

    @Override
    public boolean containsSurName() {
        return delegate.containsSurName();
    }

    @Override
    public boolean containsMiddleName() {
        return delegate.containsMiddleName();
    }

    @Override
    public boolean containsSuffix() {
        return delegate.containsSuffix();
    }

    @Override
    public boolean containsTitle() {
        return delegate.containsTitle();
    }

    @Override
    public boolean containsStreetHome() {
        return delegate.containsStreetHome();
    }

    @Override
    public boolean containsPostalCodeHome() {
        return delegate.containsPostalCodeHome();
    }

    @Override
    public boolean containsCityHome() {
        return delegate.containsCityHome();
    }

    @Override
    public boolean containsStateHome() {
        return delegate.containsStateHome();
    }

    @Override
    public boolean containsCountryHome() {
        return delegate.containsCountryHome();
    }

    @Override
    public boolean containsBirthday() {
        return delegate.containsBirthday();
    }

    @Override
    public boolean containsMaritalStatus() {
        return delegate.containsMaritalStatus();
    }

    @Override
    public boolean containsNumberOfChildren() {
        return delegate.containsNumberOfChildren();
    }

    @Override
    public boolean containsProfession() {
        return delegate.containsProfession();
    }

    @Override
    public boolean containsNickname() {
        return delegate.containsNickname();
    }

    @Override
    public boolean containsSpouseName() {
        return delegate.containsSpouseName();
    }

    @Override
    public boolean containsAnniversary() {
        return delegate.containsAnniversary();
    }

    @Override
    public boolean containsNote() {
        return delegate.containsNote();
    }

    @Override
    public boolean containsDepartment() {
        return delegate.containsDepartment();
    }

    @Override
    public boolean containsPosition() {
        return delegate.containsPosition();
    }

    @Override
    public boolean containsEmployeeType() {
        return delegate.containsEmployeeType();
    }

    @Override
    public boolean containsRoomNumber() {
        return delegate.containsRoomNumber();
    }

    @Override
    public boolean containsStreetBusiness() {
        return delegate.containsStreetBusiness();
    }

    @Override
    public boolean containsPostalCodeBusiness() {
        return delegate.containsPostalCodeBusiness();
    }

    @Override
    public boolean containsCityBusiness() {
        return delegate.containsCityBusiness();
    }

    @Override
    public boolean containsStateBusiness() {
        return delegate.containsStateBusiness();
    }

    @Override
    public boolean containsCountryBusiness() {
        return delegate.containsCountryBusiness();
    }

    @Override
    public boolean containsNumberOfEmployee() {
        return delegate.containsNumberOfEmployee();
    }

    @Override
    public boolean containsSalesVolume() {
        return delegate.containsSalesVolume();
    }

    @Override
    public boolean containsTaxID() {
        return delegate.containsTaxID();
    }

    @Override
    public boolean containsCommercialRegister() {
        return delegate.containsCommercialRegister();
    }

    @Override
    public boolean containsBranches() {
        return delegate.containsBranches();
    }

    @Override
    public boolean containsBusinessCategory() {
        return delegate.containsBusinessCategory();
    }

    @Override
    public boolean containsInfo() {
        return delegate.containsInfo();
    }

    @Override
    public boolean containsManagerName() {
        return delegate.containsManagerName();
    }

    @Override
    public boolean containsAssistantName() {
        return delegate.containsAssistantName();
    }

    @Override
    public boolean containsStreetOther() {
        return delegate.containsStreetOther();
    }

    @Override
    public boolean containsPostalCodeOther() {
        return delegate.containsPostalCodeOther();
    }

    @Override
    public boolean containsCityOther() {
        return delegate.containsCityOther();
    }

    @Override
    public boolean containsStateOther() {
        return delegate.containsStateOther();
    }

    @Override
    public boolean containsCountryOther() {
        return delegate.containsCountryOther();
    }

    @Override
    public boolean containsTelephoneBusiness1() {
        return delegate.containsTelephoneBusiness1();
    }

    @Override
    public boolean containsTelephoneBusiness2() {
        return delegate.containsTelephoneBusiness2();
    }

    @Override
    public boolean containsFaxBusiness() {
        return delegate.containsFaxBusiness();
    }

    @Override
    public boolean containsTelephoneCallback() {
        return delegate.containsTelephoneCallback();
    }

    @Override
    public boolean containsTelephoneCar() {
        return delegate.containsTelephoneCar();
    }

    @Override
    public boolean containsTelephoneCompany() {
        return delegate.containsTelephoneCompany();
    }

    @Override
    public boolean containsTelephoneHome1() {
        return delegate.containsTelephoneHome1();
    }

    @Override
    public boolean containsTelephoneHome2() {
        return delegate.containsTelephoneHome2();
    }

    @Override
    public boolean containsFaxHome() {
        return delegate.containsFaxHome();
    }

    @Override
    public boolean containsCellularTelephone1() {
        return delegate.containsCellularTelephone1();
    }

    @Override
    public boolean containsCellularTelephone2() {
        return delegate.containsCellularTelephone2();
    }

    @Override
    public boolean containsTelephoneOther() {
        return delegate.containsTelephoneOther();
    }

    @Override
    public boolean containsFaxOther() {
        return delegate.containsFaxOther();
    }

    @Override
    public boolean containsEmail1() {
        return delegate.containsEmail1();
    }

    @Override
    public boolean containsEmail2() {
        return delegate.containsEmail2();
    }

    @Override
    public boolean containsEmail3() {
        return delegate.containsEmail3();
    }

    @Override
    public boolean containsURL() {
        return delegate.containsURL();
    }

    @Override
    public boolean containsTelephoneISDN() {
        return delegate.containsTelephoneISDN();
    }

    @Override
    public boolean containsTelephonePager() {
        return delegate.containsTelephonePager();
    }

    @Override
    public boolean containsTelephonePrimary() {
        return delegate.containsTelephonePrimary();
    }

    @Override
    public boolean containsTelephoneRadio() {
        return delegate.containsTelephoneRadio();
    }

    @Override
    public boolean containsTelephoneTelex() {
        return delegate.containsTelephoneTelex();
    }

    @Override
    public boolean containsTelephoneTTYTTD() {
        return delegate.containsTelephoneTTYTTD();
    }

    @Override
    public boolean containsInstantMessenger1() {
        return delegate.containsInstantMessenger1();
    }

    @Override
    public boolean containsInstantMessenger2() {
        return delegate.containsInstantMessenger2();
    }

    @Override
    public boolean containsTelephoneIP() {
        return delegate.containsTelephoneIP();
    }

    @Override
    public boolean containsTelephoneAssistant() {
        return delegate.containsTelephoneAssistant();
    }

    @Override
    public boolean containsDefaultAddress() {
        return delegate.containsDefaultAddress();
    }

    @Override
    public boolean containsCompany() {
        return delegate.containsCompany();
    }

    @Override
    public boolean containsUserField01() {
        return delegate.containsUserField01();
    }

    @Override
    public boolean containsUserField02() {
        return delegate.containsUserField02();
    }

    @Override
    public boolean containsUserField03() {
        return delegate.containsUserField03();
    }

    @Override
    public boolean containsUserField04() {
        return delegate.containsUserField04();
    }

    @Override
    public boolean containsUserField05() {
        return delegate.containsUserField05();
    }

    @Override
    public boolean containsUserField06() {
        return delegate.containsUserField06();
    }

    @Override
    public boolean containsUserField07() {
        return delegate.containsUserField07();
    }

    @Override
    public boolean containsUserField08() {
        return delegate.containsUserField08();
    }

    @Override
    public boolean containsUserField09() {
        return delegate.containsUserField09();
    }

    @Override
    public boolean containsUserField10() {
        return delegate.containsUserField10();
    }

    @Override
    public boolean containsUserField11() {
        return delegate.containsUserField11();
    }

    @Override
    public boolean containsUserField12() {
        return delegate.containsUserField12();
    }

    @Override
    public boolean containsUserField13() {
        return delegate.containsUserField13();
    }

    @Override
    public boolean containsUserField14() {
        return delegate.containsUserField14();
    }

    @Override
    public boolean containsUserField15() {
        return delegate.containsUserField15();
    }

    @Override
    public boolean containsUserField16() {
        return delegate.containsUserField16();
    }

    @Override
    public boolean containsUserField17() {
        return delegate.containsUserField17();
    }

    @Override
    public boolean containsUserField18() {
        return delegate.containsUserField18();
    }

    @Override
    public boolean containsUserField19() {
        return delegate.containsUserField19();
    }

    @Override
    public boolean containsUserField20() {
        return delegate.containsUserField20();
    }

    @Override
    public boolean containsImage1() {
        return delegate.containsImage1();
    }

    @Override
    public boolean containsImageContentType() {
        return delegate.containsImageContentType();
    }

    @Override
    public boolean containsNumberOfDistributionLists() {
        return delegate.containsNumberOfDistributionLists();
    }

    @Override
    public boolean containsDistributionLists() {
        return delegate.containsDistributionLists();
    }

    @Override
    public int getSizeOfDistributionListArray() {
        return delegate.getSizeOfDistributionListArray();
    }

    @Override
    public boolean containsInternalUserId() {
        return delegate.containsInternalUserId();
    }

    @Override
    public boolean containsContextId() {
        return delegate.containsContextId();
    }

    @Override
    public boolean containsImageLastModified() {
        return delegate.containsImageLastModified();
    }

    @Override
    public boolean containsFileAs() {
        return delegate.containsFileAs();
    }

    @Override
    public boolean containsMarkAsDistributionlist() {
        return delegate.containsMarkAsDistributionlist();
    }

    @Override
    public boolean containsUseCount() {
        return delegate.containsUseCount();
    }

    @Override
    public boolean containsYomiFirstName() {
        return delegate.containsYomiFirstName();
    }

    @Override
    public boolean containsYomiLastName() {
        return delegate.containsYomiLastName();
    }

    @Override
    public boolean containsYomiCompany() {
        return delegate.containsYomiCompany();
    }

    @Override
    public boolean containsAddressHome() {
        return delegate.containsAddressHome();
    }

    @Override
    public boolean containsAddressBusiness() {
        return delegate.containsAddressBusiness();
    }

    @Override
    public boolean containsAddressOther() {
        return delegate.containsAddressOther();
    }

    @Override
    public boolean containsVCardId() {
        return delegate.containsVCardId();
    }

    @Override
    public boolean containsId() {
        return delegate.containsId();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void setId(String id) {
        delegate.setId(id);
    }

    @Override
    public void removeId() {
        delegate.removeId();
    }

    @Override
    public boolean containsFolderId() {
        return delegate.containsFolderId();
    }

    @Override
    public String getFolderId() {
        return delegate.getFolderId();
    }

    @Override
    public void setFolderId(String id) {
        delegate.setFolderId(id);
    }

    @Override
    public void removeFolderId() {
        delegate.removeFolderId();
    }

    @Override
    public void set(int field, Object value) {
        delegate.set(field, value);
    }

    @Override
    public Object get(int field) {
        return delegate.get(field);
    }

    @Override
    public boolean contains(int field) {
        return delegate.contains(field);
    }

    @Override
    public void remove(int field) {
        delegate.remove(field);
    }

    @Override
    public boolean canFormDisplayName() {
        return delegate.canFormDisplayName();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public Contact clone() {
        return delegate.clone();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public boolean equalsContentwise(Object obj) {
        return delegate.equalsContentwise(obj);
    }

    @Override
    public boolean matches(Contact other, int[] fields) {
        return delegate.matches(other, fields);
    }

    @Override
    public String getSortName() {
        return delegate.getSortName();
    }

    @Override
    public String getSortName(Locale locale) {
        return delegate.getSortName(locale);
    }

}
