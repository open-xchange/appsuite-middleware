# File- and Directory Name Restrictions

Regarding the case sensitivity of file and directory names, OX Drive works in a case-insensitive, but case-preserving way. That means that there cannot be two files with an equal name ignoring case in the same directory, but it's still possible to synchronize the names in a case-sensitive manner, as well as it's possible to change only the case of file- and directory names.

The same applies to equally named files and directories on the same level in the folder hierarchy, i.e. it's not possible to create a new file in a directory where an equally (ignoring case) named subdirectory already exists and vice versa.

There is a similar restriction regarding file and directory names in the same directory having different unicode normalization forms, yet the same textual representation. OX Drive requires uniqueness regarding this textual representaion of potentially different encoded unicode strings. So, in case the client tries to synchronize two textually equal files or directories, he is instructed to put one of them into quarantine. Internally the server performs an equals-check of the "NFC" normalization forms of the strings, i.e. an unicode string is normalized using full canonical decomposition, followed by the replacement of sequences with their primary composites, if possible. Details regarding unicode normalization can be found at [http://www.unicode.org/reports/tr15/tr15-23.html](http://www.unicode.org/reports/tr15/tr15-23.html) .

## Invalid and ignored Filenames

There are some filenames that are invalid or ignored and therefore not synchronized. This means that files with these names should not be taken into account when sending the directory contents to the server, or when calculating the directory checksum (see below). The following list describes when a filename is considered invalid:

* If it contains one of the following reserved characters:  

|Character | Description |
|:---------|:------------|
|<|less than|
|>|greater than|
|:|colon|
|"|double quote|
|/|forward slash|
|\\|backslash|
|\||vertical bar or pipe|
|?| question mark|
|\*| asterisk |

* It contains a character whose integer representations is in the range from 0 through 31
* The last character is a . (dot) or ' ' (space)
* It's case-invariant name without an optional extension matches one of the reserved names CON, PRN, AUX, NUL, COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9, LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, or LPT9
* It consists solely of whitespace characters  

The following list gives an overview about the ignored filenames:

* desktop.ini
* Thumbs.db
* .DS_Store
* icon\r
* Any filename ending with .drivepart
* Any filename starting with .msngr_hstr_data_ and ending with .log

Nevertheless, if the client still insists to send a file version with an invalid or ignored filename, the file creation on the server is refused with a corresponding error action (see below).

## Invalid and ignored Directory Names

There are also similar restrictions regarding invalid directory names. Any try to include them in the list of directory versions will be responded with a corresponding error action for the directory version. The following list describes when a path is considered invalid:

* If it contains one or of the following reserved characters:

|Character | Description |
|:---------|:------------|
|<|less than|
|>|greater than|
|:|colon|
|"|double quote|
|\\|backslash|
|\||vertical bar or pipe|
|?| question mark|
|\*| asterisk |

* It contains a character whose integer representations is in the range from 0 through 31
* The last character of any subpath (i.e. the last part of the whole path or the part preceding the spearator character /) is a . (dot) or ' ' (space)
* It consists solely of whitespace characters
* It not equals the root path /, but ends with a / (forward slash) character
* It contains two or more consecutive / (forward slash) characters  

The following list gives an overview about the ignored directory names:

* /.drive
* Any directory whose path ends with /.msngr_hstr_data

## Length Restrictions

The maximum allowed length for path segments, i.e. the parts between forawrd slashes (`/`) in directory and filenames, is restricted to 255 characters. Synchronizing a file or directory version that contains path segments longer than this limit leads to those versions being put into quarantine.
