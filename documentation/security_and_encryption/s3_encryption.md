---
title: Amazon SSE-S3
---

Amazon SSE-S3 or Amazon Server Side Encryption is an encryption option which can be activated to encrypt communication within the amazon storage. The keys are managed by amazon itself and they are rotated regulary.
Further information can be found [here](https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingServerSideEncryption.html]).

To activate this encryption you only need to configure the `com.openexchange.filestore.s3.[filestoreID].encryption` value to `sse-s3` or to `rsa+sse-s3` if you want both server and client side encryption.
Once activated the OX Middleware does two things: first it sends the apropiate headers which tell the S3 storage to use server side encryption and second it configures newly created buckets to only accept requests with those headers.
This ensures that no other client uses this bucket without server side encryption. This also means, that those buckets need to be reconfigured manually if server side encryption is turned of again. Therefore it is highly recommended to not activate this feature
unless you want to stick with it.