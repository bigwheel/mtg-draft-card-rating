# test1 スキーマ

# --- !Ups

CREATE TABLE ACCOUNTS(
  NAME     VARCHAR(32) NOT NULL PRIMARY KEY,
  PASSWORD VARCHAR(64) NOT NULL
);

# --- !Downs

DROP TABLE ACCOUNTS