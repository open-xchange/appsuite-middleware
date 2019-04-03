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

package com.openexchange.multifactor.provider.backupString.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.login.multifactor.MultifactorLoginService;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.multifactor.Challenge;
import com.openexchange.multifactor.ChallengeAnswer;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.ParameterlessMultifactorDevice;
import com.openexchange.multifactor.RegistrationChallenge;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.multifactor.provider.backupString.BackupStringMultifactorDevice;
import com.openexchange.multifactor.provider.backupString.storage.BackupStringMultifactorDeviceStorage;
import com.openexchange.multifactor.util.DeviceNaming;
import com.openexchange.multifactor.util.MultifactorFormatter;

/**
 * Provider for Backup String for account recovery
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class MultifactorBackupStringProvider implements MultifactorProvider {

    public static final String NAME       = "BACKUP_STRING";
    static final String        PARAM_CODE = "secret_code";

    private final BackupStringMultifactorDeviceStorage deviceStorage;
    private final LeanConfigurationService configurationService;
    private final MultifactorLoginService multifactorLoginService;

    /**
     * Initializes a new {@link MultifactorBackupStringProvider}.
     *
     * @param configurationService The {@link LeanConfigurationService} to use
     * @param deviceStorage The device storage to use
     * @param multifactorLoginService The {@link MultifactorLoginService} to use
     */
    public MultifactorBackupStringProvider(LeanConfigurationService configurationService, BackupStringMultifactorDeviceStorage deviceStorage, MultifactorLoginService multifactorLoginService) {
        this.configurationService = Objects.requireNonNull(configurationService, "configurationService must not be null");
        this.deviceStorage = Objects.requireNonNull(deviceStorage, "deviceStorage must not be null");
        this.multifactorLoginService = Objects.requireNonNull(multifactorLoginService, "MultifactorLoginService must not be null");
    }

    /**
     * Perform authentication for device
     *
     * @param device Device to check authentication against
     * @param answer The answer to the {@link Challenge}
     * @throws OXException
     */
    private void doAuthenticationInternal(BackupStringMultifactorDevice device, ChallengeAnswer answer) throws OXException {
        final String sharedSecret = device.getHashedSharedSecret();

        if (answer != null) {
            String code = MultifactorFormatter.removeWhiteSpaces(answer.requireField(BackupStringAnswerField.SECRET).toString());
            if (!code.isEmpty()) {
                //We need to hash the value because it is stored in hashed representation within the storage
                code = hashValue(device.getId(), code);
                if (code.equals(sharedSecret)) {
                    return;
                }
                throw MultifactorExceptionCodes.AUTHENTICATION_FAILED.create();
            }
        }
        throw MultifactorExceptionCodes.MISSING_PARAMETER.create(PARAM_CODE);
    }

    /**
     * Convert byte hash to hexString
     *
     * @param hash
     * @return hexString
     */
    private String toHex(byte[] hash) {
        final StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            final String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Generate a salted hash from value
     *
     * @param salt Salt to use
     * @param value Value to be hashed
     * @return Hash
     * @throws OXException
     */
    private String hashValue(String salt, String value) throws OXException {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            final byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (final NoSuchAlgorithmException e) {
            throw MultifactorExceptionCodes.ERROR_CREATING_FACTOR.create(e, e.getMessage());
        }
    }

    private static String newUid() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

    /**
     * Get configured backup string length from configuration cascade
     *
     * @param session
     * @return
     */
    private int getStringLength(MultifactorRequest multifactorRequest) {
        if (configurationService != null) {
            return configurationService.getIntProperty(multifactorRequest.getUserId(), multifactorRequest.getContextId(), MultifactorBackupStringProperty.stringLength);
        }
        return -1;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled(MultifactorRequest multifactorRequest) {
        return configurationService.getBooleanProperty(multifactorRequest.getUserId(), multifactorRequest.getContextId(), MultifactorBackupStringProperty.enabled);
    }

    @Override
    public Collection<? extends MultifactorDevice> getDevices(MultifactorRequest multifactorRequest) throws OXException {
        if (deviceStorage == null) {
            return Collections.emptyList();
        }
        //Wrap in order to remove all, possibly, security related secrets
        return deviceStorage.getDevices(multifactorRequest.getContextId(), multifactorRequest.getUserId())
            .stream().map(d -> new ParameterlessMultifactorDevice(d)).collect(Collectors.toList());
    }

    @Override
    public Collection<MultifactorDevice> getEnabledDevices(MultifactorRequest multifactorRequest) throws OXException {
        return getDevices(multifactorRequest).stream().filter(d -> d.isEnabled()).collect(Collectors.toList());
    }

    @Override
    public Optional<? extends MultifactorDevice> getDevice(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        //Wrap in order to remove all, possibly, security related secrets
        return deviceStorage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId)
            .map(d -> new ParameterlessMultifactorDevice(d));
    }

    @Override
    public RegistrationChallenge startRegistration(MultifactorRequest multifactorRequest, MultifactorDevice inputDevice) throws OXException {

        if (!multifactorLoginService.requiresMultifactor(multifactorRequest.getUserId(), multifactorRequest.getContextId())) {
            throw MultifactorExceptionCodes.ERROR_CREATING_FACTOR.create(StringHelper.valueOf(multifactorRequest.getLocale()).
            getString(BackupStringsLocalizationStrings.MUST_HAVE_OTHER_DEVICES));
        }
        final String deviceId = newUid();
        final String newSharedSecret = BackupStringCodeGenerator.generateString(getStringLength(multifactorRequest));
        DeviceNaming.applyName(inputDevice, () ->
            //DefaultName
            StringHelper.valueOf(multifactorRequest.getLocale()).
                getString(BackupStringsLocalizationStrings.MULTIFACTOR_BACKPUP_STRINGS_DEVICE_NAME));

        //We just create a new device
        //We can hash the secret so that it is not stored in plain text in the storage; we need to store the length of the original secret though for providing it back to the client
        final BackupStringMultifactorDevice device = new BackupStringMultifactorDevice(
            deviceId,
            inputDevice.getName(),
            hashValue(deviceId, newSharedSecret) /*we do only persist the hashed representation of the secret*/,
            newSharedSecret.length());
        device.enable(true);

        // Just add device here, no finishRegistration action needed.
        deviceStorage.registerDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), device);

        // Adding the shared secret as plain text in order to be returned to the caller
        return new BackupRegistrationChallenge(deviceId, newSharedSecret);
    }

    @Override
    public MultifactorDevice finishRegistration(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        return deviceStorage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId).orElseThrow(() -> MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create());
    }

    @Override
    public void deleteRegistration(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        if (deviceStorage.unregisterDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId)) {
            return;
        }
        throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();
    }

    @Override
    public boolean deleteRegistrations(int contextId, int userId) throws OXException {
        return deviceStorage.deleteAllForUser(userId, contextId);
    }

    @Override
    public boolean deleteRegistrations(int contextId) throws OXException {
        return deviceStorage.deleteAllForContext(contextId);
    }

    @Override
    public Challenge beginAuthentication(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        final Optional<BackupStringMultifactorDevice> device = deviceStorage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);
        if (!device.isPresent()) {
            throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();  // No devices
        }
        //the returned challenge contains the length of the secret code, useful for rendering the input fields
        return new Challenge() {

            @Override
            public Map<String, Object> getChallenge() {
                return Collections.singletonMap(BackupStringMultifactorDevice.BACKUP_STRING_LENGTH_PARAMETER, device.get().getSecretLength());
            }

        };
    }

    @Override
    public void doAuthentication(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        final Optional<BackupStringMultifactorDevice> device = deviceStorage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), deviceId);
        if (!device.isPresent()) {
            throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();  // No devices
        }

        doAuthenticationInternal(device.get(), answer);
    }

    @Override
    public boolean isBackupProvider() {
        return true;
    }

    @Override
    public boolean isBackupOnlyProvider() {
        return true;
    }

    @Override
    public MultifactorDevice renameDevice(MultifactorRequest multifactorRequest, MultifactorDevice inputDevice) throws OXException {
        if (Strings.isEmpty(inputDevice.getName())) {
            throw MultifactorExceptionCodes.MISSING_PARAMETER.create("Name missing or invalid");
        }
        if (deviceStorage.renameDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), inputDevice.getId(), inputDevice.getName())) {
            return deviceStorage.getDevice(multifactorRequest.getContextId(), multifactorRequest.getUserId(), inputDevice.getId()).orElseThrow(() -> MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create());
        }
        throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();
    }
}
