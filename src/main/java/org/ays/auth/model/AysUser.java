package org.ays.auth.model;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.ays.auth.model.enums.AysTokenClaims;
import org.ays.auth.model.enums.UserStatus;
import org.ays.common.model.AysPhoneNumber;
import org.ays.common.model.BaseDomainModel;
import org.ays.institution.model.Institution;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain model representing a user entity for data transfer between the service layer and controller.
 * This class extends {@link BaseDomainModel} to inherit common properties and behavior.
 * It encapsulates user-specific information such as identification, contact details, status,
 * authentication credentials, roles, and associated institution.
 */
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AysUser extends BaseDomainModel {

    private String id;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private AysPhoneNumber phoneNumber;
    private String city;
    private UserStatus status;

    private Password password;
    private LoginAttempt loginAttempt;

    private Set<AysRole> roles;
    private Institution institution;

    /**
     * Checks if the user's status is active.
     *
     * @return {@code true} if the user's status is {@link UserStatus#ACTIVE}, otherwise {@code false}.
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * Checks if the user's status is passive.
     *
     * @return {@code true} if the user's status is {@link UserStatus#PASSIVE}, otherwise {@code false}.
     */
    public boolean isPassive() {
        return UserStatus.PASSIVE.equals(this.status);
    }


    /**
     * Checks if the user's status is deleted.
     *
     * @return {@code true} if the user's status is {@link UserStatus#DELETED}, otherwise {@code false}.
     */
    public boolean isDeleted() {
        return UserStatus.DELETED.equals(this.status);
    }


    /**
     * Sets the user's status to {@link UserStatus#ACTIVE}, marking the user as active.
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Sets the user's status to {@link UserStatus#PASSIVE}, marking the user as passive.
     */
    public void passivate() {
        this.status = UserStatus.PASSIVE;
    }

    /**
     * Sets the user's status to {@link UserStatus#DELETED}, marking the user as deleted.
     */
    public void delete() {
        this.status = UserStatus.DELETED;
    }

    /**
     * Sets the user's status to {@link UserStatus#NOT_VERIFIED}, indicating the user's verification status.
     * This method typically sets the status to indicate the user's account has not yet been verified.
     */
    public void notVerify() {
        this.status = UserStatus.NOT_VERIFIED;
    }


    /**
     * Domain model representing user password information.
     * This class encapsulates the unique identifier and encrypted value of a user's password.
     */
    @Getter
    @Setter
    @SuperBuilder
    public static class Password extends BaseDomainModel {

        private Long id;
        private String value;

    }


    /**
     * Domain model representing user login attempt information.
     * This class encapsulates the unique identifier and timestamp of a user's last login attempt.
     * It provides a method to update the last login timestamp to the current time.
     */
    @Getter
    @Setter
    @SuperBuilder
    public static class LoginAttempt extends BaseDomainModel {

        private String id;
        private LocalDateTime lastLoginAt;

        public void success() {
            this.lastLoginAt = LocalDateTime.now();
        }

    }

    /**
     * Generates JWT claims based on the user's information for authentication and authorization.
     * The claims include user-specific data such as institution ID, institution name, user ID, first name,
     * last name, email address, and permissions associated with the user.
     * If available, it also includes the timestamp of the user's last successful login attempt.
     *
     * @return JWT claims containing user-related information.
     */
    public Claims getClaims() {
        final ClaimsBuilder claimsBuilder = Jwts.claims();

        claimsBuilder.add(AysTokenClaims.INSTITUTION_ID.getValue(), this.institution.getId());
        claimsBuilder.add(AysTokenClaims.INSTITUTION_NAME.getValue(), this.institution.getName());
        claimsBuilder.add(AysTokenClaims.USER_ID.getValue(), this.id);
        claimsBuilder.add(AysTokenClaims.USER_FIRST_NAME.getValue(), this.firstName);
        claimsBuilder.add(AysTokenClaims.USER_LAST_NAME.getValue(), this.lastName);
        claimsBuilder.add(AysTokenClaims.USER_EMAIL_ADDRESS.getValue(), this.emailAddress);
        claimsBuilder.add(AysTokenClaims.USER_PERMISSIONS.getValue(), this.getPermissionNames());


        if (this.loginAttempt != null && this.loginAttempt.lastLoginAt != null) {
            claimsBuilder.add(AysTokenClaims.USER_LAST_LOGIN_AT.getValue(), this.loginAttempt.lastLoginAt.toString());
        }

        return claimsBuilder.build();
    }

    private Set<String> getPermissionNames() {
        return this.roles.stream()
                .map(AysRole::getPermissions)
                .flatMap(Set::stream)
                .map(AysPermission::getName)
                .collect(Collectors.toSet());
    }

}
