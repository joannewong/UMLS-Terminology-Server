/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;

/**
 * Represents a service for performing processes and querying their execution.
 */
public interface ProcessServiceRest {

  /**
   * Adds the process config.
   *
   * @param projectId the project id
   * @param processConfig the process config
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig addProcessConfig(Long projectId,
    ProcessConfigJpa processConfig, String authToken) throws Exception;

  /**
   * Update process config.
   *
   * @param projectId the project id
   * @param processConfig the process config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateProcessConfig(Long projectId,
    ProcessConfigJpa processConfig, String authToken) throws Exception;

  /**
   * Removes the process config.
   *
   * @param projectId the project id
   * @param id the id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeProcessConfig(Long projectId, Long id, Boolean cascade,
    String authToken) throws Exception;

  /**
   * Returns the process config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig getProcessConfig(Long projectId, Long id,
    String authToken) throws Exception;

  /**
   * Find process config.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfigList findProcessConfigs(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Adds the algorithm config.
   *
   * @param projectId the project id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig addAlgorithmConfig(Long projectId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception;

  /**
   * Update algorithm config.
   *
   * @param projectId the project id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAlgorithmConfig(Long projectId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception;

  /**
   * Removes the algorithm config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAlgorithmConfig(Long projectId, Long id, String authToken)
    throws Exception;

  /**
   * Returns the algorithm config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig getAlgorithmConfig(Long projectId, Long id,
    String authToken) throws Exception;

  /**
   * Returns the insertion algorithms.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the insertion algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getInsertionAlgorithms(Long projectId,
    String authToken) throws Exception;

  /**
   * Returns the maintenance algorithms.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the maintenance algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getMaintenanceAlgorithms(Long projectId,
    String authToken) throws Exception;

  /**
   * Returns the release algorithms.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the release algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getReleaseAlgorithms(Long projectId, String authToken)
    throws Exception;

  // /**
  // * Adds the algorithm parameter.
  // *
  // * @param projectId the project id
  // * @param algorithmParameter the algorithm parameter
  // * @param authToken the auth token
  // * @return the algorithm parameter
  // * @throws Exception the exception
  // */
  // public AlgorithmParameter addAlgorithmParameter(Long projectId,
  // AlgorithmParameterJpa algorithmParameter, String authToken) throws
  // Exception;
  //
  // /**
  // * Update algorithm parameter.
  // *
  // * @param projectId the project id
  // * @param algorithmParameter the algorithm parameter
  // * @param authToken the auth token
  // * @throws Exception the exception
  // */
  // public void updateAlgorithmParameter(Long projectId,
  // AlgorithmParameterJpa algorithmParameter, String authToken) throws
  // Exception;
  //
  // /**
  // * Removes the algorithm parameter.
  // *
  // * @param projectId the project id
  // * @param id the id
  // * @param authToken the auth token
  // * @throws Exception the exception
  // */
  // public void removeAlgorithmParameter(Long projectId, Long id, String
  // authToken)
  // throws Exception;
  //
  // /**
  // * Returns the algorithm parameter.
  // *
  // * @param projectId the project id
  // * @param id the id
  // * @param authToken the auth token
  // * @return the algorithm parameter
  // * @throws Exception the exception
  // */
  // public AlgorithmParameter getAlgorithmParameter(Long projectId, Long id,
  // String authToken) throws Exception;

  /**
   * Execute process.
   *
   * @param projectId the project id
   * @param processConfigId the process config id
   * @param background the background
   * @param authToken the auth token
   * @return the long process execution id
   * @throws Exception the exception
   */
  public Long executeProcess(Long projectId, Long processConfigId,
    Boolean background, String authToken) throws Exception;

}
