package org.mastodon.mamut.tomancak.compact_lineage;

import org.scijava.listeners.Listeners;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Pretty much the same as {@link JComboBox}. Slight differences:
 * - shows the tree names in the list
 * - trees are sorted by name
 * - listener is nicer
 */
public class LineageComboBox extends JPanel {

	private final JComboBox<WrappedTree> comboBox;

	private final Listeners.SynchronizedList<Consumer<CompactLineageTree>> listeners = new Listeners.SynchronizedList<>();

	public LineageComboBox(List<CompactLineageTree> trees) {
		WrappedTree[] wrappedTrees = trees.stream().map(WrappedTree::new).toArray(WrappedTree[]::new);
		Arrays.sort(wrappedTrees, Comparator.comparing(WrappedTree::toString));
		comboBox = new JComboBox<>(wrappedTrees);
		comboBox.addActionListener(x -> onLineageComboBoxChanged());
		add(comboBox);
	}

	private void onLineageComboBoxChanged() {
		CompactLineageTree selectedTree = getSelectedItem();
		for(Consumer<CompactLineageTree> listener : listeners.list)
			listener.accept(selectedTree);
	}

	public Listeners<Consumer<CompactLineageTree>> selectionChangedListeners() {
		return listeners;
	}

	private CompactLineageTree getSelectedItem() {
		return ((WrappedTree) comboBox.getSelectedItem()).tree;
	}

	private static class WrappedTree {

		private final CompactLineageTree tree;

		private WrappedTree(CompactLineageTree tree) {
			this.tree = tree;
		}

		@Override
		public String toString() {
			return tree.getName();
		}
	}
}
