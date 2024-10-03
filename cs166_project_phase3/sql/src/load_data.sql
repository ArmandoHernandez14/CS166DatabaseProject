/* Replace the location to where you saved the data files*/
COPY Users
FROM '/class/classes/ahern672/cs166_project_phase3/data/users.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Catalog
FROM '/class/classes/ahern672/cs166_project_phase3/data/catalog.csv'
WITH DELIMITER ',' CSV HEADER;

COPY RentalOrder
FROM '/class/classes/ahern672/cs166_project_phase3/data/rentalorder.csv'
WITH DELIMITER ',' CSV HEADER;

COPY TrackingInfo
FROM '/class/classes/ahern672/cs166_project_phase3/data/trackinginfo.csv'
WITH DELIMITER ',' CSV HEADER;

COPY GamesInOrder
FROM '/class/classes/ahern672/cs166_project_phase3/data/gamesinorder.csv'
WITH DELIMITER ',' CSV HEADER;
