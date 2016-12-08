/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractSourceInsertionAlgorithm;
import com.wci.umls.server.jpa.algo.TreePositionAlgorithm;
import com.wci.umls.server.jpa.content.AtomTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.RootService;

/**
 * Implementation of an algorithm to import contexts.
 */
public class ContextLoaderAlgorithm extends AbstractSourceInsertionAlgorithm {

  /** The added tree positions. */
  private int addedTreePositions;

  /**
   * The child and descendant counts. Key = full ptr string (e.g.
   * 31926003.362215152.362207261.362220676.362208073.362250833.362169686)
   * Value.[0] = Child count Value.[1] = Descendant count
   */
  private Map<String, int[]> childAndDescendantCountsMap = new HashMap<>();

  /**
   * The lines to load. This contains ONLY lines that need to have contexts
   * loaded from the file (and not calculated).
   */
  private List<String> linesToLoad = new ArrayList<>();

  /** The created trans rels. */
  private Set<String> createdTransRels = new HashSet<>();

  /** The referenced terms. */
  private Set<Terminology> referencedTerms = new HashSet<>();

  /**
   * Instantiates an empty {@link ContextLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ContextLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("CONTEXTLOADER");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Context Loading requires a project to be set");
    }

    // Check the input directories

    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting CONTEXTLOADING");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    try {

      logInfo("[ContextLoader] Checking for new/updated Contexts");
      //
      // Load the contexts.src file
      //
      List<String> lines =
          loadFileIntoStringList(getSrcDirFile(), "contexts.src", null, null);

      // Scan the contexts.src file and see if HCD (hierarchical code)
      // for a given terminology is populated.
      final Set<Terminology> termsWithHcd = findTermsWithHcd(lines);
      final Set<Terminology> computedTerms = new HashSet<>();

      final String fields[] = new String[17];
      for (final String line : lines) {

        FieldedStringTokenizer.split(line, "|", 17, fields);

        final Terminology specifiedTerm = getCachedTerminology(fields[4]);

        if (specifiedTerm == null) {
          logWarn("Warning - terminology not found: " + fields[6]
              + ". Could not process the following line:\n\t" + line);
          continue;
        }

        // If the specified terminology never has a populated HCD, the
        // transitive relationships and tree positions can be computed.
        if (!termsWithHcd.contains(specifiedTerm)) {
          // Only compute once per terminology
          if (!computedTerms.contains(specifiedTerm)) {
            calculateContexts(specifiedTerm);
            computedTerms.add(specifiedTerm);
          }
        }
        // If the specified terminology has a populated HCD, we need to load the
        // Transitive Relationships and Tree Positions from the file contents.
        else {

          // Save this line to process later
          linesToLoad.add(line);

          // Populate childAndDescendant counts based on this line's PTR

          final String parentTreeRel = fields[7];

          // If this particular full PTR has never been seen, add with a child
          // and descendant count of 1 each.
          if (!childAndDescendantCountsMap.containsKey(parentTreeRel)) {
            childAndDescendantCountsMap.put(parentTreeRel, new int[] {
                1, 1
            });
          }
          // If it has been seen before, increment both child and descendent
          // counts by 1
          else {
            int[] currentChildDescendantCount =
                childAndDescendantCountsMap.get(parentTreeRel);
            childAndDescendantCountsMap.put(parentTreeRel, new int[] {
                ++currentChildDescendantCount[0],
                ++currentChildDescendantCount[1]
            });
          }

          String parentTreeRelSub = parentTreeRel;

          // Loop through the parentTreeRel string, stripping off trailing
          // elements until they're gone.
          do {
            parentTreeRelSub = parentTreeRelSub.substring(0,
                parentTreeRelSub.lastIndexOf("."));
            // If this particular sub PTR has never been seen, add with a child
            // count of 0, and a descendant count of 1.
            if (!childAndDescendantCountsMap.containsKey(parentTreeRelSub)) {
              childAndDescendantCountsMap.put(parentTreeRelSub, new int[] {
                  0, 1
              });
            }
            // If it has been seen before, increment descendant count only by 1
            else {
              int[] currentChildDescendantCount =
                  childAndDescendantCountsMap.get(parentTreeRelSub);
              childAndDescendantCountsMap.put(parentTreeRelSub, new int[] {
                  currentChildDescendantCount[0],
                  ++currentChildDescendantCount[1]
              });
            }
          } while (parentTreeRelSub.contains("."));
        }
      }

      // Set the number of steps to the number of contexts.src lines that will
      // be actually loaded
      setSteps(linesToLoad.size());

      for (String line : linesToLoad) {
        // Check for a cancelled call once every 100 lines
        if (getStepsCompleted() % 100 == 0) {
          if (isCancelled()) {
            throw new CancelException("Cancelled");
          }
        }

        loadContexts(line);

        // Update the progress
        updateProgress();
      }

      commitClearBegin();

      // Remove all the "old version" transitive relationships for the
      // terminology.
      int removedRelCount = 0;
      for (Terminology term : referencedTerms) {
        removedRelCount += removeTransRels(term, true);
        commitClearBegin();
      }

      logInfo("[ContextLoader] Loaded " + createdTransRels.size()
          + " new Transitive Relationships from file.");
      logInfo("[ContextLoader] Removed " + removedRelCount
          + " old-version Transitive Relationships.");
      logInfo("[ContextLoader] Loaded " + addedTreePositions
          + " new Tree Positions from file.");

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished CONTEXTLOADING");

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Removes the trans rels.
   *
   * @param term the term
   * @param oldVersions the old versions
   * @return the int
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private int removeTransRels(Terminology term, boolean oldVersions)
    throws Exception {
    int removedCount = 0;

    if (oldVersions) {
      logInfo(
          "[ContextLoader] Removing old-version transitive relationships for terminology: "
              + term.getTerminology());
    } else {
      logInfo(
          "[ContextLoader] Removing transitive relationships for terminology: "
              + term.getTerminology() + ", version: " + term.getVersion());
    }

    IdType organizingClassType = term.getOrganizingClassType();
    Class<?> clazz = null;

    if (organizingClassType.equals(IdType.CONCEPT)) {
      clazz = ConceptTransitiveRelationshipJpa.class;
    } else if (organizingClassType.equals(IdType.DESCRIPTOR)) {
      clazz = DescriptorTransitiveRelationshipJpa.class;
    } else if (organizingClassType.equals(IdType.CODE)) {
      clazz = CodeTransitiveRelationshipJpa.class;
    } else if (organizingClassType.equals(IdType.ATOM)) {
      clazz = AtomTransitiveRelationshipJpa.class;
    }

    Query query = manager.createQuery("SELECT a.id FROM "
        + clazz.getSimpleName() + " a WHERE terminology = :terminology "
        + " AND " + (oldVersions ? "NOT" : "") + " version = :version");
    query.setParameter("terminology", term.getTerminology());
    query.setParameter("version", term.getVersion());
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTransitiveRelationship(id,
          (Class<? extends TransitiveRelationship<? extends AtomClass>>) clazz);
      logAndCommit(removedCount++, RootService.logCt, RootService.commitCt);
    }

    return removedCount;
  }

  /**
   * Removes the tree positions.
   *
   * @param term the term
   * @param oldVersions the old versions
   * @return the int
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private int removeTreePositions(Terminology term, boolean oldVersions)
    throws Exception {
    int removedCount = 0;

    if (oldVersions) {
      logInfo(
          "[ContextLoader] Removing old-version tree positions for terminology: "
              + term.getTerminology());
    } else {
      logInfo("[ContextLoader] Removing tree positions for terminology: "
          + term.getTerminology() + ", version: " + term.getVersion());
    }

    IdType organizingClassType = term.getOrganizingClassType();
    Class<?> clazz = null;

    if (organizingClassType.equals(IdType.CONCEPT)) {
      clazz = ConceptTreePositionJpa.class;
    } else if (organizingClassType.equals(IdType.DESCRIPTOR)) {
      clazz = DescriptorTreePositionJpa.class;
    } else if (organizingClassType.equals(IdType.CODE)) {
      clazz = CodeTreePositionJpa.class;
    } else if (organizingClassType.equals(IdType.ATOM)) {
      clazz = AtomTreePositionJpa.class;
    }

    Query query = manager.createQuery("SELECT a.id FROM "
        + clazz.getSimpleName() + " a WHERE terminology = :terminology "
        + " AND " + (oldVersions ? "NOT" : "") + " version = :version");
    query.setParameter("terminology", term.getTerminology());
    query.setParameter("version", term.getVersion());
    for (final Long id : (List<Long>) query.getResultList()) {
      removeTreePosition(id,
          (Class<? extends TreePosition<? extends AtomClass>>) clazz);
      logAndCommit(removedCount++, RootService.logCt, RootService.commitCt);
    }

    return removedCount;
  }

  /**
   * Calculate contexts.
   *
   * @param term the term
   * @throws Exception the exception
   */
  private void calculateContexts(Terminology term) throws Exception {

    // Check for a cancelled call before starting
    if (isCancelled()) {
      throw new CancelException("Cancelled");
    }

    // Don't handle transitiveRelationships
    // //
    // // Compute transitive closures
    // //
    // TransitiveClosureAlgorithm algo = null;
    // // Only compute for organizing class types
    // if (term.getOrganizingClassType() != null) {
    // algo = new TransitiveClosureAlgorithm();
    // algo.setLastModifiedBy(getLastModifiedBy());
    // algo.setTerminology(term.getTerminology());
    // algo.setVersion(term.getVersion());
    // algo.setIdType(term.getOrganizingClassType());
    // // some terminologies may have cycles, allow these for now.
    // algo.setCycleTolerant(true);
    // algo.compute();
    // algo.close();
    //
    // }

    //
    // Compute tree positions
    //
    TreePositionAlgorithm algo2 = null;

    // Only compute for organizing class types
    if (term.getOrganizingClassType() != null) {
      algo2 = new TreePositionAlgorithm();
      algo2.setLastModifiedBy(getLastModifiedBy());
      algo2.setTerminology(term.getTerminology());
      algo2.setVersion(term.getVersion());
      algo2.setIdType(term.getOrganizingClassType());
      algo2.setWorkId(getWorkId());
      algo2.setActivityId(UUID.randomUUID().toString());
      // some terminologies may have cycles, allow these for now.
      algo2.setCycleTolerant(true);
      algo2.setComputeSemanticType(false);
      algo2.compute();
      algo2.close();
    }
  }

