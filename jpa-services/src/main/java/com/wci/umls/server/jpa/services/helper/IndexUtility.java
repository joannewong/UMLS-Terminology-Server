/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PfsParameter;

/**
 * Performs utility functions relating to Lucene indexes and Hibernate Search.
 */
public class IndexUtility {

  /** The sort field analyzed map. */
  public static Map<String, Map<String, Boolean>> sortFieldAnalyzedMap =
      new HashMap<>();

  /** The string field names map. */
  private static Map<Class<?>, Set<String>> stringFieldNames = new HashMap<>();

  /** The field names map. */
  private static Map<Class<?>, Set<String>> allFieldNames = new HashMap<>();

  // Initialize the field names maps
  static {
    try {
      final Map<String, Class<?>> reindexMap = new HashMap<>();
      final String indexProp =
          ConfigUtility.getConfigProperties().getProperty("index.packages");
      final String[] packages =
          indexProp != null ? indexProp.split(";") : new String[] {
              "com.wci.umls.server"
      };
      final Reflections reflections =
          new Reflections(new ConfigurationBuilder().forPackages(packages));
      for (final Class<?> clazz : reflections
          .getTypesAnnotatedWith(Indexed.class)) {
        reindexMap.put(clazz.getSimpleName(), clazz);
      }
      Class<?>[] classes = reindexMap.values().toArray(new Class<?>[0]);

      for (Class<?> clazz : classes) {
        stringFieldNames.put(clazz,
            IndexUtility.getIndexedFieldNames(clazz, true));
        allFieldNames.put(clazz,
            IndexUtility.getIndexedFieldNames(clazz, false));
      }
    } catch (Exception e) {
      e.printStackTrace();
      stringFieldNames = null;
    }
  }

  /**
   * Returns the indexed field names for a given class.
   *
   * @param clazz the clazz
   * @param stringOnly the string only flag
   * @return the indexed field names
   * @throws Exception the exception
   */
  public static Set<String> getIndexedFieldNames(Class<?> clazz,
    boolean stringOnly) throws Exception {

    // If already initialized, return computed values
    if (stringOnly && stringFieldNames.containsKey(clazz)) {
      return stringFieldNames.get(clazz);
    }
    if (!stringOnly && allFieldNames.containsKey(clazz)) {
      return allFieldNames.get(clazz);
    }

    // Avoid ngram and sort fields (these have special uses)
    Set<String> exclusions = new HashSet<>();
    // exclusions.add("Sort");
    exclusions.add("nGram");
    exclusions.add("NGram");

    // When looking for default fields, exclude definitions and branches
    Set<String> stringExclusions = new HashSet<>();
    stringExclusions.add("definitions");
    stringExclusions.add("branch");

    Set<String> fieldNames = new HashSet<>();

    // first cycle over all methods
    for (Method m : clazz.getMethods()) {

      // if no annotations, skip
      if (m.getAnnotations().length == 0) {
        continue;
      }

      // check for @IndexedEmbedded
      if (m.isAnnotationPresent(IndexedEmbedded.class)) {
        throw new Exception(
            "Unable to handle @IndexedEmbedded on methods, specify on field");
      }

      // determine if there's a fieldBridge (which converts the field)
      boolean hasFieldBridge = false;
      if (m.isAnnotationPresent(Field.class)) {
        if (!m.getAnnotation(Field.class).bridge().impl().toString()
            .equals("void")) {
          hasFieldBridge = true;
        }
      }

      // for non-embedded fields, only process strings
      // This is because we're handling string based query here
      // Other fields can always be used with fielded query clauses
      if (stringOnly && !hasFieldBridge
          && !m.getReturnType().equals(String.class)) {
        continue;
      }

      // check for @Field annotation
      if (m.isAnnotationPresent(Field.class)) {
        String fieldName =
            getFieldNameFromMethod(m, m.getAnnotation(Field.class));
        fieldNames.add(fieldName);
      }

      // check for @Fields annotation
      if (m.isAnnotationPresent(Fields.class)) {
        for (Field field : m.getAnnotation(Fields.class).value()) {
          String fieldName = getFieldNameFromMethod(m, field);

          fieldNames.add(fieldName);
        }
      }
    }

    // second cycle over all fields
    for (java.lang.reflect.Field f : getAllFields(clazz)) {
      // check for @IndexedEmbedded
      if (f.isAnnotationPresent(IndexedEmbedded.class)) {

        // Assumes field is a collection, and has a OneToMany, ManyToMany, or
        // ManyToOne
        // annotation
        Class<?> jpaType = null;
        if (f.isAnnotationPresent(OneToMany.class)) {
          jpaType = f.getAnnotation(OneToMany.class).targetEntity();
        } else if (f.isAnnotationPresent(ManyToMany.class)) {
          jpaType = f.getAnnotation(ManyToMany.class).targetEntity();
        } else if (f.isAnnotationPresent(ManyToOne.class)) {
          jpaType = f.getAnnotation(ManyToOne.class).targetEntity();
        } else if (f.isAnnotationPresent(OneToOne.class)) {
          jpaType = f.getAnnotation(OneToOne.class).targetEntity();
        } else {
          throw new Exception(
              "Unable to determine jpa type, @IndexedEmbedded must be used with "
                  + "@OneToOne, @OneToMany, @ManyToOne, or @ManyToMany ");

        }

        for (String embeddedField : getIndexedFieldNames(jpaType, stringOnly)) {
          fieldNames.add(f.getName() + "." + embeddedField);
        }
      }

      // determine if there's a fieldBridge (which converts the field)
      boolean hasFieldBridge = false;
      if (f.isAnnotationPresent(Field.class)) {
        if (f.getAnnotation(Field.class).bridge().impl().toString()
            .equals("void")) {
          hasFieldBridge = true;
        }
      }

      // for non-embedded fields, only process strings
      if (stringOnly && !hasFieldBridge && !f.getType().equals(String.class))
        continue;

      // check for @Field annotation
      if (f.isAnnotationPresent(Field.class)) {
        String fieldName =
            getFieldNameFromField(f, f.getAnnotation(Field.class));
        fieldNames.add(fieldName);
      }

      // check for @Fields annotation
      if (f.isAnnotationPresent(Fields.class)) {
        for (Field field : f.getAnnotation(Fields.class).value()) {
          String fieldName = getFieldNameFromField(f, field);
          fieldNames.add(fieldName);
        }
      }

    }

    // Apply filters
    Set<String> filteredFieldNames = new HashSet<>();
    OUTER: for (String fieldName : fieldNames) {
      for (String exclusion : exclusions) {
        if (fieldName.contains(exclusion)) {
          continue OUTER;
        }
      }
      for (String exclusion : stringExclusions) {
        if (stringOnly && fieldName.contains(exclusion)) {
          continue OUTER;
        }
      }
      filteredFieldNames.add(fieldName);
    }

    return filteredFieldNames;
  }

