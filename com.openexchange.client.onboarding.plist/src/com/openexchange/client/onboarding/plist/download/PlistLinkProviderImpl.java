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

package com.openexchange.client.onboarding.plist.download;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.google.common.io.BaseEncoding;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.client.onboarding.download.DownloadParameters;
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

    ServiceLookup services;
    private static final String USER_SECRET_ATTRIBUTE = "user_sms_link_secret";
    private static final String SERVLET_PATH = "plist";
    private static final char SLASH = '/';

    public PlistLinkProviderImpl(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Retrieves the user's SMS-link-secret or creates one if none is available.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The SMS-link-secret
     * @throws OXException If SMS-link-secret cannot be returned
     */
    private String getOrCreateSecret(int userId, int contextId) throws OXException {
        UserService userService = services.getService(UserService.class);
        Context context = userService.getContext(contextId);

        String secret = null;
        try {
            secret = userService.getUserAttribute(USER_SECRET_ATTRIBUTE, userId, context);
        } catch (OXException ex) {
            //do nothing
        }
        if (secret != null) {
            return secret;
        }

        secret = new UID().toString();
        userService.setUserAttribute(USER_SECRET_ATTRIBUTE, secret, userId, context);
        return secret;
    }

    private char[] toHash(int userId, int contextId, String scenario, String device) throws OXException {
        try {
            String secret = getOrCreateSecret(userId, contextId);
            String challenge = userId + contextId + device + scenario + secret;

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] challengeBytes = challenge.getBytes("UTF-8");
            md.update(challengeBytes, 0, challengeBytes.length);

            byte[] sha1hash = md.digest();
            return Strings.asHexChars(sha1hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static byte[] toAsciiBytes(String s) {
        int size = s.length();
        byte[] bytes = new byte[size];
        for (int i = size; i-- > 0;) {
            bytes[i] = (byte) s.charAt(i);
        }
        return bytes;
    }

    @Override
    public String getLink(HostData hostData, int userId, int contextId, String scenario, String device) throws OXException {
        StringBuilder url;
        {
            String serverUrl = (hostData.isSecure() ? "https://" : "http://") + hostData.getHost();
            url = new StringBuilder(serverUrl).append(getServletPrefix()).append(SERVLET_PATH);
        }

        BaseEncoding encoder = BaseEncoding.base64().omitPadding();
        {
            String userString = new String(encoder.encode(toAsciiBytes(Integer.toString(userId))));
            url.append(SLASH).append(userString);
        }
        {
            String contextString = new String(encoder.encode(toAsciiBytes(Integer.toString(contextId))));
            url.append(SLASH).append(contextString);
        }
        {
            String deviceString = new String(encoder.encode(device.getBytes(StandardCharsets.UTF_8)));
            url.append(SLASH).append(deviceString);
        }
        {
            String scenarioString = new String(encoder.encode(scenario.getBytes(StandardCharsets.UTF_8)));
            url.append(SLASH).append(scenarioString);
        }
        {
            char[] challenge = toHash(userId, contextId, scenario, device);
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
                String token = toParse.substring(index + 1, toParse.length());
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

    private String getServletPrefix() {
        DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        return prefixService == null ? DispatcherPrefixService.DEFAULT_PREFIX : prefixService.getPrefix();
    }

    @Override
    public boolean validateChallenge(int userId, int contextId, String scenario, String device, String challenge) throws OXException {
        char[] hash = toHash(userId, contextId, scenario, device);

        int n = hash.length;
        if (n != challenge.length()) {
            return false;
        }

        int i = 0;
        while (n-- != 0) {
            if (hash[i] != challenge.charAt(i)) {
                return false;
            }
            i++;
        }
        return true;
    }

}
