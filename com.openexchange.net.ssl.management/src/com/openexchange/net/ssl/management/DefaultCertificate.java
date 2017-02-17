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

package com.openexchange.net.ssl.management;

import com.openexchange.java.Strings;

/**
 * {@link DefaultCertificate} - The default certificate implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.4
 */
public class DefaultCertificate implements Certificate {

    /**
     * Creates a new empty builder instance
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder instance pre-filled with arguments from given certificate.
     *
     * @param source The certificate to copy from
     * @return The new builder instance
     */
    public static Builder builder(Certificate source) {
        Builder b = new Builder();

        String str = source.getFingerprint();
        if (false == Strings.isEmpty(str)) {
            b.fingerprint(str);
        }

        str = source.getCommonName();
        if (false == Strings.isEmpty(str)) {
            b.commonName(str);
        }

        b.expirationTimestamp(source.getExpirationTimestamp());

        str = source.getFailureReason();
        if (false == Strings.isEmpty(str)) {
            b.failureReason(str);
        }

        str = source.getHostName();
        if (false == Strings.isEmpty(str)) {
            b.hostName(str);
        }

        b.issuedOnTimestamp(source.getIssuedOnTimestamp());

        str = source.getIssuer();
        if (false == Strings.isEmpty(str)) {
            b.issuer(str);
        }

        str = source.getSerialNumber();
        if (false == Strings.isEmpty(str)) {
            b.serialNumber(str);
        }

        str = source.getSignature();
        if (false == Strings.isEmpty(str)) {
            b.signature(str);
        }

        return b;
    }

    /** The builder for a <code>DefaultCertificate</code> instance */
    public static class Builder {

        private String fingerprint;
        private long issuedOnTimestamp;
        private long expirationTimestamp;
        private boolean trusted;
        private boolean expired;
        private String issuer;
        private String signature;
        private String serialNumber;
        private String failureReason;
        private String hostName;
        private String commonName;

        Builder() {
            super();
        }

        /**
         * Sets the fingerprint
         *
         * @param fingerprint The fingerprint to set
         * @return This builder
         */
        public Builder fingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        /**
         * Sets the issued-on time stamp
         *
         * @param issuedOnTimestamp The issued-on time stamp to set
         * @return This builder
         */
        public Builder issuedOnTimestamp(long issuedOnTimestamp) {
            this.issuedOnTimestamp = issuedOnTimestamp;
            return this;
        }

        /**
         * Sets the expiration time stamp
         *
         * @param expirationTimestamp The expiration time stamp to set
         * @return This builder
         */
        public Builder expirationTimestamp(long expirationTimestamp) {
            this.expirationTimestamp = expirationTimestamp;
            return this;
        }

        /**
         * Sets the trusted flag
         *
         * @param trusted The trusted flag to set
         * @return This builder
         */
        public Builder trusted(boolean trusted) {
            this.trusted = trusted;
            return this;
        }

        /**
         * Sets the expired flag
         *
         * @param expired The expired flag to set
         * @return This builder
         */
        public Builder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        /**
         * Sets the issuer
         *
         * @param issuer The issuer to set
         * @return This builder
         */
        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /**
         * Sets the signature
         *
         * @param signature The signature to set
         * @return This builder
         */
        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        /**
         * Sets the serial number
         *
         * @param serialNumber The serial number to set
         * @return This builder
         */
        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        /**
         * Sets the failure reason
         *
         * @param failureReason The failure reason to set
         * @return This builder
         */
        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        /**
         * Sets the host name
         *
         * @param hostName The host name to set
         * @return This builder
         */
        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        /**
         * Sets the common name
         *
         * @param commonName The common name to set
         * @return This builder
         */
        public Builder commonName(String commonName) {
            this.commonName = commonName;
            return this;
        }

        /**
         * Builds the <code>DefaultCertificate</code> instance from this builder's arguments.
         *
         * @return The new <code>DefaultCertificate</code> instance
         */
        public DefaultCertificate build() {
            return new DefaultCertificate(fingerprint, issuedOnTimestamp, expirationTimestamp, trusted, expired, issuer, signature, serialNumber, failureReason, hostName, commonName);
        }

    }

    // -----------------------------------------------------------------------------------------------------

    private final String fingerprint;
    private final long issuedOnTimestamp;
    private final long expirationTimestamp;
    private final boolean trusted;
    private final boolean expired;
    private final String issuer;
    private final String signature;
    private final String serialNumber;
    private final String failureReason;
    private final String hostName;
    private final String commonName;

    DefaultCertificate(String fingerprint, long issuedOnTimestamp, long expirationTimestamp, boolean trusted, boolean expired, String issuer, String signature, String serialNumber, String failureReason, String hostName, String commonName) {
        super();
        this.fingerprint = fingerprint;
        this.issuedOnTimestamp = issuedOnTimestamp;
        this.expirationTimestamp = expirationTimestamp;
        this.trusted = trusted;
        this.expired = expired;
        this.issuer = issuer;
        this.signature = signature;
        this.serialNumber = serialNumber;
        this.failureReason = failureReason;
        this.hostName = hostName;
        this.commonName = commonName;
    }

    /**
     * Gets the fingerprint
     *
     * @return The fingerprint
     */
    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * Gets the expirationTimestamp
     *
     * @return The expirationTimestamp
     */
    @Override
    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    /**
     * Gets the trusted
     *
     * @return The trusted
     */
    @Override
    public boolean isTrusted() {
        return trusted;
    }

    /**
     * Gets the expired
     *
     * @return The expired
     */
    @Override
    public boolean isExpired() {
        return expired;
    }

    /**
     * Gets the issuedOnTimestamp
     *
     * @return The issuedOnTimestamp
     */
    @Override
    public long getIssuedOnTimestamp() {
        return issuedOnTimestamp;
    }

    /**
     * Gets the issuer
     *
     * @return The issuer
     */
    @Override
    public String getIssuer() {
        return issuer;
    }

    /**
     * Gets the signature
     *
     * @return The signature
     */
    @Override
    public String getSignature() {
        return signature;
    }

    /**
     * Gets the serialNumber
     *
     * @return The serialNumber
     */
    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Gets the failureReason
     *
     * @return The failureReason
     */
    @Override
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Gets the host name
     *
     * @return The host name
     */
    @Override
    public String getHostName() {
        return hostName;
    }

    /**
     * Gets the commonName
     *
     * @return The commonName
     */
    @Override
    public String getCommonName() {
        return commonName;
    }

}
