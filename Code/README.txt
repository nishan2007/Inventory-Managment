SmartStock Code Package
=======================

This Code directory is organized to keep the project deliverables separated by purpose while still including the full application source, a deployable build, database deliverables, and end-user documentation.

Directory Structure
-------------------

Code/
  README.txt
    This file. It explains the package layout and where each deliverable is stored.

  SourceCode/
    Complete application source package for the SmartStock Java desktop application.

    SourceCode/src/main/java/
      All Java source files used by the program, including application startup, login, inventory, sales, employee management, role/permission management, and database access.

    SourceCode/src/ICONS/
      PNG icon assets used by the desktop UI.

    SourceCode/src/Images/
      Additional UI image assets used by the application.

    SourceCode/META-INF/
      Manifest and metadata used by the packaged JAR.

    SourceCode/Inventory Managment.iml
      IntelliJ IDEA module file for the project.

  Deployable/
    Files needed to run the current packaged build of the application.

    Deployable/inventory-management.jar
      Executable Java archive for the SmartStock application.

    Deployable/Run_SmartStock.command
      Convenience launcher script for macOS.

    Deployable/src/ICONS/
    Deployable/src/Images/
      Runtime image assets copied beside the JAR because the current application loads them from relative file paths.

    Deployable/CompiledClasses/
      Current compiled .class files corresponding to the packaged build.

  Database/
    Database deliverables for this version of the project.

    Database/SmartStock_Database_Export.sql
      Plain SQL import file containing the schema and starter data needed to recreate the SmartStock database for this version.

    Database/README.txt
      Notes about the database import file, default credentials, and import sequence.

  Documentation/
    Supporting project documentation.

    Documentation/Installation_Guide.md
      Step-by-step installation guide, including screenshots and configuration steps.

    Documentation/User_Manual.md
      End-user manual describing the application workflow and available features.

    Documentation/Screenshots/
      Screenshot assets referenced by the installation guide.

    Documentation/Tools/
      Utility used to regenerate the included documentation screenshots.

Important Notes
---------------

1. The application currently expects database connection settings to be supplied at runtime through environment variables or Java system properties:
   SMARTSTOCK_DB_URL
   SMARTSTOCK_DB_USER
   SMARTSTOCK_DB_PASSWORD

2. The database import file includes starter records so the application can be launched and graded without relying on the team's live hosted database.

3. The current packaged application is a Java desktop application, not a Web site, so there is no WebSite directory in this project. Instead, the deployable output is the executable JAR in the Deployable folder.
