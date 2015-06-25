/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Subset;

/**
 * JPA-enabled implementation of an {@link Concept} {@link Subset}.
 */
@Entity
@Table(name = "concept_subsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "conceptSubset")
public class ConceptSubsetJpa extends AbstractSubset implements ConceptSubset {

  /** The disjoint subset. */
  @Column(nullable = false)
  private boolean disjointSubset = false;

  /** The markersubset. */
  @Column(nullable = false)
  private boolean markerSubset = false;

  /** The members. */
  @OneToMany(mappedBy = "subset", targetEntity = ConceptSubsetMemberJpa.class)
  private List<ConceptSubsetMember> members = null;

  /**
   * Instantiates an empty {@link ConceptSubsetJpa}.
   */
  public ConceptSubsetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptSubsetJpa} from the specified parameters.
   *
   * @param subset the subset
   * @param deepCopy the deep copy
   */
  public ConceptSubsetJpa(ConceptSubset subset, boolean deepCopy) {
    super(subset, deepCopy);
    disjointSubset = subset.isDisjointSubset();
    markerSubset = subset.isMarkerSubset();
    if (deepCopy) {
      for (ConceptSubsetMember member : subset.getMembers()) {
        addMember(new ConceptSubsetMemberJpa(member, deepCopy));
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.ConceptSubset#getMembers()
   */
  @XmlElement(type = ConceptSubsetMemberJpa.class, name = "member")
  @Override
  public List<ConceptSubsetMember> getMembers() {
    if (members == null) {
      members = new ArrayList<>();
    }
    return members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.ConceptSubset#setMembers(java.util.List)
   */
  @Override
  public void setMembers(List<ConceptSubsetMember> members) {
    this.members = members;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.ConceptSubset#addMember(com.wci.umls.
   * server .model.content.ConceptSubsetMember)
   */
  @Override
  public void addMember(ConceptSubsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.add(member);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.ConceptSubset#removeMember(com.wci.umls.
   * server.model.content.ConceptSubsetMember)
   */
  @Override
  public void removeMember(ConceptSubsetMember member) {
    if (members == null) {
      members = new ArrayList<>();
    }
    members.remove(member);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#clearMembers()
   */
  @Override
  public void clearMembers() {
    members = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#isDisjointSubset()
   */
  @Override
  public boolean isDisjointSubset() {
    return disjointSubset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Subset#setDisjointSubset(boolean)
   */
  @Override
  public void setDisjointSubset(boolean disjointSubset) {
    this.disjointSubset = disjointSubset;
  }

  @Override
  public boolean isMarkerSubset() {
    return markerSubset;
  }

  @Override
  public void setMarkerSubset(boolean markerSubset) {
    this.markerSubset = disjointSubset;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (disjointSubset ? 1231 : 1237);
    result = prime * result + (markerSubset ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConceptSubsetJpa other = (ConceptSubsetJpa) obj;
    if (disjointSubset != other.disjointSubset)
      return false;
    if (markerSubset != other.markerSubset)
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [name=" + getName()
        + ", description=" + getDescription() + ", disjointSubset="
        + disjointSubset + "]";
  }

}
