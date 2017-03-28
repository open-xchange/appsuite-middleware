---
title: Filestore S3
---

This file provides the configuration of all file storages based on the
Amazon Simple Storage Service (Amazon S3). Each connected storage is
identified by a so called "filestore ID", which refers to the authority
part of the URI configured in the "uri" column in the "filestore" table of
the config database, previously registered using
"./registerfilestore -t [filestoreID]".
For each configured filestore, an own set of the properties may be defined,
replacing [filestoreID] with the actual identifier.


| __Key__ | com.openexchange.filestore.s3.[filestoreID].endpoint |
|:----------------|:--------|
| __Description__ | Specifies the endpoint (e.g. "ec2.amazonaws.com") or a full URL, including<br>the protocol (e.g. "https://ec2.amazonaws.com") of the region specific AWS<br>endpoint this client will communicate with.<br> |
| __Default__ | s3.amazonaws.com |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].bucketName |
|:----------------|:--------|
| __Description__ | Specifies the name of the parent bucket to use. The bucket will be created<br>automatically if it not yet exists, however, it's still possible to use an<br>already existing one. There are some naming restrictions, please refer to<br>http://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html for<br>details. Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].region |
|:----------------|:--------|
| __Description__ | Configures the Amazon S3 region to use when creating new buckets. This value<br>is also used to pre-configure the client when no specific endpoint is set.<br>Possible values are "us-gov-west-1", "us-east-1", "us-west-1", "us-west-2",<br>"eu-west-1", "ap-southeast-1", "ap-southeast-2", "ap-northeast-1" and<br>"sa-east-1".<br> |
| __Default__ | us-west-2 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].pathStyleAccess |
|:----------------|:--------|
| __Description__ | Defines if path-style-access should be used when accessing the S3 API. If<br>not set to "true", virtual-hosted-style access is used. Please refer to<br>http://docs.aws.amazon.com/AmazonS3/latest/dev/VirtualHosting.html for<br>details.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].accessKey |
|:----------------|:--------|
| __Description__ | Configures the AWS access key to use. Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].secretKey |
|:----------------|:--------|
| __Description__ | Configures the AWS secret key to use. Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].encryption |
|:----------------|:--------|
| __Description__ | Optionally specifies which client-side encryption should be used. Current<br>options include "none" for no encryption, or "rsa" for an RSA-based asymmetric<br>encryption.<br>Please note that depending on the used key length and Java runtime, one<br>might need to replace the so-called "JCE Unlimited Strength Jurisdiction<br>Policy Files" accordingly.<br> |
| __Default__ | none |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].encryption.rsa.keyStore |
|:----------------|:--------|
| __Description__ | Specifies the path to the local keystore file (PKCS #12) containing the<br>public-/private-key pair to use for encryption, e.g.<br>"/opt/open-xchange/etc/cert/awss3.p12". Required if<br>"com.openexchange.filestore.s3.[filestoreID].encryption" is set to "rsa".<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].encryption.rsa.password |
|:----------------|:--------|
| __Description__ | Specifies the password used when creating the referenced keystore containing<br>public-/private-key pair to use for encryption. Note that blank or null<br>passwords are in violation of the PKCS #12 specifications. Required if<br>"com.openexchange.filestore.s3.[filestoreID].encryption" is set to "rsa".<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Credential.html">Credential</a> |
| __File__ | filestore-s3.properties |

---
| __Key__ | com.openexchange.filestore.s3.[filestoreID].signerOverride |
|:----------------|:--------|
| __Description__ | Optionally configures an override for the algorithm used to sign requests<br>against the S3 interface. If left empty, the underlying client will choose a<br>suitable signer type based on the actually used service and region<br>automatically, however, auto-detection only works properly for the "vanilla"<br>Amazon S3, and may lead to unwanted results when targeting an S3<br>implementation where not all signature types are supported.<br>Possible values include "S3SignerType" to enforce the AWS signature v2, and<br>"AWSS3V4SignerType" for AWS signature v4.<br>For backwards compatibility, the setting defaults to "S3SignerType", and<br>needs to be adjusted explicitly when targeting the newer "v4-only" regions<br>like Frankfurt or Beijing.<br> |
| __Default__ | S3SignerType |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-s3.properties |

---
