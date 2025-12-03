# Facebook CLI to GUI Conversion

I have successfully converted your CLI application to a JavaFX GUI application with a Maven project structure, including a fully styled Home Page.

## Changes Made

1.  **Project Structure**:
    *   Maven project with `pom.xml`.
    *   Source code in `src/main/java/com/facebook`.
    *   Resources in `src/main/resources/com/facebook`.

2.  **Database Configuration**:
    *   **Path**: Configured to use the shared drive path: `G:\Shared drives\Facebook\Database`.
    *   **Dynamic Loading**: The application now dynamically loads Feed posts and Contacts directly from this database path.

3.  **Styling Enhancements**:
    *   **Sidebar**: Wrapped in a shadowed, bordered container with rounded corners.
    *   **Typography**: Applied 'Segoe UI' font for a clean, modern look.
    *   **Icons**: Integrated text-based icons for navigation items.

4.  **Features**:
    *   **Authentication**: Real integration with `Database` class + Manual Fallback.
    *   **Feed**: Dynamic post loading from the shared database.
    *   **Interactivity**: "Friends" sidebar item now opens a Friends List view.
    *   **Logout**: Added a logout button (door icon) in the top navigation bar to return to the login screen.

## How to Run

### Using IntelliJ IDEA (Recommended)
1.  **Reload Maven Project**: Right-click on `pom.xml` and select "Maven" > "Reload Project".
2.  **Run**: Open `src/main/java/com/facebook/HelloApplication.java` and click the green Run button.

### Manual Login (Fallback)
If you cannot connect to the database or want to test quickly:
*   **Username**: `admin`
*   **Password**: `admin`

## Next Steps
*   Ensure your `G:` drive is mounted and accessible.
*   Run the application to see your real data synced from the shared drive.
