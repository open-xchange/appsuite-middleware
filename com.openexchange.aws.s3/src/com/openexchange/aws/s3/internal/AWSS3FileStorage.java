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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.logging.Log;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.DeleteObjectsResult.DeletedObject;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.openexchange.aws.s3.exceptions.OXAWSS3ExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link AWSS3FileStorage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSS3FileStorage implements FileStorage {

    private static final Log LOG = LogFactory.getLog(AWSS3FileStorage.class);

    /**
     * The size of the in-memory buffer for uploads to use.
     */
    private static final int UPLOAD_BUFFER_SIZE = 1024 * 1024 * 2; // 2 MiB

    private final AmazonS3Client amazonS3;
    private final String bucketName;

    /**
     * Initializes a new {@link AWSS3FileStorage}.
     *
     * @param amazonS3 The underlying S3 client
     * @param bucketName The bucket name to use
     * @throws OXException
     */
    public AWSS3FileStorage(AmazonS3Client amazonS3, String bucketName) throws OXException {
        super();
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        this.amazonS3.addRequestHandler(new WorkaroundingRequestHandler());
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        /*
         * initiate multipart
         */
        String key = newUid();
        String uploadID = initiateMultipartUpload(key);
        /*
         * upload in chunks to provide the correct content length
         */
        boolean comleted = false;
        DigestInputStream digestStream = null;
        try {
            List<PartETag> partETags = new ArrayList<PartETag>();
            byte[] buffer = new byte[UPLOAD_BUFFER_SIZE];
            int read;
            int partNumber = 1;
            digestStream = new DigestInputStream(file, MessageDigest.getInstance("MD5"));
            while (-1 != (read = digestStream.read(buffer, 0, UPLOAD_BUFFER_SIZE))) {
                byte[] digest = digestStream.getMessageDigest().digest();
                ByteArrayInputStream bais = null;
                try {
                    bais = new ByteArrayInputStream(buffer, 0, read);
                    UploadPartRequest request = new UploadPartRequest().withBucketName(bucketName).withKey(key).withUploadId(uploadID)
                        .withInputStream(bais).withPartSize(read).withPartNumber(partNumber++).withMD5Digest(Base64.encode(digest));
                    partETags.add(amazonS3.uploadPart(request).getPartETag());
                } finally {
                    Streams.close(bais);
                }
            }
            amazonS3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadID, partETags));
            comleted = true;
            return key;
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } catch (AmazonClientException e) {
            throw wrap(e);
        } catch (NoSuchAlgorithmException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            if (false == comleted) {
                try {
                    amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, key, uploadID));
                } catch (AmazonClientException e) {
                    LOG.warn("Error aborting multipart upload", e);
                }
            }
        }
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        try {
            return amazonS3.getObject(bucketName, name).getObjectContent();
        } catch (AmazonClientException e) {
            throw wrap(e);
        }
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        SortedSet<String> retval = new TreeSet<String>();
        try {
            ObjectListing listing = amazonS3.listObjects(bucketName);
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
    public long getFileSize(final String name) throws OXException {
        return getMetadata(name).getContentLength();
    }

    @Override
    public String getMimeType(String name) throws OXException {
        //TODO: makes no sense at storage layer
        return getMetadata(name).getContentType();
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        try {
            amazonS3.deleteObject(bucketName, identifier);
            return true;
        } catch (AmazonClientException e) {
            throw wrap(e);
        }
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        Set<String> retval = new TreeSet<String>();
        try {
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName);
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
            ObjectListing listing = amazonS3.listObjects(bucketName);
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            for (S3ObjectSummary summary : summaries) {
                allFiles.add(new KeyVersion(summary.getKey()));
            }
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
            deleteObjectsRequest.setKeys(allFiles);
            amazonS3.deleteObjects(deleteObjectsRequest);
        } catch (AmazonClientException e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSS3ExceptionCodes.S3_DELETE_ALL_ERROR.create(bucketName, e.getMessage());
        }
    }

    @Override
    public void recreateStateFile() throws OXException {
        // no
    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        return true;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        /*
         * TODO: This would be more efficient using the "CopyPartRequest", which is not yet supported by ceph
         * http://ceph.com/docs/next/radosgw/s3/#features-support
         */


        // http://docs.aws.amazon.com/AmazonS3/latest/API/mpUploadUploadPartCopy.html
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        /*
         * TODO: This would be much more efficient using the "CopyPartRequest", which is not yet supported by ceph
         * see: http://ceph.com/docs/next/radosgw/s3/#features-support
         */
        /*
         * copy previous file to temporary file
         */
        String tempKey = newUid();
        try {
            amazonS3.copyObject(bucketName, name, bucketName, tempKey);
            /*
             * upload $length bytes from previous file to new current file
             */
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(length);
            InputStream inputStream = null;
            try {
                inputStream = getFile(tempKey, 0, length);
                amazonS3.putObject(bucketName, name, inputStream, metadata);
            } finally {
                Streams.close(inputStream);
            }
        } catch (AmazonClientException e) {
            throw wrap(e);
        } finally {
            try {
                amazonS3.deleteObject(bucketName, tempKey);
            } catch (AmazonClientException e) {
                LOG.warn("Error cleaning up temporary file", e);
            }
        }
    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        GetObjectRequest request = new GetObjectRequest(bucketName, name);
        request.setRange(offset, offset + length);
        try {
            return amazonS3.getObject(request).getObjectContent();
        } catch (AmazonClientException e) {
            throw wrap(e);
        }
    }

    /**
     * Initiates a new multipart upload for a file with the supplied key.
     *
     * @param key The key for the new file
     * @return The upload ID for the multipart upload
     * @throws OXException
     */
    private String initiateMultipartUpload(String key) throws OXException {
        try {
            return amazonS3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key)).getUploadId();
        } catch (AmazonClientException e) {
            throw wrap(e);
        }
    }

    /**
     * Gets metadata for an existing file.
     *
     * @param key The key of the file
     * @return The metadata
     * @throws OXException
     */
    private ObjectMetadata getMetadata(String key) throws OXException {
        try {
            return amazonS3.getObjectMetadata(bucketName, key);
        } catch (AmazonServiceException e) {
            if ("NoSuchKey".equals(e.getErrorCode())) {
                throw FileStorageCodes.FILE_NOT_FOUND.create(e, key);
            }
            throw wrap(e);
        } catch (AmazonClientException e) {
            throw wrap(e);
        }
    }

    private static OXException wrap(AmazonClientException e) {
        //TODO
        if (AmazonServiceException.class.isInstance(e)) {
            AmazonServiceException ase = (AmazonServiceException)e;

        }
        return FileStorageCodes.IOERROR.create(e, e.getMessage());
    }

    private static String newUid() {
        return UUIDs.getUnformattedString(UUID.randomUUID());
    }

}
