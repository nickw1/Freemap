CREATE TABLE annotations (id serial, wayid INT, text VARCHAR(255), dir INT DEFAULT 0, xy geometry, type VARCHAR(255), authorised INT DEFAULT 0, userid INT DEFAULT 0);
CREATE TABLE panoramas (id serial, authorised INT DEFAULT 0,userid INT, xy geometry);
CREATE TABLE users (id serial, username VARCHAR(255), password VARCHAR(255), email VARCHAR(255), k INT, active INT DEFAULT 0, isadmin INT DEFAULT 0);
CREATE TABLE walkroutes (id serial, the_geom geometry, title VARCHAR(255), description TEXT, startlon FLOAT, startlat FLOAT, userid INT DEFAULT 0, distance FLOAT DEFAULT 0.0, authorised INT DEFAULT 0);
