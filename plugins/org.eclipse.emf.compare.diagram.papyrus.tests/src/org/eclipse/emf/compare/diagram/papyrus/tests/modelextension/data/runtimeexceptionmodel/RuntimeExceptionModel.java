/*******************************************************************************
 * Copyright (c) 2015 EclipseSource Muenchen GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stefan Dirix - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.compare.diagram.papyrus.tests.modelextension.data.runtimeexceptionmodel;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.infra.core.resource.AbstractBaseModel;
import org.eclipse.papyrus.infra.core.resource.IModel;

/**
 * Test Model Class to test for models which throw a Runtime Exception.
 * 
 * @author Stefan Dirix <sdirix@eclipsesource.com>
 */

public class RuntimeExceptionModel extends AbstractBaseModel implements IModel {

	@Override
	protected String getModelFileExtension() {
		return "runtimeexceptionmodeltest";
	}

	@Override
	public String getIdentifier() {
		return "runtimeexceptionmodeltest";
	}

	@Override
	protected Map<Object, Object> getSaveOptions() {
		throw new NullPointerException();
	}

	// since Papyrus 2.0 we have to implement canPersist
	// we omit @Override on purpose to be backward compatible
	@SuppressWarnings("all")
	public boolean canPersist(EObject eObject) {
		return false;
	}

	// since Papyrus 2.0 we have to implement persist
	// we omit @Override on purpose to be backward compatible
	@SuppressWarnings("all")
	public void persist(EObject object) {
		// no implementation
	}
}
