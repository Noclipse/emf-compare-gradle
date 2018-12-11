/*******************************************************************************
 * Copyright (c) 2013, 2018 Obeo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *     Martin Fleck - bug 514767
 *     Martin Fleck - bug 514415
 *     Philip Langer - bug 514079
 *******************************************************************************/
package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.internal.merge.MergeMode;
import org.eclipse.emf.compare.merge.DiffRelationshipComputer;
import org.eclipse.emf.compare.merge.IDiffRelationshipComputer;
import org.eclipse.emf.compare.merge.IMerger;
import org.eclipse.emf.compare.rcp.ui.internal.configuration.IEMFCompareConfiguration;
import org.eclipse.emf.compare.rcp.ui.structuremergeviewer.groups.IDifferenceGroupProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.tree.TreeNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
 */
public class DependencyData {

	private final IEMFCompareConfiguration compareConfiguration;

	private Set<Diff> requires;

	private Set<Diff> rejectedDiffs;

	public DependencyData(IEMFCompareConfiguration compareConfiguration) {
		this.compareConfiguration = compareConfiguration;
		requires = newHashSet();
		rejectedDiffs = newHashSet();
	}

	/**
	 * Returns the diff relationship computer instance from the compare configuration with the given merger
	 * registry. If no computer instance has been set, a default instance will be created.
	 * 
	 * @param mergerRegistry
	 *            merger registry used to compute diff relationships.
	 * @return a non-null diff relationship computer.
	 */
	protected IDiffRelationshipComputer getDiffRelationshipComputer(IMerger.Registry mergerRegistry) {
		if (compareConfiguration == null || compareConfiguration.getDiffRelationshipComputer() == null) {
			return new DiffRelationshipComputer(mergerRegistry);
		}
		IDiffRelationshipComputer diffRelationshipComputer = compareConfiguration
				.getDiffRelationshipComputer();
		diffRelationshipComputer.setMergerRegistry(mergerRegistry);
		return diffRelationshipComputer;
	}

	/**
	 * @param selection
	 */
	public void updateDependencies(ISelection selection, IMerger.Registry mergerRegistry) {
		boolean leftEditable = compareConfiguration.isLeftEditable();
		boolean rightEditable = compareConfiguration.isRightEditable();
		if (leftEditable || rightEditable) {
			Iterable<Diff> selectedDiffs = filter(getSelectedComparisonObjects(selection), Diff.class);

			MergeMode mergePreviewMode = compareConfiguration.getMergePreviewMode();
			if (compareConfiguration.isMirrored() && (mergePreviewMode == MergeMode.LEFT_TO_RIGHT
					|| mergePreviewMode == MergeMode.RIGHT_TO_LEFT)) {
				mergePreviewMode = mergePreviewMode.inverse();
			}

			requires = newHashSet();
			rejectedDiffs = newHashSet();
			for (Diff diff : selectedDiffs) {
				boolean leftToRight = mergePreviewMode.isLeftToRight(diff, leftEditable, rightEditable);
				IDiffRelationshipComputer computer = getDiffRelationshipComputer(mergerRegistry);
				requires.addAll(computer.getAllResultingMerges(diff, !leftToRight));
				requires.remove(diff);
				rejectedDiffs.addAll(computer.getAllResultingRejections(diff, !leftToRight));
				rejectedDiffs.remove(diff);
				requires.removeAll(rejectedDiffs);
			}
		}
	}

	public void clearDependencies() {
		requires = newHashSet();
		rejectedDiffs = newHashSet();
	}

	private static List<EObject> getSelectedComparisonObjects(ISelection selection) {
		List<EObject> ret = newArrayList();
		if (selection instanceof IStructuredSelection) {
			List<?> selectedObjects = ((IStructuredSelection)selection).toList();
			Iterable<EObject> data = transform(selectedObjects, ADAPTER__TARGET__DATA);
			Iterable<EObject> notNullData = Iterables.filter(data, notNull());
			addAll(ret, notNullData);
		}
		return ret;
	}

	private static final Function<Object, EObject> ADAPTER__TARGET__DATA = new Function<Object, EObject>() {
		public EObject apply(Object object) {
			return EMFCompareStructureMergeViewer.getDataOfTreeNodeOfAdapter(object);
		}
	};

	/**
	 * @return the requires
	 */
	public Set<Diff> getRequires() {
		return requires;
	}

	/**
	 * @return the unmergeables
	 */
	public Set<Diff> getRejections() {
		return rejectedDiffs;
	}

	public Collection<TreeNode> getTreeNodes(Diff diff) {
		final List<TreeNode> nodes = new ArrayList<TreeNode>();
		IDifferenceGroupProvider groupProvider = compareConfiguration.getStructureMergeViewerGrouper()
				.getProvider();
		nodes.addAll(groupProvider.getTreeNodes(diff));
		return nodes;
	}
}
