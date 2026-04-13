import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DatabaseRepository repo = new DatabaseRepository();
        LoginRegister loginRegister = new LoginRegister(repo);

        // Initialize all tables
        repo.createUserTable();
        repo.createSessionTables();

        while (true) {
            System.out.println("\n=== Welcome to Sports Training Academy ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    String loggedInEmail = loginRegister.userLogin(sc);
                    if (loggedInEmail != null) {
                        athleteMenu(sc, loggedInEmail, repo);
                    }
                    break;

                case "2":
                    loginRegister.userRegister(sc);
                    break;

                case "3":
                    System.out.println("Thank you! Exiting program...");
                    sc.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void athleteMenu(Scanner sc, String email, DatabaseRepository repo) {
        ViewTrainingSession viewTraining = new ViewTrainingSession(repo);
        BookTrainingSession bookSession = new BookTrainingSession(repo);
        ViewUserProfile viewProfile = new ViewUserProfile(repo);
        CancelReservation cancelReservation = new CancelReservation(repo);

        while (true) {
            System.out.println("\n--- Athlete Menu ---");
            System.out.println("1. View Training Sessions");
            System.out.println("2. Book a Session");
            System.out.println("3. View Profile");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. Logout");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            if (choice.isEmpty()) {
                continue;
            }

            switch (choice) {
                case "1":
                    viewTraining.viewTrainingSessions();
                    break;
                case "2":
                    bookSession.bookTrainingSession(sc, email);
                    break;
                case "3":
                    viewProfile.viewUserProfile(email);
                    break;
                case "4":
                    cancelReservation.cancelReservation(sc, email);
                    break;
                case "5":
                    System.out.println("Logging out... Goodbye!\n");
                    return;
                default:
                    System.out.println("Invalid choice! Please enter 1-5.\n");
            }
        }
    }
}