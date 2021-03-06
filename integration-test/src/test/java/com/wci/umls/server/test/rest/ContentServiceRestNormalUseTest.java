/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
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
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.test.helpers.PfsParameterForComponentTest;

/**
 * Implementation of the "Content Service REST Normal Use" Test Cases.
 */
public class ContentServiceRestNormalUseTest extends ContentServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The snomed terminology. */
  private String snomedTerminology = "SNOMEDCT_US";

  /** The snomed version. */
  private String snomedVersion = "2014_09_01";

  /** The msh terminology. */
  private String mshTerminology = "MSH";

  /** The msh version. */
  private String mshVersion = "2015_2014_09_08";

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    authToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();

  }

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // Test MSH concept

    Logger.getLogger(getClass()).info(
        "TEST - " + "M0028634, MSH, 2015_2014_09_08, " + authToken);
    Concept c =
        contentService.getConcept("M0028634", mshTerminology, mshVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Oral Ulcer");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(8, c.getAtoms().size());
    assertEquals(0, c.getAttributes().size());
    // atom definitions still used
    assertEquals(0, c.getDefinitions().size());
    assertEquals(0, c.getRelationships().size());
    assertEquals(0, c.getSemanticTypes().size());
    assertEquals(mshTerminology, c.getTerminology());
    assertEquals(mshVersion, c.getVersion());
    assertEquals("M0028634", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());

    // Test SNOMEDCT_US concept
    Logger.getLogger(getClass()).info(
        "TEST - " + "40667002, SNOMEDCT, 2014_09_01, " + authToken);
    c =
        contentService.getConcept("40667002", snomedTerminology, snomedVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Fixation of small intestine");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(3, c.getAtoms().size());
    assertEquals(5, c.getAttributes().size());
    assertEquals(0, c.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, c.getRelationships().size());
    assertEquals(1, c.getSemanticTypes().size());
    assertEquals(snomedTerminology, c.getTerminology());
    assertEquals(snomedVersion, c.getVersion());
    assertEquals("40667002", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());

    // Test UMLS concept

    Logger.getLogger(getClass()).info(
        "TEST - " + "C0018787, UMLS, latest, " + authToken);
    c =
        contentService.getConcept("C0018787", umlsTerminology, umlsVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Heart");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(10, c.getAtoms().size());
    assertEquals(3, c.getAttributes().size());
    // definitions still at atom level
    assertEquals(0, c.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, c.getRelationships().size());
    assertEquals(1, c.getSemanticTypes().size());
    assertEquals(umlsTerminology, c.getTerminology());
    assertEquals(umlsVersion, c.getVersion());
    assertEquals("C0018787", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());

  }

  /**
   * Test "get" methods for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "D019226, MSH, 2015_2014_09_08, " + authToken);
    Descriptor d =
        contentService.getDescriptor("D019226", mshTerminology, mshVersion,
            authToken);

    // Validate the concept returned
    assertNotNull(d);
    assertEquals(d.getName(), "Oral Ulcer");
    assertTrue(d.isPublishable());
    assertTrue(d.isPublished());
    assertFalse(d.isObsolete());
    assertFalse(d.isSuppressible());
    assertEquals(8, d.getAtoms().size());
    assertEquals(12, d.getAttributes().size());
    // atom definitions still used
    assertEquals(0, d.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, d.getRelationships().size());
    assertEquals(mshTerminology, d.getTerminology());
    assertEquals(mshVersion, d.getVersion());
    assertEquals("D019226", d.getTerminologyId());
    assertEquals("PUBLISHED", d.getWorkflowStatus());
    assertEquals("loader", d.getLastModifiedBy());
  }

  /**
   * Test "get" methods for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "D019226, MSH, 2015_2014_09_08, " + authToken);
    Code c =
        contentService
            .getCode("D019226", mshTerminology, mshVersion, authToken);

    // Validate the concept returned
    assertNull(c);

    // Test SNOMEDCT_US concept
    Logger.getLogger(getClass()).info(
        "TEST - " + "40667002, SNOMEDCT, 2014_09_01, " + authToken);
    c =
        contentService.getCode("40667002", snomedTerminology, snomedVersion,
            authToken);
    // Validate the concept returned
    assertNull(c);

  }

  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent004() throws Exception {
    // n/a
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent005() throws Exception {
    // n/a
  }

  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent006() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    SubsetList list =
        contentService.getAtomSubsets(snomedTerminology, snomedVersion,
            authToken);
    assertEquals(3, list.getCount());
    int foundCt = 0;
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertFalse(subset.isObsolete());
      assertFalse(subset.isSuppressible());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getVersion());
      if (subset.getName().equals("GB English")) {
        foundCt++;
        assertEquals("900000000000508004", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(12694, memberList.getTotalCount());
        memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "heart", pfs, authToken);
        assertEquals(15, memberList.getCount());
        assertEquals(15, memberList.getTotalCount());

      } else if (subset.getName().equals("US English")) {
        assertEquals("900000000000509007", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(12691, memberList.getTotalCount());
        memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "heart", pfs, authToken);
        assertEquals(15, memberList.getCount());
        assertEquals(15, memberList.getTotalCount());
        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            memberList.getObjects().get(0);
        assertTrue(member.isPublishable());
        assertTrue(member.isPublished());
        assertFalse(member.isObsolete());
        assertFalse(member.isSuppressible());
        assertEquals(1, member.getAttributes().size());
        assertEquals("loader", member.getLastModifiedBy());
        // Not completely equal because of XmlTransient
        assertEquals(member.getSubset().getName(), subset.getName());
        assertEquals(snomedTerminology, member.getTerminology());
        assertEquals(snomedVersion, member.getVersion());
      } else if (subset.getName().equals(
          "REFERS TO concept association reference set")) {
        assertEquals("900000000000531004", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(46, memberList.getTotalCount());
      }
    }
    assertEquals(3, foundCt);

  }

  /**
   * Test "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent007() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    SubsetList list =
        contentService.getConceptSubsets(snomedTerminology, snomedVersion,
            authToken);
    assertEquals(15, list.getCount());
    int foundCt = 0;
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      if (subset instanceof ConceptSubsetJpa
          && ((ConceptSubsetJpa) subset).isLabelSubset()) {
        continue;
      }
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getVersion());
      if (subset.getName().equals("SAME AS association reference set")) {
        foundCt++;
        assertFalse(subset.isObsolete());
        assertFalse(subset.isSuppressible());
        assertEquals("900000000000527005", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(1029, memberList.getTotalCount());
        memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "Karyotype", pfs, authToken);
        assertEquals(2, memberList.getCount());
        assertEquals(2, memberList.getTotalCount());

      } else if (subset.getName().equals("Non-human simple reference set")) {
        assertTrue(subset.isObsolete());
        assertTrue(subset.isSuppressible());
        assertEquals("447564002", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(5, memberList.getCount());
        assertEquals(5, memberList.getTotalCount());

      } else if (subset.getName().equals("ICD-10 complex map reference set")) {
        foundCt++;
        assertFalse(subset.isObsolete());
        assertFalse(subset.isSuppressible());
        assertEquals("447562003", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(1153, memberList.getTotalCount());
        memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "syndrome", pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(71, memberList.getTotalCount());
        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            memberList.getObjects().get(0);
        assertTrue(member.isPublishable());
        assertTrue(member.isPublished());
        assertFalse(member.isObsolete());
        assertFalse(member.isSuppressible());
        assertEquals(7, member.getAttributes().size());
        assertEquals("loader", member.getLastModifiedBy());
        // Not completely equal because of XmlTransient
        assertEquals(member.getSubset().getName(), subset.getName());
        assertEquals(snomedTerminology, member.getTerminology());
        assertEquals(snomedVersion, member.getVersion());
      }
    }
    assertEquals(3, foundCt);

  }

  /**
   * Test "find" concepts for query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent008() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());

    // Simple query with spaces, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "heart disease", null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(217, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(217, searchResults.getCount());

    // Complex fielded query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService
            .findConceptsForQuery(
                snomedTerminology,
                snomedVersion,
                "heart disease AND obsolete:false AND suppressible:false AND published:true",
                null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(210, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(210, searchResults.getCount());

    // Simple query, sorted on name
    Logger.getLogger(getClass()).info("  Simple query, sorted on name");
    pfs.setSortField("name");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // Simple query, sorted on name, descending order
    Logger.getLogger(getClass()).info(
        "  Simple query, sorted on name, descending order");
    pfs.setAscending(false);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // store the sorted results for later comparison
    SearchResultList sortedResults = searchResults;

    // Simple query, paged and sorted results, first page
    Logger.getLogger(getClass()).info(
        "  Simple query, paged and sorted results, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, paged and sorted results, second page
    Logger.getLogger(getClass()).info(
        "  Simple query, paged and sorted results, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // Simple query, query restriction
    Logger.getLogger(getClass()).info("  Simple query, query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:169559003");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("169559003"));

    // Simple query, for "active only", empty pfs
    Logger.getLogger(getClass()).info(
        "  Simple query, for \"active only\", empty pfs");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }

    // No query active only, first page
    Logger.getLogger(getClass()).info("  No query active only, first page");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    Logger.getLogger(getClass())
        .info("    count = " + searchResults.getCount());
    assertEquals(3903, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only, first page
    Logger.getLogger(getClass()).info("  No query, inactive only, first page");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

    // Simple query, active only, first page
    Logger.getLogger(getClass())
        .info("  Simple query, active only, first page");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(210, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("disease"));
    }

    // Simple query, inactive only, first page
    Logger.getLogger(getClass()).info(
        "  Simple query, inactive only, first page");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

  }

  /**
   * Test "find" descriptors by query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent009() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.getCount());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfs.setSortField("name");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, descending");
    pfs.setAscending(false);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("C118284"));

    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);

    // No query, ia active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // No query, active only and primitive only
    Logger.getLogger(getClass()).info(
        "  No query, active only and primitive only");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // Simple query and active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(69, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // Simple query and inactive active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());
  }

  /**
   * Test "find" codes by query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent010() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfs.setSortField("name");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, descending");
    pfs.setAscending(false);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    // assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
    // sortedResults, pfs));
    // assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
    // CodeJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());
    // assertTrue(searchResults.getObjects().get(0).getTerminologyId()
    // .equals("C118284"));

    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    // No query, is active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // Simple query and active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "disease",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

    // Simple query and inactive active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "disease",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

  }

  /**
   * Test ancestor/descendant for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent011() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    ConceptList conceptList;

    // Get descendants for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept descendants, empty pfs");
    conceptList =
        contentService.findDescendantConcepts("105590001", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(62, conceptList.getCount());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, empty pfs");
    conceptList =
        contentService.findAncestorConcepts("10697004", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(3, conceptList.getCount());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);

    // Get descendants for SNOMEDCT concept with paging
    Logger.getLogger(getClass()).info(
        "  Test concept descendants, with paging ");
    conceptList =
        contentService.findDescendantConcepts("105590001", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(2, conceptList.getCount());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, with paging");
    conceptList =
        contentService.findAncestorConcepts("10697004", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(2, conceptList.getCount());

  }

  /**
   * Test ancestor/descendant for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent012() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    DescriptorList descriptorList;

    // Get descendants for MSH descriptor
    Logger.getLogger(getClass()).info(
        "  Test descriptor descendants, empty pfs");
    descriptorList =
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getCount());

    // Get ancestors for MSH Descriptor
    Logger.getLogger(getClass()).info("  Test descriptor ancestors, empty pfs");
    descriptorList =
        contentService.findAncestorDescriptors("D000009", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getCount());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);

    // Get descendants for MSH descriptor with paging
    Logger.getLogger(getClass()).info(
        "  Test descriptor descendants, with paging ");
    descriptorList =
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.getCount());

    // Get ancestors for MSH descriptor
    Logger.getLogger(getClass()).info(
        "  Test descriptor ancestors, with paging");
    descriptorList =
        contentService.findAncestorDescriptors("D000009", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.getCount());
  }

  /**
   * Test ancestor/descendant for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent013() throws Exception {
    // n/a - no code ancestors or descendants
  }

  /**
   * Test "find" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent014() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass()).info("  Test get subset members for atom");
    SubsetMemberList list =
        contentService.getSubsetMembersForAtom("166113012", snomedTerminology,
            snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(2, list.getTotalCount());
    assertEquals(2, list.getCount());

    Logger.getLogger(getClass()).info("  Test get subset members for concept");
    list =
        contentService.getSubsetMembersForConcept("10123006",
            snomedTerminology, snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    assertEquals(5, list.getCount());

  }

  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent015() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass())
        .info("  Test autocomplete for snomed concepts");
    StringList list =
        contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
            "let", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(70, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
            "lett", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(73, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(73, list.getTotalCount());
    assertEquals(20, list.getCount());

    Logger.getLogger(getClass()).info("  Test autocomplete for msh concepts");
    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "let",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "lett",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    Logger.getLogger(getClass()).info("  Test autocomplete for umls concepts");
    list =
        contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
            "let", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(87, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
            "lett", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(90, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(90, list.getTotalCount());
    assertEquals(20, list.getCount());

  }

  /**
   * Test autocomplete for descriptors
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent016() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass())
        .info("  Test autocomplete for msh descriptors");
    StringList list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "let",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "lett",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

  }

  /**
   * Test autocomplete for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent017() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass()).info("  Test autocomplete for snomed codes");
    StringList list =
        contentService.autocompleteCodes(snomedTerminology, snomedVersion,
            "let", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    list =
        contentService.autocompleteCodes(snomedTerminology, snomedVersion,
            "lett", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    list =
        contentService.autocompleteCodes(snomedTerminology, snomedVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    Logger.getLogger(getClass()).info("  Test autocomplete for msh codes");
    list = contentService.autocompleteCodes("MTH", "latest", "hys", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    list = contentService.autocompleteCodes("MTH", "latest", "mesn", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    list =
        contentService.autocompleteCodes("MTH", "latest", "mesna", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

  }

  /**
   * Test get of deep relationships for a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent018() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // simple deep rels call
    Logger.getLogger(getClass()).info("  Test deep relationships");
    RelationshipList list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", new PfsParameterJpa(), null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(66, list.getCount());
    RelationshipList fullList = list;

    PfsParameterJpa pfs = new PfsParameterJpa();

    // deep rels call with paging
    Logger.getLogger(getClass()).info("  Test deep relationships with paging");
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(10, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // deep rels call with sorting
    Logger.getLogger(getClass()).info("  Test deep relationships with paging");
    pfs = new PfsParameterJpa();
    pfs.setSortField("relationshipType");
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(66, list.getCount());
    fullList = list;

    // deep rels call with sorting and paging
    Logger.getLogger(getClass()).info(
        "  Test deep relationships with sorting and paging");
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    pfs.setSortField("relationshipType");
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(10, list.getCount());
    // assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // assertTrue(PfsParameterForComponentTest.testSort(list, pfs,
    // AbstractRelationship.class));

    // deep rels call with sorting and paging, page 2
    Logger.getLogger(getClass()).info(
        "  Test deep relationships with sorting and paging");
    pfs.setStartIndex(10);
    pfs.setMaxResults(10);
    pfs.setSortField("relationshipType");
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(10, list.getCount());
    // assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // assertTrue(PfsParameterForComponentTest.testSort(list, pfs,
    // AbstractRelationship.class));

  }

  /**
   * Test find trees for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent023() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Tree lookup, empty pfs");
    TreeList list =
        contentService.findConceptTrees("259662009", snomedTerminology,
            snomedVersion, new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(5, list.getCount());

    TreeList fullList = list;
    PfsParameterJpa pfs = new PfsParameterJpa();

    // tree lookup, first page
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    list =
        contentService.findConceptTrees("259662009", snomedTerminology,
            snomedVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, second page
    Logger.getLogger(getClass()).info("  Tree lookup, second page");
    pfs.setStartIndex(2);
    pfs.setMaxResults(2);
    list =
        contentService.findConceptTrees("259662009", snomedTerminology,
            snomedVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, first page and sort order
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    pfs.setSortField("nodeTerminologyId");
    list =
        contentService.findConceptTrees("259662009", snomedTerminology,
            snomedVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // hard to verify sort order because it's based on the lowest-level node
    // information

  }

  /**
   * Test find trees for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent024() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Tree lookup, empty pfs");
    TreeList list =
        contentService.findDescriptorTrees("D018410", mshTerminology,
            mshVersion, new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(3, list.getCount());

    TreeList fullList = list;
    PfsParameterJpa pfs = new PfsParameterJpa();

    // tree lookup, first page
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    list =
        contentService.findDescriptorTrees("D018410", mshTerminology,
            mshVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, second page
    Logger.getLogger(getClass()).info("  Tree lookup, second page");
    pfs.setStartIndex(1);
    pfs.setMaxResults(1);
    list =
        contentService.findDescriptorTrees("D018410", mshTerminology,
            mshVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, first page and sort order
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    pfs.setSortField("nodeTerminologyId");
    list =
        contentService.findDescriptorTrees("D018410", mshTerminology,
            mshVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // hard to verify sort order because it's based on the lowest-level node
    // information

  }

  /**
   * Test find trees for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent025() throws Exception {
    Logger.getLogger(getClass()).info("Start test");
    // n/a - no sample data

  }

  /**
   * Test general query mechanism.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent026() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    /** Find concepts with hql query */
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    SearchResultList sml =
        contentService.findConceptsForGeneralQuery("",
            "SELECT c FROM ConceptJpa c", new PfsParameterJpa(), authToken);
    assertEquals(6944, sml.getCount());

    /** Find concepts with hql query and pfs parameter max results 20 */
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findConceptsForGeneralQuery("",
            "SELECT c FROM ConceptJpa c", pfs, authToken);
    assertEquals(20, sml.getCount());
    assertEquals(6944, sml.getTotalCount());

    /** Find concepts in intersection of lucene and hql queries */
    Logger
        .getLogger(getClass())
        .info(
            "TEST3 - "
                + "name:amino, SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
                + authToken);
    sml =
        contentService.findConceptsForGeneralQuery("name:amino",
            "SELECT c FROM ConceptJpa c", new PfsParameterJpa(), authToken);
    assertEquals(10, sml.getCount());
    assertEquals(10, sml.getTotalCount());

    /** Find concepts in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST4 - " + "name:amino, SNOMEDCT_US, 2014_09_01, " + authToken);
    sml =
        contentService.findConceptsForGeneralQuery("name:amino", "",
            new PfsParameterJpa(), authToken);
    assertEquals(10, sml.getCount());
    assertEquals(10, sml.getTotalCount());

    /** Find descriptors with hql query */
    Logger.getLogger(getClass()).info(
        "TEST5 - " + "SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("",
            "SELECT c FROM DescriptorJpa c", new PfsParameterJpa(), authToken);
    assertEquals(997, sml.getCount());

    /** Find descriptors with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST6 - " + "SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("",
            "SELECT c FROM DescriptorJpa c", pfs, authToken);
    assertEquals(20, sml.getCount());
    assertEquals(997, sml.getTotalCount());

    /** Find descriptors in intersection of lucene and hql queries */
    Logger
        .getLogger(getClass())
        .info(
            "TEST7 - "
                + "name:amino, SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2014_09_01, "
                + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("name:amino",
            "SELECT c FROM DescriptorJpa c", new PfsParameterJpa(), authToken);
    assertEquals(4, sml.getCount());
    assertEquals(4, sml.getTotalCount());

    /** Find descriptors in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST8 - " + "name:amino, SNOMEDCT_US, 2014_09_01, " + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("name:amino", "",
            new PfsParameterJpa(), authToken);
    assertEquals(4, sml.getCount());
    assertEquals(4, sml.getTotalCount());

    /** Find codes with hql query */
    Logger.getLogger(getClass()).info(
        "TEST9 - " + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    sml =
        contentService.findCodesForGeneralQuery("", "SELECT c FROM CodeJpa c",
            new PfsParameterJpa(), authToken);
    assertEquals(151, sml.getCount());

    /** Find codes with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST10 - " + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findCodesForGeneralQuery("", "SELECT c FROM CodeJpa c",
            pfs, authToken);
    assertEquals(20, sml.getCount());
    assertEquals(151, sml.getTotalCount());

    /** Find codes in intersection of lucene and hql queries */
    Logger.getLogger(getClass()).info(
        "TEST11 - "
            + "name:amino, SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    sml =
        contentService.findCodesForGeneralQuery("name:amino",
            "SELECT c FROM CodeJpa c", new PfsParameterJpa(), authToken);
    assertEquals(0, sml.getCount());
    assertEquals(0, sml.getTotalCount());

    /** Find codes in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST12 - " + "name:amino, SNOMEDCT_US, 2014_09_01, " + authToken);
    sml =
        contentService.findCodesForGeneralQuery("name:amino", "",
            new PfsParameterJpa(), authToken);
    assertEquals(0, sml.getCount());
    assertEquals(0, sml.getTotalCount());
  }

  /**
   * Test finding relationships for a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent020() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    /** Find relationships for given concept */
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "C0000737, UMLS, latest, " + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList l =
        contentService.findRelationshipsForConcept("C0000737", umlsTerminology,
            umlsVersion, "", pfs, authToken);
    assertEquals(20, l.getCount());

    /** Find relationships for given concept with pfs */
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "C0000737, UMLS, latest, " + authToken);
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    l =
        contentService.findRelationshipsForConcept("C0000737", umlsTerminology,
            umlsVersion, "", pfs, authToken);
    assertEquals(3, l.getCount());
    assertEquals(20, l.getTotalCount());

  }

  /**
   * Test finding relationships for a descriptor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent021() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    /** Find relationships for given descriptor */
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "D000015, MSH, mshVersion, " + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList l =
        contentService.findRelationshipsForDescriptor("D000015",
            mshTerminology, mshVersion, "", pfs, authToken);
    assertEquals(50, l.getCount());

    /** Find relationships for given descriptor with pfs */
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "D000015, MSH, mshVersion, " + authToken);
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    l =
        contentService.findRelationshipsForDescriptor("D000015",
            mshTerminology, mshVersion, "", pfs, authToken);
    assertEquals(3, l.getCount());
    assertEquals(50, l.getTotalCount());
  }

  /**
   * Test finding relationships for a code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent022() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    /** Find relationships for given code */
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "U000019, MSH, mshVersion, " + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList l =
        contentService.findRelationshipsForCode("U000019", mshTerminology,
            mshVersion, "", pfs, authToken);
    assertEquals(0, l.getCount());

    /** Find relationships for given code with pfs */
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "U000019, MSH, mshVersion, " + authToken);
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    l =
        contentService.findRelationshipsForCode("U000019", mshTerminology,
            mshVersion, "", pfs, authToken);
    assertEquals(0, l.getCount());
    assertEquals(0, l.getTotalCount());
  }

  /**
   * Test find concept trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent027() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    Tree tree =
        contentService.findConceptTreeForQuery(snomedTerminology,
            snomedVersion, "vitamin", new PfsParameterJpa(), authToken);

    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("vitamin"));
    }

    PfsParameterJpa pfs = new PfsParameterJpa();
    // tree lookup, limit to 3
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree =
        contentService.findConceptTreeForQuery(snomedTerminology,
            snomedVersion, "vitamin", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(3, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("vitamin"));
    }

    // wider lookup, limit to 10
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree =
        contentService.findConceptTreeForQuery(snomedTerminology,
            snomedVersion, "a*", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(7, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("a"));
    }

  }

  /**
   * Test find descriptor trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent028() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    Tree tree =
        contentService.findDescriptorTreeForQuery(mshTerminology, mshVersion,
            "pneumonia", new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(7, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("pneumonia"));
    }

    PfsParameterJpa pfs = new PfsParameterJpa();
    // tree lookup, limit to 3
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree =
        contentService.findDescriptorTreeForQuery(mshTerminology, mshVersion,
            "pneumonia", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(3, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("pneumonia"));
    }

  }

  /**
   * Test find code trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent029() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // n/a - no sample data
  }

  /**
   * Test "find" concept tree children
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent030() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

  }

  /**
   * Test "find" descriptors by query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent031() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

  }

  /**
   * Test "find" descriptors by query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent032() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

  }

  /**
   * Test get mapset.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent033() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "447562003, SNOMEDCT_US, _2014_09_01, " + authToken);
    MapSet c =
        contentService.getMapSet("447562003", "SNOMEDCT_US", "_2014_09_01",
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "ICD-10 complex map reference set");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertEquals(1, c.getAttributes().size());
    assertEquals("SNOMEDCT_US", c.getTerminology());
    assertEquals("_2014_09_01", c.getVersion());
    assertEquals("447562003", c.getTerminologyId());
    assertEquals("loader", c.getLastModifiedBy());
  }

  /**
   * Test get mapsets.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent034() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "SNOMEDCT_US, _2014_09_01, " + authToken);
    MapSetList c =
        contentService.getMapSets("SNOMEDCT_US", "_2014_09_01", authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getObjects().size(), 1);
  }

  /**
   * Test find mappings for mapset
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent035() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "SNOMEDCT_US, _2014_09_01, " + authToken);
    MappingList c =
        contentService.findMappingsForMapSet("447562003", "SNOMEDCT_US",
            "_2014_09_01", "", new PfsParameterJpa(), authToken);

    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getObjects().size(), 334);
  }

  /**
   * Test find mappings for concept
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent036() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "C0155860, UMLS, latest" + authToken);
    MappingList c =
        contentService.findMappingsForConcept("C0155860", "UMLS", "latest", "",
            new PfsParameterJpa(), authToken);

    // Validate the concept returned
    assertNotNull(c);
    assertEquals(1, c.getObjects().size());
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(authToken);

  }

}
