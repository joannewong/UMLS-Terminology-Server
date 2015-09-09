/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.SimpleRootClassChecker;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.jpa.services.helper.OwlUtility;
import com.wci.umls.server.jpa.services.helper.TerminologyUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.GeneralConceptAxiom;
import com.wci.umls.server.model.meta.Abbreviation;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Implementation of an algorithm to import Owl data.
 */
public class OwlLoaderAlgorithm extends HistoryServiceJpa implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The logging object ct threshold. */
  private final static int logCt = 2000;

  /** The commit count. */
  private final static int commitCt = 2000;

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String version;

  /** release version. */
  private String releaseVersion;

  /** The release version date. */
  private Date releaseVersionDate;

  /** counter for objects created, reset in each load section. */
  int objectCt;

  /** The input file. */
  private String inputFile;

  /** The additional relationship types. */
  private Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();

  /** The atn map. */
  private Map<String, AttributeName> atnMap = new HashMap<>();

  /** The id map. */
  private Map<String, Long> idMap = new HashMap<>();

  /** The disjoint map. */
  private Map<String, Set<String>> disjointMap = new HashMap<>();

  /** The anonymous expr map. */
  private Map<String, OWLClassExpression> anonymousExprMap = new HashMap<>();

  /** The term types. */
  private Set<String> termTypes = new HashSet<>();

  /** The languages. */
  private Set<String> languages = new HashSet<>();

  /** The concept attribute values. */
  private Set<String> generalEntryValues = new HashSet<>();

  /** The root class checker. */
  private SimpleRootClassChecker rootClassChecker = null;

  /** The top concept. */
  private Concept topConcept = null;

  /** The load as inferred. */
  private boolean loadInferred = false;

  /** The loader. */
  private final String label = "label";

  /** The comment. */
  private final String comment = "comment";

  /** The loader. */
  private final String loader = "loader";

  /** The published. */
  private final String published = "PUBLISHED";

  /** The current date. */
  private final Date currentDate = new Date();

  /** The el2 profile. */
  private final String el2Profile =
      "http://www.w3.org/TR/owl2-profiles/#OWL_2_EL";

  /** The dl2 profile. */
  private final String dl2Profile =
      "http://www.w3.org/TR/owl2-profiles/#OWL_2_DL";

  /**
   * Instantiates an empty {@link OwlLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public OwlLoaderAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the terminology version.
   *
   * @param version the terminology version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Returns the input file.
   *
   * @return the input file
   */
  public String getInputFile() {
    return inputFile;
  }

  /**
   * Sets the input file.
   *
   * @param inputFile the input file
   */
  public void setInputFile(String inputFile) {
    this.inputFile = inputFile;
  }

  /* see superclass */
  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass()).info("Starting loading Owl terminology");
    Logger.getLogger(getClass()).info("  inputFile = inputFile");
    Logger.getLogger(getClass()).info("  terminology = " + terminology);
    Logger.getLogger(getClass()).info("  version = " + version);

    try {

      setTransactionPerOperation(false);
      beginTransaction();

      if (!new File(inputFile).exists()) {
        throw new Exception("Specified input file does not exist");
      }

      //
      // Load ontology into memory
      final FileInputStream in = new FileInputStream(new File(inputFile));
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology directOntology = manager.loadOntologyFromOntologyDocument(in);

      //
      // Check compliance
      //
      Logger.getLogger(getClass()).info("Testing compliance ");
      Logger.getLogger(getClass()).info(
          "  profile = " + getConfigurableValue(terminology, "profile"));
      if ("EL".equals(getConfigurableValue(terminology, "profile"))) {
        OwlUtility.checkOWL2ELProfile(directOntology);
      } else if ("DL".equals(getConfigurableValue(terminology, "profile"))) {
        OwlUtility.checkOWL2DLProfile(directOntology);
      } else {
        // no profile checking - other assumptions will be tested
      }

      //
      // Determine version
      //
      releaseVersion = getReleaseVersion(directOntology);
      if (releaseVersion != null) {
        // TODO: consider other options
        try {
          releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);
        } catch (Exception e) {
          releaseVersionDate = new Date();
        }
      } else {
        releaseVersion = version;
        releaseVersionDate = currentDate;
      }

      //
      // Set "load as inferred" flag
      //
      loadInferred =
          "true".equals(getConfigurableValue(terminology, "loadInferred"));

      //
      // Add the root concept, if configured to do so
      //
      if ("true".equals(getConfigurableValue(terminology, "top"))) {
        loadTopConcept(directOntology);
      }

      //
      // Initialize root class checker
      //
      rootClassChecker =
          new SimpleRootClassChecker(directOntology.getImportsClosure());

      //
      // Load ontology import closure
      //
      for (OWLOntology ontology : directOntology.getImportsClosure()) {
        Logger.getLogger(getClass()).info("Processing ontology - " + ontology);
        loadOntology(ontology);
      }

      //
      // Handle metadata (after all ontology processing is done)
      //
      loadMetadata(directOntology);

      //
      // Handle reasoner and inferences
      //
      if ("true".equals(getConfigurableValue(terminology, "computeInferred"))) {
        for (OWLOntology ontology : directOntology.getImportsClosure()) {
          Logger.getLogger(getClass()).info(
              "Processing inferred ontology - " + ontology);
          loadInferred(ontology);
        }
      }

      //
      // Create ReleaseInfo for this release if it does not already exist
      //
      loadReleaseInfo();

      close();

      Logger.getLogger(getClass()).info("Done ...");

    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Owl loader failed", e);
    } finally {
      // tbd
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  /**
   * Adds the progress listener.
   *
   * @param l the l
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  /**
   * Removes the progress listener.
   *
   * @param l the l
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  /**
   * Cancel.
   */
  @Override
  public void cancel() {
    // n/a
  }

  /**
   * Load metadata.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadMetadata(OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("Load metadata");
    // relationship types - CHD, PAR, and RO
    String[] relTypes = new String[] {
        "other", "subClassOf", "superClassOf"
    };
    RelationshipType chd = null;
    RelationshipType par = null;
    RelationshipType ro = null;
    for (String rel : relTypes) {
      final RelationshipType type = new RelationshipTypeJpa();
      setCommonFields(type);
      type.setAbbreviation(rel);
      if (rel.equals("subClassOf")) {
        chd = type;
        type.setExpandedForm("Sub class of");
      } else if (rel.equals("superClassOf")) {
        par = type;
        type.setExpandedForm("Super class of");
      } else if (rel.equals("other")) {
        ro = type;
        type.setExpandedForm("Other");
      } else {
        throw new Exception("Unhandled type");
      }
      addRelationshipType(type);
    }
    chd.setInverse(par);
    par.setInverse(chd);
    ro.setInverse(ro);
    updateRelationshipType(chd);
    updateRelationshipType(par);
    updateRelationshipType(ro);

    // Term types
    for (String tty : termTypes) {

      final TermType termType = new TermTypeJpa();
      setCommonFields(termType);
      termType.setAbbreviation(tty);
      termType.setCodeVariantType(CodeVariantType.UNDEFINED);
      termType.setExpandedForm(tty);
      if (idMap.containsKey(tty)) {
        termType.setExpandedForm(getConcept(idMap.get(tty)).getName());
      }
      termType.setHierarchicalType(false);
      termType.setNameVariantType(NameVariantType.UNDEFINED);
      termType.setStyle(TermTypeStyle.SEMANTIC);
      termType.setUsageType(UsageType.UNDEFINED);
      addTermType(termType);
    }

    // Build precedence list
    final PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);

    final List<KeyValuePair> lkvp = new ArrayList<>();
    // Start with "preferred"
    for (String tty : termTypes) {
      if (isPreferredType(tty)) {
        final KeyValuePair pair = new KeyValuePair();
        pair.setKey(terminology);
        pair.setValue(tty);
        lkvp.add(pair);
      }
    }
    // Next, do label (unless already done)
    if (!isPreferredType(label)) {
      KeyValuePair pr = new KeyValuePair();
      pr.setKey(terminology);
      pr.setValue(label);
      lkvp.add(pr);
    }
    // then comment
    KeyValuePair pr = new KeyValuePair();
    pr.setKey(terminology);
    pr.setValue(comment);
    lkvp.add(pr);
    // next do anything else that is not the preferred type or label or comment
    for (String tty : termTypes) {
      if (!isPreferredType(tty) && !tty.equals(label) && !tty.equals(comment)) {
        final KeyValuePair pair = new KeyValuePair();
        pair.setKey(terminology);
        pair.setValue(tty);
        lkvp.add(pair);
      }
    }

    final KeyValuePairList kvpl = new KeyValuePairList();
    kvpl.setKeyValuePairList(lkvp);
    list.setPrecedence(kvpl);
    list.setTimestamp(releaseVersionDate);
    list.setLastModified(releaseVersionDate);
    list.setLastModifiedBy(loader);
    list.setName("DEFAULT");
    list.setTerminology(terminology);
    list.setVersion(version);
    addPrecedenceList(list);

    // Root Terminology
    RootTerminology root = new RootTerminologyJpa();
    root.setFamily(terminology);
    root.setHierarchicalName(getRootTerminologyPreferredName(ontology));
    // Unable to determine overall "language" from OWL (unless maybe in headers)
    root.setLanguage(null);
    root.setTimestamp(releaseVersionDate);
    root.setLastModified(releaseVersionDate);
    root.setLastModifiedBy(loader);
    root.setPolyhierarchy(true);
    root.setPreferredName(getRootTerminologyPreferredName(ontology));
    root.setRestrictionLevel(-1);
    root.setTerminology(terminology);
    addRootTerminology(root);

    // Terminology
    Terminology term = new TerminologyJpa();
    term.setTerminology(terminology);
    term.setVersion(version);
    term.setTimestamp(releaseVersionDate);
    term.setLastModified(releaseVersionDate);
    term.setLastModifiedBy(loader);
    term.setAssertsRelDirection(true);
    term.setCurrent(true);
    term.setDescriptionLogicTerminology(true);
    if ("EL".equals(getConfigurableValue(terminology, "profile"))) {
      term.setDescriptionLogicProfile(el2Profile);
    } else if ("DL".equals(getConfigurableValue(terminology, "profile"))) {
      term.setDescriptionLogicProfile(dl2Profile);
    }
    term.setOrganizingClassType(IdType.CONCEPT);
    term.setPreferredName(getTerminologyPreferredName(ontology));
    term.setRootTerminology(root);
    // package comment as a citation
    String comment = getComment(ontology);
    if (comment != null) {
      term.setCitation(new CitationJpa(comment));
    }
    addTerminology(term);

    String[] labels = new String[] {
        "Tree_Sort_Field", "Atoms_Label", "Attributes_Label"
    };
    String[] labelValues = new String[] {
        "nodeName", "Labels", "Properties"
    };
    int i = 0;
    for (String label : labels) {
      GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      setCommonFields(entry);
      entry.setAbbreviation(label);
      entry.setExpandedForm(labelValues[i++]);
      entry.setKey("label_metadata");
      entry.setType("label_values");
      addGeneralMetadataEntry(entry);
    }

    // Commit
    commitClearBegin();
  }

  /**
   * Returns the terminology preferred name.
   *
   * @param ontology the ontology
   * @return the terminology preferred name
   * @throws Exception the exception
   */
  private String getRootTerminologyPreferredName(OWLOntology ontology)
    throws Exception {
    // Get the rdfs:label property of the ontology itself
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isLabel()) {
        return getValue(annotation);
      }
    }
    // otherwise, just use the terminology name
    return terminology;
  }

  /**
   * Returns the terminology preferred name.
   *
   * @param ontology the ontology
   * @return the terminology preferred name
   * @throws Exception the exception
   */
  private String getTerminologyPreferredName(OWLOntology ontology)
    throws Exception {

    // If >1 owl:versionInfo, use the first one
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().toString().equals("owl:versionInfo")) {
        return getValue(annotation);
      }
    }
    // otherwise try rdfs:label
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isLabel()) {
        return getValue(annotation);
      }
    }

    return terminology;
  }

  /**
   * Load metadata.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadInferred(OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("Load inferred axioms");

    //OWLReasonerFactory reasonerFactory = new SnorocketReasonerFactory();
    //OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

    OWLReasoner reasoner=new Reasoner.ReasonerFactory().createReasoner(ontology);
    
    // Not 100% sure why this is important, vs having no arguments
    reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

    //
    if (!reasoner.isConsistent()) {
      throw new Exception("Unexpected inconsistent ontology");
    }

    // Check unsatisfiable classes
    Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
    // This node contains owl:Nothing and all the classes that are
    // equivalent to owl:Nothing - i.e. the unsatisfiable classes. We just
    // want to print out the unsatisfiable classes excluding owl:Nothing,
    // and we can used a convenience method on the node to get these
    Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
    Logger.getLogger(getClass()).error(
        "  UNSATISFIABLE CLASSES - equal to owl:Nothing");
    if (!unsatisfiable.isEmpty()) {
      for (OWLClass cls : unsatisfiable) {
        Logger.getLogger(getClass()).error("    class = " + cls);
      }
      throw new Exception("Unexpected unsatisfiable classes.");
    }

    // Check for unexpected equivalent classes among "named" classes
    // Add all sub/super-class relationships and add them as inferred
    boolean flag = false;
    for (OWLClass owlClass : ontology.getClassesInSignature()) {
      // get sub-classes
      for (Node<OWLClass> chdClass : reasoner.getSubClasses(owlClass, true)) {
        if (chdClass.getSize() > 1) {
          Logger.getLogger(getClass()).error(
              "  EQUIVALENT detected " + chdClass);
          flag = true;
        }
      }
    }
    if (flag) {
      // TODO: reactivate this, or make failure configurable.
      // throw new
      // Exception("Unexpected equivalencies detected, see log above.");
    }

    // Add all sub/super-class relationships and add them as inferred
    // Also add any restrictions at this point
    Logger.getLogger(getClass())
        .info("  Add inferred subClassOf relationships");
    objectCt = 0;
    for (OWLClass owlClass : ontology.getClassesInSignature()) {
      // get sub-classes
      for (Node<OWLClass> chdNode : reasoner.getSubClasses(owlClass, true)) {
        // generally there should only be one here
        for (OWLClass chdClass : chdNode.getEntities()) {
          // Break if bottom node encountered
          if (chdClass.isBottomEntity()) {
            break;
          }
          Concept par =
              getConcept(idMap.get(getTerminologyId(owlClass.getIRI())));
          Concept chd =
              getConcept(idMap.get(getTerminologyId(chdClass.getIRI())));
          ConceptRelationship rel = getSubClassOfRelationship(chd, par);
          rel.setInferred(true);
          rel.setStated(false);
          Logger.getLogger(getClass()).debug("  add relationship - " + rel);
          addRelationship(rel);
          chd.addRelationship(rel);
          logAndCommit(++objectCt);
        }
      }

    }

    // For each class, follow superclass path and gather all non "subClassOf"
    // relationships. Verify there are no duplicates on
    // additionalRelationshipType
    // add inferred rels to the same concepts.
    Logger.getLogger(getClass()).info(
        "  Add inferred restriction relationships");

    // Start at the top node and work down
    // if defined, just infer directly those relationships
    // if primitive, infer the non-subClassOf relationships of the parent

    objectCt = 0;
    if (reasoner.getTopClassNode().getEntities().size() != 1) {
      throw new Exception("Unexpected equivalent classes at top node");
    }
    OWLClass topClass =
        reasoner.getTopClassNode().getEntities().iterator().next();
    // infer non isa relationships at parent level to sub-class level
    inferRelationships(topClass, reasoner, ontology, 0);

  }

  /**
   * Returns the all superclass relationships.
   *
   * @param owlClass the owl class
   * @param reasoner the reasoner
   * @param ontology the ontology
   * @param ct the ct
   * @return the all superclass relationships
   * @throws Exception the exception
   */
  private int inferRelationships(OWLClass owlClass, OWLReasoner reasoner,
    OWLOntology ontology, int ct) throws Exception {
    int localCt = 0;

    // Get the child concept
    Concept parConcept = getConceptForOwlClass(owlClass, ontology, 1);
    Logger.getLogger(getClass()).debug(
        "  Infer relationships - " + parConcept.getTerminologyId());

    // Get direct sub classes
    Logger.getLogger(getClass()).debug(
        "    subclass count = "
            + reasoner.getSubClasses(owlClass, true).getNodes().size());
    for (Node<OWLClass> node : reasoner.getSubClasses(owlClass, true)) {

      // for each one, borrow from the parent class
      for (OWLClass chdClass : node.getEntities()) {

        // Skip bottom node
        if (chdClass.isBottomEntity()) {
          continue;
        }

        // Get the child concept
        Concept chdConcept = getConceptForOwlClass(chdClass, ontology, 1);

        // If fully defined, copy all stated non subClassOf rels as inferred
        if (chdConcept.isFullyDefined()) {
          Logger.getLogger(getClass()).debug("    fully defined");
          Set<ConceptRelationship> relsToAdd = new HashSet<>();
          for (ConceptRelationship rel : chdConcept.getRelationships()) {
            if (rel.isStated() && !rel.isHierarchical()) {
              ConceptRelationship rel2 = new ConceptRelationshipJpa(rel, true);
              rel2.setInferred(true);
              rel2.setStated(false);
              rel2.setId(null);
              if (!chdConcept.getRelationships().contains(rel2)) {
                Logger.getLogger(getClass()).debug(
                    "  add relationship - " + rel2);
                addRelationship(rel2);
                relsToAdd.add(rel2);
                logAndCommit(++localCt + ct);
              }
            }
          }
          for (ConceptRelationship rel : relsToAdd) {
            chdConcept.addRelationship(rel);
          }
        }

        // otherwise, determine the correct set of relationships from
        // the par/chd concept and infer them to the child
        else {
          Logger.getLogger(getClass()).debug("    primitive");
          Set<ConceptRelationship> relsToInfer =
              getRelationshipsToInfer(parConcept, chdConcept, reasoner);

          for (ConceptRelationship rel : relsToInfer) {
            ConceptRelationship rel2 = new ConceptRelationshipJpa(rel, true);
            rel2.setFrom(chdConcept);
            rel2.setInferred(true);
            rel2.setStated(false);
            rel2.setId(null);
            Logger.getLogger(getClass()).debug("  add relationship - " + rel2);
            addRelationship(rel2);
            chdConcept.addRelationship(rel2);
            logAndCommit(++localCt + ct);
          }
        }

        // Handle children as parents
        localCt += inferRelationships(chdClass, reasoner, ontology, localCt);
      }

    }

    return localCt;
  }

  /**
   * Returns the relationships to infer.
   *
   * @param par the par
   * @param chd the chd
   * @return the relationships to infer
   * @throws Exception
   */
  private Set<ConceptRelationship> getRelationshipsToInfer(Concept par,
    Concept chd, OWLReasoner reasoner) throws Exception {

    Set<ConceptRelationship> inferredRels = new HashSet<>();

    // inferred parent types
    Set<String> parTypes = new HashSet<>();
    for (ConceptRelationship parRel : par.getRelationships()) {
      if (parRel.isInferred() && !parRel.isHierarchical()) {
        parTypes.add(parRel.getAdditionalRelationshipType());
      }
    }
    Set<String> anonParTypes = new HashSet<>();
    for (ConceptRelationship parRel : par.getRelationships()) {
      if (parRel.isInferred() && !parRel.isHierarchical()
          && parRel.getTo().isAnonymous()) {
        anonParTypes.add(parRel.getAdditionalRelationshipType());
      }
    }

    // stated child types
    Set<String> chdTypes = new HashSet<>();
    for (ConceptRelationship chdRel : chd.getRelationships()) {
      if (chdRel.isStated() && !chdRel.isHierarchical()) {
        chdTypes.add(chdRel.getAdditionalRelationshipType());
      }
    }
    Set<String> anonChdTypes = new HashSet<>();
    for (ConceptRelationship chdRel : chd.getRelationships()) {
      if (chdRel.isStated() && !chdRel.isHierarchical()
          && chdRel.getTo().isAnonymous()) {
        anonChdTypes.add(chdRel.getAdditionalRelationshipType());
      }
    }

    Set<ConceptRelationship> chdRels = new HashSet<>(chd.getRelationships());

    //
    // Where chd has a stated relationship to a concept
    // and par does not have corresponding inferred rel with the same type,
    // infer those
    //
    for (ConceptRelationship chdRel : chd.getRelationships()) {
      // skip if stated, anonymous, or parTypes contains the type
      if (!chdRel.isStated() || chdRel.isHierarchical()
          || parTypes.contains(chdRel.getAdditionalRelationshipType())) {
        continue;
      }
      ConceptRelationship inferredRel =
          new ConceptRelationshipJpa(chdRel, true);
      inferredRel.setId(null);
      inferredRel.setInferred(true);
      inferredRel.setStated(false);
      if (!chdRels.contains(inferredRel)) {
        inferredRels.add(inferredRel);
      }
    }

    //
    // Where par has an inferred relationship to a concept
    // and chd does not have corresponding stated rel with the same type,
    // infer those
    //
    for (ConceptRelationship parRel : par.getRelationships()) {
      // skip if stated, anonymous, or parTypes contains the type
      if (!parRel.isInferred() || parRel.isHierarchical()
          || chdTypes.contains(parRel.getAdditionalRelationshipType())) {
        continue;
      }
      ConceptRelationship inferredRel =
          new ConceptRelationshipJpa(parRel, true);
      inferredRel.setFrom(chd);
      inferredRel.setId(null);
      inferredRel.setInferred(true);
      inferredRel.setStated(false);
      if (!chdRels.contains(inferredRel)) {
        inferredRels.add(inferredRel);
      }
    }

    //
    // Where par has an inferred relationship to a non-anonymous concept
    // and chd has a stated one matching on the type, infer the child rel
    //
    for (ConceptRelationship chdRel : chd.getRelationships()) {
      // skip if stated, anonymous, or parTypes contains the type
      if (!chdRel.isStated() || chdRel.getTo().isAnonymous()
          || chdRel.isHierarchical()
          || !anonParTypes.contains(chdRel.getAdditionalRelationshipType())) {
        continue;
      }
      ConceptRelationship inferredRel =
          new ConceptRelationshipJpa(chdRel, true);
      inferredRel.setId(null);
      inferredRel.setInferred(true);
      inferredRel.setStated(false);
      if (!chdRels.contains(inferredRel)) {
        inferredRels.add(inferredRel);
      }
    }

    //
    // Where par has an inferred relationship to an anonymous concept
    // and chd has a stated one matching on the type, infer both
    // unless the anonymous concept has only a single rel, then use child one
    // only
    //
    for (ConceptRelationship chdRel : chd.getRelationships()) {
      // skip if stated, anonymous, hierarchical, or anonParTypes contains the
      // type
      if (!chdRel.isStated() || !chdRel.getTo().isAnonymous()
          || chdRel.isHierarchical()
          || !anonParTypes.contains(chdRel.getAdditionalRelationshipType())) {
        continue;
      }

      // Infer the child rel
      ConceptRelationship inferredRel =
          new ConceptRelationshipJpa(chdRel, true);
      inferredRel.setId(null);
      inferredRel.setInferred(true);
      inferredRel.setStated(false);
      if (!chdRels.contains(inferredRel)) {
        inferredRels.add(inferredRel);
        Logger.getLogger(getClass()).info("  CHECK " + chd.getTerminologyId());
      }
    }

    // Identify anonymous parent concepts not covered by chd ones.
    for (ConceptRelationship parRel : par.getRelationships()) {
      // skip if stated, anonymous, hierarchical, or type does not match
      if (!parRel.isInferred() || !parRel.getTo().isAnonymous()
          || parRel.isHierarchical()
          || !anonChdTypes.contains(parRel.getAdditionalRelationshipType())) {
        continue;
      }

      boolean found = false;
      for (ConceptRelationship chdRel : chd.getRelationships()) {
        // skip if stated, anonymous, hierarchical, or anonParTypes contains the
        // type
        if (!chdRel.isStated()
            || !chdRel.getTo().isAnonymous()
            || chdRel.isHierarchical()
            || !chdRel.getAdditionalRelationshipType().equals(
                parRel.getAdditionalRelationshipType())) {
          continue;
        }

        // Move on if the concept names (which represent the expressions) are
        // equal
        if (chdRel.getTo().getName().equals(parRel.getTo().getName())) {
          found = true;
          break;
        }

        // If the child anonymous concept is a subset of the parent one mark the
        // found flag.
        // As a proxy for this, compare the type sets and assume if they
        // match, the child is more specific.
        // TODO: ideally use reasoner to actually compare the expressions
        // NOTE: snorocket does NOT support this.
        // e.g. if
        // (reasoner.getSubClasses(parExpr).containsAll(reasoner.getSubClasses(chdExpr))
        // then mark the found flag
        // TODO: in order to do this, we need to save the Owl class expressions
        // that go with anonymous concepts

        
        Set<String> typesPar = new HashSet<>();
        for (ConceptRelationship rel : getConcept(parRel.getTo().getId())
            .getRelationships()) {
          typesPar.add(rel.getAdditionalRelationshipType());
        }
        Set<String> typesChd = new HashSet<>();
        for (ConceptRelationship rel : getConcept(chdRel.getTo().getId())
            .getRelationships()) {
          typesChd.add(rel.getAdditionalRelationshipType());
        }
        if (typesPar.equals(typesChd)) {
          found = true;
          break;
        }

      }

      // If there is not a matching relationship to an anonymous child concept
      // that is subsumed by (or equal to) this one, then infer it.
      if (!found) {
        // Infer the parent rel
        ConceptRelationship inferredRel =
            new ConceptRelationshipJpa(parRel, true);
        inferredRel.setId(null);
        inferredRel.setInferred(true);
        inferredRel.setStated(false);
        if (!chdRels.contains(inferredRel)) {
          inferredRels.add(inferredRel);
          Logger.getLogger(getClass()).info(
              "  CHECK2 " + chd.getTerminologyId());
        }
      }
    }

    //
    // TODO: handle GCI
    // - we can find all of the descendants of the LHS and create
    // corresponding relationships to the RHS
    //

    return inferredRels;
  }

  /**
   * Load release info.
   *
   * @throws Exception the exception
   */
  private void loadReleaseInfo() throws Exception {
    ReleaseInfo info = getReleaseInfo(terminology, releaseVersion);
    if (info == null) {
      info = new ReleaseInfoJpa();
      info.setName(releaseVersion);
      info.setDescription(terminology + " " + releaseVersion + " release");
      info.setPlanned(false);
      info.setPublished(true);
      info.setReleaseBeginDate(releaseVersionDate);
      info.setReleaseFinishDate(releaseVersionDate);
      info.setTerminology(terminology);
      info.setVersion(releaseVersion);
      info.setLastModified(releaseVersionDate);
      info.setLastModified(new Date());
      info.setLastModifiedBy(loader);
      addReleaseInfo(info);
    }
    commitClearBegin();

  }

  /**
   * Returns the comment.
   *
   * @param ontology the ontology
   * @return the comment
   * @throws Exception the exception
   */
  private String getComment(OWLOntology ontology) throws Exception {
    for (OWLAnnotation annotation : ontology.getAnnotations()) {
      if (annotation.getProperty().isComment()) {
        return getValue(annotation);
      }
    }

    return null;
  }

  /**
   * Returns the version.
   *
   * @param ontology the ontology
   * @return the version
   * @throws Exception the exception
   */
  private String getReleaseVersion(OWLOntology ontology) throws Exception {

    String version = null;
    if (ontology.getOntologyID().getVersionIRI() != null) {
      version = ontology.getOntologyID().getVersionIRI().toString();
    } else {
      // check versionInfo
      for (OWLAnnotation annotation : ontology.getAnnotations()) {
        if (getTerminologyId(annotation.getProperty().getIRI()).equals(
            "versionInfo")) {
          version = getValue(annotation);
        }
      }
    }
    Logger.getLogger(getClass()).info("  release version = " + version);

    // This is the list of available patterns for extracting a date.
    // Try each one
    String[] patterns =
        new String[] {
            // e.g.
            // http://snomed.info/sct/900000000000207008/version/20150131
            ".*\\/(\\d{8})$",
            // e.g.
            // http://purl.obolibrary.org/obo/go/releases/2015-07-28/go.owl
            ".*\\/(\\d\\d\\d\\d-\\d\\d-\\d\\d)$",
            ".*\\/(\\d\\d\\d\\d-\\d\\d-\\d\\d\\/)$"
        };

    // Iterate through patterns
    for (String pattern : patterns) {
      Pattern pattern2 = Pattern.compile(pattern);
      Matcher matcher = pattern2.matcher(version);
      // Assume if it matches, the pattern has a group 1, extract and
      // prepare it.
      if (matcher.matches()) {
        String parsedVersion = matcher.group(1);
        // Remove dashes
        parsedVersion = parsedVersion.replaceAll("-", "");
        Logger.getLogger(getClass())
            .info("  parsed version = " + parsedVersion);
        return parsedVersion;
      }
    }

    // else return null
    return null;
  }

  /**
   * Returns the relationships.
   *
   * @param concept the concept
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the relationships
   * @throws Exception the exception
   */
  private Set<ConceptRelationship> getRelationships(Concept concept,
    OWLClass owlClass, OWLOntology ontology) throws Exception {
    Set<ConceptRelationship> rels = new HashSet<>();

    OwlUtility.logOwlClass(owlClass, ontology, 0);

    // Handle top-level "equivalent class"
    if (ontology.getEquivalentClassesAxioms(owlClass).size() > 0) {
      Logger.getLogger(getClass()).debug("  EQUIVALENT class detected");

      OWLEquivalentClassesAxiom axiom =
          ontology.getEquivalentClassesAxioms(owlClass).iterator().next();
      for (OWLClassExpression expr : axiom.getClassExpressions()) {

        // Skip this class
        if (expr.equals(owlClass)) {
          continue;
        }

        // Any OWLClass encountered here is simply subClassOf relationship
        if (expr instanceof OWLClass) {
          OwlUtility.logOwlClass((OWLClass) expr, ontology, 1);
          Concept toConcept =
              getConcept(idMap
                  .get(getTerminologyId(((OWLClass) expr).getIRI())));
          rels.add(getSubClassOfRelationship(concept, toConcept));
        }

        // Otherwise it is an embedded class expression from which
        // we will borrow relationships
        else {
          Concept concept2 = getConceptForOwlClassExpression(expr, ontology, 1);
          for (ConceptRelationship rel : concept2.getRelationships()) {
            rel.setFrom(concept);
            rels.add(rel);
          }

          // ASSUMPTION: there is at least one rel here
          if (rels.size() == 0) {
            throw new Exception(
                "Unexpected absence of relationships from embedded class expression "
                    + expr);
          }
        }

      }

      // Presence of equivalent class signals fully defined
      concept.setFullyDefined(true);

    }

    // Handle top-level SubClassAxioms
    if (ontology.getSubClassAxiomsForSubClass(owlClass).size() > 0) {

      // Iterate through, add super classes
      for (OWLSubClassOfAxiom axiom : ontology
          .getSubClassAxiomsForSubClass(owlClass)) {

        Logger.getLogger(getClass()).debug("  subClassOfAxiom = " + axiom);

        // Handle axioms that point to an OWLClass
        if (axiom.getSuperClass() instanceof OWLClass) {
          Concept toConcept =
              getConcept(idMap.get(getTerminologyId(((OWLClass) axiom
                  .getSuperClass()).getIRI())));
          rels.add(getSubClassOfRelationship(concept, toConcept));

        }

        // Handle intersections
        else if (axiom.getSuperClass() instanceof OWLObjectIntersectionOf) {
          Concept concept2 =
              getConceptForOwlClassExpression(axiom.getSuperClass(), ontology,
                  1);
          // Wire relationships to this concept and save
          for (ConceptRelationship rel : concept2.getRelationships()) {
            rel.setFrom(concept);
            rels.add(rel);
          }

        }

        // Handle someValuesFrom
        else if (axiom.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
          Concept concept2 =
              getConceptForOwlClassExpression(axiom.getSuperClass(), ontology,
                  1);
          // Wire relationships to this concept and save
          for (ConceptRelationship rel : concept2.getRelationships()) {
            rel.setFrom(concept);
            rels.add(rel);
          }

        }

        // Otherwise error
        else {
          throw new Exception(
              "Unexpected subClassOfAxiom expression type for super class - "
                  + axiom.getSuperClass());
        }
      }
    }

    // ASSUMPTION: no duplicate relationships
    for (ConceptRelationship rel : rels) {
      for (ConceptRelationship rel2 : rels) {
        // Avoid comparing to itself
        if (rel == rel2) {
          continue;
        }
        // look for matching source/type/destination
        if (rel.getFrom().getId().equals(rel2.getFrom().getId())
            && rel.getTo().getId().equals(rel2.getTo().getId())
            && rel.getRelationshipType().equals(rel2.getRelationshipType())) {
          Logger.getLogger(getClass()).info("  rel = " + rel);
          Logger.getLogger(getClass()).info("  rel2 = " + rel2);
          throw new Exception("Unexpected duplicate rels");
        }
      }
    }
    return rels;
  }

  /**
   * Returns the sub class of relationship.
   *
   * @param fromConcept the from concept
   * @param toConcept the to concept
   * @return the sub class of relationship
   * @throws Exception the exception
   */
  private ConceptRelationship getSubClassOfRelationship(Concept fromConcept,
    Concept toConcept) throws Exception {

    // Standard "isa" relationship
    ConceptRelationship rel = new ConceptRelationshipJpa();
    setCommonFields(rel);
    // blank terminology id
    rel.setTerminologyId("");
    rel.setFrom(fromConcept);
    rel.setTo(toConcept);
    rel.setAssertedDirection(true);
    rel.setGroup(null);
    rel.setInferred(loadInferred);
    rel.setStated(!loadInferred);
    // This is an "isa" rel.
    rel.setRelationshipType("subClassOf");
    rel.setHierarchical(true);
    String subClassOfRel = getConfigurableValue(terminology, "subClassOf");
    if (subClassOfRel == null) {
      rel.setAdditionalRelationshipType("");
    } else if (relaMap.containsKey(subClassOfRel)) {
      rel.setAdditionalRelationshipType(subClassOfRel);
    } else {
      throw new Exception(
          "configurable subClassOf rel does not exist as an additionalRelationshipType: "
              + subClassOfRel);
    }
    return rel;
  }

  /**
   * Returns the preferred name.
   *
   * @param iri the iri
   * @param ontology the ontology
   * @return the preferred name
   * @throws Exception the exception
   */
  private String getPreferredName(IRI iri, OWLOntology ontology)
    throws Exception {
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(iri)) {
      OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
      if (isPreferredType(getName(annotation))) {
        return getValue(annotation);
      }
    }
    return getTerminologyId(iri);
  }

  /**
   * Helper method to extract annotation properties attached to a class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the annotation types
   * @throws Exception the exception
   */
  private Set<Atom> getAtoms(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Atom> atoms = new HashSet<>();
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
      final Atom atom = new AtomJpa();
      setCommonFields(atom);
      atom.setTerminologyId("");
      // everything after the #
      atom.setConceptId(getTerminologyId(owlClass.getIRI()));
      atom.setDescriptorId("");
      atom.setCodeId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      // this is based on xml-lang attribute on the annotation
      atom.setLanguage(getLanguage(annotation));
      languages.add(atom.getLanguage());
      atom.setTermType(atnMap.get(getName(annotation)).getAbbreviation());
      generalEntryValues.add(atom.getTermType());
      termTypes.add(atom.getTermType());
      atom.setName(getValue(annotation));
      atom.setWorkflowStatus(published);
      atoms.add(atom);
    }
    return atoms;
  }

  /**
   * Returns the definitions.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the definitions
   * @throws Exception the exception
   */
  private Set<Definition> getDefinitions(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Definition> defs = new HashSet<>();
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      if (!isDefinitionAnnotation(annotation)) {
        continue;
      }
      final Definition def = new DefinitionJpa();
      setCommonFields(def);
      def.setTerminologyId("");
      // this is based on xml-lang attribute on the annotation
      def.setValue(getValue(annotation));
      defs.add(def);
    }
    return defs;
  }

  /**
   * Returns the attributes.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return the attributes
   * @throws Exception the exception
   */
  private Set<Attribute> getAttributes(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    Set<Attribute> attributes = new HashSet<>();
    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {

      OWLAnnotation annotation = axiom.getAnnotation();
      if (isAtomAnnotation(annotation)) {
        continue;
      }
      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute);
      attribute.setTerminologyId("");
      attribute.setName(atnMap.get(getName(annotation)).getAbbreviation());
      attribute.setValue(getValue(annotation));
      generalEntryValues.add(attribute.getName());
      attributes.add(attribute);
    }
    return attributes;
  }

  /**
   * Returns the language.
   *
   * @param annotation the annotation
   * @return the language
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private String getLanguage(OWLAnnotation annotation) throws Exception {
    if (annotation.getValue() instanceof OWLLiteral) {
      return ((OWLLiteral) annotation.getValue()).getLang();
    }
    // ASSUMPTION: annotation is an OWLLiteral
    else {
      // throw new Exception("Unexpected annotation that is not OWLLiteral - " +
      // annotation);
      return "";
    }
  }

  /**
   * Returns the name.
   *
   * @param annotation the annotation
   * @return the name
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private String getValue(OWLAnnotation annotation) throws Exception {
    if (annotation.getValue() instanceof OWLLiteral) {
      return ((OWLLiteral) annotation.getValue()).getLiteral();
    }
    // ASSUMPTION: annotation is an OWLLiteral
    else {
      // throw new Exception("Unexpected annotation that is not OWLLiteral - " +
      // annotation);
      return annotation.getValue().toString();
    }
  }

  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  private void commitClearBegin() throws Exception {
    commit();
    clear();
    beginTransaction();
  }

  /**
   * Log and commit.
   * 
   * @param objectCt the object ct
   * @throws Exception the exception
   */
  private void logAndCommit(int objectCt) throws Exception {
    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info("    count = " + objectCt);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
  }

  /**
   * Load top concept.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadTopConcept(OWLOntology ontology) throws Exception {
    topConcept = new ConceptJpa();

    setCommonFields(topConcept);
    topConcept.setTerminologyId("Thing");
    topConcept.setAnonymous(false);
    topConcept.setFullyDefined(false);
    topConcept.setUsesRelationshipIntersection(true);
    topConcept.setName(getRootTerminologyPreferredName(ontology));
    topConcept.setWorkflowStatus(published);
    Atom atom = new AtomJpa();
    setCommonFields(atom);
    atom.setName(getRootTerminologyPreferredName(ontology));
    atom.setDescriptorId("");
    atom.setCodeId("");
    atom.setLexicalClassId("");
    atom.setStringClassId("");
    atom.setConceptId("Thing");
    atom.setTerminologyId("");
    atom.setLanguage("");
    atom.setTermType(label);
    atom.setWorkflowStatus(published);
    addAtom(atom);
    topConcept.addAtom(atom);
    addConcept(topConcept);
  }

  /**
   * Log ontology.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  private void loadOntology(final OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("Load ontology - " + ontology);

    OwlUtility.logOntology(ontology);

    // Load annotation properties (e.g. attribute names)
    loadAnnotationProperties(ontology);

    // Load object properties (e.g. additional relationship types)
    loadObjectProperties(ontology);

    // Load data properties (e.g. attribute names)
    loadDataProperties(ontology);

    // Load general class axioms
    loadGeneralClassAxioms(ontology);

    //
    // Iterate through all owl classes
    // Load concepts, atoms, definitions, and attributes
    //
    Logger.getLogger(getClass()).info("  Load Concepts, atoms, and attributes");
    for (OWLClass owlClass : ontology.getClassesInSignature()) {

      // If we've already encountered this class, just skip it
      if (idMap.containsKey(getTerminologyId(owlClass.getIRI()))) {
        return;
      }

      // Skip if the owl class
      if (isObsolete(owlClass, ontology)) {
        return;
      }

      // Get the concept object
      final Concept concept =
          getConceptForOwlClassExpression(owlClass, ontology, 0);

      // Persist the concept object
      for (Atom atom : concept.getAtoms()) {
        Logger.getLogger(getClass()).debug("  add atom = " + atom);
        addAtom(atom);
      }
      for (Definition def : concept.getDefinitions()) {
        Logger.getLogger(getClass()).debug("  add definition = " + def);
        addDefinition(def, concept);
      }
      for (Attribute attribute : concept.getAttributes()) {
        Logger.getLogger(getClass()).debug("  add attribute = " + attribute);
        addAttribute(attribute, concept);
      }
      Logger.getLogger(getClass()).debug("  add concept = " + concept);
      addConcept(concept);
      idMap.put(concept.getTerminologyId(), concept.getId());

      // Check whether to add a link to "top concept"
      if ("true".equals(getConfigurableValue(terminology, "top"))) {
        if (rootClassChecker.isRootClass(owlClass)) {
          ConceptRelationship rel =
              getSubClassOfRelationship(concept, topConcept);
          Logger.getLogger(getClass()).info("  add top relationship = " + rel);
          addRelationship(rel);
          concept.addRelationship(rel);
        }
      }

      logAndCommit(++objectCt);

    }
    commitClearBegin();

    //
    // Iterate through classes again and connect relationships
    //
    Logger.getLogger(getClass()).info("  Load relationships");
    Set<String> visited = new HashSet<>();
    for (OWLClass owlClass : ontology.getClassesInSignature()) {

      final String terminologyId = getTerminologyId(owlClass.getIRI());
      if (visited.contains(terminologyId)) {
        return;
      }
      visited.add(terminologyId);

      // Skip if the owl class
      if (isObsolete(owlClass, ontology)) {
        return;
      }

      final Concept concept = getConcept(idMap.get(terminologyId));
      // ASSUMPTION: concept exists
      if (concept == null) {
        throw new Exception("Unexpected missing concept for " + terminologyId);
      }

      for (final ConceptRelationship rel : getRelationships(concept, owlClass,
          ontology)) {
        // ASSUMPTION: embedded anonymous concepts have been added
        Logger.getLogger(getClass()).debug("  add relationship = " + rel);
        addRelationship(rel);
        concept.addRelationship(rel);
      }
      // Update the concept a
      Logger.getLogger(getClass()).debug("  update concept = " + concept);
      updateConcept(concept);
      logAndCommit(++objectCt);

    }

    loadDisjointSets();
    commitClearBegin();

  }

  /**
   * Indicates whether or not obsolete is the case.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isObsolete(OWLClass owlClass, OWLOntology ontology)
    throws Exception {
    String obsoletePattern =
        getConfigurableValue(terminology, "obsoletePattern");
    String obsoleteAnnotation =
        getConfigurableValue(terminology, "obsoleteAnnotation");
    if (obsoletePattern == null || obsoleteAnnotation == null) {
      return false;
    }

    for (OWLAnnotationAssertionAxiom axiom : ontology
        .getAnnotationAssertionAxioms(owlClass.getIRI())) {
      OWLAnnotation annotation = axiom.getAnnotation();
      if (!isAtomAnnotation(annotation)) {
        continue;
      }
      // Look for a label matching the pattern
      if (getName(annotation).equals(label)
          && getValue(annotation).matches(obsoletePattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Load object properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadObjectProperties(OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("  Loading object properties");
    Map<String, String> inverses = new HashMap<>();
    Map<String, String> equiv = new HashMap<>();
    Map<String, String> parChd = new HashMap<>();
    // Add object properties
    for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()) {
      OwlUtility.logObjectProperty(prop, ontology);

      final AdditionalRelationshipType rela =
          new AdditionalRelationshipTypeJpa();
      setCommonFields(rela);

      rela.setAbbreviation(getPreferredName(prop.getIRI(), ontology));
      rela.setAsymmetric(ontology.getAsymmetricObjectPropertyAxioms(prop)
          .size() != 0);
      // domain
      if (ontology.getObjectPropertyDomainAxioms(prop).size() == 1) {
        OWLObjectPropertyDomainAxiom axiom =
            ontology.getObjectPropertyDomainAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getDomain() instanceof OWLClass) {
          rela.setDomainId(getTerminologyId(((OWLClass) axiom.getDomain())
              .getIRI()));
        }
        // ASSUMPTION: object property domain is an OWLClass
        else {
          throw new Exception("Unexpected domain type, not class");
        }
      }
      // ASSUMPTION: object property has only one domain axiom
      else if (ontology.getObjectPropertyDomainAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one domain axiom");
      }

      // range
      if (ontology.getObjectPropertyRangeAxioms(prop).size() == 1) {
        OWLObjectPropertyRangeAxiom axiom =
            ontology.getObjectPropertyRangeAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getRange() instanceof OWLClass) {
          rela.setRangeId(getTerminologyId(((OWLClass) axiom.getRange())
              .getIRI()));
        }
        // ASSUMPTION: object property rangeis an OWLClass
        else {
          throw new Exception("Unexpected range type, not class");
        }
      }
      // ASSUMPTION: object property has only one range axiom
      else if (ontology.getObjectPropertyRangeAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one range axiom");
      }

      rela.setEquivalentClasses(false);

      // e.g. "someValuesFrom"
      rela.setExistentialQuantification(true);

      // This applies to relationship group style
      rela.setGroupingType(false);

      rela.setExpandedForm(prop.getIRI().toString());

      // TODO: reenable for Owl DL features
      // NOT in OWL EL 2 - only for data properties
      // rela.setFunctional(ontology.getFunctionalObjectPropertyAxioms(prop)
      // .size() != 0);
      // NOT in OWL EL 2
      // rela.setInverseFunctional(ontology
      // .getInverseFunctionalObjectPropertyAxioms(prop).size() != 0);

      // NOT in OWL EL 2
      // rela.setIrreflexive(ontology.getIrreflexiveObjectPropertyAxioms(prop)
      // .size() != 0);
      rela.setReflexive(ontology.getReflexiveObjectPropertyAxioms(prop).size() != 0);

      Logger.getLogger(getClass()).debug(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // ASSUMPTION: object property has no annotations
      // Only works in OWLAPI 4
      // if (prop.getAnnotationPropertiesInSignature().size() > 0) {
      // throw new Exception(
      // "Unexpected annotation properties on OWLObjectProperty.");
      // }
      // ASSUMPTION: object property has no classes in signature
      if (prop.getClassesInSignature().size() > 0) {
        throw new Exception("Unexpected classes on OWLObjectProperty.");
      }
      // ASSUMPTION: object property has no data properties
      if (prop.getDataPropertiesInSignature().size() > 0) {
        throw new Exception("Unexpected data properties on OWLObjectProperty.");
      }
      // ASSUMPTION: object property has no nested class expressions
      if (prop.getNestedClassExpressions().size() > 0) {
        throw new Exception(
            "Unexpected nested class expressions on OWLObjectProperty.");
      }

      // inverse
      if (ontology.getInverseObjectPropertyAxioms(prop).size() == 1) {
        OWLInverseObjectPropertiesAxiom axiom =
            ontology.getInverseObjectPropertyAxioms(prop).iterator().next();
        OWLObjectProperty iprop = axiom.getSecondProperty().getNamedProperty();
        inverses.put(rela.getAbbreviation(), getTerminologyId(iprop.getIRI()));

      }
      // equivalent
      if (ontology.getEquivalentObjectPropertiesAxioms(prop).size() == 1) {
        OWLEquivalentObjectPropertiesAxiom axiom =
            ontology.getEquivalentObjectPropertiesAxioms(prop).iterator()
                .next();
        for (OWLObjectPropertyExpression prop2 : axiom.getProperties()) {
          String abbr = getTerminologyId(prop2.getNamedProperty().getIRI());
          // Skip this property
          if (abbr.equals(rela.getAbbreviation())) {
            continue;
          }
          // ASSUMPTION - only one equivalent property
          // If need be, support equivalences as a set
          if (equiv.containsKey(rela.getAbbreviation())) {
            throw new Exception(
                "Unexpected multiple equivalent properties for " + rela);
          }
          equiv.put(rela.getAbbreviation(), abbr);
        }
      }
      // ASSUMPTION: object property has at most one inverse
      else if (ontology.getInverseObjectPropertyAxioms(prop).size() > 1) {
        throw new Exception(
            "Unexpected more than one inverse object property axiom");
      }

      // par/chd
      if (ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size() == 1) {
        OWLSubObjectPropertyOfAxiom axiom =
            ontology.getObjectSubPropertyAxiomsForSubProperty(prop).iterator()
                .next();
        OWLObjectProperty superProp =
            axiom.getSuperProperty().getNamedProperty();
        parChd
            .put(getTerminologyId(superProp.getIRI()), rela.getAbbreviation());
      }
      // ASSUMPTION: object property has at most one super property
      else if (ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size() > 1) {
        throw new Exception("Unexpected more than one super property");
      }

      // Add rela
      Logger.getLogger(getClass()).debug("  add rela - " + rela);
      addAdditionalRelationshipType(rela);
      relaMap.put(rela.getAbbreviation(), rela);
      relaMap.put(getTerminologyId(prop.getIRI()), rela);
    }

    commitClearBegin();

    // Iterate through inverses, set and update
    for (String key : inverses.keySet()) {
      AdditionalRelationshipType type1 = relaMap.get(key);
      AdditionalRelationshipType type2 = relaMap.get(inverses.get(key));
      type1.setInverseType(type2);
      type2.setInverseType(type1);
      updateAdditionalRelationshipType(type1);
      updateAdditionalRelationshipType(type2);
    }
    commitClearBegin();

    // Iterate through parChd properties, set and update
    for (String key : parChd.keySet()) {
      AdditionalRelationshipType par = relaMap.get(key);
      AdditionalRelationshipType chd = relaMap.get(parChd.get(key));
      chd.setSuperType(par);
      updateAdditionalRelationshipType(chd);
    }

    // Iterate through equiv properties, set and update
    for (String key : equiv.keySet()) {
      AdditionalRelationshipType rela1 = relaMap.get(key);
      AdditionalRelationshipType rela2 = relaMap.get(equiv.get(key));
      rela1.setEquivalentType(rela2);
      rela2.setEquivalentType(rela1);
      updateAdditionalRelationshipType(rela1);
      updateAdditionalRelationshipType(rela2);
    }

    // Add property chains
    // Only way I could find to access property chains
    for (OWLSubPropertyChainOfAxiom prop : ontology.getAxioms(
        AxiomType.SUB_PROPERTY_CHAIN_OF, false)) {
      OwlUtility.logPropertyChain(prop, ontology);

      String superProp =
          getTerminologyId(prop.getSuperProperty().getNamedProperty().getIRI());
      List<String> links = new ArrayList<>();
      List<AdditionalRelationshipType> types = new ArrayList<>();
      for (OWLObjectPropertyExpression link : prop.getPropertyChain()) {
        String name = getTerminologyId(link.getNamedProperty().getIRI());
        links.add(name);
        types.add(relaMap.get(name));
      }

      PropertyChain chain = new PropertyChainJpa();
      setCommonFields(chain);
      StringBuilder abbreviation = new StringBuilder();
      for (String link : links) {
        abbreviation.append(link).append(" o ");
      }
      chain.setAbbreviation(abbreviation.toString().replaceAll(" o $", " => ")
          + superProp);
      chain.setChain(types);
      chain.setExpandedForm(chain.getAbbreviation());
      chain.setResult(relaMap.get(superProp));

      Logger.getLogger(getClass()).debug("  add property chain - " + chain);
      addPropertyChain(chain);

    }
    commitClearBegin();
  }

  /**
   * Load annotation properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadAnnotationProperties(OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("  Loading annotation properties");
    for (OWLAnnotationProperty prop : ontology
        .getAnnotationPropertiesInSignature()) {

      OwlUtility.logAnnotationProperty(prop, ontology);

      final AttributeName atn = new AttributeNameJpa();
      setCommonFields(atn);
      atn.setAbbreviation(getPreferredName(prop.getIRI(), ontology));
      atn.setAnnotation(true);
      atn.setExistentialQuantification(true);
      // NOT in OWL EL 2
      atn.setUniversalQuantification(false);
      atn.setExpandedForm(prop.getIRI().toString());
      Logger.getLogger(getClass()).debug(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // Add rela
      Logger.getLogger(getClass()).debug("  add atn - " + atn);
      addAttributeName(atn);
      atnMap.put(atn.getAbbreviation(), atn);
      atnMap.put(getTerminologyId(prop.getIRI()), atn);
    }

    commitClearBegin();

  }

  /**
   * Load data properties.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadDataProperties(OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("  Loading data properties");
    Map<String, String> equiv = new HashMap<>();
    Map<String, String> parChd = new HashMap<>();

    for (OWLDataProperty prop : ontology.getDataPropertiesInSignature()) {
      OwlUtility.logDataProperty(prop, ontology);

      final AttributeName atn = new AttributeNameJpa();
      setCommonFields(atn);
      atn.setAbbreviation(getPreferredName(prop.getIRI(), ontology));

      // domain
      if (ontology.getDataPropertyDomainAxioms(prop).size() == 1) {
        OWLDataPropertyDomainAxiom axiom =
            ontology.getDataPropertyDomainAxioms(prop).iterator().next();
        // Expect a class with an IRI
        if (axiom.getDomain() instanceof OWLClass) {
          atn.setDomainId(getTerminologyId(((OWLClass) axiom.getDomain())
              .getIRI()));
        }
        // ASSUMPTION: data property domain is not an OWLClass
        else {
          throw new Exception("Unexpected domain type, not class");
        }
      }
      // ASSUMPTION: data property has at most one domains
      else if (ontology.getDataPropertyDomainAxioms(prop).size() > 1) {
        throw new Exception("Unexpectedly more than one domain axiom");
      }

      // equivalent
      if (ontology.getEquivalentDataPropertiesAxioms(prop).size() == 1) {
        OWLEquivalentDataPropertiesAxiom axiom =
            ontology.getEquivalentDataPropertiesAxioms(prop).iterator().next();
        for (OWLDataPropertyExpression prop2 : axiom.getProperties()) {
          String abbr = getTerminologyId(prop2.asOWLDataProperty().getIRI());
          // Skip this property
          if (abbr.equals(atn.getAbbreviation())) {
            continue;
          }
          // ASSUMPTION - only one equivalent property
          // If need be, support equivalences as a set
          if (equiv.containsKey(atn.getAbbreviation())) {
            throw new Exception(
                "Unexpected multiple equivalent properties for " + atn);
          }
          equiv.put(atn.getAbbreviation(), abbr);
        }
      }

      atn.setExistentialQuantification(true);
      // NOT in OWL EL 2
      atn.setUniversalQuantification(false);
      atn.setExpandedForm(prop.getIRI().toString());
      atn.setFunctional(ontology.getFunctionalDataPropertyAxioms(prop).size() != 0);

      Logger.getLogger(getClass()).debug(
          "    terminologyId = " + getTerminologyId(prop.getIRI()));

      // par/chd
      if (ontology.getDataSubPropertyAxiomsForSubProperty(prop).size() == 1) {
        OWLSubDataPropertyOfAxiom axiom =
            ontology.getDataSubPropertyAxiomsForSubProperty(prop).iterator()
                .next();
        OWLDataProperty superProp =
            axiom.getSuperProperty().asOWLDataProperty();
        parChd.put(getTerminologyId(superProp.getIRI()), atn.getAbbreviation());
      }
      // ASSUMPTION: data property has at most one super property
      else if (ontology.getDataSubPropertyAxiomsForSubProperty(prop).size() > 1) {
        throw new Exception("Unexpected more than one super property");
      }

      // Add rela
      Logger.getLogger(getClass()).debug("  add atns - " + atn);
      addAttributeName(atn);
      atnMap.put(atn.getAbbreviation(), atn);
      atnMap.put(getTerminologyId(prop.getIRI()), atn);
    }

    commitClearBegin();

    // PAR/CHD
    for (String key : parChd.keySet()) {
      AttributeName par = atnMap.get(key);
      AttributeName chd = atnMap.get(parChd.get(key));
      chd.setSuperName(par);
      updateAttributeName(chd);
    }
    // equiv
    for (String key : equiv.keySet()) {
      AttributeName atn1 = atnMap.get(key);
      AttributeName atn2 = atnMap.get(equiv.get(key));
      atn1.setEquivalentName(atn2);
      atn2.setEquivalentName(atn1);
      updateAttributeName(atn2);
      updateAttributeName(atn1);
    }
    commitClearBegin();

  }

  /**
   * Load general class axioms.
   *
   * @param ontology the ontology
   * @throws Exception the exception
   */
  public void loadGeneralClassAxioms(OWLOntology ontology) throws Exception {
    Logger.getLogger(getClass()).info("  Loading general class axioms");

    for (OWLClassAxiom axiom : ontology.getGeneralClassAxioms()) {

      if (axiom instanceof OWLDisjointClassesAxiom) {
        // Create disjointness
        // TODO:
        Logger.getLogger(getClass()).info("  DISJOINT CLASSES AXIOM: " + axiom);
        throw new Exception("Not handled yet, needs impl");

      } else if (axiom instanceof OWLSubClassOfAxiom) {

        Logger.getLogger(getClass()).info("  SUB CLASS AXIOM: " + axiom);
        OWLSubClassOfAxiom axiom2 = (OWLSubClassOfAxiom) axiom;
        if (axiom2.getSuperClass() instanceof OWLClass) {
          throw new Exception(
              "Unexpectedly encountered a simple OWLClass in a general subclass axiom - "
                  + axiom2.getSuperClass());
        }
        if (axiom2.getSubClass() instanceof OWLClass) {
          throw new Exception(
              "Unexpectedly encountered a simple OWLClass in a general subclass axiom - "
                  + axiom2.getSuperClass());
        }

        Concept concept1 =
            getConceptForOwlClassExpression(axiom2.getSuperClass(), ontology, 1);
        Concept concept2 =
            getConceptForOwlClassExpression(axiom2.getSubClass(), ontology, 1);
        // Reuse if they exist already, otherwise add
        if (idMap.containsKey(concept1.getTerminologyId())) {
          concept1 = getConcept(idMap.get(concept1.getTerminologyId()));
        } else {
          addAnonymousConcept(concept1);
          anonymousExprMap.put(concept1.getTerminologyId(),
              axiom2.getSuperClass());
        }
        if (idMap.containsKey(concept2.getTerminologyId())) {
          concept2 = getConcept(idMap.get(concept2.getTerminologyId()));
        } else {
          addAnonymousConcept(concept2);
          anonymousExprMap.put(concept2.getTerminologyId(),
              axiom2.getSubClass());
        }
        GeneralConceptAxiom gca = new GeneralConceptAxiomJpa();
        setCommonFields(gca);
        gca.setEquivalent(false);
        gca.setSubClass(true);
        gca.setLeftHandSide(concept1);
        gca.setRightHandSide(concept2);
        Logger.getLogger(getClass()).info("  add general class axiom - " + gca);
        addGeneralConceptAxiom(gca);

      } else if (axiom instanceof OWLEquivalentClassesAxiom) {
        Logger.getLogger(getClass()).info(
            "  EQUIVALENT CLASSES AXIOM: " + axiom);
        OWLEquivalentClassesAxiom axiom2 = (OWLEquivalentClassesAxiom) axiom;
        // Each of the class expressions is equivalent,
        // create pairwise "general class axioms" from them
        for (OWLClassExpression expr : axiom2.getClassExpressions()) {
          for (OWLClassExpression expr2 : axiom2.getClassExpressions()) {
            // Get concepts
            Concept concept1 =
                getConceptForOwlClassExpression(expr, ontology, 1);
            Concept concept2 =
                getConceptForOwlClassExpression(expr2, ontology, 1);
            // Only do in one direction
            if (concept1.getTerminologyId().compareTo(
                concept2.getTerminologyId()) >= 0) {
              continue;
            }
            // Reuse if they exist already, otherwise add
            if (idMap.containsKey(concept1.getTerminologyId())) {
              concept1 = getConcept(idMap.get(concept1.getTerminologyId()));
            } else {
              addAnonymousConcept(concept1);
              anonymousExprMap.put(concept1.getTerminologyId(), expr);
            }
            if (idMap.containsKey(concept2.getTerminologyId())) {
              concept2 = getConcept(idMap.get(concept2.getTerminologyId()));
            } else {
              addAnonymousConcept(concept2);
              anonymousExprMap.put(concept2.getTerminologyId(), expr2);
            }
            GeneralConceptAxiom gca = new GeneralConceptAxiomJpa();
            setCommonFields(gca);
            gca.setEquivalent(true);
            gca.setSubClass(false);
            gca.setLeftHandSide(concept1);
            gca.setRightHandSide(concept2);
            Logger.getLogger(getClass()).info(
                "  add general class axiom - " + gca);
            addGeneralConceptAxiom(gca);
          }
        }

      } else {
        throw new Exception("Unexpected general class axiom type: " + axiom);
      }
    }
    commitClearBegin();

  }

  /**
   * Load disjoint sets.
   *
   * @throws Exception the exception
   */
  private void loadDisjointSets() throws Exception {
    Logger.getLogger(getClass()).debug("  Load disjoint subsets");

    // Iterate through disjoint Map
    // Create a subset, wire all subset members, etc.
    int ct = 1;
    for (String key : disjointMap.keySet()) {
      ConceptSubset subset = new ConceptSubsetJpa();
      setCommonFields(subset);
      subset.setTerminologyId("");
      subset.setDisjointSubset(true);
      subset.setLabelSubset(false);
      subset.setName(terminology + " disjoint subset " + ct++);
      subset.setDescription("Collection of disjoint concepts from "
          + terminology);
      Logger.getLogger(getClass()).debug("    subset = " + subset);
      addSubset(subset);
      commitClearBegin();

      for (String id : disjointMap.get(key)) {
        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        setCommonFields(member);
        member.setTerminologyId("");
        member.setMember(getConcept(idMap.get(id)));
        member.setSubset(subset);
        Logger.getLogger(getClass()).debug("  add member = " + member);
        addSubsetMember(member);
        member.getMember().addMember(member);
        updateConcept(member.getMember());
        subset.addMember(member);
      }
      // Update the subset
      updateSubset(subset);
      commitClearBegin();
      Logger.getLogger(getClass()).debug(
          "      count = " + subset.getMembers().size());
    }
  }

  /**
   * Load owl class.
   *
   * @param expr the owl class
   * @param ontology the ontology
   * @param level the level
   * @return the concept
   * @throws Exception the exception
   */
  private Concept getConceptForOwlClassExpression(OWLClassExpression expr,
    OWLOntology ontology, int level) throws Exception {

    // Log it
    if (expr instanceof OWLClass) {
      OwlUtility.logOwlClass((OWLClass) expr, ontology, level);
    } else {
      OwlUtility.logOwlClassExpression(expr, ontology, level);
    }

    // Handle direct OWLClass
    if (expr instanceof OWLClass) {
      return getConceptForOwlClass((OWLClass) expr, ontology, level);
    }

    // Handle ObjectIntersectionOf
    else if (expr instanceof OWLObjectIntersectionOf) {
      return getConceptForIntersectionOf((OWLObjectIntersectionOf) expr,
          ontology, level);
    }

    // Handle ObjectSomeValuesFrom
    else if (expr instanceof OWLObjectSomeValuesFrom) {
      return getConceptForSomeValuesFrom((OWLObjectSomeValuesFrom) expr,
          ontology, level);

    }

    else {
      throw new Exception("Unexpected class expression type - "
          + expr.getClassExpressionType());
    }

  }

  /**
   * Returns the concept for owl class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @param level the level
   * @return the concept for owl class
   * @throws Exception the exception
   */
  private Concept getConceptForOwlClass(OWLClass owlClass,
    OWLOntology ontology, int level) throws Exception {

    // If class already exists, simply return it.
    if (idMap.containsKey(getTerminologyId(owlClass.getIRI()))) {
      return getConcept(idMap.get(getTerminologyId(owlClass.getIRI())));
    }

    Concept concept = new ConceptJpa();

    // Standard settings
    setCommonFields(concept);
    concept.setWorkflowStatus(published);

    // Currently only OWL EL 2 is supported - no union
    concept.setUsesRelationshipUnion(false);
    concept.setUsesRelationshipIntersection(true);

    // Set fully defined
    if (ontology.getEquivalentClassesAxioms(owlClass).size() > 0) {
      // ASSUMPTION: only one equivalent class statement
      if (ontology.getEquivalentClassesAxioms(owlClass).size() > 1) {
        throw new Exception(
            "Unexpected more than one equivalent class axiom for " + owlClass);
      }
      concept.setFullyDefined(true);
    } else {
      concept.setFullyDefined(false);
    }

    // Set anonymous flag and identifier
    if (owlClass.isAnonymous()) {
      concept.setAnonymous(true);
      String uuid = TerminologyUtility.getUuid(owlClass.toString()).toString();
      // Check for an already existing, matching anonymous class
      if (idMap.containsKey(uuid)) {
        concept = getConcept(idMap.get(uuid));
      } else {
        concept.setTerminologyId(uuid);
      }
      Logger.getLogger(getClass()).debug(
          "  anonymous class = " + uuid + ", " + concept);

    } else {
      concept.setAnonymous(owlClass.isAnonymous());
      concept.setFullyDefined(false);
      concept.setTerminologyId(getTerminologyId(owlClass.getIRI()));
    }

    //
    // Lookup and create atoms (from annotations)
    //
    final Set<Atom> atoms = getAtoms(owlClass, ontology);
    boolean flag = true;
    for (Atom atom : atoms) {
      // Use first RDFS label as the preferred name
      if (flag && isPreferredType(atom.getTermType())) {
        concept.setName(atom.getName());
        flag = false;
      }
      concept.addAtom(atom);
    }

    //
    // Lookup and create definitions (from annotations)
    //
    final Set<Definition> defs = getDefinitions(owlClass, ontology);
    for (Definition def : defs) {
      concept.addDefinition(def);
    }

    //
    // Lookup and create attributes (from annotations)
    //
    final Set<Attribute> attributes = getAttributes(owlClass, ontology);
    for (Attribute attribute : attributes) {
      concept.addAttribute(attribute);

    }

    //
    // Handle disjoint classes
    //
    for (OWLDisjointClassesAxiom axiom : ontology
        .getDisjointClassesAxioms(owlClass)) {

      Set<String> disjointSet = new HashSet<>();
      for (OWLClassExpression expr : axiom.getClassExpressions()) {
        if (expr instanceof OWLClass) {
          final String disjointId =
              getTerminologyId(((OWLClass) expr).getIRI());
          if (!concept.getTerminologyId().equals(disjointId)) {
            disjointSet.add(disjointId);
          }
        } else {
          throw new Exception(
              "Unexpected disjoint classes axiom that is not an OWLClass - "
                  + expr);
        }
      }
      // If this disjoint set overlaps with another one, add all
      // this way at the end we have fully computed sets
      boolean disjointFlag = false;
      for (Set<String> set : disjointMap.values()) {
        for (String id : disjointSet) {
          if (set.contains(id)) {
            disjointFlag = true;
            break;
          }
        }
        if (disjointFlag) {
          set.addAll(disjointSet);
          break;
        }
      }
      if (!disjointFlag) {
        disjointMap.put(concept.getTerminologyId(), disjointSet);
      }
    }

    return concept;
  }

  /**
   * Returns the concept for intersection.
   *
   * @param expr the expr
   * @param ontology the ontology
   * @param level the level
   * @return the concept for intersection
   * @throws Exception the exception
   */
  private Concept getConceptForIntersectionOf(OWLObjectIntersectionOf expr,
    OWLOntology ontology, int level) throws Exception {
    String uuid = TerminologyUtility.getUuid(expr.toString()).toString();

    if (idMap.containsKey(uuid)) {
      return getConcept(idMap.get(uuid));
    }

    Concept concept = new ConceptJpa();
    setCommonFields(concept);
    concept.setAnonymous(true);
    concept.setTerminologyId(uuid);
    concept.setName(expr.toString());

    // Handle nested class expressions
    if (expr.getOperands().size() > 1) {

      // Iterate through expressions and either add a parent relationship
      // or add relationships from the concept itself. No new anonymous
      // concepts are directly created here.
      for (OWLClassExpression expr2 : expr.getOperands()) {
        final Concept concept2 =
            getConceptForOwlClassExpression(expr2, ontology, level + 1);
        // If it's a restriction, borrow its relationships
        if (expr2 instanceof OWLObjectSomeValuesFrom) {
          for (ConceptRelationship rel : concept2.getRelationships()) {
            // rewire the "from" concept to this one and add the rel
            rel.setFrom(concept);
            concept.addRelationship(rel);
          }
        }
        // otherwise, simply add this concept as a parent
        else if (expr2 instanceof OWLClass) {
          ConceptRelationship rel =
              getSubClassOfRelationship(concept, concept2);
          concept.addRelationship(rel);
        }
        // otherwise, unknown type
        else {
          throw new Exception("Unexpected operand expression type - " + expr2);
        }
      }

      return concept;

    }

    // ASSUMPTION: intersection has at least two sub-expressions
    else {
      throw new Exception(
          "Unexpected number of intersection nested class expressions - "
              + expr);
    }

  }

  /**
   * Adds the anonymous concept.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  private void addAnonymousConcept(Concept concept) throws Exception {
    if (concept.isAnonymous() && !idMap.containsKey(concept.getTerminologyId())) {
      Atom atom = new AtomJpa();
      setCommonFields(atom);
      atom.setTerminologyId("");
      atom.setTermType(label);
      atom.setConceptId(concept.getTerminologyId());
      atom.setCodeId("");
      atom.setDescriptorId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      atom.setLanguage("");
      atom.setPublishable(false);
      atom.setPublished(false);
      atom.setName(concept.getName());
      Logger.getLogger(getClass()).debug("  add atom - " + atom);
      addAtom(atom);
      concept.addAtom(atom);

      Logger.getLogger(getClass()).debug("  add concept - " + concept);
      addConcept(concept);
      idMap.put(concept.getTerminologyId(), concept.getId());

      // ASSUMPTION - the concept has no atoms or attributes
      if (concept.getAtoms().size() > 1 || concept.getAttributes().size() > 0) {
        Logger.getLogger(getClass()).error("  atoms = " + concept.getAtoms());
        Logger.getLogger(getClass()).error(
            "  attributes = " + concept.getAttributes());
        throw new Exception(
            "Unexpected anonymous concept with atoms or attributes ");
      }

      for (ConceptRelationship rel : concept.getRelationships()) {
        Logger.getLogger(getClass()).debug("  add relationship - " + rel);
        addRelationship(rel);
      }
    }
  }

  /**
   * Returns the concept for some values from.
   *
   * @param expr the expr
   * @param ontology the ontology
   * @param level the level
   * @return the concept for some values from
   * @throws Exception the exception
   */
  private Concept getConceptForSomeValuesFrom(OWLObjectSomeValuesFrom expr,
    OWLOntology ontology, int level) throws Exception {
    String uuid = TerminologyUtility.getUuid(expr.toString()).toString();

    if (idMap.containsKey(uuid)) {
      return getConcept(idMap.get(uuid));
    }

    Concept concept = new ConceptJpa();
    setCommonFields(concept);
    concept.setAnonymous(true);
    concept.setTerminologyId(uuid);
    concept.setName(expr.toString());

    // This is a restriction on a property with existential quantification.
    // It is a relationship to either a class or an anonymous class

    // Get the target concept
    Concept concept2 =
        getConceptForOwlClassExpression(expr.getFiller(), ontology, level + 1);
    // Add if anonymous and doesn't exist yet
    if (concept2.isAnonymous()
        && !idMap.containsKey(concept2.getTerminologyId())) {
      addAnonymousConcept(concept2);
      anonymousExprMap.put(concept2.getTerminologyId(), expr);
    }

    // Get the property and create a relationship
    OWLObjectProperty property = (OWLObjectProperty) expr.getProperty();
    ConceptRelationship rel = new ConceptRelationshipJpa();
    setCommonFields(rel);
    rel.setTerminologyId("");
    rel.setRelationshipType("other");
    rel.setAdditionalRelationshipType(relaMap.get(
        getTerminologyId(property.getIRI())).getAbbreviation());
    rel.setFrom(concept);
    rel.setTo(concept2);
    rel.setAssertedDirection(true);
    rel.setInferred(loadInferred);
    rel.setStated(!loadInferred);
    concept.addRelationship(rel);

    return concept;
  }

  /**
   * Indicates whether or not atom annotation is the case.
   *
   * @param annotation the annotation
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isAtomAnnotation(OWLAnnotation annotation) throws Exception {
    String name = getName(annotation);
    if (name.equals(label)) {
      return true;
    }

    String atomAnnotations =
        getConfigurableValue(terminology, "atomAnnotations");
    if (atomAnnotations != null) {
      for (String field : FieldedStringTokenizer.split(atomAnnotations, ",")) {
        if (name.equals(field)) {
          return true;
        }
      }
    } else {
      Logger.getLogger(getClass()).warn(
          "  NO atom annotations are specifically declared, "
              + "just using rdfs:label and rdfs:comment");
    }

    return false;
  }

  /**
   * Indicates whether or not definition annotation is the case.
   *
   * @param annotation the annotation
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isDefinitionAnnotation(OWLAnnotation annotation)
    throws Exception {
    String name = getName(annotation);
    String atomAnnotations =
        getConfigurableValue(terminology, "definitionAnnotations");
    if (atomAnnotations != null) {
      for (String field : FieldedStringTokenizer.split(atomAnnotations, ",")) {
        if (name.equals(field)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the term type.
   *
   * @param annotation the annotation
   * @return the term type
   */
  private String getName(OWLAnnotation annotation) {
    return getTerminologyId(annotation.getProperty().getIRI());
  }

  /**
   * Is preferred type.
   *
   * @param tty the tty
   * @return the string
   * @throws Exception the exception
   */
  private boolean isPreferredType(String tty) throws Exception {

    if (tty.equals(getConfigurableValue(terminology, "preferredType"))) {
      return true;
    }

    // Don't look further if the configurable type is set
    if (getConfigurableValue(terminology, "preferredType") != null) {
      return false;
    }

    if (tty.equals(label)) {
      return true;
    }

    return false;
  }

  /**
   * Returns the terminology id.
   *
   * @param iri the iri
   * @return the terminology id
   */
  @SuppressWarnings("static-method")
  private String getTerminologyId(IRI iri) {
    // TODO: we probably need to save information about the parts of the URL we
    // are stripping
    if (iri.toString().contains("#")) {
      // everything after the last #
      return iri.toString().substring(iri.toString().lastIndexOf("#") + 1);
    } else if (iri.toString().contains("/")) {
      // everything after the last slash
      return iri.toString().substring(iri.toString().lastIndexOf("/") + 1);
    }
    // otherwise, just return the iri
    return iri.toString();
  }

  /**
   * Sets the common fields.
   *
   * @param component the common fields
   */
  private void setCommonFields(Component component) {
    component.setTerminology(terminology);
    component.setVersion(version);
    component.setTimestamp(releaseVersionDate);
    component.setLastModified(releaseVersionDate);
    component.setLastModifiedBy(loader);
    component.setPublishable(true);
    component.setPublished(true);
    component.setObsolete(false);
    component.setSuppressible(false);
  }

  /**
   * Sets the common fields.
   *
   * @param abbreviation the common fields
   */
  private void setCommonFields(Abbreviation abbreviation) {
    abbreviation.setTerminology(terminology);
    abbreviation.setVersion(version);
    abbreviation.setTimestamp(releaseVersionDate);
    abbreviation.setLastModified(releaseVersionDate);
    abbreviation.setLastModifiedBy(loader);
    abbreviation.setPublishable(true);
    abbreviation.setPublished(true);

  }

  /**
   * Returns the configurable value.
   *
   * @param terminology the terminology
   * @param key the key
   * @return the configurable value
   * @throws Exception the exception
   */
  private String getConfigurableValue(String terminology, String key)
    throws Exception {
    Properties p = ConfigUtility.getConfigProperties();
    String fullKey = getClass().getName() + "." + terminology + "." + key;
    if (p.containsKey(fullKey)) {
      return p.getProperty(fullKey);
    }
    return null;
  }

}
