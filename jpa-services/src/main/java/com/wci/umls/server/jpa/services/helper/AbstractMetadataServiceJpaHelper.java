/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.jpa.services.RootServiceJpa;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.IdentifierType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;

/**
 * Default implementation of {@link MetadataService}.
 */
public abstract class AbstractMetadataServiceJpaHelper extends RootServiceJpa
    implements MetadataService {

  /**
   * Instantiates an empty {@link AbstractMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public AbstractMetadataServiceJpaHelper() throws Exception {
    super();
  }

  //
  // Not needed for sub-handler

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#getTerminologies()
   */
  @Override
  public List<RootTerminology> getTerminologies() throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getVersions(java.lang.String)
   */
  @Override
  public List<Terminology> getVersions(String terminology) throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getLatestVersion(java.lang
   * .String)
   */
  @Override
  public String getLatestVersion(String terminology) throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTerminologyLatestVersions()
   */
  @Override
  public Map<String, String> getTerminologyLatestVersions() throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAllMetadata(java.lang.String
   * , java.lang.String)
   */
  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    // n/a handled by superclass
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#clearMetadata(java.lang.String
   * , java.lang.String)
   */
  @Override
  public void clearMetadata(String terminology, String version)
    throws Exception {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#enableListeners()
   */
  @Override
  public void enableListeners() {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#disableListeners()
   */
  @Override
  public void disableListeners() {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#isLastModifiedFlag()
   */
  @Override
  public boolean isLastModifiedFlag() {
    // n/a
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#setLastModifiedFlag(boolean)
   */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addSemanticType(com.wci.umls
   * .server.model.meta.SemanticType)
   */
  @Override
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateSemanticType(com.wci
   * .umls.server.model.meta.SemanticType)
   */
  @Override
  public void updateSemanticType(SemanticType semanticType) throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeSemanticType(java.lang
   * .Long)
   */
  @Override
  public void removeSemanticType(Long id) throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addAttributeName(com.wci.umls
   * .server.model.meta.AttributeName)
   */
  @Override
  public AttributeName addAttributeName(AttributeName AttributeName)
    throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateAttributeName(com.wci
   * .umls.server.model.meta.AttributeName)
   */
  @Override
  public void updateAttributeName(AttributeName AttributeName) throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeAttributeName(java.lang
   * .Long)
   */
  @Override
  public void removeAttributeName(Long id) throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addIdentifierType(com.wci.
   * umls.server.model.meta.IdentifierType)
   */
  @Override
  public IdentifierType addIdentifierType(IdentifierType IdentifierType)
    throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateIdentifierType(com.wci
   * .umls.server.model.meta.IdentifierType)
   */
  @Override
  public void updateIdentifierType(IdentifierType IdentifierType)
    throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeIdentifierType(java.
   * lang.Long)
   */
  @Override
  public void removeIdentifierType(Long id) throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addLanguage(com.wci.umls.server
   * .model.meta.Language)
   */
  @Override
  public Language addLanguage(Language Language) throws Exception {
    // n/a
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateLanguage(com.wci.umls
   * .server.model.meta.Language)
   */
  @Override
  public void updateLanguage(Language Language) throws Exception {
    // n/a

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeLanguage(java.lang.Long)
   */
  @Override
  public void removeLanguage(Long id) throws Exception {
    // n/a

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#addAdditionalRelationshipType(com.wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    // n/a
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#updateAdditionalRelationshipType(com.wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void updateAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    // n/a

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#removeAdditionalRelationshipType(java.lang.Long)
   */
  @Override
  public void removeAdditionalRelationshipType(Long id) throws Exception {
    // n/a

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#addRelationshipType(com.wci.umls.server.model.meta.RelationshipType)
   */
  @Override
  public RelationshipType addRelationshipType(RelationshipType relationshipType)
    throws Exception {
    // n/a
    return null;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#updateRelationshipType(com.wci.umls.server.model.meta.RelationshipType)
   */
  @Override
  public void updateRelationshipType(RelationshipType relationshipType)
    throws Exception {
    // n/a
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#removeRelationshipType(java.lang.Long)
   */
  @Override
  public void removeRelationshipType(Long id) throws Exception {
    // n/a
  }


  @Override
  public Terminology addTerminology(Terminology terminology) throws Exception {
    // n/a
    return null;
  }

  @Override
  public void updateTerminology(Terminology terminology) throws Exception {
    // n/a
    
  }

  @Override
  public void removeTerminology(Long id) throws Exception {
    // n/a
    
  }

  @Override
  public RootTerminology addRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    // n/a
    return null;
  }

  @Override
  public void updateRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    // n/a
    
  }

  @Override
  public void removeRootTerminology(Long id) throws Exception {
    // n/a
    
  }
  
  @Override
  public TermType addTermType(TermType termType) throws Exception {
    // n/a
    return null;
  }

  @Override
  public void updateTermType(TermType termType) throws Exception {
    // n/a TODO Auto-generated method stub
  }

  @Override
  public void removeTermType(Long id) throws Exception {
    // n/a
  }
}
