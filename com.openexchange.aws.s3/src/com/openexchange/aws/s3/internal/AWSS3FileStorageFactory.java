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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.aws.s3.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.BucketNameUtils;
import com.openexchange.exception.OXException;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageFactoryCandidate;

/**
 * {@link AWSS3FileStorageFactory}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSS3FileStorageFactory implements FileStorageFactoryCandidate {

    /**
     * The URI scheme identifying S3 file storages.
     */
    private static final String S3_SCHEME = "s3";

    /**
     * The file storage's ranking compared to other sharing the same URL scheme.
     */
    private static final int RANKING = 5634;

    private final ConcurrentMap<URI, AWSS3FileStorage> storages;
    private final AmazonS3Client s3client;

    /**
     * Initializes a new {@link AWSS3FileStorageFactory}.
     *
     * @param s3client The S3 client to use
     */
    public AWSS3FileStorageFactory(AmazonS3Client s3client) {
        super();
        this.storages = new ConcurrentHashMap<URI, AWSS3FileStorage>();
        this.s3client = s3client;
    }

    @Override
    public AWSS3FileStorage getFileStorage(URI uri) throws OXException {
        try { uri = new URI("s3:/development"); } catch (URISyntaxException e) { }
        AWSS3FileStorage storage = storages.get(uri);
        if (null == storage) {
            String bucketName = initBucket(uri);
            AWSS3FileStorage newStorage = new AWSS3FileStorage(s3client, bucketName);
            storage = storages.putIfAbsent(uri, newStorage);
            if (null == storage) {
                storage = newStorage;
            }
        }
        return storage;
    }

    @Override
    public FileStorage getInternalFileStorage(URI uri) throws OXException {
        return getFileStorage(uri);
    }

    @Override
    public boolean supports(URI uri) {
        return true;
         //return null != uri && S3_SCHEME.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public int getRanking() {
        return RANKING;
    }

    /**
     * Extracts and validates the bucket name part from the configured file storage URI.
     *
     * @param uri The file storage URI
     * @return The bucket name
     * @throws IllegalArgumentException If the specified bucket name doesn't follow Amazon S3's guidelines
     */
    private static String extractBucketName(URI uri) throws IllegalArgumentException {
        String path = uri.getPath();
        while (0 < path.length() && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        new BucketNameUtils().validateBucketName(path);
        return path;
    }

    /**
     * Initializes the bucket denoted by the supplied URI, creating the bucket dynamically if needed.
     *
     * @param uri The file storage URI
     * @return The bucket name
     * @throws OXException If initialization fails
     */
    private String initBucket(URI uri) throws OXException {
        try {
            String bucketName = extractBucketName(uri);
            if (false == s3client.doesBucketExist(bucketName)) {
                s3client.createBucket(bucketName);
            }
            return bucketName;
        } catch (IllegalArgumentException e) {
            throw AwsS3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (AmazonClientException e) {
            throw AwsS3ExceptionCode.wrap(e);
        } catch (RuntimeException e) {
            throw AwsS3ExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
