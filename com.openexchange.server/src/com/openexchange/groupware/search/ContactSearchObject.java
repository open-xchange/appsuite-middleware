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

package com.openexchange.groupware.search;

import java.util.Date;

public class ContactSearchObject extends SearchObject {

    private String surname = null;

    private String yomiSurname = null;

    private String displayName = null;

    private String givenname = null;

    private String yomiGivenname = null;

    private String company = null;

    private String yomiCompany = null;

	private String email1 = null;

    private String email2 = null;

    private String email3 = null;

    private String city_business = null;

    private String street_business = null;

    private String department = null;

    @Deprecated
    private String from = null, to = null;

    @Deprecated
    private int ignoreOwn = 0;

    @Deprecated
    private int[] dynamicSearchField = null;

    @Deprecated
    private String[] dynamicSearchFieldValue = null;

    @Deprecated
    private String[] privatePostalCodeRange = null;

    @Deprecated
    private String[] businessPostalCodeRange = null;

    @Deprecated
    private String[] otherPostalCodeRange = null;

    @Deprecated
    private Date[] birthdayRange = null;

    @Deprecated
    private Date[] anniversaryRange = null;

    @Deprecated
    private String[] numberOfEmployeesRange = null;

    @Deprecated
    private String[] salesVolumeRange = null;

    @Deprecated
    private Date[] creationDateRange = null;

    @Deprecated
    private Date[] lastModifiedRange = null;

    @Deprecated
    private String allFolderSQLINString = null;

    private boolean startLetter = false;

    private boolean emailAutoComplete = false;

    private boolean hasImage = false;

    private boolean orSearch = false;

    private boolean exactMatch = false;

    private int[] userIds = null;

    public ContactSearchObject() {
        super();
    }

    @Deprecated
    public Date[] getAnniversaryRange() {
        return anniversaryRange;
    }

    @Deprecated
    public void setAnniversaryRange(final Date[] anniversaryRange) {
        this.anniversaryRange = anniversaryRange;
    }

    @Deprecated
    public Date[] getBirthdayRange() {
        return birthdayRange;
    }

    @Deprecated
    public void setBirthdayRange(final Date[] birthdayRange) {
        this.birthdayRange = birthdayRange;
    }

    @Deprecated
    public String[] getBusinessPostalCodeRange() {
        return businessPostalCodeRange;
    }

