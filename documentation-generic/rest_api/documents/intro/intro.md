# Introduction

This page describes the rest http api. It provides interfaces for administrative purposes and is therefore not intended to be used by clients for endusers.
Basic authentication is used to authenticate the client, which needs to be configured in the middleware via the following properties:

```
com.openexchange.rest.services.basic-auth.login
com.openexchange.rest.services.basic-auth.password
```