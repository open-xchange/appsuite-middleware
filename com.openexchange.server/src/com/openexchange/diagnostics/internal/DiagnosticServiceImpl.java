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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.diagnostics.internal;

import java.nio.charset.Charset;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import javax.net.ssl.SSLSocketFactory;
import com.openexchange.diagnostics.DiagnosticService;
import com.openexchange.version.Version;

/**
 * {@link DiagnosticServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DiagnosticServiceImpl implements DiagnosticService {

    private final BiConsumer<Map.Entry<String, Charset>, StringBuilder> charsetAliasConsumer = (charset, charsetBuilder) -> getCharsetAlias(charset, charsetBuilder);
    private final BiConsumer<Map.Entry<String, Charset>, StringBuilder> charsetConsumer = (charset, charsetBuilder) -> {};

    /**
     * Initialises a new {@link DiagnosticServiceImpl}.
     */
    public DiagnosticServiceImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.diagnostics.DiagnosticService#getCharsets()
     */
    @Override
    public List<String> getCharsets(boolean aliases) {
        SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
        Set<String> charsets = new HashSet<>();

        BiConsumer<Map.Entry<String, Charset>, StringBuilder> consumer = aliases ? charsetAliasConsumer : charsetConsumer;
        StringBuilder charsetBuilder = new StringBuilder(256);
        for (Map.Entry<String, Charset> charset : availableCharsets.entrySet()) {
            charsetBuilder.append(charset.getKey());
            consumer.accept(charset, charsetBuilder);
            charsets.add(charsetBuilder.toString());
            charsetBuilder.setLength(0);
        }

        return convertToSortedList(charsets, availableCharsets.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.diagnostics.DiagnosticService#getProtocols()
     */
    @Override
    public List<String> getProtocols() {
        List<String> protocols = new LinkedList<String>();
        for (Provider provider : Security.getProviders()) {
            parseProvider(protocols, provider);
        }
        Collections.sort(protocols);
        return Collections.unmodifiableList(protocols);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.diagnostics.DiagnosticService#getCipherSuites()
     */
    @Override
    public List<String> getCipherSuites() {
        String[] defaultCipherSuites = ((SSLSocketFactory) SSLSocketFactory.getDefault()).getDefaultCipherSuites();
        final List<String> cipherSuites = new CopyOnWriteArrayList<String>();
        for (String cipherSuite : defaultCipherSuites) {
            cipherSuites.add(cipherSuite);
        }
        return Collections.unmodifiableList(cipherSuites);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.diagnostics.DiagnosticService#getVersion()
     */
    @Override
    public String getVersion() {
        return Version.getInstance().getVersionString();
    }

    /////////////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Appends to the specified {@link StringBuilder} all aliases of the specified {@link Charset} (if any)
     * 
     * @param charset The {@link Charset}
     * @param charsetBuilder The {@link StringBuilder} containing the string representation of the current {@link Charset} entry
     */
    private void getCharsetAlias(Map.Entry<String, Charset> charset, StringBuilder charsetBuilder) {
        Charset cs = charset.getValue();
        if (cs == null) {
            return;
        }
        Set<String> aliases = cs.aliases();
        if (null == aliases || aliases.isEmpty()) {
            return;
        }
        for (String alias : aliases) {
            charsetBuilder.append(", ").append(alias);
        }
    }

    /**
     * Converts the specified {@link Set} to a sorted unmodifiable {@link List}
     * 
     * @param strings The {@link Set} with the strings to convert
     * @param size The size of the new {@link List}
     * @return A sorted and unmodifiable {@link List} with the contents of the specified {@link Set}
     */
    private List<String> convertToSortedList(Set<String> strings, int size) {
        List<String> charsetWithAliases = new ArrayList<>(size);
        for (String charset : strings) {
            charsetWithAliases.add(charset);
        }

        Collections.sort(charsetWithAliases);
        return Collections.unmodifiableList(charsetWithAliases);
    }

    /**
     * Parses the SSL protocols of the specified {@link Provider} and adds them to the specified {@link List}
     * 
     * @param protocols The {@link List} with all parsed protocols
     * @param provider The {@link Provider} for which to parse the SSL protocols
     */
    private void parseProvider(List<String> protocols, Provider provider) {
        for (Object prop : provider.keySet()) {
            if (!(prop instanceof String)) {
                continue;
            }
            String key = (String) prop;
            if (key.startsWith("SSLContext.") && !key.equals("SSLContext.Default") && key.matches(".*[0-9].*")) {
                protocols.add(key.substring("SSLContext.".length()));
            } else if (key.startsWith("Alg.Alias.SSLContext.") && key.matches(".*[0-9].*")) {
                protocols.add(key.substring("Alg.Alias.SSLContext.".length()));
            }
        }
    }
}
