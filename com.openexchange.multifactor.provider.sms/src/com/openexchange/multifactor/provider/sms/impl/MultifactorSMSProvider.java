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

package com.openexchange.multifactor.provider.sms.impl;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.multifactor.Challenge;
import com.openexchange.multifactor.ChallengeAnswer;
import com.openexchange.multifactor.DefaultRegistrationChallenge;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.MultifactorToken;
import com.openexchange.multifactor.ParameterlessMultifactorDevice;
import com.openexchange.multifactor.RegistrationChallenge;
import com.openexchange.multifactor.TokenCreationStrategy;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.multifactor.provider.sms.MultifactorSMSProperty;
import com.openexchange.multifactor.provider.sms.SMSMultifactorDevice;
import com.openexchange.multifactor.provider.sms.storage.SMSMultifactorDeviceStorage;
import com.openexchange.multifactor.storage.MultifactorTokenStorage;
import com.openexchange.multifactor.storage.impl.MemoryMultifactorDeviceStorage;
import com.openexchange.multifactor.util.DeviceNaming;
import com.openexchange.multifactor.util.MultifactorFormatter;
import com.openexchange.sms.PhoneNumberParserService;
import com.openexchange.sms.SMSServiceSPI;

/**
 * {@link SMSMultifactorProvider} - A multifactor provider which sends a secret token to a user's phone via SMS
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10."
 */
public class MultifactorSMSProvider implements MultifactorProvider{

    private static final Logger LOG = LoggerFactory.getLogger(MultifactorSMSProvider.class);

    public static final String NAME = "SMS";

    private final SMSMultifactorDeviceStorage                           storage;
    private final MemoryMultifactorDeviceStorage<SMSMultifactorDevice>  pendingStorage;
    private final TokenCreationStrategy                           tokenCreationStrategy;
    private MultifactorTokenStorage<MultifactorToken<String>>     tokenStorage;
    private final PhoneNumberParserService                        phoneNumberParser;
    private final SMSServiceSPI                                   smsService;
    private final LeanConfigurationService                        configService;

    /**
     *
     * Initializes a new {@link MultifactorSMSProvider}.
     *
     * @param configService The configuration service to use
     * @param storage The {@link SMSMultifactorDeviceStorage} to use
     * @param pendingStorage The storage to use for pending devices
     * @param tokenCreationStrategy The {@link TokenCreationStrategy} used to create secret tokens
     * @param smsServiceSpi  The {@link SMSServiceSPI} to use for sending messages
     * @param phoneNumberParser  The {@link PhoneNumberParserService} for parsing phone numbers
     */
    public MultifactorSMSProvider(LeanConfigurationService configService,
                                  SMSMultifactorDeviceStorage storage,
                                  MemoryMultifactorDeviceStorage<SMSMultifactorDevice> pendingStorage,
                                  TokenCreationStrategy tokenCreationStrategy,
                                  SMSServiceSPI smsServiceSpi,
                                  PhoneNumberParserService phoneNumberParser) {
        this.configService = Objects.requireNonNull(configService, " must not be null");
        this.storage = storage;
        this.pendingStorage = pendingStorage;
        this.tokenCreationStrategy = Objects.requireNonNull(tokenCreationStrategy, "tokenCreationStrategy must not be null");
        this.smsService = Objects.requireNonNull(smsServiceSpi, "smsServiceSpi must not be null");
        this.phoneNumberParser = Objects.requireNonNull(phoneNumberParser,"phoneNumberParser must not be null");
    }

    private SMSMultifactorDeviceStorage getStorageSave() throws OXException {
        if(storage == null) {
            throw MultifactorExceptionCodes.SERVICE_UNAVAILABLE.create(SMSMultifactorDeviceStorage.class.getSimpleName());
        }
        return storage;
    }

    private MultifactorTokenStorage<MultifactorToken<String>> getTokenStorageSave() {
        return Objects.requireNonNull(tokenStorage, "tokenStorage must not be null");
    }

    private boolean isDemoMode() {
       return this.configService.getBooleanProperty(MultifactorProperties.demo);
    }

