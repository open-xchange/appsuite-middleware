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

package com.openexchange.filestore.s3.internal;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link S3ExceptionMessages} - Exception messages for S3 module that needs to be translated.
 * <p>
 * See <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html">Error Responses</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class S3ExceptionMessages implements LocalizableStrings {

    // Access denied.
    public static final String AccessDenied_MSG = "Access denied.";

    // There is a problem with your AWS account that prevents the operation from completing successfully.
    public static final String AccountProblem_MSG = "There is a problem with your AWS account that prevents the operation from completing successfully.";

    // The e-mail address you provided is associated with more than one account
    public static final String AmbiguousGrantByEmailAddress_MSG = "The e-mail address you provided is associated with more than one account";

    // The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please select a different name and try again.
    public static final String BucketAlreadyExists_MSG = "The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please select a different name and try again.";

    // Your previous request to create the named bucket succeeded and you already own it.
    public static final String BucketAlreadyOwnedByYou_MSG = "Your previous request to create the named bucket succeeded and you already own it.";

    // The bucket you tried to delete is not empty.
    public static final String BucketNotEmpty_MSG = "The bucket you tried to delete is not empty.";

    // This request does not support credentials.
    public static final String CredentialsNotSupported_MSG = "This request does not support credentials.";

    // Cross location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location.
    public static final String CrossLocationLoggingProhibited_MSG = "Cross location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location.";

    // Your proposed upload is smaller than the minimum allowed object size.
    public static final String EntityTooSmall_MSG = "Your proposed upload is smaller than the minimum allowed object size.";

    // Your proposed upload exceeds the maximum allowed object size.
    public static final String EntityTooLarge_MSG = "Your proposed upload exceeds the maximum allowed object size.";

    // The provided token has expired.
    public static final String ExpiredToken_MSG = "The provided token has expired.";

    // The AWS Access Key Id you provided does not exist in our records.
    public static final String InvalidAccessKeyId_MSG = "The AWS Access Key Id you provided does not exist in our records.";

    // The specified bucket is not valid.
    public static final String InvalidBucketName_MSG = "The specified bucket is not valid.";

    // All access to this object has been disabled.
    public static final String InvalidPayer_MSG = "All access to this object has been disabled.";

    // The provided security credentials are not valid.
    public static final String InvalidSecurity_MSG = "The provided security credentials are not valid.";

    // The provided token is malformed or otherwise invalid.
    public static final String InvalidToken_MSG = "The provided token is malformed or otherwise invalid.";

    // Couldn't parse the specified URI.
    public static final String InvalidURI_MSG = "Couldn't parse the specified URI.";

    // Your key is too long.
    public static final String KeyTooLong_MSG = "Your key is too long.";

    // Your request was too big.
    public static final String MaxMessageLengthExceeded_MSG = "Your request was too big.";

    // The specified method is not allowed against this resource.
    public static final String MethodNotAllowed_MSG = "The specified method is not allowed against this resource.";

    // The specified bucket does not exist.
    public static final String NoSuchBucket_MSG = "The specified bucket does not exist.";

    public static final String NoSuchKey_MSG = "The specified key does not exist.";

    // Your account is not signed up for the Amazon S3 service.
    public static final String NotSignedUp_MSG = "Your account is not signed up for the Amazon S3 service.";

    // A conflicting conditional operation is currently in progress against this resource. Please try again.
    public static final String OperationAborted_MSG = "A conflicting conditional operation is currently in progress against this resource. Please try again.";

    // At least one of the preconditions you specified did not hold.
    public static final String PreconditionFailed_MSG = "At least one of the preconditions you specified did not hold.";

    // Temporary redirect.
    public static final String Redirect_MSG = "Temporary redirect.";

    // Object restore is already in progress.
    public static final String RestoreAlreadyInProgress_MSG = "Object restore is already in progress.";

    // You are being redirected to the bucket while DNS updates.
    public static final String TemporaryRedirect_MSG = "You are being redirected to the bucket while DNS updates.";

    // The provided token must be refreshed.
    public static final String TokenRefreshRequired_MSG = "The provided token must be refreshed.";

    // The e-mail address you provided does not match any account on record.
    public static final String UnresolvableGrantByEmailAddress_MSG = "The e-mail address you provided does not match any account on record.";

    // The S3 storage responds with \"Bad Request\". Please check whether com.openexchange.filestore.s3.[filestoreID].signerOverride is properly configured for this filestore.
    public static final String BadRequest_MSG = "The S3 storage responds with \"Bad Request\". Please check whether com.openexchange.filestore.s3.[filestoreID].signerOverride is properly configured for this filestore.";

    // Failed to create a bucket for name "%1$s" using region "%2$s"
    public static final String BUCKET_CREATION_FAILED = "Failed to create a bucket for name \"%1$s\" using region \"%2$s\"";

    /**
     * Initializes a new {@link S3ExceptionMessages}.
     */
    private S3ExceptionMessages() {
        super();
    }

}
