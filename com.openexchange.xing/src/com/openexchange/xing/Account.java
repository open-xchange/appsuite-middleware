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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;


/**
 * {@link Account} - Represents a Xing user's account.
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

    /**
     * Initializes a new {@link Account}.
     * 
     * @param accountInfo The JSON account information
     * @throws XingException If parsing account information fails
     */
    public Account(final JSONObject accountInfo) throws XingException {
        super();
        try {
            this.id = accountInfo.getString("id");
            this.firstName = accountInfo.getString("first_name");
            this.lastName = accountInfo.getString("last_name");
            this.displayName = accountInfo.getString("display_name");
            this.pageName = accountInfo.getString("page_name");
            this.permalink = accountInfo.getString("permalink");
            this.gender = accountInfo.getString("gender");
            
            // TODO: Parse rest
            
            /*-
             * 
             * {
    "id": "123456_abcdef",
    "first_name": "Max",
    "last_name": "Mustermann",
    "display_name": "Max Mustermann",
    "page_name": "Max_Mustermann",
    "permalink": "https://www.xing.com/profile/Max_Mustermann",
    "gender": "m",
    "birth_date": {
      "day": 12,
      "month": 8,
      "year": 1963
    },
    "active_email": "max.mustermann@xing.com",
    "time_zone": {
      "name": "Europe/Copenhagen",
      "utc_offset": 2.0
    },
    "premium_services": ["SEARCH", "PRIVATEMESSAGES"],
    "badges": ["PREMIUM", "MODERATOR"],
    "wants": "einen neuen Job",
    "haves": "viele tolle Skills",
    "interests": "Flitzebogen schießen and so on",
    "organisation_member": "ACM, GI",
    "languages": {
      "de": "NATIVE",
      "en": "FLUENT",
      "fr": null,
      "zh": "BASIC"
    },
    "private_address": {
      "city": "Hamburg",
      "country": "DE",
      "zip_code": "20357",
      "street": "Privatstraße 1",
      "phone": "49|40|1234560",
      "fax": "||",
      "province": "Hamburg",
      "email": "max@mustermann.de",
      "mobile_phone": "49|0155|1234567"
    },
    "business_address": {
      "city": "Hamburg",
      "country": "DE",
      "zip_code": "20357",
      "street": "Geschäftsstraße 1a",
      "phone": "49|40|1234569",
      "fax": "49|40|1234561",
      "province": "Hamburg",
      "email": "max.mustermann@xing.com",
      "mobile_phone": "49|160|66666661"
    },
    "web_profiles": {
      "qype": ["http://qype.de/users/foo"],
      "google_plus": ["http://plus.google.com/foo"],
      "blog": ["http://blog.example.org"],
      "homepage": ["http://example.org", "http://other-example.org"]
    },
    "instant_messaging_accounts": {
      "skype": "1122334455",
      "googletalk": "max.mustermann"
    },
    "professional_experience": {
      "primary_company": {
        "name": "XING AG",
        "title": "Softwareentwickler",
        "company_size": "201-500",
        "tag": null,
        "url": "http://www.xing.com",
        "career_level": "PROFESSIONAL_EXPERIENCED",
        "begin_date": "2010-01",
        "description": null,
        "end_date": null,
        "industry": "AEROSPACE"
      },
      "non_primary_companies": [{
        "name": "Ninja Ltd.",
        "title": "DevOps",
        "company_size": null,
        "tag": "NINJA",
        "url": "http://www.ninja-ltd.co.uk",
        "career_level": null,
        "begin_date": "2009-04",
        "description": null,
        "end_date": "2010-07",
        "industry": "ALTERNATIVE_MEDICINE"
      },
      {
        "name": null,
        "title": "Wiss. Mitarbeiter",
        "company_size": null,
        "tag": "OFFIS",
        "url": "http://www.uni.de",
        "career_level": null,
        "begin_date": "2007",
        "description": null,
        "end_date": "2008",
        "industry": "APPAREL_AND_FASHION"
      },
      {
        "name": null,
        "title": "TEST NINJA",
        "company_size": "201-500",
        "tag": "TESTCOMPANY",
        "url": null,
        "career_level": "ENTRY_LEVEL",
        "begin_date": "1998-12",
        "description": null,
        "end_date": "1999-05",
        "industry": "ARTS_AND_CRAFTS"
      }],
      "awards": [{
        "name": "Awesome Dude Of The Year",
        "date_awarded": 2007,
        "url": null
      }]
    },
    "educational_background": {
      "schools": [{
        "name": "Carl-von-Ossietzky Universtät Schellenburg",
        "degree": "MSc CE/CS",
        "notes": null,
        "subject": null,
        "begin_date": "1998-08",
        "end_date": "2005-02"
      }],
      "qualifications": ["TOEFLS", "PADI AOWD"]
    },
    "photo_urls": {
      "large": "http://www.xing.com/img/users/e/3/d/f94ef165a.123456,1.140x185.jpg",
      "mini_thumb": "http://www.xing.com/img/users/e/3/d/f94ef165a.123456,1.18x24.jpg",
      "thumb": "http://www.xing.com/img/users/e/3/d/f94ef165a.123456,1.30x40.jpg",
      "medium_thumb": "http://www.xing.com/img/users/e/3/d/f94ef165a.123456,1.57x75.jpg",
      "maxi_thumb": "http://www.xing.com/img/users/e/3/d/f94ef165a.123456,1.70x93.jpg"
    }
  }
             */
            
        } catch (JSONException e) {
            throw new XingException(e);
        }
    }

}
