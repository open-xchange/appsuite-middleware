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

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link AwsS3ExceptionCode} - Enumeration of all {@link OXException}s known in AWS S3 module.
 * <p>
 * See <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html">Error Responses</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum AwsS3ExceptionCode implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(AwsS3ExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(AwsS3ExceptionMessages.IO_ERROR_MSG, CATEGORY_ERROR, 2),
    /**
     * Access denied.
     */
    AccessDenied(AwsS3ExceptionMessages.AccessDenied_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * There is a problem with your AWS account that prevents the operation from completing successfully.
     */
    AccountProblem(AwsS3ExceptionMessages.AccountProblem_MSG, CATEGORY_USER_INPUT, 4),
    /**
     * The e-mail address you provided is associated with more than one account
     */
    AmbiguousGrantByEmailAddress(AwsS3ExceptionMessages.AmbiguousGrantByEmailAddress_MSG, CATEGORY_USER_INPUT, 5),
    /**
     * The Content-MD5 you specified did not match what we received.
     */
    BadDigest(AwsS3ExceptionMessages.BadDigest_MSG, CATEGORY_USER_INPUT, 6),
    /**
     * The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please select a different name
     * and try again.
     */
    BucketAlreadyExists(AwsS3ExceptionMessages.BucketAlreadyExists_MSG, CATEGORY_USER_INPUT, 7),
    /**
     * Your previous request to create the named bucket succeeded and you already own it.
     */
    BucketAlreadyOwnedByYou(AwsS3ExceptionMessages.BucketAlreadyOwnedByYou_MSG, CATEGORY_USER_INPUT, 8),
    /**
     * The bucket you tried to delete is not empty.
     */
    BucketNotEmpty(AwsS3ExceptionMessages.BucketNotEmpty_MSG, CATEGORY_USER_INPUT, 9),
    /**
     * This request does not support credentials.
     */
    CredentialsNotSupported(AwsS3ExceptionMessages.CredentialsNotSupported_MSG, CATEGORY_USER_INPUT, 10),
    /**
     * Cross location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location.
     */
    CrossLocationLoggingProhibited(AwsS3ExceptionMessages.CrossLocationLoggingProhibited_MSG, CATEGORY_USER_INPUT, 11),
    /**
     * Your proposed upload is smaller than the minimum allowed object size.
     */
    EntityTooSmall(AwsS3ExceptionMessages.EntityTooSmall_MSG, CATEGORY_USER_INPUT, 12),
    /**
     * Your proposed upload exceeds the maximum allowed object size.
     */
    EntityTooLarge(AwsS3ExceptionMessages.EntityTooLarge_MSG, CATEGORY_USER_INPUT, 13),
    /**
     * The provided token has expired.
     */
    ExpiredToken(AwsS3ExceptionMessages.ExpiredToken_MSG, CATEGORY_USER_INPUT, 14),
    /**
     * The versioning configuration is invalid.
     */
    IllegalVersioningConfigurationException(AwsS3ExceptionMessages.IllegalVersioningConfigurationException_MSG, CATEGORY_USER_INPUT, 15),
    /**
     * You did not provide the number of bytes specified by the Content-Length HTTP header
     */
    IncompleteBody(AwsS3ExceptionMessages.IncompleteBody_MSG, CATEGORY_USER_INPUT, 16),
    /**
     * POST requires exactly one file upload per request.
     */
    IncorrectNumberOfFilesInPostRequest(AwsS3ExceptionMessages.IncorrectNumberOfFilesInPostRequest_MSG, CATEGORY_USER_INPUT, 17),
    /**
     * Inline data exceeds the maximum allowed size.
     */
    InlineDataTooLarge(AwsS3ExceptionMessages.InlineDataTooLarge_MSG, CATEGORY_USER_INPUT, 18),
    /**
     * We encountered an internal error. Please try again.
     */
    InternalError(AwsS3ExceptionMessages.InternalError_MSG, CATEGORY_USER_INPUT, 19),
    /**
     * The AWS Access Key Id you provided does not exist in our records.
     */
    InvalidAccessKeyId(AwsS3ExceptionMessages.InvalidAccessKeyId_MSG, CATEGORY_USER_INPUT, 20),
    /**
     * You must specify the Anonymous role.
     */
    InvalidAddressingHeader(AwsS3ExceptionMessages.InvalidAddressingHeader_MSG, CATEGORY_USER_INPUT, 21),
    /**
     * Invalid argument
     */
    InvalidArgument(AwsS3ExceptionMessages.InvalidArgument_MSG, CATEGORY_USER_INPUT, 22),
    /**
     * The specified bucket is not valid.
     */
    InvalidBucketName(AwsS3ExceptionMessages.InvalidBucketName_MSG, CATEGORY_USER_INPUT, 23),
    /**
     * The request is not valid with the current state of the bucket.
     */
    InvalidBucketState(AwsS3ExceptionMessages.InvalidBucketState_MSG, CATEGORY_USER_INPUT, 24),
    /**
     * The Content-MD5 you specified was an invalid.
     */
    InvalidDigest(AwsS3ExceptionMessages.InvalidDigest_MSG, CATEGORY_USER_INPUT, 25),
    /**
     * The specified location constraint is not valid.
     */
    InvalidLocationConstraint(AwsS3ExceptionMessages.InvalidLocationConstraint_MSG, CATEGORY_USER_INPUT, 26),
    /**
     * The operation is not valid for the current state of the object.
     */
    InvalidObjectState(AwsS3ExceptionMessages.InvalidObjectState_MSG, CATEGORY_USER_INPUT, 27),
    /**
     * One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity tag might not
     * have matched the part's entity tag.
     */
    InvalidPart(AwsS3ExceptionMessages.InvalidPart_MSG, CATEGORY_USER_INPUT, 28),
    /**
     * The list of parts was not in ascending order.Parts list must specified in order by part number.
     */
    InvalidPartOrder(AwsS3ExceptionMessages.InvalidPartOrder_MSG, CATEGORY_USER_INPUT, 29),
    /**
     * All access to this object has been disabled.
     */
    InvalidPayer(AwsS3ExceptionMessages.InvalidPayer_MSG, CATEGORY_USER_INPUT, 30),
    /**
     * The content of the form does not meet the conditions specified in the policy document.
     */
    InvalidPolicyDocument(AwsS3ExceptionMessages.InvalidPolicyDocument_MSG, CATEGORY_USER_INPUT, 31),
    /**
     * The requested range cannot be satisfied.
     */
    InvalidRange(AwsS3ExceptionMessages.InvalidRange_MSG, CATEGORY_USER_INPUT, 32),
    /**
     * SOAP requests must be made over an HTTPS connection.
     */
    InvalidRequest(AwsS3ExceptionMessages.InvalidRequest_MSG, CATEGORY_USER_INPUT, 33),
    /**
     * The provided security credentials are not valid.
     */
    InvalidSecurity(AwsS3ExceptionMessages.InvalidSecurity_MSG, CATEGORY_USER_INPUT, 34),
    /**
     * The SOAP request body is invalid.
     */
    InvalidSOAPRequest(AwsS3ExceptionMessages.InvalidSOAPRequest_MSG, CATEGORY_USER_INPUT, 35),
    /**
     * The storage class you specified is not valid.
     */
    InvalidStorageClass(AwsS3ExceptionMessages.InvalidStorageClass_MSG, CATEGORY_USER_INPUT, 36),
    /**
     * The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery
     * group.
     */
    InvalidTargetBucketForLogging(AwsS3ExceptionMessages.InvalidTargetBucketForLogging_MSG, CATEGORY_USER_INPUT, 37),
    /**
     * The provided token is malformed or otherwise invalid.
     */
    InvalidToken(AwsS3ExceptionMessages.InvalidToken_MSG, CATEGORY_USER_INPUT, 38),
    /**
     * Couldn't parse the specified URI.
     */
    InvalidURI(AwsS3ExceptionMessages.InvalidURI_MSG, CATEGORY_USER_INPUT, 39),
    /**
     * Your key is too long.
     */
    KeyTooLong(AwsS3ExceptionMessages.KeyTooLong_MSG, CATEGORY_USER_INPUT, 40),
    /**
     * The XML you provided was not well-formed or did not validate against our published schema.
     */
    MalformedACLError(AwsS3ExceptionMessages.MalformedACLError_MSG, CATEGORY_USER_INPUT, 41),
    /**
     * The body of your POST request is not well-formed multipart/form-data.
     */
    MalformedPOSTRequest(AwsS3ExceptionMessages.MalformedPOSTRequest_MSG, CATEGORY_USER_INPUT, 42),
    /**
     * The XML was not well-formed or did not validate against our published schema.
     */
    MalformedXML(AwsS3ExceptionMessages.MalformedXML_MSG, CATEGORY_USER_INPUT, 43),
    /**
     * Your request was too big.
     */
    MaxMessageLengthExceeded(AwsS3ExceptionMessages.MaxMessageLengthExceeded_MSG, CATEGORY_USER_INPUT, 44),
    /**
     * Your POST request fields preceding the upload file were too large.
     */
    MaxPostPreDataLengthExceededError(AwsS3ExceptionMessages.MaxPostPreDataLengthExceededError_MSG, CATEGORY_USER_INPUT, 45),
    /**
     * Your metadata headers exceed the maximum allowed metadata size.
     */
    MetadataTooLarge(AwsS3ExceptionMessages.MetadataTooLarge_MSG, CATEGORY_USER_INPUT, 46),
    /**
     * The specified method is not allowed against this resource.
     */
    MethodNotAllowed(AwsS3ExceptionMessages.MethodNotAllowed_MSG, CATEGORY_USER_INPUT, 47),
    /**
     * A SOAP attachment was expected, but none were found.
     */
    MissingAttachment(AwsS3ExceptionMessages.MissingAttachment_MSG, CATEGORY_USER_INPUT, 48),
    /**
     * You must provide the Content-Length HTTP header.
     */
    MissingContentLength(AwsS3ExceptionMessages.MissingContentLength_MSG, CATEGORY_USER_INPUT, 49),
    /**
     * Request body is empty.
     */
    MissingRequestBodyError(AwsS3ExceptionMessages.MissingRequestBodyError_MSG, CATEGORY_USER_INPUT, 50),
    /**
     * The SOAP 1.1 request is missing a security element.
     */
    MissingSecurityElement(AwsS3ExceptionMessages.MissingSecurityElement_MSG, CATEGORY_USER_INPUT, 51),
    /**
     * Your request was missing a required header.
     */
    MissingSecurityHeader(AwsS3ExceptionMessages.MissingSecurityHeader_MSG, CATEGORY_USER_INPUT, 52),
    /**
     * There is no such thing as a logging status sub-resource for a key.
     */
    NoLoggingStatusForKey(AwsS3ExceptionMessages.NoLoggingStatusForKey_MSG, CATEGORY_USER_INPUT, 53),
    /**
     * The specified bucket does not exist.
     */
    NoSuchBucket(AwsS3ExceptionMessages.NoSuchBucket_MSG, CATEGORY_USER_INPUT, 54),
    /**
     * The specified key does not exist.
     */
    NoSuchKey(AwsS3ExceptionMessages.NoSuchKey_MSG, CATEGORY_USER_INPUT, 55),
    /**
     * The lifecycle configuration does not exist.
     */
    NoSuchLifecycleConfiguration(AwsS3ExceptionMessages.NoSuchLifecycleConfiguration_MSG, CATEGORY_USER_INPUT, 56),
    /**
     * The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or
     * completed.
     */
    NoSuchUpload(AwsS3ExceptionMessages.NoSuchUpload_MSG, CATEGORY_USER_INPUT, 57),
    /**
     * Indicates that the version ID specified in the request does not match an existing version.
     */
    NoSuchVersion(AwsS3ExceptionMessages.NoSuchVersion_MSG, CATEGORY_USER_INPUT, 58),
    /**
     * A header you provided implies functionality that is not implemented.
     */
    NotImplemented(AwsS3ExceptionMessages.NotImplemented_MSG, CATEGORY_USER_INPUT, 59),
    /**
     * Your account is not signed up for the Amazon S3 service.
     */
    NotSignedUp(AwsS3ExceptionMessages.NotSignedUp_MSG, CATEGORY_USER_INPUT, 60),
    /**
     * The specified bucket does not have a bucket policy.
     */
    NotSuchBucketPolicy(AwsS3ExceptionMessages.NotSuchBucketPolicy_MSG, CATEGORY_USER_INPUT, 61),
    /**
     * A conflicting conditional operation is currently in progress against this resource. Please try again.
     */
    OperationAborted(AwsS3ExceptionMessages.OperationAborted_MSG, CATEGORY_USER_INPUT, 62),
    /**
     * The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this
     * endpoint.
     */
    PermanentRedirect(AwsS3ExceptionMessages.PermanentRedirect_MSG, CATEGORY_USER_INPUT, 63),
    /**
     * At least one of the preconditions you specified did not hold.
     */
    PreconditionFailed(AwsS3ExceptionMessages.PreconditionFailed_MSG, CATEGORY_USER_INPUT, 64),
    /**
     * Temporary redirect.
     */
    Redirect(AwsS3ExceptionMessages.Redirect_MSG, CATEGORY_USER_INPUT, 65),
    /**
     * Object restore is already in progress.
     */
    RestoreAlreadyInProgress(AwsS3ExceptionMessages.RestoreAlreadyInProgress_MSG, CATEGORY_USER_INPUT, 66),
    /**
     * Bucket POST must be of the enclosure-type multipart/form-data.
     */
    RequestIsNotMultiPartContent(AwsS3ExceptionMessages.RequestIsNotMultiPartContent_MSG, CATEGORY_USER_INPUT, 67),
    /**
     * Your socket connection to the server was not read from or written to within the timeout period.
     */
    RequestTimeout(AwsS3ExceptionMessages.RequestTimeout_MSG, CATEGORY_USER_INPUT, 68),
    /**
     * The difference between the request time and the server's time is too large.
     */
    RequestTimeTooSkewed(AwsS3ExceptionMessages.RequestTimeTooSkewed_MSG, CATEGORY_USER_INPUT, 69),
    /**
     * Requesting the torrent file of a bucket is not permitted.
     */
    RequestTorrentOfBucketError(AwsS3ExceptionMessages.RequestTorrentOfBucketError_MSG, CATEGORY_USER_INPUT, 70),
    /**
     * The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method.
     */
    SignatureDoesNotMatch(AwsS3ExceptionMessages.SignatureDoesNotMatch_MSG, CATEGORY_USER_INPUT, 71),
    /**
     * Please reduce your request rate.
     */
    ServiceUnavailable(AwsS3ExceptionMessages.ServiceUnavailable_MSG, CATEGORY_USER_INPUT, 72),
    /**
     * Please reduce your request rate.
     */
    SlowDown(AwsS3ExceptionMessages.SlowDown_MSG, CATEGORY_USER_INPUT, 73),
    /**
     * You are being redirected to the bucket while DNS updates.
     */
    TemporaryRedirect(AwsS3ExceptionMessages.TemporaryRedirect_MSG, CATEGORY_USER_INPUT, 74),
    /**
     * The provided token must be refreshed.
     */
    TokenRefreshRequired(AwsS3ExceptionMessages.TokenRefreshRequired_MSG, CATEGORY_USER_INPUT, 75),
    /**
     * You have attempted to create more buckets than allowed.
     */
    TooManyBuckets(AwsS3ExceptionMessages.TooManyBuckets_MSG, CATEGORY_USER_INPUT, 76),
    /**
     * This request does not support content.
     */
    UnexpectedContent(AwsS3ExceptionMessages.UnexpectedContent_MSG, CATEGORY_USER_INPUT, 77),
    /**
     * The e-mail address you provided does not match any account on record.
     */
    UnresolvableGrantByEmailAddress(AwsS3ExceptionMessages.UnresolvableGrantByEmailAddress_MSG, CATEGORY_USER_INPUT, 78),
    /**
     * The bucket POST must contain the specified field name. If it is specified, please check the order of the fields.
     */
    UserKeyMustBeSpecified(AwsS3ExceptionMessages.UserKeyMustBeSpecified_MSG, CATEGORY_USER_INPUT, 79),

    ;

    /**
     * The error code prefix for AWS S3 module.
     */
    public static final String PREFIX = "AWS";

    private static final Map<String, AwsS3ExceptionCode> MAP;
    static {
        final Map<String, AwsS3ExceptionCode> m = new HashMap<String, AwsS3ExceptionCode>(100);
        m.put(toLowerCase("AccessDenied"), AccessDenied);
        m.put(toLowerCase("AccountProblem"), AccountProblem);
        m.put(toLowerCase("AmbiguousGrantByEmailAddress"), AmbiguousGrantByEmailAddress);
        m.put(toLowerCase("BadDigest"), BadDigest);
        m.put(toLowerCase("BucketAlreadyExists"), BucketAlreadyExists);
        m.put(toLowerCase("BucketAlreadyOwnedByYou"), BucketAlreadyOwnedByYou);
        m.put(toLowerCase("BucketNotEmpty"), BucketNotEmpty);
        m.put(toLowerCase("CredentialsNotSupported"), CredentialsNotSupported);
        m.put(toLowerCase("CrossLocationLoggingProhibited"), CrossLocationLoggingProhibited);
        m.put(toLowerCase("EntityTooSmall"), EntityTooSmall);
        m.put(toLowerCase("EntityTooLarge"), EntityTooLarge);
        m.put(toLowerCase("ExpiredToken"), ExpiredToken);
        m.put(toLowerCase("IllegalVersioningConfigurationException"), IllegalVersioningConfigurationException);
        m.put(toLowerCase("IncompleteBody"), IncompleteBody);
        m.put(toLowerCase("IncorrectNumberOfFilesInPostRequest"), IncorrectNumberOfFilesInPostRequest);
        m.put(toLowerCase("InlineDataTooLarge"), InlineDataTooLarge);
        m.put(toLowerCase("InternalError"), InternalError);
        m.put(toLowerCase("InvalidAccessKeyId"), InvalidAccessKeyId);
        m.put(toLowerCase("InvalidAddressingHeader"), InvalidAddressingHeader);
        m.put(toLowerCase("InvalidArgument"), InvalidArgument);
        m.put(toLowerCase("InvalidBucketName"), InvalidBucketName);
        m.put(toLowerCase("InvalidBucketState"), InvalidBucketState);
        m.put(toLowerCase("InvalidDigest"), InvalidDigest);
        m.put(toLowerCase("InvalidLocationConstraint"), InvalidLocationConstraint);
        m.put(toLowerCase("InvalidObjectState"), InvalidObjectState);
        m.put(toLowerCase("InvalidPart"), InvalidPart);
        m.put(toLowerCase("InvalidPartOrder"), InvalidPartOrder);
        m.put(toLowerCase("InvalidPayer"), InvalidPayer);
        m.put(toLowerCase("InvalidPolicyDocument"), InvalidPolicyDocument);
        m.put(toLowerCase("InvalidRange"), InvalidRange);
        m.put(toLowerCase("InvalidRequest"), InvalidRequest);
        m.put(toLowerCase("InvalidSecurity"), InvalidSecurity);
        m.put(toLowerCase("InvalidSOAPRequest"), InvalidSOAPRequest);
        m.put(toLowerCase("InvalidStorageClass"), InvalidStorageClass);
        m.put(toLowerCase("InvalidTargetBucketForLogging"), InvalidTargetBucketForLogging);
        m.put(toLowerCase("InvalidToken"), InvalidToken);
        m.put(toLowerCase("InvalidURI"), InvalidURI);
        m.put(toLowerCase("KeyTooLong"), KeyTooLong);
        m.put(toLowerCase("MalformedACLError"), MalformedACLError);
        m.put(toLowerCase("MalformedPOSTRequest"), MalformedPOSTRequest);
        m.put(toLowerCase("MalformedXML"), MalformedXML);
        m.put(toLowerCase("MaxMessageLengthExceeded"), MaxMessageLengthExceeded);
        m.put(toLowerCase("MaxPostPreDataLengthExceededError"), MaxPostPreDataLengthExceededError);
        m.put(toLowerCase("MetadataTooLarge"), MetadataTooLarge);
        m.put(toLowerCase("MethodNotAllowed"), MethodNotAllowed);
        m.put(toLowerCase("MissingAttachment"), MissingAttachment);
        m.put(toLowerCase("MissingContentLength"), MissingContentLength);
        m.put(toLowerCase("MissingRequestBodyError"), MissingRequestBodyError);
        m.put(toLowerCase("MissingSecurityElement"), MissingSecurityElement);
        m.put(toLowerCase("MissingSecurityHeader"), MissingSecurityHeader);
        m.put(toLowerCase("NoLoggingStatusForKey"), NoLoggingStatusForKey);
        m.put(toLowerCase("NoSuchBucket"), NoSuchBucket);
        m.put(toLowerCase("NoSuchKey"), NoSuchKey);
        m.put(toLowerCase("NoSuchLifecycleConfiguration"), NoSuchLifecycleConfiguration);
        m.put(toLowerCase("NoSuchUpload"), NoSuchUpload);
        m.put(toLowerCase("NoSuchVersion"), NoSuchVersion);
        m.put(toLowerCase("NotImplemented"), NotImplemented);
        m.put(toLowerCase("NotSignedUp"), NotSignedUp);
        m.put(toLowerCase("NotSuchBucketPolicy"), NotSuchBucketPolicy);
        m.put(toLowerCase("OperationAborted"), OperationAborted);
        m.put(toLowerCase("PermanentRedirect"), PermanentRedirect);
        m.put(toLowerCase("PreconditionFailed"), PreconditionFailed);
        m.put(toLowerCase("Redirect"), Redirect);
        m.put(toLowerCase("RestoreAlreadyInProgress"), RestoreAlreadyInProgress);
        m.put(toLowerCase("RequestIsNotMultiPartContent"), RequestIsNotMultiPartContent);
        m.put(toLowerCase("RequestTimeout"), RequestTimeout);
        m.put(toLowerCase("RequestTimeTooSkewed"), RequestTimeTooSkewed);
        m.put(toLowerCase("RequestTorrentOfBucketError"), RequestTorrentOfBucketError);
        m.put(toLowerCase("SignatureDoesNotMatch"), SignatureDoesNotMatch);
        m.put(toLowerCase("ServiceUnavailable"), ServiceUnavailable);
        m.put(toLowerCase("SlowDown"), SlowDown);
        m.put(toLowerCase("TemporaryRedirect"), TemporaryRedirect);
        m.put(toLowerCase("TokenRefreshRequired"), TokenRefreshRequired);
        m.put(toLowerCase("TooManyBuckets"), TooManyBuckets);
        m.put(toLowerCase("UnexpectedContent"), UnexpectedContent);
        m.put(toLowerCase("UnresolvableGrantByEmailAddress"), UnresolvableGrantByEmailAddress);
        m.put(toLowerCase("UserKeyMustBeSpecified"), UserKeyMustBeSpecified);
        MAP = Collections.unmodifiableMap(m);
    }

    /**
     * Gets the appropriate S3 error code for specified error code identifier
     *
     * @param errorCode The error code identifier
     * @return The S3 error code or <code>null</code>
     */
    public static AwsS3ExceptionCode getCodeFor(final String errorCode) {
        if (isEmpty(errorCode)) {
            return null;
        }
        return MAP.get(toLowerCase(errorCode));
    }

    private final Category category;

    private final int detailNumber;

    private final String message;

    private AwsS3ExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
