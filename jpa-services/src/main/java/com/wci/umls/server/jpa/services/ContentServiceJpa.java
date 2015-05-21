/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.content.AbstractComponentHasAttributes;
import com.wci.umls.server.jpa.content.AbstractRelationship;
import com.wci.umls.server.jpa.content.AbstractSubset;
import com.wci.umls.server.jpa.content.AbstractSubsetMember;
import com.wci.umls.server.jpa.content.AbstractTransitiveRelationship;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.IndexUtility;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.AtomListJpa;
import com.wci.umls.server.jpa.helpers.content.AttributeListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.LexicalClassListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.StringClassListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link ContentService}.
 */
public class ContentServiceJpa extends MetadataServiceJpa implements
    ContentService {

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = false;

  /** The id assignment handler . */
  public static Map<String, IdentifierAssignmentHandler> idHandlerMap =
      new HashMap<>();
  static {

    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "identifier.assignment.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        IdentifierAssignmentHandler handlerService =
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
      String key = "compute.preferred.name.handler";
      for (String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        ComputePreferredNameHandler handlerService =
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
      String key = "normalized.string.handler";
      String handlerName = config.getProperty(key);

      NormalizedStringHandler handlerService =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, NormalizedStringHandler.class);
      normalizedStringHandler = handlerService;
    } catch (Exception e) {
      e.printStackTrace();
      normalizedStringHandler = null;
    }
  }

  /** The graph resolver. */
  public static Map<String, GraphResolutionHandler> graphResolverMap = null;
  static {
    graphResolverMap = new HashMap<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "graph.resolution.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        GraphResolutionHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, GraphResolutionHandler.class);
        graphResolverMap.put(handlerName, handlerService);
      }
      if (!graphResolverMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("graph.resolution.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      graphResolverMap = null;
    }
  }

  /** The concept field names. */
  private static String[] conceptFieldNames = {};

  /** The descriptor field names. */
  private static String[] descriptorFieldNames = {};

  /** The code field names. */
  private static String[] codeFieldNames = {};

  static {

    try {
      conceptFieldNames =
          IndexUtility.getIndexedStringFieldNames(ConceptJpa.class).toArray(
              new String[] {});
      descriptorFieldNames =
          IndexUtility.getIndexedStringFieldNames(DescriptorJpa.class).toArray(
              new String[] {});
      codeFieldNames =
          IndexUtility.getIndexedStringFieldNames(CodeJpa.class).toArray(
              new String[] {});
    } catch (Exception e) {
      e.printStackTrace();
      conceptFieldNames = null;
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
    if (graphResolverMap == null) {
      throw new Exception(
          "Graph resolver did not properly initialize, serious error.");
    }

    if (idHandlerMap == null) {
      throw new Exception(
          "Identifier assignment handler did not properly initialize, serious error.");
    }

    if (pnHandlerMap == null) {
      throw new Exception(
          "Preferred name handler did not properly initialize, serious error.");
    }

    if (conceptFieldNames == null) {
      throw new Exception(
          "Concept indexed field names did not properly initialize, serious error.");
    }

    if (normalizedStringHandler == null) {
      throw new Exception(
          "Normalized string handler did not properly initialize, serious error.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getConcept(java.lang.Long)
   */
  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get concept " + id);
    return getComponent(id, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getConcepts(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concepts " + terminologyId + "/" + terminology
            + "/" + version);
    List<Concept> concepts =
        getComponents(terminologyId, terminology, version, ConceptJpa.class);
    if (concepts == null) {
      return null;
    }
    ConceptList list = new ConceptListJpa();
    list.setTotalCount(concepts.size());
    list.setObjects(concepts);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getConcept(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addConcept(com.wci.umls.server
   * .model.content.Concept)
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
    Concept newConcept = addComponent(concept);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.conceptChanged(newConcept, WorkflowListener.Action.ADD);
      }
    }
    return newConcept;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateConcept(com.wci.umls.
   * server.model.content.Concept)
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
        Concept concept2 = getConcept(concept.getId());
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
      for (WorkflowListener listener : listeners) {
        listener.conceptChanged(concept, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeConcept(java.lang.Long)
   */
  @Override
  public void removeConcept(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove concept " + id);
    // Remove the component
    Concept concept = removeComponent(id, ConceptJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.conceptChanged(concept, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getSubset(java.lang.Long)
   */
  @Override
  public Subset getSubset(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - subset " + id);
    return getComponent(id, AbstractSubset.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubset(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Subset getSubset(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AbstractSubset.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAtomSubsets(java.lang.String
   * , java.lang.String)
   */
  @Override
  public SubsetList getAtomSubsets(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atom subsets " + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetJpa a where "
            + "terminologyVersion = :version and terminology = :terminology");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      List<Subset> m = query.getResultList();
      SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());

      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getConceptSubsets(java.lang
   * .String, java.lang.String)
   */
  @Override
  public SubsetList getConceptSubsets(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concept subsets " + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetJpa a where "
            + "terminologyVersion = :version and terminology = :terminology");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      List<Subset> m = query.getResultList();
      SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());
      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAtomSubsetMembers(java.
   * lang.String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String branch, PfsParameter pfs) {
    Logger.getLogger(getClass()).debug(
        "Content Service - find atom subset members " + subsetId + "/"
            + terminology + "/" + version);
    javax.persistence.Query query =
        applyPfsToQuery("select a from AtomSubsetMemberJpa a "
            + "where terminologyId = :subsetId "
            + "and terminologyVersion = :version "
            + "and terminology = :terminology", pfs);
    javax.persistence.Query ctQuery =
        manager.createQuery("select count(a) ct from AtomSubsetMemberJpa a "
            + "where terminologyId = :subsetId "
            + "and terminologyVersion = :version "
            + "and terminology = :terminology");
    try {
      SubsetMemberList list = new SubsetMemberListJpa();

      // execute count query
      ctQuery.setParameter("terminologyId", subsetId);
      ctQuery.setParameter("terminology", terminology);
      ctQuery.setParameter("version", version);
      list.setTotalCount(((BigDecimal) ctQuery.getResultList().get(0))
          .intValue());

      // Get results
      query.setParameter("terminologyId", subsetId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findConceptSubsetMembers(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String branch, PfsParameter pfs) {
    Logger.getLogger(getClass()).debug(
        "Content Service - find concept subset members " + subsetId + "/"
            + terminology + "/" + version);
    javax.persistence.Query query =
        applyPfsToQuery("select a from ConceptSubsetMemberJpa a "
            + "where terminologyId = :subsetId "
            + "and terminologyVersion = :version "
            + "and terminology = :terminology", pfs);
    javax.persistence.Query ctQuery =
        manager.createQuery("select count(a) ct from ConceptSubsetMemberJpa a "
            + "where terminologyId = :subsetId "
            + "and terminologyVersion = :version "
            + "and terminology = :terminology");
    try {
      SubsetMemberList list = new SubsetMemberListJpa();

      // execute count query
      ctQuery.setParameter("terminologyId", subsetId);
      ctQuery.setParameter("terminology", terminology);
      ctQuery.setParameter("version", version);
      list.setTotalCount(((BigDecimal) ctQuery.getResultList().get(0))
          .intValue());

      // Get results
      query.setParameter("terminologyId", subsetId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMembersForAtom(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembersForAtom(String atomId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members for atom " + atomId + "/"
            + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetMemberJpa a, "
            + " AtomJpa b where b.terminologyId = :atomId "
            + "and b.terminologyVersion = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("atomId", atomId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (SubsetMember<? extends ComponentHasAttributesAndName> s : list
       * .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMembersForConcept(
   * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembersForConcept(String conceptId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members for concept " + conceptId + "/"
            + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetMemberJpa a, "
            + " ConceptJpa b where b.terminologyId = :conceptId "
            + "and b.terminologyVersion = :version "
            + "and b.terminology = :terminology and s.member = b");

    try {
      SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("conceptId", conceptId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (SubsetMember<? extends ComponentHasAttributesAndName> s : list
       * .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */
      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllSubsets(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public SubsetList getAllSubsets(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all subsets " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from AbstractSubset a "
              + "where terminologyVersion = :version "
              + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      List<Subset> subsets = query.getResultList();
      SubsetList subsetList = new SubsetListJpa();
      subsetList.setObjects(subsets);
      subsetList.setTotalCount(subsets.size());
      return subsetList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addDefinition(com.wci.umls.
   * server.model.content.Definition)
   */
  @Override
  public Definition addDefinition(Definition definition) throws Exception {
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
      String id = idHandler.getTerminologyId(definition);
      definition.setTerminologyId(id);
    }

    // Add component
    Definition newDefinition = addComponent(definition);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.definitionChanged(newDefinition, WorkflowListener.Action.ADD);
      }
    }
    return newDefinition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateDefinition(com.wci.umls
   * .server.model.content.Definition)
   */
  @Override
  public void updateDefinition(Definition definition) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update definition " + definition);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(definition.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Definition definition2 =
            getComponent(definition.getId(), DefinitionJpa.class);
        if (!idHandler.getTerminologyId(definition).equals(
            idHandler.getTerminologyId(definition2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set definition id on update
        definition.setTerminologyId(idHandler.getTerminologyId(definition));
      }
    }
    // update component
    this.updateComponent(definition);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.definitionChanged(definition, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeDefinition(java.lang.
   * Long)
   */
  @Override
  public void removeDefinition(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove definition " + id);
    // Remove the component
    Definition definition = removeComponent(id, DefinitionJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.definitionChanged(definition, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addSemanticTypeComponent(com
   * .wci.umls.server.model.content.SemanticTypeComponent)
   */
  @Override
  public SemanticTypeComponent addSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent) throws Exception {
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
      String id = idHandler.getTerminologyId(semanticTypeComponent);
      semanticTypeComponent.setTerminologyId(id);
    }

    // Add component
    SemanticTypeComponent newSemanticTypeComponent =
        addComponent(semanticTypeComponent);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(newSemanticTypeComponent,
            WorkflowListener.Action.ADD);
      }
    }
    return newSemanticTypeComponent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateSemanticTypeComponent
   * (com.wci.umls.server.model.content.SemanticTypeComponent)
   */
  @Override
  public void updateSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent) throws Exception {
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
        if (!idHandler.getTerminologyId(semanticTypeComponent).equals(
            idHandler.getTerminologyId(semanticTypeComponent2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set semanticTypeComponent id on update
        semanticTypeComponent.setTerminologyId(idHandler
            .getTerminologyId(semanticTypeComponent));
      }
    }
    // update component
    this.updateComponent(semanticTypeComponent);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(semanticTypeComponent,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeSemanticTypeComponent
   * (java.lang.Long)
   */
  @Override
  public void removeSemanticTypeComponent(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove semanticTypeComponent " + id);
    // Remove the component
    SemanticTypeComponent semanticTypeComponent =
        removeComponent(id, SemanticTypeComponentJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(semanticTypeComponent,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getDescriptor(java.lang.Long)
   */
  @Override
  public Descriptor getDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get descriptor " + id);
    return getComponent(id, DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getDescriptors(java.lang.String
   * , java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DescriptorList getDescriptors(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get descriptors " + terminologyId + "/"
            + terminology + "/" + version);
    List<Descriptor> descriptors =
        getComponents(terminologyId, terminology, version, DescriptorJpa.class);
    if (descriptors == null) {
      return null;
    }
    DescriptorList list = new DescriptorListJpa();
    list.setTotalCount(descriptors.size());
    list.setObjects(descriptors);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getDescriptor(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addDescriptor(com.wci.umls.
   * server.model.content.Descriptor)
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
      for (WorkflowListener listener : listeners) {
        listener.descriptorChanged(newDescriptor, WorkflowListener.Action.ADD);
      }
    }
    return newDescriptor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateDescriptor(com.wci.umls
   * .server.model.content.Descriptor)
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
      for (WorkflowListener listener : listeners) {
        listener.descriptorChanged(descriptor, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeDescriptor(java.lang.
   * Long)
   */
  @Override
  public void removeDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove descriptor " + id);
    // Remove the component
    Descriptor descriptor = removeComponent(id, DescriptorJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.descriptorChanged(descriptor, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getCode(java.lang.Long)
   */
  @Override
  public Code getCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get code " + id);
    Code c = manager.find(CodeJpa.class, id);
    return c;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getCodes(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public CodeList getCodes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get codes " + terminologyId + "/" + terminology
            + "/" + version);
    List<Code> codes =
        getComponents(terminologyId, terminology, version, CodeJpa.class);
    if (codes == null) {
      return null;
    }
    CodeList list = new CodeListJpa();
    list.setTotalCount(codes.size());
    list.setObjects(codes);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getCode(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addCode(com.wci.umls.server
   * .model.content.Code)
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
    Code newCode = addComponent(code);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.codeChanged(newCode, WorkflowListener.Action.ADD);
      }
    }
    return newCode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateCode(com.wci.umls.server
   * .model.content.Code)
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
      for (WorkflowListener listener : listeners) {
        listener.codeChanged(code, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#removeCode(java.lang.Long)
   */
  @Override
  public void removeCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove code " + id);
    // Remove the component
    Code code = removeComponent(id, CodeJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.codeChanged(code, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getLexicalClass(java.lang.Long)
   */
  @Override
  public LexicalClass getLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical class " + id);
    return getComponent(id, LexicalClassJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getLexicalClasses(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public LexicalClassList getLexicalClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical classes " + terminologyId + "/"
            + terminology + "/" + version);
    List<LexicalClass> luis =
        getComponents(terminologyId, terminology, version,
            LexicalClassJpa.class);
    if (luis == null) {
      return null;
    }
    LexicalClassList list = new LexicalClassListJpa();
    list.setTotalCount(luis.size());
    list.setObjects(luis);
    return list;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getLexicalClass(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addLexicalClass(com.wci.umls
   * .server.model.content.LexicalClass)
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
    LexicalClass newLexicalClass = addComponent(lexicalClass);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(newLexicalClass,
            WorkflowListener.Action.ADD);
      }
    }
    return newLexicalClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateLexicalClass(com.wci.
   * umls.server.model.content.LexicalClass)
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
      for (WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(lexicalClass,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeLexicalClass(java.lang
   * .Long)
   */
  @Override
  public void removeLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove lexical class " + id);
    // Remove the component
    LexicalClass lexicalClass = removeComponent(id, LexicalClassJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(lexicalClass,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getStringClass(java.lang.Long)
   */
  @Override
  public StringClass getStringClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string class " + id);
    return getComponent(id, StringClassJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getStringClasses(java.lang.
   * String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public StringClassList getStringClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string classes " + terminologyId + "/"
            + terminology + "/" + version);
    List<StringClass> suis =
        getComponents(terminologyId, terminology, version, StringClassJpa.class);
    if (suis == null) {
      return null;
    }
    StringClassList list = new StringClassListJpa();
    list.setTotalCount(suis.size());
    list.setObjects(suis);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getStringClass(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addStringClass(com.wci.umls
   * .server.model.content.StringClass)
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
    StringClass newStringClass = addComponent(stringClass);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(newStringClass, WorkflowListener.Action.ADD);
      }
    }
    return newStringClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateStringClass(com.wci.umls
   * .server.model.content.StringClass)
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
      for (WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(stringClass, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeStringClass(java.lang
   * .Long)
   */
  @Override
  public void removeStringClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove string class " + id);
    // Remove the component
    StringClass stringClass = removeComponent(id, StringClassJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(stringClass, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescendantConcepts(java
   * .lang.String, java.lang.String, java.lang.String, boolean,
   * java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant concepts " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Concept> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, ConceptJpa.class, totalCt);
    ConceptList list = new ConceptListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAncestorConcepts(java.lang
   * .String, java.lang.String, java.lang.String, boolean, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor concepts " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Concept> descendants =
        this.findAncestorsHelper(terminologyId, terminology, version,
            parentsOnly, branch, pfs, ConceptJpa.class, totalCt);
    ConceptList list = new ConceptListJpa();
    list.setObjects(descendants);
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

    // TODO: implement "children only" flag

    if (pfs != null && pfs.getQueryRestriction() != null) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    String queryStr =
        "select a from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " super, " + clazz.getName() + " a "
            + " where super.terminologyVersion = :version "
            + " and super.terminology = :terminology "
            + " and super.terminologyId = :terminologyId"
            + " and tr.superType = super" + " and tr.subType = a "
            + " and tr.superType != tr.subType";
    javax.persistence.Query query = applyPfsToQuery(queryStr, pfs);

    javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " super, " + clazz.getName() + " a "
            + " where super.terminologyVersion = :version "
            + " and super.terminology = :terminology "
            + " and super.terminologyId = :terminologyId"
            + " and tr.superType = super" + " and tr.subType = a "
            + " and tr.superType != tr.subType");

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
    // TODO: implement "parents only" flag
    if (pfs != null && pfs.getQueryRestriction() != null) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    String queryStr =
        "select a from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " sub, " + clazz.getName() + " a "
            + " where sub.terminologyVersion = :version "
            + " and sub.terminology = :terminology "
            + " and sub.terminologyId = :terminologyId"
            + " and tr.subType = sub and tr.superType = a "
            + " and tr.superType != tr.subType ";
    javax.persistence.Query query = applyPfsToQuery(queryStr, pfs);

    javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " sub, " + clazz.getName() + " a "
            + " where sub.terminologyVersion = :version "
            + " and sub.terminology = :terminology "
            + " and sub.terminologyId = :terminologyId"
            + " and tr.subType = sub and tr.superType = a "
            + " and tr.superType != tr.subType ");

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
   * Find tree positions helper.
   *
   * @param atomClass the atom class
   * @param pfs the pfs
   * @param branch the branch
   * @param clazz the clazz
   * @return the tree position list
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private TreePositionList findTreePositionsHelper(AtomClass atomClass,
    PfsParameter pfs, String branch, Class<?> clazz) throws Exception {
    if (pfs != null && pfs.getQueryRestriction() != null) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    String queryStr =
        "select tr from " + clazz.getName().replace("Jpa", "TreePositionJpa")
            + " tr, " + clazz.getName()

            + " a " + " where a.terminologyVersion = :version "
            + " and a.terminology = :terminology "
            + " and a.terminologyId = :terminologyId" + " and tr.node = a ";
    javax.persistence.Query query = applyPfsToQuery(queryStr, pfs);

    javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TreePositionJpa") + " tr, "
            + clazz.getName() + " a "
            + " where a.terminologyVersion = :version "
            + " and a.terminology = :terminology "
            + " and a.terminologyId = :terminologyId" + " and tr.node = a ");
    TreePositionList list = new TreePositionListJpa();
    ctQuery.setParameter("terminology", atomClass.getTerminology());
    ctQuery.setParameter("version", atomClass.getTerminologyVersion());
    ctQuery.setParameter("terminologyId", atomClass.getTerminologyId());
    list.setTotalCount(((Long) ctQuery.getSingleResult()).intValue());

    query.setParameter("terminology", atomClass.getTerminology());
    query.setParameter("version", atomClass.getTerminologyVersion());
    query.setParameter("terminologyId", atomClass.getTerminologyId());

    list.setObjects(query.getResultList());
    return list;
  }

  /**
   * Apply pfs to query.
   *
   * @param queryStr the query str
   * @param pfs the pfs
   * @return the javax.persistence. query
   */
  protected javax.persistence.Query applyPfsToQuery(String queryStr,
    PfsParameter pfs) {
    String localQueryStr = queryStr;
    if (pfs != null && pfs.getSortField() != null) {
      localQueryStr += " order by a." + pfs.getSortField();
    }

    Logger.getLogger(getClass()).info(
        "localQueryStr: " + localQueryStr);
    javax.persistence.Query query = manager.createQuery(localQueryStr);
    if (pfs != null && pfs.getStartIndex() > -1 && pfs.getMaxResults() > -1) {
      query.setFirstResult(pfs.getStartIndex());
      query.setMaxResults(pfs.getMaxResults());
    }
    return query;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescendantDescriptors(java
   * .lang.String, java.lang.String, java.lang.String, boolean,
   * java.lang.String, com.wci.umls.server.helpers.PfsParameter)
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
    List<Descriptor> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, DescriptorJpa.class, totalCt);
    DescriptorList list = new DescriptorListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAncestorDescriptors(java
   * .lang.String, java.lang.String, java.lang.String, boolean,
   * java.lang.String, com.wci.umls.server.helpers.PfsParameter)
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
    List<Descriptor> descendants =
        this.findAncestorsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, DescriptorJpa.class, totalCt);
    DescriptorList list = new DescriptorListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescriptorTreePositions
   * (com.wci.umls.server.model.content.Descriptor, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public TreePositionList findDescriptorTreePositions(Descriptor descriptor,
    String branch, PfsParameter pfs) throws Exception {
    return this.findTreePositionsHelper(descriptor, pfs, branch,
        DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescendantCodes(java.lang
   * .String, java.lang.String, java.lang.String, boolean, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
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
    List<Code> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, CodeJpa.class, totalCt);
    CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAncestorCodes(java.lang
   * .String, java.lang.String, java.lang.String, boolean, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
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
    List<Code> descendants =
        this.findAncestorsHelper(terminologyId, terminology, version,
            parentsOnly, branch, pfs, CodeJpa.class, totalCt);
    CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findCodeTreePositions(com.wci
   * .umls.server.model.content.Code, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public TreePositionList findCodeTreePositions(Code code, String branch,
    PfsParameter pfs) throws Exception {
    return this.findTreePositionsHelper(code, pfs, branch, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getAtom(java.lang.Long)
   */
  @Override
  public Atom getAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get atom " + id);
    return getComponent(id, AtomJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getAtoms(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AtomList getAtoms(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atoms " + terminologyId + "/" + terminology
            + "/" + version);
    List<Atom> atoms =
        getComponents(terminologyId, terminology, version, AtomJpa.class);
    if (atoms == null) {
      return null;
    }
    AtomList list = new AtomListJpa();
    list.setTotalCount(atoms.size());
    list.setObjects(atoms);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getAtom(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addAtom(com.wci.umls.server
   * .model.content.Atom)
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
    Atom newAtom = addComponent(atom);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.atomChanged(newAtom, WorkflowListener.Action.ADD);
      }
    }
    return newAtom;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateAtom(com.wci.umls.server
   * .model.content.Atom)
   */
  @Override
  public void updateAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update atom " + atom);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(atom.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      Atom atom2 = getAtom(atom.getId());
      if (!idHandler.getTerminologyId(atom).equals(
          idHandler.getTerminologyId(atom2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(atom);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.atomChanged(atom, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#removeAtom(java.lang.Long)
   */
  @Override
  public void removeAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove atom " + id);
    // Remove the component
    Atom atom = removeComponent(id, AtomJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.atomChanged(atom, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getRelationship(java.lang.Long)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationship " + id);
    return getComponent(id, AbstractRelationship.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getRelationships(java.lang.
   * String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipList getRelationships(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships " + terminologyId + "/"
            + terminology + "/" + version);
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationships =
        getComponents(terminologyId, terminology, version,
            AbstractRelationship.class);
    if (relationships == null) {
      return null;
    }
    RelationshipList list = new RelationshipListJpa();
    list.setTotalCount(relationships.size());
    list.setObjects(relationships);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getRelationship(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationship " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AbstractRelationship.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addRelationship(com.wci.umls
   * .server.model.content.Relationship)
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
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.relationshipChanged(newRel, WorkflowListener.Action.ADD);
      }
    }
    return newRel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateRelationship(com.wci.
   * umls.server.model.content.Relationship)
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
      for (WorkflowListener listener : listeners) {
        listener.relationshipChanged(rel, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeRelationship(java.lang
   * .Long)
   */
  @Override
  public void removeRelationship(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove relationship " + id);
    // Remove the component
    @SuppressWarnings("unchecked")
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
        removeComponent(id, AbstractRelationship.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.relationshipChanged(rel, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addTransitiveRelationship(com
   * .wci.umls.server.model.content.TransitiveRelationship)
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
    TransitiveRelationship<? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    return newRel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateTransitiveRelationship
   * (com.wci.umls.server.model.content.TransitiveRelationship)
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
        TransitiveRelationship<? extends ComponentHasAttributes> rel2 =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeTransitiveRelationship
   * (java.lang.Long)
   */
  @Override
  public void removeTransitiveRelationship(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove transitive relationship " + id);
    // Remove the component
    @SuppressWarnings({
        "unchecked", "unused"
    })
    TransitiveRelationship<? extends ComponentHasAttributes> rel =
        removeComponent(id, AbstractTransitiveRelationship.class);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addSubset(com.wci.umls.server
   * .model.content.Subset)
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
    Subset newSubset = addComponent(subset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetChanged(newSubset, WorkflowListener.Action.ADD);
      }
    }
    return newSubset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateSubset(com.wci.umls.server
   * .model.content.Subset)
   */
  @Override
  public void updateSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update subset " + subset);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subset.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      Subset subset2 = getSubset(subset.getId());
      if (!idHandler.getTerminologyId(subset).equals(
          idHandler.getTerminologyId(subset2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(subset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetChanged(subset, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeSubset(java.lang.Long)
   */
  @Override
  public void removeSubset(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove subset " + id);
    // Remove the component
    Subset subset = removeComponent(id, AbstractSubset.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetChanged(subset, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMember(java.lang.Long)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName> getSubsetMember(
    Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset member " + id);
    return getComponent(id, AbstractSubsetMember.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMembers(java.lang.
   * String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembers(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members " + terminologyId + "/"
            + terminology + "/" + version);
    List<SubsetMember<? extends ComponentHasAttributesAndName>> members =
        getComponents(terminologyId, terminology, version,
            AbstractSubsetMember.class);
    if (members == null) {
      return null;
    }
    SubsetMemberList list = new SubsetMemberListJpa();
    list.setTotalCount(members.size());
    list.setObjects(members);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMember(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName> getSubsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset member " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AbstractSubsetMember.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addSubsetMember(com.wci.umls
   * .server.model.content.SubsetMember)
   */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName> addSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName> subsetMember)
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
    SubsetMember<? extends ComponentHasAttributesAndName> newSubsetMember =
        addComponent(subsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(newSubsetMember,
            WorkflowListener.Action.ADD);
      }
    }
    return newSubsetMember;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateSubsetMember(com.wci.
   * umls.server.model.content.SubsetMember)
   */
  @Override
  public void updateSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName> subsetMember)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update subsetMember " + subsetMember);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subsetMember.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      @SuppressWarnings("unchecked")
      SubsetMember<? extends ComponentHasAttributesAndName> subsetMember2 =
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
      for (WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(subsetMember,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeSubsetMember(java.lang
   * .Long)
   */
  @Override
  public void removeSubsetMember(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove subsetMember " + id);
    // Remove the component
    @SuppressWarnings("unchecked")
    SubsetMember<? extends ComponentHasAttributesAndName> subsetMember =
        removeComponent(id, AbstractSubsetMember.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(subsetMember,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAttribute(java.lang.Long)
   */
  @Override
  public Attribute getAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get attribute " + id);
    return getComponent(id, AttributeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAttributes(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeList getAttributes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get attributes " + terminologyId + "/" + terminology
            + "/" + version);
    List<Attribute> attributes =
        getComponents(terminologyId, terminology, version, AttributeJpa.class);
    if (attributes == null) {
      return null;
    }
    AttributeList list = new AttributeListJpa();
    list.setTotalCount(attributes.size());
    list.setObjects(attributes);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAttribute(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addAttribute(com.wci.umls.server
   * .model.content.Attribute)
   */
  @Override
  public Attribute addAttribute(Attribute attribute) throws Exception {
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
      String id = idHandler.getTerminologyId(attribute);
      attribute.setTerminologyId(id);
    }

    // Add component
    Attribute newAttribute = addComponent(attribute);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.attributeChanged(newAttribute, WorkflowListener.Action.ADD);
      }
    }
    return newAttribute;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateAttribute(com.wci.umls
   * .server.model.content.Attribute)
   */
  @Override
  public void updateAttribute(Attribute attribute) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update attribute " + attribute);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(attribute.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Attribute attribute2 = getAttribute(attribute.getId());
        if (!idHandler.getTerminologyId(attribute).equals(
            idHandler.getTerminologyId(attribute2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        attribute.setTerminologyId(idHandler.getTerminologyId(attribute));
      }
    }
    // update component
    this.updateComponent(attribute);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.attributeChanged(attribute, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeAttribute(java.lang.Long)
   */
  @Override
  public void removeAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove attribute " + id);
    // Remove the component
    Attribute attribute = removeComponent(id, AttributeJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.attributeChanged(attribute, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findConceptsForQuery(java.lang
   * .String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find concepts " + terminology + "/" + version + "/"
            + query);
    return findForQueryHelper(terminology, version, branch, query, pfs,
        conceptFieldNames, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#autocompleteConcepts(java.lang
   * .String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescriptorsForQuery(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptors " + terminology + "/" + version
            + "/" + query);
    return findForQueryHelper(terminology, version, branch, query, pfs,
        descriptorFieldNames, DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#autocompleteDescriptors(java
   * .lang.String, java.lang.String, java.lang.String)
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
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @param fieldNames the field names
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findForQueryHelper(String terminology,
    String version, String branch, String query, PfsParameter pfs,
    String[] fieldNames, Class<?> clazz) throws Exception {
    // Prepare results
    SearchResultList results = new SearchResultListJpa();
    List<AtomClass> classes = null;
    int totalCt[] = new int[1];

    // Perform Lucene search
    List<AtomClass> queryClasses = new ArrayList<>();
    boolean queryFlag = false;
    if (query != null && !query.equals("") && !query.equals("null")) {
      queryClasses =
          getQueryResults(terminology, version, branch, query, fieldNames,
              clazz, pfs, totalCt);
      queryFlag = true;
    }

    boolean criteriaFlag = false;
    List<AtomClass> criteriaClasses = new ArrayList<>();
    if (pfs != null) {
      boolean init = false;
      for (SearchCriteria criteria : pfs.getSearchCriteria()) {
        criteriaFlag = true;
        if (!init) {
          criteriaClasses =
              getSearchCriteriaResults(terminology, version, criteria, clazz);
          init = true;
        } else {
          // Perform intersection operation (presume "AND" semantic between
          // multiple search criteria)
          criteriaClasses.retainAll(getSearchCriteriaResults(terminology,
              version, criteria, clazz));
        }
      }
    }

    // Determine whether both query and criteria were used, or just one or the
    // other

    // Start with query results if they exist
    if (queryFlag) {
      classes = queryClasses;
    }

    if (criteriaFlag) {

      if (queryFlag) {
        // Intersect the lucene and HQL results
        classes.retainAll(criteriaClasses);
      } else {
        // Otherwise, just use criteria classes
        classes = criteriaClasses;
      }

      // Here we know the total size
      totalCt[0] = classes.size();

      // Apply PFS sorting manually
      if (pfs != null && pfs.getSortField() != null) {
        final Field sortField = clazz.getField(pfs.getSortField());
        if (sortField.getType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        sortField.setAccessible(true);
        Collections.sort(classes, new Comparator<AtomClass>() {
          @SuppressWarnings({
              "rawtypes", "unchecked"
          })
          @Override
          public int compare(AtomClass o1, AtomClass o2) {
            try {
              Comparable f1 = (Comparable) sortField.get(o1);
              Comparable f2 = (Comparable) sortField.get(o2);
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
    assert classes != null;

    // construct the search results
    for (AtomClass atomClass : classes) {
      SearchResult sr = new SearchResultJpa();
      sr.setId(atomClass.getId());
      sr.setTerminologyId(atomClass.getTerminologyId());
      sr.setTerminology(atomClass.getTerminology());
      sr.setTerminologyVersion(atomClass.getTerminologyVersion());
      sr.setValue(atomClass.getName());
      results.addObject(sr);
    }

    results.setTotalCount(totalCt[0]);
    return results;

  }

  /**
   * Returns the query results.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param fieldNames the field names
   * @param clazz the clazz
   * @param pfs the pfs
   * @param totalCt the total ct
   * @return the query results
   * @throws Exception the exception
   */
  private List<AtomClass> getQueryResults(String terminology, String version,
    String branch, String query, String[] fieldNames, Class<?> clazz,
    PfsParameter pfs, int[] totalCt) throws Exception {
    // Prepare the query string
    StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query);
    finalQuery.append(" AND terminology:" + terminology
        + " AND terminologyVersion:" + version);
    if (pfs != null && pfs.getQueryRestriction() != null) {
      finalQuery.append(" AND ");
      finalQuery.append(pfs.getQueryRestriction());
    }
    Logger.getLogger(getClass()).info("query " + finalQuery);

    // Prepare the manager and lucene query
    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
    Query luceneQuery;
    try {
      QueryParser queryParser =
          new MultiFieldQueryParser(fieldNames,
              searchFactory.getAnalyzer(clazz));

      luceneQuery = queryParser.parse(finalQuery.toString());
    } catch (ParseException e) {
      throw new LocalException(
          "The specified search terms cannot be parsed.  Please check syntax and try again.");
    }
    FullTextQuery fullTextQuery =
        fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);

    // Apply paging and sorting parameters - if no search criteria
    if (pfs.getSearchCriteria().isEmpty()) {
      applyPfsToLuceneQuery(clazz, fullTextQuery, pfs);
      // Get result size if we know it.
      totalCt[0] = fullTextQuery.getResultSize();
    }

    // execute the query
    @SuppressWarnings("unchecked")
    List<AtomClass> classes = fullTextQuery.getResultList();
    fullTextEntityManager.close();
    // closing fullTextEntityManager closes manager as well, recreate
    manager = factory.createEntityManager();
    return classes;
  }

  /**
   * Returns the search criteria results.
   *
   * @param terminology the terminology
   * @param version the version
   * @param criteria the criteria
   * @param clazz the clazz
   * @return the search criteria results
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private List<AtomClass> getSearchCriteriaResults(String terminology,
    String version, SearchCriteria criteria, Class<?> clazz) throws Exception {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT c FROM " + clazz.getName() + " c "
        + "WHERE terminology = :terminology "
        + "AND terminologyVersion = :version ");

    String terminologyId = null;

    // findActiveOnly
    if (criteria.getActiveOnly()) {
      builder.append("AND obsolete = 0 ");
    }

    // findInactiveOnly
    if (criteria.getInactiveOnly()) {
      builder.append("AND obsolete = 1 ");
    }

    // findDefinedOnly (applies to Concept only)
    if (criteria.getDefinedOnly()) {
      if (ConceptJpa.class.isAssignableFrom(clazz)) {
        builder.append("AND fullyDefined = 1 ");
      }
    }

    // findPrimitiveOnly (applies to Concept only)
    if (criteria.getPrimitiveOnly()) {
      if (ConceptJpa.class.isAssignableFrom(clazz)) {
        builder.append("AND fullyDefined = 0 ");
      }
    }

    // Find "to" end of a relationship
    // with a "from" id and optionally a "type"
    // and optionally find descendants of those things
    String relType = null;
    if (criteria.getRelationshipFromId() != null) {
      StringBuilder relBuilder = new StringBuilder();
      terminologyId = criteria.getRelationshipFromId();

      if (criteria.getRelationshipDescendantsFlag()) {
        relBuilder.append("SELECT DISTINCT a.to FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa")
            + " a, "
            + clazz.getName().replace("Jpa",
                "TransitiveRelationshipJpa" + " b, ") + clazz.getName() + " c "
            + "WHERE a.from = b.subType " + "AND b.superType = c "
            + "AND a.obsolete = 0 "
            + "AND c.terminology = :terminology "
            + "AND c.terminologyVersion = :version "
            + "AND c.terminologyId = :terminologyId");
      } else {
        relBuilder.append("SELECT a.to FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa") + " a, "
            + clazz.getName() + " b " + "WHERE a.from = b "
            + "AND a.obsolete = 0 "
            + "AND b.terminology = :terminology "
            + "AND b.terminologyVersion = :version "
            + "AND b.terminologyId = :terminologyId");
      }

      if (criteria.getRelationshipType() != null) {
        relType = criteria.getRelationshipType();
        relBuilder.append(" AND additionalRelationshipType = :type");
      }

      builder.append("AND c IN (").append(relBuilder.toString()).append(")");
    }

    // Find "from" end of a relationship
    // with a "to" id and optionally a "type"
    // and optionally find descendants of those things
    if (criteria.getRelationshipToId() != null) {
      StringBuilder relBuilder = new StringBuilder();
      terminologyId = criteria.getRelationshipToId();

      if (criteria.getRelationshipDescendantsFlag()) {
        relBuilder.append("SELECT DISTINCT a.from FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa")
            + " a, "
            + clazz.getName().replace("Jpa",
                "TransitiveRelationshipJpa" + " b, ") + clazz.getName() + " c "
            + "WHERE a.to = b.subType " + "AND b.superType = c "
            + "AND a.obsolete = 0 "
            + "AND c.terminology = :terminology "
            + "AND c.terminologyVersion = :version "
            + "AND c.terminologyId = :terminologyId");
      } else {
        relBuilder.append("SELECT a.from FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa") + " a, "
            + clazz.getName() + " b " + "WHERE a.to = b "
            + "AND a.obsolete = 0 "
            + "AND b.terminology = :terminology "
            + "AND b.terminologyVersion = :version "
            + "AND b.terminologyId = :terminologyId");
      }

      if (criteria.getRelationshipType() != null) {
        relType = criteria.getRelationshipType();
        relBuilder.append(" AND additionalRelationshipType = :type");
      }

      builder.append("AND c IN (").append(relBuilder.toString()).append(")");
    }

    // wrapper around query to findDescendants of results and self (unless specified)
    if (criteria.getFindDescendants()) {
      StringBuilder descBuilder = new StringBuilder();
      descBuilder
          .append(
              "SELECT t.subType FROM "
                  + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa") + " t "
                  + " WHERE t.superType IN (").append(builder.toString())
          .append(")");

      if (!criteria.getFindSelf()) {
        // Not self.
        descBuilder.append(" AND t.superType != t.subType ");
      }

      builder = descBuilder;
    }

    // findByRelationshipTypeId on its own
    if (criteria.getRelationshipType() != null && relType == null) {
      throw new Exception(
          "Unexpected use of relationship type criteria without "
              + "specifying a from or to relationship id");
    }

    // Run the final query
    Logger.getLogger(getClass()).debug("  QUERY = " + builder);
    javax.persistence.Query query = manager.createQuery(builder.toString());
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    if (terminologyId != null) {
      query.setParameter("terminologyId", terminologyId);
    }
    if (relType != null) {
      query.setParameter("type", relType);
    }
    List<AtomClass> classes = query.getResultList();

    return classes;

  }

  /**
   * Autocomplete helper.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param clazz the clazz
   * @return the string list
   * @throws Exception the exception
   */
  private StringList autocompleteHelper(String terminology, String version,
    String searchTerm, Class<?> clazz) throws Exception {

    final String TITLE_EDGE_NGRAM_INDEX = "atoms.edgeNGramName";
    final String TITLE_NGRAM_INDEX = "atoms.nGramName";

    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    QueryBuilder titleQB =
        fullTextEntityManager.getSearchFactory().buildQueryBuilder()
            .forEntity(clazz).get();

    Query query =
        titleQB.phrase().withSlop(2).onField(TITLE_NGRAM_INDEX)
            .andField(TITLE_EDGE_NGRAM_INDEX).boostedTo(5)
            .sentence(searchTerm.toLowerCase()).createQuery();

    Query term1 = new TermQuery(new Term("terminology", terminology));
    Query term2 = new TermQuery(new Term("terminologyVersion", version));
    BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(term1, BooleanClause.Occur.MUST);
    booleanQuery.add(term2, BooleanClause.Occur.MUST);
    booleanQuery.add(query, BooleanClause.Occur.MUST);

    FullTextQuery fullTextQuery =
        fullTextEntityManager.createFullTextQuery(booleanQuery, clazz);

    fullTextQuery.setMaxResults(20);

    @SuppressWarnings("unchecked")
    List<AtomClass> results = fullTextQuery.getResultList();
    StringList list = new StringList();
    for (AtomClass result : results) {
      // exclude duplicates
      if (!list.contains(result.getName()))
        list.addObject(result.getName());
    }
    return list;
  }

  /**
   * Apply pfs to lucene query.
   *
   * @param clazz the clazz
   * @param fullTextQuery the full text query
   * @param pfs the pfs
   * @throws Exception the exception
   */
  protected void applyPfsToLuceneQuery(Class<?> clazz,
    FullTextQuery fullTextQuery, PfsParameter pfs) throws Exception {

    // set paging/filtering/sorting if indicated
    if (pfs != null) {
      // if start index and max results are set, set paging
      if (pfs.getStartIndex() != -1 && pfs.getMaxResults() != -1) {
        fullTextQuery.setFirstResult(pfs.getStartIndex());
        fullTextQuery.setMaxResults(pfs.getMaxResults());
      }

      // if sort field is specified, set sort key
      if (pfs.getSortField() != null && !pfs.getSortField().isEmpty()) {
        Map<String, Boolean> nameToAnalyzedMap =
            this.getNameAnalyzedPairsFromAnnotation(clazz, pfs.getSortField());
        String sortField = null;

        if (nameToAnalyzedMap.size() == 0) {
          throw new Exception(clazz.getName()
              + " does not have declared, annotated method for field "
              + pfs.getSortField());
        }

        // first check the default name (rendered as ""), if not analyzed, use
        // this as sort
        if (nameToAnalyzedMap.get("") != null
            && nameToAnalyzedMap.get("").equals(false)) {
          sortField = pfs.getSortField();
        }

        // otherwise check explicit [name]Sort index
        else if (nameToAnalyzedMap.get(pfs.getSortField() + "Sort") != null
            && nameToAnalyzedMap.get(pfs.getSortField() + "Sort").equals(false)) {
          sortField = pfs.getSortField() + "Sort";
        }

        // if none, throw exception
        if (sortField == null) {
          throw new Exception(
              "Could not retrieve a non-analyzed Field annotation for get method for variable name "
                  + pfs.getSortField());
        }

        Sort sort =
            new Sort(new SortField(sortField, SortField.Type.STRING,
                !pfs.isAscending()));
        fullTextQuery.setSort(sort);
      }
    }
  }

  /**
   * Returns the name analyzed pairs from annotation.
   *
   * @param clazz the clazz
   * @param sortField the sort field
   * @return the name analyzed pairs from annotation
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   */
  @SuppressWarnings("static-method")
  private Map<String, Boolean> getNameAnalyzedPairsFromAnnotation(
    Class<?> clazz, String sortField) throws NoSuchMethodException,
    SecurityException {

    // initialize the name->analyzed pair map
    Map<String, Boolean> nameAnalyzedPairs = new HashMap<>();

    Method m =
        clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase()
            + sortField.substring(1), new Class<?>[] {});

    Set<org.hibernate.search.annotations.Field> annotationFields =
        new HashSet<>();

    // check for Field annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
      annotationFields.add(m
          .getAnnotation(org.hibernate.search.annotations.Field.class));
    }

    // check for Fields annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
      // add all specified fields
      for (org.hibernate.search.annotations.Field f : m.getAnnotation(
          org.hibernate.search.annotations.Fields.class).value()) {
        annotationFields.add(f);
      }
    }

    // cycle over discovered fields and put name and analyze == YES into map
    for (org.hibernate.search.annotations.Field f : annotationFields) {
      nameAnalyzedPairs.put(f.name(), f.analyze().equals(Analyze.YES) ? true
          : false);
    }

    return nameAnalyzedPairs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findCodesForQuery(java.lang
   * .String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find codes " + terminology + "/" + version + "/"
            + query);
    return findForQueryHelper(terminology, version, branch, query, pfs,
        codeFieldNames, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#autocompleteCodes(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete codes " + terminology + ", " + version
            + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllConcepts(java.lang.String
   * , java.lang.String, java.lang.String)
   */
  @Override
  public ConceptList getAllConcepts(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all concepts " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from ConceptJpa a "
              + "where terminologyVersion = :version "
              + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      List<Concept> concepts = query.getResultList();
      ConceptList conceptList = new ConceptListJpa();
      conceptList.setObjects(concepts);
      conceptList.setTotalCount(concepts.size());
      return conceptList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllDescriptors(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public DescriptorList getAllDescriptors(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all descriptors " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from DescriptorJpa a "
              + "where terminologyVersion = :version "
              + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");

      @SuppressWarnings("unchecked")
      List<Descriptor> descriptors = query.getResultList();
      DescriptorList descriptorList = new DescriptorListJpa();
      descriptorList.setObjects(descriptors);
      descriptorList.setTotalCount(descriptors.size());
      return descriptorList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllCodes(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public CodeList getAllCodes(String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all codes " + terminology + "/" + version + "/"
            + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from CodeJpa a "
              + "where terminologyVersion = :version "
              + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      List<Code> codes = query.getResultList();
      CodeList codeList = new CodeListJpa();
      codeList.setObjects(codes);
      codeList.setTotalCount(codes.size());
      return codeList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearTransitiveClosure(java
   * .lang.String, java.lang.String)
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
              + " and terminologyVersion = :version");
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
                  + " and terminologyVersion = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From CodeTransitiveRelationshipJpa "
              + " c where terminology = :terminology "
              + " and terminologyVersion = :version");
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearTreePositions(java.lang
   * .String, java.lang.String)
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
              + " and terminologyVersion = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From DescriptorTreePositionJpa "
              + " c where terminology = :terminology "
              + " and terminologyVersion = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From CodeTreePositionJpa "
              + " c where terminology = :terminology "
              + " and terminologyVersion = :version");
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearContent(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void clearContent(String terminology, String version) {

    Logger.getLogger(getClass()).info("Metadata service - clear metadata");
    try {
      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.begin();
      }

      for (EntityType<?> type : manager.getMetamodel().getEntities()) {
        String jpaTable = type.getName();
        // Skip audit trail tables
        if (jpaTable.toUpperCase().indexOf("_AUD") != -1) {
          continue;
        }
        // skip all abstract abbreviations and terminology classes
        if (!AbstractComponent.class.isAssignableFrom(type
            .getBindableJavaType())) {
          continue;
        }
        Logger.getLogger(getClass()).info("  Remove " + jpaTable);
        javax.persistence.Query query = null;

        query =
            manager.createQuery("DELETE FROM " + jpaTable
                + " WHERE terminology = :terminology "
                + " AND terminologyVersion = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);

        int deleteRecords = query.executeUpdate();
        Logger.getLogger(getClass()).info(
            "    " + jpaTable + " records deleted: " + deleteRecords);

      }

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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearBranch(java.lang.String)
   */
  @Override
  public void clearBranch(String branch) {
    // TODO: part of implementing branching
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getGraphResolutionHandler(java
   * .lang.String)
   */
  @Override
  public GraphResolutionHandler getGraphResolutionHandler(String terminology)
    throws Exception {
    if (graphResolverMap.containsKey(terminology)) {
      return graphResolverMap.get(terminology);
    }
    return graphResolverMap.get(ConfigUtility.DEFAULT);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getIdentifierAssignmentHandler
   * (java.lang.String)
   */
  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    if (idHandlerMap.containsKey(terminology)) {
      return idHandlerMap.get(terminology);
    }
    return idHandlerMap.get(ConfigUtility.DEFAULT);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getComputePreferredNameHandler
   * (java.lang.String)
   */
  @Override
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception {
    if (pnHandlerMap.containsKey(terminology)) {
      return pnHandlerMap.get(terminology);
    }
    return pnHandlerMap.get(ConfigUtility.DEFAULT);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getComputedPreferredName(com
   * .wci.umls.server.model.content.AtomClass)
   */
  @Override
  public String getComputedPreferredName(AtomClass atomClass) throws Exception {
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
      final String pn = handler.computePreferredName(atomClass.getAtoms());
      return pn;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getNormalizedString(java.lang
   * .String)
   */
  @Override
  public String getNormalizedString(String string) throws Exception {
    return normalizedStringHandler.getNormalizedString(string);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.services.ContentService#isLastModifiedFlag()
   */
  /**
   * Indicates whether or not last modified flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.MetadataServiceJpa#setLastModifiedFlag
   * (boolean)
   */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#setAssignIdentifiersFlag(boolean
   * )
   */
  @Override
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag) {
    this.assignIdentifiersFlag = assignIdentifiersFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getComponentStats(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, Integer> getComponentStats(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - getComponentStats");
    assert branch != null;
    Map<String, Integer> stats = new HashMap<>();
    for (EntityType<?> type : manager.getMetamodel().getEntities()) {
      String jpaTable = type.getName();
      // Logger.getLogger(getClass()).debug("  jpaTable = " + jpaTable);
      // Skip audit trail tables
      if (jpaTable.toUpperCase().indexOf("_AUD") != -1) {
        continue;
      }
      if (!AbstractAbbreviation.class.isAssignableFrom(type
          .getBindableJavaType())
          && !AbstractComponentHasAttributes.class.isAssignableFrom(type
              .getBindableJavaType())) {
        continue;
      }
      Logger.getLogger(getClass()).info("  " + jpaTable);
      javax.persistence.Query query = null;
      if (terminology != null) {
        query =
            manager.createQuery("select count(*) from " + jpaTable
                + " where terminology = :terminology "
                + "and terminologyVersion = :version ");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);

      } else {
        query = manager.createQuery("select count(*) from " + jpaTable);
      }
      int ct = ((Long) query.getSingleResult()).intValue();
      stats.put("Total " + jpaTable, ct);

      // Only compute active counts for components
      if (AbstractComponentHasAttributes.class.isAssignableFrom(type
          .getBindableJavaType())) {
        if (terminology != null) {

          query =
              manager.createQuery("select count(*) from " + jpaTable
                  + " where obsolete = 0 and terminology = :terminology "
                  + "and terminologyVersion = :version ");
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
      javax.persistence.Query query =
          manager
              .createQuery("select a from "
                  + clazz.getName()
                  + " a where terminologyId = :terminologyId and terminologyVersion = :version and terminology = :terminology");
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

    List<T> results = getComponents(terminologyId, terminology, version, clazz);
    if (results.isEmpty()) {
      Logger.getLogger(getClass()).debug("  no " + clazz.getName());
      return null;
    }
    T defaultBranch = null;
    for (T obj : results) {
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForConcept
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs) {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for concept " + conceptId + "/"
            + terminology + "/" + version);
    return findRelationshipsHelper(conceptId, terminology, version, branch,
        inverseFlag, pfs, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDeepRelationshipsForConcept
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public RelationshipList findDeepRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find deep relationships for concept " + conceptId
            + "/" + terminology + "/" + version);
    try {

      Concept concept = getConcept(conceptId, terminology, version, branch);

      List<Object[]> results = new ArrayList<>();
      String queryStr =
          "select a.id, a.terminologyId, a.terminology, a.terminologyVersion, "
              + "a.relationshipType, a.additionalRelationshipType, a.to.terminologyId "
              + "from ConceptRelationshipJpa a " + "where "
              + (inverseFlag ? "a.to" : "a.from") + ".id = :conceptId ";
      javax.persistence.Query query = manager.createQuery(queryStr);
      query.setParameter("conceptId", concept.getId());
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.terminologyVersion, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2) "
              + "from AtomRelationshipJpa a join a.to.conceptTerminologyIds cui2 "
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
          "select a.id, a.terminologyId, a.terminology, a.terminologyVersion, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2) "
              + "from DescriptorRelationshipJpa a, DescriptorJpa b, AtomJpa c, "
              + "DescriptorJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.descriptorId "
              + "and b.terminology = c.terminology "
              + "and b.terminologyVersion = c.terminologyVersion "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.descriptorId "
              + "and d.terminology = e.terminology "
              + "and d.terminologyVersion = e.terminologyVersion "
              + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.terminologyVersion, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2) "
              + "from ConceptRelationshipJpa a, ConceptJpa b, AtomJpa c, "
              + "ConceptJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.conceptId "
              + "and b.terminology = c.terminology "
              + "and b.terminologyVersion = c.terminologyVersion "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.conceptId "
              + "and d.terminology = e.terminology "
              + "and d.terminologyVersion = e.terminologyVersion "
              + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.terminologyVersion, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2) "
              + "from CodeRelationshipJpa a, CodeJpa b, AtomJpa c, "
              + "CodeJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.codeId "
              + "and b.terminology = c.terminology "
              + "and b.terminologyVersion = c.terminologyVersion "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.codeId "
              + "and d.terminology = e.terminology "
              + "and d.terminologyVersion = e.terminologyVersion "
              + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> conceptRels =
          new ArrayList<>();
      for (final Object[] result : results) {
        final ConceptRelationship relationship = new ConceptRelationshipJpa();
        final Concept toConcept = new ConceptJpa();
        toConcept.setTerminology(concept.getTerminology());
        toConcept.setTerminologyVersion(concept.getTerminologyVersion());
        toConcept.setTerminologyId(result[6].toString());
        relationship.setId(Long.parseLong(result[0].toString()));
        relationship.setFrom(concept);
        relationship.setTerminologyId(result[1].toString());
        relationship.setTerminology(result[2].toString());
        relationship.setTerminologyVersion(result[3].toString());
        relationship.setRelationshipType(result[4].toString());
        relationship.setAdditionalRelationshipType(result[5].toString());
        relationship.setTo(toConcept);
        conceptRels.add(relationship);
      }

      // Apply PFS
      // Apply PFS sorting manually
      if (pfs != null && pfs.getSortField() != null) {
        final Field sortField =
            ConceptRelationshipJpa.class.getField(pfs.getSortField());
        if (sortField.getType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        sortField.setAccessible(true);
        Collections.sort(conceptRels, new Comparator<Relationship>() {
          @Override
          public int compare(Relationship o1, Relationship o2) {
            try {
              Comparable f1 = (Comparable) sortField.get(o1);
              Comparable f2 = (Comparable) sortField.get(o2);
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
        int toIndex = conceptRels.size();
        toIndex = Math.min(toIndex, startIndex + pfs.getMaxResults());
        conceptRels = conceptRels.subList(startIndex, toIndex);
      }

      RelationshipList list = new RelationshipListJpa();
      list.setTotalCount(results.size());
      list.setObjects(conceptRels);

      return null;
    } catch (Throwable e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForDescriptor
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForDescriptor(String descriptorId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs) {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for descriptor " + descriptorId
            + "/" + terminology + "/" + version);
    return findRelationshipsHelper(descriptorId, terminology, version, branch,
        inverseFlag, pfs, DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForCode(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForCode(String codeId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs) {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for code " + codeId + "/"
            + terminology + "/" + version);
    return findRelationshipsHelper(codeId, terminology, version, branch,
        inverseFlag, pfs, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForAtom(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForAtom(String atomId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs) {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for atom " + atomId + "/"
            + terminology + "/" + version);
    return findRelationshipsHelper(atomId, terminology, version, branch,
        inverseFlag, pfs, AtomJpa.class);
  }

  /**
   * Find relationships helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the relationship list
   */
  @SuppressWarnings("unchecked")
  private RelationshipList findRelationshipsHelper(String terminologyId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs, Class<?> clazz) {
    // Match "to" for inverseFlag, "from" otherwise
    javax.persistence.Query query =
        applyPfsToQuery(
            "select a from "
                + clazz.getName().replace("Jpa", "RelationshipJpa") + " a, "
                + clazz.getName()
                + " b where b.terminologyId = :terminologyId "
                + "and b.terminologyVersion = :version "
                + "and b.terminology = :terminology and a."
                + (inverseFlag ? "to" : "from") + " = b ", pfs);
    javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "RelationshipJpa") + " a, "
            + clazz.getName() + " b where b.terminologyId = :terminologyId "
            + "and b.terminologyVersion = :version "
            + "and b.terminology = :terminology and a."
            + (inverseFlag ? "to" : "from") + " = b");
    try {
      RelationshipList list = new RelationshipListJpa();

      // execute count query
      ctQuery.setParameter("terminologyId", terminologyId);
      ctQuery.setParameter("terminology", terminology);
      ctQuery.setParameter("version", version);
      list.setTotalCount(((BigDecimal) ctQuery.getResultList().get(0))
          .intValue());

      query.setParameter("terminologyId", terminologyId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findTreePositionsForConcept
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter, java.lang.String)
   */
  @Override
  public TreePositionList findTreePositionsForConcept(String terminologyId,
    String terminology, String version, PfsParameter pfs, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for concept " + terminologyId
            + "/" + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        pfs, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findTreePositionsForDescriptor
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter, java.lang.String)
   */
  @Override
  public TreePositionList findTreePositionsForDescriptor(String terminologyId,
    String terminology, String version, PfsParameter pfs, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for descriptor " + terminologyId
            + "/" + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        pfs, DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findTreePositionsForCode(java
   * .lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter, java.lang.String)
   */
  @Override
  public TreePositionList findTreePositionsForCode(String terminologyId,
    String terminology, String version, PfsParameter pfs, String branch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for code " + terminologyId + "/"
            + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        pfs, CodeJpa.class);

  }

  /**
   * Find tree positions helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the tree position list
   */
  @SuppressWarnings("unchecked")
  private TreePositionList findTreePositionsHelper(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs,
    Class<?> clazz) {
    javax.persistence.Query query =
        applyPfsToQuery(
            "select a from "
                + clazz.getName().replace("Jpa", "TreePositionJpa") + " a, "
                + clazz.getName()
                + " b where b.terminologyId = :terminologyId "
                + "and b.terminologyVersion = :version "
                + "and b.terminology = :terminology and a.node = b ", pfs);
    javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TreePositionJpa") + " a, "
            + clazz.getName() + " b where b.terminologyId = :terminologyId "
            + "and b.terminologyVersion = :version "
            + "and b.terminology = :terminology and a.node = b");
    try {
      TreePositionList list = new TreePositionListJpa();

      // execute count query
      ctQuery.setParameter("terminologyId", terminologyId);
      ctQuery.setParameter("terminology", terminology);
      ctQuery.setParameter("version", version);
      list.setTotalCount(((BigDecimal) ctQuery.getResultList().get(0))
          .intValue());

      query.setParameter("terminologyId", terminologyId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      return list;
    } catch (NoResultException e) {
      return null;
    }

  }

}
