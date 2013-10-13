/*******************************************************************************
 * Copyright (c) 2013 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;

import static com.google.common.collect.Iterables.getFirst;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.eventbus.Subscribe;

import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.INavigatable;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareHandlerService;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.command.ICompareCopyCommand;
import org.eclipse.emf.compare.domain.ICompareEditingDomain;
import org.eclipse.emf.compare.domain.impl.EMFCompareEditingDomain;
import org.eclipse.emf.compare.ide.ui.internal.configuration.EMFCompareConfiguration;
import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.RedoAction;
import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.UndoAction;
import org.eclipse.emf.compare.ide.ui.internal.editor.ComparisonScopeInput;
import org.eclipse.emf.compare.ide.ui.internal.logical.ComparisonScopeBuilder;
import org.eclipse.emf.compare.ide.ui.internal.util.ExceptionUtil;
import org.eclipse.emf.compare.ide.ui.internal.util.JFaceUtil;
import org.eclipse.emf.compare.internal.utils.ComparisonUtil;
import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
import org.eclipse.emf.compare.rcp.ui.internal.configuration.ICompareEditingDomainChange;
import org.eclipse.emf.compare.rcp.ui.internal.configuration.IMergePreviewModeChange;
import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.IDifferenceFilterChange;
import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.filters.StructureMergeViewerFilter;
import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider;
import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProviderChange;
import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.StructureMergeViewerGrouper;
import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.provider.TreeItemProviderAdapterFactorySpec;
import org.eclipse.emf.compare.rcp.ui.internal.util.SWTUtil;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.tree.TreeFactory;
import org.eclipse.emf.edit.tree.TreeNode;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Implementation of {@link AbstractViewerWrapper}.
 * 
 * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
 */
public class EMFCompareStructureMergeViewer extends AbstractStructuredViewerWrapper<Composite, WrappableTreeViewer> implements CommandStackListener {

	/** The width of the tree ruler. */
	private static final int TREE_RULER_WIDTH = 17;

	/** The adapter factory. */
	private ComposedAdapterFactory fAdapterFactory;

	/** The tree ruler associated with this viewer. */
	private EMFCompareDiffTreeRuler treeRuler;

	private ICompareInputChangeListener fCompareInputChangeListener;

	/** The expand/collapse item listener. */
	private ITreeViewerListener fWrappedTreeListener;

	/** The tree viewer. */

	/** The undo action. */
	private UndoAction undoAction;

	/** The redo action. */
	private RedoAction redoAction;

	/** The compare handler service. */
	private CompareHandlerService fHandlerService;

	/**
	 * When comparing EObjects from a resource, the resource involved doesn't need to be unload by EMF
	 * Compare.
	 */
	private boolean resourcesShouldBeUnload;

	private DependencyData dependencyData;

	private ISelectionChangedListener selectionChangeListener;

