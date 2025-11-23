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
import java.util.List;
import java.util.ArrayList;

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
    
    // Setup mock objects
    @Mock
    DatabaseService mockDatabase;
    @Mock
    ReviewService mockReviewService;
    @Mock
    NotificationService mockNotificationService;
    @Mock
    Book mockBook;
    @Mock
    User mockUser;
    @Spy
    List<String> spyReviewsList = new ArrayList<>();

    // can't be used, only for copy-pasting
    private final String[] invalidISBNs = {
        "123",                     // too short
        "97803064A06157",          // contains a letter
        "9780306406158",           // 13 digits, wrong check digit
        "978-0-306-40615-X"        // hyphen + invalid character
    };
    private final String[] invalidTitles = {
        ""
    };
    private final String[] invalidAuthors = {
        "",                 // empty
        "1John",            // starts with number
        "John2",            // ends with number
        "John@Doe",         // contains invalid symbol
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
    };               
    private final String[] invalidUserIds = { 
            "",                 // empty
            "12345678901A",     // 12 characters with letter
            "1234567890",       // 10 characters
            "1234567890123"     // 13 characters
    };


    // Initialize mock objects
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
        Book invalidBook = mockBook;
        when(invalidBook.getISBN()).thenReturn(invalidISBN);

        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.addBook(invalidBook)
        );
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockDatabase, never()).addBook(anyString(), any(Book.class));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",                // empty
    })
    public void givenInvalidTitle_whenAddBook_thenThrowIllegalArgumentException(String invalidTitle){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book invalidBook = mockBook;
        when(invalidBook.getTitle()).thenReturn(invalidTitle);

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
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.isBorrowed()).thenReturn(false);
        when(validBook.getAuthor()).thenReturn(invalidAuthor);
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
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("123-456-789");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(true);
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
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("123-456-789");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(false);
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
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(false);
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
        User invalidUser = mockUser;
        when(invalidUser.getId()).thenReturn(invalidUserId);
        when(invalidUser.getName()).thenReturn("Some Name");
        when(invalidUser.getNotificationService()).thenReturn(mockNotificationService);
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
        User invalidUser = mockUser;
        when(invalidUser.getName()).thenReturn(illegalName);
        when(invalidUser.getId()).thenReturn("123456789012");
        when(invalidUser.getNotificationService()).thenReturn(mockNotificationService);
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
        User invalidUser = mockUser;
        when(invalidUser.getName()).thenReturn("Some Name");
        when(invalidUser.getId()).thenReturn("123456789012");
        when(invalidUser.getNotificationService()).thenReturn(null);
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
        User validUser = mockUser;
        when(validUser.getName()).thenReturn("Some Name");
        when(validUser.getId()).thenReturn("123456789012");
        when(validUser.getNotificationService()).thenReturn(mockNotificationService);
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
        User validUser = mockUser;
        when(validUser.getName()).thenReturn("Some Name");
        when(validUser.getId()).thenReturn("123456789012");
        when(validUser.getNotificationService()).thenReturn(mockNotificationService);
        when(mockDatabase.getUserById(validUser.getId())).thenReturn(null);
        // Act
        library.registerUser(validUser);
        // Assert
        verify(mockDatabase, times(1)).getUserById(validUser.getId());
        verify(mockDatabase, times(1)).registerUser(validUser.getId(), validUser);
    }



    //        borrowBook tests
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "123",                     // too short
        "97803064A06157",          // contains a letter
        "9780306406158",           // 13 digits, wrong check digit
        "978-0-306-40615-X"        // hyphen + invalid character
    })
    public void givenInvalidISBN_whenBorrowBook_thenThrowIllegalArgumentException(String invalidISBN){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book invalidBook = mockBook;
        when(invalidBook.getISBN()).thenReturn(invalidISBN);
        when(invalidBook.getTitle()).thenReturn("Some Title");
        when(invalidBook.getAuthor()).thenReturn("Some Author");
        when(invalidBook.isBorrowed()).thenReturn(false);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.borrowBook(invalidISBN, "123456789012")
        );
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
    }

    @Test
    public void givenNullBook_whenBorrowBook_thenThrowBookNotFoundException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(null);
        // Act & Assert
        BookNotFoundException thrown = Assertions.assertThrows(
            BookNotFoundException.class,
            () -> library.borrowBook("123-456-789", "123456789012")
        );
        Assertions.assertEquals("Book not found!", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { 
        "",                 // empty
        "12345678901A",     // 12 characters with letter
        "1234567890",       // 10 characters
        "1234567890123"     // 13 characters
    })
    public void givenInvalidUserId_whenBorrowBook_thenThrowIllegalArgumentException(String invalidUserId){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(false);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        when(mockDatabase.getUserById(invalidUserId)).thenReturn(null);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.borrowBook("9780306406157", invalidUserId)
        );
        Assertions.assertEquals("Invalid user Id.", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
    }

    @Test
    public void givenValidUserIDThatDoesntExist_whenBorrowBook_thenThrowUserNotRegisteredException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(false);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        when(mockDatabase.getUserById("123456789012")).thenReturn(null);
        // Act & Assert
        UserNotRegisteredException thrown = Assertions.assertThrows(
            UserNotRegisteredException.class,
            () -> library.borrowBook("9780306406157", "123456789012")
        );
        Assertions.assertEquals("User not found!", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
    }

    @Test
    public void givenBorrowedValidBook_whenBorrowBook_thenThrowBookAlreadyBorrowedException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(true);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        when(mockDatabase.getUserById("123456789012")).thenReturn(mockUser);
        // Act & Assert
        BookAlreadyBorrowedException thrown = Assertions.assertThrows(
            BookAlreadyBorrowedException.class,
            () -> library.borrowBook("9780306406157", "123456789012")
        );
        Assertions.assertEquals("Book is already borrowed!", thrown.getMessage());
        verify(mockDatabase, never()).borrowBook(anyString(), anyString());
    }

    @Test
    public void givenValidBookAndUser_whenBorrowBook_thenBorrowBookInDatabase(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(false);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        when(mockDatabase.getUserById("123456789012")).thenReturn(mockUser);
        doNothing().when(validBook).borrow();
        // Act
        library.borrowBook("9780306406157", "123456789012");
        // Assert
        verify(mockDatabase, times(1)).borrowBook(validBook.getISBN(), "123456789012");
        verify(validBook, times(1)).borrow();
    }



    //       returnBook tests
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
        verify(mockDatabase, never()).getBookByISBN(anyString());
    }

    @Test
    public void givenNonExistingBook_whenReturnBook_thenThrowBookNotFoundException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(null);
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
        Book invalidBook = mockBook;
        when(invalidBook.getISBN()).thenReturn("9780306406157");
        when(invalidBook.getTitle()).thenReturn("Some Title");
        when(invalidBook.getAuthor()).thenReturn("Some Author");
        when(invalidBook.isBorrowed()).thenReturn(false);
        when(mockDatabase.getBookByISBN(invalidBook.getISBN())).thenReturn(invalidBook);
        // Act
        BookNotBorrowedException thrown = Assertions.assertThrows(
            BookNotBorrowedException.class,
            () -> library.returnBook(invalidBook.getISBN())
        );
        // Assert
        Assertions.assertEquals("Book wasn't borrowed!", thrown.getMessage());
        verify(mockDatabase, never()).returnBook(anyString());
        verify(mockDatabase, times(1)).getBookByISBN(invalidBook.getISBN());
    }

    @Test
    public void givenValidBook_whenReturnBook_thenReturnBookInDatabase(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(true);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        doNothing().when(validBook).returnBook();
        // Act
        library.returnBook(validBook.getISBN());
        // Assert
        verify(mockDatabase, times(1)).returnBook(validBook.getISBN());
        verify(validBook, times(1)).returnBook();
    }



    // notifyUserWithBookReviews tests
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "123",                     // too short
        "97803064A06157",          // contains a letter
        "9780306406158",           // 13 digits, wrong check digit
        "978-0-306-40615-X"        // hyphen + invalid character
    })
    public void givenInvalidISBN_whenNotifyUserWithBookReviews_thenThrowIllegalArgumentException(String invalidISBN){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.notifyUserWithBookReviews(invalidISBN, "123456789012")
        );
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",                 // empty
        "12345678901A",     // 12 characters with letter
        "1234567890",       // 10 characters
        "1234567890123"     // 13 characters
    })
    public void givenInvalidUserId_whenNotifyUserWithBookReviews_thenThrowIllegalArgumentException(String invalidUserId){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        Book validBook = mockBook;
        when(validBook.getISBN()).thenReturn("9780306406157");
        when(validBook.getTitle()).thenReturn("Some Title");
        when(validBook.getAuthor()).thenReturn("Some Author");
        when(validBook.isBorrowed()).thenReturn(false);
        when(mockDatabase.getBookByISBN(validBook.getISBN())).thenReturn(validBook);
        when(mockDatabase.getUserById(invalidUserId)).thenReturn(null);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.notifyUserWithBookReviews("9780306406157", invalidUserId)
        );
        Assertions.assertEquals("Invalid user Id.", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());
    }

    @Test
    public void givenValidIsbnWithNullBook_whenNotifyUserWithBookReviews_thenThrowBookNotFoundException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(null);
        // Act & Assert
        BookNotFoundException thrown = Assertions.assertThrows(
            BookNotFoundException.class,
            () -> library.notifyUserWithBookReviews("9780306406157", "123456789012")
        );
        Assertions.assertEquals("Book not found!", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());
    }

    @Test
    public void givenValidUserIdWithNullUser_whenNotifyUserWithBookReviews_thenThrowUserNotRegisteredException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockDatabase.getUserById(anyString())).thenReturn(null);
        // Act & Assert
        UserNotRegisteredException thrown = Assertions.assertThrows(
            UserNotRegisteredException.class,
            () -> library.notifyUserWithBookReviews("9780306406157", "123456789012")
        );
        Assertions.assertEquals("User not found!", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());
    }

    @Test
    public void givenValidUserIdAndValidIsbnButNoReviewsFoundCausedByEmptyList_whenNotifyUserWithBookReviews_thenThrowNoReviewsFoundException() {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockDatabase.getUserById(anyString())).thenReturn(mockUser);
        when(mockReviewService.getReviewsForBook(anyString())).thenReturn(new ArrayList<String>());
        // Act & Assert
        NoReviewsFoundException thrown = Assertions.assertThrows(
            NoReviewsFoundException.class,
            () -> library.notifyUserWithBookReviews("9780306406157", "123456789012")
        );
        Assertions.assertEquals("No reviews found!", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());  
    }

    @Test
    public void givenValidUserIdAndValidIsbnButNoReviewsFoundCausedByNull_whenNotifyUserWithBookReviews_thenThrowNoReviewsFoundException() {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockDatabase.getUserById(anyString())).thenReturn(mockUser);
        when(mockReviewService.getReviewsForBook(anyString())).thenReturn(null);
        // Act & Assert
        NoReviewsFoundException thrown = Assertions.assertThrows(
            NoReviewsFoundException.class,
            () -> library.notifyUserWithBookReviews("9780306406157", "123456789012")
        );
        Assertions.assertEquals("No reviews found!", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());
    }

    @Test
    public void givenValidUserIdAndValidIsbnButNoReviewsFoundCausedByReviewServiceUnavailableException_whenNotifyUserWithBookReviews_thenThrowReviewServiceUnavailableException() {
        // Arrange
        when(mockReviewService.getReviewsForBook(anyString())).thenThrow(new ReviewException("fail"));
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockDatabase.getUserById(anyString())).thenReturn(mockUser);
        // Act & Assert
        ReviewServiceUnavailableException thrown = Assertions.assertThrows(
            ReviewServiceUnavailableException.class, 
            () -> library.notifyUserWithBookReviews("9780306406157", "123456789012")
        );
        Assertions.assertEquals("Review service unavailable!", thrown.getMessage());  
        verify(mockReviewService, times(1)).getReviewsForBook(anyString());
        verify(mockReviewService, times(1)).close();
    }

    @Test
    public void givenValidUserIdAndValidIsbnButNoReviewsFoundCausedByNotificationException_whenNotifyUserWithBookReviews_thenThrowNotificationException() {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockDatabase.getUserById(anyString())).thenReturn(mockUser);
        when(mockReviewService.getReviewsForBook(anyString())).thenReturn(new ArrayList<String>());
        doThrow(new NotificationException("fail")).when(mockUser).sendNotification(anyString());
        // Act & Assert
        NotificationException thrown = Assertions.assertThrows(
            NotificationException.class,
            () -> library.notifyUserWithBookReviews("9780306406157", "123456789012")
        );
        Assertions.assertEquals("Notification failed!", thrown.getMessage());
        verify(mockReviewService, never()).getReviewsForBook(anyString());
        verify(mockReviewService, times(1)).close();
        verify(mockUser, times(1)).sendNotification(anyString());
    }

    @Test
    public void givenAllValidParameters_whenNotifyUserWithBookReviews_thenSendNotificationToUser() {
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        spyReviewsList.clear();
        spyReviewsList.add("Great Book");
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockDatabase.getUserById(anyString())).thenReturn(mockUser);
        when(mockReviewService.getReviewsForBook(anyString())).thenReturn(spyReviewsList);
        when(mockBook.getTitle()).thenReturn("title");
        doNothing().when(mockUser).sendNotification(anyString());
        // Act & Assert
        library.notifyUserWithBookReviews("9780306406157", "123456789012");
        verify(mockReviewService, times(1)).getReviewsForBook(anyString());
        verify(mockReviewService, times(1)).close();
        verify(mockUser, times(1)).sendNotification(anyString());
    }



    // getBookByISBN tests
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "123",                     // too short
        "97803064A06157",          // contains a letter
        "9780306406158",           // 13 digits, wrong check digit
        "978-0-306-40615-X"        // hyphen + invalid character
    })
    public void givenInvalidISBN_whenGetBookByISBN_thenThrowIllegalArgumentException(String invalidISBN){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.getBookByISBN(invalidISBN, "123456789012")
        );
        Assertions.assertEquals("Invalid ISBN.", thrown.getMessage());
        verify(mockDatabase, never()).getBookByISBN(anyString());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",                 // empty
        "12345678901A",     // 12 characters with letter
        "1234567890",       // 10 characters
        "1234567890123"     // 13 characters
    })
    public void givenInvalidUserId_whenGetBookByISBN_thenThrowIllegalArgumentException(String invalidUserId){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        // Act & Assert
        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> library.getBookByISBN("9780306406157", invalidUserId)
        );
        Assertions.assertEquals("Invalid user Id.", thrown.getMessage());
        verify(mockDatabase, never()).getBookByISBN(anyString());
    }

    @Test
    public void givenValidISBNAndUserIdButNoBookFound_whenGetBookByISBN_thenThrowBookNotFoundException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(null);
        // Act & Assert
        BookNotFoundException thrown = Assertions.assertThrows(
            BookNotFoundException.class,
            () -> library.getBookByISBN("9780306406157", "123456789012")
        );
        Assertions.assertEquals("Book not found!", thrown.getMessage());
        verify(mockDatabase, times(1)).getBookByISBN("9780306406157");
    }

    @Test
    public void givenValidISBNAndUserIdButBookIsBorrowed_whenGetBookByISBN_thenThrowBookAlreadyBorrowedException(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(true);
        // Act & Assert
        BookAlreadyBorrowedException thrown = Assertions.assertThrows(
            BookAlreadyBorrowedException.class,
            () -> library.getBookByISBN("9780306406157", "123456789012")
        );
        Assertions.assertEquals("Book is already borrowed!", thrown.getMessage());
        verify(mockDatabase, times(1)).getBookByISBN("9780306406157");
        verify(mockUser, never()).sendNotification(anyString());
    }

    @Test
    public void givenValidISBNAndUserIdButBookIsNotBorrowed_whenGetBookByISBN_thenReturnBook(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(false);
        // Act & Assert
        Book returnedBook = library.getBookByISBN("9780306406157", "123456789012");
        Assertions.assertEquals(mockBook, returnedBook);
    }

    @Test
    public void givenValidISBNAndUserIdButBookIsBorrowedAndNotificationFails_whenGetBookByISBN_thenReturnBook(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(true);
        doThrow(new NotificationException("fail")).when(mockUser).sendNotification(anyString());
        // Act & Assert
        Book returnedBook = library.getBookByISBN("9780306406157", "123456789012");
        Assertions.assertEquals(mockBook, returnedBook);
        verify(mockUser, times(1)).sendNotification(anyString());
    }

    @Test
    public void givenValidISBNAndUserIdButBookIsNotBorrowedAndNotificationFails_whenGetBookByISBN_thenReturnBook(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(false);
        doThrow(new NotificationException("fail")).when(mockUser).sendNotification(anyString());
        // Act & Assert
        Book returnedBook = library.getBookByISBN("9780306406157", "123456789012");
        Assertions.assertEquals(mockBook, returnedBook);
    }

    @Test
    public void givenValidISBNAndUserIdButBookIsNotBorrowedAndNotificationSucceeds_whenGetBookByISBN_thenReturnBook(){
        // Arrange
        Library library = new Library(mockDatabase, mockReviewService);
        when(mockDatabase.getBookByISBN(anyString())).thenReturn(mockBook);
        when(mockBook.isBorrowed()).thenReturn(false);
        doNothing().when(mockUser).sendNotification(anyString());
        // Act & Assert
        Book returnedBook = library.getBookByISBN("9780306406157", "123456789012");
        Assertions.assertEquals(mockBook, returnedBook);
    }
}