  /**
   * Load contexts.
   *
   * @param line the line
   * @throws Exception the exception
   */
  private void loadContexts(String line) throws Exception {

    final String fields[] = new String[17];
    FieldedStringTokenizer.split(line, "|", 17, fields);

    // If sg_type_1 and sg_type_2 don't match, fire a warning and skip the
    // line.
    if (!fields[12].equals(fields[15])) {
      logWarn("Warning - type 1: " + fields[12] + " does not equals type 2: "
          + fields[15] + ". Could not process the following line:\n\t" + line);
      return;
    }

    // Extract the "ptr" field 12345.12346.12347 and the sg_type_1
    // field -> this will tell you the type of object (e.g.
    // ConceptTransitiveRelationship
    final String parentTreeRel = fields[7];

    List<Atom> ptrAtoms = new ArrayList<>();
    List<String> ptrAltIds = new ArrayList<>();
    ptrAltIds.addAll(Arrays.asList(parentTreeRel.split("\\.")));

    // Add the atom alternate Id in the first column to the end of the list
    // (needed in that position for Transitive Relationships, and Tree Positions
    // will use the loaded atom as well)
    ptrAltIds.add(fields[0]);

    for (String element : ptrAltIds) {

      final Atom atom = (Atom) getComponent("SRC_ATOM_ID", element, null, null);

      // If Atom can't be found, fire a warning and move onto the
      // next line of the file.
      if (atom == null) {
        // EXCEPTION: if this is the first id in the list, the reason it
        // couldn't be found is likely because it is a SRC atom. In this case,
        // don't error out - just skip the element and continue.
        if (ptrAltIds.indexOf(element) == 0) {
          continue;
        }
        logWarn("Warning - atom not found for alternate Terminology Id: "
            + element + ". Could not process the following line:\n\t" + line);
        return;
      }

      ptrAtoms.add(atom);
    }

    // Check the first atom to make sure it isn't a SRC atom. If it is, drop it
    // from the list.
    if (ptrAtoms.get(0).getTerminology().equals("SRC")) {
      ptrAtoms.remove(0);
    }

    // Don't handle transitiveRelationships
    // createTransitiveRelationships(clazz, ptrAtoms);

    // Tree Positions use the last atom in the list (which was loaded from the
    // first column of the line) for determining the node.
    Atom nodeAtom = ptrAtoms.get(ptrAtoms.size() - 1);
    createTreePositions(fields[12], nodeAtom, parentTreeRel, fields[6]);
  }

