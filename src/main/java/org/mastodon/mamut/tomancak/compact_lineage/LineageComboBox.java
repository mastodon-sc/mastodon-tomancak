/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
