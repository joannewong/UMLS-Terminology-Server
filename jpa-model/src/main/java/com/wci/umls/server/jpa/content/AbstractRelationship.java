/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Relationship;

/**
 * Abstract JPA-enabled implementation of {@link Relationship}.
 *
 * @param <S> the left hand side of the relationship
 * @param <T> the right hand side of the relationship
 */
@Audited
@MappedSuperclass
public abstract class AbstractRelationship<S extends ComponentHasAttributes, T extends ComponentHasAttributes>
    extends AbstractComponentHasAttributes implements Relationship<S, T> {

  /** The relationship type. */
  @Column(nullable = false)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The group. */
  @Column(name = "relGroup", nullable = true)
  private String group;

  /** The inferred. */
  @Column(nullable = false)
  private boolean inferred;

  /** The stated. */
  @Column(nullable = false)
  private boolean stated;

  /** The asserted direction flag. */
  @Column(nullable = false)
  private boolean isAssertedDirection;

  /**
   * Instantiates an empty {@link AbstractRelationship}.
   */
  public AbstractRelationship() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractRelationship} from the specified parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public AbstractRelationship(Relationship<S, T> relationship,
      boolean deepCopy) {
    super(relationship, deepCopy);
    relationshipType = relationship.getRelationshipType();
    additionalRelationshipType = relationship.getAdditionalRelationshipType();
    group = relationship.getGroup();
    inferred = relationship.isInferred();
    stated = relationship.isStated();
    isAssertedDirection = relationship.isAssertedDirection();
  }

  /**
   * Returns the relationship type.
   *
   * @return the relationship type
   */
  @Override
  public String getRelationshipType() {
    return relationshipType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setRelationshipType(java
   * .lang.String)
   */
  @Override
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /**
   * Returns the additional relationship type.
   *
   * @return the additional relationship type
   */
  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /**
   * Sets the additional relationship type.
   *
   * @param additionalRelationshipType the additional relationship type
   */
  @Override
  public void setAdditionalRelationshipType(String additionalRelationshipType) {
    this.additionalRelationshipType = additionalRelationshipType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#getGroup()
   */
  @Override
  public String getGroup() {
    return group;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setGroup(java.lang.String)
   */
  @Override
  public void setGroup(String group) {
    this.group = group;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#isInferred()
   */
  @Override
  public boolean isInferred() {
    return inferred;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#setInferred(boolean)
   */
  @Override
  public void setInferred(boolean inferred) {
    this.inferred = inferred;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#isStated()
   */
  @Override
  public boolean isStated() {
    return stated;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#setStated(boolean)
   */
  @Override
  public void setStated(boolean stated) {
    this.stated = stated;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#isAssertedDirection()
   */
  @Override
  public boolean isAssertedDirection() {
    return isAssertedDirection;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setAssertedDirection(boolean
   * )
   */
  @Override
  public void setAssertedDirection(boolean assertedDirection) {
    this.isAssertedDirection = assertedDirection;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((additionalRelationshipType == null) ? 0
                : additionalRelationshipType.hashCode());
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + (inferred ? 1231 : 1237);
    result = prime * result + (isAssertedDirection ? 1231 : 1237);
    result =
        prime * result
            + ((relationshipType == null) ? 0 : relationshipType.hashCode());
    result = prime * result + (stated ? 1231 : 1237);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractRelationship<?, ?> other = (AbstractRelationship<?, ?>) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (group == null) {
      if (other.group != null)
        return false;
    } else if (!group.equals(other.group))
      return false;
    if (inferred != other.inferred)
      return false;
    if (isAssertedDirection != other.isAssertedDirection)
      return false;
    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    if (stated != other.stated)
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
    return "AbstractRelationship [relationshipType=" + relationshipType
        + ", additionalRelationshipType=" + additionalRelationshipType
        + ", group=" + group + ", inferred=" + inferred + ", stated=" + stated
        + ", isAssertedDirection=" + isAssertedDirection + "]";
  }

}