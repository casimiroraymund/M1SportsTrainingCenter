import java.util.Scanner;

public class LoginRegister {

    private final DatabaseRepository repo;

    public LoginRegister(DatabaseRepository repo) {
        this.repo = repo;
    }

    public void userRegister(Scanner sc) {
        System.out.println("\n=== Registration ===");

        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) { System.out.println("Email cannot be empty."); return; }

        System.out.print("Full Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Full Name cannot be empty."); return; }

        System.out.print("Sport: ");
        String sportType = sc.nextLine().trim();
        if (sportType.isEmpty()) { System.out.println("Sport cannot be empty."); return; }

        String role = "";
        while (true) {
            System.out.println("Select Role: 1. Athlete  2. Trainer");
            String input = sc.nextLine().trim();
            if (input.equals("1")) { role = "athlete"; break; }
            else if (input.equals("2")) { role = "trainer"; break; }
            else System.out.println("Invalid choice.");
        }

        System.out.print("Password: ");
        String password = sc.nextLine();
        System.out.print("Confirm Password: ");
        String confirm = sc.nextLine();

        if (!password.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }

        if (repo.emailExists(email)) {
            System.out.println("Email already exists.");
            return;
        }

        repo.registerUser(name, email, password, role, sportType);
    }

    public String userLogin(Scanner sc) {
        System.out.print("Enter email: ");
        String email = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        User user = repo.loginUser(email, password);

        if (user != null) {
            System.out.println("\nLogin successful! Welcome, " + user.getName());
            return email;
        } else {
            System.out.println("Invalid email or password.");
            return null;
        }
    }
}
