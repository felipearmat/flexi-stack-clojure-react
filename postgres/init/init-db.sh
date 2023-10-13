#!/bin/bash
set -e

# Creating databases for dev and test anvironments
USER="$POSTGRES_USER"
DB="$POSTGRES_DB"
DEV_DB="$POSTGRES_DB"_dev
TEST_DB="$POSTGRES_DB"_test

psql -v ON_ERROR_STOP=1 --username "$USER" --dbname "$DB" <<-EOSQL
  SELECT 'CREATE DATABASE $DB'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB');\gexec

  SELECT 'CREATE DATABASE $DEV_DB'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DEV_DB');\gexec

  SELECT 'CREATE DATABASE $TEST_DB'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$TEST_DB');\gexec

  GRANT ALL PRIVILEGES ON DATABASE $DB TO $USER;
  GRANT ALL PRIVILEGES ON DATABASE $DEV_DB TO $USER;
  GRANT ALL PRIVILEGES ON DATABASE $TEST_DB TO $USER;
EOSQL