    private static String newUid() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    /**
     * Internal method to create a device
     *
     * @param multifactorRequest The request to create a device for
     * @param sourceDevice The source device to create the new device from
     * @return The new SMS device
     * @throws OXException
     */
    private SMSMultifactorDevice createDevice(MultifactorRequest multifactorRequest, SMSMultifactorDevice sourceDevice) throws OXException {
        if (Strings.isEmpty(sourceDevice.getPhoneNumber())) {
            //no phone number was provided
            throw MultifactorExceptionCodes.MISSING_PARAMETER.create(SMSMultifactorDevice.PHONE_NUMBER_PARAMETER);
        }

        //prepare phone number; must be in international format
        String phoneNumber = sourceDevice.getPhoneNumber();
        phoneNumber = phoneNumberParser.parsePhoneNumber(phoneNumber /**always store in the same format => international format*/);
        //ensure "+"-sign
        if(!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + phoneNumber;
        }

        DeviceNaming.applyName(sourceDevice, () -> getDefaultName(multifactorRequest));
        if (sourceDevice.getName() != null && sourceDevice.getName().isEmpty() && (phoneNumber.length() > 4)) {
            sourceDevice.setName("*" + phoneNumber.substring(phoneNumber.length() - 4));
        }

        return new SMSMultifactorDevice(newUid(), sourceDevice.getName(), phoneNumber, sourceDevice.isBackup());
    }

    /**
     * Gets the length of the secret token to create
     *
     * @param multifactorRequest The request
     * @return The length of the secret token to create
     */
    private int getTokenLength(MultifactorRequest multifactorRequest) {
        return this.configService.getIntProperty(multifactorRequest.getUserId(), multifactorRequest.getContextId(), MultifactorSMSProperty.tokenLength);
    }

    /**
     * Gets the life-time of a secret token. I.e the time the user can use the token for authentication.
     *
     * @return The lifetime of the token
     */
    private Duration getTokenLifeTime() {
        return Duration.ofSeconds(this.configService.getIntProperty(MultifactorSMSProperty.tokenLifetime));
    }

    /**
     * Generates a new secret token
     *
     * @param length The length of the token to create
     * @return The new token with the given length
     * @throws OXException
     */
    private MultifactorToken<String> generateToken(int length) throws OXException {
        return new MultifactorToken<String>(this.tokenCreationStrategy.createToken(length), getTokenLifeTime());
    }

    /**
     * Gets the token with the given key from the token storage
     *
     * @param multifactorRequest The request
     * @param key The key to get the token for
     * @return The token with the given key or an empty {@link Optional} if no such token was found
     * @throws OXException
     */
    private Optional<MultifactorToken<String>> getToken(MultifactorRequest multifactorRequest, String key) throws OXException {
        return getTokenStorageSave().getAndRemove(multifactorRequest, key);
    }

    /**
     * Returns whether or not a new token should be created for a user
     *
     * @param multifactorRequest The request
     * @return <code>true</code>, if a new token should be created false otherwise, <code>false</code> if the max. amount of tokens for user has been reached
     * @throws OXException
     */
    private boolean shouldCreateNewToken(MultifactorRequest multifactorRequest) throws OXException {
        final int maxTokensAllowed = this.configService.getIntProperty(MultifactorSMSProperty.maxTokenAmount);
        return getTokenStorageSave().getTokenCount(multifactorRequest) < maxTokensAllowed;
    }

    /**
     * Stores a token in the token storage
     *
     * @param multifactorRequest The request to store the token for
     * @param token The token to store
     * @throws OXException
     */
    private void storeToken(MultifactorRequest multifactorRequest, MultifactorToken<String> token) throws OXException {
        //For SMS: The value is the key
        getTokenStorageSave().add(multifactorRequest, token.getValue(), token);
    }

    /**
     * Sends a token to the user's phone
     *
     * @param multifactorRequest The request
     * @param device The phone device
     * @param token The token to send to the device
     * @throws OXException
     */
    private void sendToken(MultifactorRequest multifactorRequest, SMSMultifactorDevice device, MultifactorToken<String> token) throws OXException {
        final String formattedToken = MultifactorFormatter.divide(token.getValue());
        if(!isDemoMode()) {
            smsService.sendMessage(
                new String[] { device.getPhoneNumber()},
                SMSMessageCreator.createMessage(multifactorRequest, formattedToken),
                multifactorRequest.getUserId(),
                multifactorRequest.getContextId());
        } else {
            LOG.info("Cannot send out SMS authentication token because the system is in demo mode");
        }
    }

