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

package com.openexchange.test.common.test;

import com.openexchange.ajax.framework.AJAXClient;

/**
 * 
 * {@link TestClassConfig}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class TestClassConfig {

    final int numberOfContexts;
    final int numberOfusersPerContext;
    final boolean createAjaxClients;
    final boolean createApiClients;
    final boolean useEnhancedApiClients;

    /**
     * Initializes a new {@link TestClassConfig}.
     *
     * @param numberOfContexts The amount of context to prepare for the test
     * @param numberOfusersPerContext The amount of users to prepare for each context
     * @param createAjaxClients <code>true</code> to generate an initialized {@link AJAXClient} for each user
     * @param createApiClients <code>true</code> to generate an initialized {@link AJAXClient} for each user
     * @param useEnhancedApiClients <code>true</code> to use com.openexchange.ajax.chronos.EnhancedApiClient
     */
    public TestClassConfig(int numberOfContexts, int numberOfusersPerContext, boolean createAjaxClients, boolean createApiClients, boolean useEnhancedApiClients) {
        super();
        this.numberOfContexts = numberOfContexts;
        this.numberOfusersPerContext = 2 > numberOfusersPerContext ? 2 : numberOfusersPerContext;
        this.createAjaxClients = createAjaxClients;
        this.createApiClients = createApiClients;
        this.useEnhancedApiClients = useEnhancedApiClients;
    }

    /**
     * The amount of context to prepare for the test
     *
     * @return the amount
     */
    public int getNumberOfContexts() {
        return numberOfContexts;
    }

    /**
     * The amount of users to prepare for each context
     *
     * @return the amount
     */
    public int getNumberOfusersPerContext() {
        return numberOfusersPerContext;
    }

    /**
     * Whether or not to create AJAX clients for each user
     *
     * @return <code>true</code> if client shall be generated
     */
    public boolean createAjaxClients() {
        return createAjaxClients;
    }

    /**
     * Whether or not to create API clients for each user
     *
     * @return <code>true</code> if client shall be generated
     */
    public boolean createApiClients() {
        return createApiClients;
    }

    /**
     * Whether or not to use the com.openexchange.ajax.chronos.EnhancedApiClient class
     * instead of the normal API client
     *
     * @return <code>true</code> if the enhanced client shalll be used
     */
    public boolean useEnhancedApiClients() {
        return useEnhancedApiClients;
    }

    /**
     * Builder for a {@link TestClassConfig}
     * 
     * @return The builder
     */
    public static TestConfigBuilder builder() {
        return new TestConfigBuilder();
    }

    /**
     *
     * {@link TestConfigBuilder}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.5
     */
    public static class TestConfigBuilder {

        private int numberOfContexts = 1;
        private int numberOfusersPerContext = 1;
        private boolean createAjaxClients = false;
        private boolean createApiClients = false;
        boolean useEnhancedApiClients = false;

        /**
         * Initializes a new {@link TestClassConfig}.
         */
        TestConfigBuilder() {}

        /**
         * The amount of context to prepare for the test
         *
         * @param x The amoutn
         * @return This builder for chaining
         */
        public TestConfigBuilder withContexts(int x) {
            numberOfContexts = x;
            return this;
        }

        /**
         * The amount of users to prepare for each context
         *
         * @param x The amount
         * @return This builder for chaining
         */
        public TestConfigBuilder withUserPerContext(int x) {
            numberOfusersPerContext = x;
            return this;
        }

        /**
         * Enables the generation of an AJAX client per user
         *
         * @return This builder for chaining
         */
        public TestConfigBuilder createAjaxClient() {
            createAjaxClients = true;
            return this;
        }

        /**
         * Enables the generation of an API client per user
         *
         * @return This builder for chaining
         */
        public TestConfigBuilder createApiClient() {
            createApiClients = true;
            return this;
        }

        /**
         * Will use the com.openexchange.ajax.chronos.EnhancedApiClient class
         * instead of the normal API class
         *
         * @return This builder for chaining
         */
        public TestConfigBuilder useEnhancedApiClients() {
            useEnhancedApiClients = true;
            return this;
        }

        /**
         * Build the configuration
         *
         * @return The {@link TestClassConfig}
         */
        public TestClassConfig build() {
            return new TestClassConfig(numberOfContexts, numberOfusersPerContext, createAjaxClients, createApiClients, useEnhancedApiClients);
        }

    }

}
