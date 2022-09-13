/*******************************************************************************
 * Copyright (c) 2012, 2017 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *     Philip Langer - adds further test cases
 *     Martin Fleck - add EMFResourceMappingMergerPreMergeTest
 *******************************************************************************/
package org.eclipse.emf.compare.ide.ui.tests.suite;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.egit.core.GitCorePreferences;
import org.eclipse.emf.compare.ComparePackage;
import org.eclipse.emf.compare.ide.ui.tests.merge.AdditiveMergeTests;
import org.eclipse.emf.compare.ide.ui.tests.merge.EMFResourceMappingMergerPreMergeTest;
import org.eclipse.emf.compare.ide.ui.tests.merge.GitResourceVariantTreeSubscriberTest;
import org.eclipse.emf.compare.ide.ui.tests.merge.MultipleAncestorsTest;
import org.eclipse.emf.compare.ide.ui.tests.merge.RemoteNewProjectTests;
import org.eclipse.emf.compare.ide.ui.tests.merge.RenamedControlledResourceTests;
import org.eclipse.emf.compare.ide.ui.tests.merge.ResourceVariantTest;
import org.eclipse.emf.compare.ide.ui.tests.merge.TreeWalkResourceVariantTreeProviderTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.GitLogicalMergeTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.GitLogicalMergeWithCustomDependenciesTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.GitLogicalModelTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.GitMergeTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.ModelResolverLocalTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.ModelResolverRemoteTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.MovedImplicitResourceAmongChangedResourcesTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.ResourceUtilPathTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.RevisionedURIConverterTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.ThreadedModelResolverResolutionTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.incoming.IncomingMultiEReferenceTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.incoming.IncomingSingleEReferenceOldSerializationTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.incoming.IncomingSingleEReferenceTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.outgoing.OutgoingMultiEReferenceTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.outgoing.OutgoingSingleEReferenceOldSerializationTest;
import org.eclipse.emf.compare.ide.ui.tests.unit.outgoing.OutgoingSingleEReferenceTest;
import org.eclipse.emf.compare.tests.nodes.NodesPackage;
import org.eclipse.emf.compare.tests.nodes.util.NodesResourceFactoryImpl;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({AdditiveMergeTests.class, GitLogicalMergeTest.class,
		GitLogicalMergeWithCustomDependenciesTest.class, GitLogicalModelTest.class, GitMergeTest.class,
		GitResourceVariantTreeSubscriberTest.class, IncomingMultiEReferenceTest.class,
		IncomingSingleEReferenceOldSerializationTest.class, IncomingSingleEReferenceTest.class,
		OutgoingSingleEReferenceOldSerializationTest.class, OutgoingSingleEReferenceTest.class,
		OutgoingMultiEReferenceTest.class, ModelResolverLocalTest.class, ModelResolverRemoteTest.class,
		MovedImplicitResourceAmongChangedResourcesTest.class, RenamedControlledResourceTests.class,
		RemoteNewProjectTests.class, ResourceVariantTest.class, ResourceUtilPathTest.class,
		RevisionedURIConverterTest.class, ThreadedModelResolverResolutionTest.class,
		TreeWalkResourceVariantTreeProviderTest.class, EMFResourceMappingMergerPreMergeTest.class,
		MultipleAncestorsTest.class, })
public class GitTests {

	@BeforeClass
	public static void disableEGitAutomaticBehavior() {
		// suppress auto-ignoring and auto-sharing to avoid interference
		IEclipsePreferences eGitPreferences = InstanceScope.INSTANCE
				.getNode(org.eclipse.egit.core.Activator.PLUGIN_ID);
		eGitPreferences.put(GitCorePreferences.core_preferredMergeStrategy, "model recursive");
		eGitPreferences.putBoolean(GitCorePreferences.core_autoShareProjects, false);
	}

	@BeforeClass
	public static void fillEMFRegistries() {
		EPackage.Registry.INSTANCE.put(ComparePackage.eNS_URI, ComparePackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(NodesPackage.eNS_URI, NodesPackage.eINSTANCE);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("nodes", //$NON-NLS-1$
				new NodesResourceFactoryImpl());
	}
}