    /**
     * Performs authentication using a provided secret token. This checks if a token is valid for a given session.
     *
     * @param multifactorRequest The request/session containing to check
     * @param device The device A device for re-triggering a new token in case the authentication failed
     * @param token The secret token to validate
     * @throws OXException
     */
    private void doAuthenticationInternal(MultifactorRequest multifactorRequest, SMSMultifactorDevice device, String token) throws OXException {
        if (Strings.isEmpty(token)) {
            triggerToken(device, multifactorRequest);
            throw MultifactorExceptionCodes.MISSING_PARAMETER.create(SMSMultifactorDevice.SECRET_CODE_PARAMETER);
        }

        final String clientToken = MultifactorFormatter.removeWhiteSpaces(token);
        final Optional<MultifactorToken<String>> storedToken = getToken(multifactorRequest, clientToken);

        if (storedToken.isPresent()) {
            return;
        }
        throw MultifactorExceptionCodes.AUTHENTICATION_FAILED.create();
    }

    /**
     * Internal method to check if a device is registered
     *
     * @param multifactorRequest The request
     * @param deviceId The ID of the device
     * @return <code>true</code>, if a device with the given ID is registered, <code>false</code> otherwise
     * @throws OXException
     */
    private boolean isDeviceRegistered(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        return getDevice(multifactorRequest, deviceId).isPresent();
    }

    /**
     * Internal method to trigger (create,store,send) a token if the maximum amount of allowed tokens has not been exceeded
     *
     * @param device The device to send the token to
     * @param multifactorRequest The request
     * @return <code>true</code>, if a new token was created, stored and send. <code>false</code> if the token was not created, because the maximum amount of allowed token has been exceeded.
     * @throws OXException
     */
    private boolean triggerToken(SMSMultifactorDevice device, MultifactorRequest multifactorRequest) throws OXException {
        if (shouldCreateNewToken(multifactorRequest)) {
           final MultifactorToken<String> newToken = generateToken(getTokenLength(multifactorRequest));
           storeToken(multifactorRequest, newToken);
           sendToken(multifactorRequest, device, newToken);
           return true;
        }
        LOG.info("Could not send a new SMS token to user {} in context{}, because the max. allowed amount of active tokens exceeded", multifactorRequest.getUserId(), multifactorRequest.getContextId());
        return false;
    }

    /**
     * Internal method to create a default, translated, device name
     *
     * @param request The request to create the default name for
     * @return The default device name in the user's language
     */
    private String getDefaultName(MultifactorRequest request) {
        return StringHelper.valueOf(request.getLocale()).getString(MultifactorSMSStrings.MULTIFACTOR_SMS_DEFAULT_DEVICE_NAME);
    }

    public MultifactorSMSProvider setTokenStorage(MultifactorTokenStorage<MultifactorToken<String>> tokenStorage) {
       this.tokenStorage = tokenStorage;
       return this;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled(MultifactorRequest multifactorRequest) {
        return configService.getBooleanProperty(multifactorRequest.getUserId(), multifactorRequest.getContextId(), MultifactorSMSProperty.enabled);
    }

    @Override
    public Collection<? extends MultifactorDevice> getDevices(MultifactorRequest multifactorRequest) throws OXException {
        if(storage == null) {
            return Collections.emptyList();
        }
        Collection<SMSMultifactorDevice> devices = storage.getDevices(multifactorRequest.getContextId(), multifactorRequest.getUserId());
        //Wrap in order to remove all, possibly, security related secrets
        return devices.stream().map(d -> new ParameterlessMultifactorDevice(d)).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends MultifactorDevice> getEnabledDevices(MultifactorRequest multifactorRequest) throws OXException {
        return getDevices(multifactorRequest).stream().filter(d -> d.isEnabled()).collect(Collectors.toList());
    }

    @Override
    public Optional<? extends MultifactorDevice> getDevice(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        Optional<SMSMultifactorDevice> device = storage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);
        //Wrap in order to remove all, possibly, security related secrets
        return device.map(d -> new ParameterlessMultifactorDevice(d));
    }

    @Override
    public RegistrationChallenge startRegistration(MultifactorRequest multifactorRequest, MultifactorDevice device) throws OXException {

        SMSMultifactorDevice inputDevice = new SMSMultifactorDevice(device);
        //Gets the device name from the request if present, default-name otherwise

        //Create a new phone device
        final SMSMultifactorDevice phoneDevice = createDevice(multifactorRequest, inputDevice);

        //Add pending registration
        pendingStorage.registerDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), phoneDevice);

