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

package com.openexchange.ajax.multifactor.u2fclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateEncodingException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.multifactor.u2fclient.U2FDeviceAccess.AttestationCertificate;
import com.openexchange.ajax.multifactor.u2fclient.U2FDeviceAccess.U2FKeyPair;
import com.openexchange.ajax.multifactor.u2fclient.impl.BouncyCastleU2FClientCrypto;
import com.openexchange.ajax.multifactor.u2fclient.impl.U2FTestDeviceAccess;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseDataChallengeSignRequests;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseDataChallengeRegisterRequests;

/**
 * {@link U2FClient} - A simple U2F client implementation
 * <br>
 * <br>
 * See <a href="https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html">FIDO U2F Raw Message Formats</a>
 * for reference.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class U2FClient {

    //The version supported by the client
    public static final String U2F_VERSION = "U2F_V2";


    //A reserved byte for the signature
    //https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-response-message-success
    private static final byte REGISTRATION_SIGNED_RFU = (byte) 0x0;

    //A reserved byte for the registration response message
    //https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-response-message-success
    private static final byte REGISTRATION_RESPONSE_RFU = (byte) 0x5;

    private U2FClientCrypto crypto;
    private final U2FDeviceAccess deviceAccess;

    private enum ClientDataType{
        FINISH_ENROLLMENT,
        GET_ASSERTION
    }

    /**
     * {@link RegisterData} - The registration data sent to the server in order to finish a U2F registration process
     */
    public static class RegisterData {

        private final String clientData;
        private final String registrationData;

        /***
         * Initializes a new {@link RegisterData}.
         *
         * @param clientData The client data
         * @param registrationData The registration data
         */
        public RegisterData(String clientData, String registrationData) {
            this.clientData = clientData;
            this.registrationData = registrationData;
        }

        /**
         * Gets the client data
         *
         * @return The client data
         */
        public String getClientData() {
            return clientData;
        }

        /**
         * Gets the registration data
         *
         * @return The registration data
         */
        public String getRegistrationData() {
            return registrationData;
        }
    }

    /**
     * {@link AuthenticationData} - The authentication data sent to the server in order to finish a U2F authentication process
     */
    public static class AuthenticationData{

        private final String clientData;
        private final String keyHandle;
        private final String signatureData;

        /**
         * Initializes a new {@link AuthenticationData}.
         *
         * @param clientData The client data
         * @param keyHandle The handle of the public key
         * @param signatureData The signature
         */
        public AuthenticationData(String clientData, String keyHandle, String signatureData) {
            this.clientData = clientData;
            this.keyHandle = keyHandle;
            this.signatureData = signatureData;
        }

        /**
         * Gets the client data
         * @return The client data
         */
        public String getClientData() {
            return clientData;
        }

        /**
         * Gets the key handle of the public key
         * @return The handle
         */
        public String getKeyHandle() {
            return keyHandle;
        }

        /**
         * Gets the signature
         *
         * @return The signature
         */
        public String getSignatureData() {
            return signatureData;
        }
    }

    /**
     * Convenience method to create a {@link U2FClient} for testing purpose only(!)
     *
     * @return The U2F client for testing
     */
    public static U2FClient createTestClient() {
       return new U2FClient(new BouncyCastleU2FClientCrypto(), new U2FTestDeviceAccess());
    }

    /**
     * Initializes a new {@link U2FClient}.
     *
     * @param crypto The crypto implementation to use
     * @param deviceAccess The access to the U2F device
     */
    public U2FClient(U2FClientCrypto crypto, U2FDeviceAccess deviceAccess) {
        this.crypto = crypto;
        this.deviceAccess = deviceAccess;
    }

    /**
     * Sets the crypto implementation
     *
     * @param crypto the {@link U2FClientCrypto}
     */
    public void setCrypto(U2FClientCrypto crypto) {
        this.crypto = crypto;
    }

    /**
     * Gets the crypto implementation
     *
     * @return the {@link U2FClientCrypto}
     */
    public U2FClientCrypto getCrypto() {
        return this.crypto;
    }

    /**
     * Gets the device access
     *
     * @return The {@link U2FDeviceAccess}
     */
    public U2FDeviceAccess getDeviceAccess() {
        return this.deviceAccess;
    }

    private String createClientData(String challenge, String appid, ClientDataType type) throws JSONException {
        JSONObject clientData = new JSONObject();
        clientData.put("challenge", challenge);
        clientData.put("origin", appid);
        clientData.put("typ", type == ClientDataType.FINISH_ENROLLMENT ? "navigator.id.finishEnrollment" : "navigator.id.getAssertion");
        return clientData.toString();
    }

    private byte[] createRegisterDataToSign(byte[] applicationParameterSha256,
        byte[] challengeParameterSha256,
        byte[] keyHandle,
        byte[] userPublicKey) throws IOException, U2FClientException {

        if(applicationParameterSha256.length != 32) {
            throw new U2FClientException("The application paramter must be 32 bytes but was " + applicationParameterSha256.length);
        }

        if(challengeParameterSha256.length != 32) {
            throw new U2FClientException("The challenge paramter must be 32 bytes but was " +  challengeParameterSha256.length);
        }

        if(userPublicKey.length != 65) {
            throw new U2FClientException("User public key length must be 65 bytes but was " + userPublicKey.length);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(REGISTRATION_SIGNED_RFU);
        bos.write(applicationParameterSha256);
        bos.write(challengeParameterSha256);
        bos.write(keyHandle);
        bos.write(userPublicKey);

        return bos.toByteArray();
    }

    private byte[] createAuthenticationDataToSign(byte[] applicationParameterSha256,
        byte userPresence,
        int counter,
        byte[] challenge) throws IOException, U2FClientException {


        if(applicationParameterSha256.length != 32) {
            throw new U2FClientException("The application paramter must be 32 bytes but was " + applicationParameterSha256.length);
        }

        if(challenge.length != 32) {
            throw new U2FClientException("The application paramter must be 32 bytes but was " + applicationParameterSha256.length);
        }

        //SEE: https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-response-message-success
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(applicationParameterSha256);
        bos.write(userPresence);
        bos.write(ByteBuffer.allocate(4).putInt(counter).array()); //encode as full 32 bit integer
        bos.write(challenge);
        return bos.toByteArray();
    }

    private byte[] createRegistrationResponse(
        byte[] userPublicKey,
        byte[] keyHandle,
        byte[] attestationCertificate,
        byte[] signature) throws IOException, U2FClientException {

        if(keyHandle.length > 255) {
            throw new U2FClientException("Key handle length must not exceed 255 bytes but was " + keyHandle.length);
        }
        if(userPublicKey.length != 65) {
            throw new U2FClientException("User public key length must be 65 bytes but was " + userPublicKey.length);
        }

        //SEE: https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-response-message-success
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(REGISTRATION_RESPONSE_RFU);
        bos.write(userPublicKey);
        bos.write(keyHandle.length);
        bos.write(keyHandle);
        bos.write(attestationCertificate);
        bos.write(signature);

        return bos.toByteArray();
    }

    private byte[] createAuthenticationResponse(byte userPresence, int counter, byte[] signature) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //SEE https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#authentication-response-message-success
        bos.write(userPresence);
        bos.write(ByteBuffer.allocate(4).putInt(counter).array()); //encode as full 32 bit integer
        bos.write(signature);
        return bos.toByteArray();
    }

    /**
     * Creates the registration data necessary to process the registration process of a U2F device
     *
     * @param startRegistrationData The initial registration information received from the server
     * @return The data ready to be sent to the server in order to finish the registration process
     * @throws U2FClientException
     */
    public RegisterData createRegisterData(MultifactorStartRegistrationResponseData startRegistrationData) throws U2FClientException {

        //https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-response-message-error-test-of-user-presence-required

        MultifactorStartRegistrationResponseDataChallengeRegisterRequests registerRequest = startRegistrationData.getChallenge().getRegisterRequests().get(0);

        if(! U2F_VERSION.equals(registerRequest.getVersion())) {
            throw new U2FClientException(String.format("Unsupported U2F version %s", registerRequest.getVersion()));
        }

        final String challenge = registerRequest.getChallenge();
        final String appId = registerRequest.getAppId();
        final byte[] appIdSha256 = crypto.sha256(appId.getBytes());

        //TODO: verify that the appId matches the actual origin

        try {
            //Client data erstellen; base64,
            String rawClientData = createClientData(challenge, appId, ClientDataType.FINISH_ENROLLMENT);
            byte[] clientDataSha256 = crypto.sha256(rawClientData.getBytes());
            final String base64ClientData = U2FEncodings.encodeBase64Url(rawClientData);

            //Get the  attestation certificate
            AttestationCertificate attestationCert = deviceAccess.getCertificate();

            //Generate a new Key for the current appid
            U2FKeyPair key = deviceAccess.getKeyPair(new String(appIdSha256), new String(clientDataSha256));
            byte[] publicKeyEncodedData = deviceAccess.encodePublicKey(key);

            //Create the registration data neccessary to proceed the registartion process
            byte[] dataToSign = createRegisterDataToSign(appIdSha256,
                clientDataSha256,
                key.getKeyHandle(),
                publicKeyEncodedData);
            //Sign the data with the attestationCert's private key
            byte[] signature = crypto.sign(dataToSign, attestationCert.getPrivateKey());

            //Put everthing together
            byte[] rawResponseData = createRegistrationResponse(publicKeyEncodedData,
                key.getKeyHandle(),
                attestationCert.getCertificate().getEncoded(),
                signature);
            final String base64ResponseData = U2FEncodings.encodeBase64Url(rawResponseData);
            return new RegisterData(base64ClientData, base64ResponseData);
        } catch (JSONException | IOException | CertificateEncodingException e) {
            throw new U2FClientException(e.getMessage(), e);
        }
    }

    /**
     * Creates the authentication neccesary to process the authentication process for a U2F deive
     *
     * @param startAuthenticationData The data containing the callenge; Receviced from the server when triggered the authentication process
     * @return The data ready to be snet to the server in order to perform the actual authentication.
     * @throws U2FClientException
     */
    public AuthenticationData createAuthenticationData(MultifactorStartAuthenticationResponseData startAuthenticationData) throws U2FClientException {

        MultifactorStartAuthenticationResponseDataChallengeSignRequests signRequest = startAuthenticationData.getChallenge().getSignRequests().get(0);
        String base64KeyHandle = signRequest.getKeyHandle();

        final String appId = signRequest.getAppId();
        final byte[] appIdSha256 = crypto.sha256(appId.getBytes());

        try {
            //Client data erstellen: mit "getAssertion"
            final String rawClientData = createClientData(signRequest.getChallenge(), appId, ClientDataType.GET_ASSERTION);
            final String base64ClientData = U2FEncodings.encodeBase64Url(rawClientData);
            //client data (containing the server's raw challenge) becomes the challenge to send back
            byte[] challenge = crypto.sha256(rawClientData.getBytes());


            //By now we assume the user's presence (i.e. the user touched the button)
            final byte userPresence = 1;

            //Create the data to sign neccessary to finish the authentication process
            U2FKeyPair key = deviceAccess.getKeyPair(U2FEncodings.decodeBase64(base64KeyHandle));
            if(key != null) {
                final int counter = key.getCounter();
                final String base64ClientHandle = U2FEncodings.encodeBase64Url(key.getKeyHandle());

                byte[] dataToSign = createAuthenticationDataToSign(appIdSha256,
                    userPresence,
                    counter,
                    challenge);
                byte[] signature = crypto.sign(dataToSign, key.getKeyPair().getPrivate());

                //put everything together
                byte[] rawResponseData = createAuthenticationResponse(userPresence, counter, signature);
                String base64ResponseData = U2FEncodings.encodeBase64Url(rawResponseData);

                return new AuthenticationData(base64ClientData, base64ClientHandle, base64ResponseData);
            }
            else {
                throw new U2FClientException("Unable to get key for client handle");
            }

        } catch (JSONException | IOException e) {
            throw new U2FClientException(e.getMessage(), e);
        }
    }
}
