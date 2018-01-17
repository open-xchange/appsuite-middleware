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

package com.openexchange.multifactor.provider.u2f.impl;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.AbstractMultifactorDevice;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.SignRequestData;

/**
 * {@link U2FMultifactorDevice}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */

public class U2FMultifactorDevice extends AbstractMultifactorDevice {

    public static final String REQUEST_ID_PARAMETER = "requestId";
    public static final String SIGN_REQUESTS_PARAMETER = "signRequests";
    private static final String REGISTERED_KEYS_PARAMETER = "registeredKeys";
    private static final String REGISTER_REQUESTS_PARAMETER = "registerRequests";

    public static final String REGISTRATION_DATA_PARAMETER = "registrationData";
    public static final String CLIENT_DATA_PARAMETER = "clientData";
    public static final String KEYHANDLE_DATA_PARAMETER = "keyHandle";
    public  static final String SIGNATURE_DATA_PARAMETER = "signatureData";

    private String              publicKey;
    private String              attestationCertificate;
    private long                counter;
    private boolean             compromised;
    private RegisterRequestData registerRequestData;

    /**
     *
     * Initializes a new {@link U2FMultifactorDevice}.
     *
     * @param id The ID of the device
     * @param name The name of the device
     */
    public U2FMultifactorDevice(String id, String name) {
        super(id, MultifactorU2FProvider.NAME, name);
        this.publicKey = null;
        this.counter = 0;
        this.compromised = false;
        this.attestationCertificate = null;
    }

    /**
     *
     * Initializes a new {@link U2FMultifactorDevice}.
     *
     * @param id The ID of the device
     * @param keyHandle The handle of the key
     * @param publicKey The public key
     * @param attestationCertificate The attestation cert as base64 encoded String
     * @param counter The counter
     * @param compromised Compromised state
     */
    public U2FMultifactorDevice(String id,
        String keyHandle,
        String attestationCertificate,
        String publicKey,
        long counter,
        boolean compromised) {

        super(id, MultifactorU2FProvider.NAME);
        this.publicKey = publicKey;
        this.attestationCertificate = attestationCertificate;
        this.counter = counter;
        this.compromised = compromised;
        setKeyHandle(keyHandle);
    }

    /**
     * Initializes a new {@link TotpMultifactorDevice} on base of an existing {@link MultifactorDevice}.
     *
     * @param source The {@link MultifactorDevice} to create the new device from
     */
    public U2FMultifactorDevice(MultifactorDevice source) {
        super(source.getId(),
              source.getProviderName(),
              source.getName(),
              source.getParameters());
        setBackup(source.isBackup());
    }

    /**
     * Gets the publicKey
     *
     * @return The publicKey
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Gets the attestation cert
     *
     * @return The attestation cert as base64 encoded string
     */
    public String getAttestationCertificate() {
        return this.attestationCertificate;
    }

    /**
     * Sets the attestationCertificate
     *
     * @param cert The cert as base64 encoded string
     */
    public void setAttestationCertificate(String cert) {
        this.attestationCertificate = cert;
    }

    /**
     * Sets the attestationCertificate
     *
     * @param cert The cert
     * @throws CertificateEncodingException
     */
    public void setAttestationCertificate(X509Certificate cert) throws CertificateEncodingException {
        Objects.requireNonNull(cert, "attestationCertificate must not be null");
        setAttestationCertificate(new String(Base64.getEncoder().encode(cert.getEncoded())));
    }

    /**
     * Sets the publicKey
     *
     * @param publicKey The publicKey to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Gets the counter
     *
     * @return The counter
     */
    public long getCounter() {
        return counter;
    }

    /**
     * Sets the counter
     *
     * @param counter The counter to set
     */
    public void setCounter(long counter) {
        this.counter = counter;
    }

    /**
     * Gets the compromised
     *
     * @return The compromised
     */
    public boolean isCompromised() {
        return compromised;
    }

    /**
     * Sets the compromised
     *
     * @param compromised The compromised to set
     */
    public void setCompromised(boolean compromised) {
        this.compromised = compromised;
    }

    /**
     * Gets the temporary, non persistent, registration data.
     *
     * @return  Gets the temporary, non persistent, registration data, or an empty optional if the device is not in a registration state
     */
    public Optional<RegisterRequestData> getRequestData() {
        return Optional.ofNullable(registerRequestData);
    }

    /**
     * Sets the U2F {@link RegisterRequestData} to the device and it's paramters
     *
     * @param registerRequestData The temporary registration data to set
     * @throws OXException
     */
    public U2FMultifactorDevice setRegisterRequestData(RegisterRequestData registerRequestData) throws OXException {
        try {
            //Holding the complete request data in order to be able to finish the registration process later
            this.registerRequestData = registerRequestData;

            //Putting some informations into the parameters which can be sent back to the caller
            JSONObject json = new JSONObject(registerRequestData.toJson());
            setParameter(REQUEST_ID_PARAMETER, registerRequestData.getRequestId());
            setParameter(REGISTERED_KEYS_PARAMETER, json.getJSONArray(REGISTERED_KEYS_PARAMETER));
            setParameter(REGISTER_REQUESTS_PARAMETER, json.getJSONArray(REGISTER_REQUESTS_PARAMETER));
            return this;
        } catch (JSONException e) {
            throw MultifactorExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Sets the U2F {@link SignRequestData} (challenge for the client) to the device's parameters
     *
     * @param data The data add to the devices parameters
     * @throws OXException
     * @return this
     */
    public U2FMultifactorDevice setSignRequestData(SignRequestData data) throws OXException {
        try {
            setParameter(REQUEST_ID_PARAMETER, data.getRequestId());
            setParameter(SIGN_REQUESTS_PARAMETER, new JSONObject(data.toJson()).getJSONArray(SIGN_REQUESTS_PARAMETER));
            return this;
        } catch (JSONException e) {
            throw MultifactorExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Returns the U2F registration data sent by the client
     *
     * @return The U2F registration data sent by the client
     */
    public String getRegistrationData() {
       return getParameter(REGISTRATION_DATA_PARAMETER);
    }

    /**
     * Returns the U2F client data sent by the client
     *
     * @return The U2F client data sent by the client
     */
    public String getClientData() {
       return getParameter(CLIENT_DATA_PARAMETER);
    }

    /**
     * Returns the U2F signature sent by the client
     *
     * @return The signature
     */
    public String getSignatureData() {
        return getParameter(SIGNATURE_DATA_PARAMETER);
    }

    /**
     * Return the U2F key handle sent by the client
     *
     * @return The U2F key handle sent by the client
     */
    public String getKeyHandle() {
        return getParameter(KEYHANDLE_DATA_PARAMETER);
    }

    /**
     * The key handle to set
     *
     * @param keyHandle The key handle
     */
    public void setKeyHandle(String keyHandle) {
       setParameter(KEYHANDLE_DATA_PARAMETER, keyHandle);
    }
}