        try {
            //Send SMS token
            triggerToken(phoneDevice, multifactorRequest);
        } catch (final Exception e) {
           pendingStorage.unregisterDevice(multifactorRequest.getContextId(), multifactorRequest.getContextId(), phoneDevice);
           throw e;
        }

        return new DefaultRegistrationChallenge(phoneDevice.getId(), Collections.emptyMap());
    }

    @Override
    public MultifactorDevice finishRegistration(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        if (!isDeviceRegistered(multifactorRequest, deviceId)) {
            final Optional<SMSMultifactorDevice> pendingDevice = pendingStorage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);
            if (pendingDevice.isPresent()) {
                doAuthenticationInternal(multifactorRequest, pendingDevice.get(), (String) answer.requireField(SMSAnswerField.SECRET));

                //Enable the device
                pendingDevice.get().enable(true);

                try {
                    //Add the device to the persistent storage
                    getStorageSave().registerDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), pendingDevice.get());
                } catch(Exception e) {
                   pendingDevice.get().enable(false);
                   throw e;
                }

                //Remove the device from the pending registrations
                pendingStorage.unregisterDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);

                return pendingDevice.get();
            }
            throw MultifactorExceptionCodes.REGISTRATION_FAILED.create();
        }
        throw MultifactorExceptionCodes.DEVICE_ALREADY_REGISTERED.create();
    }

    @Override
    public void deleteRegistration(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        if (getStorageSave().unregisterDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId)) {
            return;
        }
        throw MultifactorExceptionCodes.DEVICE_REMOVAL_FAILED.create();
    }

    @Override
    public boolean deleteRegistrations(int contextId, int userId) throws OXException {
        return getStorageSave().deleteAllForUser(userId, contextId);
    }

    @Override
    public boolean deleteRegistrations(int contextId) throws OXException {
        return getStorageSave().deleteAllForContext(contextId);
    }

    @Override
    public Challenge beginAuthentication(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        final Optional<SMSMultifactorDevice> device = getStorageSave().getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);
        if (!device.isPresent()) {
            throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();  // No devices
        }
        triggerToken(device.get(), multifactorRequest);

        //the returned challenge contains the last digits of the phone number.
        //This is for usability reason, if the user owns more than one mobile phone.
        return new Challenge() {

            @Override
            public Map<String, Object> getChallenge() {
                return Collections.singletonMap(SMSMultifactorDevice.PHONE_NUMBER_TAIL_PARAMETER, device.get().getPhoneNumberTail());
            }

        };
    }

    @Override
    public void doAuthentication(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        final Optional<SMSMultifactorDevice> device = getStorageSave().getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);
        if (!device.isPresent()) {
            throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();  // No devices
        }

        doAuthenticationInternal(multifactorRequest, device.get(), answer.requireField(SMSAnswerField.SECRET).toString());
    }

    @Override
    public boolean isBackupProvider() {
        return configService.getBooleanProperty(MultifactorSMSProperty.backup);
    }

    @Override
    public MultifactorDevice renameDevice(MultifactorRequest multifactorRequest, MultifactorDevice inputDevice) throws OXException {
        if (Strings.isEmpty(inputDevice.getName())) {
            throw MultifactorExceptionCodes.MISSING_PARAMETER.create("Name missing or invalid");
        }
        if (storage.renameDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), inputDevice.getId(), inputDevice.getName())) {
            return storage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), inputDevice.getId()).get();
        }
        throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();
    }
}
