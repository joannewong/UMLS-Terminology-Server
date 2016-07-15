/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;

/**
 * The Interface ContentServiceRest.
 */
public interface MetaEditingServiceRest {

  /**
   * Add semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp representing concept's state
   * @param semanticTypeComponent the semantic type component
   * @param overrideWarnings whether to override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addSemanticType(Long projectId, Long conceptId,
    Long timestamp, SemanticTypeComponentJpa semanticTypeComponent,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Remove semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp representing concept's state
   * @param semanticTypeComponentId the semantic type component id
   * @param overrideWarnings whether to override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeSemanticType(Long projectId, Long conceptId,
    Long timestamp, Long semanticTypeComponentId, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Add attribute.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param attribute the attribute
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addAttribute(Long projectId, Long conceptId,
    Long timestamp, AttributeJpa attribute, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Remove attribute.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param attributeId the attribute id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeAttribute(Long projectId, Long conceptId,
    Long timestamp, Long attributeId, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Adds the atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param atom the atom
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addAtom(Long projectId, Long conceptId,
    Long timestamp, AtomJpa atom, boolean overrideWarnings, String authToken)
    throws Exception;

  /**
   * Removes the atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param atomId the atom id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeAtom(Long projectId, Long conceptId,
    Long timestamp, Long atomId, boolean overrideWarnings, String authToken)
    throws Exception;

  /**
   * Adds the relationship.
   *
   * @param projectId the project id
   * @param conceptId the from concept id
   * @param timestamp the timestamp
   * @param relationship the relationship
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addRelationship(Long projectId, Long conceptId,
    Long timestamp, ConceptRelationshipJpa relationship,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Removes the relationship.
   *
   * @param projectId the project id
   * @param conceptId the from concept id
   * @param timestamp the timestamp
   * @param relationshipId the relationship id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeRelationship(Long projectId, Long conceptId,
    Long timestamp, Long relationshipId, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Merge concepts.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param conceptId2 the concept id 2
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult mergeConcepts(Long projectId, Long conceptId,
    Long timestamp, Long conceptId2, boolean overrideWarnings, String authToken)
    throws Exception;
}
