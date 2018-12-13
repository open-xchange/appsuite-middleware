LOAD DATA LOCAL INFILE '/absolute/path/to/the/MaxMind/GeoCityIpBlocks.csv' INTO TABLE ip_blocks COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' IGNORE 1 LINES (
@network,
geoname_id,
registered_country_geoname_id,
represented_country_geoname_id,
is_anonymous_proxy,
is_satellite_provider,
postal_code,
latitude,
longitude,
accuracy_radius) SET
ip_from = INET_ATON(SUBSTRING(@network, 1, LOCATE('/', @network) - 1)),
ip_to = (INET_ATON(SUBSTRING(@network, 1, LOCATE('/', @network) - 1)) + (pow(2, (32-CONVERT(SUBSTRING(@network, LOCATE('/', @network) + 1), UNSIGNED INTEGER)))-1));

LOAD DATA LOCAL INFILE '/absolute/path/to/the/MaxMind/GeoCityLocations.csv' INTO TABLE ip_locations CHARACTER SET UTF8 COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' IGNORE 1 LINES (
geoname_id,
locale_code,
continent_code,
continent_name,
country_iso_code,
country_name,
subdivision_1_iso_code,
subdivision_1_name,
subdivision_2_iso_code,
subdivision_2_name,
city_name,
metro_code,
time_zone);