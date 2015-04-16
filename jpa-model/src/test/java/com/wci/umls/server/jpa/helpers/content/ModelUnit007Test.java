/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Definition;

/**
 * Unit testing for {@link AtomJpa}.
 */
public class ModelUnit007Test {

  /** The model object to test. */
  private AtomJpa object;

  /** The list. */
  private KeyValuePairList list;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    object = new AtomJpa();
    list = new KeyValuePairList();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet007");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("conceptsList");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode007");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("terminologyVersion");
    tester.include("codeId");
    tester.include("descriptorId");
    tester.include("language");
    tester.include("lexicalClassId");
    tester.include("stringClassId");
    tester.include("term");
    tester.include("termType");

    tester.proxy(KeyValuePairList.class, 1, list);
    tester.proxy(KeyValuePairList.class, 2, list);
    assertTrue(tester.testIdentitiyFieldEquals());
    assertTrue(tester.testNonIdentitiyFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentitiyFieldHashcode());
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy007");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(KeyValuePairList.class, 1, list);
    tester.proxy(KeyValuePairList.class, 2, list);
    assertTrue(tester.testCopyConstructor(Atom.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy007");

    Atom atom = new AtomJpa();
    ProxyTester tester = new ProxyTester(atom);
    tester.proxy(KeyValuePairList.class, 1, new KeyValuePairList());
    atom = (Atom) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester2.createObject(1);

    ProxyTester tester3 = new ProxyTester(new AtomRelationshipJpa());
    AtomRelationship rel = (AtomRelationship) tester3.createObject(1);

    ProxyTester tester4 = new ProxyTester(new DefinitionJpa());
    Definition def = (Definition) tester4.createObject(1);

    atom.addAttribute(att);
    atom.addDefinition(def);
    atom.addRelationship(rel);

    Atom atom2 = new AtomJpa(atom, false);
    assertEquals(0, atom2.getAttributes().size());
    assertEquals(0, atom2.getDefinitions().size());
    assertEquals(0, atom2.getRelationships().size());

    Atom atom3 = new AtomJpa(atom, true);
    assertEquals(1, atom3.getAttributes().size());
    assertEquals(att, atom3.getAttributes().iterator().next());
    assertEquals(1, atom3.getDefinitions().size());
    assertEquals(rel, atom3.getRelationships().iterator().next());
    assertEquals(1, atom3.getRelationships().size());
    assertEquals(def, atom3.getDefinitions().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization007");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(KeyValuePairList.class, 1, list);
    tester.proxy(KeyValuePairList.class, 2, list);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient007");

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<conceptsList>"));
    Assert.assertFalse(xml.contains("<conceptTerminologyIdMap>"));

  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField007() throws Exception {
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
    tester.include("terminologyVersion");
    tester.include("conceptTerminologyIdMap");
    tester.include("codeId");
    tester.include("language");
    tester.include("lexicalClassId");
    tester.include("stringClassId");
    tester.include("term");
    tester.include("termType");

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