    @Deprecated
    public void setBusinessPostalCodeRange(final String[] businessPostalCodeRange) {
        this.businessPostalCodeRange = businessPostalCodeRange;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(final String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(final String email2) {
        this.email2 = email2;
    }

    public String getEmail3() {
        return email3;
    }

    public void setEmail3(final String email3) {
        this.email3 = email3;
    }

    /**
     * Convenience method to set all E-Mail fields to specified value.
     * <p>
     * Simply a short-hand for calling {@link #setEmail1(String) setEmail1}, {@link #setEmail2(String) setEmail2} and {@link #setEmail3(String) setEmail3} for the same value.
     *
     * @param email The E-Mail address to set
     */
    public void setAllEmail(final String email) {
        this.email1 = email;
        this.email2 = email;
        this.email3 = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(final String department) {
        this.department = department;
    }

    public String getCityBusiness() {
        return city_business;
    }

    public void setCityBusiness(final String city_business) {
        this.city_business = city_business;
    }

    public String getStreetBusiness() {
        return street_business;
    }

    public void setStreetBusiness(final String street_business) {
        this.street_business = street_business;
    }

    @Deprecated
    public Date[] getCreationDateRange() {
        return creationDateRange;
    }

    @Deprecated
    public void setCreationDateRange(final Date[] creationDateRange) {
        this.creationDateRange = creationDateRange;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Deprecated
    public int[] getDynamicSearchField() {
        return dynamicSearchField;
    }

    @Deprecated
    public void setDynamicSearchField(final int[] dynamicSearchField) {
        this.dynamicSearchField = dynamicSearchField;
    }

    @Deprecated
    public String[] getDynamicSearchFieldValue() {
        return dynamicSearchFieldValue;
    }

    @Deprecated
    public void setDynamicSearchFieldValue(final String[] dynamicSearchFieldValue) {
        this.dynamicSearchFieldValue = dynamicSearchFieldValue;
    }

    public String getGivenName() {
        return givenname;
    }

    @Deprecated
    public String getAllFolderSQLINString(){
        return allFolderSQLINString;
    }

    @Deprecated
    public int getIgnoreOwn(){
        return ignoreOwn;
    }

    @Deprecated
    public void setIgnoreOwn(final int ignoreOwn){
        this.ignoreOwn = ignoreOwn;
    }

    @Deprecated
    public void setAllFolderSQLINString(final String allFolderSQLINString){
        this.allFolderSQLINString = allFolderSQLINString;
    }

    public void setGivenName(final String forename) {
        this.givenname = forename;
    }

    @Deprecated
    public Date[] getLastModifiedRange() {
        return lastModifiedRange;
    }

    @Deprecated
    public void setLastModifiedRange(final Date[] lastModifiedRange) {
        this.lastModifiedRange = lastModifiedRange;
    }

    @Deprecated
    public String[] getNumberOfEmployeesRange() {
        return numberOfEmployeesRange;
    }

    @Deprecated
    public void setNumberOfEmployeesRange(final String[] numberOfEmployeesRange) {
        this.numberOfEmployeesRange = numberOfEmployeesRange;
    }

    @Deprecated
    public String[] getOtherPostalCodeRange() {
        return otherPostalCodeRange;
    }

    @Deprecated
    public void setOtherPostalCodeRange(final String[] otherPostalCodeRange) {
        this.otherPostalCodeRange = otherPostalCodeRange;
    }

    @Deprecated
    public String[] getPrivatePostalCodeRange() {
        return privatePostalCodeRange;
    }

    @Deprecated
    public void setPrivatePostalCodeRange(final String[] privatePostalCodeRange) {
        this.privatePostalCodeRange = privatePostalCodeRange;
    }

    @Deprecated
    public String[] getSalesVolumeRange() {
        return salesVolumeRange;
    }

    @Deprecated
    public void setSalesVolumeRange(final String[] salesVolumeRange) {
        this.salesVolumeRange = salesVolumeRange;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public void setEmailAutoComplete(final boolean status){
        this.emailAutoComplete = status;
    }

    public boolean isEmailAutoComplete(){
        return emailAutoComplete;
    }

    public boolean isOrSearch() {
        return orSearch;
    }

    public void setOrSearch(final boolean orSearch) {
        this.orSearch = orSearch;
    }

    public final boolean isStartLetter() {
        return startLetter;
    }

    public final void setStartLetter(final boolean startLetter) {
        this.startLetter = startLetter;
    }

    public String getYomiLastName() {
		return yomiSurname;
	}

	public void setYomiLastName(String yomiFirstName) {
		this.yomiSurname = yomiFirstName;
	}

	public String getYomiFirstName() {
		return yomiGivenname;
	}

	public void setYomiFirstname(String yomiFirstName) {
		this.yomiGivenname = yomiFirstName;
	}

	public String getYomiCompany() {
		return yomiCompany;
	}

	public void setYomiCompany(String yomiCompany) {
		this.yomiCompany = yomiCompany;
	}

    @Deprecated
	public void setFrom(String from) {
		this.from = from;
	}

    @Deprecated
	public String getFrom() {
		return from;
	}

    @Deprecated
	public void setTo(String to) {
		this.to = to;
	}

    @Deprecated
	public String getTo() {
		return to;
	}

    /**
     * Gets the hasImage
     *
     * @return The hasImage
     */
    public boolean hasImage() {
        return hasImage;
    }

    /**
     * Sets the hasImage
     *
     * @param hasImage The hasImage to set
     */
    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    /**
     * Gets the exactMatch
     *
     * @return The exactMatch
     */
    public boolean isExactMatch() {
        return exactMatch;
    }

    /**
     * Sets the exactMatch
     *
     * @param exactMatch The exactMatch to set
     */
    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public int[] getUserIds() {
        return userIds;
    }

    public void setUserIds(int[] userIds) {
        this.userIds = userIds;
    }

}
