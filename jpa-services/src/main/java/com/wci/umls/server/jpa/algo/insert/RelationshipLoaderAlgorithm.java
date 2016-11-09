/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractSourceLoaderAlgorithm;
import com.wci.umls.server.jpa.content.AbstractRelationship;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of an algorithm to import relationships.
 */
public class RelationshipLoaderAlgorithm extends AbstractSourceLoaderAlgorithm {

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /** The handler. */
  private IdentifierAssignmentHandler handler = null;

  /** The add count. */
  private int addCount = 0;

  /** The update count. */
  private int updateCount = 0;

  /**
   * Instantiates an empty {@link RelationshipLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RelationshipLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RELATIONSHIPLOADER");
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
      throw new Exception("Relationship Loading requires a project to be set");
    }

    // Check the input directories

    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    srcDirFile = new File(srcFullPath);
    if (!srcDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting RELATIONSHIPLOADING");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Set up the handler for identifier assignment
    handler = newIdentifierAssignmentHandler(getProject().getTerminology());
    handler.setTransactionPerOperation(false);
    handler.beginTransaction();

    // Count number of added and updated Relationships, for logging
    addCount = 0;
    updateCount = 0;

    try {

      previousProgress = 0;
      stepsCompleted = 0;

      logInfo("[RelationshipLoader] Checking for new/updated Relationships");

      //
      // Load the relationships.src file
      //
      List<String> lines =
          loadFileIntoStringList(srcDirFile, "relationships.src", null, null);

      //
      // Load the contexts.src file
      //
      // Only keep "PAR" relationship rows.
      // If sg_type_1 or sg_type_2 = SCR_ATOM_ID, skip.
      List<String> lines2 = loadFileIntoStringList(srcDirFile, "contexts.src",
          "[0-9]+?\\|PAR(.*)", "(.*)SRC_ATOM_ID(.*)");

      // There will be many duplicated lines in the contexts file, since the main
      // distinguishing field "parent_treenum" is ignored for these purposes.
      // Remove the dups.
      lines = removeDups(lines);

      // Set the number of steps to the number of relationships to be processed
      steps = lines.size() + lines2.size();


      // 
      // Process relationships.src lines
      //
      String fields[] = new String[18];
      for (String line : lines) {

        // Check for a cancelled call once every 100 relationships (doing it
        // every time
        // makes things too slow)
        if (stepsCompleted % 100 == 0 && isCancelled()) {
          throw new CancelException("Cancelled");
        }

        FieldedStringTokenizer.split(line, "|", 18, fields);

        // Fields:
        // 0 src_relationship_id (Not used)
        // 1 level
        // 2 id_1
        // 3 relationship_name
        // 4 relationship_attribute
        // 5 id_2
        // 6 source
        // 7 source_of_label
        // 8 status
        // 9 tobereleased
        // 10 released
        // 11 suppressible
        // 12 id_type_1
        // 13 id_qualifier_1
        // 14 id_type_2
        // 15 id_qualifier_2
        // 16 source_rui
        // 17 relationship_group

        // e.g.
        // 40|S|C17260|RT|Gene_Plays_Role_In_Process|C29949|NCI_2016_05E|
        // NCI_2016_05E|R|Y|N|N|SOURCE_CUI|NCI_2016_05E|SOURCE_CUI|NCI_2016_05E|||

        //
        // Relationship based on input line.
        //

        final String fromTermId = fields[5];
        final String fromTermAndVersion = fields[15];
        final String fromClassIdType = fields[14];
        final String toTermId = fields[2];
        final String toTermAndVersion = fields[13];
        final String toClassIdType = fields[12];
        final String additionalRelType = fields[4];
        final String group = fields[17];
        final String publishable = fields[9];
        final String published = fields[10];
        final String relType = fields[3];
        final String suppresible = fields[11];
        final String sourceTermAndVersion = fields[6];
        final String sourceTermId = fields[16];
        final String workflowStatusStr = fields[8];

        handleRelationships(line, fromTermId, fromTermAndVersion,
            fromClassIdType, toTermId, toTermAndVersion, toClassIdType,
            additionalRelType, group, publishable, published, relType,
            suppresible, sourceTermAndVersion, sourceTermId, workflowStatusStr);

      }


      // 
      // Process contexts.src lines
      //
      String fields2[] = new String[17];
      for (final String line : lines2) {

        // Check for a cancelled call once every 100 relationships (doing it
        // every time
        // makes things too slow)
        if (stepsCompleted % 100 == 0 && isCancelled()) {
          throw new CancelException("Cancelled");
        }

        FieldedStringTokenizer.split(line, "|", 17, fields2);

        // Fields:
        // 0 source_atom_id_1
        // 1 relationship_name
        // 2 relationship_attribute
        // 3 source_atom_id_2
        // 4 source
        // 5 source_of_label
        // 6 hcd
        // 7 parent_treenum
        // 8 release mode
        // 9 source_rui
        // 10 relationship_group
        // 11 sg_id_1
        // 12 sg_type_1
        // 13 sg_qualifier_1
        // 14 sg_id_2
        // 15 sg_type_2
        // 16 sg_qualifier_2

        // e.g.
        // 362241646|PAR|isa|362239326|NCI_2016_05E|NCI_2016_05E||
        // 31926003.362204588.362250568.362172407.362239326|00|||C90893|
        // SOURCE_CUI|NCI_2016_05E|C29696|SOURCE_CUI|NCI_2016_05E|

        //
        // Relationship based on input line.
        //

        final String fromTermId = fields2[11];
        final String fromTermAndVersion = fields2[13];
        final String fromClassIdType = fields2[12];
        final String toTermId = fields2[14];
        final String toTermAndVersion = fields2[16];
        final String toClassIdType = fields2[15];
        final String additionalRelType = fields2[2];
        final String group = fields2[10];
        final String publishable = "Y";
        final String published = "N";
        // Note: relType and additionalRelType are swapped in file. We're only
        // keeping "PAR" rows, so we hard-code relType as "CHD"
        final String relType = "CHD";
        final String suppresible = "N";
        final String sourceTermAndVersion = fields2[4];
        final String sourceTermId = fields2[9];
        final String workflowStatusStr = "R";

        handleRelationships(line, fromTermId, fromTermAndVersion,
            fromClassIdType, toTermId, toTermAndVersion, toClassIdType,
            additionalRelType, group, publishable, published, relType,
            suppresible, sourceTermAndVersion, sourceTermId, workflowStatusStr);

      }

      commitClearBegin();
      handler.commitClearBegin();
      
      logInfo("[RelationshipLoader] Added " + addCount + " new Relationships.");
      logInfo("[RelationshipLoader] Updated " + updateCount
          + " existing Relationships.");

      //
      // Load the relationships from relationships.src
      //

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished RELATIONSHIPLOADING");

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Removes the dups.
   *
   * @param lineList the line list
   * @return the list
   */
  private List<String> removeDups(List<String> lineList) {
    // Make a set of the rela, ID1, and ID2, so you don't create duplicate
    // relationships.
    Set<String> seenLines = new HashSet<>();
    List<String> lines = new ArrayList<>();

    String fields[] = new String[17];

    for (String line : lineList) {

      FieldedStringTokenizer.split(line, "|", 17, fields);
      String concatedFields = fields[2] + fields[14] + fields[14];

      if (!seenLines.contains(concatedFields)) {
        lines.add(line);
        seenLines.add(concatedFields);
      }
    }

    return lines;
  }

