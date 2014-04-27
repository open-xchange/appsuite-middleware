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

package com.openexchange.rest.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.openexchange.annotation.NonNull;

/**
 * An {@link OXRESTRoute} consists of an HTTP method name (e.g. GET, POST, PUT, DELETE, etc. see the package
 * <code>com.openexchange.rest.services.annotation</code>) and a path declaration with variables led by a colon.
 * <p>
 * e.g. <code>/resources/:myResourceId</code><br>
 * which would match the path /resources/12 keeping the 12 as the myResourceId variable.
 * <p>
 * The route is yielded from an annotated method declaration inside an {@link OXRESTService} sub-class.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXRESTRoute {

    private String method;
    private String path;
    private Pattern pattern;
    private final List<String> variableNames;

    /**
     * Initializes a new {@link OXRESTRoute}.
     *
     * @param method The HTTP method (e.g. GET, POST, PUT, DELETE, etc. see the package
     *            <code>com.openexchange.rest.services.annotation</code>)
     * @param path The path; e.g. <code>"/resources/:myResourceId"</code>
     */
    public OXRESTRoute(String method, String path) {
        super();
        variableNames = new ArrayList<String>(5);
        this.method = method.toUpperCase();
        setPath(path);
    }

    /**
     * Gets the associated HTTP method name (e.g. GET, POST, PUT, DELETE, etc. see the package
     * <code>com.openexchange.rest.services.annotation</code>).
     *
     * @return The HTTP method name
     */
    public @NonNull String getMethod() {
        return method;
    }

    /**
     * Sets the associated HTTP method name (e.g. GET, POST, PUT, DELETE, etc. see the package
     * <code>com.openexchange.rest.services.annotation</code>).
     *
     * @param method The HTTP method name
     */
    public void setMethod(@NonNull String method) {
        this.method = method.toUpperCase();
    }

    /**
     * Gets the path declaration
     *
     * @return The path declaration.
     */
    public @NonNull String getPath() {
        return path;
    }

    /**
     * Sets the path declaration (should start with a slash <code>"/"</code>).
     *
     * @param path The path declaration
     * @throws IllegalArgumentException If given path is invalid
     */
    public void setPath(@NonNull String path) {
        final String pazz = path.startsWith("/") ? path : "/" + path;
        this.path = pazz;

        // Build a pattern from path
        final StringBuilder regexBuilder = new StringBuilder("^");
        boolean captureName = false;
        boolean quote = false;
        final StringBuilder nameBuilder = new StringBuilder();
        final StringBuilder quoteBuilder = new StringBuilder();

        pattern = null;
        variableNames.clear();

        final int length = pazz.length();
        for (int i = 0; i < length; i++) {
            final char c = pazz.charAt(i);
            if (captureName) {
                if (c == '/') {
                    captureName = false;
                    regexBuilder.append("([^/]*)/");
                    variableNames.add(nameBuilder.toString());
                    nameBuilder.setLength(0);
                } else {
                    nameBuilder.append(c);
                }
            } else {
                if (c == ':') {
                    captureName = true;
                    if (quote) {
                        regexBuilder.append(Pattern.quote(quoteBuilder.toString()));
                        quote = false;
                        quoteBuilder.setLength(0);
                    }
                } else {
                    quote = true;
                    quoteBuilder.append(c);
                }
            }
        }

        if (quote) {
            regexBuilder.append(Pattern.quote(quoteBuilder.toString()));
        }

        if (captureName) {
            regexBuilder.append("([^/]*)$");
            variableNames.add(nameBuilder.toString());
        }

        try {
            pattern = Pattern.compile(regexBuilder.toString());
        } catch (final PatternSyntaxException e) {
            throw new IllegalArgumentException("Specified path is invalid", e);
        }
    }

    /**
     * Tries to match the given path to this route.
     *
     * @param method The method associated with given path
     * @param path The path to check on
     * @return <code>null</code> if the path does not match, or a {@link OXRESTMatch} instance containing the variables of the match.
     */
    public OXRESTMatch match(String method, String path) {
        if (!method.equalsIgnoreCase(this.method)) {
            return null;
        }

        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            OXRESTMatch match = new OXRESTMatch();
            match.setRoute(this);
            for (int i = 0, size = variableNames.size(); i < size; i++) {
                match.getParameters().put(variableNames.get(i), matcher.group(i + 1));
            }
            match.setParameterNames(new ArrayList<String>(variableNames));
            return match;
        }

        return null;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(256);
        if (method != null) {
            builder.append(method).append(" ");
        }
        if (path != null) {
            builder.append(path);
        }
        return builder.toString();
    }

}
