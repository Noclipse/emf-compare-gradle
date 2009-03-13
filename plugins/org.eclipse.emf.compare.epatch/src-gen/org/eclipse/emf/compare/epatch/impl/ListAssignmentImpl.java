/**
 * <copyright>
 * </copyright>
 *
 */
package org.eclipse.emf.compare.epatch.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.compare.epatch.AssignmentValue;
import org.eclipse.emf.compare.epatch.EpatchPackage;
import org.eclipse.emf.compare.epatch.ListAssignment;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>List Assignment</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.emf.compare.epatch.impl.ListAssignmentImpl#getLeftValues <em>Left Values</em>}</li>
 *   <li>{@link org.eclipse.emf.compare.epatch.impl.ListAssignmentImpl#getRightValues <em>Right Values</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ListAssignmentImpl extends AssignmentImpl implements ListAssignment
{
  /**
   * The cached value of the '{@link #getLeftValues() <em>Left Values</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLeftValues()
   * @generated
   * @ordered
   */
  protected EList<AssignmentValue> leftValues;

  /**
   * The cached value of the '{@link #getRightValues() <em>Right Values</em>}' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getRightValues()
   * @generated
   * @ordered
   */
  protected EList<AssignmentValue> rightValues;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ListAssignmentImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return EpatchPackage.Literals.LIST_ASSIGNMENT;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<AssignmentValue> getLeftValues()
  {
    if (leftValues == null)
    {
      leftValues = new EObjectContainmentEList<AssignmentValue>(AssignmentValue.class, this, EpatchPackage.LIST_ASSIGNMENT__LEFT_VALUES);
    }
    return leftValues;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EList<AssignmentValue> getRightValues()
  {
    if (rightValues == null)
    {
      rightValues = new EObjectContainmentEList<AssignmentValue>(AssignmentValue.class, this, EpatchPackage.LIST_ASSIGNMENT__RIGHT_VALUES);
    }
    return rightValues;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case EpatchPackage.LIST_ASSIGNMENT__LEFT_VALUES:
        return ((InternalEList<?>)getLeftValues()).basicRemove(otherEnd, msgs);
      case EpatchPackage.LIST_ASSIGNMENT__RIGHT_VALUES:
        return ((InternalEList<?>)getRightValues()).basicRemove(otherEnd, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case EpatchPackage.LIST_ASSIGNMENT__LEFT_VALUES:
        return getLeftValues();
      case EpatchPackage.LIST_ASSIGNMENT__RIGHT_VALUES:
        return getRightValues();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case EpatchPackage.LIST_ASSIGNMENT__LEFT_VALUES:
        getLeftValues().clear();
        getLeftValues().addAll((Collection<? extends AssignmentValue>)newValue);
        return;
      case EpatchPackage.LIST_ASSIGNMENT__RIGHT_VALUES:
        getRightValues().clear();
        getRightValues().addAll((Collection<? extends AssignmentValue>)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case EpatchPackage.LIST_ASSIGNMENT__LEFT_VALUES:
        getLeftValues().clear();
        return;
      case EpatchPackage.LIST_ASSIGNMENT__RIGHT_VALUES:
        getRightValues().clear();
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case EpatchPackage.LIST_ASSIGNMENT__LEFT_VALUES:
        return leftValues != null && !leftValues.isEmpty();
      case EpatchPackage.LIST_ASSIGNMENT__RIGHT_VALUES:
        return rightValues != null && !rightValues.isEmpty();
    }
    return super.eIsSet(featureID);
  }

} //ListAssignmentImpl