	private final Job inputChangedTask = new Job("Compute Model Differences") {
		@Override
		public IStatus run(IProgressMonitor monitor) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, "Computing Model Differences", 100);
			compareInputChanged((ICompareInput)getInput(), subMonitor.newChild(100));
			return Status.OK_STATUS;
		}
	};

	private CompareToolBar toolBar;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the SWT parent control under which to create the viewer's SWT control.
	 * @param config
	 *            a compare configuration the newly created viewer might want to use.
	 */
	public EMFCompareStructureMergeViewer(Composite parent, EMFCompareConfiguration config) {
		super(parent, config);

		StructureMergeViewerFilter structureMergeViewerFilter = getCompareConfiguration()
				.getStructureMergeViewerFilter();
		getViewer().addFilter(structureMergeViewerFilter);

		StructureMergeViewerGrouper structureMergeViewerGrouper = getCompareConfiguration()
				.getStructureMergeViewerGrouper();
		structureMergeViewerGrouper.install(getViewer());

		toolBar = new CompareToolBar(structureMergeViewerGrouper, structureMergeViewerFilter,
				getCompareConfiguration());
		getViewer().addSelectionChangedListener(toolBar);
		toolBar.initToolbar(CompareViewerPane.getToolBarManager(parent), getViewer());

		selectionChangeListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChangedEvent(event);
			}
		};
		addSelectionChangedListener(selectionChangeListener);

		fHandlerService = CompareHandlerService.createFor(getCompareConfiguration().getContainer(),
				getControl().getShell());

		setContentProvider(new EMFCompareStructureMergeViewerContentProvider(getCompareConfiguration()
				.getAdapterFactory()));
		setLabelProvider(new DelegatingStyledCellLabelProvider(
				new EMFCompareStructureMergeViewerLabelProvider(
						getCompareConfiguration().getAdapterFactory(), this)));

		undoAction = new UndoAction(null);
		redoAction = new RedoAction(null);

		inputChangedTask.setPriority(Job.LONG);
		config.getEventBus().register(this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractViewerWrapper#preHookCreateControlAndViewer()
	 */
	@Override
	protected void preHookCreateControlAndViewer() {
		fAdapterFactory = new ComposedAdapterFactory(EMFCompareRCPPlugin.getDefault()
				.getAdapterFactoryRegistry());

		fAdapterFactory.addAdapterFactory(new TreeItemProviderAdapterFactorySpec());
		fAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		fAdapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());

		getCompareConfiguration().setAdapterFactory(fAdapterFactory);

		dependencyData = new DependencyData(getCompareConfiguration());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see 
	 *      org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.ViewerWrapper.createControl(Composite,
	 *      CompareConfiguration)
	 */
	@Override
	protected ControlAndViewer<Composite, WrappableTreeViewer> createControlAndViewer(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		control.setLayout(layout);
		control.setLayoutData(data);
		final WrappableTreeViewer treeViewer = new EMFCompareDiffTreeViewer(control, dependencyData);
		dependencyData.setTreeViewer(treeViewer);
		INavigatable nav = new Navigatable(fAdapterFactory, treeViewer);
		control.setData(INavigatable.NAVIGATOR_PROPERTY, nav);
		control.setData(CompareUI.COMPARE_VIEWER_TITLE, "Model differences");
		treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
		layoutData.widthHint = TREE_RULER_WIDTH;
		layoutData.minimumWidth = TREE_RULER_WIDTH;
		treeRuler = new EMFCompareDiffTreeRuler(control, SWT.NONE, layoutData.widthHint, treeViewer,
				dependencyData);
		treeRuler.setLayoutData(layoutData);

		fCompareInputChangeListener = new ICompareInputChangeListener() {
			public void compareInputChanged(ICompareInput input) {
				EMFCompareStructureMergeViewer.this.compareInputChanged(input);
			}
		};

		fWrappedTreeListener = new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent event) {
				treeRuler.redraw();
			}

			public void treeCollapsed(TreeExpansionEvent event) {
				treeRuler.redraw();
			}
		};
		treeViewer.addTreeListener(fWrappedTreeListener);

		fHandlerService = CompareHandlerService.createFor(getCompareConfiguration().getContainer(),
				treeViewer.getControl().getShell());

		return ControlAndViewer.create(control, treeViewer);
	}

	@Subscribe
	public void handleEditingDomainChange(ICompareEditingDomainChange event) {
		editingDomainChange(event.getOldValue(), event.getNewValue());
	}

	protected void editingDomainChange(ICompareEditingDomain oldValue, ICompareEditingDomain newValue) {
		if (newValue != oldValue) {
			if (oldValue != null) {
				oldValue.getCommandStack().removeCommandStackListener(this);
			}

			if (newValue != null) {
				newValue.getCommandStack().addCommandStackListener(this);
				// setLeftDirty(newValue.getCommandStack().isLeftSaveNeeded());
				// setRightDirty(newValue.getCommandStack().isRightSaveNeeded());
			}

			undoAction.setEditingDomain(newValue);
			redoAction.setEditingDomain(newValue);
		}
	}

	private void refreshTitle() {
		Composite parent = getControl().getParent();
		if (parent instanceof CompareViewerSwitchingPane) {
			int displayedDiff = JFaceUtil.filterVisibleElement(getViewer(), IS_DIFF).size();
			Comparison comparison = getCompareConfiguration().getComparison();
			if (comparison != null) {
				int computedDiff = comparison.getDifferences().size();
				int filteredDiff = computedDiff - displayedDiff;
				((CompareViewerSwitchingPane)parent).setTitleArgument(computedDiff + " differences – "
						+ filteredDiff + " differences filtered from view");
			}
		}
	}

	private static final Predicate<? super Object> IS_DIFF = new Predicate<Object>() {
		public boolean apply(Object object) {
			return getDataOfTreeNodeOfAdapter(object) instanceof Diff;
		}
	};

	static EObject getDataOfTreeNodeOfAdapter(Object object) {
		EObject data = null;
		if (object instanceof Adapter) {
			Notifier target = ((Adapter)object).getTarget();
			if (target instanceof TreeNode) {
				data = ((TreeNode)target).getData();
			}
		}
		return data;
	}

	static final Function<Object, EObject> ADAPTER__TARGET__DATA = new Function<Object, EObject>() {
		public EObject apply(Object object) {
			return getDataOfTreeNodeOfAdapter(object);
		}
	};

	@Subscribe
	public void mergePreviewModeChange(IMergePreviewModeChange event) {
		dependencyData.updateDependencies(getSelection());
		getControl().redraw();
	}

	@Subscribe
	public void handleDifferenceFilterChange(IDifferenceFilterChange event) {
		SWTUtil.safeRefresh(this, true);
	}

	@Subscribe
	public void handleDifferenceGroupProviderChange(IDifferenceGroupProviderChange event) {
		SWTUtil.safeRefresh(this, true);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(Object, Object)
	 */
	@Override
	protected void inputChanged(Object input, Object oldInput) {
		if (oldInput instanceof ICompareInput) {
			ICompareInput old = (ICompareInput)oldInput;
			old.removeCompareInputChangeListener(fCompareInputChangeListener);
		}
		if (input instanceof ICompareInput) {
			ICompareInput ci = (ICompareInput)input;
			ci.addCompareInputChangeListener(fCompareInputChangeListener);

			// Hack to display a message in the tree viewer while the differences are being computed.
			TreeItem item = new TreeItem(getViewer().getTree(), SWT.NONE);
			item.setText("Computing model differences...");

			compareInputChanged(ci);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractViewerWrapper#handleDispose(DisposeEvent)
	 */
	@Override
	protected void handleDispose(DisposeEvent event) {
		if (fHandlerService != null) {
			fHandlerService.dispose();
		}
		getCompareConfiguration().getEventBus().unregister(this);
		getViewer().removeTreeListener(fWrappedTreeListener);
		Object input = getInput();
		if (input instanceof ICompareInput) {
			ICompareInput ci = (ICompareInput)input;
			ci.removeCompareInputChangeListener(fCompareInputChangeListener);
		}
		removeSelectionChangedListener(selectionChangeListener);
		getViewer().removeSelectionChangedListener(toolBar);
		compareInputChanged((ICompareInput)null);
		treeRuler.handleDispose();
		fAdapterFactory.dispose();
		toolBar.dispose();
		super.handleDispose(event);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.common.command.CommandStackListener#commandStackChanged(java.util.EventObject)
	 */
	public void commandStackChanged(EventObject event) {
		if (undoAction != null) {
			undoAction.update();
		}
		if (redoAction != null) {
			redoAction.update();
		}

		Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
		if (mostRecentCommand instanceof ICompareCopyCommand) {
			Collection<?> affectedObjects = mostRecentCommand.getAffectedObjects();

			if (!affectedObjects.isEmpty()) {
				// MUST NOT call a setSelection with a list, o.e.compare does not handle it (cf
				// org.eclipse.compare.CompareEditorInput#getElement(ISelection))
				Object first = getFirst(affectedObjects, null);
				if (first instanceof EObject) {
					IDifferenceGroupProvider groupProvider = getCompareConfiguration()
							.getStructureMergeViewerGrouper().getProvider();
					Iterable<TreeNode> treeNodes = groupProvider.getTreeNodes((EObject)first);
					TreeNode treeNode = getFirst(treeNodes, null);
					if (treeNode != null) {
						final Object adaptedAffectedObject = fAdapterFactory.adapt(treeNode,
								ICompareInput.class);
						SWTUtil.safeAsyncExec(new Runnable() {
							public void run() {
								refresh();
								setSelection(new StructuredSelection(adaptedAffectedObject), true);
							}
						});
					}
				}
			}
		} else {
			// FIXME, should recompute the difference, something happened outside of this compare editor
		}

	}

	/**
	 * Triggered by fCompareInputChangeListener and {@link #inputChanged(Object, Object)}.
	 */
	void compareInputChanged(ICompareInput input) {
		if (input == null) {
			// When closing, we don't need a progress monitor to handle the input change
			compareInputChanged((ICompareInput)null, new NullProgressMonitor());
			return;
		}
		// The compare configuration is nulled when the viewer is disposed
		if (getCompareConfiguration() != null) {
			inputChangedTask.schedule();
		}
	}

	void compareInputChanged(CompareInputAdapter input, IProgressMonitor monitor) {
		compareInputChanged(null, (Comparison)input.getComparisonObject());
	}

	void compareInputChanged(ComparisonScopeInput input, IProgressMonitor monitor) {
		EMFCompare comparator = getCompareConfiguration().getEMFComparator();

		IComparisonScope comparisonScope = input.getComparisonScope();
		Comparison comparison = comparator.compare(comparisonScope, BasicMonitor.toMonitor(monitor));

		reportErrors(comparison);

		compareInputChanged(input.getComparisonScope(), comparison);
	}

	void compareInputChanged(final IComparisonScope scope, final Comparison comparison) {
		if (!getControl().isDisposed()) { // guard against disposal
			final TreeNode treeNode = TreeFactory.eINSTANCE.createTreeNode();
			treeNode.setData(comparison);
			final Object input = fAdapterFactory.adapt(treeNode, ICompareInput.class);

			// this will set to the EMPTY difference group provider, but necessary to avoid NPE while setting
			// input.
			IDifferenceGroupProvider groupProvider = getCompareConfiguration()
					.getStructureMergeViewerGrouper().getProvider();
			treeNode.eAdapters().add(groupProvider);

			// must set the input now in a synchronous mean. It will be used in the #setComparisonAndScope
			// afterwards during the initialization of StructureMergeViewerFilter and
			// StructureMergeViewerGrouper.
			SWTUtil.safeSyncExec(new Runnable() {
				public void run() {
					getViewer().setInput(input);
				}
			});

			getCompareConfiguration().setComparisonAndScope(comparison, scope);

			SWTUtil.safeAsyncExec(new Runnable() {
				public void run() {
					// the tree has now a proper group provider and its input, so we can create the child
					// silently.
					// ((EMFCompareDiffTreeViewer)getViewer()).createChildrenSilently();

					// title is not initialized as the comparison was set in the configuration after the
					// refresh caused by the initialization of the viewer filters and the groupe providers.
					refreshTitle();

					// XXX: fixme!!
					// ((EMFCompareDiffTreeViewer)getViewer()).initialSelection();
				}
			});

			SWTUtil.safeAsyncExec(new Runnable() {
				public void run() {
					fHandlerService.updatePaneActionHandlers(new Runnable() {
						public void run() {
							fHandlerService.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
							fHandlerService.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);

						}
					});
				}
			});
		}
	}

	void compareInputChanged(ICompareInput input, IProgressMonitor monitor) {
		if (input != null) {
			if (input instanceof CompareInputAdapter) {
				resourcesShouldBeUnload = false;
				compareInputChanged((CompareInputAdapter)input, monitor);
			} else if (input instanceof ComparisonScopeInput) {
				resourcesShouldBeUnload = false;
				compareInputChanged((ComparisonScopeInput)input, monitor);
			} else {
				resourcesShouldBeUnload = true;
				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

				final ITypedElement left = input.getLeft();
				final ITypedElement right = input.getRight();
				final ITypedElement origin = input.getAncestor();

				IComparisonScope scope = null;
				try {
					scope = ComparisonScopeBuilder.create(getCompareConfiguration().getContainer(), left,
							right, origin, subMonitor.newChild(85));
				} catch (Exception e) {
					ExceptionUtil.handleException(e, getCompareConfiguration(), true);
					return;
				}
				final Comparison compareResult = EMFCompare
						.builder()
						.setMatchEngineFactoryRegistry(
								EMFCompareRCPPlugin.getDefault().getMatchEngineFactoryRegistry())
						.setPostProcessorRegistry(EMFCompareRCPPlugin.getDefault().getPostProcessorRegistry())
						.build().compare(scope, BasicMonitor.toMonitor(subMonitor.newChild(15)));

				reportErrors(compareResult);

				final ResourceSet leftResourceSet = (ResourceSet)scope.getLeft();
				final ResourceSet rightResourceSet = (ResourceSet)scope.getRight();
				final ResourceSet originResourceSet = (ResourceSet)scope.getOrigin();

				ICompareEditingDomain editingDomain = EMFCompareEditingDomain.create(leftResourceSet,
						rightResourceSet, originResourceSet);
				getCompareConfiguration().setEditingDomain(editingDomain);

				compareInputChanged(scope, compareResult);
			}
		} else {
			compareInputChangedToNull();
		}
	}

	private void compareInputChangedToNull() {
		ResourceSet leftResourceSet = null;
		ResourceSet rightResourceSet = null;
		ResourceSet originResourceSet = null;

		if (getCompareConfiguration().getComparison() != null) {
			Comparison comparison = getCompareConfiguration().getComparison();
			Iterator<Match> matchIt = comparison.getMatches().iterator();
			if (comparison.isThreeWay()) {
				while (matchIt.hasNext()
						&& (leftResourceSet == null || rightResourceSet == null || originResourceSet == null)) {
					Match match = matchIt.next();
					if (leftResourceSet == null) {
						leftResourceSet = getResourceSet(match.getLeft());
					}
					if (rightResourceSet == null) {
						rightResourceSet = getResourceSet(match.getRight());
					}
					if (originResourceSet == null) {
						originResourceSet = getResourceSet(match.getOrigin());
					}
				}
			} else {
				while (matchIt.hasNext() && (leftResourceSet == null || rightResourceSet == null)) {
					Match match = matchIt.next();
					if (leftResourceSet == null) {
						leftResourceSet = getResourceSet(match.getLeft());
					}
					if (rightResourceSet == null) {
						rightResourceSet = getResourceSet(match.getRight());
					}
				}
			}
		}

		editingDomainChange(getCompareConfiguration().getEditingDomain(), null);

		if (resourcesShouldBeUnload) {
			unload(leftResourceSet);
			unload(rightResourceSet);
			unload(originResourceSet);
		}

		if (getCompareConfiguration() != null) {
			getCompareConfiguration().dispose();
		}
		getViewer().setInput(null);
	}

	private void reportErrors(final Comparison comparison) {
		if (ComparisonUtil.containsErrors(comparison)) {
			SWTUtil.safeAsyncExec(new Runnable() {
				public void run() {
					DiagnosticDialog.open(getControl().getShell(), "Comparison report", //$NON-NLS-1$
							"Some issues were detected.", comparison.getDiagnostic()); //$NON-NLS-1$
				}
			});
		}
	}

	private static void unload(ResourceSet resourceSet) {
		if (resourceSet != null) {
			for (Resource resource : resourceSet.getResources()) {
				resource.unload();
			}
			resourceSet.getResources().clear();
		}
	}

	private static ResourceSet getResourceSet(EObject eObject) {
		if (eObject != null) {
			Resource eResource = eObject.eResource();
			if (eResource != null) {
				return eResource.getResourceSet();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	@Override
	protected void internalRefresh(Object element) {
		getViewer().refresh();

		dependencyData.updateTreeItemMappings();
		dependencyData.updateDependencies(getSelection());
		
		getControl().redraw();
		
		refreshTitle();
	}

	private void handleSelectionChangedEvent(SelectionChangedEvent event) {
		dependencyData.updateDependencies(event.getSelection());
		getControl().redraw();
	}

}
