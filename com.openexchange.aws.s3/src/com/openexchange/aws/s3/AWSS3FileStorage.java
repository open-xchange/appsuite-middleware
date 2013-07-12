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

package com.openexchange.aws.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.DeleteObjectsResult.DeletedObject;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.openexchange.aws.s3.exceptions.OXAWSS3ExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.file.external.FileStorage;

/**
 * {@link AWSS3FileStorage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSS3FileStorage implements FileStorage {

    private static final Log LOG = LogFactory.getLog(AWSS3FileStorage.class);

    private final AmazonS3 amazonS3;

    private final AWSS3Configuration config;

    private final URI uri;

    private final Bucket bucket;

    /**
     * Initializes a new {@link AWSS3FileStorage}.
     *
     * @throws OXException
     */
    public AWSS3FileStorage(AmazonS3 amazonS3, AWSS3Configuration config, URI uri) throws OXException {
        super();
        this.amazonS3 = amazonS3;
        this.config = config;
        this.uri = uri;
        this.bucket = init();
    }

    private Bucket init() throws OXException {
        Bucket retval = null;
        try {
            String path = null;
            if (uri.getPath() == null || uri.getPath().equals("")) {
                path = config.getDefaultBucket();
            } else {
                path = getContextId(uri);
            }
            ListBucketsRequest listBucketsRequest = new ListBucketsRequest();
            List<Bucket> buckets = amazonS3.listBuckets(listBucketsRequest);
            boolean bucketExists = false;
            for (Bucket b : buckets) {
                if (b.getName().equals(path)) {
                    bucketExists = true;
                    retval = b;
                    break;
                }
            }
            if (!bucketExists) {
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(path);
                retval = amazonS3.createBucket(createBucketRequest);
            }
            if (config.isVersioningEnabled()) {
                SetBucketVersioningConfigurationRequest versioningConfigurationRequest = new SetBucketVersioningConfigurationRequest(
                    retval.getName(),
                    new BucketVersioningConfiguration(BucketVersioningConfiguration.ENABLED));
                amazonS3.setBucketVersioningConfiguration(versioningConfigurationRequest);
            }
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_INITIALIZATION_ERROR.create(e.getMessage());
        }
        return retval;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    public String saveNewFile(InputStream file) throws OXException {
        String retval = null;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.available());
            final PutObjectRequest putRequest = new PutObjectRequest(
                bucket.getName(),
                generateMD5(String.valueOf(System.currentTimeMillis())),
                file,
                metadata);
            putRequest.setProgressListener(new ProgressListener() {

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        LOG.warn("Transfer failed");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE) {
                        LOG.info("Transfer cancelled by user");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Request saveNewFile on object " + putRequest.getKey() + " completed");
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(progressEvent.getBytesTransfered() + " Bytes transferred");
                    }
                }
            });
            amazonS3.putObject(putRequest);
            retval = putRequest.getKey();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_IOERROR.create();
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_SAVE_FAILED.create(e.getMessage());
        }
        return retval;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    public InputStream getFile(String name) throws OXException {
        InputStream retval = null;
        try {
            final GetObjectRequest getRequest = new GetObjectRequest(bucket.getName(), name);
            getRequest.setProgressListener(new ProgressListener() {

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        LOG.warn("Transfer failed");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE) {
                        LOG.info("Transfer cancelled by user");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Request getFile on object " + getRequest.getKey() + " completed");
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(progressEvent.getBytesTransfered() + " Bytes transferred");
                    }
                }
            });
            S3Object object = amazonS3.getObject(getRequest);
            retval = object.getObjectContent();
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_GET_FILE_FAILED.create(name, e.getMessage());
        }
        return retval;
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        SortedSet<String> retval = new TreeSet<String>();
        try {
            ObjectListing listing = amazonS3.listObjects(bucket.getName());
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            for (S3ObjectSummary summary : summaries) {
                retval.add(summary.getKey());
            }
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_GET_FILELIST_FAILED.create(e.getMessage());
        }
        return retval;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    public long getFileSize(final String name) throws OXException {
        long retval = 0;
        try {
            final GetObjectRequest getRequest = new GetObjectRequest(bucket.getName(), name);
            getRequest.setProgressListener(new ProgressListener() {

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        LOG.warn("Transfer failed");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE) {
                        LOG.info("Transfer cancelled by user");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Request getFileSize on object " + getRequest.getKey() + " completed");
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(progressEvent.getBytesTransfered() + " Bytes transferred");
                    }
                }
            });
            S3Object object = amazonS3.getObject(getRequest);
            retval = object.getObjectMetadata().getContentLength();
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_GET_FILESIZE_FAILED.create(name, e.getMessage());
        }
        return retval;
    }

    @Override
    @SuppressWarnings("synthetic-access")
    public String getMimeType(String name) throws OXException {
        String retval = null;
        try {
            final GetObjectRequest getRequest = new GetObjectRequest(bucket.getName(), name);
            getRequest.setProgressListener(new ProgressListener() {

                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    if (progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                        LOG.warn("Transfer failed");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.CANCELED_EVENT_CODE) {
                        LOG.info("Transfer cancelled by user");
                    }
                    if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Request getMimeType on object " + getRequest.getKey() + " completed");
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(progressEvent.getBytesTransfered() + " Bytes transferred");
                    }
                }
            });
            S3Object object = amazonS3.getObject(getRequest);
            retval = object.getObjectMetadata().getContentType();
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_GET_MIMETYPE_FAILED.create(name, e.getMessage());
        }
        return retval;
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        try {
            DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucket.getName(), identifier);
            amazonS3.deleteObject(deleteRequest);
            return true;
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_DELETE_FILE_FAILED.create(identifier, e.getMessage());
        }
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        Set<String> retval = new TreeSet<String>();
        try {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucket.getName());
            List<KeyVersion> keys = new ArrayList<KeyVersion>();
            for (String file : identifiers) {
                KeyVersion keyVersion = new KeyVersion(file);
                keys.add(keyVersion);
            }
            deleteRequest.setKeys(keys);
            DeleteObjectsResult deleteResult = amazonS3.deleteObjects(deleteRequest);
            for (DeletedObject object : deleteResult.getDeletedObjects()) {
                retval.add(object.getKey());
            }
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_DELETE_MULTIPLE_FAILED.create(identifiers.length, e.getMessage());
        }
        return retval;
    }

    @Override
    public void remove() throws OXException {
        try {
            List<KeyVersion> allFiles = new ArrayList<KeyVersion>();
            ObjectListing listing = amazonS3.listObjects(bucket.getName());
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            for (S3ObjectSummary summary : summaries) {
                allFiles.add(new KeyVersion(summary.getKey()));
            }
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket.getName());
            deleteObjectsRequest.setKeys(allFiles);
            amazonS3.deleteObjects(deleteObjectsRequest);
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_DELETE_ALL_ERROR.create(bucket.getName(), e.getMessage());
        }
    }

    @Override
    public void recreateStateFile() throws OXException {
        if (!config.isVersioningEnabled()) {
            throw OXAWSS3ExceptionCodes.S3_VERSIONING_DISABLED.create();
        }
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        if (!config.isVersioningEnabled()) {
            throw OXAWSS3ExceptionCodes.S3_VERSIONING_DISABLED.create();
        }
        return true;
    }

    private String generateMD5(String input) throws OXException {
        MessageDigest md;
        String retval = null;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] message = input.getBytes("UTF-8");
            byte[] digest = md.digest(message);
            BigInteger bigInt = new BigInteger(digest);
            retval = bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_CREATE_HASH_FAILED.create(input, e.getMessage());
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_CREATE_HASH_FAILED.create(input, e.getMessage());
        }
        return retval;
    }

    private String getContextId(URI u) {
        String path = u.getPath();
        Pattern pattern = Pattern.compile("(.*?)(\\d+)(.*?)");
        Matcher m = pattern.matcher(path);
        if (m.matches()) {
            return m.group(2);
        }
        return null;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        throw new UnsupportedOperationException("not implemented");
    }

}
