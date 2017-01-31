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

/**
 * {@link Certificate}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Certificate {

    private final String fingerprint;
    private long issuedOnTimestamp;
    private long expirationTimestamp;
    private boolean trusted;
    private boolean expired;
    private String commonName;
    private String issuer;
    private String signature;
    private String serialNumber;

    /**
     * Initialises a new {@link Certificate}.
     */
    public Certificate(String fingerprint) {
        super();
        this.fingerprint = fingerprint;
    }

    /**
     * Gets the fingerprint
     *
     * @return The fingerprint
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * Gets the expirationTimestamp
     *
     * @return The expirationTimestamp
     */
    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    /**
     * Sets the expirationTimestamp
     *
     * @param expirationTimestamp The expirationTimestamp to set
     */
    public void setExpirationTimestamp(long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    /**
     * Gets the trusted
     *
     * @return The trusted
     */
    public boolean isTrusted() {
        return trusted;
    }

    /**
     * Sets the trusted
     *
     * @param trusted The trusted to set
     */
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    /**
     * Gets the expired
     *
     * @return The expired
     */
    public boolean isExpired() {
        return expired;
    }

    /**
     * Sets the expired
     *
     * @param expired The expired to set
     */
    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    /**
     * Gets the issuedOnTimestamp
     *
     * @return The issuedOnTimestamp
     */
    public long getIssuedOnTimestamp() {
        return issuedOnTimestamp;
    }

    /**
     * Sets the issuedOnTimestamp
     *
     * @param issuedOnTimestamp The issuedOnTimestamp to set
     */
    public void setIssuedOnTimestamp(long issuedOnTimestamp) {
        this.issuedOnTimestamp = issuedOnTimestamp;
    }

    /**
     * Gets the commonName
     *
     * @return The commonName
     */
    public String getCommonName() {
        return commonName;
    }

    /**
     * Sets the commonName
     *
     * @param commonName The commonName to set
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Gets the issuer
     *
     * @return The issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Sets the issuer
     *
     * @param issuer The issuer to set
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Gets the signature
     *
     * @return The signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the signature
     *
     * @param signature The signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Gets the serialNumber
     *
     * @return The serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serialNumber
     *
     * @param serialNumber The serialNumber to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"issuer\":\"").append(issuer).append("\",");
        builder.append("\"commonName\":\"").append(commonName).append("\",");
        builder.append("\"fingerprint\":\"").append(fingerprint).append("\"");
        builder.append("}");
        return builder.toString();
    }
}
