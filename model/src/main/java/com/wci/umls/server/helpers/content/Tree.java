/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import java.util.List;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Represents a tree of {@link TreePosition} objects.
 */
public interface Tree {

  /**
   * Merge specified tree with this one.
   *
   * @param tree the tree
   */
  public void mergeTree(Tree tree);

  /**
   * Returns the self.
   *
   * @return the self
   */
  public TreePosition<? extends ComponentHasAttributesAndName> getSelf();

  /**
   * Sets the self.
   *
   * @param self the self
   */
  public void setSelf(TreePosition<? extends ComponentHasAttributesAndName> self);

  /**
   * Returns the children.
   *
   * @return the children
   */
  public List<Tree> getChildren();

  /**
   * Sets the children.
   *
   * @param children the children
   */
  public void setChildren(List<Tree> children);

}