/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for moving atoms from one concept to another.
 */
public class MoveMolecularAction extends AbstractMolecularAction {

  /** The atom ids. */
  private List<Long> atomIds;

  /** The move atoms. */
  private List<Atom> moveAtoms;

  /** The to concept pre updates. */
  private Concept toConceptPreUpdates;

  /** The to concept post updates. */
  private Concept toConceptPostUpdates;

  /** The from concept pre updates. */
  private Concept fromConceptPreUpdates;

  /** The from concept post updates. */
  private Concept fromConceptPostUpdates;

  /**
   * Instantiates an empty {@link MoveMolecularAction}.
   *
   * @throws Exception the exception
   */
  public MoveMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Sets the atom ids.
   *
   * @param atomIds the atom ids
   */
  public void setAtomIds(List<Long> atomIds) {
    this.atomIds = atomIds;
  }

  /**
   * Returns the from concept.
   *
   * @return the from concept
   */
  public Concept getFromConcept() {
    return getConcept();
  }

  /**
   * Returns the to concept.
   *
   * @return the to concept
   */
  public Concept getToConcept() {
    return getConcept2();
  }

  /**
   * Returns the to concept pre updates.
   *
   * @return the to concept pre updates
   */
  public Concept getToConceptPreUpdates() {
    return toConceptPreUpdates;
  }

  /**
   * Returns the to concept post updates.
   *
   * @return the to concept post updates
   */
  public Concept getToConceptPostUpdates() {
    return toConceptPostUpdates;
  }

  /**
   * Returns the from concept pre updates.
   *
   * @return the from concept pre updates
   */
  public Concept getFromConceptPreUpdates() {
    return fromConceptPreUpdates;
  }

  /**
   * Returns the from concept post updates.
   *
   * @return the from concept post updates
   */
  public Concept getFromConceptPostUpdates() {
    return fromConceptPostUpdates;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Same concept check
    if (getFromConcept() == getToConcept()) {
      throw new LocalException("Cannot move atoms from concept "
          + getFromConcept().getId() + " to concept " + getToConcept().getId()
          + " - identical concept.");
    }

    // Moving concepts must be from the same terminology
    if (!(getFromConcept().getTerminology().toString()
        .equals(getToConcept().getTerminology().toString()))) {
      throw new LocalException(
          "Two concepts must be from the same terminology to have atoms moved between them, but concept "
              + getFromConcept().getId() + " has terminology "
              + getFromConcept().getTerminology() + ", and Concept "
              + getToConcept().getId() + " has terminology "
              + getToConcept().getTerminology());
    }

    // Populate move-atom list, and exists check
    moveAtoms = new ArrayList<Atom>();
    for (final Atom atm : getFromConcept().getAtoms()) {
      if (atomIds.contains(atm.getId())) {
        moveAtoms.add(atm);
      }
    }

    if (!(moveAtoms.size() == atomIds.size())) {
      throw new LocalException("Atom to move not found on from Concept");
    }

    // Check preconditions
    validationResult.merge(super.checkPreconditions());

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Make copy of toConcept and fromConcept before changes, to pass into
    // change event
    fromConceptPreUpdates = new ConceptJpa(getFromConcept(), false);
    toConceptPreUpdates = new ConceptJpa(getToConcept(), false);

    //
    // Make a copy of the atoms to be moved
    //
    List<Atom> moveAtomsList = moveAtoms;

    //
    // Remove all atoms from the fromConcept
    //
    for (final Atom atom : moveAtomsList) {
      getFromConcept().getAtoms().remove(atom);
    }

    //
    // Update fromConcept
    //
    updateConcept(getFromConcept());

    //
    // Remove the objects from the database
    //
    // Not done for Atoms

    //
    // Change status of the atoms to be added
    //
    if (getChangeStatusFlag()) {
      for (final Atom atom : moveAtomsList) {
        atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
    }

    //
    // Add the atoms to the toConcept
    //
    for (final Atom atom : moveAtomsList) {
      getToConcept().getAtoms().add(atom);
    }

    //
    // Change status of the concepts
    //
    if (getChangeStatusFlag()) {
      getFromConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      getToConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // Update the to and from Concepts
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());

    // log the REST calls
    addLogEntry(getUserName(), getProject().getId(), getFromConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " " + atomIds + " from Concept " + getFromConcept().getId()
            + " to concept " + getToConcept().getId());
    addLogEntry(getUserName(), getProject().getId(), getFromConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " " + atomIds + " to Concept " + getToConcept().getId()
            + " from concept " + getFromConcept().getId());

    // Make copy of toConcept and fromConcept to pass into change event
    fromConceptPostUpdates = new ConceptJpa(getFromConcept(), false);
    toConceptPostUpdates = new ConceptJpa(getToConcept(), false);

  }

}