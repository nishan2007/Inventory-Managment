SmartStock Database Deliverables
================================

Included File
-------------

SmartStock_Database_Export.sql
  Combined PostgreSQL schema and starter data script prepared for the SmartStock application.

Usage
-----

1. Run the SQL file against the target PostgreSQL database:

   psql -U <db_user> -d <db_name> -f SmartStock_Database_Export.sql

Default Application Login
-------------------------

Username: admin
Password: Admin123!

Notes
-----

The SQL file creates the schema, roles, permissions, sample products, sample inventory, and default users required for a first launch.
