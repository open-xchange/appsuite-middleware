---
title: Login Rate Limiting
---

# Introduction

The Open-Xchange Middleware offers two rate limiting possibilities. Both act the same way: 

* The [token bucket](https://en.wikipedia.org/wiki/Token_bucket) algorithm is used
* Before every login attempt, check if login attempt is already rate limited (all tokens/permits exhausted)
* On every failed login attempt due to invalid credentials, decrease the number of possible further attempts (redeem a token/permit) in the defined time frame.
* Once a successful login is performed, rate limit boundaries are dropped

Moreover, both come with the same implications:

* Rate-limit is per Middleware node, i.e. the total number of possible attempts is the number per node times the number of nodes.
* This doesn't really solve a brute-force case where e.g. a large pool of source IP addresses and User-Agents iterates over a pool of user names
* The limit is across all clients/interfaces. I.e. if a user changes his password and forgets to adjust his sync client accordingly, the sync client might cause a constant rate limit of login attempts, which effectively also blocks the webmail login until the sync client is stopped.

They only differ on how the associations take place. One executes the rate limiting of failed login attempts by attributes/characterirstics of HTTP protocol (like value of the User-Agent header and Internet Protocol (IP) address of the client or last proxy that sent the request). A client that exceeds that limit will receive a `"429 Too Many Requests"` HTTP error code.

The other one is solely based on the unique user name provided by login. A client that exceeds that limit will receive the `"LGI-0026"` or `"LGI-0027"` HTTP-API error code

## Rate Limiting by HTTP attributes/characterirstics

Available with v7.8.0.

### Configuration

* `com.openexchange.ajax.login.maxRate` Specifies the maximum number of permits that applies to incoming HTTP login requests. Default value is `50`.
* `com.openexchange.ajax.login.maxRateTimeWindow` Specifies the rate limit's time window in which to track incoming HTTP login requests. Default value is `300000` (5 minutes).

## Rate Limiting by login user name

Available with v7.10.1.

### Configuration

* `com.openexchange.ajax.login.rateLimitByLogin.enabled` If set to `true` a rate limiter based on user names for login is enabled. Default is `false`
* `com.openexchange.ajax.login.rateLimitByLogin.permits` Specifies the number of available permits for the rate limiter based on user names for login. Default value is `3`.
* `com.openexchange.ajax.login.rateLimitByLogin.timeFrameInSeconds` Specifies the time frame in seconds for the rate limiter based on user names for login. Default value is `30` (30 seconds).

