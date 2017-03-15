---
title: DeleteUserFeedback
---

# Deletes stored user feedback data
This tool allows the deletion of stored user feedback data located on the globaldb.

It's possible to start the tool without any parameters or to select a context group, feedback type and time range of the data, that is supposed to be deleted.

Since the command line tool uses the REST API to delete the data, the user credentials are mandatory. The user can also set a distinct API path for the REST calls transmitted by this CLT.

## Parameters

- ``--api-root <arg>``      
URL to an alternative HTTP API endpoint. Example:
'https://192.168.0.1:8443/userfeedback/v1'
- ``-e,--end-time <arg> ``       
End time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given before this time is deleted. If not set, all feedback since -s is deleted.
- ``-g,--context-group <arg>``   
The context group identifying the global DB where the feedback is stored. Default: 'default'.
- ``-s,--start-time <arg>``      
Start time in seconds since 1970-01-01 00:00:00 UTC. Only feedback given after this time is deleted. If not set, all feedback up to -e is deleted.
- ``-t,--type <arg>``            
The feedback type to delete. Default: 'star-rating-v1'.
- ``-U,--api-user <arg>``        
Username and password to use for API authentication (user:password).

### Examples

<code>deleteuserfeedback -s 1489536000</code>

Deletes all data, stored from 17.02.17 00:00 until now for the context group ``default`` and the feedback type ``star-rating-v1``.