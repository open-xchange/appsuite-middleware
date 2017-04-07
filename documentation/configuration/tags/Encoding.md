---
title: Encoding
---

This page shows all properties with the tag: Encoding

| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | The default charset to be used by JavaMail. If not set (the normal case), the standard J2SE file.encoding System property is used.<br>This allows applications to specify a default character set for sending messages that's different than the character set used for files stored on the system.<br>This is common on Japanese systems.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodetext.strict |
|:----------------|:--------|
| __Description__ | RFC 2047 requires that encoded text start at the beginning of a whitespace separated word.<br>Some mailers, especially Japanese mailers, improperly encode text and include encoded text in the middle of words.<br>This property controls whether JavaMail will attempt to decode such incorrectly encoded text.<br>The default is true, meaning that JavaMail wil not attempt to decode such improperly decoded text.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeeol.strict |
|:----------------|:--------|
| __Description__ | When choosing an encoding for the data of a message, JavaMail assumes that any of CR, LF, or CRLF are valid line<br>terminators in message parts that contain only printable ASCII characters, even if the part is not a MIME text type.<br>It's common, especially on UNIX systems, for data of MIME type application/octet-stream (for example) to really be<br>textual data that should be transmitted with the encoding rules for MIME text. In rare cases, such pure ASCII text<br>may in fact be binary data in which the CR and LF characters must be preserved exactly.<br>If this property is set to true, JavaMail will consider a lone CR or LF in a body part that's not a MIME text type to<br>indicate that the body part needs to be encoded.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.smtp.allow8bitmime |
|:----------------|:--------|
| __Description__ | If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64"<br>encodings are converted to use "8bit" encoding if they follow the RFC2045 rules for 8bit text. <br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.charset |
|:----------------|:--------|
| __Description__ | The mail.mime.charset System property can be used to specify the default MIME charset to use for encoded words and text parts that don't<br>otherwise specify a charset. Normally, the default MIME charset is derived from the default Java charset, as specified in the file.encoding<br>System property. Most applications will have no need to explicitly set the default MIME charset.<br>In cases where the default MIME charset to be used for mail messages is different than the charset used for files stored on the system,<br>this property should be set.<br> |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodetext.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.decodetext.strict property controls decoding of MIME encoded words.<br>The MIME spec requires that encoded words start at the beginning of a whitespace separated word.<br>Some mailers incorrectly include encoded words in the middle of a word.<br>If the mail.mime.decodetext.strict System property is set to "false", an attempt will be made to decode these illegal encoded words.<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeeol.strict |
|:----------------|:--------|
| __Description__ | The mail.mime.encodeeol.strict property controls the choice of Content-Transfer-Encoding for MIME parts that are not of type "text".<br>Often such parts will contain textual data for which an encoding that allows normal end of line conventions is appropriate.<br>In rare cases, such a part will appear to contain entirely textual data, but will require an encoding that preserves CR and LF characters<br>without change. If the mail.mime.encodeeol.strict System property is set to "true", such an encoding will be used when necessary.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the getFileName method uses the MimeUtility method decodeText to decode any non-ASCII characters in the filename.<br>Note that this decoding violates the MIME specification, but is useful for interoperating with some mail clients that use this convention.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodefilename |
|:----------------|:--------|
| __Description__ | If set to "true", the setFileName method uses the MimeUtility method encodeText to encode any non-ASCII characters in the filename.<br>Note that this encoding violates the MIME specification, but is useful for interoperating with some mail clients that use this convention.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.decodeparameters |
|:----------------|:--------|
| __Description__ | If set to "true", non-ASCII parameters in a ParameterList, e.g., in a Content-Type header, will be encoded as specified by RFC 2231.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.encodeparameters |
|:----------------|:--------|
| __Description__ | If set to "true", non-ASCII parameters in a ParameterList, e.g., in a Content-Type header, will be decoded as specified by RFC 2231.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.base64.ignoreerrors |
|:----------------|:--------|
| __Description__ | If set to "true", the BASE64 decoder will ignore errors in the encoded data, returning EOF.<br>This may be useful when dealing with improperly encoded messages that contain extraneous data at the end of the encoded stream.<br>Note however that errors anywhere in the stream will cause the decoder to stop decoding so this should be used with extreme caution.<br> |
| __Default__ | false |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
| __Key__ | mail.mime.setdefaulttextcharset |
|:----------------|:--------|
| __Description__ | When updating the headers of a message, a body part with a text content type but no charset parameter will have<br>a charset parameter added to it if this property is set to "true".<br> |
| __Default__ | true |
| __Reloadable__ | false |
| __Configcascade Aware__ | false |
| __Tags__ | <a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Mail.html">Mail</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/IMAP.html">IMAP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/POP3.html">POP3</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/SMTP.html">SMTP</a>,<a href="https://documentation.open-xchange.com/latest/middleware/configuration/tags/Encoding.html">Encoding</a> |
| __File__ | javamail.properties |

---
