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

package com.openexchange.jsieve.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Capabilities} - The capabilities provided on connect to sieve server.
 *
 * <pre>
 * &quot;IMPLEMENTATION&quot; &quot;Sieve server name and version&quot;
 * &quot;SASL&quot; &quot;PLAIN&quot;
 * &quot;SIEVE&quot; &quot;fileinto reject envelope vacation imapflags notify subaddress relational comparator-i;ascii-numeric regex&quot;
 * &quot;STARTTLS&quot;
 * OK
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Capabilities {

    private Boolean starttls = Boolean.FALSE;

    private String implementation = null;

    private ArrayList<String> sieve = null;

    private ArrayList<String> sasl = null;

    private final Map<String, Object> extendedProperties = new HashMap<>();

    public List<String> getSasl() {
        return sasl;
    }

    public void setSasl(final ArrayList<String> sasl) {
        this.sasl = sasl;
    }

    public void addSasl(final String saslcapa) {
        if (null != sasl) {
            sasl.add(saslcapa);
        } else {
            sasl = new ArrayList<String>();
            sasl.add(saslcapa);
        }
    }

    public ArrayList<String> getSieve() {
        return sieve;
    }

    public void setSieve(final ArrayList<String> sieve) {
        this.sieve = sieve;
    }

    public void addSieve(final String sievecapa) {
        if (null != sieve) {
            sieve.add(sievecapa);
        } else {
            sieve = new ArrayList<String>();
            sieve.add(sievecapa);
        }
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(final String implementation) {
        this.implementation = implementation;
    }

    public Boolean getStarttls() {
        return starttls;
    }

    public void setStarttls(final Boolean starttls) {
        this.starttls = starttls;
    }

    public void addExtendedProperty(String key, Object value){
        extendedProperties.put(key, value);
    }

    public Map<String, Object> getExtendedProperties(){
        return extendedProperties;
    }
}
