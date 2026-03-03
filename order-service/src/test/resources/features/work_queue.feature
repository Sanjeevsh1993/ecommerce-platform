# BC References: BC-016, BC-017, BC-026, BC-027, BC-028
Feature: Work Queue Order History
  As a customer service representative
  I want to add order history entries with work queue flags
  So that issues requiring follow-up are tracked separately from standard order entries

  Background:
    Given customer 10 with number "CUST-001" exists in user-service

  # BC-016/017: work queue add uses a DISTINCT endpoint (/work-queue) from standard add
  Scenario: Work queue add with valid type and non-sequential reason ID 101
    Given a work queue request with typeId 1 and reasonId 101 for order "WQ-ORD-001"
    When the work queue entry is submitted
    Then the response status is 201

  # BC-028: reason ID 201 is valid (non-sequential, not 2)
  Scenario: Work queue add with non-sequential reason ID 201
    Given a work queue request with typeId 2 and reasonId 201 for order "WQ-ORD-002"
    When the work queue entry is submitted
    Then the response status is 201

  # BC-016: work queue type is mandatory for WQ endpoint
  Scenario: Work queue add without type ID is rejected
    Given a work queue request with no type id for order "WQ-ORD-003"
    When the work queue entry is submitted
    Then the response status is 422
    And the error code is "MISSING_WORK_QUEUE_TYPE"

  # BC-027: reference data endpoint returns all 5 work queue types
  Scenario: Reference data returns 5 work queue types
    When the client requests work queue types reference data
    Then the response status is 200
    And 5 work queue types are returned

  # BC-028: CRITICAL — verify non-sequential reason IDs in reference data
  Scenario: Work queue reasons have non-sequential IDs starting at 101
    When the client requests work queue reasons reference data
    Then the response status is 200
    And reason id 101 has name "Missing Payment Info"
    And reason id 201 has name "Order Verification Required"
    And reason id 100 does not exist
