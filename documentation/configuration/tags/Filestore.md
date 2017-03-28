---
title: Filestore
---

This page shows all properties with the tag: Filestore

| __Key__ | com.openexchange.filestore.swift.[filestoreID].protocol |
|:----------------|:--------|
| __Description__ | Specifies the protocol to be used for network communication (http or https)<br>Required.<br> |
| __Default__ | https |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].hosts |
|:----------------|:--------|
| __Description__ | Specifies the API end-point pairs to be used. At least one host must be provided.<br>Multiple hosts can be specified as comma-separated list; e.g. "my1.clouddrive.invalid, my2.clouddrive.invalid"<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].path |
|:----------------|:--------|
| __Description__ | The path consisting of API version and tenant identifier; e.g. "/v1/MyFS_aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].userName |
|:----------------|:--------|
| __Description__ | Specifies the user name to use for authentication.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].tenantName |
|:----------------|:--------|
| __Description__ | Specifies the tenant name to use for authentication.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].authType |
|:----------------|:--------|
| __Description__ | Specifies the authentication type to use for authentication against Identity API v2.0;<br>see (http://developer.openstack.org/api-ref-identity-v2.html)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].authValue |
|:----------------|:--------|
| __Description__ | Specifies the authentication value to use for authentication against Identity API v2.0;<br>see (http://developer.openstack.org/api-ref-identity-v2.html)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].identityUrl |
|:----------------|:--------|
| __Description__ | The URL for the Identity API v2.0 end-point; e.g. "https://identity.api.mycloud.invalid/v2.0/tokens"<br>Not needed in case "authType" is set to "raxkey" (implicitly set to "https://identity.api.rackspacecloud.com/v2.0/tokens").<br>Required (for "authType" different from "raxkey")<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].maxConnections |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with the swift<br>endpoints. If you have specified more than one hosts, this setting should be configured<br>so that maxConnectionsPerHost < maxConnections <= n \* maxConnectionsPerHost.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].maxConnectionsPerHost |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with a certain<br>swift endpoint.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | com.openexchange.filestore.swift.[filestoreID].heartbeatInterval |
|:----------------|:--------|
| __Description__ | Hosts can get blacklisted if the client considers them to be unavailable. All hosts on the<br>blacklist are checked periodically if they are available again and are then removed from the<br>blacklist if so. A host is considered available again if the namespace configuration file<br>(<protocol>://<host>/<path>/.conf) can be requested without any error. This setting specifies<br>the interval in milliseconds between two heartbeat runs. The above specified timeouts must be<br>taken into account for specifying a decent value, as every heartbeat run might block until a<br>timeout happens for every still unavailable host.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-swift.properties |

---
| __Key__ | AVERAGE_USER_SIZE |
|:----------------|:--------|
| __Description__ | The average file storage occupation for a user in MB.<br> |
| __Default__ | 100 |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | AdminUser.properties |

---
| __Key__ | ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET |
|:----------------|:--------|
| __Description__ | Defines whether it is allowed to change the quota value for a user that has no individual file storage set<br><br>If set to "true" and the user has not yet an individual file storage set, an appropriate file storage gets<br>assigned to the user. This implicitly includes to move the user's files from context file storage to that<br>newly assigned file storage, which might be a long operation.<br> |
| __Default__ | false |
| __Version__ | 7.8.3 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Admin.html">Admin</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/User.html">User</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | AdminUser.properties |

---
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
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].protocol |
|:----------------|:--------|
| __Description__ | Specifies the protocol to be used for network communication (http or https)<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].hosts |
|:----------------|:--------|
| __Description__ | Specifies the hosts as <hostname>:<port> pairs to be used for network communication.<br>At least one host must be provided, multiple hosts can be specified as comma-separated<br>list.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].path |
|:----------------|:--------|
| __Description__ | The path under which sproxyd is available. The path must lead to the namespace under<br>which OX related files shall be stored. It is expected that the namespace configuration<br>is available under <protocol>://<host>/<path>/.conf.<br>Required.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].maxConnections |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with the sproxyd<br>endpoints. If you have specified more than one hosts, this setting should be configured<br>so that maxConnectionsPerHost < maxConnections <= n \* maxConnectionsPerHost.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].maxConnectionsPerHost |
|:----------------|:--------|
| __Description__ | The max. number of concurrent HTTP connections that may be established with a certain<br>sproxyd endpoint.<br> |
| __Default__ | 100 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].connectionTimeout |
|:----------------|:--------|
| __Description__ | The connection timeout in milliseconds. If establishing a new HTTP connection to a certain<br>host, it is blacklisted until it is considered available again. A periodic heartbeat task<br>that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 5000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].socketReadTimeout |
|:----------------|:--------|
| __Description__ | The socket read timeout in milliseconds. If waiting for the next expected TCP packet exceeds<br>this value, the host is blacklisted until it is considered available again. A periodic heartbeat<br>task that tries to read the namespace configuration (<protocol>://<host>/<path>/.conf) decides<br>whether an endpoint is considered available again.<br> |
| __Default__ | 15000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

---
| __Key__ | com.openexchange.filestore.sproxyd.[filestoreID].heartbeatInterval |
|:----------------|:--------|
| __Description__ | Hosts can get blacklisted if the client consieders them to be unavailable. All hosts on the<br>blacklist are checked periodically if they are available again and are then removed from the<br>blacklist if so. A host is considered available again if the namespace configuration file<br>(<protocol>://<host>/<path>/.conf) can be requested without any error. This setting specifies<br>the interval in milliseconds between two heartbeat runs. The above specified timeouts must be<br>taken into account for specifying a decent value, as every heartbeat run might block until a<br>timeout happens for every still unavailable host.<br> |
| __Default__ | 60000 |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Filestore.html">Filestore</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Black_List.html">Black List</a> |
| __File__ | filestore-sproxyd.properties |

---
