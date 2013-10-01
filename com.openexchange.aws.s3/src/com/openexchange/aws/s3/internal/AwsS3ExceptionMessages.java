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

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link AwsS3ExceptionMessages} - Exception messages for AWS S3 module that needs to be translated.
 * <p>
 * See <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html">Error Responses</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AwsS3ExceptionMessages implements LocalizableStrings {

    // An error occurred: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "An error occurred: %1$s";

    // An I/O error occurred: %1$s
    public static final String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    // Access denied.
    public static final String AccessDenied_MSG = "Access denied.";

    // There is a problem with your AWS account that prevents the operation from completing successfully.
    public static final String AccountProblem_MSG = "There is a problem with your AWS account that prevents the operation from completing successfully.";

    // The e-mail address you provided is associated with more than one account
    public static final String AmbiguousGrantByEmailAddress_MSG = "The e-mail address you provided is associated with more than one account";

    // The Content-MD5 you specified did not match what we received.
    public static final String BadDigest_MSG = "The Content-MD5 you specified did not match what we received.";

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

    // The versioning configuration is invalid.
    public static final String IllegalVersioningConfigurationException_MSG = "The versioning configuration is invalid.";

    // You did not provide the number of bytes specified by the Content-Length HTTP header
    public static final String IncompleteBody_MSG = "You did not provide the number of bytes specified by the Content-Length HTTP header";

    // POST requires exactly one file upload per request.
    public static final String IncorrectNumberOfFilesInPostRequest_MSG = "POST requires exactly one file upload per request.e.";

    // Inline data exceeds the maximum allowed size.
    public static final String InlineDataTooLarge_MSG = "Inline data exceeds the maximum allowed size.";

    // We encountered an internal error. Please try again.
    public static final String InternalError_MSG = "We encountered an internal error. Please try again.";

    // The AWS Access Key Id you provided does not exist in our records.
    public static final String InvalidAccessKeyId_MSG = "The AWS Access Key Id you provided does not exist in our records.";

    // You must specify the Anonymous role.
    public static final String InvalidAddressingHeader_MSG = "You must specify the Anonymous role.";

    // Invalid argument
    public static final String InvalidArgument_MSG = "Invalid argument";

    // The specified bucket is not valid.
    public static final String InvalidBucketName_MSG = "The specified bucket is not valid.";

    // The request is not valid with the current state of the bucket.
    public static final String InvalidBucketState_MSG = "The request is not valid with the current state of the bucket.";

    // The Content-MD5 you specified was an invalid.
    public static final String InvalidDigest_MSG = "The Content-MD5 you specified was an invalid.";

    // The specified location constraint is not valid.
    public static final String InvalidLocationConstraint_MSG = "The specified location constraint is not valid.";

    // The operation is not valid for the current state of the object.
    public static final String InvalidObjectState_MSG = "The operation is not valid for the current state of the object.";

    // One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity tag might not have matched the part's entity tag.
    public static final String InvalidPart_MSG = "One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity tag might not have matched the part's entity tag.";

    // The list of parts was not in ascending order.Parts list must specified in order by part number.
    public static final String InvalidPartOrder_MSG = "The list of parts was not in ascending order.Parts list must specified in order by part number.";

    // All access to this object has been disabled.
    public static final String InvalidPayer_MSG = "All access to this object has been disabled.";

    // The content of the form does not meet the conditions specified in the policy document.
    public static final String InvalidPolicyDocument_MSG = "The content of the form does not meet the conditions specified in the policy document.";

    // The requested range cannot be satisfied.
    public static final String InvalidRange_MSG = "The requested range cannot be satisfied.";

    // SOAP requests must be made over an HTTPS connection.
    public static final String InvalidRequest_MSG = "SOAP requests must be made over an HTTPS connection.";

    // The provided security credentials are not valid.
    public static final String InvalidSecurity_MSG = "The provided security credentials are not valid.";

    // The SOAP request body is invalid.
    public static final String InvalidSOAPRequest_MSG = "The SOAP request body is invalid.";

    // The storage class you specified is not valid.
    public static final String InvalidStorageClass_MSG = "The storage class you specified is not valid.";

    // The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery group.
    public static final String InvalidTargetBucketForLogging_MSG = "The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery group.";

    // The provided token is malformed or otherwise invalid.
    public static final String InvalidToken_MSG = "The provided token is malformed or otherwise invalid.";

    // Couldn't parse the specified URI.
    public static final String InvalidURI_MSG = "Couldn't parse the specified URI.";

    // Your key is too long.
    public static final String KeyTooLong_MSG = "Your key is too long.";

    // The XML you provided was not well-formed or did not validate against our published schema.
    public static final String MalformedACLError_MSG = "The XML you provided was not well-formed or did not validate against our published schema.";

    // The body of your POST request is not well-formed multipart/form-data.
    public static final String MalformedPOSTRequest_MSG = "The body of your POST request is not well-formed multipart/form-data.";

    // The XML was not well-formed or did not validate against our published schema.
    public static final String MalformedXML_MSG = "The XML was not well-formed or did not validate against our published schema.";

    // Your request was too big.
    public static final String MaxMessageLengthExceeded_MSG = "Your request was too big.";

    // Your POST request fields preceding the upload file were too large.
    public static final String MaxPostPreDataLengthExceededError_MSG = "Your POST request fields preceding the upload file were too large.";

    // Your metadata headers exceed the maximum allowed metadata size.
    public static final String MetadataTooLarge_MSG = "Your metadata headers exceed the maximum allowed metadata size.";

    // The specified method is not allowed against this resource.
    public static final String MethodNotAllowed_MSG = "The specified method is not allowed against this resource.";

    // A SOAP attachment was expected, but none were found.
    public static final String MissingAttachment_MSG = "A SOAP attachment was expected, but none were found.";

    // You must provide the Content-Length HTTP header.
    public static final String MissingContentLength_MSG = "You must provide the Content-Length HTTP header.";

    // Request body is empty.
    public static final String MissingRequestBodyError_MSG = "Request body is empty.";

    // The SOAP 1.1 request is missing a security element.
    public static final String MissingSecurityElement_MSG = "The SOAP 1.1 request is missing a security element.";

    // Your request was missing a required header.
    public static final String MissingSecurityHeader_MSG = "Your request was missing a required header.";

    // There is no such thing as a logging status sub-resource for a key.
    public static final String NoLoggingStatusForKey_MSG = "There is no such thing as a logging status sub-resource for a key.";

    // The specified bucket does not exist.
    public static final String NoSuchBucket_MSG = "The specified bucket does not exist.";

    // The specified key does not exist.
    public static final String NoSuchKey_MSG = "The specified key does not exist.";

    // The lifecycle configuration does not exist.
    public static final String NoSuchLifecycleConfiguration_MSG = "The lifecycle configuration does not exist.";

    // The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or completed.
    public static final String NoSuchUpload_MSG = "The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or completed.";

    // Indicates that the version ID specified in the request does not match an existing version.
    public static final String NoSuchVersion_MSG = "Indicates that the version ID specified in the request does not match an existing version.";

    // A header you provided implies functionality that is not implemented.
    public static final String NotImplemented_MSG = "A header you provided implies functionality that is not implemented.";

    // Your account is not signed up for the Amazon S3 service.
    public static final String NotSignedUp_MSG = "Your account is not signed up for the Amazon S3 service.";

    // The specified bucket does not have a bucket policy.
    public static final String NotSuchBucketPolicy_MSG = "The specified bucket does not have a bucket policy.";

    // A conflicting conditional operation is currently in progress against this resource. Please try again.
    public static final String OperationAborted_MSG = "A conflicting conditional operation is currently in progress against this resource. Please try again.";

    // The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this endpoint.
    public static final String PermanentRedirect_MSG = "The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this endpoint.";

    // At least one of the preconditions you specified did not hold.
    public static final String PreconditionFailed_MSG = "At least one of the preconditions you specified did not hold.";

    // Temporary redirect.
    public static final String Redirect_MSG = "Temporary redirect.";

    // Object restore is already in progress.
    public static final String RestoreAlreadyInProgress_MSG = "Object restore is already in progress.";

    // Bucket POST must be of the enclosure-type multipart/form-data.
    public static final String RequestIsNotMultiPartContent_MSG = "Bucket POST must be of the enclosure-type multipart/form-data.";

    // Your socket connection to the server was not read from or written to within the timeout period.
    public static final String RequestTimeout_MSG = "Your socket connection to the server was not read from or written to within the timeout period.";

    // The difference between the request time and the server's time is too large.
    public static final String RequestTimeTooSkewed_MSG = "The difference between the request time and the server's time is too large.";

    // Requesting the torrent file of a bucket is not permitted.
    public static final String RequestTorrentOfBucketError_MSG = "Requesting the torrent file of a bucket is not permitted.";

    // The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method.
    public static final String SignatureDoesNotMatch_MSG = "The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method.";

    // Please reduce your request rate.
    public static final String ServiceUnavailable_MSG = "Please reduce your request rate.";

    // Please reduce your request rate.
    public static final String SlowDown_MSG = "Please reduce your request rate.";

    // You are being redirected to the bucket while DNS updates.
    public static final String TemporaryRedirect_MSG = "You are being redirected to the bucket while DNS updates.";

    // The provided token must be refreshed.
    public static final String TokenRefreshRequired_MSG = "The provided token must be refreshed.";

    // You have attempted to create more buckets than allowed.
    public static final String TooManyBuckets_MSG = "You have attempted to create more buckets than allowed.";

    // This request does not support content.
    public static final String UnexpectedContent_MSG = "This request does not support content.";

    // The e-mail address you provided does not match any account on record.
    public static final String UnresolvableGrantByEmailAddress_MSG = "The e-mail address you provided does not match any account on record.";

    // The bucket POST must contain the specified field name. If it is specified, please check the order of the fields.
    public static final String UserKeyMustBeSpecified_MSG = "The bucket POST must contain the specified field name. If it is specified, please check the order of the fields.";

    /**
     * Initializes a new {@link AwsS3ExceptionMessages}.
     */
    private AwsS3ExceptionMessages() {
        super();
    }

}
