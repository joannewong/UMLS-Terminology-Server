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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;

/**
 * Unit testing for {@link AtomRelationshipJpa}.
 */
public class AtomRelationshipJpaUnitTest {

  /** The model object to test. */
  private AtomRelationshipJpa object;

  /** test fixture */
  private Atom atom1;

  /** test fixture */
  private Atom atom2;

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
    object = new AtomRelationshipJpa();

    map1 = new HashMap<>();
    map1.put("1", "1");
    map2 = new HashMap<>();
    map2.put("2", "2");

    ProxyTester tester = new ProxyTester(new AtomJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    atom1 = (AtomJpa) tester.createObject(1);
    atom2 = (AtomJpa) tester.createObject(2);

    object.setFrom(atom1);
    object.setTo(atom2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet008");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("fromId");
    tester.exclude("fromTerminologyId");
    tester.exclude("fromName");
    tester.exclude("toId");
    tester.exclude("toTerminologyId");
    tester.exclude("toName");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode008");
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

    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testNonIdentityFieldHashcode());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy008");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Atom.class, 1, atom1);
    tester.proxy(Atom.class, 2, atom2);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testCopyConstructorDeep(AtomRelationship.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy008");

    AtomRelationship rel = new AtomRelationshipJpa();
    ProxyTester tester = new ProxyTester(rel);
    tester.proxy(Map.class, 1, map1);
    rel = (AtomRelationship) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new AtomJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    Atom fromAtom = (Atom) tester2.createObject(1);
    Atom toAtom = (Atom) tester2.createObject(2);

    ProxyTester tester3 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester3.createObject(1);

    rel.setFrom(fromAtom);
    rel.setTo(toAtom);
    rel.getAttributes().add(att);

    AtomRelationship rel2 = new AtomRelationshipJpa(rel, false);
    assertEquals(0, rel2.getAttributes().size());

    AtomRelationship rel3 = new AtomRelationshipJpa(rel, true);
    assertEquals(1, rel3.getAttributes().size());
    assertEquals(att, rel3.getAttributes().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization008");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy atoms can have only "id" and "term" set due to xml transient
    Atom atom1 = new AtomJpa();
    atom1.setId(1L);
    atom1.setName("1");
    Atom atom2 = new AtomJpa();
    atom2.setId(2L);
    atom2.setName("2");

    tester.proxy(Atom.class, 1, atom1);
    tester.proxy(Atom.class, 2, atom2);
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
  public void testXmlTransient008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient008");

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<fromId>"));
    assertTrue(xml.contains("<fromName>"));
    assertTrue(xml.contains("<toId>"));
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
  public void testModelNotNullField008() throws Exception {
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
    tester.include("from");
    tester.include("to");
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
