@Sanity @Person @API
Feature: Check Person RestApi endpoints

  Scenario: Create and check successfully created Case
    Given API: I create a new person
     When API: I create a new case
     Then API: I check if the response contains OK
      And API: I check if the response has status 200
  
  Scenario: Create and check a case which is using an already created CASE UUID
    Given API: I create a new person
     When API: I create a new case
     When API: I create a new case with already created Case uuid
     Then API: I check if the response contains ERROR
      And API: I check if the response has status 200
  
  Scenario: Create and check a case which is using an already created person
    Given API: I create a new person
     When API: I create a new case
     When API: I create a new case with an already created case
     Then API: I check if the response contains TOO_OLD
      And API: I check if the response has status 200
  
  Scenario: Create and check a case which is has invalid disease
    Given API: I create a new person
     When API: I create a new case with disease fun
      And API: I check if the response has status 400
     Then API: I check if the response contains Cannot deserialize value of type
      And API: I check if the response contains not one of the values accepted for Enum
  
  Scenario: Create and check successfully created Case by querying
    Given API: I create a new person
     When API: I create a new case
      And API: I query the last created case
      And API: I check if the response has status 200
     Then API: I check if the response can be converted to a case object
