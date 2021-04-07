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
 *    trademarks of the OX Software GmbH. group of companies.
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

import java.util.Set;

/**
 * {@link ContactsSearchObject}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsSearchObject {

    /**
     * No search pattern.
     */
    public static final String NO_PATTERN = null;

    /**
     * No category search.
     */
    public static final String NO_CATEGORIES = null;

    private String givenname = null;
    private String surname = null;
    private String displayName = null;

    private String company = null;

    private String email1 = null;
    private String email2 = null;
    private String email3 = null;

    private boolean startLetter = false;
    private boolean emailAutoComplete = false;
    private boolean hasImage = false;
    private boolean orSearch = false;
    private boolean exactMatch = false;

    private Set<String> folders;
    private Set<String> excludeFolders;

    private String pattern = NO_PATTERN;
    private String catgories = NO_CATEGORIES;

    private boolean subfolderSearch;

    /**
     * Initializes a new {@link ContactsSearchObject}.
     */
    public ContactsSearchObject() {
        super();
    }

    /**
     * Initializes a new {@link ContactsSearchObject} based on antoher one.
     *
     * @param other The search object to take over the properties from
     */
    public ContactsSearchObject(ContactsSearchObject other) {
        this();
        setCatgories(other.getCatgories());
        setCompany(other.getCompany());
        setDisplayName(other.getDisplayName());
        setEmail1(other.getEmail1());
        setEmail2(other.getEmail2());
        setEmail3(other.getEmail3());
        setEmailAutoComplete(other.isEmailAutoComplete());
        setExactMatch(other.isExactMatch());
        setExcludeFolders(other.getExcludeFolders());
        setFolders(other.getExcludeFolders());
        setGivenName(other.getGivenName());
        setHasImage(other.isHasImage());
        setOrSearch(other.isOrSearch());
        setPattern(other.getPattern());
        setStartLetter(other.isStartLetter());
        setSubfolderSearch(other.isSubfolderSearch());
        setSurname(other.getSurname());
    }

    /**
     * Gets the givenname
     *
     * @return The givenname
     */
    public String getGivenName() {
        return givenname;
    }

    /**
     * Sets the givenname
     *
     * @param givenname The givenname to set
     */
    public void setGivenName(String givenname) {
        this.givenname = givenname;
    }

    /**
     * Gets the surname
     *
     * @return The surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets the surname
     *
     * @param surname The surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Gets the displayName
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the displayName
     *
     * @param displayName The displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    /**
     * Gets the company
     *
     * @return The company
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the company
     *
     * @param company The company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Gets the email1
     *
     * @return The email1
     */
    public String getEmail1() {
        return email1;
    }

    /**
     * Sets the email1
     *
     * @param email1 The email1 to set
     */
    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    /**
     * Gets the email2
     *
     * @return The email2
     */
    public String getEmail2() {
        return email2;
    }

    /**
     * Sets the email2
     *
     * @param email2 The email2 to set
     */
    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    /**
     * Gets the email3
     *
     * @return The email3
     */
    public String getEmail3() {
        return email3;
    }

    /**
     * Sets the email3
     *
     * @param email3 The email3 to set
     */
    public void setEmail3(String email3) {
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

    /**
     * Gets the startLetter
     *
     * @return The startLetter
     */
    public boolean isStartLetter() {
        return startLetter;
    }

    /**
     * Sets the startLetter
     *
     * @param startLetter The startLetter to set
     */
    public void setStartLetter(boolean startLetter) {
        this.startLetter = startLetter;
    }

    /**
     * Gets the emailAutoComplete
     *
     * @return The emailAutoComplete
     */
    public boolean isEmailAutoComplete() {
        return emailAutoComplete;
    }

    /**
     * Sets the emailAutoComplete
     *
     * @param emailAutoComplete The emailAutoComplete to set
     */
    public void setEmailAutoComplete(boolean emailAutoComplete) {
        this.emailAutoComplete = emailAutoComplete;
    }

    /**
     * Gets the hasImage
     *
     * @return The hasImage
     */
    public boolean isHasImage() {
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
     * Gets the orSearch
     *
     * @return The orSearch
     */
    public boolean isOrSearch() {
        return orSearch;
    }

    /**
     * Sets the orSearch
     *
     * @param orSearch The orSearch to set
     */
    public void setOrSearch(boolean orSearch) {
        this.orSearch = orSearch;
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

    /**
     * Gets the folders
     *
     * @return The folders
     */
    public Set<String> getFolders() {
        return folders;
    }

    /**
     * Sets the folders
     *
     * @param folders The folders to set
     */
    public void setFolders(Set<String> folders) {
        this.folders = folders;
    }

    /**
     * Gets the pattern
     *
     * @return The pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the pattern
     *
     * @param pattern The pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Gets the catgories
     *
     * @return The catgories
     */
    public String getCatgories() {
        return catgories;
    }

    /**
     * Sets the catgories
     *
     * @param catgories The catgories to set
     */
    public void setCatgories(String catgories) {
        this.catgories = catgories;
    }

    /**
     * Gets the subfolderSearch
     *
     * @return The subfolderSearch
     */
    public boolean isSubfolderSearch() {
        return subfolderSearch;
    }

    /**
     * Sets the subfolderSearch
     *
     * @param subfolderSearch The subfolderSearch to set
     */
    public void setSubfolderSearch(boolean subfolderSearch) {
        this.subfolderSearch = subfolderSearch;
    }

    /**
     * Gets the excludeFolders
     *
     * @return The excludeFolders
     */
    public Set<String> getExcludeFolders() {
        return excludeFolders;
    }

    public void setExcludeFolders(Set<String> excludeFolders) {
        this.excludeFolders = excludeFolders;
    }

}