  /**
   * Handle relationships.
   *
   * @param line the line
   * @param fromTermId the from term id
   * @param fromTermAndVersion the from term and version
   * @param fromClassIdType the from class id type
   * @param toTermId the to term id
   * @param toTermAndVersion the to term and version
   * @param toClassIdType the to class id type
   * @param additionalRelType the additional rel type
   * @param group the group
   * @param publishable the publishable
   * @param published the published
   * @param relType the rel type
   * @param suppresible the suppresible
   * @param sourceTermAndVersion the source term and version
   * @param sourceTermId the source term id
   * @param workflowStatusStr the workflow status str
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  private void handleRelationships(String line, String fromTermId,
    String fromTermAndVersion, String fromClassIdType, String toTermId,
    String toTermAndVersion, String toClassIdType, String additionalRelType,
    String group, String publishable, String published, String relType,
    String suppresible, String sourceTermAndVersion, String sourceTermId,
    String workflowStatusStr) throws Exception {

    // Load the containing objects based on type
    final String fromTerminologyId = fromTermId;
    final String fromTerminology = fromTermAndVersion.contains("_")
        ? fromTermAndVersion.substring(0, fromTermAndVersion.indexOf('_'))
        : fromTermAndVersion;
    final Class<? extends Component> fromClass = lookupClass(fromClassIdType);

    final Long fromComponentId =
        getId(fromClass, fromTerminologyId, fromTerminology);

    final Component fromComponent = fromComponentId == null ? null
        : getComponent(fromComponentId, fromClass);

    if (fromComponent == null) {
      logWarn(
          "Warning - could not find from Component for the following line:\n\t"
              + line);
      updateProgress();
      logAndCommit("[Relationship Loader] Relationships processed ",
          stepsCompleted, RootService.logCt, RootService.commitCt);
      return;
    }

    final String toTerminologyId = toTermId;
    final String toTerminology = toTermAndVersion.contains("_")
        ? toTermAndVersion.substring(0, toTermAndVersion.indexOf('_'))
        : toTermAndVersion;
    final Class<? extends Component> toClass = lookupClass(toClassIdType);

    final Long toComponentId = getId(toClass, toTerminologyId, toTerminology);

    final Component toComponent =
        toComponentId == null ? null : getComponent(toComponentId, toClass);

    if (toComponent == null) {
      logWarn(
          "Warning - could not find to Component for the following line:\n\t"
              + line);
      updateProgress();
      logAndCommit("[Relationship Loader] Relationships processed ",
          stepsCompleted, RootService.logCt, RootService.commitCt);
      return;
    }

    // Create the relationship.
    // If id_type_1 equals id_type_2, the relationship is of that type.
    // If they are not equal, it's a Component Info Relationship
    AbstractRelationship newRelationship = null;
    Class relClass = null;

    if (!fromClass.equals(toClass)) {
      relClass = ComponentInfoRelationshipJpa.class;
      newRelationship = new ComponentInfoRelationshipJpa();
    } else if (fromClass.equals(ConceptJpa.class)
        && toClass.equals(ConceptJpa.class)) {
      relClass = ConceptRelationshipJpa.class;
      newRelationship = new ConceptRelationshipJpa();
    } else if (fromClass.equals(CodeJpa.class)
        && toClass.equals(CodeJpa.class)) {
      relClass = CodeRelationshipJpa.class;
      newRelationship = new CodeRelationshipJpa();
    } else {
      throw new Exception("Error - unhandled class type: " + fromClass);
    }

    newRelationship.setAdditionalRelationshipType(additionalRelType);
    newRelationship.setBranch(Branch.ROOT);
    newRelationship.setFrom(fromComponent);
    newRelationship.setGroup(group);
    newRelationship.setInferred(true);
    newRelationship.setObsolete(false);
    newRelationship.setPublishable(publishable.equals("Y"));
    newRelationship.setPublished(published.equals("Y"));
    newRelationship.setRelationshipType(lookupRelationshipType(relType));
    newRelationship.setHierarchical(false);
    newRelationship.setStated(true);
    newRelationship.setSuppressible(suppresible.equals("Y"));
    Terminology term = getCachedTerminology(sourceTermAndVersion);
    if (term == null) {
      throw new Exception("ERROR: lookup for " + sourceTermAndVersion
          + " returned no terminology");
    } else {
      newRelationship.setAssertedDirection(term.isAssertsRelDirection());
      newRelationship.setTerminology(term.getTerminology());
      newRelationship.setVersion(term.getVersion());
    }
    newRelationship.setTerminologyId(sourceTermId);
    newRelationship.setTo(toComponent);
    newRelationship.setWorkflowStatus(lookupWorkflowStatus(workflowStatusStr));

    // Calculate inverseRel and inverseAdditionalRel types, to use in the
    // RUI handler and the inverse relationship creation
    String inverseRelType =
        getRelationshipType(newRelationship.getRelationshipType(),
            getProject().getTerminology(), getProject().getVersion())
                .getInverse().getAbbreviation();

    String inverseAdditionalRelType = "";
    if (!newRelationship.getAdditionalRelationshipType().equals("")) {
      inverseAdditionalRelType = getAdditionalRelationshipType(
          newRelationship.getAdditionalRelationshipType(),
          getProject().getTerminology(), getProject().getVersion()).getInverse()
              .getAbbreviation();
    }

    // Create the inverse relationship
    AbstractRelationship newInverseRelationship =
        (AbstractRelationship) newRelationship.createInverseRelationship(
            newRelationship, inverseRelType, inverseAdditionalRelType);

    // Compute identity for relationship and its inverse
    // Note: need to pass in the inverse RelType and AdditionalRelType
    String newRelationshipRui = handler.getTerminologyId(newRelationship,
        inverseRelType, inverseAdditionalRelType);
    String newInverseRelationshipRui = handler.getTerminologyId(
        newInverseRelationship, newRelationship.getRelationshipType(),
        newRelationship.getAdditionalRelationshipType());

    // Check to see if relationship with matching RUI already exists in the
    // database
    Long oldRelationshipId =
        getId(relClass, newRelationshipRui, newRelationship.getTerminology());
    Long oldInverseRelationshipId = getId(relClass, newInverseRelationshipRui,
        newInverseRelationship.getTerminology());

    // If no relationships with the same RUI exists, add this new
    // relationship
    if (oldRelationshipId == null) {
      newRelationship.getAlternateTerminologyIds()
          .put(getProject().getTerminology() + "-SRC", newRelationshipRui);
      newRelationship = (AbstractRelationship) addRelationship(newRelationship);

      addCount++;
      putId(relClass, newRelationshipRui, newRelationship.getTerminology(),
          newRelationship.getId());

      // No need to explicitly attach to component - will be done
      // automatically by addRelationship.

    }
    // If an existing relationship DOES exist, update it
    else {
      boolean oldRelChanged = false;

      final AbstractRelationship oldRelationship =
          (AbstractRelationship) getRelationship(oldRelationshipId, relClass);

      // Update "alternateTerminologyIds" for the relationship
      if (!oldRelationship.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-SRC")) {
        oldRelationship.getAlternateTerminologyIds()
            .put(getProject().getTerminology() + "-SRC", newRelationshipRui);
        oldRelChanged = true;
      }

      // Update the version
      if (!oldRelationship.getVersion().equals(newRelationship.getVersion())) {
        oldRelationship.setVersion(newRelationship.getVersion());
        oldRelChanged = true;
      }

      // If the existing relationship doesn't exactly equal the new one,
      // update obsolete, suppressible, and group as well
      if (!oldRelationship.equals(newRelationship)) {
        if (oldRelationship.isObsolete() != newRelationship.isObsolete()) {
          oldRelationship.setObsolete(newRelationship.isObsolete());
          oldRelChanged = true;
        }
        if (oldRelationship.isSuppressible() != newRelationship
            .isSuppressible()) {
          oldRelationship.setSuppressible(newRelationship.isSuppressible());
          oldRelChanged = true;
        }
        if (!oldRelationship.getGroup().equals(newRelationship.getGroup())) {
          oldRelationship.setGroup(newRelationship.getGroup());
          oldRelChanged = true;
        }
      }

      if (oldRelChanged) {
        updateCount++;
        updateRelationship(oldRelationship);
      }
    }

    // If no inverse relationships with the same RUI exists, add the new
    // inverse relationship
    if (oldInverseRelationshipId == null) {
      newInverseRelationship.getAlternateTerminologyIds().put("SRC",
          newInverseRelationshipRui);
      newInverseRelationship =
          (AbstractRelationship) addRelationship(newInverseRelationship);

      addCount++;
      putId(relClass, newInverseRelationshipRui,
          newInverseRelationship.getTerminology(),
          newInverseRelationship.getId());

      // No need to explicitly attach to component - will be done
      // automatically by addRelationship.

    }
    // If an existing inverse relationship DOES exist, update it
    else {
      boolean oldInverseRelChanged = false;

      final AbstractRelationship oldInverseRelationship =
          (AbstractRelationship) getRelationship(oldInverseRelationshipId,
              relClass);

      // Update "alternateTerminologyIds" for the atom
      if (!oldInverseRelationship.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-SRC")) {
        oldInverseRelationship.getAlternateTerminologyIds().put(
            getProject().getTerminology() + "-SRC", newInverseRelationshipRui);
        oldInverseRelChanged = true;
      }

      // Update the version
      if (!oldInverseRelationship.getVersion()
          .equals(newInverseRelationship.getVersion())) {
        oldInverseRelationship.setVersion(newInverseRelationship.getVersion());
        oldInverseRelChanged = true;
      }

      // If the existing inverse relationship doesn't exactly equal the new
      // one,
      // update obsolete, suppressible, and group as well
      if (!oldInverseRelationship.equals(newInverseRelationship)) {
        if (oldInverseRelationship.isObsolete() != newInverseRelationship
            .isObsolete()) {
          oldInverseRelationship
              .setObsolete(newInverseRelationship.isObsolete());
          oldInverseRelChanged = true;
        }
        if (oldInverseRelationship.isSuppressible() != newInverseRelationship
            .isSuppressible()) {
          oldInverseRelationship
              .setSuppressible(newInverseRelationship.isSuppressible());
          oldInverseRelChanged = true;
        }
        if (!oldInverseRelationship.getGroup()
            .equals(newInverseRelationship.getGroup())) {
          oldInverseRelationship.setGroup(newInverseRelationship.getGroup());
          oldInverseRelChanged = true;
        }
      }

      if (oldInverseRelChanged) {
        updateCount++;
        updateRelationship(oldInverseRelationship);
      }
    }

    // Update the progress
    updateProgress();

    logAndCommit("[Relationship Loader] Relationships processed ",
        stepsCompleted, RootService.logCt, RootService.commitCt);
    handler.logAndCommit(
        "[Relationship Loader] Relationship Identities processed ",
        stepsCompleted, RootService.logCt, RootService.commitCt);

  }

  /**
   * Lookup relationship type.
   *
   * @param string the string
   * @return the string
   * @throws Exception the exception
   */
  private String lookupRelationshipType(String string) throws Exception {

    String relationshipType = null;

    switch (string) {
      case "RT":
        relationshipType = "RO";
        break;
      case "NT":
        relationshipType = "RN";
        break;
      case "BT":
        relationshipType = "RB";
        break;
      case "RT?":
        relationshipType = "RQ";
        break;
      case "SY":
        relationshipType = "SY";
        break;
      case "SFO/LFO":
        relationshipType = "SY";
        break;
      case "PAR":
        relationshipType = "PAR";
        break;
      case "CHD":
        relationshipType = "CHD";
        break;
      default:
        throw new Exception("Invalid relationship type: " + relationshipType);
    }

    return relationshipType;

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

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      fireProgressEvent(currentProgress,
          "RELATIONSHIPLOADING progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        // TODO - handle problem with config.properties needing properties
    }, p);

  }

  /**
   * Returns the parameters.
   *
   * @return the parameters
   */
  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

}