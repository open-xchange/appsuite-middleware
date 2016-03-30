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

    private boolean orSearch = false;

    private boolean exactMatch = false;

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

}
