/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.NoResultException;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractMergeAlgorithm;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.services.ProcessService;

/**
 * Implementation of an algorithm to save information before an insertion.
 */
public class PreInsertionAlgorithm extends AbstractMergeAlgorithm {

  /**
   * Instantiates an empty {@link PreInsertionAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PreInsertionAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("PREINSERTION");
    setLastModifiedBy("admin");
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Pre Insertion requires a project to be set");
    }

    // Go through all the files needed by insertion and check for presence
    // Check the input directories
    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    checkFileExist(srcFullPath, "attributes.src");
    checkFileExist(srcFullPath, "classes_atoms.src");
    checkFileExist(srcFullPath, "contexts.src");
    checkFileExist(srcFullPath, "mergefacts.src");
    checkFileExist(srcFullPath, "MRDOC.RRF");
    checkFileExist(srcFullPath, "relationships.src  ");
    checkFileExist(srcFullPath, "sources.src");
    checkFileExist(srcFullPath, "termgroups.src");

    return validationResult;
  }

  /**
   * Check file exist.
   *
   * @param srcFullPath the src full path
   * @param fileName the file name
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void checkFileExist(String srcFullPath, String fileName)
    throws Exception {

    File sourceFile = new File(srcFullPath + File.separator + fileName);
    if (!sourceFile.exists()) {
      throw new Exception(fileName
          + " file doesn't exist at specified input directory: " + srcFullPath);
    }

  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting PREINSERTION");

    // No Molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Populate the executionInfo map of the process' execution.
    ProcessExecution processExecution = getProcess();

    // Get the max atom Id prior to the insertion starting (used to identify
    // which atoms are new)
    Long atomId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AtomJpa a ");
      final Long atomId2 = (Long) query.getSingleResult();
      atomId = atomId2 != null ? atomId2 : atomId;
    } catch (NoResultException e) {
      atomId = 0L;
    }
    processExecution.getExecutionInfo().put("maxAtomIdPreInsertion",
        atomId.toString());
    logInfo(" maxAtomIdPreInsertion = "
        + processExecution.getExecutionInfo().get("maxAtomIdPreInsertion"));

    // Get the max Semantic Type Component Id prior to the insertion starting
    Long styId = null;
    try {
      final javax.persistence.Query query = manager
          .createQuery("select max(a.id) from SemanticTypeComponentJpa a ");
      final Long styId2 = (Long) query.getSingleResult();
      styId = styId2 != null ? styId2 : styId;
    } catch (NoResultException e) {
      styId = 0L;
    }
    processExecution.getExecutionInfo().put("maxStyIdPreInsertion",
        styId.toString());
    logInfo(" maxStyIdPreInsertion = "
        + processExecution.getExecutionInfo().get("maxStyIdPreInsertion"));

    ProcessService processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
    processService.updateProcessExecution(processExecution);

    logInfo(" project = " + getProject().getId());
    logInfo(" workId = " + getWorkId());
    logInfo(" activityId = " + getActivityId());
    logInfo(" user = " + getLastModifiedBy());
    logInfo("Finished PREINSERTION");
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  @Override
  public String getDescription() {
    return "Prepares an insertion to operate and validates starting conditions.";
  }
}