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

package com.openexchange.client.onboarding.plist.download;

import java.nio.charset.StandardCharsets;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.util.Optional;
import com.google.common.io.BaseEncoding;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.download.DownloadParameters;
import com.openexchange.client.onboarding.plist.servlet.PListDownloadServlet;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;

/**
 * {@link PlistLinkProviderImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class PlistLinkProviderImpl implements DownloadLinkProvider {

    private static final String USER_SECRET_ATTRIBUTE = "user_sms_link_secret";
    private static final String SERVLET_PATH = PListDownloadServlet.SERVLET_PATH;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link PlistLinkProviderImpl}.
     *
     * @param services The service look-up providing tracked OSGi services
     */
    public PlistLinkProviderImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    private static final char SLASH = '/';

    @Override
    public String getLink(HostData hostData, int userId, int contextId, String scenario, String device) throws OXException {
        StringBuilder url = new StringBuilder(256);
        url.append(hostData.isSecure() ? "https://" : "http://");
        url.append(hostData.getHost());
        url.append(getServletPrefix()).append(SERVLET_PATH);

        BaseEncoding encoder = BaseEncoding.base64().omitPadding();
        {
            String userString = encoder.encode(toAsciiBytes(Integer.toString(userId)));
            url.append(SLASH).append(userString);
        }
        {
            String contextString = encoder.encode(toAsciiBytes(Integer.toString(contextId)));
            url.append(SLASH).append(contextString);
        }
        {
            String deviceString = encoder.encode(device.getBytes(StandardCharsets.UTF_8));
            url.append(SLASH).append(deviceString);
        }
        {
            String scenarioString = encoder.encode(scenario.getBytes(StandardCharsets.UTF_8));
            url.append(SLASH).append(scenarioString);
        }
        {
            String challenge = toHash(userId, contextId, scenario, device, true).get();
            url.append(SLASH).append(challenge);
        }

        return url.toString();
    }

    @Override
    public DownloadParameters getParameter(String url) throws OXException {
        if (Strings.isEmpty(url)) {
            throw OnboardingExceptionCodes.INVALID_DOWNLOAD_LINK.create();
        }

        // Expect something like: /<user-id>/<context-id>/<device-id>/<scenario-id>/<challenge>
        String[] result = new String[5];
        BaseEncoding decoder = BaseEncoding.base64().omitPadding();

        try {
            String toParse = url;
            for (int x = 5; x-- > 0;) {
                int index = toParse.lastIndexOf(SLASH);
                if (index == -1 || index == toParse.length() - 1) {
                    throw OnboardingExceptionCodes.INVALID_DOWNLOAD_LINK.create();
                }
                String token = toParse.substring(index + 1);
                result[x] = x <= 3 ? Charsets.toAsciiString(decoder.decode(token)) : token;
                toParse = toParse.substring(0, index);
            }
        } catch (RuntimeException e) {
            throw OnboardingExceptionCodes.INVALID_DOWNLOAD_LINK.create(e);
        }

        try {
            return new DownloadParameters(Integer.parseInt(result[0]), Integer.parseInt(result[1]), result[2], result[3], result[4]);
        } catch (NumberFormatException e) {
            throw OnboardingExceptionCodes.INVALID_DOWNLOAD_LINK.create(e);
        }
    }

    @Override
    public boolean validateChallenge(int userId, int contextId, String scenario, String device, String challenge) throws OXException {
        Optional<String> hash = toHash(userId, contextId, scenario, device, false);
        return hash.isPresent() && hash.get().equals(challenge);
    }

    // ----------------------------------------------------- HELPERS ------------------------------------------------------------------------

    private String getServletPrefix() {
        DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        return prefixService == null ? DispatcherPrefixService.DEFAULT_PREFIX : prefixService.getPrefix();
    }

    private Optional<String> toHash(int userId, int contextId, String scenario, String device, boolean createSecretIfAbsent) throws OXException {
        try {
            Optional<String> optionalSecret = getOrCreateSecret(userId, contextId, createSecretIfAbsent);
            if (!optionalSecret.isPresent()) {
                return Optional.empty();
            }

            String challenge = new StringBuilder(128).append(userId).append(contextId)
                .append(device).append(scenario).append(optionalSecret.get()).toString();

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] challengeBytes = challenge.getBytes(StandardCharsets.UTF_8);
            md.update(challengeBytes, 0, challengeBytes.length);

            byte[] sha1hash = md.digest();
            return Optional.of(Strings.asHex(sha1hash));
        } catch (OXException e) {
            throw e;
        } catch (Exception e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Retrieves the user's SMS-link-secret or creates one if none is available (provided that <code>createIfAbsent</code> is <code>true</code>).
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param createIfAbsent <code>true</code> to create a secret of absent; otherwise <code>false</code>
     * @return The SMS-link-secret or empty (if there is none and <code>createIfAbsent</code> is <code>false</code>)
     * @throws OXException If SMS-link-secret cannot be returned
     */
    private Optional<String> getOrCreateSecret(int userId, int contextId, boolean createIfAbsent) throws OXException {
        UserService userService = services.getService(UserService.class);
        Context context = userService.getContext(contextId);

        // Read secret (if any)
        String secret = null;
        try {
            secret = userService.getUserAttribute(USER_SECRET_ATTRIBUTE, userId, context);
        } catch (OXException ex) {
            //do nothing
        }

        // Create if absent
        if (createIfAbsent && secret == null) {
            secret = new UID().toString();
            userService.setUserAttribute(USER_SECRET_ATTRIBUTE, secret, userId, context);
        }
        return Optional.ofNullable(secret);
    }

    private static byte[] toAsciiBytes(String s) {
        int size = s.length();
        byte[] bytes = new byte[size];
        for (int i = size; i-- > 0;) {
            bytes[i] = (byte) s.charAt(i);
        }
        return bytes;
    }

}
