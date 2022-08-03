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