  /**
   * Helper function to get a field name from a method and annotation.
   *
   * @param m the reflected, annotated method, assumed to be of form
   *          getFieldName()
   * @param annotationField the annotation field
   * @return the indexed field name
   */
  private static String getFieldNameFromMethod(Method m,
    Field annotationField) {
    // iannotationField annotationFieldield has a speciannotationFieldied name,
    // use that
    if (annotationField.name() != null && !annotationField.name().isEmpty())
      return annotationField.name();

    // otherwise, assume method name of form getannotationFieldName
    // where the desired value is annotationFieldName
    if (m.getName().startsWith("get")) {
      return StringUtils.uncapitalize(m.getName().substring(3));
    } else if (m.getName().startsWith("is")) {
      return StringUtils.uncapitalize(m.getName().substring(2));
    } else
      return m.getName();

  }

  /**
   * Helper function get a field name from reflected Field and annotation.
   *
   * @param annotatedField the reflected, annotated field
   * @param annotationField the field annotation
   * @return the indexed field name
   */
  private static String getFieldNameFromField(
    java.lang.reflect.Field annotatedField, Field annotationField) {
    if (annotationField.name() != null && !annotationField.name().isEmpty()) {
      return annotationField.name();
    }

    return annotatedField.getName();
  }

  /**
   * Returns the all fields.
   *
   * @param type the type
   * @return the all fields
   */
  private static java.lang.reflect.Field[] getAllFields(Class<?> type) {
    if (type.getSuperclass() != null) {
      return ArrayUtils.addAll(getAllFields(type.getSuperclass()),
          type.getDeclaredFields());
    }
    return type.getDeclaredFields();
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
  public static Map<String, Boolean> getNameAnalyzedPairsFromAnnotation(
    Class<?> clazz, String sortField)
      throws NoSuchMethodException, SecurityException {
    final String key = clazz.getName() + "." + sortField;
    if (sortFieldAnalyzedMap.containsKey(key)) {
      return sortFieldAnalyzedMap.get(key);
    }

    // initialize the name->analyzed pair map
    Map<String, Boolean> nameAnalyzedPairs = new HashMap<>();

    Method m = clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase()
        + sortField.substring(1), new Class<?>[] {});

    Set<org.hibernate.search.annotations.Field> annotationFields =
        new HashSet<>();

    // check for Field annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
      annotationFields
          .add(m.getAnnotation(org.hibernate.search.annotations.Field.class));
    }

