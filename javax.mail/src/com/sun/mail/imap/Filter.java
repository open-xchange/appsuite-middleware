/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.mail.imap;

/**
 * {@link Filter} - Specifies the filter, which is used when issuing a <code>FILTER</code> command.
 * <p>
 * <b>Note</b>: Requires the <code>"FILTER=SIEVE"</code> capability being advertised by IMAP server
 *
 * @author Thorben Betten
 */
public class Filter {

    /** Specifies the filter specification, which is used when issuing a <code>FILTER</code> command. */
    public static enum FilterSpec {

        /**
         * The Sieve filtering normally applied at delivery is applied to the
         * matching messages.  This allows e.g. re-filtering messages that
         * were handled wrong at actual delivery.  This at least means that
         * the "active script" as configured through [MANAGESIEVE] is run for
         * the matching messages.  Some installations apply certain Sieve
         * rules in addition to (before or after) the user's active script
         * which are outside the user's control.
         */
        DELIVERY("DELIVERY"),
        /**
         * The Sieve script with the specified name that is stored in the
         * user's own personal (private) Sieve repository is applied to the
         * matching messages.  Implementations that support ManageSieve
         * [MANAGESIEVE] can use the PUTSCRIPT command to store named scripts
         * in the personal repository.  This is the same repository from
         * which the Sieve "include" control structure [SIEVE-INCLUDE]
         * retrieves ":personal" scripts.
         */
        PERSONAL("PERSONAL"),
        /**
         * The Sieve script with the specified name that is stored in the
         * global (site-wide) Sieve repository is applied to the matching
         * messages. This the same repository from which the Sieve "include"
         * control structure [SIEVE-INCLUDE] retrieves ":global" scripts.
         */
        GLOBAL("GLOBAL"),
        /**
         * The Sieve script provided in the string argument is compiled and
         * executed for all the matching messages.  This is e.g. useful to
         * test an individual Sieve rule, or apply a new rule to already
         * delivered messages.
         */
        SCRIPT("SCRIPT");

        private final String name;

        private FilterSpec(String name) {
            this.name = name;
        }

        /**
         * Gets the specification's name
         *
         * @return The name
         */
        public String getName() {
            return name;
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Filter DELIVERY_FILTER = new Filter(FilterSpec.DELIVERY, null, null);

    /**
     * Gets the {@link FilterSpec#DELIVERY DELIVERY} filter.
     *
     * @return The DELIVERY filter
     */
    public static Filter getDeliveryFilter() {
        return DELIVERY_FILTER;
    }

    /**
     * Creates the {@link FilterSpec#PERSONAL PERSONAL} filter for specified script name.
     *
     * @param scriptName The script name
     * @return The PERSONAL filter using specified script name
     */
    public static Filter getPersonalFilter(String scriptName) {
        if (null == scriptName) {
            throw new IllegalArgumentException("Script name must not be null");
        }
        return new Filter(FilterSpec.PERSONAL, scriptName, null);
    }

    /**
     * Creates the {@link FilterSpec#GLOBAL GLOBAL} filter for specified script name.
     *
     * @param scriptName The script name
     * @return The GLOBAL filter using specified script name
     */
    public static Filter getGlobalFilter(String scriptName) {
        if (null == scriptName) {
            throw new IllegalArgumentException("Script name must not be null");
        }
        return new Filter(FilterSpec.GLOBAL, scriptName, null);
    }

    /**
     * Creates the {@link FilterSpec#SCRIPT SCRIPT} filter for specified script.
     *
     * @param script The script
     * @return The SCRIPT filter using specified script
     */
    public static Filter getScriptFilter(String script) {
        if (null == script) {
            throw new IllegalArgumentException("Script must not be null");
        }
        return new Filter(FilterSpec.SCRIPT, null, script);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final FilterSpec spec;
    private final String scriptName;
    private final String script;

    /**
     * Initializes a new {@link Filter}.
     */
    private Filter(FilterSpec spec, String scriptName, String script) {
        super();
        this.spec = spec;
        this.scriptName = scriptName;
        this.script = script;
    }

    /**
     * Gets the specification.
     *
     * @return The specification
     */
    public FilterSpec getSpec() {
        return spec;
    }

    /**
     * Gets the SIEVE script name.
     * <p>
     * Only available for {@link FilterSpec#PERSONAL PERSONAL} and {@link FilterSpec#GLOBAL GLOBAL} specifications.
     *
     * @return The SIEVE script name or <code>null</code>
     * @see #getPersonalFilter(String)
     * @see #getGlobalFilter(String)
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Gets the SIEVE script.
     * <p>
     * Only available for {@link FilterSpec#SCRIPT SCRIPT} specification.
     *
     * @return The SIEVE script or <code>null</code>
     * @see #getScriptFilter(String)
     */
    public String getScript() {
        return script;
    }

}
