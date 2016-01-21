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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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
import java.rmi.server.UID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.download.DownloadLinkProvider;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostData;
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
    private static final String SLASH = "/";

    public PlistLinkProviderImpl(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Retrieves the users sms-link-secret or creates one if none is available.
     *
     * @param userId
     * @param contextId
     * @return
     * @throws OXException
     */
    private String getOrCreateSecret(int userId, int contextId) throws OXException {
        String secret = getSecret(userId, contextId);
        if (secret != null) {
            return secret;
        }

        secret = new UID().toString();
        UserService userService = services.getService(UserService.class);
        Context con = userService.getContext(contextId);
        userService.setUserAttribute(USER_SECRET_ATTRIBUTE, secret, userId, con);
        return secret;
    }

    /**
     * Retrieves the users sms-link-secret.
     *
     * @param userId
     * @param contextId
     * @return
     * @throws OXException
     */
    private String getSecret(int userId, int contextId) throws OXException {
        UserService userService = services.getService(UserService.class);
        Context con = userService.getContext(contextId);
        String secret = null;
        try {
            secret = userService.getUserAttribute(USER_SECRET_ATTRIBUTE, userId, con);
        } catch (OXException ex) {
            //do nothing
        }
        return secret;
    }

    String toHash(int userId, int contextId, String scenario, String device) throws OXException {
        try {
            String secret = getOrCreateSecret(userId, contextId);
            String challenge = userId + contextId + device + scenario + secret;
            MessageDigest md;

            md = MessageDigest.getInstance("SHA-1");

            byte[] sha1hash = new byte[40];
            md.update(challenge.getBytes("UTF-8"), 0, challenge.length());
            sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static String convertToHex(byte[] data)
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < data.length; i++)
        {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do
            {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    @Override
    public String getLink(HostData hostData, int userId, int contextId, String scenario, String device) throws OXException {
        String serverUrl = (hostData.isSecure() ? "https://" : "http://") + hostData.getHost();
        StringBuilder url = new StringBuilder();
        String userString = new String(Base64.encodeBase64(String.valueOf(userId).getBytes()));
        String contextString = new String(Base64.encodeBase64(String.valueOf(contextId).getBytes()));
        String scenarioString = new String(Base64.encodeBase64(scenario.getBytes()));
        String deviceString = new String(Base64.encodeBase64(device.getBytes()));
        String challenge = toHash(userId, contextId, scenario, device);
        url.append(serverUrl).append(getServletPrefix()).append(SERVLET_PATH);
        url.append(SLASH).append(userString).append(SLASH).append(contextString).append(SLASH).append(deviceString).append(SLASH).append(scenarioString).append(SLASH).append(challenge);
        return url.toString();
    }

    @Override
    public String[] getParameter(String url) {
        String[] result = new String[5];
        for (int x = 4; x >= 0; x--) {
            int index = url.lastIndexOf(SLASH);
            result[x] = url.substring(index + 1, url.length());
            url = url.substring(0, index);
        }
        for (int x = 0; x <= 3; x++) {
            result[x] = new String(Base64.decodeBase64(result[x]));
        }

        return result;
    }

    private String getServletPrefix() {
        DispatcherPrefixService prefixService = services.getService(DispatcherPrefixService.class);
        if (prefixService == null) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }

        return prefixService.getPrefix();
    }

    @Override
    public boolean validateChallenge(int userId, int contextId, String scenario, String device, String challenge) throws OXException {
        String hash = toHash(userId, contextId, scenario, device);
        return hash.contentEquals(challenge);
    }

}
