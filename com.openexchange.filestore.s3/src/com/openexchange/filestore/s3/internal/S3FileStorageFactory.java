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

package com.openexchange.filestore.s3.internal;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.InterestsAware;
import com.openexchange.filestore.s3.internal.client.S3ClientRegistry;
import com.openexchange.filestore.s3.internal.client.S3FileStorageClient;
import com.openexchange.filestore.s3.internal.config.S3ClientConfig;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link S3FileStorageFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class S3FileStorageFactory implements FileStorageProvider, InterestsAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(S3FileStorageFactory.class);

    /**
     * The URI scheme identifying S3 file storages.
     */
    private static final String S3_SCHEME = "s3";

    /**
     * The expected pattern for file store names associated with a context - defined by
     * com.openexchange.filestore.FileStorages.getNameForContext(int) ,
     * so expect nothing else; e.g. <code>"57462_ctx_store"</code>
     */
    private static final Pattern CTX_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_store");

    /**
     * The expected pattern for file store names associated with a user - defined by
     * com.openexchange.filestore.FileStorages.getNameForUser(int, int) ,
     * so expect nothing else; e.g. <code>"57462_ctx_5_user_store"</code>
     */
    private static final Pattern USER_STORE_PATTERN = Pattern.compile("(\\d+)_ctx_(\\d+)_user_store");

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 5634;

    private final S3ClientRegistry clientRegistry;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link S3FileStorageFactory}.
     *
     * @param clientRegistry The {@link S3ClientRegistry}
     * @param services The service lookup
     */
    public S3FileStorageFactory(S3ClientRegistry clientRegistry, ServiceLookup services) {
        super();
        this.clientRegistry = clientRegistry;
        this.services = services;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties("com.openexchange.filestore.s3.*");
    }

    @Override
    public S3FileStorage getFileStorage(URI uri) throws OXException {
        try {
            LOG.debug("Initializing S3 client for {}", uri);
            /*
             * extract filestore ID from authority part of URI
             */
            String filestoreID = extractFilestoreID(uri);
            LOG.debug("Using \"{}\" as filestore ID.", filestoreID);

            S3ClientConfig clientConfig = S3ClientConfig.init(filestoreID, services.getServiceSafe(LeanConfigurationService.class));
            S3FileStorageClient client = clientRegistry.getOrCreate(clientConfig);
            LOG.debug("Using \"{}\" as bucket name for filestore \"{}\". Client is \"{}\" with scope \"{}\".",
                clientConfig.getBucketName(), filestoreID, client.getKey(), clientConfig.getClientScope());
            S3FileStorage s3FileStorage = new S3FileStorage(uri, extractFilestorePrefix(uri), clientConfig.getBucketName(), client);
            s3FileStorage.ensureBucket();
            return s3FileStorage;
        } catch (OXException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof AmazonS3Exception) {
                AmazonS3Exception s3Exception = (AmazonS3Exception) cause;
                if (s3Exception.getStatusCode() == 400) {
                    // throw a simple OXException here to be able forwarding this exception to RMI clients (Bug #42102)
                    throw new OXException(S3ExceptionCode.BadRequest.getNumber(), S3ExceptionMessages.BadRequest_MSG, s3Exception);
                }
            }

            throw ex;
        }
    }

    @Override
    public boolean supports(URI uri) {
         return null != uri && S3_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public int getRanking() {
        return RANKING;
    }

    /**
     * Extracts the filestore prefix from the configured file store URI, i.e. the 'path' part of the URI.
     *
     * @param uri The file store URI
     * @return The prefix to use
     * @throws IllegalArgumentException If the specified bucket name doesn't follow Amazon S3's guidelines
     */
    private static String extractFilestorePrefix(URI uri) throws IllegalArgumentException {
        // Extract & prepare path to be used as prefix
        String path = uri.getPath();
        while (0 < path.length() && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.endsWith("ctx_store")) {
            // Expect context store identifier
            Matcher matcher = CTX_STORE_PATTERN.matcher(path);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Path does not match the expected pattern \"\\d+_ctx_store\" in URI: " + uri);
            }
            return new StringBuilder(16).append(matcher.group(1)).append("ctxstore").toString();
        }

        if (path.endsWith("user_store")) {
            // Expect user store identifier
            Matcher matcher = USER_STORE_PATTERN.matcher(path);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Path does not match the expected pattern \"(\\d+)_ctx_(\\d+)_user_store\" in URI: " + uri);
            }
            return new StringBuilder(24).append(matcher.group(1)).append("ctx").append(matcher.group(2)).append("userstore").toString();
        }

        // Any path that serves as prefix; e.g. "photos"
        return sanitizePathForPrefix(path, uri);
    }

    /**
     * Strips all characters from specified prefix path, which are no allows according to
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html">this article</a>
     *
     * @param path The path to sanitize
     * @param uri The file store URI containing the path
     * @return The sanitized path ready to be used as prefix
     */
    private static String sanitizePathForPrefix(String path, URI uri) {
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException("Path is empty in URI: " + uri);
        }

        StringBuilder sb = null;
        for (int k = path.length(), i = 0; k-- > 0; i++) {
            char ch = path.charAt(i);
            if (Strings.isAsciiLetterOrDigit(ch) || isAllowedSpecial(ch)) {
                // Append
                if (null != sb) {
                    sb.append(ch);
                }
            } else {
                // Not allowed in prefix
                if (null == sb) {
                    sb = new StringBuilder(path.length());
                    if (i > 0) {
                        sb.append(path, 0, i);
                    }
                }
            }
        }
        return null == sb ? path : sb.toString();
    }

    /**
     * Checks if the character is an allowed special character
     *
     * @param ch The character to check
     * @return true if it is an allowed special character, false otherwise
     */
    private static boolean isAllowedSpecial(char ch) {
        switch (ch) {
            case '!':
            case '-':
            case '_':
            case '.':
            case '*':
            case '\'':
            case '(':
            case ')':
                return true;
            default:
                return false;
        }
    }

    /**
     * Extracts the filestore ID from the configured file store URI, i.e. the 'authority' part from the URI.
     *
     * @param uri The file store URI
     * @return The filestore ID
     * @throws IllegalArgumentException If no valid ID could be extracted
     */
    private static String extractFilestoreID(URI uri) throws IllegalArgumentException {
        String authority = uri.getAuthority();
        if (null == authority) {
            throw new IllegalArgumentException("No 'authority' part specified in filestore URI");
        }
        while (0 < authority.length() && '/' == authority.charAt(0)) {
            authority = authority.substring(1);
        }
        if (0 == authority.length()) {
            throw new IllegalArgumentException("No 'authority' part specified in filestore URI");
        }
        return authority;
    }

}
