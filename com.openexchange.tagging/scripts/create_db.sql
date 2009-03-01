DROP TABLE IF EXISTS tags;

CREATE TABLE tags (
cid INT4 UNSIGNED NOT NULL, 
object_id INT4 UNSIGNED NOT NULL, 
folder_id INT4 UNSIGNED NOT NULL, 
tag VARCHAR(255) NOT NULL, 
PRIMARY KEY (cid, object_id, folder_id), 
FOREIGN KEY(cid, folder_id) REFERENCES oxfolder_tree(cid, fuid)) 
ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
