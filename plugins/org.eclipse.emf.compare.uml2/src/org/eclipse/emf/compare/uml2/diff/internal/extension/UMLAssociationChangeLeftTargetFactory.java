/*******************************************************************************
 * Copyright (c) 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.compare.uml2.diff.internal.extension;

import java.util.Iterator;

import org.eclipse.emf.compare.diff.metamodel.AbstractDiffExtension;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
import org.eclipse.emf.compare.uml2.diff.UML2DiffEngine;
import org.eclipse.emf.compare.uml2diff.UML2DiffFactory;
import org.eclipse.emf.compare.uml2diff.UMLAssociationChangeLeftTarget;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Property;

public class UMLAssociationChangeLeftTargetFactory extends AbstractDiffExtensionFactory {
	
	public UMLAssociationChangeLeftTargetFactory(UML2DiffEngine engine) {
		super(engine);
	}

	public boolean handles(DiffElement input) {
		return input instanceof ModelElementChangeLeftTarget &&
		((ModelElementChangeLeftTarget) input).getLeftElement() instanceof Association;
	}

	public AbstractDiffExtension create(DiffElement input) {
		ModelElementChangeLeftTarget changeLeftTarget = (ModelElementChangeLeftTarget) input;
		final Association association = (Association) changeLeftTarget.getLeftElement();
		
		UMLAssociationChangeLeftTarget ret = UML2DiffFactory.eINSTANCE.createUMLAssociationChangeLeftTarget();
		
		for (Property memberEnd : association.getMemberEnds()) {
			Element memberOwner = memberEnd.getOwner();
			if (memberOwner != association) {
				/*
				 * We have to find the corresponding diff element (if it exists in order to hide
				 * it)
				 */
				final Iterator<EObject> diffIt = changeLeftTarget.eContainer().eAllContents();
				while (diffIt.hasNext()) {
					final EObject childElem = diffIt.next();
					if (childElem instanceof ModelElementChangeLeftTarget
							&& ((ModelElementChangeLeftTarget)childElem).getLeftElement() == memberEnd) {
						ret.getHideElements().add((ModelElementChangeLeftTarget)childElem);
					}
				}
			}
		}
		
		ret.getHideElements().add(changeLeftTarget);
		
		ret.setRightParent(changeLeftTarget.getRightParent());
		ret.setLeftElement(changeLeftTarget.getLeftElement());
		
		return ret;
	}
}
