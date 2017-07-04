---
title: ExportUserFeedback
---

# Exports stored user feedback data
This tool allows the export of all stored user feedback data in form of a CSV-file.

It's possible to start the tool with only API credentials and the storage path, or to provide various additional filters in form of input parameters. If no filters are set, all data available for type ``star-rating-v1`` and context group ``default`` are exported. The path to the location, where the result should be stored is appended without any parameter at the end of the line.

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
- ``--delimiter <arg>``      
CSV export only: The column delimiter used. Default: ';'.

### Examples

<code>exportuserfeedback -s 1489536000 /tmp/feedback.csv</code>

Exports all data to /tmp/feedback.csv, stored from 17.02.17 00:00 until now for the context group ``default`` and the feedback type ``star-rating-v1``.

**CSV example**

```
Time,User,Score,App,Comment,OS,Browser,Version,UserAgent,Resolution
2017-01-24 09:02:59,ae43fc98,1,general,"I do not like it!",Windows Vista,Chrome,49.0,Mozilla/5.0 (Windows NT 6.0) AppleWebKit/537.36 (KHTML  like Gecko) Chrome/49.0.2623.112 Safari/537.36,1366x768
2017-01-24 14:10:19,34eefab0,5,mail,"Everything is fine!",Windows 8,Chrome,55.0,Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML  like Gecko) Chrome/55.0.2883.87 Safari/537.36,1366x768
```

Attention, the username is hashed and therefore can not be deciphered. Since a usernames hash value is always the same, multiple entries can still be matched to a single user.