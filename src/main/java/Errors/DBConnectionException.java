package Errors;

public class DBConnectionException extends Exception {
    public DBConnectionException(String errorMessage) {
        super(errorMessage);
    }
}
