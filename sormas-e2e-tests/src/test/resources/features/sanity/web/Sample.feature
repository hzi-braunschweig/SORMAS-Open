@Sanity @Sample
Feature: Sample Functionalities

  Scenario: Edit a new Sample
    Given I log in with the user
    And I click on the Cases button from navbar
    And I click on the NEW CASE button
    When I create a new case with specific data
    And I collect the case person UUID displayed on Edit case page
    And I click on New Sample
    When I collect the sample UUID displayed on create new sample page
    And I create a new Sample with specific data
    And I create a new Test result with specific data
    And I click on the Sample button from navbar
    And I search for Sample using Sample UUID from the created Sample
    When I open created Sample
    Then I check the created Sample is correctly displayed on Edit Sample page
    When I change all Sample fields
    Then I check the created Test result is correctly displayed on Edit Sample page, Test creation tab
    When I change all Test result fields and save
    And I click on the Cases button from navbar
    And I confirm navigation
    And Search for Case using Case UUID from the created Task
    When I open last created case
    When I click on edit Sample
    Then I check the created Sample is correctly displayed on Edit Sample page

    @jenkins_run
  Scenario: Delete created sample
    Given I log in with the user
    Given API: I create a new case
    Given API: I create a new sample
    When I click on the Sample button from navbar
    Then I open the last created sample via API
    Then I delete the sample
    Then I search after the last created Sample via API
    And I check that number of displayed sample results is 0