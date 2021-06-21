@Sanity @Contacts
Feature: Contacts end to end tests
  
  Scenario: Create simple contact
    Given I log in with the user
      And I click on the Contacts button from navbar
      And I click on the NEW CONTACT button
      And I create a new contact
      Then I check the created data is correctly displayed on Edit Contact page
      Then I open Contact Person tab
      And I check the created data is correctly displayed on Edit Contact Person page

    Scenario: Delete created contact
      Given I log in with the user
      When API: I create a new person
      Then API: I create a new contact
      When I click on the Contacts button from navbar
      Then I open the last created contact
      Then I delete the contact
      And I check that number of displayed contact results is 0

  @EditContact @issue=5634
  Scenario: Edit a created contact
    Given I log in with the user
    When API: I create a new person
    And API: I create a new contact
    And I navigate to the last created contact via the url
    And I change all contact fields and save
    And I navigate to the last created contact via the url
    Then I check the edited data is correctly displayed on Edit Contact page after editing

  Scenario: Source case selected for contact
    Given I log in with the user
    Given API: I create a new person
    Given API: I create a new case
    When API: I create a new person
    And API: I create a new contact
    And I navigate to the last created contact via the url
    And I click on the CHOOSE SOURCE CASE button
    And I click yes on the DISCARD UNSAVED CHANGES popup
    And I search for the last case uuid in the CHOOSE SOURCE window
    And I open the first found result in the CHOOSE SOURCE window
    Then I check the linked case information is correctly displayed
    When I open the Case Contacts tab of the created case via api
    Then I check the linked contact information is correctly displayed