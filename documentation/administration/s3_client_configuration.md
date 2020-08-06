---
title: S3 Client Configuration
icon: fa-archive
tags: Administration, Filestore, Configuration, S3
---

Up to and including appsuite middleware version 7.10.3 the middleware created one s3 client for every filestore which is a problem in several respects. 
On the one hand, it is not possible to monitor the clients properly because multiple clients use the same endpoint. And on the other hand some limits 
like the maximum number of sockets are ineffective because of the same reason. Additionally the configuration effort for systems with a lot of filestores is tremendous.
It was therefore decided to introduce a new configuration system for s3 clients with appsuite middleware 7.10.4. The new system allows to configure s3 clients per endpoint instead of
one client per filestore. For example if your existing environment consists of two s3 storage endpoints with 500 buckets per endpoint you can reduce the number of configured clients from 1000 to 2.
Please be aware that the configuration via the old system is still possible but we strongly advice to migrate it to the new system. 
It is also possible to use both at the same time, but again we highly recommend to migrate completly to the new system.

## How to configure s3 clients

The new clients are configured via the new namespace `com.openexchange.filestore.s3client.[clientID]`. A complete configuration could look like this:

```

com.openexchange.filestore.s3client.ox-filestore-1.endpoint=https://mys3host/
com.openexchange.filestore.s3client.ox-filestore-1.accessKey=myAccessKey
com.openexchange.filestore.s3client.ox-filestore-1.secretKey=myAccessSecret
com.openexchange.filestore.s3client.ox-filestore-1.encryption=none
com.openexchange.filestore.s3client.ox-filestore-1.pathStyleAccess=true
com.openexchange.filestore.s3client.ox-filestore-1.chunkSize=8MB
com.openexchange.filestore.s3client.ox-filestore-1.buckets=ox-filestore-1-1-*, ox-filestore2-1-1-5 
```

As you can see the most properties are exactly the same as before. But there is one new property called 
`com.openexchange.filestore.s3client.[clientID].buckets` which contains all buckets the client serves.
It is possible to use multiple entries separated by comma and you can even use wildcards or a combination of both.

Once you configured all your clients you only have to configure the filestore mapping. For this purpose you use the existing 
`com.openexchange.filestore.s3.[filestoreID].bucketName` property. For example for two filestores:

```
com.openexchange.filestore.s3.MyFilestore1.bucketName=ox-filestore-1-1-5
com.openexchange.filestore.s3.MyFilestore2.bucketName=ox-filestore2-1-1-5
```

The bucket name is then matched against the buckets list from the s3 client configuration and every matching filestore uses the same client. 
In our example both filestores `MyFilestore1` and `MyFilestore2` use the `ox-filestore-1` client. This means in case of a migration that you can reduce the number of config properties per filestore from 7 to 1.
But this can be improved even further in case your filestore names and your bucket names match. In this case you can omit the bucketName property and only need to make sure that the s3 clients buckets match the filestore ids.
In our example this would work in case all filestore id are composed like `ox-filestore-[storage_system]-[storage_user_id]-[sequential_number_per_storage_user]`. This entails that every storage user has its own pair of credentials which requires an own configured client. An example with two storage system and two users could look like the following:

```
com.openexchange.filestore.s3client.ox-filestore-1.endpoint=https://storageSystem1/
com.openexchange.filestore.s3client.ox-filestore-1.accessKey=myAccessKey
com.openexchange.filestore.s3client.ox-filestore-1.secretKey=myAccessSecret
com.openexchange.filestore.s3client.ox-filestore-1.encryption=none
com.openexchange.filestore.s3client.ox-filestore-1.pathStyleAccess=true
com.openexchange.filestore.s3client.ox-filestore-1.chunkSize=8MB
com.openexchange.filestore.s3client.ox-filestore-1.buckets=ox-filestore-1-1-*

com.openexchange.filestore.s3client.ox-filestore-2.endpoint=https://storageSystem1/
com.openexchange.filestore.s3client.ox-filestore-2.accessKey=myAccessKey2
com.openexchange.filestore.s3client.ox-filestore-2.secretKey=myAccessSecret2
com.openexchange.filestore.s3client.ox-filestore-2.encryption=none
com.openexchange.filestore.s3client.ox-filestore-2.pathStyleAccess=true
com.openexchange.filestore.s3client.ox-filestore-2.chunkSize=8MB
com.openexchange.filestore.s3client.ox-filestore-2.buckets=ox-filestore-1-2-* 

com.openexchange.filestore.s3client.ox-filestore-3.endpoint=https://storageSystem2/
com.openexchange.filestore.s3client.ox-filestore-3.accessKey=myAccessKey3
com.openexchange.filestore.s3client.ox-filestore-3.secretKey=myAccessSecret3
com.openexchange.filestore.s3client.ox-filestore-3.encryption=none
com.openexchange.filestore.s3client.ox-filestore-3.pathStyleAccess=true
com.openexchange.filestore.s3client.ox-filestore-3.chunkSize=8MB
com.openexchange.filestore.s3client.ox-filestore-3.buckets=ox-filestore-2-1-*

com.openexchange.filestore.s3client.ox-filestore-4.endpoint=https://storageSystem2/
com.openexchange.filestore.s3client.ox-filestore-4.accessKey=myAccessKey4
com.openexchange.filestore.s3client.ox-filestore-4.secretKey=myAccessSecret4
com.openexchange.filestore.s3client.ox-filestore-4.encryption=none
com.openexchange.filestore.s3client.ox-filestore-4.pathStyleAccess=true
com.openexchange.filestore.s3client.ox-filestore-4.chunkSize=8MB
com.openexchange.filestore.s3client.ox-filestore-4.buckets=ox-filestore-2-2-*
```
Lets say every user can provide 500 buckets then previously you would have to configure 2000 clients instead of just those four.