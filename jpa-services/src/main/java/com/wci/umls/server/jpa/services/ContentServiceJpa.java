/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ComponentInfoList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.NoteList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DefinitionList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.GeneralConceptAxiomList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.jpa.content.AbstractAtomClass;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeNoteJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptNoteJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorNoteJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.ComponentInfoListJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.AtomListJpa;
import com.wci.umls.server.jpa.helpers.content.AttributeListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DefinitionListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.GeneralConceptAxiomListJpa;
import com.wci.umls.server.jpa.helpers.content.LexicalClassListJpa;
import com.wci.umls.server.jpa.helpers.content.MapSetListJpa;
import com.wci.umls.server.jpa.helpers.content.MappingListJpa;
import com.wci.umls.server.jpa.helpers.content.NoteListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.StringClassListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.jpa.services.handlers.EclExpressionHandler;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentHasDefinitions;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.GeneralConceptAxiom;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.ExpressionHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;
import com.wci.umls.server.services.handlers.SearchHandler;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * JPA and JAXB enabled implementation of {@link ContentService}.
 */
public class ContentServiceJpa extends MetadataServiceJpa implements
    ContentService {

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = true;

  /** The id assignment handler . */
  static Map<String, IdentifierAssignmentHandler> idHandlerMap =
      new HashMap<>();

  /** The search handlers. */
  static Map<String, SearchHandler> searchHandlers = new HashMap<>();

  /** The query timeout. */
  static int queryTimeout = 1000;

  static {

    try {
      if (ConfigUtility.getConfigProperties().containsKey(
          "javax.persistence.query.timeout")) {
        queryTimeout =
            Integer.parseInt(ConfigUtility.getConfigProperties().getProperty(
                "javax.persistence.query.timeout"));
      }

      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "identifier.assignment.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        final IdentifierAssignmentHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, IdentifierAssignmentHandler.class);
        idHandlerMap.put(handlerName, handlerService);
      }
      if (!idHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("identifier.assignment.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      idHandlerMap = null;
    }
  }

  /** The helper map. */
  private static Map<String, ComputePreferredNameHandler> pnHandlerMap = null;

  static {
    pnHandlerMap = new HashMap<>();

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "compute.preferred.name.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final ComputePreferredNameHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ComputePreferredNameHandler.class);
        pnHandlerMap.put(handlerName, handlerService);
      }
      if (!pnHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("compute.preferred.name.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      pnHandlerMap = null;
    }
  }

  /** The normalized string handler. */
  private static NormalizedStringHandler normalizedStringHandler = null;

  static {
    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "normalized.string.handler";
      final String handlerName = config.getProperty(key);

      final NormalizedStringHandler handlerService =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, NormalizedStringHandler.class);
      normalizedStringHandler = handlerService;
    } catch (Exception e) {
      e.printStackTrace();
      normalizedStringHandler = null;
    }
  }

  /** The search. */
  private static Set<String> searchHandlerNames = null;

  static {
    searchHandlerNames = new HashSet<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "search.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        searchHandlerNames.add(handlerName);

      }
      if (!searchHandlerNames.contains(ConfigUtility.DEFAULT)) {
        throw new Exception("search.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
      if (!searchHandlerNames.contains(ConfigUtility.ATOMCLASS)) {
        throw new Exception("search.handler." + ConfigUtility.ATOMCLASS
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      searchHandlerNames = null;
    }
  }

  /**
   * Instantiates an empty {@link ContentServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ContentServiceJpa() throws Exception {
    super();

    if (listeners == null) {
      throw new Exception(
          "Listeners did not properly initialize, serious error.");
    }

    if (idHandlerMap == null) {
      throw new Exception(
          "Identifier assignment handler did not properly initialize, serious error.");
    }

    if (pnHandlerMap == null) {
      throw new Exception(
          "Preferred name handler did not properly initialize, serious error.");
    }

    if (normalizedStringHandler == null) {
      throw new Exception(
          "Normalized string handler did not properly initialize, serious error.");
    }

    if (searchHandlerNames == null) {
      throw new Exception(
          "Search handler names did not properly initialize, serious error.");
    }
  }

  /* see superclass */
  /**
   * Returns the concept.
   *
   * @param id the id
   * @return the concept
   * @throws Exception the exception
   */
  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get concept " + id);
    return getComponent(id, ConceptJpa.class);
  }

  /* see superclass */
  /**
   * Returns the concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concepts
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concepts " + terminologyId + "/" + terminology
            + "/" + version);
    final List<Concept> concepts =
        getComponents(terminologyId, terminology, version, ConceptJpa.class);
    if (concepts == null) {
      return null;
    }
    final ConceptList list = new ConceptListJpa();
    list.setTotalCount(concepts.size());
    list.setObjects(concepts);
    return list;
  }

  /* see superclass */
  /**
   * Returns the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the concept
   * @throws Exception the exception
   */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concept " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        ConceptJpa.class);
  }

  /* see superclass */
  /**
   * Adds the concept.
   *
   * @param concept the concept
   * @return the concept
   * @throws Exception the exception
   */
  @Override
  public Concept addConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add concept " + concept);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(concept.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + concept.getTerminology());
      }
      String id = idHandler.getTerminologyId(concept);
      concept.setTerminologyId(id);
    }

    // Add component
    final Concept newConcept = addComponent(concept);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.conceptChanged(newConcept, WorkflowListener.Action.ADD);
      }
    }
    return newConcept;
  }

  /* see superclass */
  /**
   * Update concept.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  @Override
  public void updateConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update concept " + concept);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(concept.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowConceptIdChangeOnUpdate()) {
        final Concept concept2 = getConcept(concept.getId());
        if (!idHandler.getTerminologyId(concept).equals(
            idHandler.getTerminologyId(concept2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set concept id on update
        concept.setTerminologyId(idHandler.getTerminologyId(concept));
      }
    }
    // update component
    this.updateComponent(concept);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.conceptChanged(concept, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the concept.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeConcept(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove concept " + id);
    // Remove the component
    final Concept concept = removeComponent(id, ConceptJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.conceptChanged(concept, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the subset.
   *
   * @param id the id
   * @param subsetClass the subset class
   * @return the subset
   * @throws Exception the exception
   */
  @Override
  public Subset getSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - subset " + id);
    if (subsetClass != null) {
      return getComponent(id, subsetClass);
    } else {
      Subset subset = getComponent(id, AtomSubsetJpa.class);
      if (subset == null) {
        subset = getComponent(id, ConceptSubsetJpa.class);
      }
      return subset;
    }
  }

  /* see superclass */
  /**
   * Returns the subset.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param subsetClass the subset class
   * @return the subset
   * @throws Exception the exception
   */
  @Override
  public Subset getSubset(String terminologyId, String terminology,
    String version, String branch, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    if (subsetClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          subsetClass);
    } else {
      Subset subset =
          getComponent(terminologyId, terminology, version, branch,
              AtomSubsetJpa.class);
      if (subset == null) {
        subset =
            getComponent(terminologyId, terminology, version, branch,
                ConceptSubsetJpa.class);
      }
      return subset;
    }

  }

  /* see superclass */
  /**
   * Returns the atom subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the atom subsets
   * @throws Exception the exception
   */
  @Override
  public SubsetList getAtomSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atom subsets " + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<Subset> m = query.getResultList();
      final SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());

      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the concept subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the concept subsets
   * @throws Exception the exception
   */
  @Override
  public SubsetList getConceptSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concept subsets " + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetJpa a where "
            + "version = :version and terminology = :terminology");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<Subset> m = query.getResultList();
      final SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());
      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Find atom subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the subset member list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find atom subset members " + subsetId + "/"
            + terminology + "/" + version + ", query=" + query);
    // Prepare the query string

    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (subsetId != null && !subsetId.isEmpty()) {
      if (finalQuery.length() > 0) {
        finalQuery.append(" AND ");
      }
      finalQuery.append("subsetTerminologyId:" + subsetId);
    }
    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    final SubsetMemberList list = new SubsetMemberListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, finalQuery.toString(), "memberNameSort",
        ConceptSubsetMemberJpa.class, AtomSubsetMemberJpa.class, pfs, totalCt,
        manager));
    list.setTotalCount(totalCt[0]);
    return list;
  }

  /* see superclass */
  /**
   * Find concept subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the subset member list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  @Override
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find concept subset members " + subsetId + "/"
            + terminology + "/" + version + ", query=" + query);
    // Prepare the query string

    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (subsetId != null && !subsetId.isEmpty()) {
      if (finalQuery.length() > 0) {
        finalQuery.append(" AND ");
      }
      finalQuery.append("subsetTerminologyId:" + subsetId);
    }

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    final SubsetMemberList list = new SubsetMemberListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, finalQuery.toString(), "memberNameSort",
        ConceptSubsetMemberJpa.class, ConceptSubsetMemberJpa.class, pfs,
        totalCt, manager));
    list.setTotalCount(totalCt[0]);

    return list;
  }

  /* see superclass */
  /**
   * Returns the subset members for atom.
   *
   * @param atomId the atom id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the subset members for atom
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembersForAtom(String atomId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members for atom " + atomId + "/"
            + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetMemberJpa a, "
            + " AtomJpa b where b.terminologyId = :atomId "
            + "and b.version = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      final SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("atomId", atomId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (final SubsetMember<? extends ComponentHasAttributesAndName> s :
       * list .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the subset members for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the subset members for concept
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembersForConcept(String conceptId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members for concept " + conceptId + "/"
            + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetMemberJpa a, "
            + " ConceptJpa b where b.terminologyId = :conceptId "
            + "and b.version = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      final SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("conceptId", conceptId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (final SubsetMember<? extends ComponentHasAttributesAndName> s :
       * list .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */
      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the all subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all subsets
   * @throws Exception the exception
   */
  @Override
  public SubsetList getAllSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all subsets " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      final SubsetList list = getAtomSubsets(terminology, version, branch);
      if (list == null) {
        return getConceptSubsets(terminology, version, branch);
      } else {
        list.getObjects().addAll(
            getConceptSubsets(terminology, version, branch).getObjects());
      }
      list.setTotalCount(list.getObjects().size());
      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the definition.
   *
   * @param id the id
   * @return the definition
   * @throws Exception the exception
   */
  @Override
  public Definition getDefinition(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get definition " + id);
    return getComponent(id, DefinitionJpa.class);
  }

  /* see superclass */
  /**
   * Returns the definitions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the definitions
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public DefinitionList getDefinitions(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get definitions " + terminologyId + "/"
            + terminology + "/" + version);
    final List<Definition> definitions =
        getComponents(terminologyId, terminology, version, DefinitionJpa.class);
    if (definitions == null) {
      return null;
    }
    final DefinitionList list = new DefinitionListJpa();
    list.setTotalCount(definitions.size());
    list.setObjects(definitions);
    return list;
  }

  /* see superclass */
  /**
   * Returns the definition.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the definition
   * @throws Exception the exception
   */
  @Override
  public Definition getDefinition(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get definition " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        DefinitionJpa.class);
  }

  /* see superclass */
  /**
   * Adds the definition.
   *
   * @param definition the definition
   * @param component the component
   * @return the definition
   * @throws Exception the exception
   */
  @Override
  public Definition addDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add definition " + definition);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(definition.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + definition.getTerminology());
      }
      String id = idHandler.getTerminologyId(definition, component);
      definition.setTerminologyId(id);
    }

    // Add component
    final Definition newDefinition = addComponent(definition);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.definitionChanged(newDefinition, WorkflowListener.Action.ADD);
      }
    }
    return newDefinition;
  }

  /* see superclass */
  /**
   * Update definition.
   *
   * @param definition the definition
   * @param component the component
   * @throws Exception the exception
   */
  @Override
  public void updateDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update definition " + definition);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(definition.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Definition definition2 =
            getComponent(definition.getId(), DefinitionJpa.class);
        if (!idHandler.getTerminologyId(definition, component).equals(
            idHandler.getTerminologyId(definition2, component))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set definition id on update
        definition.setTerminologyId(idHandler.getTerminologyId(definition,
            component));
      }
    }
    // update component
    this.updateComponent(definition);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.definitionChanged(definition, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the definition.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeDefinition(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove definition " + id);
    // Remove the component
    final Definition definition = removeComponent(id, DefinitionJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.definitionChanged(definition, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Adds the semantic type component.
   *
   * @param semanticTypeComponent the semantic type component
   * @param concept the concept
   * @return the semantic type component
   * @throws Exception the exception
   */
  @Override
  public SemanticTypeComponent addSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent, Concept concept)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add semanticTypeComponent " + semanticTypeComponent);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(semanticTypeComponent.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + semanticTypeComponent.getTerminology());
      }
      String id = idHandler.getTerminologyId(semanticTypeComponent, concept);
      semanticTypeComponent.setTerminologyId(id);
    }

    // Add component
    final SemanticTypeComponent newSemanticTypeComponent =
        addComponent(semanticTypeComponent);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(newSemanticTypeComponent,
            WorkflowListener.Action.ADD);
      }
    }
    return newSemanticTypeComponent;
  }

  /* see superclass */
  /**
   * Update semantic type component.
   *
   * @param semanticTypeComponent the semantic type component
   * @param concept the concept
   * @throws Exception the exception
   */
  @Override
  public void updateSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent, Concept concept)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update semanticTypeComponent "
            + semanticTypeComponent);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(semanticTypeComponent.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        SemanticTypeComponent semanticTypeComponent2 =
            getComponent(semanticTypeComponent.getId(),
                SemanticTypeComponent.class);
        if (!idHandler.getTerminologyId(semanticTypeComponent, concept).equals(
            idHandler.getTerminologyId(semanticTypeComponent2, concept))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set semanticTypeComponent id on update
        semanticTypeComponent.setTerminologyId(idHandler.getTerminologyId(
            semanticTypeComponent, concept));
      }
    }
    // update component
    this.updateComponent(semanticTypeComponent);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(semanticTypeComponent,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the semantic type component.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeSemanticTypeComponent(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove semanticTypeComponent " + id);
    // Remove the component
    final SemanticTypeComponent semanticTypeComponent =
        removeComponent(id, SemanticTypeComponentJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(semanticTypeComponent,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the descriptor.
   *
   * @param id the id
   * @return the descriptor
   * @throws Exception the exception
   */
  @Override
  public Descriptor getDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get descriptor " + id);
    return getComponent(id, DescriptorJpa.class);
  }

  /* see superclass */
  /**
   * Returns the descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the descriptors
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public DescriptorList getDescriptors(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get descriptors " + terminologyId + "/"
            + terminology + "/" + version);
    final List<Descriptor> descriptors =
        getComponents(terminologyId, terminology, version, DescriptorJpa.class);
    if (descriptors == null) {
      return null;
    }
    DescriptorList list = new DescriptorListJpa();
    list.setTotalCount(descriptors.size());
    list.setObjects(descriptors);
    return list;
  }

  /* see superclass */
  /**
   * Returns the descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the descriptor
   * @throws Exception the exception
   */
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get descriptor " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        DescriptorJpa.class);
  }

  /* see superclass */
  /**
   * Adds the descriptor.
   *
   * @param descriptor the descriptor
   * @return the descriptor
   * @throws Exception the exception
   */
  @Override
  public Descriptor addDescriptor(Descriptor descriptor) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add descriptor " + descriptor);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(descriptor.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + descriptor.getTerminology());
      }
      String id = idHandler.getTerminologyId(descriptor);
      descriptor.setTerminologyId(id);
    }

    // Add component
    Descriptor newDescriptor = addComponent(descriptor);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.descriptorChanged(newDescriptor, WorkflowListener.Action.ADD);
      }
    }
    return newDescriptor;
  }

  /* see superclass */
  /**
   * Update descriptor.
   *
   * @param descriptor the descriptor
   * @throws Exception the exception
   */
  @Override
  public void updateDescriptor(Descriptor descriptor) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update descriptor " + descriptor);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(descriptor.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Descriptor descriptor2 = getDescriptor(descriptor.getId());
        if (!idHandler.getTerminologyId(descriptor).equals(
            idHandler.getTerminologyId(descriptor2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set descriptor id on update
        descriptor.setTerminologyId(idHandler.getTerminologyId(descriptor));
      }
    }
    // update component
    this.updateComponent(descriptor);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.descriptorChanged(descriptor, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the descriptor.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove descriptor " + id);
    // Remove the component
    final Descriptor descriptor = removeComponent(id, DescriptorJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.descriptorChanged(descriptor, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the code.
   *
   * @param id the id
   * @return the code
   * @throws Exception the exception
   */
  @Override
  public Code getCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get code " + id);
    final Code c = manager.find(CodeJpa.class, id);
    return c;
  }

  /* see superclass */
  /**
   * Returns the codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the codes
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public CodeList getCodes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get codes " + terminologyId + "/" + terminology
            + "/" + version);
    final List<Code> codes =
        getComponents(terminologyId, terminology, version, CodeJpa.class);
    if (codes == null) {
      return null;
    }
    final CodeList list = new CodeListJpa();
    list.setTotalCount(codes.size());
    list.setObjects(codes);
    return list;
  }

  /* see superclass */
  /**
   * Returns the code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the code
   * @throws Exception the exception
   */
  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get code " + terminologyId + "/" + terminology + "/"
            + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        CodeJpa.class);
  }

  /* see superclass */
  /**
   * Adds the code.
   *
   * @param code the code
   * @return the code
   * @throws Exception the exception
   */
  @Override
  public Code addCode(Code code) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add code " + code);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(code.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + code.getTerminology());
      }
      String id = idHandler.getTerminologyId(code);
      code.setTerminologyId(id);
    }

    // Add component
    final Code newCode = addComponent(code);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.codeChanged(newCode, WorkflowListener.Action.ADD);
      }
    }
    return newCode;
  }

  /* see superclass */
  /**
   * Update code.
   *
   * @param code the code
   * @throws Exception the exception
   */
  @Override
  public void updateCode(Code code) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update code " + code);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(code.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Code code2 = getCode(code.getId());
        if (!idHandler.getTerminologyId(code).equals(
            idHandler.getTerminologyId(code2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set code id on update
        code.setTerminologyId(idHandler.getTerminologyId(code));
      }
    }
    // update component
    this.updateComponent(code);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.codeChanged(code, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the code.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove code " + id);
    // Remove the component
    final Code code = removeComponent(id, CodeJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.codeChanged(code, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the lexical class.
   *
   * @param id the id
   * @return the lexical class
   * @throws Exception the exception
   */
  @Override
  public LexicalClass getLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical class " + id);
    return getComponent(id, LexicalClassJpa.class);
  }

  /* see superclass */
  /**
   * Returns the lexical classes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the lexical classes
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public LexicalClassList getLexicalClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical classes " + terminologyId + "/"
            + terminology + "/" + version);
    final List<LexicalClass> luis =
        getComponents(terminologyId, terminology, version,
            LexicalClassJpa.class);
    if (luis == null) {
      return null;
    }
    final LexicalClassList list = new LexicalClassListJpa();
    list.setTotalCount(luis.size());
    list.setObjects(luis);
    return list;

  }

  /* see superclass */
  /**
   * Returns the lexical class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the lexical class
   * @throws Exception the exception
   */
  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical class " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        LexicalClassJpa.class);
  }

  /* see superclass */
  /**
   * Adds the lexical class.
   *
   * @param lexicalClass the lexical class
   * @return the lexical class
   * @throws Exception the exception
   */
  @Override
  public LexicalClass addLexicalClass(LexicalClass lexicalClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add lexical class " + lexicalClass);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(lexicalClass.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + lexicalClass.getTerminology());
      }
      String id = idHandler.getTerminologyId(lexicalClass);
      lexicalClass.setTerminologyId(id);
    }

    // Add component
    final LexicalClass newLexicalClass = addComponent(lexicalClass);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(newLexicalClass,
            WorkflowListener.Action.ADD);
      }
    }
    return newLexicalClass;
  }

  /* see superclass */
  /**
   * Update lexical class.
   *
   * @param lexicalClass the lexical class
   * @throws Exception the exception
   */
  @Override
  public void updateLexicalClass(LexicalClass lexicalClass) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update lexical class " + lexicalClass);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(lexicalClass.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        LexicalClass lexicalClass2 = getLexicalClass(lexicalClass.getId());
        if (!idHandler.getTerminologyId(lexicalClass).equals(
            idHandler.getTerminologyId(lexicalClass2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set lexicalClass id on update
        lexicalClass.setTerminologyId(idHandler.getTerminologyId(lexicalClass));
      }
    }
    // update component
    this.updateComponent(lexicalClass);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(lexicalClass,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the lexical class.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove lexical class " + id);
    // Remove the component
    final LexicalClass lexicalClass =
        removeComponent(id, LexicalClassJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(lexicalClass,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the string class.
   *
   * @param id the id
   * @return the string class
   * @throws Exception the exception
   */
  @Override
  public StringClass getStringClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string class " + id);
    return getComponent(id, StringClassJpa.class);
  }

  /* see superclass */
  /**
   * Returns the string classes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the string classes
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public StringClassList getStringClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string classes " + terminologyId + "/"
            + terminology + "/" + version);
    final List<StringClass> suis =
        getComponents(terminologyId, terminology, version, StringClassJpa.class);
    if (suis == null) {
      return null;
    }
    final StringClassList list = new StringClassListJpa();
    list.setTotalCount(suis.size());
    list.setObjects(suis);
    return list;
  }

  /* see superclass */
  /**
   * Returns the string class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the string class
   * @throws Exception the exception
   */
  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string class " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        StringClass.class);
  }

  /* see superclass */
  /**
   * Adds the string class.
   *
   * @param stringClass the string class
   * @return the string class
   * @throws Exception the exception
   */
  @Override
  public StringClass addStringClass(StringClass stringClass) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add string class " + stringClass);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(stringClass.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + stringClass.getTerminology());
      }
      String id = idHandler.getTerminologyId(stringClass);
      stringClass.setTerminologyId(id);
    }

    // Add component
    final StringClass newStringClass = addComponent(stringClass);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(newStringClass, WorkflowListener.Action.ADD);
      }
    }
    return newStringClass;
  }

  /* see superclass */
  /**
   * Update string class.
   *
   * @param stringClass the string class
   * @throws Exception the exception
   */
  @Override
  public void updateStringClass(StringClass stringClass) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update string class " + stringClass);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(stringClass.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        StringClass stringClass2 = getStringClass(stringClass.getId());
        if (!idHandler.getTerminologyId(stringClass).equals(
            idHandler.getTerminologyId(stringClass2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set stringClass id on update
        stringClass.setTerminologyId(idHandler.getTerminologyId(stringClass));
      }
    }
    // update component
    this.updateComponent(stringClass);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(stringClass, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the string class.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeStringClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove string class " + id);
    // Remove the component
    final StringClass stringClass = removeComponent(id, StringClassJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(stringClass, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Find descendant concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  @Override
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant concepts " + terminologyId + ", "
            + terminology);
    final long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Concept> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, ConceptJpa.class, totalCt);
    final ConceptList list = new ConceptListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  /**
   * Find ancestor concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  @Override
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor concepts " + terminologyId + ", "
            + terminology);
    final long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Concept> ancestors =
        this.findAncestorsHelper(terminologyId, terminology, version,
            parentsOnly, branch, pfs, ConceptJpa.class, totalCt);
    final ConceptList list = new ConceptListJpa();
    list.setObjects(ancestors);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /**
   * Find descendants helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @param totalCt the total ct
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  private List findDescendantsHelper(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs,
    Class<?> clazz, long[] totalCt) throws Exception {

    if (pfs != null && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    final String queryStr =
        "select a from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " super, " + clazz.getName() + " a "
            + " where super.version = :version "
            + " and super.terminology = :terminology "
            + " and super.terminologyId = :terminologyId"
            + " and tr.superType = super" + " and tr.subType = a "
            + " and tr.superType != tr.subType"
            + (childrenOnly ? " and depth = 1" : "");
    final javax.persistence.Query query = applyPfsToJqlQuery(queryStr, pfs);

    final javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " super, " + clazz.getName() + " a "
            + " where super.version = :version "
            + " and super.terminology = :terminology "
            + " and super.terminologyId = :terminologyId"
            + " and tr.superType = super" + " and tr.subType = a "
            + " and tr.superType != tr.subType"
            + (childrenOnly ? " and depth = 1" : ""));

    ctQuery.setParameter("terminology", terminology);
    ctQuery.setParameter("version", version);
    ctQuery.setParameter("terminologyId", terminologyId);
    totalCt[0] = ((Long) ctQuery.getSingleResult()).intValue();

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    query.setParameter("terminologyId", terminologyId);

    return query.getResultList();
  }

  /**
   * Find ancestors helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @param totalCt the total ct
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  private List findAncestorsHelper(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs,
    Class<?> clazz, long[] totalCt) throws Exception {

    if (pfs != null && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    final String queryStr =
        "select a from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " sub, " + clazz.getName() + " a "
            + " where sub.version = :version "
            + " and sub.terminology = :terminology "
            + " and sub.terminologyId = :terminologyId"
            + " and tr.subType = sub" + " and tr.superType = a "
            + " and tr.subType != tr.superType"
            + (parentsOnly ? " and depth = 1" : "");
    final javax.persistence.Query query = applyPfsToJqlQuery(queryStr, pfs);

    final javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " sub, " + clazz.getName() + " a "
            + " where sub.version = :version "
            + " and sub.terminology = :terminology "
            + " and sub.terminologyId = :terminologyId"
            + " and tr.subType = sub" + " and tr.superType = a "
            + " and tr.subType != tr.superType"
            + (parentsOnly ? " and depth = 1" : ""));

    ctQuery.setParameter("terminology", terminology);
    ctQuery.setParameter("version", version);
    ctQuery.setParameter("terminologyId", terminologyId);
    totalCt[0] = ((Long) ctQuery.getSingleResult()).intValue();

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    query.setParameter("terminologyId", terminologyId);

    return query.getResultList();
  }

  /* see superclass */
  /**
   * Find descendant descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the descriptor list
   * @throws Exception the exception
   */
  @Override
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant descriptors " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Descriptor> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, DescriptorJpa.class, totalCt);
    final DescriptorList list = new DescriptorListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  /**
   * Find ancestor descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the descriptor list
   * @throws Exception the exception
   */
  @Override
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor descriptors " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Descriptor> ancestors =
        this.findAncestorsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, DescriptorJpa.class, totalCt);
    final DescriptorList list = new DescriptorListJpa();
    list.setObjects(ancestors);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  /**
   * Find descendant codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the code list
   * @throws Exception the exception
   */
  @Override
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant codes " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Code> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, CodeJpa.class, totalCt);
    final CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  /**
   * Find ancestor codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @return the code list
   * @throws Exception the exception
   */
  @Override
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor codes " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Code> descendants =
        this.findAncestorsHelper(terminologyId, terminology, version,
            parentsOnly, branch, pfs, CodeJpa.class, totalCt);
    final CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  /**
   * Returns the atom.
   *
   * @param id the id
   * @return the atom
   * @throws Exception the exception
   */
  @Override
  public Atom getAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get atom " + id);
    return getComponent(id, AtomJpa.class);
  }

  /* see superclass */
  /**
   * Returns the atoms.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the atoms
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public AtomList getAtoms(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atoms " + terminologyId + "/" + terminology
            + "/" + version);
    final List<Atom> atoms =
        getComponents(terminologyId, terminology, version, AtomJpa.class);
    if (atoms == null) {
      return null;
    }
    final AtomList list = new AtomListJpa();
    list.setTotalCount(atoms.size());
    list.setObjects(atoms);
    return list;
  }

  /* see superclass */
  /**
   * Returns the atom.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the atom
   * @throws Exception the exception
   */
  @Override
  public Atom getAtom(String terminologyId, String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atom " + terminologyId + "/" + terminology + "/"
            + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AtomJpa.class);
  }

  /* see superclass */
  /**
   * Adds the atom.
   *
   * @param atom the atom
   * @return the atom
   * @throws Exception the exception
   */
  @Override
  public Atom addAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add atom " + atom);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(atom.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + atom.getTerminology());
      }
      atom.setTerminologyId(idHandler.getTerminologyId(atom));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + atom.getTerminology());
    }

    // Add component
    final Atom newAtom = addComponent(atom);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.atomChanged(newAtom, WorkflowListener.Action.ADD);
      }
    }
    return newAtom;
  }

  /* see superclass */
  /**
   * Update atom.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  @Override
  public void updateAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update atom " + atom);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(atom.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      final Atom atom2 = getAtom(atom.getId());
      if (!idHandler.getTerminologyId(atom).equals(
          idHandler.getTerminologyId(atom2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(atom);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.atomChanged(atom, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the atom.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove atom " + id);
    // Remove the component
    final Atom atom = removeComponent(id, AtomJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.atomChanged(atom, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @return the relationship
   * @throws Exception the exception
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    Long id,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationship " + id);
    if (relationshipClass != null) {
      return getComponent(id, relationshipClass);
    } else {
      Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
          getComponent(id, ConceptRelationshipJpa.class);
      if (rel == null) {
        rel = getComponent(id, AtomRelationshipJpa.class);
      }
      if (rel == null) {
        rel = getComponent(id, CodeRelationshipJpa.class);
      }
      if (rel == null) {
        rel = getComponent(id, DescriptorRelationshipJpa.class);
      }
      return rel;
    }
  }

  /* see superclass */
  /**
   * Returns the relationships.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param relationshipClass the relationship class
   * @return the relationships
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipList getRelationships(
    String terminologyId,
    String terminology,
    String version,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships " + terminologyId + "/"
            + terminology + "/" + version);
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationships =
        null;
    if (relationshipClass != null) {
      relationships =
          getComponents(terminologyId, terminology, version, relationshipClass);
    } else {
      relationships =
          getComponents(terminologyId, terminology, version,
              ConceptRelationshipJpa.class);
      if (relationships == null) {
        relationships =
            getComponents(terminologyId, terminology, version,
                AtomRelationshipJpa.class);
      }
      if (relationships == null) {
        relationships =
            getComponents(terminologyId, terminology, version,
                CodeRelationshipJpa.class);
      }
      if (relationships == null) {
        relationships =
            getComponents(terminologyId, terminology, version,
                DescriptorRelationshipJpa.class);
      }
    }

    if (relationships == null) {
      return null;
    }
    final RelationshipList list = new RelationshipListJpa();
    list.setTotalCount(relationships.size());
    list.setObjects(relationships);
    return list;
  }

  /* see superclass */
  /**
   * Returns the relationship.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param relationshipClass the relationship class
   * @return the relationship
   * @throws Exception the exception
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    String terminologyId,
    String terminology,
    String version,
    String branch,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationship " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    if (relationshipClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          relationshipClass);
    }
    final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
        getComponent(terminologyId, terminology, version, branch,
            ConceptRelationshipJpa.class);
    if (rel == null) {
      getComponent(terminologyId, terminology, version, branch,
          AtomRelationshipJpa.class);
    }
    if (rel == null) {
      getComponent(terminologyId, terminology, version, branch,
          CodeRelationshipJpa.class);
    }
    if (rel == null) {
      getComponent(terminologyId, terminology, version, branch,
          DescriptorRelationshipJpa.class);
    }
    return rel;
  }

  /* see superclass */
  /**
   * Adds the relationship.
   *
   * @param rel the rel
   * @return the relationship
   * @throws Exception the exception
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> addRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add relationship " + rel);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(rel.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + rel.getTerminology());
      }
      String id = idHandler.getTerminologyId(rel);
      rel.setTerminologyId(id);
    }

    // Add component
    final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.relationshipChanged(newRel, WorkflowListener.Action.ADD);
      }
    }
    return newRel;
  }

  /* see superclass */
  /**
   * Update relationship.
   *
   * @param rel the rel
   * @throws Exception the exception
   */
  @Override
  public void updateRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update relationship " + rel);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(rel.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel2 =
            getComponent(rel.getId(), rel.getClass());
        if (!idHandler.getTerminologyId(rel).equals(
            idHandler.getTerminologyId(rel2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        rel.setTerminologyId(idHandler.getTerminologyId(rel));
      }
    }
    // update component
    this.updateComponent(rel);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.relationshipChanged(rel, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public void removeRelationship(
    Long id,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove relationship " + id);
    // Remove the component
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
        null;
    if (relationshipClass != null) {
      rel = removeComponent(id, relationshipClass);
    } else {
      rel = getRelationship(id, relationshipClass);
      rel = removeComponent(id, rel.getClass());
    }
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.relationshipChanged(rel, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the transitive relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @return the transitive relationship
   * @throws Exception the exception
   */
  @Override
  public TransitiveRelationship<? extends AtomClass> getTransitiveRelationship(
    Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get transitive relationship " + id);
    if (relationshipClass != null) {
      return getComponent(id, relationshipClass);
    } else {
      TransitiveRelationship<? extends AtomClass> rel =
          getComponent(id, ConceptTransitiveRelationshipJpa.class);
      if (rel == null) {
        rel = getComponent(id, CodeTransitiveRelationshipJpa.class);
      }
      if (rel == null) {
        rel = getComponent(id, DescriptorTransitiveRelationshipJpa.class);
      }
      return rel;
    }
  }

  /* see superclass */
  /**
   * Adds the transitive relationship.
   *
   * @param rel the rel
   * @return the transitive relationship
   * @throws Exception the exception
   */
  @Override
  public TransitiveRelationship<? extends ComponentHasAttributes> addTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add transitive relationship " + rel);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(rel.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + rel.getTerminology());
      }
      String id = idHandler.getTerminologyId(rel);
      rel.setTerminologyId(id);
    }

    // Add component
    final TransitiveRelationship<? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    return newRel;
  }

  /* see superclass */
  /**
   * Update transitive relationship.
   *
   * @param rel the rel
   * @throws Exception the exception
   */
  @Override
  public void updateTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update transitive relationship " + rel);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(rel.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        final TransitiveRelationship<? extends ComponentHasAttributes> rel2 =
            getComponent(rel.getId(), rel.getClass());
        if (!idHandler.getTerminologyId(rel).equals(
            idHandler.getTerminologyId(rel2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        rel.setTerminologyId(idHandler.getTerminologyId(rel));
      }
    }
    // update component
    this.updateComponent(rel);

  }

  /* see superclass */
  /**
   * Removes the transitive relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @throws Exception the exception
   */
  @Override
  public void removeTransitiveRelationship(
    Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove transitive relationship " + id);

    final TransitiveRelationship<? extends ComponentHasAttributes> rel =
        getComponent(id, relationshipClass);
    removeComponent(id, rel.getClass());
  }

  /* see superclass */
  /**
   * Returns the tree position.
   *
   * @param id the id
   * @param treeposClass the treepos class
   * @return the tree position
   * @throws Exception the exception
   */
  @Override
  public TreePosition<? extends AtomClass> getTreePosition(Long id,
    Class<? extends TreePosition<? extends AtomClass>> treeposClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get tree position " + id);
    if (treeposClass != null) {
      return getComponent(id, treeposClass);
    } else {
      TreePosition<? extends AtomClass> treepos =
          getComponent(id, ConceptTreePositionJpa.class);
      if (treepos == null) {
        treepos = getComponent(id, CodeTreePositionJpa.class);
      }
      if (treepos == null) {
        treepos = getComponent(id, DescriptorTreePositionJpa.class);
      }
      return treepos;
    }
  }

  /* see superclass */
  /**
   * Adds the tree position.
   *
   * @param treepos the treepos
   * @return the tree position
   * @throws Exception the exception
   */
  @Override
  public TreePosition<? extends ComponentHasAttributesAndName> addTreePosition(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add tree position " + treepos);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(treepos.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + treepos.getTerminology());
      }
      String id = idHandler.getTerminologyId(treepos);
      treepos.setTerminologyId(id);
    }

    // Add component
    final TreePosition<? extends ComponentHasAttributesAndName> newTreepos =
        addComponent(treepos);

    return newTreepos;
  }

  /* see superclass */
  /**
   * Update tree position.
   *
   * @param treepos the treepos
   * @throws Exception the exception
   */
  @Override
  public void updateTreePosition(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update tree position " + treepos);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(treepos.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        TreePosition<? extends ComponentHasAttributesAndName> treepos2 =
            getComponent(treepos.getId(), treepos.getClass());
        if (!idHandler.getTerminologyId(treepos).equals(
            idHandler.getTerminologyId(treepos2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        treepos.setTerminologyId(idHandler.getTerminologyId(treepos));
      }
    }
    // update component
    this.updateComponent(treepos);

  }

  /* see superclass */
  /**
   * Removes the tree position.
   *
   * @param id the id
   * @param treeposClass the treepos class
   * @throws Exception the exception
   */
  @Override
  public void removeTreePosition(Long id,
    Class<? extends TreePosition<? extends AtomClass>> treeposClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove tree position " + id);
    final TreePosition<? extends ComponentHasAttributesAndName> treepos =
        getComponent(id, treeposClass);
    removeComponent(id, treepos.getClass());
  }

  /* see superclass */
  /**
   * Adds the subset.
   *
   * @param subset the subset
   * @return the subset
   * @throws Exception the exception
   */
  @Override
  public Subset addSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add subset " + subset);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(subset.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + subset.getTerminology());
      }
      subset.setTerminologyId(idHandler.getTerminologyId(subset));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + subset.getTerminology());
    }

    // Add component
    final Subset newSubset = addComponent(subset);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.subsetChanged(newSubset, WorkflowListener.Action.ADD);
      }
    }
    return newSubset;
  }

  /* see superclass */
  /**
   * Update subset.
   *
   * @param subset the subset
   * @throws Exception the exception
   */
  @Override
  public void updateSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update subset " + subset);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subset.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      final Subset subset2 = getSubset(subset.getId(), subset.getClass());
      if (!idHandler.getTerminologyId(subset).equals(
          idHandler.getTerminologyId(subset2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(subset);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.subsetChanged(subset, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the subset.
   *
   * @param id the id
   * @param subsetClass the subset class
   * @throws Exception the exception
   */
  @Override
  public void removeSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove subset " + id);
    Subset subset = null;
    // Remove the component
    if (subsetClass != null) {
      subset = removeComponent(id, subsetClass);
    } else {
      subset = getSubset(id, subsetClass);
      removeComponent(id, subset.getClass());
    }

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.subsetChanged(subset, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the subset member.
   *
   * @param id the id
   * @param memberClass the member class
   * @return the subset member
   * @throws Exception the exception
   */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset member " + id);
    if (memberClass != null) {
      return getComponent(id, memberClass);
    } else {
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
          getComponent(id, AtomSubsetMemberJpa.class);
      if (member == null) {
        member = getComponent(id, ConceptSubsetMemberJpa.class);
      }
      return member;
    }

  }

  /* see superclass */
  /**
   * Returns the subset members.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param memberClass the member class
   * @return the subset members
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembers(
    String terminologyId,
    String terminology,
    String version,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members " + terminologyId + "/"
            + terminology + "/" + version);
    List<SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> members =
        null;
    if (memberClass != null) {
      members =

      getComponents(terminologyId, terminology, version, memberClass);
    } else {
      members =
          getComponents(terminologyId, terminology, version,
              AtomSubsetMemberJpa.class);
      if (members == null) {
        members =
            getComponents(terminologyId, terminology, version,
                ConceptSubsetMemberJpa.class);

      }
    }
    if (members == null) {
      return null;
    }
    final SubsetMemberList list = new SubsetMemberListJpa();
    list.setTotalCount(members.size());
    list.setObjects(members);
    return list;
  }

  /* see superclass */
  /**
   * Returns the subset member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param memberClass the member class
   * @return the subset member
   * @throws Exception the exception
   */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    String terminologyId,
    String terminology,
    String version,
    String branch,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset member " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    if (memberClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          memberClass);
    } else {
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
          getComponent(terminologyId, terminology, version, branch,
              AtomSubsetMemberJpa.class);
      if (member == null) {
        member =
            getComponent(terminologyId, terminology, version, branch,
                ConceptSubsetMemberJpa.class);
      }
      return member;
    }
  }

  /* see superclass */
  /**
   * Adds the subset member.
   *
   * @param subsetMember the subset member
   * @return the subset member
   * @throws Exception the exception
   */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> addSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add subset member " + subsetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(subsetMember.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + subsetMember.getTerminology());
      }
      subsetMember.setTerminologyId(idHandler.getTerminologyId(subsetMember));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + subsetMember.getTerminology());
    }

    // Add component
    final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> newSubsetMember =
        addComponent(subsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(newSubsetMember,
            WorkflowListener.Action.ADD);
      }
    }
    return newSubsetMember;
  }

  /* see superclass */
  /**
   * Update subset member.
   *
   * @param subsetMember the subset member
   * @throws Exception the exception
   */
  @Override
  public void updateSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update subsetMember " + subsetMember);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subsetMember.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      @SuppressWarnings("unchecked")
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember2 =
          getComponent(subsetMember.getId(), subsetMember.getClass());
      if (!idHandler.getTerminologyId(subsetMember).equals(
          idHandler.getTerminologyId(subsetMember2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(subsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(subsetMember,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the subset member.
   *
   * @param id the id
   * @param memberClass the member class
   * @throws Exception the exception
   */
  @Override
  public void removeSubsetMember(
    Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove subsetMember " + id);
    // find and remove the component
    final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
        getComponent(id, memberClass);
    removeComponent(id, member.getClass());

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(member, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the attribute.
   *
   * @param id the id
   * @return the attribute
   * @throws Exception the exception
   */
  @Override
  public Attribute getAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get attribute " + id);
    return getComponent(id, AttributeJpa.class);
  }

  /* see superclass */
  /**
   * Returns the attributes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the attributes
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeList getAttributes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get attributes " + terminologyId + "/" + terminology
            + "/" + version);
    final List<Attribute> attributes =
        getComponents(terminologyId, terminology, version, AttributeJpa.class);
    if (attributes == null) {
      return null;
    }
    final AttributeList list = new AttributeListJpa();
    list.setTotalCount(attributes.size());
    list.setObjects(attributes);
    return list;
  }

  /* see superclass */
  /**
   * Returns the attribute.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the attribute
   * @throws Exception the exception
   */
  @Override
  public Attribute getAttribute(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get attribute " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AttributeJpa.class);
  }

  /* see superclass */
  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   * @param component the component
   * @return the attribute
   * @throws Exception the exception
   */
  @Override
  public Attribute addAttribute(Attribute attribute,
    ComponentHasAttributes component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add attribute " + attribute);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(attribute.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + attribute.getTerminology());
      }
      String id = idHandler.getTerminologyId(attribute, component);
      attribute.setTerminologyId(id);
    }

    // Add component
    final Attribute newAttribute = addComponent(attribute);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.attributeChanged(newAttribute, WorkflowListener.Action.ADD);
      }
    }
    return newAttribute;
  }

  /* see superclass */
  /**
   * Update attribute.
   *
   * @param attribute the attribute
   * @param component the component
   * @throws Exception the exception
   */
  @Override
  public void updateAttribute(Attribute attribute,
    ComponentHasAttributes component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update attribute " + attribute);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(attribute.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Attribute attribute2 = getAttribute(attribute.getId());
        if (!idHandler.getTerminologyId(attribute, component).equals(
            idHandler.getTerminologyId(attribute2, component))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        attribute.setTerminologyId(idHandler.getTerminologyId(attribute,
            component));
      }
    }
    // update component
    this.updateComponent(attribute);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.attributeChanged(attribute, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the attribute.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove attribute " + id);
    // Remove the component
    final Attribute attribute = removeComponent(id, AttributeJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.attributeChanged(attribute, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Find concepts for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find concepts " + terminology + "/" + version + "/"
            + query);
    SearchResultList results =
        findForQueryHelper(terminology, version, branch, query, pfs,
            ConceptJpa.class, ConceptJpa.class);
    for (SearchResult result : results.getObjects()) {
      result.setType(IdType.CONCEPT);
    }
    return results;
  }

  /* see superclass */
  /**
   * Autocomplete concepts.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @return the string list
   * @throws Exception the exception
   */
  @Override
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete concepts " + terminology + ", "
            + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm,
        ConceptJpa.class);
  }

  /* see superclass */
  /**
   * Find descriptors for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptors " + terminology + "/" + version
            + "/" + query);
    SearchResultList results =
        findForQueryHelper(terminology, version, branch, query, pfs,
            DescriptorJpa.class, DescriptorJpa.class);
    for (SearchResult result : results.getObjects()) {
      result.setType(IdType.DESCRIPTOR);
    }
    return results;
  }

  /* see superclass */
  /**
   * Autocomplete descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @return the string list
   * @throws Exception the exception
   */
  @Override
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete descriptors " + terminology + ", "
            + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm,
        DescriptorJpa.class);
  }

  /**
   * Find for query helper.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  public <T extends AtomClass> SearchResultList findForQueryHelper(
    String terminology, String version, String branch, String query,
    PfsParameter pfs, Class<?> fieldNamesKey, Class<T> clazz) throws Exception {
    // Prepare results
    final SearchResultList results = new SearchResultListJpa();
    int totalCt[] = new int[1];

    // construct return lists for lucene and expression results
    List<T> luceneResults = null;
    SearchResultList exprResults = null;

    // construct local pfs
    PfsParameter localPfs = new PfsParameterJpa(pfs);

    // declare search handler
    SearchHandler searchHandler = null;

    if (pfs.getExpression() != null && !pfs.getExpression().isEmpty()) {

      // get the results
      ExpressionHandler exprHandler =
          getExpressionHandler(terminology, version);
      exprResults = exprHandler.resolve(pfs.getExpression());

      // if results found, constuct a query restriction
      if (exprResults.getCount() > 0) {
        String exprQueryRestr =
            (pfs.getQueryRestriction() != null
                && !pfs.getQueryRestriction().isEmpty() ? " AND " : "")
                + "terminologyId:(";
        for (SearchResult exprResult : exprResults.getObjects()) {
          exprQueryRestr += exprResult.getTerminologyId() + " ";
        }
        // trim last space, close parenthesis and add boost based on count
        exprQueryRestr =
            exprQueryRestr.substring(0, exprQueryRestr.length() - 1) + ")^"
                + exprResults.getCount();
        localPfs.setQueryRestriction((pfs.getQueryRestriction() != null ? pfs
            .getQueryRestriction() : "") + exprQueryRestr);
      }
    }

    // if an atom class, use atom class
    if (AbstractAtomClass.class.isAssignableFrom(clazz)) {
      searchHandler = getSearchHandler(ConfigUtility.ATOMCLASS);
    }

    // otherwise look for terminology specific handlers (this condition may be
    // impossible)
    else {
      searchHandler = getSearchHandler(terminology);
    }

    // if no expression, or expression with results, perform lucene query
    if (exprResults == null || exprResults.getCount() > 0) {
      luceneResults =
          searchHandler.getQueryResults(terminology, version, branch, query,
              "atoms.nameSort", fieldNamesKey, clazz, localPfs, totalCt,
              manager);
      Logger.getLogger(getClass()).debug(
          "    lucene result count = " + luceneResults.size());

      // set the total count
      results.setTotalCount(totalCt[0]);
    }

    // construct the search results (if any found)
    if (luceneResults != null) {
      Map<Long, Float> scoreMap = searchHandler.getScoreMap();
      for (T r : luceneResults) {
        SearchResult sr = new SearchResultJpa();
        sr.setId(r.getId());
        sr.setTerminology(r.getTerminology());
        sr.setVersion(r.getVersion());
        sr.setTerminologyId(r.getTerminologyId());
        sr.setValue(r.getName());
        sr.setScore(scoreMap.get(r.getId()));
        results.getObjects().add(sr);
      }
    }

    return results;

  }

  /**
   * Find for general query helper.
   *
   * @param <T> the
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private <T extends AtomClass> SearchResultList findForGeneralQueryHelper(
    String luceneQuery, String jqlQuery, String branch, PfsParameter pfs,
    Class<?> fieldNamesKey, Class<T> clazz) throws Exception {
    // Prepare results
    final SearchResultList results = new SearchResultListJpa();
    List<T> classes = null;
    final int totalCt[] = new int[1];

    // Perform Lucene search
    final List<T> luceneQueryClasses = new ArrayList<>();
    boolean luceneQueryFlag = false;
    if (luceneQuery != null && !luceneQuery.equals("")) {
      SearchHandler searchHandler = getSearchHandler("");
      luceneQueryClasses.addAll(searchHandler.getQueryResults(null, null,
          branch, luceneQuery, "atomsName.sort", fieldNamesKey, clazz, pfs,
          totalCt, manager));
      luceneQueryFlag = true;
    }

    boolean jqlQueryFlag = false;
    final List<T> jqlQueryClasses = new ArrayList<>();
    if (jqlQuery != null && !jqlQuery.equals("")) {
      if (!jqlQuery.toLowerCase().startsWith("select"))
        throw new Exception(
            "The jql query did not start with the keyword 'select'. "
                + jqlQuery);
      if (jqlQuery.contains(";"))
        throw new Exception("The jql query must not contain the ';'. "
            + jqlQuery);
      javax.persistence.Query hQuery = manager.createQuery(jqlQuery);

      // Support for this is probably in Mysql 5.7.4
      // See http://mysqlserverteam.com/server-side-select-statement-timeouts/
      // It doesn't work with Mysql 5.6, seems to simply be ignored
      hQuery.setHint("javax.persistence.query.timeout", queryTimeout);
      try {
        final List<T> jqlResults = hQuery.getResultList();
        for (final T r : jqlResults) {
          jqlQueryClasses.add(r);
        }
      } catch (ClassCastException e) {
        throw new Exception(
            "The jql query returned items of an unexpected type. ", e);
      }

      jqlQueryFlag = true;
    }

    // Determine whether both query and criteria were used, or just one or the
    // other

    // Start with query results if they exist
    if (luceneQueryFlag) {
      classes = luceneQueryClasses;
    }

    if (jqlQueryFlag) {

      if (luceneQueryFlag) {
        // Intersect the lucene and HQL results
        classes.retainAll(jqlQueryClasses);
      } else {
        // Otherwise, just use jql classes
        classes = jqlQueryClasses;
      }

      // Here we know the total size
      totalCt[0] = classes.size();

      // Apply PFS sorting manually
      if (pfs != null && pfs.getSortField() != null) {
        final Method getMethod =
            clazz.getMethod("get"
                + pfs.getSortField().substring(0, 1).toUpperCase()
                + pfs.getSortField().substring(1));
        if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        Collections.sort(classes, new Comparator<AtomClass>() {
          @SuppressWarnings("rawtypes")
          @Override
          public int compare(AtomClass o1, AtomClass o2) {
            try {
              final Comparable f1 =
                  (Comparable) getMethod.invoke(o1, new Object[] {});
              final Comparable f2 =
                  (Comparable) getMethod.invoke(o2, new Object[] {});
              return f1.compareTo(f2);
            } catch (Exception e) {
              // do nothing
            }
            return 0;
          }
        });
      }

      // Apply PFS paging manually
      if (pfs != null && pfs.getStartIndex() != -1) {
        int startIndex = pfs.getStartIndex();
        int toIndex = classes.size();
        toIndex = Math.min(toIndex, startIndex + pfs.getMaxResults());
        classes = classes.subList(startIndex, toIndex);
      }

    } else {
      // If criteria flag wasn't triggered, then PFS was already handled
      // by the query mechanism - which only applies PFS if criteria isn't
      // also used. Therefore, we are ready to go.

      // Manual PFS handling is in the section above.
    }

    // Some result has been found, even if empty
    if (classes == null)
      return results;

    // construct the search results
    for (final AtomClass atomClass : classes) {
      final SearchResult sr = new SearchResultJpa();
      sr.setId(atomClass.getId());
      sr.setTerminologyId(atomClass.getTerminologyId());
      sr.setTerminology(atomClass.getTerminology());
      sr.setVersion(atomClass.getVersion());
      sr.setValue(atomClass.getName());
      sr.setObsolete(atomClass.isObsolete());
      results.getObjects().add(sr);
    }

    results.setTotalCount(totalCt[0]);
    return results;

  }

  /**
   * Autocomplete helper.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param clazz the clazz
   * @return the string list
   * @throws Exception the exception
   */
  private <T extends AtomClass> StringList autocompleteHelper(
    String terminology, String version, String searchTerm, Class<T> clazz)
    throws Exception {

    if (terminology == null || version == null || searchTerm == null) {
      return new StringList();
    }
    final String TITLE_EDGE_NGRAM_INDEX = "atoms.edgeNGramName";
    final String TITLE_NGRAM_INDEX = "atoms.nGramName";

    final FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    final QueryBuilder titleQB =
        fullTextEntityManager.getSearchFactory().buildQueryBuilder()
            .forEntity(clazz).get();

    final Query query =
        titleQB.phrase().withSlop(2).onField(TITLE_NGRAM_INDEX)
            .andField(TITLE_EDGE_NGRAM_INDEX).boostedTo(5)
            .andField("atoms.name").boostedTo(5)
            .sentence(searchTerm.toLowerCase()).createQuery();

    final Query term1 = new TermQuery(new Term("terminology", terminology));
    final Query term2 = new TermQuery(new Term("version", version));
    final Query term3 = new TermQuery(new Term("atoms.suppressible", "false"));
    final Query term4 = new TermQuery(new Term("suppressible", "false"));
    final Query term5 = new TermQuery(new Term("anonymous", "false"));
    final BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(term1, BooleanClause.Occur.MUST);
    booleanQuery.add(term2, BooleanClause.Occur.MUST);
    booleanQuery.add(term3, BooleanClause.Occur.MUST);
    booleanQuery.add(term4, BooleanClause.Occur.MUST);
    booleanQuery.add(term5, BooleanClause.Occur.MUST);
    booleanQuery.add(query, BooleanClause.Occur.MUST);

    final FullTextQuery fullTextQuery =
        fullTextEntityManager.createFullTextQuery(booleanQuery, clazz);

    fullTextQuery.setMaxResults(20);

    @SuppressWarnings("unchecked")
    final List<AtomClass> results = fullTextQuery.getResultList();
    final StringList list = new StringList();
    list.setTotalCount(fullTextQuery.getResultSize());
    for (final AtomClass result : results) {
      // exclude duplicates
      if (!list.contains(result.getName()))
        list.getObjects().add(result.getName());
    }
    return list;
  }

  /* see superclass */
  /**
   * Find codes for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find codes " + terminology + "/" + version + "/"
            + query);
    SearchResultList results =
        findForQueryHelper(terminology, version, branch, query, pfs,
            CodeJpa.class, CodeJpa.class);
    for (SearchResult result : results.getObjects()) {
      result.setType(IdType.CODE);
    }
    return results;
  }

  /* see superclass */
  /**
   * Autocomplete codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @return the string list
   * @throws Exception the exception
   */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete codes " + terminology + ", " + version
            + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm, CodeJpa.class);
  }

  /* see superclass */
  /**
   * Returns the all concepts.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all concepts
   */
  @Override
  public ConceptList getAllConcepts(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all concepts " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from ConceptJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      final List<Concept> concepts = query.getResultList();
      final ConceptList conceptList = new ConceptListJpa();
      conceptList.setObjects(concepts);
      conceptList.setTotalCount(concepts.size());
      return conceptList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the all descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all descriptors
   */
  @Override
  public DescriptorList getAllDescriptors(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all descriptors " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from DescriptorJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");

      @SuppressWarnings("unchecked")
      final List<Descriptor> descriptors = query.getResultList();
      final DescriptorList descriptorList = new DescriptorListJpa();
      descriptorList.setObjects(descriptors);
      descriptorList.setTotalCount(descriptors.size());
      return descriptorList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the all codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all codes
   */
  @Override
  public CodeList getAllCodes(String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all codes " + terminology + "/" + version + "/"
            + branch);
    assert branch != null;

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from CodeJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      final List<Code> codes = query.getResultList();
      final CodeList codeList = new CodeListJpa();
      codeList.setObjects(codes);
      codeList.setTotalCount(codes.size());
      return codeList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Clear transitive closure.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  @Override
  public void clearTransitiveClosure(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - Clear transitive closure data for " + terminology
            + ", " + version);
    try {
      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.begin();
      }

      javax.persistence.Query query =
          manager.createQuery("DELETE From ConceptTransitiveRelationshipJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager
              .createQuery("DELETE From DescriptorTransitiveRelationshipJpa "
                  + " c where terminology = :terminology "
                  + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From CodeTransitiveRelationshipJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    CodeTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.commit();
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /* see superclass */
  /**
   * Clear tree positions.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  @Override
  public void clearTreePositions(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - Clear tree positions data for " + terminology + ", "
            + version);
    try {
      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.begin();
      }

      javax.persistence.Query query =
          manager.createQuery("DELETE From ConceptTreePositionJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From DescriptorTreePositionJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From CodeTreePositionJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    CodeTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.commit();
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /* see superclass */
  /**
   * Clear branch.
   *
   * @param branch the branch
   */
  @Override
  public void clearBranch(String branch) {
    // TBD
  }

  /* see superclass */
  /**
   * Returns the identifier assignment handler.
   *
   * @param terminology the terminology
   * @return the identifier assignment handler
   * @throws Exception the exception
   */
  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    if (idHandlerMap.containsKey(terminology)) {
      return idHandlerMap.get(terminology);
    }
    return idHandlerMap.get(ConfigUtility.DEFAULT);

  }

  /* see superclass */
  /**
   * Returns the compute preferred name handler.
   *
   * @param terminology the terminology
   * @return the compute preferred name handler
   * @throws Exception the exception
   */
  @Override
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception {
    if (pnHandlerMap.containsKey(terminology)) {
      return pnHandlerMap.get(terminology);
    }
    return pnHandlerMap.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  /**
   * Returns the computed preferred name.
   *
   * @param atomClass the atom class
   * @param list the list
   * @return the computed preferred name
   * @throws Exception the exception
   */
  @Override
  public String getComputedPreferredName(AtomClass atomClass,
    PrecedenceList list) throws Exception {
    try {
      ComputePreferredNameHandler handler =
          pnHandlerMap.get(atomClass.getTerminology());
      // look for default if null
      if (handler == null) {
        handler = pnHandlerMap.get(ConfigUtility.DEFAULT);
      }
      if (handler == null) {
        throw new Exception(
            "Compute preferred name handler is not configured for DEFAULT or for "
                + atomClass.getTerminology());
      }
      final String pn =
          handler.computePreferredName(atomClass.getAtoms(), list);
      return pn;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  /**
   * Returns the normalized string.
   *
   * @param string the string
   * @return the normalized string
   * @throws Exception the exception
   */
  @Override
  public String getNormalizedString(String string) throws Exception {
    return normalizedStringHandler.getNormalizedString(string);
  }

  /* see superclass */
  /**
   * Indicates whether or not last modified flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /* see superclass */
  /**
   * Sets the last modified flag.
   *
   * @param lastModifiedFlag the last modified flag
   */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

  /* see superclass */
  /**
   * Sets the assign identifiers flag.
   *
   * @param assignIdentifiersFlag the assign identifiers flag
   */
  @Override
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag) {
    this.assignIdentifiersFlag = assignIdentifiersFlag;
  }

  /* see superclass */
  /**
   * Returns the component stats.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the component stats
   * @throws Exception the exception
   */
  @Override
  public Map<String, Integer> getComponentStats(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - getComponentStats");
    assert branch != null;
    final Map<String, Integer> stats = new TreeMap<>();
    for (final EntityType<?> type : manager.getMetamodel().getEntities()) {
      final String jpaTable = type.getName();
      // Logger.getLogger(getClass()).debug(" jpaTable = " + jpaTable);
      // Skip audit trail tables
      if (jpaTable.toUpperCase().indexOf("_AUD") != -1) {
        continue;
      }
      if (!AbstractAbbreviation.class.isAssignableFrom(type
          .getBindableJavaType())
          && !AbstractComponent.class.isAssignableFrom(type
              .getBindableJavaType())) {
        continue;
      }
      Logger.getLogger(getClass()).info("  " + jpaTable);
      javax.persistence.Query query = null;
      if (terminology != null) {
        query =
            manager.createQuery("select count(*) from " + jpaTable
                + " where terminology = :terminology "
                + "and version = :version ");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);

      } else {
        query = manager.createQuery("select count(*) from " + jpaTable);
      }
      int ct = ((Long) query.getSingleResult()).intValue();
      stats.put("Total " + jpaTable, ct);

      // Only compute active counts for components
      if (AbstractComponent.class.isAssignableFrom(type.getBindableJavaType())) {
        if (terminology != null) {

          query =
              manager.createQuery("select count(*) from " + jpaTable
                  + " where obsolete = 0 and terminology = :terminology "
                  + "and version = :version ");
          query.setParameter("terminology", terminology);
          query.setParameter("version", version);
        } else {
          query = manager.createQuery("select count(*) from " + jpaTable);
        }
        ct = ((Long) query.getSingleResult()).intValue();
        stats.put("Non-obsolete " + jpaTable, ct);
      }
    }
    return stats;
  }

  /**
   * Returns the components.
   *
   * @param <T> the
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param clazz the clazz
   * @return the components
   */
  @SuppressWarnings("rawtypes")
  private <T extends Component> List getComponents(String terminologyId,
    String terminology, String version, Class<T> clazz) {
    try {
      final javax.persistence.Query query =
          manager
              .createQuery("select a from "
                  + clazz.getName()
                  + " a where terminologyId = :terminologyId and version = :version and terminology = :terminology");
      query.setParameter("terminologyId", terminologyId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Adds the component.
   *
   * @param <T> the
   * @param component the component
   * @return the t
   * @throws Exception the exception
   */
  private <T extends Component> T addComponent(T component) throws Exception {
    try {
      // Set last modified date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(component);
        tx.commit();
      } else {
        manager.persist(component);
      }
      return component;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Update component.
   *
   * @param <T> the
   * @param component the component
   * @throws Exception the exception
   */
  private <T extends Component> void updateComponent(T component)
    throws Exception {
    try {
      // Set modification date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(component);
        tx.commit();
      } else {
        manager.merge(component);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /**
   * Returns the component.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the component
   * @throws Exception the exception
   */
  private <T extends Component> T getComponent(Long id, Class<T> clazz)
    throws Exception {
    // Get transaction and object
    tx = manager.getTransaction();
    T component = manager.find(clazz, id);
    return component;
  }

  /**
   * Returns the component.
   *
   * @param <T> the
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param clazz the clazz
   * @return the component
   */
  @SuppressWarnings("unchecked")
  private <T extends Component> T getComponent(String terminologyId,
    String terminology, String version, String branch, Class<T> clazz) {

    final List<T> results =
        getComponents(terminologyId, terminology, version, clazz);
    if (results.isEmpty()) {
      Logger.getLogger(getClass()).debug("  no " + clazz.getName());
      return null;
    }
    T defaultBranch = null;
    for (final T obj : results) {
      // handle default case
      if (obj.getBranch().equals(Branch.ROOT)) {
        defaultBranch = obj;
      }
      if (obj.getBranch().equals(branch)) {
        return obj;
      }
    }
    // If no matching branch is found, use default
    if (defaultBranch != null) {
      return defaultBranch;
    }
    // If nothing found, return null;
    return null;
  }

  /**
   * Removes the component.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  private <T extends Component> T removeComponent(Long id, Class<T> clazz)
    throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      T component = manager.find(clazz, id);

      // Set modification date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(component)) {
          manager.remove(component);
        } else {
          manager.remove(manager.merge(component));
        }
        tx.commit();
      } else {
        if (manager.contains(component)) {
          manager.remove(component);
        } else {
          manager.remove(manager.merge(component));
        }
      }
      return component;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  /**
   * Find relationships for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  @Override
  public RelationshipList findRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for concept " + conceptId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query + "/"
            + inverseFlag);

    return findRelationshipsForComponentHelper(conceptId, terminology, version,
        branch, query, inverseFlag, pfs, ConceptRelationshipJpa.class);
  }

  /* see superclass */
  /**
   * Find deep relationships for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param filter the filter
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public RelationshipList findDeepRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, String filter,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find deep relationships for concept " + conceptId
            + "/" + terminology + "/" + version + "/" + filter);

    if (pfs != null && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }

    try {

      final Concept concept =
          getConcept(conceptId, terminology, version, branch);
      final List<Object[]> results = new ArrayList<>();
      String queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, "
              + (inverseFlag ? "a.from.terminologyId" : "a.to.terminologyId")
              + ", a.obsolete, a.suppressible, a.published, a.publishable, "
              + (inverseFlag ? "a.from.name " : "a.to.name ")
              + "from ConceptRelationshipJpa a " + "where "
              + (inverseFlag ? "a.to" : "a.from") + ".id = :conceptId ";
      javax.persistence.Query query = manager.createQuery(queryStr);
      query.setParameter("conceptId", concept.getId());
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable, "
              + (inverseFlag ? "a.from.name " : "a.to.name ")
              + "from AtomRelationshipJpa a join "
              + (inverseFlag ? "a.from.conceptTerminologyIds"
                  : "a.to.conceptTerminologyIds") + " cui2 "
              + "where key(cui2) = '" + concept.getTerminology() + "' and "
              + (inverseFlag ? "a.to" : "a.from") + ".id in (:atomIds) ";
      query = manager.createQuery(queryStr);
      final Set<Long> atomIds = new HashSet<>();
      for (final Atom atom : concept.getAtoms()) {
        atomIds.add(atom.getId());
      }
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable, "
              + (inverseFlag ? "a.from.name " : "a.to.name ")
              + "from DescriptorRelationshipJpa a, DescriptorJpa b, AtomJpa c, "
              + "DescriptorJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.descriptorId "
              + "and b.terminology = c.terminology "
              + "and b.version = c.version "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.descriptorId "
              + "and d.terminology = e.terminology "
              + "and d.version = e.version " + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable, "
              + (inverseFlag ? "a.from.name " : "a.to.name ")
              + "from ConceptRelationshipJpa a, ConceptJpa b, AtomJpa c, "
              + "ConceptJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.conceptId "
              + "and b.terminology = c.terminology "
              + "and b.version = c.version "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.conceptId "
              + "and d.terminology = e.terminology "
              + "and d.version = e.version " + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable, "
              + (inverseFlag ? "a.from.name " : "a.to.name ")
              + "from CodeRelationshipJpa a, CodeJpa b, AtomJpa c, "
              + "CodeJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.codeId "
              + "and b.terminology = c.terminology "
              + "and b.version = c.version "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.codeId "
              + "and d.terminology = e.terminology "
              + "and d.version = e.version " + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      // Use a set to "uniq" them
      final Set<ConceptRelationship> conceptRels = new HashSet<>();
      for (final Object[] result : results) {
        final ConceptRelationship relationship = new ConceptRelationshipJpa();
        final Concept toConcept = new ConceptJpa();
        toConcept.setTerminology(concept.getTerminology());
        toConcept.setVersion(concept.getVersion());
        toConcept.setTerminologyId(result[6].toString());
        toConcept.setName(result[11].toString());
        relationship.setId(Long.parseLong(result[0].toString()));
        relationship.setFrom(concept);
        relationship.setTerminologyId(result[1].toString());
        relationship.setTerminology(result[2].toString());
        relationship.setVersion(result[3].toString());
        relationship.setRelationshipType(result[4].toString());
        relationship.setHierarchical(result[4].toString().equals("CHD")
            || result[4].toString().equals("subClassOf"));
        relationship.setAdditionalRelationshipType(result[5].toString());
        relationship.setObsolete(result[7].toString().equals("1"));
        relationship.setSuppressible(result[8].toString().equals("1"));
        relationship.setPublished(result[9].toString().equals("1"));
        relationship.setPublishable(result[10].toString().equals("1"));
        relationship.setTo(toConcept);
        conceptRels.add(relationship);
      }
      List<ConceptRelationship> conceptRelList = new ArrayList<>(conceptRels);

      // set filter as query restriction for use in applyPfsToList
      final PfsParameter pfsLocal = new PfsParameterJpa(pfs);
      pfsLocal.setQueryRestriction(filter);

      final int[] totalCt = new int[1];
      conceptRelList =
          this.applyPfsToList(conceptRelList, ConceptRelationship.class,
              totalCt, pfsLocal);

      RelationshipList list = new RelationshipListJpa();
      list.setTotalCount(totalCt[0]);
      for (final ConceptRelationship cr : conceptRelList) {
        list.getObjects().add(cr);
      }

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Find relationships for descriptor.
   *
   * @param descriptorId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  @Override
  public RelationshipList findRelationshipsForDescriptor(String descriptorId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for descriptor " + descriptorId
            + "/" + terminology + "/" + version + "/" + branch + "/" + query
            + "/" + inverseFlag);

    return findRelationshipsForComponentHelper(descriptorId, terminology,
        version, branch, query, inverseFlag, pfs,
        DescriptorRelationshipJpa.class);

  }

  /* see superclass */
  /**
   * Find relationships for code.
   *
   * @param codeId the code id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  @Override
  public RelationshipList findRelationshipsForCode(String codeId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for code " + codeId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query + "/"
            + inverseFlag);

    return findRelationshipsForComponentHelper(codeId, terminology, version,
        branch, query, inverseFlag, pfs, CodeRelationshipJpa.class);

  }

  /**
   * Find relationships helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the relationship list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private RelationshipList findRelationshipsForComponentHelper(
    String terminologyId, String terminology, String version, String branch,
    String query, boolean inverseFlag, PfsParameter pfs,
    Class<? extends Relationship> clazz) throws Exception {

    if (terminologyId == null || terminologyId.isEmpty()) {
      throw new Exception("Terminology id is required");
    }

    final RelationshipList results = new RelationshipListJpa();

    // Prepare the query string
    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }

    // add id/terminology/version constraints based on inverse flag
    if (inverseFlag == true) {
      finalQuery.append("toTerminologyId:" + terminologyId
          + " AND toTerminology:" + terminology + " AND toVersion:" + version);
    } else {
      finalQuery.append("fromTerminologyId:" + terminologyId
          + " AND fromTerminology:" + terminology + " AND fromVersion:"
          + version);
    }

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    // pass empty terminology/version because it's handled above
    results.setObjects((List) searchHandler.getQueryResults("", "", branch,
        finalQuery.toString(), "toNameSort", ConceptRelationshipJpa.class,
        clazz, pfs, totalCt, manager));
    results.setTotalCount(totalCt[0]);

    for (final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : results
        .getObjects()) {
      getGraphResolutionHandler(terminology).resolve(rel);
    }
    return results;

  }

  /**
   * Find mappings for component helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private MappingList findMappingsForComponentHelper(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {

    if (terminologyId == null || terminologyId.isEmpty()) {
      throw new Exception("Terminology id is required");
    }

    final MappingList results = new MappingListJpa();

    // Prepare the query string
    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }

    finalQuery
        .append("fromTerminologyId:" + terminologyId + " AND fromTerminology:"
            + terminology + " AND fromVersion:" + version);

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    // pass empty terminology/version because it's handled above
    results.setObjects((List) searchHandler.getQueryResults("", "", branch,
        finalQuery.toString(), "fromNameSort", MappingJpa.class,
        MappingJpa.class, pfs, totalCt, manager));
    results.setTotalCount(totalCt[0]);

    for (final Mapping mapping : results.getObjects()) {
      getGraphResolutionHandler(terminology).resolve(mapping);
    }
    return results;

  }

  /* see superclass */
  /**
   * Find tree positions for concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findTreePositionsForConcept(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find tree positions for concept " + terminologyId
            + "/" + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        "", pfs, ConceptTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find tree positions for descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findTreePositionsForDescriptor(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find tree positionss for descriptor "
            + terminologyId + "/" + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        "", pfs, DescriptorTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find tree positions for code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findTreePositionsForCode(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find tree positions for code " + terminologyId + "/"
            + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        "", pfs, CodeTreePositionJpa.class);

  }

  /**
   * Find tree positions helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the tree position list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private TreePositionList findTreePositionsHelper(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs, Class<? extends TreePosition> clazz) throws Exception {

    // Prepare the query string
    final StringBuilder finalQuery = new StringBuilder();

    // add the query, if not null and not empty
    finalQuery.append(query == null || query.isEmpty() ? "" : query);
    if (terminologyId != null) {
      if (finalQuery.length() > 0) {
        finalQuery.append(" AND ");
      }
      finalQuery.append("nodeTerminologyId:" + terminologyId);
    }

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    final TreePositionList list = new TreePositionListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, finalQuery.toString(), "nodeNameSort",
        ConceptTreePositionJpa.class, clazz, pfs, totalCt, manager));
    list.setTotalCount(totalCt[0]);

    // If the list has <30 entries and all are roman numerals
    // and the sortField is "nodeTerminologyId" then use a roman numeral sort
    // This is a hack for roman numeral sorted top-level hierarchies
    if (list.getCount() < 30 && pfs != null && pfs.getSortField() != null
        && pfs.getSortField().equals("nodeTerminologyId")) {
      boolean nonRomanFound = false;
      for (final TreePosition treepos : list.getObjects()) {
        if (!ConfigUtility.isRomanNumeral(treepos.getNode().getTerminologyId())) {
          nonRomanFound = true;
          break;
        }
      }
      if (!nonRomanFound) {
        Collections.sort(list.getObjects(), new Comparator<TreePosition>() {
          @Override
          public int compare(TreePosition o1, TreePosition o2) {
            try {
              return ConfigUtility.toArabic(o1.getNode().getTerminologyId())
                  - ConfigUtility.toArabic(o2.getNode().getTerminologyId());
            } catch (Exception e) {
              // just return zero, don't worry about handling the error
              e.printStackTrace();
              return 0;
            }
          }

        });
      }
    }
    return list;

  }

  /* see superclass */
  /**
   * Find codes for general query.
   *
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  @Override
  public SearchResultList findCodesForGeneralQuery(String luceneQuery,
    String jqlQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find codes " + luceneQuery + "/" + jqlQuery + "/");
    return findForGeneralQueryHelper(luceneQuery, jqlQuery, branch, pfs,
        CodeJpa.class, CodeJpa.class);
  }

  /* see superclass */
  /**
   * Find concepts for general query.
   *
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  @Override
  public SearchResultList findConceptsForGeneralQuery(String luceneQuery,
    String jqlQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info(
            "Content Service - find concepts " + luceneQuery + "/" + jqlQuery
                + "/");
    return findForGeneralQueryHelper(luceneQuery, jqlQuery, branch, pfs,
        ConceptJpa.class, ConceptJpa.class);
  }

  /* see superclass */
  /**
   * Find descriptors for general query.
   *
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  @Override
  public SearchResultList findDescriptorsForGeneralQuery(String luceneQuery,
    String jqlQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptors " + luceneQuery + "/" + jqlQuery
            + "/");
    return findForGeneralQueryHelper(luceneQuery, jqlQuery, branch, pfs,
        DescriptorJpa.class, DescriptorJpa.class);
  }

  /* see superclass */
  /**
   * Returns the tree for tree position.
   *
   * @param treePosition the tree position
   * @return the tree for tree position
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Override
  public Tree getTreeForTreePosition(
    TreePosition<? extends AtomClass> treePosition) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - get tree for tree position");

    Long tpId = treePosition.getNode().getId();

    // Determine type
    Class<?> clazz = null;
    if (manager.find(ConceptJpa.class, tpId) != null) {
      clazz = ConceptTreePositionJpa.class;
    } else if (manager.find(DescriptorJpa.class, tpId) != null) {
      clazz = DescriptorTreePositionJpa.class;
    } else if (manager.find(CodeJpa.class, tpId) != null) {
      clazz = CodeTreePositionJpa.class;
    } else {
      throw new Exception("Unknown tree position type.");
    }
    Logger.getLogger(getClass()).debug("  type = " + clazz.getName());

    // tree to return
    Tree tree = null;

    // the current tree variables (ancestor path and local tree)
    String partAncPath = ""; // initially top-level
    Tree parentTree = tree; // initially the empty tree

    // Prepare lucene
    final FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    final SearchFactory searchFactory =
        fullTextEntityManager.getSearchFactory();
    final QueryParser queryParser =
        new MultiFieldQueryParser(IndexUtility.getIndexedFieldNames(
            ConceptTreePositionJpa.class, true).toArray(new String[] {}),
            searchFactory.getAnalyzer(clazz));
    final String fullAncPath =
        treePosition.getAncestorPath()
            + (treePosition.getAncestorPath().isEmpty() ? "" : "~") + tpId;

    // Iterate over ancestor path
    for (final String pathPart : fullAncPath.split("~")) {
      final Long partId = Long.parseLong(pathPart);

      final StringBuilder finalQuery = new StringBuilder();
      finalQuery.append("nodeId:" + partId + " AND ");
      if (partAncPath.isEmpty()) {
        // query for empty value
        finalQuery.append("-ancestorPath:[* TO *]");
      } else {
        finalQuery.append("ancestorPath:\"" + partAncPath + "\"");
      }
      // Prepare the manager and lucene query
      final Query luceneQuery = queryParser.parse(finalQuery.toString());
      final FullTextQuery fullTextQuery =
          fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);

      // // projection approach -- don't want to have to instantiate node Jpa
      // object
      // fullTextQuery.setProjection("nodeId", "nodeTerminologyId", "nodeName",
      // "childCt", "ancestorPath");
      //
      // List<Object[]> results = fullTextQuery.getResultList();
      //
      // if (fullTextQuery.getResultSize() != 1) {
      // throw new Exception("Unexpected number of results: "
      // + fullTextQuery.getResultSize());
      // }
      // Object[] result = results.get(0);
      //
      // // fill in the tree object
      // partTree.setId((Long) result[0]);
      // partTree.setTerminologyId((String) result[1]);
      // partTree.setName((String) result[2]);
      // partTree.setChildCt((Integer) result[3]);
      // partTree.setAncestorPath((String) result[4]);
      // partTree.setTerminology(treePosition.getTerminology());
      // partTree.setVersion(treePosition.getVersion());

      // original approach
      if (fullTextQuery.getResultSize() != 1) {
        throw new Exception("Unexpected number of results: "
            + fullTextQuery.getResultSize());
      }

      final TreePosition<? extends AtomClass> treepos =
          (TreePosition<? extends AtomClass>) fullTextQuery.getResultList()
              .get(0);

      final Tree partTree = new TreeJpa(treepos);

      if (tree == null) {
        tree = partTree;
      }

      if (parentTree != null) {
        parentTree.addChild(partTree);
      }

      // set parent tree to the just constructed
      parentTree = partTree;

      partAncPath += (partAncPath.equals("") ? "" : "~");
      partAncPath += pathPart;
    }

    return tree;
  }

  /* see superclass */
  /**
   * Find concept tree positions for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findConceptTreePositionsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find concept tree positions " + terminology + "/"
            + version + "/" + query);
    return this.findTreePositionsHelper(null, terminology, version, branch,
        query, pfs, ConceptTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find descriptor tree positions for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findDescriptorTreePositionsForQuery(
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptor tree positions " + terminology + "/"
            + version + "/" + query);
    return this.findTreePositionsHelper(null, terminology, version, branch,
        query, pfs, DescriptorTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find code tree positions for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findCodeTreePositionsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find code tree positions " + terminology + "/"
            + version + "/" + query);
    return this.findTreePositionsHelper(null, terminology, version, branch,
        query, pfs, CodeTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find concept tree position children.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findConceptTreePositionChildren(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "Content Service - find children of a concept tree position "
            + terminologyId + "/" + terminology + "/" + version);
    return getTreePositionChildrenHelper(terminologyId, terminology, version,
        branch, pfs, ConceptTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find descriptor tree position children.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findDescriptorTreePositionChildren(
    String terminologyId, String terminology, String version, String branch,
    PfsParameter pfs) throws Exception {

    Logger.getLogger(getClass()).info(
        "Content Service - find children of a descriptor tree position "
            + terminology + "/" + version);
    return getTreePositionChildrenHelper(terminologyId, terminology, version,
        branch, pfs, DescriptorTreePositionJpa.class);
  }

  /* see superclass */
  /**
   * Find code tree position children.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @Override
  public TreePositionList findCodeTreePositionChildren(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "Content Service - find children of a code tree position "
            + terminology + "/" + version);

    return getTreePositionChildrenHelper(terminologyId, terminology, version,
        branch, pfs, CodeTreePositionJpa.class);
  }

  /**
   * Returns the child tree positions helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the child tree positions helper
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private TreePositionList getTreePositionChildrenHelper(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs,
    Class<? extends TreePosition> clazz) throws Exception {

    final PfsParameter childPfs = new PfsParameterJpa();
    childPfs.setStartIndex(0);
    childPfs.setMaxResults(1);
    // get a tree position for each child, for child ct
    TreePositionList tpList =
        findTreePositionsHelper(terminologyId, terminology, version, branch,
            "", childPfs, clazz);

    if (tpList.getCount() == 0) {
      return new TreePositionListJpa();
    }
    final TreePosition<? extends AtomClass> treePosition =
        tpList.getObjects().get(0);

    final Long tpId = treePosition.getNode().getId();
    final String fullAncPath =
        treePosition.getAncestorPath()
            + (treePosition.getAncestorPath().isEmpty() ? "" : "~") + tpId;

    final String query = "ancestorPath:\"" + fullAncPath + "\"";

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];

    final TreePositionList list = new TreePositionListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, query, null, ConceptTreePositionJpa.class,
        ConceptTreePositionJpa.class, pfs, totalCt, manager));
    list.setTotalCount(totalCt[0]);

    // If the list has <30 entries and all are roman numerals
    // and the sortField is "nodeTerminologyId" then use a roman numeral sort
    if (list.getCount() < 30 && pfs != null && pfs.getSortField() != null
        && pfs.getSortField().equals("nodeTerminologyId")) {
      boolean nonRomanFound = false;
      for (final TreePosition treepos : list.getObjects()) {
        if (!ConfigUtility.isRomanNumeral(treepos.getNode().getTerminologyId())) {
          nonRomanFound = true;
          break;
        }
      }
      if (!nonRomanFound) {
        Collections.sort(list.getObjects(), new Comparator<TreePosition>() {
          @Override
          public int compare(TreePosition o1, TreePosition o2) {
            try {
              return ConfigUtility.toArabic(o1.getNode().getTerminologyId())
                  - ConfigUtility.toArabic(o2.getNode().getTerminologyId());
            } catch (Exception e) {
              // just return zero, don't worry about handling the error
              e.printStackTrace();
              return 0;
            }
          }

        });
      }
    }

    return list;
  }

  /* see superclass */
  /**
   * Adds the general concept axiom.
   *
   * @param axiom the axiom
   * @return the general concept axiom
   * @throws Exception the exception
   */
  @Override
  public GeneralConceptAxiom addGeneralConceptAxiom(GeneralConceptAxiom axiom)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add axiom " + axiom);
    // No need to worry about assigning ids.

    final GeneralConceptAxiom newAxiom = addComponent(axiom);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.conceptChanged(newAxiom.getLeftHandSide(),
            WorkflowListener.Action.ADD);
        listener.conceptChanged(newAxiom.getRightHandSide(),
            WorkflowListener.Action.ADD);
      }
    }
    return newAxiom;
  }

  /* see superclass */
  /**
   * Update general concept axiom.
   *
   * @param axiom the axiom
   * @throws Exception the exception
   */
  @Override
  public void updateGeneralConceptAxiom(GeneralConceptAxiom axiom)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update axiom " + axiom);
    // update component
    this.updateComponent(axiom);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.conceptChanged(axiom.getLeftHandSide(),
            WorkflowListener.Action.ADD);
        listener.conceptChanged(axiom.getRightHandSide(),
            WorkflowListener.Action.ADD);
      }
    }

  }

  /* see superclass */
  /**
   * Removes the general concept axiom.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeGeneralConceptAxiom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove axiom " + id);

    removeComponent(id, GeneralConceptAxiomJpa.class);

  }

  /* see superclass */
  /**
   * Returns the general concept axioms.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the general concept axioms
   * @throws Exception the exception
   */
  @Override
  public GeneralConceptAxiomList getGeneralConceptAxioms(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get general concept axioms " + terminology + "/"
            + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from GeneralConceptAxiomJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<GeneralConceptAxiom> m = query.getResultList();
      final GeneralConceptAxiomListJpa generalConceptAxiomList =
          new GeneralConceptAxiomListJpa();
      generalConceptAxiomList.setObjects(m);
      generalConceptAxiomList.setTotalCount(m.size());

      return generalConceptAxiomList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Returns the search handler.
   *
   * @param key the key
   * @return the search handler
   * @throws Exception the exception
   */
  @Override
  public SearchHandler getSearchHandler(String key) throws Exception {
    if (searchHandlers.containsKey(key)) {
      return searchHandlers.get(key);
    }
    if (searchHandlerNames.contains(key)) {
      // Add handlers to map
      SearchHandler searchHandler =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(
              "search.handler", key, SearchHandler.class);
      searchHandlers.put(key, searchHandler);
      return searchHandler;
    }
    final SearchHandler searchHandler =
        ConfigUtility.newStandardHandlerInstanceWithConfiguration(
            "search.handler", ConfigUtility.DEFAULT, SearchHandler.class);
    searchHandlers.put(key, searchHandler);
    return searchHandler;
  }

  /**
   * Returns the expression handler.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the expression handler
   * @throws Exception the exception
   */
  @Override
  public ExpressionHandler getExpressionHandler(String terminology,
    String version) throws Exception {

    // NOTE: Only ECL expression searching currently supported.
    return new EclExpressionHandler(terminology, version);
  }

  /* see superclass */
  /**
   * Adds the mapping.
   *
   * @param mapping the mapping
   * @return the mapping
   * @throws Exception the exception
   */
  @Override
  public Mapping addMapping(Mapping mapping) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add mapping " + mapping);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(mapping.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + mapping.getTerminology());
      }
      String id = idHandler.getTerminologyId(mapping);
      mapping.setTerminologyId(id);
    }

    // Add component
    final Mapping newMapping = addComponent(mapping);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.mappingChanged(newMapping, WorkflowListener.Action.ADD);
      }
    }
    return newMapping;
  }

  /* see superclass */
  /**
   * Update mapping.
   *
   * @param mapping the mapping
   * @throws Exception the exception
   */
  @Override
  public void updateMapping(Mapping mapping) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update mapping " + mapping);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(mapping.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Mapping mapping2 = getMapping(mapping.getId());
        if (!idHandler.getTerminologyId(mapping).equals(
            idHandler.getTerminologyId(mapping2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set mapping id on update
        mapping.setTerminologyId(idHandler.getTerminologyId(mapping));
      }
    }
    // update component
    this.updateComponent(mapping);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.mappingChanged(mapping, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the mapping.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeMapping(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove mapping " + id);
    // Remove the component
    final Mapping mapping = removeComponent(id, MappingJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.mappingChanged(mapping, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the map set.
   *
   * @param id the id
   * @return the map set
   * @throws Exception the exception
   */
  @Override
  public MapSet getMapSet(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get mapSet " + id);
    return getComponent(id, MapSetJpa.class);
  }

  /* see superclass */
  /**
   * Returns the map set.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the map set
   * @throws Exception the exception
   */
  @Override
  public MapSet getMapSet(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get mapset " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        MapSetJpa.class);
  }

  /* see superclass */
  /**
   * Returns the map sets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the map sets
   * @throws Exception the exception
   */
  @Override
  public MapSetList getMapSets(String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get mapsets " + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from MapSetJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<MapSet> m = query.getResultList();
      final MapSetListJpa mapSetList = new MapSetListJpa();
      mapSetList.setObjects(m);
      mapSetList.setTotalCount(m.size());

      return mapSetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  /**
   * Returns the mapping.
   *
   * @param id the id
   * @return the mapping
   * @throws Exception the exception
   */
  @Override
  public Mapping getMapping(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get mapping " + id);
    return getComponent(id, MappingJpa.class);
  }

  /* see superclass */
  /**
   * Returns the mapping.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the mapping
   * @throws Exception the exception
   */
  @Override
  public Mapping getMapping(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get mapping " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        MappingJpa.class);
  }

  /* see superclass */
  /**
   * Find mappings for map set.
   *
   * @param mapSetId the map set id
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public MappingList findMappingsForMapSet(Long mapSetId, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find mappings " + mapSetId + ", query=" + query);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (mapSetId == null) {
      sb.append("mapSetId:[* TO *]");
    } else {
      sb.append("mapSetId:" + mapSetId);
    }

    int[] totalCt = new int[1];
    final List<Mapping> list =
        (List<Mapping>) getQueryResults(sb.toString(), MappingJpa.class,
            MappingJpa.class, pfs, totalCt);
    final MappingList result = new MappingListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  /**
   * Find mappings for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  @Override
  public MappingList findMappingsForConcept(String conceptId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find mappings for concept " + conceptId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query);

    return findMappingsForComponentHelper(conceptId, terminology, version,
        branch, query, pfs);
  }

  /* see superclass */
  /**
   * Find mappings for code.
   *
   * @param codeId the code id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  @Override
  public MappingList findMappingsForCode(String codeId, String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find mappings for code " + codeId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query);

    return findMappingsForComponentHelper(codeId, terminology, version, branch,
        query, pfs);
  }

  /* see superclass */
  /**
   * Find mappings for descriptor.
   *
   * @param descriptorId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  @Override
  public MappingList findMappingsForDescriptor(String descriptorId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find mappings for descriptor " + descriptorId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query);

    return findMappingsForComponentHelper(descriptorId, terminology, version,
        branch, query, pfs);
  }

  /* see superclass */
  /**
   * Adds the map set.
   *
   * @param mapSet the map set
   * @return the map set
   * @throws Exception the exception
   */
  @Override
  public MapSet addMapSet(MapSet mapSet) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add mapSet " + mapSet);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(mapSet.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + mapSet.getTerminology());
      }
      String id = idHandler.getTerminologyId(mapSet);
      mapSet.setTerminologyId(id);
    }

    // Add component
    final MapSet newMapSet = addComponent(mapSet);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.mapSetChanged(newMapSet, WorkflowListener.Action.ADD);
      }
    }
    return newMapSet;
  }

  /* see superclass */
  /**
   * Update map set.
   *
   * @param mapSet the map set
   * @throws Exception the exception
   */
  @Override
  public void updateMapSet(MapSet mapSet) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update mapSet " + mapSet);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(mapSet.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        MapSet mapSet2 = getMapSet(mapSet.getId());
        if (!idHandler.getTerminologyId(mapSet).equals(
            idHandler.getTerminologyId(mapSet2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set mapSet id on update
        mapSet.setTerminologyId(idHandler.getTerminologyId(mapSet));
      }
    }
    // update component
    this.updateComponent(mapSet);

    // Inform listeners
    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.mapSetChanged(mapSet, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /* see superclass */
  /**
   * Removes the map set.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeMapSet(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove mapSet " + id);
    // Remove the component
    final MapSet mapSet = removeComponent(id, MapSetJpa.class);

    if (listenersEnabled) {
      for (final WorkflowListener listener : listeners) {
        listener.mapSetChanged(mapSet, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /* see superclass */
  /**
   * Returns the terminology id map.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the terminology id map
   * @throws Exception the exception
   */
  @Override
  public Map<Long, String> getTerminologyIdMap(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get id to terminology id map " + terminology + "/"
            + version);

    Terminology terminologyObj = getTerminology(terminology, version);

    String tableName = null;
    switch (terminologyObj.getOrganizingClassType()) {
      case CODE:
        tableName = "CodeJpa";
        break;
      case CONCEPT:
        tableName = "ConceptJpa";
        break;
      case DESCRIPTOR:
        tableName = "DescriptorJpa";
        break;
      default:
        throw new Exception(
            "Could not determine organizing class type for terminology");

    }

    javax.persistence.Query query =
        manager
            .createQuery("select a.id, a.terminologyId from " + tableName
                + " a where "
                + "version = :version and terminology = :terminology");

    Map<Long, String> idMap = new HashMap<>();

    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      List<Object[]> rows = query.getResultList();
      for (Object[] row : rows) {
        idMap.put((Long) row[0], row[1].toString());
      }

      return idMap;

    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Returns the note.
   *
   * @param id the id
   * @param noteClass the note class
   * @return the note
   * @throws Exception the exception
   */
  @Override
  public Note getNote(Long id, Class<? extends Note> noteClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get tree position " + id);
    tx = manager.getTransaction();
    Note note = null;
    if (noteClass != null) {
      note = manager.find(noteClass, id);

    } else {
      note = manager.find(ConceptNoteJpa.class, id);
      if (note == null) {
        note = manager.find(CodeNoteJpa.class, id);
      }
      if (note == null) {
        note = manager.find(DescriptorNoteJpa.class, id);
      }
    }
    return note;
  }

  /**
   * Adds the note.
   *
   * @param note the note
   * @return the note
   * @throws Exception the exception
   */
  @Override
  public Note addNote(Note note) throws Exception {

    Logger.getLogger(getClass()).debug(
        "Content Service - add userNote " + note.toString());

    // Add component
    Note newNote = addHasLastModified(note);

    // do not inform listeners
    return newNote;
  }

  /* see superclass */
  /**
   * Removes the note.
   *
   * @param id the id
   * @param type the type
   * @throws Exception the exception
   */
  @Override
  public void removeNote(Long id, Class<? extends Note> type) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove userNote " + id);
    // Remove the note
    removeHasLastModified(id, type);

  }

  /**
   * Find component infos for query.
   *
   * @param userName the user name
   * @param terminology the terminology
   * @param version the version
   * @param queryStr the query str
   * @param pfs the pfs
   * @return the component info list
   * @throws Exception the exception
   */
  @Override
  public ComponentInfoList findComponentInfosForQuery(String userName,
    String terminology, String version, String queryStr, PfsParameter pfs)
    throws Exception {
    ComponentInfoList favorites = new ComponentInfoListJpa();

    try {
      UserPreferences preferences = this.getUser(userName).getUserPreferences();

      javax.persistence.Query query =
          manager
              .createQuery(" select u from ComponentInfoJpa u where terminology=:terminology and version=:version and userName=:userName and preferences_id = :preferencesId");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("userName", userName);
      query.setParameter("preferencesId", preferences.getId());
      @SuppressWarnings("unchecked")
      List<ComponentInfo> userFavorites = query.getResultList();
      favorites.setObjects(userFavorites);
      favorites.setTotalCount(userFavorites.size());

    } catch (NoResultException e) {
      // n/a - just remove empty list
    }
    return favorites;
  }

  /* see superclass */
  /**
   * Adds the component info.
   *
   * @param userFavorite the user favorite
   * @return the component info
   * @throws Exception the exception
   */
  @Override
  public ComponentInfo addComponentInfo(ComponentInfo userFavorite)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add userFavorite " + userFavorite);

    // Add component
    ComponentInfo newComponentInfo = addHasLastModified(userFavorite);

    return newComponentInfo;
  }

  /* see superclass */
  /**
   * Update component info.
   *
   * @param userFavorite the user favorite
   * @throws Exception the exception
   */
  @Override
  public void updateComponentInfo(ComponentInfo userFavorite) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update userFavorite " + userFavorite);

    // update component
    updateHasLastModified(userFavorite);

  }

  /* see superclass */
  /**
   * Removes the component info.
   *
   * @param id the id
   * @throws Exception the exception
   */
  @Override
  public void removeComponentInfo(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove userFavorite " + id);
    // Remove the component
    removeHasLastModified(id, ComponentInfoJpa.class);
  }

  /**
   * Find concept notes for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the note list
   * @throws Exception the exception
   */
  @Override
  public NoteList findConceptNotesForQuery(String query, PfsParameter pfs)
    throws Exception {
    NoteList results = new NoteListJpa();
    SearchHandler searchHandler = this.getSearchHandler(null);
    final int[] totalCt = new int[1];
    List<ConceptNoteJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            ConceptNoteJpa.class, ConceptNoteJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (ConceptNoteJpa note : luceneResults) {
      results.getObjects().add(note);
    }
    return results;
  }

  /**
   * Find descriptor notes for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the note list
   * @throws Exception the exception
   */
  @Override
  public NoteList findDescriptorNotesForQuery(String query, PfsParameter pfs)
    throws Exception {
    NoteList results = new NoteListJpa();
    SearchHandler searchHandler = this.getSearchHandler(null);
    final int[] totalCt = new int[1];
    List<DescriptorNoteJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            DescriptorNoteJpa.class, DescriptorNoteJpa.class, pfs, totalCt,
            manager);
    results.setTotalCount(totalCt[0]);
    for (DescriptorNoteJpa note : luceneResults) {
      results.getObjects().add(note);
    }
    return results;
  }

  /**
   * Find code notes for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the note list
   * @throws Exception the exception
   */
  @Override
  public NoteList findCodeNotesForQuery(String query, PfsParameter pfs)
    throws Exception {
    NoteList results = new NoteListJpa();
    SearchHandler searchHandler = this.getSearchHandler(null);
    final int[] totalCt = new int[1];
    List<CodeNoteJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            CodeNoteJpa.class, CodeNoteJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (CodeNoteJpa note : luceneResults) {
      results.getObjects().add(note);
    }

    return results;
  }

}
