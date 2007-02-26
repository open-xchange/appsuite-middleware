
CREATE TABLE user_configuration (
	cid integer,
	user integer,
	permissions integer
);

CREATE TABLE user_setting_mail (
    cid integer,
    user integer,
    bits integer,
    send_addr text,
    reply_to_addr  text,
    msg_format integer,
    display_msg_headers text,
    auto_linebreak integer,
    std_trash text,
    std_sent text,
    std_drafts text,
    std_spam text,
    upload_quota integer,
    upload_quota_per_file integer
);

CREATE TABLE user_setting_mail_signature (
    cid integer,
    user integer,
    id text,
    signature text
);

CREATE TABLE user_setting_spellcheck (
    cid integer,
    user integer,
    user_dic text
);

CREATE TABLE user_setting_admin (
    cid integer,
    user integer
);

CREATE TABLE user_setting (
    cid INTEGER,
    user_id INTEGER,
    path_id INTEGER,
    value text
);