  // /**
  // * Creates the transitive relationships.
  // *
  // * @param clazz the clazz
  // * @param ptrAtoms the ptr atoms
  // * @throws Exception the exception
  // */
  // private void createTransitiveRelationships(Class<?> clazz,
  // List<Atom> ptrAtoms) throws Exception {
  // // For transitive relationships, create one from each "higher" level
  // // object to each "lower" level one (e.g. in the example above
  // // 12345->12346, 12345->123467, and 12346->123467). Save these
  // // pairwise connections so you don't recreate objects for the same
  // // pairs (e.g. have a Set<String> that stores superTypeId+subTypeId)
  //
  // // Can't create relationships if there aren't any atoms in the
  // // list...
  // if (ptrAtoms.isEmpty()) {
  // return;
  // }
  //
  // final Atom superAtom = ptrAtoms.get(0);
  // final AbstractAtomClass superAtomContainer =
  // getCachedAtomContainer(clazz, superAtom);
  // int depthCounter = 0;
  // for (Atom subAtom : ptrAtoms) {
  // final AbstractAtomClass subAtomContainer =
  // getCachedAtomContainer(clazz, subAtom);
  //
  // // Skip if this pair of containers have already created a Transitive
  // // Relationship created for them.
  // if (createdTransRels.contains(superAtomContainer.getId().toString() + "_"
  // + subAtomContainer.getId().toString())) {
  // continue;
  // }
  //
  // TransitiveRelationship<? extends ComponentHasAttributes> newTransRel =
  // null;
  //
  // if (Concept.class.isAssignableFrom(clazz)) {
  // final ConceptTransitiveRelationshipJpa ctr =
  // new ConceptTransitiveRelationshipJpa();
  // ctr.setSubType((Concept) subAtomContainer);
  // ctr.setSuperType((Concept) superAtomContainer);
  // newTransRel = ctr;
  // } else if (Descriptor.class.isAssignableFrom(clazz)) {
  // final DescriptorTransitiveRelationshipJpa dtr =
  // new DescriptorTransitiveRelationshipJpa();
  // dtr.setSubType((Descriptor) subAtomContainer);
  // dtr.setSuperType((Descriptor) superAtomContainer);
  // newTransRel = dtr;
  // } else if (Code.class.isAssignableFrom(clazz)) {
  // final CodeTransitiveRelationshipJpa cdtr =
  // new CodeTransitiveRelationshipJpa();
  // cdtr.setSubType((Code) subAtomContainer);
  // cdtr.setSuperType((Code) superAtomContainer);
  // newTransRel = cdtr;
  // } else if (Atom.class.isAssignableFrom(clazz)) {
  // final AtomTransitiveRelationshipJpa atr =
  // new AtomTransitiveRelationshipJpa();
  // atr.setSubType(subAtom);
  // atr.setSuperType(superAtom);
  // newTransRel = atr;
  // }
  //
  // newTransRel.setDepth(depthCounter++);
  // newTransRel.setObsolete(false);
  // newTransRel.setPublishable(true);
  // newTransRel.setPublished(false);
  // newTransRel.setSuppressible(false);
  // newTransRel.setTerminology(newTransRel.getSuperType().getTerminology());
  // newTransRel.setTerminologyId("");
  // newTransRel.setVersion(newTransRel.getSuperType().getVersion());
  //
  // // persist the Transitive Relationship
  // addTransitiveRelationship(newTransRel);
  // createdTransRels.add(superAtomContainer.getId().toString() + "_"
  // + subAtomContainer.getId().toString());
  // }
  //
  // // Once all of the relationships have been made with this super atom,
  // remove
  // // it from the list, and run the remaining ones through again.
  // List<Atom> shortenedAtomList = new ArrayList<>(ptrAtoms);
  // shortenedAtomList.remove(superAtom);
  // createTransitiveRelationships(clazz, shortenedAtomList);
  //
  // }

