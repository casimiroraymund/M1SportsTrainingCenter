import java.sql.*;
import java.util.UUID;

public class DatabaseRepository {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/franciskiersarte/Downloads/mitry.db";

    // ====================== TABLE CREATION ======================
    public void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "sport_type TEXT)";
        executeUpdate(sql);
    }

    public void createSessionTables() {
        String sessionSql = "CREATE TABLE IF NOT EXISTS sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "label TEXT NOT NULL," +
                "date TEXT NOT NULL," +
                "coach TEXT NOT NULL," +
                "slots INTEGER NOT NULL," +
                "description TEXT)";

        String reservationSql = "CREATE TABLE IF NOT EXISTS reservations (" +
                "reservationID TEXT PRIMARY KEY," +
                "sessionID INTEGER NOT NULL," +
                "athleteEmail TEXT NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY(sessionID) REFERENCES sessions(id))";

        executeUpdate(sessionSql);
        executeUpdate(reservationSql);
    }

    // ====================== USER METHODS ======================
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void registerUser(String name, String email, String password, String role, String sportType) {
        String sql = "INSERT INTO users(name, email, password, role, sport_type) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            pstmt.setString(5, sportType);
            pstmt.executeUpdate();
            System.out.println("Account created successfully.");
        } catch (SQLException e) {
            System.out.println("Database error during registration.");
            e.printStackTrace();
        }
    }

    public User loginUser(String email, String password) {
        String sql = "SELECT name, role, sport_type FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("name"), email, rs.getString("role"), rs.getString("sport_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserProfile(String email) {
        String sql = "SELECT name, email, role, sport_type FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("name"), rs.getString("email"),
                        rs.getString("role"), rs.getString("sport_type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ====================== SESSION & RESERVATION METHODS ======================

    public void viewAllTrainingSessions() {
        String sql = "SELECT * FROM sessions";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasSessions = false;
            while (rs.next()) {
                hasSessions = true;
                System.out.println("Session ID     : " + rs.getInt("id"));
                System.out.println("Training Title : " + rs.getString("label"));
                System.out.println("Date & Time    : " + rs.getString("date"));
                System.out.println("Trainer        : " + rs.getString("coach"));
                System.out.println("Location       : " + rs.getString("description"));
                System.out.println("Available Slots: " + rs.getInt("slots"));
                System.out.println("-----------------------------------");
            }
            if (!hasSessions) {
                System.out.println("No training sessions available.");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving sessions.");
            e.printStackTrace();
        }
    }

    public boolean bookTrainingSession(int sessionID, String athleteEmail) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String checkSql = "SELECT slots FROM sessions WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, sessionID);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || rs.getInt("slots") <= 0) {
                    System.out.println("Session not found or no slots available.");
                    return false;
                }
            }

            String updateSql = "UPDATE sessions SET slots = slots - 1 WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, sessionID);
                updateStmt.executeUpdate();
            }

            String reservationID = UUID.randomUUID().toString();
            String insertSql = "INSERT INTO reservations(reservationID, sessionID, athleteEmail, status) VALUES(?,?,?,?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, reservationID);
                insertStmt.setInt(2, sessionID);
                insertStmt.setString(3, athleteEmail);
                insertStmt.setString(4, "Confirmed");
                insertStmt.executeUpdate();
            }

            System.out.println("Booking successful! Reservation ID: " + reservationID + "\n");
            return true;

        } catch (SQLException e) {
            System.out.println("Error booking session.");
            e.printStackTrace();
            return false;
        }
    }

    public void viewMyReservations(String athleteEmail) {
        String sql = "SELECT r.reservationID, s.label, s.date, s.coach, s.description " +
                "FROM reservations r JOIN sessions s ON r.sessionID = s.id " +
                "WHERE r.athleteEmail = ? AND r.status = 'Confirmed'";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, athleteEmail);
            ResultSet rs = pstmt.executeQuery();

            boolean hasReservations = false;
            System.out.println("\n=== Your Current Reservations ===");

            while (rs.next()) {
                hasReservations = true;
                System.out.println("Reservation ID : " + rs.getString("reservationID"));
                System.out.println("Training Title : " + rs.getString("label"));
                System.out.println("Date & Time    : " + rs.getString("date"));
                System.out.println("Trainer        : " + rs.getString("coach"));
                System.out.println("Location       : " + rs.getString("description"));
                System.out.println("-----------------------------------");
            }

            if (!hasReservations) {
                System.out.println("You have no active reservations.");
            }
            System.out.println();
        } catch (SQLException e) {
            System.out.println("Error retrieving reservations.");
            e.printStackTrace();
        }
    }

    public boolean cancelReservation(String reservationID, String athleteEmail) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            String checkSql = "SELECT sessionID FROM reservations WHERE reservationID = ? AND athleteEmail = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, reservationID);
                checkStmt.setString(2, athleteEmail);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    return false;
                }

                int sessionID = rs.getInt("sessionID");

                // Delete reservation
                String deleteSql = "DELETE FROM reservations WHERE reservationID = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, reservationID);
                    deleteStmt.executeUpdate();
                }

                // Restore slot
                String updateSql = "UPDATE sessions SET slots = slots + 1 WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, sessionID);
                    updateStmt.executeUpdate();
                }

                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error cancelling reservation.");
            e.printStackTrace();
            return false;
        }
    }

    // Helper
    private void executeUpdate(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database setup error.");
            e.printStackTrace();
        }
    }

    public void bookTrainingSession() {
    }
}