package coda.scanner.java;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ScanningException extends RuntimeException {
    public ScanningException(String message) {
        super(message);
    }

    public ScanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
