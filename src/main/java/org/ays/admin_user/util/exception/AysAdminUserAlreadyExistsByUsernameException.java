package org.ays.admin_user.util.exception;

import org.ays.common.util.exception.AysAlreadyException;

import java.io.Serial;

/**
 * Exception indicating that an admin user already exists with the specified username.
 * This exception is a subclass of AysAlreadyException.
 */
@Deprecated(since = "AysAdminUserAlreadyExistsByUsernameException V2 Production'a alınınca burası silinecektir.", forRemoval = true)
public class AysAdminUserAlreadyExistsByUsernameException extends AysAlreadyException {

    /**
     * Unique identifier for serialization.
     */
    @Serial
    private static final long serialVersionUID = -3186201226632256358L;

    /**
     * Constructs a new AysAdminUserAlreadyExistsByUsernameException with the specified username.
     *
     * @param username The username of the admin user that already exists.
     */
    public AysAdminUserAlreadyExistsByUsernameException(String username) {
        super("ADMIN USER ALREADY EXIST! username:" + username);
    }

}
