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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.ajax.chronos.schedjoules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import com.openexchange.ajax.chronos.AbstractExternalProviderChronosTest;
import com.openexchange.configuration.asset.Asset;
import com.openexchange.configuration.asset.AssetType;
import com.openexchange.testing.httpclient.models.BrowseResponse;
import com.openexchange.testing.httpclient.models.CountriesResponse;
import com.openexchange.testing.httpclient.models.CountryData;
import com.openexchange.testing.httpclient.models.ItemsData;
import com.openexchange.testing.httpclient.models.ItemsDataInner;
import com.openexchange.testing.httpclient.models.LanguageData;
import com.openexchange.testing.httpclient.models.LanguagesResponse;
import com.openexchange.testing.httpclient.models.PageData;
import com.openexchange.testing.httpclient.models.PageSectionsData;
import com.openexchange.testing.httpclient.models.SearchData;
import com.openexchange.testing.httpclient.models.SearchResponse;

/**
 * {@link BasicSchedJoulesAPITest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicSchedJoulesAPITest extends AbstractExternalProviderChronosTest {

    private static final Map<String, String> RESPONSE_HEADERS = Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "application/json");

    /**
     * Initialises a new {@link BasicSchedJoulesAPITest}.
     */
    public BasicSchedJoulesAPITest() {
        super("schedjoules");
    }

    /**
     * Tests the available languages
     */
    @Test
    public void testGetLanguages() throws Exception {
        Asset asset = assetManager.getAsset(AssetType.json, "schedjoulesLanguagesResponse.json");
        mock("http://example.com/languages?", assetManager.readAssetString(asset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        LanguagesResponse response = chronosApi.languages(defaultUserApi.getSession());
        List<LanguageData> languages = response.getData();
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
        assertNotNull("The languages list is null", languages);
        assertFalse("The languages list is empty", languages.isEmpty());
    }

    /**
     * Tests the available countries with default locale
     */
    @Test
    public void testGetCountriesWithDefaultLocale() throws Exception {
        Asset asset = assetManager.getAsset(AssetType.json, "schedjoulesCountriesResponse.json");
        mock("http://example.com/countries?", assetManager.readAssetString(asset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        CountriesResponse response = chronosApi.countries(defaultUserApi.getSession(), null);
        List<CountryData> countries = response.getData();
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
        assertNotNull("The countries list is null", countries);
        assertFalse("The countries list is empty", countries.isEmpty());
    }

    /**
     * Tests the available countries with random locale
     */
    @Test
    public void testGetCountriesWithRandomLocale() throws Exception {
        String randomLanguage = getRandomLanguage();

        Asset countriesAsset = assetManager.getAsset(AssetType.json, "schedjoulesCountriesResponse.json");
        mock("http://example.com/countries?locale=" + randomLanguage, assetManager.readAssetString(countriesAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        CountriesResponse response = chronosApi.countries(defaultUserApi.getSession(), randomLanguage);
        List<CountryData> countries = response.getData();
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
        assertNotNull("The countries list is null", countries);
        assertFalse("The countries list is empty", countries.isEmpty());
    }

    /**
     * Tests getting a page that does not exist
     */
    @Test
    public void testGetNonExistentPage() throws Exception {
        Asset asset = assetManager.getAsset(AssetType.json, "schedjoulesPageNotFoundResponse.json");
        mock("http://example.com/pages/1138", assetManager.readAssetString(asset), HttpStatus.SC_NOT_FOUND, RESPONSE_HEADERS);

        BrowseResponse response = chronosApi.browse(defaultUserApi.getSession(), 1138, null, null);
        assertNotNull("There was no error returned", response.getError());
        assertEquals("The exception code does not match", "SCHEDJOULES-API-0012", response.getCode());
    }

    /**
     * Tests browsing the root page of a random chosen country in a random chosen language
     */
    @Test
    public void testBrowseRootOfRandomCountry() throws Exception {
        String randomLanguage = getRandomLanguage();
        String randomCountry = getRandomCountry();
        Asset rootPageAsset = assetManager.getAsset(AssetType.json, "schedjoulesRootPageResponse.json");
        mock("http://example.com/pages?locale=" + randomLanguage + "&location" + randomCountry, assetManager.readAssetString(rootPageAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        BrowseResponse response = chronosApi.browse(defaultUserApi.getSession(), null, randomLanguage, randomCountry);
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
    }

    /**
     * Tests browsing a page
     */
    @Test
    public void testBrowsePage() throws Exception {
        String randomLanguage = getRandomLanguage();
        String randomCountry = getRandomCountry();
        Asset rootPageAsset = assetManager.getAsset(AssetType.json, "schedjoulesRootPageResponse.json");
        mock("http://example.com/pages?locale=" + randomLanguage + "&location" + randomCountry, assetManager.readAssetString(rootPageAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        BrowseResponse response = chronosApi.browse(defaultUserApi.getSession(), null, randomLanguage, randomCountry);
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());

        PageData pageData = response.getData();
        assertNotNull("No page data was returned", pageData);
        assertFalse("The page sections are empty", pageData.getPageSections().isEmpty());

        List<PageSectionsData> pageSections = pageData.getPageSections();
        assertNotNull("The " + pageData.getItemId() + " page's sections is null", pageSections);
        assertFalse("The root page has no page sections", pageSections.isEmpty());

        PageSectionsData pageSectionsData = pageSections.get(0);
        assertNotNull("The page section is null", pageSectionsData);

        ItemsData itemsData = pageSectionsData.getItems();
        ItemsDataInner itemData = itemsData.get(0);

        Asset pageAsset = assetManager.getAsset(AssetType.json, "schedjoulesPageResponse.json");
        mock("http://example.com/pages/" + itemData.getItem().getItemId() + "?locale=" + randomLanguage, assetManager.readAssetString(pageAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        response = chronosApi.browse(defaultUserApi.getSession(), itemData.getItem().getItemId(), getRandomLanguage(), null);
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
    }

    /**
     * Tests the search
     */
    @Test
    public void testSearch() throws Exception {
        String language = getRandomLanguage();
        String query = "Star Wars";
        int numberOfResults = 5;
        Asset pageAsset = assetManager.getAsset(AssetType.json, "schedjoulesSearchResponse.json");
        mock("http://example.com/pages/search?q=" + URLEncoder.encode(query, "UTF-8") + "&nr_results=" + numberOfResults + "&locale=" + language, assetManager.readAssetString(pageAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        SearchResponse response = chronosApi.search(defaultUserApi.getSession(), query, language, numberOfResults);
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());

        SearchData searchData = response.getData();
        assertEquals("The name does not match the query", query, searchData.getName());
        assertNotNull("There were no page sections returned", searchData.getPageSections());
    }

    ///////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Get a random language from the available languages
     * 
     * @return The ISO 6391 code of the selected language
     * @throws Exception
     */
    private String getRandomLanguage() throws Exception {
        Asset asset = assetManager.getAsset(AssetType.json, "schedjoulesLanguagesResponse.json");
        mock("http://example.com/languages?", assetManager.readAssetString(asset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        LanguagesResponse response = chronosApi.languages(defaultUserApi.getSession());
        List<LanguageData> languages = response.getData();
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
        assertNotNull("The languages list is null", languages);
        assertFalse("The languages list is empty", languages.isEmpty());

        int r = (int) (Math.random() * (languages.size() - 1));
        LanguageData languageData = languages.get(r);
        return languageData.getIso6391();
    }

    /**
     * Get a random country from the available countries
     * 
     * @returnThe ISO 3166 country code
     * @throws Exception
     */
    private String getRandomCountry() throws Exception {
        Asset countriesAsset = assetManager.getAsset(AssetType.json, "schedjoulesCountriesResponse.json");
        mock("http://example.com/countries?", assetManager.readAssetString(countriesAsset), HttpStatus.SC_OK, RESPONSE_HEADERS);

        CountriesResponse response = chronosApi.countries(defaultUserApi.getSession(), null);
        List<CountryData> countries = response.getData();
        assertNull("Errors detected", response.getError());
        assertTrue("Exception was thrown on server side", response.getErrorStack().isEmpty());
        assertNotNull("The countries list is null", countries);
        assertFalse("The countries list is empty", countries.isEmpty());

        int r = (int) (Math.random() * (countries.size() - 1));
        CountryData countryData = countries.get(r);
        return countryData.getIso3166();
    }
}