    // check for Fields annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
      // add all specified fields
      for (org.hibernate.search.annotations.Field f : m
          .getAnnotation(org.hibernate.search.annotations.Fields.class)
          .value()) {
        annotationFields.add(f);
      }
    }

    // cycle over discovered fields and put name and analyze == YES into map
    for (org.hibernate.search.annotations.Field f : annotationFields) {
      nameAnalyzedPairs.put(f.name(),
          f.analyze().equals(Analyze.YES) ? true : false);
    }

    sortFieldAnalyzedMap.put(key, nameAnalyzedPairs);

    return nameAnalyzedPairs;
  }

  /**
   * Apply pfs to lucene query2.
   *
   * @param clazz the clazz
   * @param fieldNamesKey the field names key
   * @param query the query
   * @param pfs the pfs
   * @param manager the manager
   * @return the full text query
   * @throws Exception the exception
   */
  public static FullTextQuery applyPfsToLuceneQuery(Class<?> clazz,
    Class<?> fieldNamesKey, String query, PfsParameter pfs,
    EntityManager manager) throws Exception {

    FullTextQuery fullTextQuery = null;

    // Build up the query
    StringBuilder pfsQuery = new StringBuilder();
    pfsQuery.append(query);
    if (pfs != null) {
      if (pfs.getActiveOnly()) {
        pfsQuery.append(" AND obsolete:false");
      }
      if (pfs.getInactiveOnly()) {
        pfsQuery.append(" AND obsolete:true");
      }
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfsQuery.append(" AND " + pfs.getQueryRestriction());
      }
    }

    // Set up the "full text query"
    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();

    Query luceneQuery;
    QueryParser queryParser = new MultiFieldQueryParser(IndexUtility
        .getIndexedFieldNames(fieldNamesKey, true).toArray(new String[] {}),
        searchFactory.getAnalyzer(clazz));
    String finalQuery = pfsQuery.toString();
    if (pfsQuery.toString().startsWith(" AND ")) {
      finalQuery = finalQuery.substring(5);
    }
    Logger.getLogger(IndexUtility.class).debug("  query = " + finalQuery);
    luceneQuery = queryParser.parse(finalQuery);

    // Validate query terms
    luceneQuery = luceneQuery.rewrite(fullTextEntityManager.getSearchFactory()
        .getIndexReaderAccessor().open(clazz));
    Set<Term> terms = new HashSet<>();
    luceneQuery.extractTerms(terms);
    for (Term t : terms) {
      if (t.field() != null && !t.field().isEmpty() && !IndexUtility
          .getIndexedFieldNames(fieldNamesKey, false).contains(t.field())) {
        throw new ParseException(
            "Query references invalid field name " + t.field() + ", "
                + IndexUtility.getIndexedFieldNames(fieldNamesKey, false));
      }
    }

    fullTextQuery =
        fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);

    if (pfs != null) {
      // if start index and max results are set, set paging
      if (pfs.getStartIndex() >= 0 && pfs.getMaxResults() >= 0) {
        fullTextQuery.setFirstResult(pfs.getStartIndex());
        fullTextQuery.setMaxResults(pfs.getMaxResults());
      }

      // if sort specified (single or multi-field sort), set sorting
      if ((pfs.getSortFields() != null && !pfs.getSortFields().isEmpty())
          || (pfs.getSortField() != null && !pfs.getSortField().isEmpty())) {

        // convenience container for sort field names (from either method)
        List<String> sortFieldNames = null;

        // use multiple-field sort before backwards-compatible single-field sort
        if (pfs.getSortFields() != null && !pfs.getSortFields().isEmpty()) {
          sortFieldNames = pfs.getSortFields();
        } else {
          sortFieldNames = new ArrayList<>();
          sortFieldNames.add(pfs.getSortField());
        }

        // the constructed sort fields to sort on
        List<SortField> sortFields = new ArrayList<>();

        for (String sortFieldName : sortFieldNames) {
          Map<String, Boolean> nameToAnalyzedMap = IndexUtility
              .getNameAnalyzedPairsFromAnnotation(clazz, sortFieldName);

          // the computed string name of the indexed field to sort by
          String sortFieldStr = null;

          // check existence of the annotated get[SortFieldName]() method
          if (nameToAnalyzedMap.size() == 0) {
            throw new Exception(clazz.getName()
                + " does not have declared, annotated method for field "
                + sortFieldName);
          }

          // first check the default name (rendered as ""), if not analyzed, use
          // this as sort
          if (nameToAnalyzedMap.get("") != null
              && nameToAnalyzedMap.get("").equals(false)) {
            sortFieldStr = sortFieldName;
          }

          // otherwise check explicit [SortFieldName]Sort index
          else if (nameToAnalyzedMap.get(sortFieldName + "Sort") != null
              && nameToAnalyzedMap.get(sortFieldName + "Sort")
                  .equals(false)) {
            sortFieldStr = sortFieldName + "Sort";
          }

          // if an indexed sort field could not be found, throw exception
          if (sortFieldStr == null) {
            throw new Exception(
                "Could not retrieve a non-analyzed Field annotation for get method for variable name "
                    + sortFieldName);
          }

          // construct the sort field object
          SortField sortField = null;

          // check for LONG fields
          if (sortFieldStr.equals("lastModified") || sortFieldStr.equals("timestamp")
              || sortFieldStr.equals("id")) {
            sortField = new SortField(sortFieldStr, SortField.Type.LONG,
                !pfs.isAscending());
          }

          // otherwise, sort by STRING value
          else {
            sortField = new SortField(sortFieldStr, SortField.Type.STRING,
                !pfs.isAscending());
          }

          // add the field
          sortFields.add(sortField);
        }
        
        SortField[] sfs = new SortField[sortFields.size()];
        for (int i = 0; i < sortFields.size(); i++) {
          sfs[i] = sortFields.get(i);
        }
        fullTextQuery.setSort(new Sort(sfs));

      }

    }
    return fullTextQuery;
  }
}
