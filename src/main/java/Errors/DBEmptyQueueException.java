package Errors;

public class DBEmptyQueueException extends Exception{
    public DBEmptyQueueException(String errorMessage) {
        super(errorMessage);
    }
}