  /**
   * Creates the tree positions.
   *
   * @param idType the id type
   * @param nodeAtom the node atom
   * @param parentTreeRel the parent tree rel
   * @param hcd the hcd
   * @throws Exception the exception
   */
  private void createTreePositions(String idType, Atom nodeAtom,
    String parentTreeRel, String hcd) throws Exception {
    // For tree positions, create one each line. The
    // "node" will always be based on the first field of the
    // contexts.src file.

    String ancestorPath = parentTreeRel.replace('.', '~');

    // Instantiate the tree position
    TreePosition<? extends ComponentHasAttributesAndName> newTreePos = null;
    if (idType.equals("SOURCE_CUI")) {
      final ConceptTreePosition ctp = new ConceptTreePositionJpa();
      final Concept concept = (Concept) getComponent(idType,
          nodeAtom.getConceptId(), nodeAtom.getTerminology(), null);
      ctp.setNode(concept);
      newTreePos = ctp;
    } else if (idType.equals("SOURCE_DUI")) {
      final DescriptorTreePosition dtp = new DescriptorTreePositionJpa();
      final Descriptor descriptor = (Descriptor) getComponent(idType,
          nodeAtom.getConceptId(), nodeAtom.getTerminology(), null);
      dtp.setNode(descriptor);
      newTreePos = dtp;
    } else if (idType.equals("CODE_SOURCE")) {
      final CodeTreePosition ctp = new CodeTreePositionJpa();
      final Code code = (Code) getComponent(idType, nodeAtom.getConceptId(),
          nodeAtom.getTerminology(), null);
      ctp.setNode(code);
      newTreePos = ctp;
    } else if (idType.equals(IdType.ATOM)) {
      final AtomTreePosition atp = new AtomTreePositionJpa();
      final Atom atom = nodeAtom;
      atp.setNode(atom);
      newTreePos = atp;
    } else {
      throw new Exception("Unsupported id type: " + idType);
    }
    newTreePos.setObsolete(false);
    newTreePos.setSuppressible(false);
    newTreePos.setPublishable(true);
    newTreePos.setPublished(false);
    newTreePos.setAncestorPath(ancestorPath);
    newTreePos.setTerminology(newTreePos.getNode().getTerminology());
    newTreePos.setVersion(newTreePos.getNode().getVersion());
    newTreePos.setChildCt(childAndDescendantCountsMap.get(parentTreeRel)[0]);
    newTreePos
        .setDescendantCt(childAndDescendantCountsMap.get(parentTreeRel)[1]);
    newTreePos.setTerminologyId(hcd);

    // persist the tree position
    addTreePosition(newTreePos);
    addedTreePositions++;

  }

