# BC References: BC-013..025, BC-014, BC-015, BC-056
Feature: Order History Management
  As a customer service representative
  I want to add and manage customer order history entries
  So that I can track all customer order interactions accurately

  Background:
    Given customer 10 with number "CUST-001" exists in user-service

  # BC-014: maintainScreen=false → return to customer summary after save
  Scenario: Add order history — navigate to customer summary when maintainScreen is false
    Given an order history request for order "ORD-001" with maintainScreen false
    When the order history entry is submitted
    Then the response status is 201
    And the navigation hint is "CUSTOMER_SUMMARY"

  # BC-015: maintainScreen=true → return to maintain list after save
  Scenario: Add order history — navigate to maintain list when maintainScreen is true
    Given an order history request for order "ORD-002" with maintainScreen true
    When the order history entry is submitted
    Then the response status is 201
    And the navigation hint is "ORDER_HISTORY_LIST"

  # BC-056: webService=true → no navigation redirect at all
  Scenario: Add order history in web service mode — no navigation hint
    Given an order history request for order "ORD-003" in web service mode
    When the order history entry is submitted
    Then the response status is 201
    And the navigation hint is "NONE"

  # BC-013/020: list order history, external orders excluded by default
  Scenario: List order history excludes external orders by default
    When the client lists order history for customer 10
    Then the response status is 200
    And external orders are excluded
