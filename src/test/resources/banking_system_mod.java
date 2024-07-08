import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class BankAccount {
    private String accountNumber;
    private String accountHolderName;
    private double balance;

    public BankAccount(String accountNumber, String accountHolderName) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = 0.0;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Successfully deposited: $" + amount);
        } else {
            System.out.println("Deposit amount must be positive.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Successfully withdrew: $" + amount);
        } else if (amount > balance) {
            System.out.println("Insufficient funds.");
        } else {
            System.out.println("Withdrawal amount must be positive.");
        }
    }

    public void displayBalance() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Holder: " + accountHolderName);
        System.out.println("Current Balance: $" + balance);
    }
}

public class BankingSystem {
    private static Map<String, BankAccount> accounts = new HashMap<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nBanking System Menu:");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline left-over

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    performTransaction("deposit");
                    break;
                case 3:
                    performTransaction("withdraw");
                    break;
                case 4:
                    checkBalance();
                    break;
                case 5:
                    System.out.println("Exiting the banking system. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createAccount() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter account holder name: ");
        String accountHolderName = scanner.nextLine();

        if (accounts.containsKey(accountNumber)) {
            System.out.println("Account number already exists. Please try again.");
        } else {
            BankAccount newAccount = new BankAccount(accountNumber, accountHolderName);
            accounts.put(accountNumber, newAccount);
            System.out.println("Account successfully created.");
        }
    }

    private static void performTransaction(String transactionType) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = accounts.get(accountNumber);

        if (account != null) {
            System.out.print("Enter amount to " + transactionType + ": ");
            double amount = scanner.nextDouble();
            if (transactionType.equals("deposit")) {
                account.deposit(amount);
            } else if (transactionType.equals("withdraw")) {
                account.withdraw(amount);
            }
        } else {
            System.out.println("Account not found. Please try again.");
        }
    }

    private static void checkBalance() {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = accounts.get(accountNumber);

        if (account != null) {
            account.displayBalance();
        } else {
            System.out.println("Account not found. Please try again.");
        }
    }
}
