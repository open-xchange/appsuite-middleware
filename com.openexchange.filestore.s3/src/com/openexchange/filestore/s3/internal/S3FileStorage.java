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

package com.openexchange.filestore.s3.internal;

import static com.openexchange.filestore.s3.internal.S3ExceptionCode.wrap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.BucketNameUtils;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.MultiObjectDeleteException.DeleteError;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link S3FileStorage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class S3FileStorage implements FileStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(S3FileStorage.class);

    /**
     * The size of the in-memory buffer for uploads to use.
     */
    private static final int UPLOAD_BUFFER_SIZE = 1024 * 1024 * 2; // 2 MiB

    /**
     * The delimiter character to separate the prefix from the keys
     */
    private static final String DELIMITER = "/";

    private final AmazonS3Client amazonS3;
    private final String bucketName;
    private final String prefix;

    /**
     * Initializes a new {@link S3FileStorage}.
     *
     * @param amazonS3 The underlying S3 client
     * @param bucketName The bucket name to use
     * @param prefix The prefix to use
     * @throws OXException
     */
    public S3FileStorage(AmazonS3Client amazonS3, String bucketName, String prefix) {
        super();
        BucketNameUtils.validateBucketName(bucketName);
        if (Strings.isEmpty(prefix) || prefix.contains(DELIMITER)) {
            throw new IllegalArgumentException(prefix);
        }
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        this.prefix = prefix;
        LOG.info("S3 file storage initialized for \"" + bucketName + "/" + prefix + DELIMITER + "\"");
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        /*
         * prepare upload
         */
        String key = generateKey(true);
        DigestInputStream digestStream = null;
        try {
            byte[] buffer = new byte[UPLOAD_BUFFER_SIZE];
            digestStream = new DigestInputStream(file, MessageDigest.getInstance("MD5"));
            /*
             * fill first buffer from stream
             */
            int read = fillBuffer(digestStream, buffer);
            if (read < UPLOAD_BUFFER_SIZE) {
                /*
                 * whole file fits into buffer (this includes a zero byte file), upload directly
                 */
                int length = -1 == read ? 0 : read;
                byte[] digest = digestStream.getMessageDigest().digest();
                ByteArrayInputStream bais = null;
                try {
                    bais = new ByteArrayInputStream(buffer, 0, length);
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(length);
                    metadata.setContentMD5(Base64.encode(digest));
                    amazonS3.putObject(bucketName, key, bais, metadata);
                } finally {
                    Streams.close(bais);
                }
            } else {
                /*
                 * upload in multipart chunks to provide the correct content length
                 */
                String uploadID = initiateMultipartUpload(key);
                boolean completed = false;
                try {
                    List<PartETag> partETags = new ArrayList<PartETag>();
                    int partNumber = 1;
                    do {
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
                    } while (-1 != (read = fillBuffer(digestStream, buffer)));
                    /*
                     * complete upload
                     */
                    amazonS3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, key, uploadID, partETags));
                    completed = true;
                } finally {
                    if (false == completed) {
                        try {
                            amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, key, uploadID));
                        } catch (AmazonClientException e) {
                            LOG.warn("Error aborting multipart upload", e);
                        }
                    }
                }
            }
            return removePrefix(key);
        } catch (NoSuchAlgorithmException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } catch (AmazonClientException e) {
            throw wrap(e);
        } finally {
            Streams.close(digestStream);
        }
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        return getObject(addPrefix(name)).getObjectContent();
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        SortedSet<String> files = new TreeSet<String>();
        /*
         * results may be paginated - repeat listing objects as long as result is truncated
         */
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucketName).withDelimiter(DELIMITER).withPrefix(prefix + DELIMITER);
        ObjectListing objectListing;
        do {
            objectListing = amazonS3.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                files.add(removePrefix(objectSummary.getKey()));
            }
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());
        return files;
    }

    @Override
    public long getFileSize(final String name) throws OXException {
        return getMetadata(addPrefix(name)).getContentLength();
    }

    @Override
    public String getMimeType(String name) throws OXException {
        //TODO: makes no sense at storage layer
        return getMetadata(addPrefix(name)).getContentType();
    }

    @Override
    public boolean deleteFile(String name) throws OXException {
        String key = addPrefix(name);
        try {
            amazonS3.deleteObject(bucketName, key);
            return true;
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    @Override
    public Set<String> deleteFiles(String[] names) throws OXException {
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName).withKeys(addPrefix(names));
        try {
            amazonS3.deleteObjects(deleteRequest);
        } catch (MultiObjectDeleteException e) {
            List<DeleteError> errors = e.getErrors();
            if (null != errors && 0 < errors.size()) {
                Set<String> notDeleted = new HashSet<String>();
                for (DeleteError error : errors) {
                    notDeleted.add(removePrefix(error.getKey()));
                }
                return notDeleted;
            }
        } catch (AmazonClientException e) {
            throw wrap(e);
        }
        return Collections.emptySet();
    }

    @Override
    public void remove() throws OXException {
        try {
            /*
             * try and delete all contained files repeatedly
             */
            final int RETRY_COUNT = 10;
            for (int i = 0; i < RETRY_COUNT; i++) {
                try {
                    SortedSet<String> fileList = getFileList();
                    if (null == fileList || 0 == fileList.size()) {
                        break; // no more files found
                    }
                    amazonS3.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(addPrefix(fileList)));
                } catch (MultiObjectDeleteException e) {
                    if (i < RETRY_COUNT - 1) {
                        LOG.warn("Not all files in bucket deleted yet, trying again.", e);
                    } else {
                        throw FileStorageCodes.NOT_ELIMINATED.create("Not all files in bucket deleted after " + i + " tries, giving up.", e);
                    }
                }
            }
        } catch (OXException e) {
            throw FileStorageCodes.NOT_ELIMINATED.create(e);
        } catch (AmazonClientException e) {
            throw FileStorageCodes.NOT_ELIMINATED.create(wrap(e));
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
        /*
         * prepare temporary file for append operation
         */
        String key = addPrefix(name);
        String tempKey = generateKey(true);
        String uploadID = initiateMultipartUpload(key);
        boolean completed = false;
        try {
            /*
             * upload existing data as first part
             */
            List<PartETag> partETags = new ArrayList<PartETag>();
            InputStream inputStream = null;
            try {
                inputStream = getObject(key).getObjectContent();
                UploadPartRequest request = new UploadPartRequest().withBucketName(bucketName).withKey(tempKey).withUploadId(uploadID)
                    .withInputStream(inputStream).withPartSize(offset).withPartNumber(1 + partETags.size());
                partETags.add(amazonS3.uploadPart(request).getPartETag());
            } finally {
                Streams.close(inputStream);
            }
            /*
             * append new data to multipart
             */
            DigestInputStream digestStream = null;
            try {
                byte[] buffer = new byte[UPLOAD_BUFFER_SIZE];
                int read;
                digestStream = new DigestInputStream(file, MessageDigest.getInstance("MD5"));
                while (-1 != (read = fillBuffer(digestStream, buffer))) {
                    byte[] digest = digestStream.getMessageDigest().digest();
                    ByteArrayInputStream bais = null;
                    try {
                        bais = new ByteArrayInputStream(buffer, 0, read);
                        UploadPartRequest request = new UploadPartRequest().withBucketName(bucketName).withKey(tempKey).withUploadId(uploadID)
                            .withInputStream(bais).withPartSize(read).withPartNumber(1 + partETags.size()).withMD5Digest(Base64.encode(digest));
                        partETags.add(amazonS3.uploadPart(request).getPartETag());
                    } finally {
                        Streams.close(bais);
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                throw FileStorageCodes.IOERROR.create(e, e.getMessage());
            } catch (IOException e) {
                throw FileStorageCodes.IOERROR.create(e, e.getMessage());
            } finally {
                Streams.close(digestStream);
            }
            /*
             * complete upload
             */
            amazonS3.completeMultipartUpload(new CompleteMultipartUploadRequest(bucketName, tempKey, uploadID, partETags));
            completed = true;
            /*
             * replace old file, cleanup
             */
            amazonS3.copyObject(bucketName, tempKey, bucketName, key);
            amazonS3.deleteObject(bucketName, tempKey);
            return getMetadata(key).getContentLength();
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        } finally {
            if (false == completed) {
                try {
                    amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, tempKey, uploadID));
                } catch (AmazonClientException e) {
                    LOG.warn("Error aborting multipart upload", e);
                }
            }
        }
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
        String key = addPrefix(name);
        String tempKey = generateKey(true);
        try {
            amazonS3.copyObject(bucketName, key, bucketName, tempKey);
            /*
             * upload $length bytes from previous file to new current file
             */
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(length);
            InputStream inputStream = null;
            try {
                inputStream = getFile(tempKey, 0, length);
                amazonS3.putObject(bucketName, key, inputStream, metadata);
            } finally {
                Streams.close(inputStream);
            }
        } catch (AmazonClientException e) {
            throw wrap(e, key);
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
        String key = addPrefix(name);
        GetObjectRequest request = new GetObjectRequest(bucketName, key).withRange(offset, offset + length - 1);
        try {
            return amazonS3.getObject(request).getObjectContent();
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    /**
     * Initiates a new multipart upload for a file with the supplied key.
     *
     * @param key The (full) key for the new file; no additional prefix will be prepended implicitly
     * @return The upload ID for the multipart upload
     * @throws OXException
     */
    private String initiateMultipartUpload(String key) throws OXException {
        try {
            return amazonS3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key)).getUploadId();
        } catch (AmazonClientException e) {
            throw wrap(e, key);
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
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    /**
     * Gets a stored S3 object.
     *
     * @param key The key of the file
     * @return The S3 object
     * @throws OXException
     */
    private S3Object getObject(String key) throws OXException {
        try {
            return amazonS3.getObject(bucketName, key);
        } catch (AmazonClientException e) {
            throw wrap(e, key);
        }
    }

    /**
     * Creates a new arbitrary key (an unformatted string representation of a new random UUID), optionally prepended with the configured
     * prefix and delimiter.
     *
     * @param withPrefix <code>true</code> to prepend the prefix, <code>false</code>, otherwise
     *
     * @return A new UID string, optionally with prefix and delimiter, e.g. <code>[prefix]/067e61623b6f4ae2a1712470b63dff00</code>.
     */
    private String generateKey(boolean withPrefix) {
        String uuid = UUIDs.getUnformattedString(UUID.randomUUID());
        return withPrefix ? prefix + DELIMITER + uuid : uuid;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied name.
     *
     * @param name The name to prepend the prefix
     * @return The name with prefix
     */
    private String addPrefix(String name) {
        return prefix + DELIMITER + name;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied names.
     *
     * @param names The names to prepend the prefix
     * @return The names with prefix in an array
     */
    private String[] addPrefix(Collection<? extends String> names) {
        String[] keys = new String[names.size()];
        int i = 0;
        for (String name : names) {
            keys[i++] = addPrefix(name);
        }
        return keys;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied names.
     *
     * @param names The names to prepend the prefix
     * @return The names with prefix in an array
     */
    private String[] addPrefix(String[] names) {
        String[] keys = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            keys[i] = addPrefix(names[i]);
        }
        return keys;
    }

    /**
     * Strips the prefix and delimiter character sequence to the supplied key.
     *
     * @param key The key to strip the prefix from
     * @return The key without prefix
     */
    private String removePrefix(String key) {
        int idx = prefix.length() + DELIMITER.length();
        if (idx > key.length() || false == key.startsWith(prefix + DELIMITER)) {
            throw new IllegalArgumentException(key);
        }
        return key.substring(idx);
    }

    /**
     * Reads up as many bytes as possible from the supplied input stream into the buffer. Repeated reads are made until either the buffer
     * is filled completely, or the end of the stream is reached.
     *
     * @param inputStream the stream to read from
     * @param buffer The buffer to fill
     * @return The number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has
     *         been reached.
     * @throws IOException
     */
    private static int fillBuffer(InputStream inputStream, byte[] buffer) throws IOException {
        int offset = 0;
        boolean eof = false;
        do {
            int read = inputStream.read(buffer, offset, buffer.length - offset);
            if (-1 == read) {
                eof = true;
            } else {
                offset += read;
            }
        } while (offset < buffer.length && false == eof);
        return eof && 0 == offset ? -1 : offset;
    }

}
