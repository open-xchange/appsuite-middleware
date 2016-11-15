# Client side filtering

OX Drive clients may define a user- and/or application-defined list of file- and directory name exclusions. Those exclusion filters are then taken into account during synchronization, i.e. files and directories matching a defined exclusion pattern are ignored when comparing the list of server-, client- and original versions. Also, the file exclusion lists are considered for the calculation of aggergated directory checksums.

The exclusion filters may be set, changed or unset at any time during synchronization, there are no additional requests needed to set them up. Instead, the list of excluded files and directories is simply sent along with each syncFolders, syncFiles and download request. The following tables show the JSON representation of file- and directory patterns that are used to build up the exlcusion lists:

## Directory pattern

A directory pattern is defined by a pattern string and further attributes.

|Name|Type|Value|
|:---|:---|:----|
|type|String|The pattern type, currently one of `exact` or `glob`.|
|path|String|The path pattern, in a format depending on the pattern type.|
|caseSensitive|Boolean|Optional flag to enable case-sensitive matching, defaults to `false`|

## File pattern

A file pattern is defined by pattern strings for the filename and path, as well as further attributes.

|Name|Type|Value|
|:---|:---|:----|
|type|String|The pattern type, currently one of `exact` or `glob`.|
|path|String|The path pattern, in a format depending on the pattern type.|
|name|String|The filename pattern, in a format depending on the pattern type.|
|caseSensitive|Boolean|Optional flag to enable case-sensitive matching, defaults to `false`|

## Pattern types

A pattern currently may be defined in two formats: exact or glob.

* exact  
An exact pattern, matching the file- or directory version literally. For example, to exclude the file Backup.pst in the subfolder Mail below the root synchronization folder, an exact file pattern would look like: `{"path":"/Mail","name":"Backup.pst","type":"exact"}`, or, an exact directory pattern for the directory /Archive would be represented as `{"path":"/Archive","type":"exact"}`.
* glob  
A simple pattern allowing to use the common wildcards `*` and `?` to match file- and directory versions. For example, to exclude all files ending with .tmp across all directories, the glob file pattern could be defined as `{"path":"*","name":"*.tmp","type":"glob"}`, or, to exclude the directory `/Project/.git` and all its subdirectories recursively, this would be expressed using a combination of the following two directory patterns: `[{"path":"/Project/.git","type":"exact"},{"path":"/Project/.git*","type":"glob"}]`.

## Further considerations

* It's possible to exclude a (parent) directory with an appropriate pattern, while still subfolders below that directory being synchronized. This usually results in the excluded directory being created ob both client- and server side, but no file contents within the excluded directory being exchanged. If subfolders should be excluded, too, a wildcard should be used in the pattern to match any subdirectories.
* If the client tries to synchronize a file- or directory version that is ignored, i.e. a version that would match any of the provided exclusion filters, the server behaves similarly to the handling of invalid and ignored file- and directory names (see above), i.e. the client would be instructed to put those versions into quarantine.
* For the calculation of directory checksums, it's important that the server and client perform exactly the same matching for ignored filenames: A * character matches zero or more characters, a ? character matches exactly one character. All other characters are matched literally. Advanced glob flavors like braces to define subpattern alternatives or square brackets for character sets are not used.
* Client-side filtering is available with API version 2. The API version that is supported by the server is included in the response of the [Settings](#Drive_getSettings) request.
* Whenever there are active exclusion filters, the syncFolders request should contain all of both directory and file exclusion filter lists. For the syncFiles request, it's sufficient to include the list of file exclusions.
