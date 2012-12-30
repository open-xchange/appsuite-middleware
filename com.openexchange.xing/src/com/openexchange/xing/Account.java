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

package com.openexchange.xing;

import static com.openexchange.xing.util.JSONCoercion.coerceToNative;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;

/**
 * {@link Account} - Represents a XING user account.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Account {

    private final String id;
    private final String displayName;
    private final String lastName;
    private final String firstName;
    private final String pageName;
    private final String permalink;
    private final String gender;
    private final String activeMail;
    private final TimeZone timeZone;
    private final List<String> premiumServices;
    private final List<String> badges;
    private final String wants;
    private final String haves;
    private final String interests;
    private final String organisationMember;
    private final Map<Locale, String> languages;
    private final Address privateAddress;
    private final Address businessAddress;
    private final Map<String, List<String>> webProfiles;
    private final Map<String, String> instantMessagingAccounts;
    private final Map<String, Object> professionalExperience;
    private final Map<String, Object> educationalBackground;
    private final Map<String, Object> photoUrls;

    /**
     * Initializes a new {@link Account}.
     * 
     * @param accountInfo The JSON account information
     * @throws XingException If parsing account information fails
     */
    public Account(final JSONObject accountInfo) throws XingException {
        super();
        try {
            this.id = accountInfo.optString("id");
            this.firstName = accountInfo.optString("first_name");
            this.lastName = accountInfo.optString("last_name");
            this.displayName = accountInfo.optString("display_name");
            this.pageName = accountInfo.optString("page_name");
            this.permalink = accountInfo.optString("permalink");
            this.gender = accountInfo.optString("gender");
            this.activeMail = accountInfo.optString("active_email");
            if (accountInfo.has("time_zone")) {
                final JSONObject tz = accountInfo.optJSONObject("time_zone");
                this.timeZone = TimeZone.getTimeZone(tz.getString("name"));
            } else {
                this.timeZone = null;
            }
            if (accountInfo.has("premium_services")) {
                final JSONArray ps = accountInfo.optJSONArray("premium_services");
                final int length = ps.length();
                premiumServices = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    premiumServices.add(ps.getString(i));
                }
            } else {
                premiumServices = Collections.emptyList();
            }
            if (accountInfo.has("badges")) {
                final JSONArray b = accountInfo.optJSONArray("badges");
                final int length = b.length();
                badges = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    badges.add(b.getString(i));
                }
            } else {
                badges = Collections.emptyList();
            }
            this.wants = accountInfo.getString("wants");
            this.haves = accountInfo.getString("haves");
            this.interests = accountInfo.getString("interests");
            this.organisationMember = accountInfo.getString("organisation_member");
            if (accountInfo.has("languages")) {
                final JSONObject l = accountInfo.optJSONObject("languages");
                languages = new LinkedHashMap<Locale, String>(l.length());
                for (final Entry<String, Object> entry : l.entrySet()) {
                    final Object value = entry.getValue();
                    if (null != value) {
                        languages.put(new Locale(entry.getKey().toLowerCase(Locale.US)), value.toString());
                    }
                }
            } else {
                languages = Collections.emptyMap();
            }
            if (accountInfo.has("private_address")) {
                this.privateAddress = new Address(accountInfo.optJSONObject("private_address"));
            } else {
                this.privateAddress = null;
            }
            if (accountInfo.has("business_address")) {
                this.businessAddress = new Address(accountInfo.optJSONObject("business_address"));
            } else {
                this.businessAddress = null;
            }
            if (accountInfo.has("web_profiles")) {
                final JSONObject jo = accountInfo.optJSONObject("web_profiles");
                this.webProfiles = new LinkedHashMap<String, List<String>>(jo.length());
                for (final String key : jo.keySet()) {
                    final JSONArray ja = jo.optJSONArray(key);
                    final int length = ja.length();
                    final List<String> addrs = new ArrayList<String>(length);
                    for (int i = 0; i < length; i++) {
                        addrs.add(ja.getString(i));
                    }
                    webProfiles.put(key, addrs);
                }
            } else {
                webProfiles = Collections.emptyMap();
            }
            if (accountInfo.has("instant_messaging_accounts")) {
                final JSONObject jo = accountInfo.optJSONObject("instant_messaging_accounts");
                instantMessagingAccounts = new LinkedHashMap<String, String>(jo.length());
                for (final Entry<String, Object> entry : jo.entrySet()) {
                    final Object value = entry.getValue();
                    if (null != value) {
                        instantMessagingAccounts.put(entry.getKey(), value.toString());
                    }
                }
            } else {
                instantMessagingAccounts = Collections.emptyMap();
            }
            if (accountInfo.has("professional_experience")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) coerceToNative(accountInfo.optJSONObject("professional_experience"));
                this.professionalExperience = map;
            } else {
                this.professionalExperience = Collections.emptyMap();
            }
            if (accountInfo.has("educational_background")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) coerceToNative(accountInfo.optJSONObject("educational_background"));
                this.educationalBackground = map;
            } else {
                this.educationalBackground = Collections.emptyMap();
            }
            if (accountInfo.has("photo_urls")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) coerceToNative(accountInfo.optJSONObject("photo_urls"));
                this.photoUrls = map;
            } else {
                this.photoUrls = Collections.emptyMap();
            }
        } catch (final JSONException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the identifier
     * 
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the last name
     * 
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the first name
     * 
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the page name
     * 
     * @return The page name
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * Gets the perma link
     * 
     * @return The perma link
     */
    public String getPermalink() {
        return permalink;
    }

    /**
     * Gets the gender
     * 
     * @return The gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Gets the active mail
     * 
     * @return The active mail
     */
    public String getActiveMail() {
        return activeMail;
    }

    /**
     * Gets the timeZone
     * 
     * @return The timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Gets the XING premium services
     * 
     * @return The XING premium services
     */
    public List<String> getPremiumServices() {
        return premiumServices;
    }

    /**
     * Gets the badges
     * 
     * @return The badges
     */
    public List<String> getBadges() {
        return badges;
    }

    /**
     * Gets the wants
     * 
     * @return The wants
     */
    public String getWants() {
        return wants;
    }

    /**
     * Gets the haves
     * 
     * @return The haves
     */
    public String getHaves() {
        return haves;
    }

    /**
     * Gets the interests
     * 
     * @return The interests
     */
    public String getInterests() {
        return interests;
    }

    /**
     * Gets the organisation member
     * 
     * @return The organisation member
     */
    public String getOrganisationMember() {
        return organisationMember;
    }

    /**
     * Gets the languages
     * 
     * @return The languages
     */
    public Map<Locale, String> getLanguages() {
        return languages;
    }

    /**
     * Gets the private address
     * 
     * @return The private address
     */
    public Address getPrivateAddress() {
        return privateAddress;
    }

    /**
     * Gets the business address
     * 
     * @return The business address
     */
    public Address getBusinessAddress() {
        return businessAddress;
    }

    /**
     * Gets the web profiles
     * 
     * @return The web profiles
     */
    public Map<String, List<String>> getWebProfiles() {
        return webProfiles;
    }

    /**
     * Gets the IM accounts
     * 
     * @return The IM accounts
     */
    public Map<String, String> getInstantMessagingAccounts() {
        return instantMessagingAccounts;
    }

    /**
     * Gets the professional experience
     * 
     * @return The professional experience
     */
    public Map<String, Object> getProfessionalExperience() {
        return professionalExperience;
    }

    /**
     * Gets the educational background
     * 
     * @return The educational background
     */
    public Map<String, Object> getEducationalBackground() {
        return educationalBackground;
    }

    /**
     * Gets the photo URLs
     * 
     * @return The photo URLs
     */
    public Map<String, Object> getPhotoUrls() {
        return photoUrls;
    }

}
