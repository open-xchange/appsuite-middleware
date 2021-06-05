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

package com.openexchange.xing;

import static com.openexchange.xing.util.JSONCoercion.coerceToNative;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
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
 * {@link User} - Represents a XING user account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class User {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

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
    private final PhotoUrls photoUrls;
    private final Date birthDate;
    private final JSONObject json;

    /**
     * Initializes a new {@link User}.
     *
     * @param accountInfo The JSON account information
     * @throws XingException If parsing account information fails
     */
    public User(final JSONObject accountInfo) throws XingException {
        super();
        this.json = accountInfo;
        try {
            this.id = accountInfo.optString("id", null);
            this.firstName = accountInfo.optString("first_name", null);
            this.lastName = accountInfo.optString("last_name", null);
            this.displayName = accountInfo.optString("display_name", null);
            this.pageName = accountInfo.optString("page_name", null);
            this.permalink = accountInfo.optString("permalink", null);
            this.gender = accountInfo.optString("gender", null);
            {
                String email = accountInfo.optString("active_email", null);
                if (null == email) {
                    email = accountInfo.optString("email", null);
                }
                this.activeMail = email;
            }
            if (accountInfo.hasAndNotNull("time_zone")) {
                final JSONObject tz = accountInfo.optJSONObject("time_zone");
                this.timeZone = TimeZone.getTimeZone(tz.getString("name"));
            } else {
                this.timeZone = null;
            }
            if (accountInfo.hasAndNotNull("birth_date")) {
                final JSONObject jo = accountInfo.getJSONObject("birth_date");
                final int year = jo.optInt("year", -1);
                final int month = jo.optInt("month", -1);
                final int day = jo.optInt("day", -1);
                if (year > 0 && month > 0 && day > 0) {
                    final Calendar cal = GregorianCalendar.getInstance(UTC);
                    cal.set(year, month - 1, day, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    this.birthDate = cal.getTime();
                } else {
                    this.birthDate = null;
                }
            } else {
                this.birthDate = null;
            }
            if (accountInfo.hasAndNotNull("premium_services")) {
                final JSONArray ps = accountInfo.optJSONArray("premium_services");
                final int length = ps.length();
                premiumServices = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    premiumServices.add(ps.getString(i));
                }
            } else {
                premiumServices = Collections.emptyList();
            }
            if (accountInfo.hasAndNotNull("badges")) {
                final JSONArray b = accountInfo.optJSONArray("badges");
                final int length = b.length();
                badges = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    badges.add(b.getString(i));
                }
            } else {
                badges = Collections.emptyList();
            }
            this.wants = accountInfo.optString("wants", null);
            this.haves = accountInfo.optString("haves", null);
            this.interests = accountInfo.optString("interests", null);
            this.organisationMember = accountInfo.optString("organisation_member", null);
            if (accountInfo.hasAndNotNull("languages")) {
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
            if (accountInfo.hasAndNotNull("private_address")) {
                this.privateAddress = new Address(accountInfo.optJSONObject("private_address"));
            } else {
                this.privateAddress = null;
            }
            if (accountInfo.hasAndNotNull("business_address")) {
                this.businessAddress = new Address(accountInfo.optJSONObject("business_address"));
            } else {
                this.businessAddress = null;
            }
            if (accountInfo.hasAndNotNull("web_profiles")) {
                final JSONObject jo = accountInfo.optJSONObject("web_profiles");
                this.webProfiles = new LinkedHashMap<String, List<String>>(jo.length());
                for (final String key : jo.keySet()) {
                    final Object value = jo.opt(key);
                    if (value instanceof JSONArray) {
                        final JSONArray ja = (JSONArray) value;
                        final int length = ja.length();
                        final List<String> addrs = new ArrayList<String>(length);
                        for (int i = 0; i < length; i++) {
                            addrs.add(ja.getString(i));
                        }
                        webProfiles.put(key, addrs);
                    } else {
                        webProfiles.put(key, Collections.singletonList(value.toString()));
                    }
                }
            } else {
                webProfiles = Collections.emptyMap();
            }
            if (accountInfo.hasAndNotNull("instant_messaging_accounts")) {
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
            if (accountInfo.hasAndNotNull("professional_experience")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) coerceToNative(accountInfo.optJSONObject("professional_experience"));
                this.professionalExperience = map;
            } else {
                this.professionalExperience = Collections.emptyMap();
            }
            if (accountInfo.hasAndNotNull("educational_background")) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) coerceToNative(accountInfo.optJSONObject("educational_background"));
                this.educationalBackground = map;
            } else {
                this.educationalBackground = Collections.emptyMap();
            }
            if (accountInfo.hasAndNotNull("photo_urls")) {
                this.photoUrls = new PhotoUrls(accountInfo.getJSONObject("photo_urls"));
            } else {
                this.photoUrls = new PhotoUrls();
            }
        } catch (JSONException e) {
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
     * Gets the birth date.
     *
     * @return The birth date
     */
    public Date getBirthDate() {
        return birthDate;
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
    public PhotoUrls getPhotoUrls() {
        return photoUrls;
    }

    /**
     * Returns the original JSON object of this user.
     */
    public JSONObject toJSON() {
        return json;
    }

    @Override
    public String toString() {
        return json.toString();
    }

}
