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

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.filestore.FileStorageCodes;

/**
 * {@link S3ExceptionCode} - Enumeration of all {@link OXException}s known in S3 module.
 * <p>
 * See <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html">Error Responses</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum S3ExceptionCode implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", CATEGORY_ERROR, 2),
    /**
     * Access denied.
     */
    AccessDenied("Access denied.", CATEGORY_USER_INPUT, 3, S3ExceptionMessages.AccessDenied_MSG),
    /**
     * There is a problem with your AWS account that prevents the operation from completing successfully.
     */
    AccountProblem("There is a problem with your AWS account that prevents the operation from completing successfully.",
        CATEGORY_USER_INPUT, 4, S3ExceptionMessages.AccountProblem_MSG),
    /**
     * The e-mail address you provided is associated with more than one account
     */
    AmbiguousGrantByEmailAddress("The e-mail address you provided is associated with more than one account", CATEGORY_USER_INPUT, 5,
        S3ExceptionMessages.AmbiguousGrantByEmailAddress_MSG),
    /**
     * The Content-MD5 you specified did not match what we received.
     */
    BadDigest("The Content-MD5 you specified did not match what we received.", CATEGORY_USER_INPUT, 6),
    /**
     * The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please select a different name
     * and try again.
     */
    BucketAlreadyExists("The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please"
        + " select a different name and try again.", CATEGORY_USER_INPUT, 7, S3ExceptionMessages.BucketAlreadyExists_MSG),
    /**
     * Your previous request to create the named bucket succeeded and you already own it.
     */
    BucketAlreadyOwnedByYou("Your previous request to create the named bucket succeeded and you already own it.", CATEGORY_USER_INPUT, 8,
        S3ExceptionMessages.BucketAlreadyOwnedByYou_MSG),
    /**
     * The bucket you tried to delete is not empty.
     */
    BucketNotEmpty("The bucket you tried to delete is not empty.", CATEGORY_USER_INPUT, 9, S3ExceptionMessages.BucketNotEmpty_MSG),
    /**
     * This request does not support credentials.
     */
    CredentialsNotSupported("This request does not support credentials.", CATEGORY_USER_INPUT, 10,
        S3ExceptionMessages.CredentialsNotSupported_MSG),
    /**
     * Cross location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location.
     */
    CrossLocationLoggingProhibited("Cross location logging not allowed. Buckets in one geographic location cannot log information to a "
        + "bucket in another location.", CATEGORY_USER_INPUT, 11, S3ExceptionMessages.CrossLocationLoggingProhibited_MSG),
    /**
     * Your proposed upload is smaller than the minimum allowed object size.
     */
    EntityTooSmall("Your proposed upload is smaller than the minimum allowed object size.", CATEGORY_USER_INPUT, 12,
        S3ExceptionMessages.EntityTooSmall_MSG),
    /**
     * Your proposed upload exceeds the maximum allowed object size.
     */
    EntityTooLarge("Your proposed upload exceeds the maximum allowed object size.", CATEGORY_USER_INPUT, 13,
        S3ExceptionMessages.EntityTooLarge_MSG),
    /**
     * The provided token has expired.
     */
    ExpiredToken("The provided token has expired.", CATEGORY_USER_INPUT, 14, S3ExceptionMessages.ExpiredToken_MSG),
    /**
     * The versioning configuration is invalid.
     */
    IllegalVersioningConfigurationException("The versioning configuration is invalid.", CATEGORY_ERROR, 15),
    /**
     * You did not provide the number of bytes specified by the Content-Length HTTP header
     */
    IncompleteBody("You did not provide the number of bytes specified by the Content-Length HTTP header", CATEGORY_ERROR, 16),
    /**
     * POST requires exactly one file upload per request.
     */
    IncorrectNumberOfFilesInPostRequest("POST requires exactly one file upload per request.e.", CATEGORY_ERROR, 17),
    /**
     * Inline data exceeds the maximum allowed size.
     */
    InlineDataTooLarge("Inline data exceeds the maximum allowed size.", CATEGORY_ERROR, 18),
    /**
     * We encountered an internal error. Please try again.
     */
    InternalError("We encountered an internal error. Please try again.", CATEGORY_ERROR, 19),
    /**
     * The AWS Access Key Id you provided does not exist in our records.
     */
    InvalidAccessKeyId("The AWS Access Key Id you provided does not exist in our records.", CATEGORY_CONFIGURATION, 20,
        S3ExceptionMessages.InvalidAccessKeyId_MSG),
    /**
     * You must specify the Anonymous role.
     */
    InvalidAddressingHeader("You must specify the Anonymous role.", CATEGORY_ERROR, 21),
    /**
     * Invalid argument
     */
    InvalidArgument("Invalid argument", CATEGORY_ERROR, 22),
    /**
     * The specified bucket is not valid.
     */
    InvalidBucketName("The specified bucket is not valid.", CATEGORY_USER_INPUT, 23, S3ExceptionMessages.InvalidBucketName_MSG),
    /**
     * The request is not valid with the current state of the bucket.
     */
    InvalidBucketState("The request is not valid with the current state of the bucket.", CATEGORY_ERROR, 24),
    /**
     * The Content-MD5 you specified was an invalid.
     */
    InvalidDigest("The Content-MD5 you specified was an invalid.", CATEGORY_ERROR, 25),
    /**
     * The specified location constraint is not valid.
     */
    InvalidLocationConstraint("The specified location constraint is not valid.", CATEGORY_ERROR, 26),
    /**
     * The operation is not valid for the current state of the object.
     */
    InvalidObjectState("The operation is not valid for the current state of the object.", CATEGORY_ERROR, 27),
    /**
     * One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity tag might not
     * have matched the part's entity tag.
     */
    InvalidPart("One or more of the specified parts could not be found. The part might not have been uploaded, or the specified entity"
        + " tag might not have matched the part's entity tag.", CATEGORY_ERROR, 28),
    /**
     * The list of parts was not in ascending order.Parts list must specified in order by part number.
     */
    InvalidPartOrder("The list of parts was not in ascending order.Parts list must specified in order by part number.", CATEGORY_ERROR, 29),
    /**
     * All access to this object has been disabled.
     */
    InvalidPayer("All access to this object has been disabled.", CATEGORY_PERMISSION_DENIED, 30, S3ExceptionMessages.InvalidPayer_MSG),
    /**
     * The content of the form does not meet the conditions specified in the policy document.
     */
    InvalidPolicyDocument("The content of the form does not meet the conditions specified in the policy document.", CATEGORY_ERROR, 31),
    /**
     * The requested range cannot be satisfied.
     */
    InvalidRange("The requested range cannot be satisfied.", CATEGORY_ERROR, 32),
    /**
     * SOAP requests must be made over an HTTPS connection.
     */
    InvalidRequest("SOAP requests must be made over an HTTPS connection.", CATEGORY_ERROR, 33),
    /**
     * The provided security credentials are not valid.
     */
    InvalidSecurity("The provided security credentials are not valid.", CATEGORY_CONFIGURATION, 34, S3ExceptionMessages.InvalidSecurity_MSG),
    /**
     * The SOAP request body is invalid.
     */
    InvalidSOAPRequest("The SOAP request body is invalid.", CATEGORY_ERROR, 35),
    /**
     * The storage class you specified is not valid.
     */
    InvalidStorageClass("The storage class you specified is not valid.", CATEGORY_ERROR, 36),
    /**
     * The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery
     * group.
     */
    InvalidTargetBucketForLogging("The target bucket for logging does not exist, is not owned by you, or does not have the appropriate"
        + " grants for the log-delivery group.", CATEGORY_ERROR, 37),
    /**
     * The provided token is malformed or otherwise invalid.
     */
    InvalidToken("The provided token is malformed or otherwise invalid.", CATEGORY_ERROR, 38, S3ExceptionMessages.InvalidToken_MSG),
    /**
     * Couldn't parse the specified URI.
     */
    InvalidURI("Couldn't parse the specified URI.", CATEGORY_USER_INPUT, 39, S3ExceptionMessages.InvalidURI_MSG),
    /**
     * Your key is too long.
     */
    KeyTooLong("Your key is too long.", CATEGORY_USER_INPUT, 40, S3ExceptionMessages.KeyTooLong_MSG),
    /**
     * The XML you provided was not well-formed or did not validate against our published schema.
     */
    MalformedACLError("The XML you provided was not well-formed or did not validate against our published schema.", CATEGORY_ERROR, 41),
    /**
     * The body of your POST request is not well-formed multipart/form-data.
     */
    MalformedPOSTRequest("The body of your POST request is not well-formed multipart/form-data.", CATEGORY_ERROR, 42),
    /**
     * The XML was not well-formed or did not validate against our published schema.
     */
    MalformedXML("The XML was not well-formed or did not validate against our published schema.", CATEGORY_ERROR, 43),
    /**
     * Your request was too big.
     */
    MaxMessageLengthExceeded("Your request was too big.", CATEGORY_USER_INPUT, 44, S3ExceptionMessages.MaxMessageLengthExceeded_MSG),
    /**
     * Your POST request fields preceding the upload file were too large.
     */
    MaxPostPreDataLengthExceededError("Your POST request fields preceding the upload file were too large.", CATEGORY_ERROR, 45),
    /**
     * Your metadata headers exceed the maximum allowed metadata size.
     */
    MetadataTooLarge("Your metadata headers exceed the maximum allowed metadata size.", CATEGORY_ERROR, 46),
    /**
     * The specified method is not allowed against this resource.
     */
    MethodNotAllowed("The specified method is not allowed against this resource.", CATEGORY_USER_INPUT, 47,
        S3ExceptionMessages.MethodNotAllowed_MSG),
    /**
     * A SOAP attachment was expected, but none were found.
     */
    MissingAttachment("A SOAP attachment was expected, but none were found.", CATEGORY_ERROR, 48),
    /**
     * You must provide the Content-Length HTTP header.
     */
    MissingContentLength("You must provide the Content-Length HTTP header.", CATEGORY_ERROR, 49),
    /**
     * Request body is empty.
     */
    MissingRequestBodyError("Request body is empty.", CATEGORY_ERROR, 50),
    /**
     * The SOAP 1.1 request is missing a security element.
     */
    MissingSecurityElement("The SOAP 1.1 request is missing a security element.", CATEGORY_ERROR, 51),
    /**
     * Your request was missing a required header.
     */
    MissingSecurityHeader("Your request was missing a required header.", CATEGORY_ERROR, 52),
    /**
     * There is no such thing as a logging status sub-resource for a key.
     */
    NoLoggingStatusForKey("There is no such thing as a logging status sub-resource for a key.", CATEGORY_ERROR, 53),
    /**
     * The specified bucket does not exist.
     */
    NoSuchBucket("The specified bucket does not exist.", CATEGORY_USER_INPUT, 54, S3ExceptionMessages.NoSuchBucket_MSG),
    /**
     * The specified key does not exist.
     */
    NoSuchKey("The specified key does not exist.", CATEGORY_USER_INPUT, 55, S3ExceptionMessages.NoSuchKey_MSG),
    /**
     * The lifecycle configuration does not exist.
     */
    NoSuchLifecycleConfiguration("The lifecycle configuration does not exist.", CATEGORY_ERROR, 56),
    /**
     * The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been aborted or
     * completed.
     */
    NoSuchUpload("The specified multipart upload does not exist. The upload ID might be invalid, or the multipart upload might have been"
        + " aborted or completed.", CATEGORY_ERROR, 57),
    /**
     * Indicates that the version ID specified in the request does not match an existing version.
     */
    NoSuchVersion("Indicates that the version ID specified in the request does not match an existing version.", CATEGORY_ERROR, 58),
    /**
     * A header you provided implies functionality that is not implemented.
     */
    NotImplemented("A header you provided implies functionality that is not implemented.", CATEGORY_ERROR, 59),
    /**
     * Your account is not signed up for the Amazon S3 service.
     */
    NotSignedUp("Your account is not signed up for the Amazon S3 service.", CATEGORY_USER_INPUT, 60, S3ExceptionMessages.NotSignedUp_MSG),
    /**
     * The specified bucket does not have a bucket policy.
     */
    NotSuchBucketPolicy("The specified bucket does not have a bucket policy.", CATEGORY_ERROR, 61),
    /**
     * A conflicting conditional operation is currently in progress against this resource. Please try again.
     */
    OperationAborted("A conflicting conditional operation is currently in progress against this resource. Please try again.",
        CATEGORY_ERROR, 62, S3ExceptionMessages.OperationAborted_MSG),
    /**
     * The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this
     * endpoint.
     */
    PermanentRedirect("The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future"
        + " requests to this endpoint.", CATEGORY_ERROR, 63),
    /**
     * At least one of the preconditions you specified did not hold.
     */
    PreconditionFailed("At least one of the preconditions you specified did not hold.",
        CATEGORY_USER_INPUT, 64, S3ExceptionMessages.PreconditionFailed_MSG),
    /**
     * Temporary redirect.
     */
    Redirect("Temporary redirect.", CATEGORY_ERROR, 65, S3ExceptionMessages.Redirect_MSG),
    /**
     * Object restore is already in progress.
     */
    RestoreAlreadyInProgress("Object restore is already in progress.", CATEGORY_ERROR, 66, S3ExceptionMessages.RestoreAlreadyInProgress_MSG),
    /**
     * Bucket POST must be of the enclosure-type multipart/form-data.
     */
    RequestIsNotMultiPartContent("Bucket POST must be of the enclosure-type multipart/form-data.", CATEGORY_ERROR, 67),
    /**
     * Your socket connection to the server was not read from or written to within the timeout period.
     */
    RequestTimeout("Your socket connection to the server was not read from or written to within the timeout period.", CATEGORY_ERROR, 68),
    /**
     * The difference between the request time and the server's time is too large.
     */
    RequestTimeTooSkewed("The difference between the request time and the server's time is too large.", CATEGORY_ERROR, 69),
    /**
     * Requesting the torrent file of a bucket is not permitted.
     */
    RequestTorrentOfBucketError("Requesting the torrent file of a bucket is not permitted.", CATEGORY_ERROR, 70),
    /**
     * The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method.
     */
    SignatureDoesNotMatch("The request signature we calculated does not match the signature you provided. Check your AWS Secret Access "
        + "Key and signing method.", CATEGORY_CONFIGURATION, 71),
    /**
     * Please reduce your request rate.
     */
    ServiceUnavailable("Please reduce your request rate.", CATEGORY_ERROR, 72),
    /**
     * Please reduce your request rate.
     */
    SlowDown("Please reduce your request rate.", CATEGORY_ERROR, 73),
    /**
     * You are being redirected to the bucket while DNS updates.
     */
    TemporaryRedirect("You are being redirected to the bucket while DNS updates.", CATEGORY_WARNING, 74,
        S3ExceptionMessages.TemporaryRedirect_MSG),
    /**
     * The provided token must be refreshed.
     */
    TokenRefreshRequired("The provided token must be refreshed.", CATEGORY_TRY_AGAIN, 75, S3ExceptionMessages.TokenRefreshRequired_MSG),
    /**
     * You have attempted to create more buckets than allowed.
     */
    TooManyBuckets("You have attempted to create more buckets than allowed.", CATEGORY_ERROR, 76),
    /**
     * This request does not support content.
     */
    UnexpectedContent("This request does not support content.", CATEGORY_ERROR, 77),
    /**
     * The e-mail address you provided does not match any account on record.
     */
    UnresolvableGrantByEmailAddress("The e-mail address you provided does not match any account on record.", CATEGORY_CONFIGURATION, 78,
        S3ExceptionMessages.UnresolvableGrantByEmailAddress_MSG),
    /**
     * The bucket POST must contain the specified field name. If it is specified, please check the order of the fields.
     */
    UserKeyMustBeSpecified("The bucket POST must contain the specified field name. If it is specified, please check the order of the"
        + " fields.", CATEGORY_ERROR, 79),

    /**
     * The S3 storage responds with \"Bad Request\". Please check whether com.openexchange.filestore.s3.[filestoreID].signerOverride is properly configured for this filestore.
     */
    BadRequest(S3ExceptionMessages.BadRequest_MSG, CATEGORY_ERROR, 80),

    ;

    /**
     * The error code prefix for S3 module.
     */
    public static final String PREFIX = "S3F";

    private static final Map<String, S3ExceptionCode> MAP;
    static {
        final Map<String, S3ExceptionCode> m = new HashMap<String, S3ExceptionCode>(100);
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
    public static S3ExceptionCode getCodeFor(final String errorCode) {
        if (isEmpty(errorCode)) {
            return null;
        }
        return MAP.get(toLowerCase(errorCode));
    }

    /**
     * Wraps given Amazon S3 exception with an appropriate error code.
     *
     * @param e The Amazon S3 exception
     * @return The wrapping error code
     */
    public static OXException wrap(final AmazonClientException e) {
        return wrap(e, null);
    }

    /**
     * Wraps given Amazon S3 exception with an appropriate error code.
     *
     * @param e The Amazon S3 exception
     * @param key The key that was accessed by the previous operation
     * @return The wrapping error code
     */
    public static OXException wrap(final AmazonClientException e, String key) {
        if (AmazonServiceException.class.isInstance(e)) {
            final AmazonServiceException serviceError = (AmazonServiceException) e;
            /*
             * Map to appropriate FileStorageCodes if possible
             */
            if (HttpServletResponse.SC_NOT_FOUND == serviceError.getStatusCode()) {
                return FileStorageCodes.FILE_NOT_FOUND.create(e, key);
            }
            // Get the error code
            final String errorCode = serviceError.getErrorCode();
            final S3ExceptionCode code = S3ExceptionCode.getCodeFor(errorCode);
            if (null != code) {
                return code.create(e, new Object[0]);
            }
        }
        return FileStorageCodes.IOERROR.create(e, e.getMessage());
    }

    private final Category category;

    private final int detailNumber;

    private final String message;

    private final String displayMessage;

    private S3ExceptionCode(final String message, final Category category, final int detailNumber, String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    private S3ExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = OXExceptionStrings.MESSAGE;
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
    public String getDisplayMessage() {
        return displayMessage;
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
