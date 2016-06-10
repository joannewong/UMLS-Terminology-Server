/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.ComponentInfoRelationship;

/**
 * Unit testing for {@link ComponentInfoRelationshipJpa}.
 */
public class ComponentInfoRelationshipJpaUnitTest {

  /** The model object to test. */
  private ComponentInfoRelationshipJpa object;

  /** test fixture */
  private ComponentInfo componentInfo1;

  /** test fixture */
  private ComponentInfo componentInfo2;

  /** The map fixture 1. */
  private Map<String, String> map1;

  /** The map fixture 2. */
  private Map<String, String> map2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new ComponentInfoRelationshipJpa();

    map1 = new HashMap<>();
    map1.put("1", "1");
    map2 = new HashMap<>();
    map2.put("2", "2");

    ProxyTester tester = new ProxyTester(new ComponentInfoJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    componentInfo1 = (ComponentInfoJpa) tester.createObject(1);
    componentInfo2 = (ComponentInfoJpa) tester.createObject(2);

    object.setFrom(componentInfo1);
    object.setTo(componentInfo2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet012");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.include("fromId");
    tester.include("fromTerminology");
    tester.include("fromVersion");
    tester.include("fromTerminologyId");
    tester.include("fromName");
    tester.include("toId");
    tester.include("toTerminology");
    tester.include("toVersion");
    tester.include("toTerminologyId");
    tester.include("toName");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode012");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("alternateTerminologyIds");
    tester.include("assertedDirection");
    tester.include("additionalRelationshipType");
    tester.include("group");
    tester.include("inferred");
    tester.include("relationshipType");
    tester.include("stated");
    tester.include("hierarchical");
    tester.include("to");
    tester.include("from");
    tester.include("toTerminologyId");
    tester.include("fromTerminologyId");
    tester.include("workflowStatus");

    tester.proxy(ComponentInfo.class, 1, new ComponentInfoJpa(componentInfo1));
    tester.proxy(ComponentInfo.class, 2, new ComponentInfoJpa(componentInfo2));
    /*tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);*/

    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(ComponentInfo.class, 1, new ComponentInfoJpa(componentInfo1));
    tester.proxy(ComponentInfo.class, 2, new ComponentInfoJpa(componentInfo2));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(ComponentInfo.class, 1, new ComponentInfoJpa(componentInfo1));
    tester.proxy(ComponentInfo.class, 2, new ComponentInfoJpa(componentInfo2));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(ComponentInfo.class, 1, new ComponentInfoJpa(componentInfo1));
    tester.proxy(ComponentInfo.class, 2, new ComponentInfoJpa(componentInfo2));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(ComponentInfo.class, 1, new ComponentInfoJpa(componentInfo1));
    tester.proxy(ComponentInfo.class, 2, new ComponentInfoJpa(componentInfo2));
    assertTrue(tester.testNonIdentityFieldHashcode());
    tester.proxy(ComponentInfo.class, 1, new ComponentInfoJpa(componentInfo1));
    tester.proxy(ComponentInfo.class, 2, new ComponentInfoJpa(componentInfo2));
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy012");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(ComponentInfo.class, 1, componentInfo1);
    tester.proxy(ComponentInfo.class, 2, componentInfo2);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testCopyConstructorDeep(ComponentInfoRelationship.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy012");

    ComponentInfoRelationship rel = new ComponentInfoRelationshipJpa();
    ProxyTester tester = new ProxyTester(rel);
    tester.proxy(Map.class, 1, map1);
    rel = (ComponentInfoRelationship) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new ComponentInfoJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    ComponentInfo fromComponentInfo = (ComponentInfo) tester2.createObject(1);
    ComponentInfo toComponentInfo = (ComponentInfo) tester2.createObject(2);

    ProxyTester tester3 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester3.createObject(1);

    rel.setFrom(fromComponentInfo);
    rel.setTo(toComponentInfo);
    rel.getAttributes().add(att);

    ComponentInfoRelationship rel2 = new ComponentInfoRelationshipJpa(rel, false);
    assertEquals(0, rel2.getAttributes().size());

    ComponentInfoRelationship rel3 = new ComponentInfoRelationshipJpa(rel, true);
    assertEquals(1, rel3.getAttributes().size());
    assertEquals(att, rel3.getAttributes().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization012");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy componentInfos can have only "id" and "term" set due to xml transient
    ComponentInfo componentInfo1 = new ComponentInfoJpa();
    componentInfo1.setId(1L);
    componentInfo1.setName("1");
    ComponentInfo componentInfo2 = new ComponentInfoJpa();
    componentInfo2.setId(2L);
    componentInfo2.setName("2");

    tester.proxy(ComponentInfo.class, 1, componentInfo1);
    tester.proxy(ComponentInfo.class, 2, componentInfo2);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient012");

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<fromTerminologyId>"));
    assertTrue(xml.contains("<fromName>"));
    assertTrue(xml.contains("<toTerminologyId>"));
    assertTrue(xml.contains("<toName>"));
    assertFalse(xml.contains("<from>"));
    assertFalse(xml.contains("<to>"));

  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField012() throws Exception {
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("assertedDirection");
    tester.include("relationshipType");
    tester.include("inferred");
    tester.include("stated");
    tester.include("hierarchical");
    tester.include("workflowStatus");
    assertTrue(tester.testNotNullFields());
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
