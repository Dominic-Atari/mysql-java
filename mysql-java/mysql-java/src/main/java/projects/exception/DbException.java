package projects.exception;

/**
 * This exception class is used throughout the DIY projects application. It is an unchecked
 * exception.
 * 
 * Note the use of @SupressWarnings("serial"). This is used because the Java compiler generates a
 * warning if the instance variable serialVersionUID is missing. The variable serialVersionUID is
 * used by the Serialization process to specify a version number that is used in Serialization.
 * Serialization is the process of sending a class and its data to a remote process, likely on
 * another server. The warning is generated because the parent class, RuntimeException, declares the
 * serialVersionUID variable. Since we are not interested in Serialization, this can be safely
 * ignored. By adding @SuppressWarnings("serial"), the warning is turned off.
 * 
 * @author Promineo
 *
 */
@SuppressWarnings("serial")
public class DbException extends RuntimeException {

  /**
   * Create an exception with a message. The message is stored in the parent class.
   * 
   * @param message The message.
   */
  public DbException(String message) {
    super(message);
  }

  /**
   * Create an exception with a cause. The cause is stored in the parent class.
   * 
   * @param cause The cause.
   */
  public DbException(Throwable cause) {
    super(cause);
  }

  /**
   * Create an exception with a message and a cause.
   * 
   * @param message The message.
   * @param cause The cause.
   */
  public DbException(String message, Throwable cause) {
    super(message, cause);
  }
}
