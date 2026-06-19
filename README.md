# Campus Lost and Found Database Layer

JDBC integration with Supabase (PostgreSQL) for the Campus Lost and Found Reporting System.  
Provides a complete data access layer using DAO pattern for the `item_reports`, `claim`, `admin`, `activity_logs`, and `category` tables.

## Technologies
- Java 17+
- Maven
- PostgreSQL (Supabase)
- JDBC

## Setup for Backend Developer

1. **Clone the repository**
   ```bash
   git clone https://github.com/grayuschaurus/LostandFoundDB.git
   cd LostandFoundDB

2. Create config.properties file
Create the folder src/main/resources (if missing).
Inside it, create a file named config.properties with the following content (replace YOUR_DB_PASSWORD with the password provided by the database team):

properties:
db.url=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
db.user=postgres.ddrukchfncfxmsglimjx
db.password=YOUR_DB_PASSWORD
Important: This file is ignored by Git (see .gitignore). Never commit it.

3. Build the project
Using Maven:

bash
mvn clean compile
Or open in IntelliJ as a Maven project (dependencies will auto-download).

4. Run the test
Execute DatabaseTest.java (inside com.campuslf.test package) to verify the connection and basic CRUD operations.

Expected output:
Admin already exists, skipping insert.
Item report added: true
Claim added: true
Activity log added: true

Project Structure:
src/main/java/com/campuslf/
├── database/
│   └── DatabaseConnection.java      # Loads credentials, provides connection
├── models/
│   ├── Admin.java
│   ├── Category.java
│   ├── ItemReport.java
│   ├── Claim.java
│   └── ActivityLog.java
├── dao/
│   ├── AdminDAO.java
│   ├── ItemReportDAO.java
│   ├── ClaimDAO.java
│   └── ActivityLogDAO.java
└── test/
    └── DatabaseTest.java            # Example usage

DAO Methods Overview
* ItemReportDAO -	addItemReport() 
              - getAllItemReports(statusFilter) 
              - getItemReportById() 
              - updateReportStatus() 
              - deleteItemReport()
* ClaimDAO	- addClaim()
          - getClaimsByReportId()
          - updateClaimStatus()
* AdminDAO	- getAdminByUsername()
          - addAdmin()
* ActivityLogDAO	- addLog()
                - getAllLogs()


Usage Example (JavaFX Controller)
// Get all unclaimed items
ItemReportDAO itemDAO = new ItemReportDAO();
List<ItemReport> unclaimedItems = itemDAO.getAllItemReports("Pending");

// Add a new found item
ItemReport newItem = new ItemReport();
newItem.setAdminId(1);
newItem.setCategoryId(1);
newItem.setItemName("Black Wallet");
newItem.setDescription("Leather wallet, contains student ID");
newItem.setLocationFound("Canteen table");
newItem.setDateReported(LocalDate.now());
newItem.setDatePosted(LocalDate.now());
newItem.setFinderStudentId("2024-12345");
newItem.setFinderContactNum("09123456789");
newItem.setImageUrl("https://example.com/wallet.jpg");
newItem.setReportStatus("Pending");

boolean success = itemDAO.addItemReport(newItem);



Important Notes on Enums
The Supabase schema uses custom enum types for status fields:
* item_reports.status → type report_status (values: 'Pending', 'Claimed', 'Archived')
* claim.claim_status → type claim_status_enum (values: 'Pending', 'Approved', 'Rejected')

The DAO methods automatically handle the required casting using CAST(? AS report_status).
You only need to pass plain Java strings like "Pending". Do not try to pass the enum type directly.



 Troubleshooting
 * Connection refused - Check that config.properties has correct URL and user. Ensure you're using the session pooler port (5432).
 * FATAL: password authentication failed - The password in config.properties is incorrect. Contact the database team for the current password.
 * relation "category" does not exist - The database schema is missing. Run the CREATE TABLE script (available from database team).
 * column "status" is of type report_status but expression is of type character varying - You forgot to use the DAO method – it already includes CAST. If you write raw SQL, add CAST(? AS report_status).