  /**
   * Scan terms and hcds.
   *
   * @param lines the lines
   * @return the map
   * @throws Exception the exception
   */
  private Set<Terminology> findTermsWithHcd(List<String> lines)
    throws Exception {
    Set<Terminology> termsWithHcds = new HashSet<>();

    String fields[] = new String[17];
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 17, fields);

      final String termNameAndVersion = fields[4];
      final Boolean termHasHcd = !ConfigUtility.isEmpty(fields[6]);

      Terminology terminology = getCachedTerminology(termNameAndVersion);

      if (terminology == null) {
        // No need to fire a warning here - will be done in compute
        continue;
      }

      // Add all terms with populated hcd's to the set
      if (termHasHcd) {
        termsWithHcds.add(terminology);
      }

      // Add all unique terminologies to the referencedTerminologies set
      referencedTerms.add(terminology);

    }

    return termsWithHcds;
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    //
    // Delete any TreePositions and TransitiveRelationships for all terminology
    // and versions referenced in the contexts.src file.

    // No molecular actions will be generated by this algorithm reset
    setMolecularActionFlag(false);

    // Make sure the process' preconditions are still valid, and that the
    // srcDirFile is set.
    checkPreconditions();

    logInfo(
        "[ContextLoader] Reset: removing all Tree Positions and Transitive Relationships added by previous run");
    //
    // Load the contexts.src file
    //
    List<String> lines =
        loadFileIntoStringList(getSrcDirFile(), "contexts.src", null, null);

    // Scan through contexts.src, and collect all terminology/versions
    // referenced.
    Set<String> terminologyAndVersions = new HashSet<>();

    String fields[] = new String[17];
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 17, fields);

      final String termNameAndVersion = fields[4];
      terminologyAndVersions.add(termNameAndVersion);
    }

    int removedRelCount = 0;
    int removedTreePosCount = 0;

    for (String terminologyVersion : terminologyAndVersions) {
      Terminology terminology = getCachedTerminology(terminologyVersion);
      removedRelCount += removeTransRels(terminology, false);
      removedTreePosCount += removeTreePositions(terminology, false);

      commitClearBegin();
    }

    logInfo("[ContextLoader] Removed " + removedRelCount
        + " Transitive Relationships added in previous run.");
    logInfo("[ContextLoader] Removed " + removedTreePosCount
        + " Tree Positions added in previous run.");

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
    return "Loads and processes contexts.src and computes tree positions where possible from PAR/CHD relationships.";
  }

}