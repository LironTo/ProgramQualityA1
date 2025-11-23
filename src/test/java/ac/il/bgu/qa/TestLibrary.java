package ac.il.bgu.qa;

// JUnit 5
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

// Java standard library
import java.util.Arrays;


// Mockito
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

// Your classes
import ac.il.bgu.qa.*;
import ac.il.bgu.qa.services.*;
import ac.il.bgu.qa.errors.*;

public class TestLibrary {
// Implement here'
    
    
    @Mock
    DatabaseService mockDatabase;
    @Mock
    ReviewService mockReviewService;
    @Mock
    NotificationService mockNotificationService;
    @Mock
    Book book;


    @BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

    // addBook tests
    @Test
    public void givenNullBook_whenAddBook_thenThrowIllegalArgumentException()  {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);

        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(null)
        );
        Assertions.assertEquals("Invalid book.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "123",                     // too short
            "97803064A06157",          // contains a letter
            "9780306406158",           // 13 digits, wrong check digit
            "978-0-306-40615-X"        // hyphen + invalid character
    })
    public void givenInvalidISBN_whenAddBook_thenThrowIllegalArgumentException(String invalidISBN) {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book invalidBook = new Book(invalidISBN, "Some Title", "Some Author");

        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(invalidBook)
        );
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }
    @Test
    public void givenNullTitle_whenAddBook_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book invalidBook = new Book("123-456-789", null, "123");

        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(invalidBook)
        );
        Assertions.assertEquals("Invalid title.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }
    @Test
    public void givenEmptyTitle_whenAddBook_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book invalidBook = new Book("123-456-789", "", "123");

        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(invalidBook)
        );
        Assertions.assertEquals("Invalid title.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",                 // empty
        "1John",            // starts with number
        "John2",            // ends with number
        "John@Doe",         // contains invalid symb    ol
        "John--Doe",        // consecutive hyphens
        "O''Connor",        // consecutive apostrophes
        ".John",            // starts with dot
        "John.",            // ends with dot
        "-Mary",            // starts with hyphen
        "Mary-",            // ends with hyphen
        "''Alice",          // starts with consecutive apostrophes
        "Alice''",          // ends with consecutive apostrophes
        "John_Doe",         // contains underscore (invalid)
        "John#Doe"          // contains hash (invalid)
})
    public void givenInvalidAuthor_whenAddBook_thenThrowIllegalArgumentException(String invalidAuthor){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("9780306406157", "Some Title", invalidAuthor);
        // Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(validBook)
        );
        Assertions.assertEquals("Invalid author.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }
    @Test
    public void givenBorrowedBook_whenAddBook_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("123-456-789", "Some Title", "Some Author");
        validBook.borrow();
        // Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(validBook)
        );
        Assertions.assertEquals("Book with invalid borrowed state.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }
    @Test
    public void givenExistingBookInDatabase_whenAddBook_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("123-456-789", "Some Title", "Some Author");
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(validBook)
        );
        Assertions.assertEquals("Book already exists.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
        verify(mockDatabase, times(1)).getBookByISBN(validBook.getISBN());
    }
    @Test
    public void givenValidBook_whenAddBook_thenAddBookToDatabase(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("9780306406157", "Some Title", "Some Author");
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(null);
        // Act
        library.addBook(validBook);
        // Assert
        verify(mockDatabase, times(1)).getBookByISBN(validBook.getISBN());
        verify(mockDatabase, times(1)).addBook(validBook.getISBN(), validBook);
    }


    // registerUser tests
    @Test
    public void givenNullUser_whenRegisterUser_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.registerUser(null)
        );
        Assertions.assertEquals("Invalid user.", thrown.getMessage());
        verify(mockDatabase, never()).registerUser(anyString(), any(User.class));
    }
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { 
        "",                 // empty
        "12345678901A",     // 12 characters with letter
        "1234567890",       // 10 characters
        "1234567890123"     // 13 characters
    })
    public void givenUserWithInvalidId_whenRegisterUser_thenThrowIllegalArgumentException(String invalidUserId) {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        NotificationService n = new NotificationService() {
            @Override
            public void notifyUser(String userId, String message) {
                // Do nothing
            }
        };

        User invalidUser = new User("Valid Name", invalidUserId, n);

        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.registerUser(invalidUser)
        );

        Assertions.assertEquals("Invalid user Id.", thrown.getMessage());
        verify(mockDatabase, never()).registerUser(anyString(), any(User.class));
    }
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",                     // empty
    })
    public void givenUserIllegalName_whenRegisterUser_thenThrowIllegalArgumentException(String illegalName){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        NotificationService n = new NotificationService() {
            @Override
            public void notifyUser(String userId, String message) {
                // Do nothing
            }
        };
        User invalidUser = new User(illegalName, "12345678901", n);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.registerUser(invalidUser)
        );
        Assertions.assertEquals("Invalid user name.", thrown.getMessage());
        verify(mockDatabase, never()).registerUser(anyString(), any(User.class));
    }
    @Test
    public void givenUserWithNullNotificationService_whenRegisterUser_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        User invalidUser = new User("Some Name", "12345678901", null);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.registerUser(invalidUser)
        );
        Assertions.assertEquals("Invalid notification service.", thrown.getMessage());
        verify(mockDatabase, never()).registerUser(anyString(), any(User.class));
    }
    @Test
    public void givenExistingUser_whenRegisterUser_thenThrowIllegalArgumentException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        NotificationService n = new NotificationService() {
            @Override
            public void notifyUser(String userId, String message) {
                // Do nothing
            }
        };
        User validUser = new User("Some Name", "12345678901", n);
        when(mockDatabase.getUserById(validUser.getId())).thenReturn(validUser);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.registerUser(validUser)
        );
        Assertions.assertEquals("User already exists.", thrown.getMessage());
        verify(mockDatabase, never()).registerUser(anyString(), any(User.class));
        verify(mockDatabase, times(1)).getUserById(validUser.getId());
    }
    @Test
    public void givenValidUser_whenRegisterUser_thenRegisterUserInDatabase(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        NotificationService n = new NotificationService() {
            @Override
            public void notifyUser(String userId, String message) {
                // Do nothing
            }
        };
        User validUser = new User("Some Name", "12345678901", n);
        when(mockDatabase.getUserById(validUser.getId())).thenReturn(null);
        // Act
        library.registerUser(validUser);
        // Assert
        verify(mockDatabase, times(1)).getUserById(validUser.getId());
        verify(mockDatabase, times(1)).registerUser(validUser.getId(), validUser);
    }




    // borrowBook tests
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "123",                     // too short
        "97803064A6157",          // contains a letter
        "9780306406158",           // 13 digits, wrong check digit
        "978-0-306-40615-X"        // hyphen + invalid character
    })
    public void givenInvalidISBN_whenBorrowBook_thenThrowIllegalArgumentException(String invalidISBN) {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        // Act
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.borrowBook(invalidISBN, "12345678901")
        );
        // Assert
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
        verify(mockDatabase, never()).getBookByISBN(invalidISBN);
        verify(mockDatabase, never()).getUserById("12345678901");
    }

    @Test
    public void givenNonExistingBook_whenBorrowBook_thenThrowBookNotFoundException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN("1234567890123")).thenReturn(null);
        // Act
        BookNotFoundException thrown = Assertions.assertThrows(
            BookNotFoundException.class,
            () -> library.borrowBook("1234567890123", "12345678901")
        );
        // Assert
        Assertions.assertEquals("Book not found!", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
        verify(mockDatabase, times(1)).getBookByISBN("1234567890123");
        verify(mockDatabase, never()).getUserById("12345678901");
    }

    @Test
    public void givenNonExistingUser_whenBorrowBook_thenThrowUserNotRegisteredException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN("9780306406157")).thenReturn(new Book("9780306406157", "Some Title", "Some Author"));
        when(mockDatabase.getUserById("12345678901")).thenReturn(null);
        // Act
        UserNotRegisteredException thrown = Assertions.assertThrows(
            UserNotRegisteredException.class,
            () -> library.borrowBook("9780306406157", "12345678901")
        );
        // Assert
        Assertions.assertEquals("User not found!", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
        verify(mockDatabase, times(1)).getBookByISBN("9780306406157");
        verify(mockDatabase, times(1)).getUserById("12345678901");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",                     // empty
        "12345678901A",     // 12 characters with letter
        "1234567890",       // 10 characters
        "1234567890123"     // 13 characters
    })
    public void givenInvalidUser_whenBorrowBook_thenThrowIllegalArgumentException(String invalidUserId) {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN("9780306406157")).thenReturn(new Book("9780306406157", "Some Title", "Some Author"));
        // Act
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.borrowBook("9780306406157", invalidUserId)
        );
        // Assert
        Assertions.assertEquals("Invalid user Id.", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
        verify(mockDatabase, times(1)).getBookByISBN("9780306406157");
        verify(mockDatabase, never()).getUserById(invalidUserId);
    }

    @Test
    public void givenBorrowedBook_whenBorrowBook_thenThrowBookAlreadyBorrowedException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("9780306406157", "Some Title", "Some Author");
        User validUser = new User("Some Name", "12345678901", mockNotificationService);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        validBook.borrow();
        // Act
        BookAlreadyBorrowedException thrown = Assertions.assertThrows(
            BookAlreadyBorrowedException.class,
            () -> library.borrowBook(validBook.getISBN(), validUser.getId())
        );
        // Assert
        Assertions.assertEquals("Book is already borrowed!", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
    }

    @Test
    public void givenValidBookAndUser_whenBorrowBook_thenBorrowBookInDatabase(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("9780306406157", "Some Title", "Some Author");
        User validUser = new User("Some Name", "12345678901", mockNotificationService);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        when(mockDatabase.getUserById(validUser.getId())).thenReturn(validUser);
        // Act
        library.borrowBook(validBook.getISBN(), validUser.getId());
        // Assert
        Assertions.assertTrue(validBook.isBorrowed());
        verify(mockDatabase, times(1)).borrowBook(validBook.getISBN(), validUser.getId());
        verify(mockDatabase, times(1)).getBookByISBN(validBook.getISBN());
        verify(mockDatabase, times(1)).getUserById(validUser.getId());
        verify(validBook, times(1)).borrow();
    }



    // returnBook tests
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "123",                     // too short
        "97803064A6157",          // contains a letter
        "9780306406158",           // 13 digits, wrong check digit
        "978-0-306-40615-X"        // hyphen + invalid character
    })
    public void givenInvalidISBN_whenReturnBook_thenThrowIllegalArgumentException(String invalidISBN) {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        // Act
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.returnBook(invalidISBN)
        );
        // Assert
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockDatabase, never()).returnBook(anyString());
        verify(mockDatabase, never()).getBookByISBN(invalidISBN);
    }

    @Test
    public void givenNonExistingBook_whenReturnBook_thenThrowBookNotFoundException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN("9780306406157")).thenReturn(null);
        // Act
        BookNotFoundException thrown = Assertions.assertThrows(
            BookNotFoundException.class,
            () -> library.returnBook("9780306406157")
        );
        // Assert
        Assertions.assertEquals("Book not found!", thrown.getMessage());
        verify(mockDatabase, never()).returnBook(anyString());
        verify(mockDatabase, times(1)).getBookByISBN("9780306406157");
    }

    @Test
    public void givenNonBorrowedBook_whenReturnBook_thenThrowBookNotBorrowedException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN("9780306406157")).thenReturn(new Book("9780306406157", "Some Title", "Some Author"));
        // Act
        BookNotBorrowedException thrown = Assertions.assertThrows(
            BookNotBorrowedException.class,
            () -> library.returnBook("9780306406157")
        );
        // Assert
        Assertions.assertEquals("Book wasn't borrowed!", thrown.getMessage());
        verify(mockDatabase, never()).returnBook(anyString());
        verify(mockDatabase, times(1)).getBookByISBN("9780306406157");
    }

    @Test
    public void givenValidBook_whenReturnBook_thenReturnBookInDatabase(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = new Book("9780306406157", "Some Title", "Some Author");
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        validBook.borrow();
        // Act
        library.returnBook(validBook.getISBN());
        // Assert
        Assertions.assertFalse(validBook.isBorrowed());
        verify(mockDatabase, times(1)).returnBook(validBook.getISBN());
    }
}
