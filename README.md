# HomEx - Household Expense Manager

**Version**: 1.0.0  
**Author**: Aurélien JOURDAN ; Kiroshan SIVAKUMAR

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Technologies Used](#technologies-used)
4. [Setup and Installation](#setup-and-installation)
5. [Usage Instructions](#usage-instructions)
6. [Database Structure](#database-structure)
7. [Class Descriptions](#class-descriptions)

## Project Overview
HomEx (Household Expense Manager) is a Java Swing-based application designed to help users manage household expenses effectively. With HomEx, multiple household members can log in, track expenses, create expense groups, and analyze financial data to stay organized and avoid going over budget.

## Features
- **User Authentication**: Secure login and sign-up functionality using MariaDB.
- **Avatar Selection**: Users can select an avatar from predefined images or upload their own.
- **Personal Wallet**: Track personal budgets and expenses, categorized as gains or losses.
- **Expense Groups**: Create and manage expense groups for different categories (e.g., utilities, groceries) with password protection.
- **Add, Edit, Remove Expenses**: Manage expenses for both individual users and groups. Gains are displayed in green, and losses in red.
- **Expense Reporting and Analysis**: Analyze expenses and view reports to make informed financial decisions.
- **Shared Household Management**: Multiple users can collaborate, with actions tracked by the user who performed them.

## Technologies Used
- **Java Swing**: For building the graphical user interface (GUI).
- **MariaDB SQL**: For storing user and expense data.
- **JDBC**: For connecting the Java application to the MariaDB database.

## Setup and Installation

### Prerequisites
- Java JDK 11 or later
- MariaDB SQL
- Java IDE

**Database Setup**  
   - Install MariaDB and create a new database called `homex_db`.
   - Import the SQL schema provided in `src/main/resources/homex_schema.sql` to set up the necessary tables.

3. **Configure Database Credentials**  
   Update the database credentials in the `LogIn.java` file:
   ```java
   String url = "jdbc:mariadb://localhost:3306/homex_db";
   String user = "your_username";
   String password = "your_password";
   ```

4. **Run the Application**  
   - Open the project in your preferred IDE.
   - Run the `Main.java` class to launch the application.

## Usage Instructions
1. **Login / Sign Up**:  
   When you launch the app, use your existing credentials or sign up to create a new account. Select or upload an avatar during sign-up.

2. **Personal Wallet**:  
   Access your personal wallet to view your current budget and add expenses. Specify whether each expense is a gain or a loss. The budget will adjust automatically based on your expenses.

3. **Manage Expense Groups**:  
   Create a new group or join an existing group using the group's password. Once in a group, you can add, edit, or remove expenses. Expenses added by other users are visible along with the name of the user who added them.

4. **Analyze Expenses**:  
   View and filter expenses to generate reports, helping you understand your spending patterns and budget usage.

## Database Structure
The following tables are used in the HomEx database:
- **users**: Stores user credentials and avatar info.
- **groups**: Stores information about expense groups.
- **expenses**: Stores individual expenses, including whether they are gains or losses and which user added them.

## Class Descriptions
- **LogIn.java**: Manages user authentication (login, sign-up).
- **Main.java**: Main entry point for the application.
- **Profile.java**: Handles user profile management, including username, password, and avatar changes.
- **PersonalWallet.java**: Manages personal budget and expenses, accounting for gains and losses.
- **Group.java**: Handles group management, including adding, editing, and removing expenses within groups.
- **User.java**: Represents user details, managing user-specific data like budgets and expenses.
- **Home.java**: The home screen of the application, from where users can navigate to various sections.
