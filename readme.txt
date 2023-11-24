Requirements:

Python3 - book_parser.py was coded and run using Python version 3.9.6
Java - Java files were coded and run using JavaSE-17
MySQL Server - download MySQL server from https://dev.mysql.com/downloads/mysql/, download compatible version
MySQL Driver - download MySQL driver from https://dev.mysql.com/downloads/connector/j/, download compatible version
    - place the jar file from the driver into 'lib' folder of project
    - project was coded using vscode which required code below to be placed in settings.json within .vscode folder:

        {
            "java.project.referencedLibraries": [
                "/Users/ejroceles/CS6360/Project1/lib/mysql-connector-j-8.2.0.jar"
            ]
        }

    - Other IDEs will most likely require a similar step in order to connect and read the driver properly, search for what your IDE or system requires for the driver to connect properly

Imports:

Pandas is needed for book_parser.py
    - Should be able to be installed using pip or pip3 install pandas

Java files use Swing for GUI, should be built into Java download, however if your version does not contain Swing, search for how to download onto your system/version

Steps to run program:

- Have books.csv and borrowers.csv downloaded and within project folder
- Run book_parser.py which should create 4 .csv files: BOOK.csv, AUTHORS.csv, BOOK.csv, BORROWER.csv
- Using mysql, you should be able to use source create-library-database.sql (replace with correct path to your sql file if need)
    - There may be issues with the last 4 lines regarding access issues, search to solve problem depending on what error appears
    - OPTIONAL SOLUTION:
        - MySQL Workbench can be downloaded as helper tool, create-library-database.sql file should run correctly on that
- Once database has been successfully created with necessary tables properly loaded with values, make sure all java files are edited to have your correct information for url, user, and password for the sql connection wherever needed
- Run Main.java and the application should be up and running!
