import java.io.*;
import java.util.*;

class Book implements Serializable {
    private String title;
    private String author;
    private String isbn;
    private int quantity;
    private int sold;

    public Book(String title, String author, String isbn, int quantity) {
        // Validate title
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty.");
        }
        this.title = title;

        // Validate author
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty.");
        }
        this.author = author;

        // Validate ISBN
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty.");
        }
        this.isbn = isbn;

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive integer.");
        }
        this.quantity = quantity;

        // Initialize sold copies to 0
        this.sold = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSold() {
        return sold;
    }

    public void buyBook() {
        if (quantity > 0) {
            quantity--;
            sold++;
        } else {
            throw new IllegalStateException("Book is out of stock.");
        }
    }

    @Override
    public String toString() {
        return String.format("Title: %s, Author: %s, ISBN: %s, Quantity: %d, Sold: %d",
                title, author, isbn, quantity, sold);
    }
}


class Borrower implements Serializable {
    private String name;
    private String id;

    public Borrower(String name, String id) {

        if(name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        this.name = name;

        if(id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty.");
        }
        this.id = id;

    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, ID: %s", name, id);
    }
}


class Library implements Serializable {
    private List<Book> books;
    private List<Borrower> borrowers;
    private Map<Book, Borrower> borrowingRecords;

    public Library() {
        books = new ArrayList<>();
        borrowers = new ArrayList<>();
        borrowingRecords = new HashMap<>();
    }

    // Add a method to load books from a CSV file
    public void loadBooksFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String title = parts[0].trim();
                    String author = parts[1].trim();
                    String isbn = parts[2].trim();
                    int quantity = Integer.parseInt(parts[3].trim());
                    books.add(new Book(title, author, isbn, quantity));
                }
            }

            System.out.println("Books loaded from file: " + filename);
        }
        catch (IOException e) {
            System.out.println("Error loading books from file: " + e.getMessage());
        }
    }


    // Add a method to load borrowers from a CSV file
    public void loadBorrowersFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    String id = parts[1].trim();
                    borrowers.add(new Borrower(name, id));
                }
            }

            System.out.println("Borrowers loaded from file: " + filename);
        }
        catch (IOException e) {
            System.out.println("Error loading borrowers from file: " + e.getMessage());
        }
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(String isbn) {
        books.removeIf(book -> book.getIsbn().equals(isbn));
    }

    public Book searchBook(String query) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(query) || book.getAuthor().equalsIgnoreCase(query) || book.getIsbn().equals(query)) {
                return book;
            }
        }
        return null;
    }

    public void addBorrower(Borrower borrower) {
        borrowers.add(borrower);
    }

    public void removeBorrower(String id) {
        borrowers.removeIf(borrower -> borrower.getId().equals(id));
    }

    public void borrowBook(String isbn, String borrowerId) {
        Book book = searchBook(isbn);

        Borrower borrower = borrowers.stream()
                .filter(b -> b.getId().equals(borrowerId))
                .findFirst()
                .orElse(null);

        if (book != null && borrower != null && book.getQuantity() > 0) {
            borrowingRecords.put(book, borrower);
            book.setQuantity(book.getQuantity() - 1);
            System.out.println("Book borrowed successfully!");
        }
        else {
            System.out.println("Book or borrower not found, or book out of stock.");
        }
    }

    public void returnBook(String isbn) {
        Book book = searchBook(isbn);
        if (book != null && borrowingRecords.containsKey(book)) {
            borrowingRecords.remove(book);
            book.setQuantity(book.getQuantity() + 1);
            System.out.println("Book returned successfully!");
        }
        else {
            System.out.println("Book not found or not borrowed.");
        }
    }

    public void displayBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available.");
        }
        else {
            books.forEach(System.out::println);
        }
    }

    public void displayBorrowers() {
        if (borrowers.isEmpty()) {
            System.out.println("No borrowers registered.");
        }
        else {
            borrowers.forEach(System.out::println);
        }
    }

    public void saveData(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
            System.out.println("Data saved successfully!");
        }
        catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public static Library loadData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Library) ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
            return new Library();
        }
    }

    public void buyBook(String isbn, String borrowerId) {
        Book book = searchBook(isbn);
        Borrower borrower = borrowers.stream()
                .filter(b -> b.getId().equals(borrowerId))
                .findFirst()
                .orElse(null);

        if (book != null && borrower != null) {
            try {
                book.buyBook();
                System.out.println("Book purchased successfully!");
            } catch (IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Book or borrower not found.");
        }
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Library library;

        // Load data if file exists
        File file = new File("library_data.ser");
        if (file.exists()) {
            library = Library.loadData("library_data.ser");
        }
        else {
            library = new Library();
            // Load books and borrowers from input files
            library.loadBooksFromFile("books.csv");
            library.loadBorrowersFromFile("borrowers.csv");
        }

        while (true) {
            System.out.println("\n--- Welcome in our Library Management System" +
                    "Please Choose a number between 1 and 10 ---");

            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. Search Book");
            System.out.println("4. Add Borrower");
            System.out.println("5. Borrow Book");
            System.out.println("6. Return Book");
            System.out.println("7. Buy Book");
            System.out.println("8. Display All Books");
            System.out.println("9. Display All Borrowers");
            System.out.println("10. Save and Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Enter book title: ");
                    String title = scanner.nextLine();

                    System.out.println("Enter book author: ");
                    String author = scanner.nextLine();

                    System.out.println("Enter book ISBN: ");
                    String isbn = scanner.nextLine();

                    System.out.println("Enter book quantity: ");
                    int quantity = scanner.nextInt();

                    scanner.nextLine();
                    library.addBook(new Book(title, author, isbn, quantity));
                    break;

                case 2:
                    System.out.print("Enter ISBN of the book to remove: ");
                    String removeIsbn = scanner.nextLine();
                    library.removeBook(removeIsbn);
                    break;

                case 3:
                    System.out.print("Enter title, author, or ISBN to search: ");
                    String searchQuery = scanner.nextLine();
                    Book foundBook = library.searchBook(searchQuery);
                    if (foundBook != null) {
                        System.out.println("Book found: " + foundBook);
                    }
                    else {
                        System.out.println("Book not found.");
                    }
                    break;

                case 4:
                    System.out.print("Enter borrower name: ");
                    String name = scanner.nextLine();

                    System.out.print("Enter borrower ID: ");
                    String id = scanner.nextLine();

                    library.addBorrower(new Borrower(name, id));
                    break;

                case 5:
                    System.out.print("Enter ISBN of the book to borrow: ");
                    String borrowIsbn = scanner.nextLine();

                    System.out.print("Enter borrower ID: ");
                    String borrowId = scanner.nextLine();

                    library.borrowBook(borrowIsbn, borrowId);
                    break;

                case 6:
                    System.out.print("Enter ISBN of the book to return: ");
                    String returnIsbn = scanner.nextLine();
                    library.returnBook(returnIsbn);
                    break;

                case 7:
                    // Buy book logic
                    System.out.print("Enter ISBN of the book to buy: ");
                    String buyIsbn = scanner.nextLine();
                    System.out.print("Enter borrower ID: ");
                    String buyBorrowerId = scanner.nextLine();
                    library.buyBook(buyIsbn, buyBorrowerId);
                    break;

                case 8:
                    System.out.println("\n--- All Books ---");
                    library.displayBooks();
                    break;

                case 9:
                    System.out.println("\n--- All Borrowers ---");
                    library.displayBorrowers();
                    break;

                case 10:
                    library.saveData("library_data.ser");
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice." +
                            " Please try again.");
            }
        }
    }
}