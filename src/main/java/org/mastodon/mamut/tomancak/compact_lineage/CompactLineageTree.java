package org.mastodon.mamut.tomancak.compact_lineage;

import java.util.Objects;

public class CompactLineageTree {

	private final String name;
	private final CompactLineageTree left;
	private final CompactLineageTree right;

	private CompactLineageTree(String name, CompactLineageTree left, CompactLineageTree right) {
		this.name = name;
		this.left = left;
		this.right = right;
	}

	public static CompactLineageTree node(String name, CompactLineageTree left, CompactLineageTree right)
	{
		return new CompactLineageTree(name, Objects.requireNonNull(left), Objects.requireNonNull(right));
	}

	public static CompactLineageTree leaf(String name) {
		return new CompactLineageTree(name, null, null);
	}

	public String getName() {
		return name;
	}

	public CompactLineageTree getLeft() {
		return left;
	}

	public CompactLineageTree getRight() {
		return right;
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}
}
