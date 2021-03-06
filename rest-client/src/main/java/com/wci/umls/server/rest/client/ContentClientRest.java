/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.MapSetListJpa;
import com.wci.umls.server.jpa.helpers.content.MappingListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreeListJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.StringClass;

/**
 * A client for connecting to a content REST service.
 */
public class ContentClientRest extends RootClientRest
    implements ContentServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ContentClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public void loadTerminologyRrf(String terminology, String version,
    Boolean singleMode, Boolean codeFlag, String prefix, String inputDir,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - load terminology rrf "
        + terminology + ", " + version + ", " + inputDir);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputDir, "inputDir");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/terminology/load/rrf?terminology="
            + terminology + "&version=" + version + "&prefix=" + prefix
            + "&singleMode=" + (singleMode == null ? false : singleMode)
            + (codeFlag == null ? "" : "&codeFlag=" + codeFlag));

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void computeTransitiveClosure(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - compute transitive closure " + terminology
            + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/terminology/closure/compute/"
            + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(""));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void computeTreePositions(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - compute tree positions " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/terminology/treepos/compute/"
            + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(""));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - lucene reindex " + indexedObjects);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/reindex");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(indexedObjects));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      if (response.getStatus() != 204)
        throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public boolean removeTerminology(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - remove terminology " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/terminology/remove/" + terminology + "/" + version);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      if (response.getStatus() != 204)
        throw new Exception("Unexpected status " + response.getStatus());
    }
    return true;
  }

  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get concept "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/concept/"
            + terminology + "/" + version + "/" + terminologyId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptJpa concept =
        ConfigUtility.getGraphForString(resultString, ConceptJpa.class);
    return concept;
  }

  /* see superclass */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - find concepts "
        + terminology + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();

    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/concept/" + terminology + "/" + version + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SearchResultList findConceptsForGeneralQuery(String query, String jql,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find concepts " + query + ", " + jql + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/concept" + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "&jql=" + URLEncoder.encode(jql == null ? "" : jql, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - autocomplete concepts "
        + terminology + ", " + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/concept/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        ConfigUtility.getGraphForString(resultString, StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get descriptor "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/descriptor/"
            + terminology + "/" + version + "/" + terminologyId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    DescriptorJpa descriptor =
        ConfigUtility.getGraphForString(resultString, DescriptorJpa.class);
    return descriptor;
  }

  /* see superclass */
  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - find descriptors "
        + terminology + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/descriptor/" + terminology + "/" + version + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SearchResultList findDescriptorsForGeneralQuery(String query,
    String jql, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descriptors " + query + ", " + jql + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/descriptor" + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "&jql=" + URLEncoder.encode(jql == null ? "" : jql, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - autocomplete descriptors " + terminology + ", "
            + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/descriptor/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        ConfigUtility.getGraphForString(resultString, StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get code "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/code/" + terminology + "/" + version + "/" + terminologyId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    if (resultString.isEmpty()) {
      return null;
    }
    // converting to object
    return ConfigUtility.getGraphForString(resultString, CodeJpa.class);
  }

  /* see superclass */
  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - find codes "
        + terminology + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/code/" + terminology + "/" + version + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SearchResultList findCodesForGeneralQuery(String query, String jql,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find codes " + query + ", " + jql + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/code" + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "&jql=" + URLEncoder.encode(jql == null ? "" : jql, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - autocomplete codes "
        + terminology + ", " + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        ConfigUtility.getGraphForString(resultString, StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get lexical class "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/lui/" + terminology + "/" + version + "/" + terminologyId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    LexicalClassJpa lexicalClass =
        ConfigUtility.getGraphForString(resultString, LexicalClassJpa.class);
    return lexicalClass;
  }

  /* see superclass */
  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get string class "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/sui/" + terminology + "/" + version + "/" + terminologyId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringClassJpa stringClass =
        ConfigUtility.getGraphForString(resultString, StringClassJpa.class);
    return stringClass;
  }

  /* see superclass */
  @Override
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find ancestor concepts " + terminologyId + ", "
            + terminology + ", " + version + ", " + parentsOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/concept/" + terminology + "/"
            + version + "/" + terminologyId + "/ancestors/" + parentsOnly);
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list =
        ConfigUtility.getGraphForString(resultString, ConceptListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find descendant concepts " + terminologyId
            + ", " + terminology + ", " + version + ", " + childrenOnly + ", "
            + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/concept/" + terminology + "/"
            + version + "/" + terminologyId + "/descendants/" + childrenOnly);
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list =
        ConfigUtility.getGraphForString(resultString, ConceptListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find ancestor descriptors " + terminologyId
            + ", " + terminology + ", " + version + ", " + parentsOnly + ", "
            + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/descriptor/" + terminology + "/" + version + "/"
        + terminologyId + "/ancestors/" + parentsOnly);
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    DescriptorListJpa list =
        ConfigUtility.getGraphForString(resultString, DescriptorListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find descendant descriptors " + terminologyId
            + ", " + terminology + ", " + version + ", " + childrenOnly + ", "
            + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/descriptor/" + terminology + "/" + version + "/"
        + terminologyId + "/descendants/" + childrenOnly);
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    DescriptorListJpa list =
        ConfigUtility.getGraphForString(resultString, DescriptorListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find ancestor codes " + terminologyId + ", "
            + terminology + ", " + version + ", " + parentsOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/code/" + terminology + "/"
            + version + "/" + terminologyId + "/ancestors/" + parentsOnly);

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    CodeListJpa list =
        ConfigUtility.getGraphForString(resultString, CodeListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find descendant codes " + terminologyId + ", "
            + terminology + ", " + version + ", " + childrenOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/code/" + terminology + "/"
            + version + "/" + terminologyId + "/descendants/" + childrenOnly);
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    CodeListJpa list =
        ConfigUtility.getGraphForString(resultString, CodeListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetMemberList getSubsetMembersForConcept(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get subset members for concept "
            + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/concept/"
            + terminology + "/" + version + "/" + terminologyId + "/members");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetMemberListJpa list = ConfigUtility.getGraphForString(resultString,
        SubsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetMemberList getSubsetMembersForAtom(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get subset members for atom " + terminologyId
            + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/aui/"
            + terminology + "/" + version + "/" + terminologyId + "/members");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetMemberListJpa list = ConfigUtility.getGraphForString(resultString,
        SubsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public void loadTerminologyRf2Snapshot(String terminology, String version,
    String inputDir, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - load terminology rf2 snapshot " + terminology
            + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/terminology/load/rf2/snapshot/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  @Override
  public void loadTerminologyRf2Full(String terminology, String version,
    String inputDir, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - load terminology rf2 full " + terminology
            + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/terminology/load/rf2/full/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void loadTerminologyRf2Delta(String terminology, String inputDir,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - load terminology rf2 delta " + terminology);
    validateNotEmpty(inputDir, "inputDir");
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/terminology/load/rf2/delta/" + terminology);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void loadTerminologyClaml(String terminology, String version,
    String inputFile, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - load terminology ClaML " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputFile, "inputFile");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/terminology/load/claml/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(inputFile));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }
  }

  @Override
  public void loadTerminologyOwl(String terminology, String version,
    String inputFile, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - load terminology Owl "
        + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputFile, "inputFile");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/terminology/load/owl/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.text(inputFile));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public SubsetList getAtomSubsets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get atom subsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/aui/subset/all/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetListJpa list =
        ConfigUtility.getGraphForString(resultString, SubsetListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetList getConceptSubsets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept subsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/concept/subset/all/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetListJpa list =
        ConfigUtility.getGraphForString(resultString, SubsetListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get atom subset members " + terminology + ", "
            + version);
    validateNotEmpty(subsetId, "subsetId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/aui/subset/"
            + subsetId + "/" + terminology + "/" + version + "/members"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    SubsetMemberListJpa list = ConfigUtility.getGraphForString(resultString,
        SubsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get concept subset members " + terminology
            + ", " + version);
    validateNotEmpty(subsetId, "subsetId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/concept/subset/"
            + subsetId + "/" + terminology + "/" + version + "/members"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    SubsetMemberListJpa list = ConfigUtility.getGraphForString(resultString,
        SubsetMemberListJpa.class);
    return list;
  }

  /**
   * Find deep relationships for concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public RelationshipList findDeepRelationshipsForConcept(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String filter,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find deep relationships for concept "
            + terminologyId + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/concept/" + terminology + "/"
            + version + "/" + terminologyId + "/relationships/deep");
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    RelationshipListJpa list = ConfigUtility.getGraphForString(resultString,
        RelationshipListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public RelationshipList findRelationshipsForConcept(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find relationships for concept "
            + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Logger.getLogger(getClass())
        .debug("Content Client - find relationships for concept "
            + terminologyId + ", " + terminology + ", " + version + ", " + pfs
            + ", " + query);
    return findRelationshipsHelper("concept", terminologyId, terminology,
        version, query, pfs, authToken);
  }

  /* see superclass */
  @Override
  public RelationshipList findRelationshipsForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find relationships for descriptor "
            + terminologyId + ", " + terminology + ", " + version + ", " + pfs
            + ", " + query);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findRelationshipsHelper("descriptor", terminologyId, terminology,
        version, query, pfs, authToken);
  }

  /* see superclass */
  @Override
  public RelationshipList findRelationshipsForCode(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find relationships for code " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs + ", " + query);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findRelationshipsHelper("code", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /**
   * Find relationships helper.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  private RelationshipList findRelationshipsHelper(String type,
    String terminologyId, String terminology, String version, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/content/" + type + "/" + terminology
            + "/" + version + "/" + terminologyId + "/relationships" + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    RelationshipListJpa list = ConfigUtility.getGraphForString(resultString,
        RelationshipListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public MappingList findMappingsForConcept(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for concept " + terminologyId
            + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for concept " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs + ", " + query);
    return findMappingsHelper("concept", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /* see superclass */
  @Override
  public MappingList findMappingsForCode(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for code " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for code " + terminologyId + ", "
            + terminology + ", " + version + ", " + pfs + ", " + query);
    return findMappingsHelper("code", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /* see superclass */
  @Override
  public MappingList findMappingsForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for descriptor " + terminologyId
            + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for descriptor " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs + ", " + query);
    return findMappingsHelper("descriptor", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /**
   * Find mappings helper.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the mapping list
   * @throws Exception the exception
   */
  private MappingList findMappingsHelper(String type, String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + type + "/"
            + terminologyId + "/" + terminology + "/" + version + "/mappings"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    MappingListJpa list =
        ConfigUtility.getGraphForString(resultString, MappingListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public TreeList findConceptTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get tree positions for concept "
            + terminologyId + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("concept", terminologyId, terminology, version, pfs,
        authToken);
  }

  /* see superclass */
  @Override
  public TreeList findDescriptorTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get tree positions for descriptor "
            + terminologyId + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("descriptor", terminologyId, terminology, version,
        pfs, authToken);
  }

  /* see superclass */
  @Override
  public TreeList findCodeTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get tree positions for code " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("code", terminologyId, terminology, version, pfs,
        authToken);
  }

  /**
   * Find trees helper.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  private TreeList findTreesHelper(String type, String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + type + "/"
            + terminology + "/" + version + "/" + terminologyId + "/trees");
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeListJpa list =
        ConfigUtility.getGraphForString(resultString, TreeListJpa.class);
    return list;

  }

  /* see superclass */
  @Override
  public Tree findConceptTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get concept tree for query " + ", "
            + terminology + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreeForQueryHelper("concept", terminology, version, query, pfs,
        authToken);
  }

  /* see superclass */
  @Override
  public Tree findDescriptorTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get descriptor tree for query " + ", "
            + terminology + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreeForQueryHelper("descriptor", terminology, version, query,
        pfs, authToken);
  }

  /* see superclass */
  @Override
  public Tree findCodeTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get code tree for query " + ", " + terminology
            + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreeForQueryHelper("code", terminology, version, query, pfs,
        authToken);
  }

  /**
   * Find tree for query helper.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  private Tree findTreeForQueryHelper(String type, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/" + type + "/" + terminology + "/" + version + "/trees"
        + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeJpa tree = ConfigUtility.getGraphForString(resultString, TreeJpa.class);
    return tree;
  }

  /* see superclass */
  @Override
  public TreeList findConceptTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/concept"
            + "/" + terminology + "/" + version + "/trees/children");

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeListJpa list =
        ConfigUtility.getGraphForString(resultString, TreeListJpa.class);
    return list;

  }

  /* see superclass */
  @Override
  public TreeList findDescriptorTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/" + "/descriptor"
            + "/" + terminology + "/" + version + "/trees/children");

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeListJpa list =
        ConfigUtility.getGraphForString(resultString, TreeListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public TreeList findCodeTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/code"
            + "/" + terminology + "/" + version + "/trees/children");

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeListJpa list =
        ConfigUtility.getGraphForString(resultString, TreeListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public Tree findConceptTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/concept"
            + "/" + terminology + "/" + version + "/trees/roots");

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeJpa tree = ConfigUtility.getGraphForString(resultString, TreeJpa.class);
    return tree;
  }

  /* see superclass */
  @Override
  public Tree findCodeTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/code"
            + "/" + terminology + "/" + version + "/trees/roots");

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeJpa tree = ConfigUtility.getGraphForString(resultString, TreeJpa.class);
    return tree;
  }

  /* see superclass */
  @Override
  public Tree findDescriptorTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/" + "/descriptor"
            + "/" + terminology + "/" + version + "/trees/roots");

    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeJpa tree = ConfigUtility.getGraphForString(resultString, TreeJpa.class);
    return tree;
  }

  @Override
  public MapSet getMapSet(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get mapSet "
        + terminologyId + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/mapset/"
            + terminology + "/" + version + "/" + terminologyId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    MapSetJpa concept =
        ConfigUtility.getGraphForString(resultString, MapSetJpa.class);
    return concept;
  }

  @Override
  public MapSetList getMapSets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - get mapsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/mapset/all/" + terminology + "/" + version);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    MapSetListJpa list =
        ConfigUtility.getGraphForString(resultString, MapSetListJpa.class);
    return list;
  }

  @Override
  public MappingList findMappingsForMapSet(String mapSetId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find mappings for mapset " + terminology + ", "
            + version);
    validateNotEmpty(mapSetId, "subsetId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/mapset/"
            + mapSetId + "/" + terminology + "/" + version + "/mappings"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    MappingListJpa list =
        ConfigUtility.getGraphForString(resultString, MappingListJpa.class);
    return list;
  }

  @Override
  public void computeExpressionIndexes(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - reindex Ecl indexes for " + terminology + ", "
            + version);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/expression/index/" + terminology + "/" + version);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public Integer getEclExpressionResultCount(String query, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - check if ECL expression for " + terminology
            + ", " + version + ", for query: " + query);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/ecl/isExpression/"
            + terminology + "/" + version + "/" + query);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    Integer result = response.readEntity(Integer.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      return result;
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public SearchResultList getEclExpressionResults(String terminology,
    String version, String query, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get ECL results for "
        + terminology + ", " + version + ", for query: " + query);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/expression/query/"
            + terminology + "/" + version + "/" + query);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // converting to object
      SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
          SearchResultListJpa.class);
      return list;
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public SearchResultList getFavoritesForUser(PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get user favorites");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/favorites");
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

  @Override
  public void addConceptNote(String terminology, String version,
    String terminologyId, String noteText, String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Content Client - add concept note for " + terminology + ", "
            + version + ", " + terminologyId + ", with text " + noteText);

    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(version, "version");
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/concept/note/"
            + terminology + "/" + version + "/" + terminologyId + "/add");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(noteText));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void removeConceptNote(Long noteId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - remove concept note for id " + noteId);

    validateNotEmpty(noteId, "note id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/concept/note/" + noteId + "/remove");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public void addDescriptorNote(String terminology, String version,
    String terminologyId, String noteText, String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Content Client - add descriptor note for " + terminology + ", "
            + version + ", " + terminologyId + ", with text " + noteText);

    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(version, "version");
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/content/descriptor/note/"
            + terminology + "/" + version + "/" + terminologyId + "/add");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(noteText));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void removeDescriptorNote(Long noteId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - remove descriptor note for id " + noteId);

    validateNotEmpty(noteId, "note id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/descriptor/note/" + noteId + "/remove");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public void addCodeNote(String terminology, String version,
    String terminologyId, String noteText, String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Content Client - add code note for " + terminology + ", "
            + version + ", " + terminologyId + ", with text " + noteText);

    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(version, "version");
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/code/note/"
            + terminology + "/" + version + "/" + terminologyId + "/add");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(noteText));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void removeCodeNote(Long noteId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - remove code note for id " + noteId);

    validateNotEmpty(noteId, "note id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/content/code/note/" + noteId + "/remove");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public SearchResultList getComponentsWithNotesForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Content Client - get components with notes for query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/component/notes?query=" + query);
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list = ConfigUtility.getGraphForString(resultString,
        SearchResultListJpa.class);
    return list;
  }

}
