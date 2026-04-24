# SmartStock User Manual

## 1. Purpose

SmartStock is a desktop inventory and sales management application for store-based stock control, product maintenance, employee administration, and transaction review.

## 2. Main Workflow

The normal user flow is:

1. Launch SmartStock.
2. Test the database connection from the Welcome screen.
3. Continue to the login screen.
4. Sign in with a valid username and password.
5. Select a store if the user is assigned to more than one location.
6. Use the Main Menu to open the permitted modules.

For first-time review of the packaged submission, the default administrator login is:

- Username: `admin`
- Password: `Admin123!`

## 3. Welcome Screen

The Welcome screen provides:

- Application title and subtitle
- Database connection test button
- Continue button that becomes available after a successful connection test

Use this screen to confirm the system can connect to the configured database before proceeding.

## 4. Login

The Login screen requires:

- Username
- Password

After successful authentication, the system loads the user role and assigned store locations. If multiple stores are available, the user must choose one for the current session.

## 5. Main Menu

The Main Menu is the central navigation screen. Depending on role permissions, the user may see or access:

- Make a Sale
- View Sales
- View Inventory
- Add Item
- Edit Items
- Employees
- Roles & Permissions
- Logout

Buttons are enabled or disabled based on the permissions loaded for the signed-in user role.

## 6. Make a Sale

The `Make a Sale` screen is used for point-of-sale activity.

Key functions:

- Search for products
- Add products to the cart
- Adjust price and quantity
- Choose a payment method: `CASH`, `CARD`, or `CHEQUE`
- Complete checkout

The screen also displays the active store, current user, date, and time.

## 7. View Inventory

The `View Inventory` module shows product stock by store.

Users can:

- search by product details
- refresh the listing
- filter inventory by stock status
- review quantity on hand and reorder level

Color cues are used to show stock conditions such as out-of-stock or low-stock items.

## 8. Add Item

The `Add Item` screen allows users with permission to create new products.

Available fields include:

- Item name
- SKU
- Description
- Barcode
- Additional barcodes
- Cost price
- Selling price
- Category ID
- Starting quantity

When saved, the new product is inserted into the database for the currently selected location.

## 9. Edit Items

The `Edit Items` screen is intended for updating existing product information such as descriptions, pricing, and stock-related details.

Use this module when a product already exists and must be corrected or maintained.

## 10. View Sales

The `View Sales` module lets users review previous completed transactions.

Users can:

- search by sale ID, cashier, store, or payment method
- filter by date range
- refresh the list
- clear filters
- open sale details for the selected transaction

The summary area displays transaction counts and total information derived from the loaded records.

## 11. Employee Management

Authorized users can manage employee accounts by:

- viewing employee records
- adding a new employee
- updating an existing employee
- assigning stores to an employee

Role assignment is handled during employee setup or update.

## 12. Roles and Permissions

The `Roles & Permissions` screen allows administrative users to:

- create new roles
- load an existing role
- enable or disable individual permissions
- save permission changes

This controls which screens and actions are available to each role in the application.

## 13. Logout

Use the `Logout` button on the Main Menu to end the current session and return to the login screen.

## 14. Common Issues

### Login fails

Verify the username and password exist in the configured database.

### No store available

The signed-in user must have at least one assigned record in `user_locations`.

### Access denied

The current role does not have the permission required for that screen or action.

### No inventory or sales shown

Check that:

- the database import completed successfully
- the current store contains data
- filters are not excluding the expected records
