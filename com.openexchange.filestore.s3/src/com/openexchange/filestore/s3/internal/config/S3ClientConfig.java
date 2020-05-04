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

package com.openexchange.filestore.s3.internal.config;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import com.amazonaws.services.s3.internal.BucketNameUtils;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link S3ClientConfig}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class S3ClientConfig {

    private final String filestoreID;
    private final BucketAndClientInfo bucketAndClientInfo;
    private final LeanConfigurationService configService;

    /**
     * Initializes a new {@link S3ClientConfig}.
     *
     * @param filestoreID The filestore id
     * @param bucketAndClientInfo The {@link BucketAndClientInfo}
     * @param configService The {@link LeanConfigurationService}
     */
    private S3ClientConfig(String filestoreID, BucketAndClientInfo bucketAndClientInfo, LeanConfigurationService configService) {
        super();
        this.filestoreID = filestoreID;
        this.bucketAndClientInfo = bucketAndClientInfo;
        this.configService = configService;
    }

    /**
     * Initializes a new {@link S3ClientConfig}
     *
     * @param filestoreID The filestore id
     * @param configService The {@link LeanConfigurationService}
     * @return The {@link S3ClientConfig}
     * @throws OXException
     */
    public static S3ClientConfig init(String filestoreID, LeanConfigurationService configService) throws OXException {
        return new S3ClientConfig(filestoreID, getBucketAndClientInfo(filestoreID, configService), configService);
    }

    /**
     * Gets the bucket name
     *
     * @return The s3 bucket name
     */
    public String getBucketName() {
        return bucketAndClientInfo.getBucketName();
    }

    /**
     * Gets the {@link S3ClientScope}
     *
     * @return The {@link S3ClientScope}
     */
    public S3ClientScope getClientScope() {
        if (bucketAndClientInfo.getClientID().isPresent()) {
            return S3ClientScope.SHARED;
        }

        return S3ClientScope.DEDICATED;
    }

    /**
     * Gets the client ID if this is a shared client (i.e. {@link #getClientScope()} {@code == S3ClientScope.SHARED}).
     *
     * @return An optional containing the client ID or empty if this config denotes a per-filestore client.
     */
    public Optional<String> getClientID() {
        return bucketAndClientInfo.getClientID();
    }

    /**
     * Gets the filestore id
     *
     * @return The filestore id
     */
    public String getFilestoreID() {
        return filestoreID;
    }

    /**
     * Gets the property name and value for the current client scope.
     *
     * @param property The {@link S3ClientProperty} to get
     * @return The {@link ConfigProperty}
     */
    public ConfigProperty getProperty(S3ClientProperty property) {
        Object defaultValueObj = property.getDefaultValue();
        String defaultValue = defaultValueObj == null ? null : defaultValueObj.toString();
        Map<String, String> clientQualifier = getClientQualifier(getClientID());
        if (getClientScope().isShared()) {
            return new ConfigProperty(property.getFQPropertyName(clientQualifier), configService.getProperty(property, clientQualifier));
        }

        Optional<S3Property> filestoreProperty = S3Property.of(property);
        if (filestoreProperty.isPresent()) {
            Map<String, String> filestoreQualifier = getFilestoreQualifier(filestoreID);
            return new ConfigProperty(filestoreProperty.get().getFQPropertyName(filestoreQualifier), configService.getProperty(filestoreProperty.get(), filestoreQualifier));
        }

        return new ConfigProperty(property.getFQPropertyName(clientQualifier), defaultValue);
    }

    /**
     * Gets the property value for the most specific scope it is defined in.
     * If it is not defined and the default value is {@code null} or the empty string or
     * if it is defined but its value is {@code null} or the empty string, an exception
     * is thrown.
     *
     * @param property The {@link S3ClientProperty} to get
     * @return The value
     * @throws OXException {@link ConfigurationExceptionCodes#INVALID_CONFIGURATION}
     */
    public ConfigProperty getPropertySafe(S3ClientProperty property) throws OXException {
        ConfigProperty configProperty = getProperty(property);
        if (Strings.isEmpty(configProperty.getValue())) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(getExpectedFQN(property));
        }

        return configProperty;
    }

    /**
     * Gets the value of the given {@link S3ClientProperty}
     *
     * @param property The property to get the value for
     * @return The value of the given property
     */
    public String getValue(S3ClientProperty property) {
        return getProperty(property).getValue();
    }

    /**
     * Gets the expected full-qualified property name based on current client scope.
     *
     * @param property The {@link S3ClientConfig}
     * @return The full-qualified property name based on current client scope.
     */
    public String getExpectedFQN(S3ClientProperty property) {
        if (getClientScope().isShared()) {
            return property.getFQPropertyName(getClientQualifier(getClientID()));
        }

        Optional<S3Property> filestoreProperty = S3Property.of(property);
        if (filestoreProperty.isPresent()) {
            return filestoreProperty.get().getFQPropertyName(getFilestoreQualifier(filestoreID));
        }
        return property.getFQPropertyName();
    }

    /**
     * Gets the non-{@code null} and non-empty property string value
     *
     * @param property The {@link S3ClientProperty} to retrieve
     * @return The property value
     * @throws OXException {@link ConfigurationExceptionCodes#PROPERTY_MISSING) if property value is {@code null} or empty string
     */
    public String getValueSafe(S3ClientProperty property) throws OXException {
        String value = getValue(property);
        if (Strings.isEmpty(value)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create(getExpectedFQN(property));
        }
        return value;
    }

    /**
     * Gets a fingerprint in form of a Java hash code of the client configuration based on the most recent values.
     * This can be used to quickly check for configuration changes.
     *
     * @return The fingerprint
     */
    public int getFingerprint() {
        S3ClientScope clientScope = getClientScope();
        String qualifier = getClientID().orElse(getFilestoreID());
        return getFingerprint(configService, clientScope, qualifier);
    }

    /**
     * Gets the number of currently configured clients.
     */
    public int getNumberOfConfiguredClients() {
        Map<String, String> s3ClientProperties = configService.getProperties((k, v) -> k.startsWith(S3ClientProperty.PREFIX));
        return (int) s3ClientProperties.entrySet()
                                 .stream()
                                 .filter(e -> e.getKey().endsWith('.' + S3ClientProperty.BUCKETS.getShortName()) && Strings.isNotEmpty(e.getValue()))
                                 .count();
    }

    /**
     * Gets the max. number of clients that might be configured to have per-client monitoring enabled.
     */
    public int getMaxNumberOfMonitoredClients() {
        return configService.getIntProperty(S3Property.MAX_NUMBER_OF_MONITORED_CLIENTS);
    }

    /**
     * Gets whether metric collection is enabled.
     */
    public boolean enableMetricCollection() {
        return configService.getBooleanProperty(S3Property.METRIC_COLLECTION);
    }

    /**
     * Gets a fingerprint in form of a Java hash code of the client configuration based on the most recent values.
     * This can be used to quickly check for configuration changes.
     *
     * @param configService The {@link LeanConfigurationService} instance to obtain the config values from
     * @param scope The client scope
     * @param qualifier The config qualifier; either client or filestore ID depending on the scope
     * @return The fingerprint
     */
    public static int getFingerprint(LeanConfigurationService configService, S3ClientScope scope, String qualifier) {
        Map<String, String> qualifierMap;
        if (scope == S3ClientScope.SHARED) {
            qualifierMap = getClientQualifier(Optional.of(qualifier));
        } else {
            qualifierMap = getFilestoreQualifier(qualifier);
        }

        List<Object> contributors = new LinkedList<>();
        contributors.add(configService.getBooleanProperty(S3Property.METRIC_COLLECTION));
        contributors.add(configService.getIntProperty(S3Property.MAX_NUMBER_OF_MONITORED_CLIENTS));
        for (S3ClientProperty property : S3ClientProperty.values()) {
            if (scope == S3ClientScope.SHARED) {
                contributors.add(configService.getProperty(property, qualifierMap));
            } else {
                contributors.add(S3Property.of(property)
                    .map(p -> (Object) configService.getProperty(p, qualifierMap))
                    .orElse(property.getDefaultValue()));
            }
        }

        return Objects.hash(contributors.toArray());
    }

    /**
     * Gets the client qualifier for the given clientID
     *
     * @param clientID The optional client id
     * @return A map only containing the client qualifier
     */
    private static Map<String, String> getClientQualifier(Optional<String> clientID) {
        if (clientID.isPresent() == false) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(S3ClientProperty.QUALIFIER_CLIENT_ID, clientID.get());
    }

    /**
     * Gets the bucket and client info
     *
     * @param filestoreID The filestore id
     * @param configService The {@link LeanConfigurationService}
     * @return The {@link BucketAndClientInfo}
     * @throws OXException
     */
    private static BucketAndClientInfo getBucketAndClientInfo(String filestoreID, LeanConfigurationService configService) throws OXException {
        // look for full-qualified property 'com.openexchange.filestore.s3.[filestoreID].bucketName'
        String bucketName = configService.getProperty(S3Property.BUCKET_NAME, getFilestoreQualifier(filestoreID));

        // look for bucket name contained in 'com.openexchange.filestore.s3client.[clientID].buckets'
        Optional<String> clientID = Optional.empty();
        if (bucketName != null || BucketNameUtils.isValidV2BucketName(filestoreID)) {
            String assumedBucketName = bucketName == null ? filestoreID : bucketName;
            Map<String, String> s3ClientProperties = configService.getProperties((k, v) -> k.startsWith(S3ClientProperty.PREFIX));
            Map<String, String> clientIDsToBuckets = s3ClientProperties.entrySet().stream()
                .filter(e -> e.getKey().endsWith('.' + S3ClientProperty.BUCKETS.getShortName()) && Strings.isNotEmpty(e.getValue()))
                .collect(Collectors.toMap(
                    e -> getQualifierFromProperty(S3ClientProperty.BUCKETS, e.getKey()),
                    e -> e.getValue()));

            Optional<Entry<String, String>> optEntry = clientIDsToBuckets.entrySet().parallelStream()
                                                                                    .filter((e) -> Pattern.compile("\\s*,\\s*")
                                                                                                          .splitAsStream(e.getValue())
                                                                                                          .anyMatch((bn) -> isBucketNameMatch(bn, assumedBucketName)))
                                                                                    .findFirst();
            if(optEntry.isPresent()) {
                bucketName = assumedBucketName;
                clientID = Optional.of(optEntry.get().getKey());
            }
        }

        if (bucketName == null) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("No bucket name was found for filestore '" + filestoreID + "'");
        }

        BucketAndClientInfo info = new BucketAndClientInfo(clientID, bucketName);
        return info;
    }

    /**
     * Checks if the given bucketname matches the bucketNamePattern
     *
     * @param bucketNamePattern The configured bucket name pattern to match the name against
     * @param bucketName The bucket name to match
     * @return <code>true</code> if they match, <code>false</code> otherwise
     */
    private static boolean isBucketNameMatch(String bucketNamePattern, String bucketName) {
        try {
            return Pattern.matches(Strings.wildcardToRegex(bucketNamePattern), bucketName);
        } catch (@SuppressWarnings("unused") PatternSyntaxException e) {
            return bucketNamePattern.equals(bucketName);
        }
    }

    /**
     * Extracts the qualifier, which is either a {@code filestoreID} or a {@code clientID}, from a given
     * {@link S3Property} and its runtime key. Behavior is undefined for any config key that does represent
     * a runtime key of the given property!
     *
     * @param property The {@link S3ClientProperty}
     * @param configKey The config key
     * @return The qualifier
     */
    private static String getQualifierFromProperty(S3ClientProperty property, String configKey) {
        return configKey.substring(S3ClientProperty.PREFIX.length(), configKey.length() - property.getShortName().length() - 1);
    }

    /**
     * Creates a map containing only the given filestore id
     *
     * @param filestoreId The filestore id
     * @return A map containing the filestore id
     */
    private static Map<String, String> getFilestoreQualifier(String filestoreId) {
        return Collections.singletonMap(S3Property.OPTIONAL_NAME, filestoreId);
    }

    /**
     * {@link ConfigProperty} is a wrapper for a config key and value
     */
    public static final class ConfigProperty {

        private final String key;
        private final String value;

        /**
         * Initializes a new {@link ConfigProperty}.
         *
         * @param key
         * @param value
         */
        public ConfigProperty(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the key
         *
         * @return The key
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets the value
         *
         * @return The value
         */
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

    }

    /**
     * {@link BucketAndClientInfo}
     */
    private static final class BucketAndClientInfo {

        private final Optional<String> clientID;
        private final String bucketName;

        /**
         * Initializes a new {@link BucketAndClientInfo}.
         *
         * @param clientID The client id
         * @param bucketName The bucket name
         */
        public BucketAndClientInfo(Optional<String> clientID, String bucketName) {
            super();
            this.clientID = clientID;
            this.bucketName = bucketName;
        }


        /**
         * Gets the clientID
         *
         * @return The clientID
         */
        public Optional<String> getClientID() {
            return clientID;
        }


        /**
         * Gets the bucketName
         *
         * @return The bucketName
         */
        public String getBucketName() {
            return bucketName;
        }

    }

}
