# BC References: BC-034..042
Feature: Authentication
  As a user of the e-commerce platform
  I want to register, login, and manage my session
  So that I can securely access protected resources

  # BC-034: self-registration creates ROLE_CUSTOMER account
  Scenario: Successful registration with valid details
    Given a registration request with email "newuser@example.com" and password "SecurePass1!"
    When the user submits the registration
    Then the response status is 201
    And the response contains an access token
    And the response contains role "ROLE_CUSTOMER"

  # BC-035: duplicate email rejected
  Scenario: Registration fails with duplicate email
    Given a user already exists with email "dup@example.com"
    And a registration request with email "dup@example.com" and password "SecurePass1!"
    When the user submits the registration
    Then the response status is 409
    And the error code is "DUPLICATE_RESOURCE"

  # BC-034: login with valid credentials
  Scenario: Successful login returns access token
    Given a registered user with email "login@example.com" password "password123" and role "ROLE_CUSTOMER"
    When the user logs in with email "login@example.com" and password "password123"
    Then the response status is 200
    And the response contains an access token

  # BC-037: forgot password silently succeeds for unknown email (no enumeration)
  Scenario: Forgot password with unknown email returns 200
    When the user requests a password reset for "unknown@nowhere.com"
    Then the response status is 200

  # BC-037: reset password with expired token
  Scenario: Password reset with expired token returns error
    Given a password reset token "expired-token-abc" that is expired
    When the user resets password with token "expired-token-abc" and new password "NewPass1!"
    Then the response status is 422
    And the error code is "EXPIRED_RESET_TOKEN"
