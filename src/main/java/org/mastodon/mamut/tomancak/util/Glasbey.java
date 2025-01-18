/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.util;

import java.util.function.IntSupplier;

/**
 * Glasbey Color Tables.
 *
 * @author Matthias Arzt
 */
public class Glasbey
{

	private Glasbey()
	{
		// prevent instantiation
	}

	/**
	 * Returns a color supplier for light Glasbey colors.
	 * <br>
	 * Each call to {@link IntSupplier#getAsInt()} returns the next color from the table.
	 * The table contains 256 different colors, which are repeated if the supplier is
	 * called more than 256 times. The colors are meant to be distinguishable from each
	 * other. All colors are light colors, they have good contrast to black background
	 * or text.
	 */
	public static IntSupplier getGlasbeyLightColorSupplier()
	{
		return new ColorSupplier( GLASBEY_LIGHT );
	}

	private static class ColorSupplier implements IntSupplier
	{

		private final int[] colors;

		int index = 0;

		private ColorSupplier( final int[] colors )
		{
			this.colors = colors;
		}

		@Override
		public int getAsInt()
		{
			return colors[ index++ % colors.length ];
		}
	}

	/**
	 * A Glasbey color table with only light colors. It is taken from "colorcet", in the python package the color table is called
	 * "glasbey_light".
	 *
	 * @see <a href="https://colorcet.holoviz.org/user_guide/Categorical.html">https://colorcet.holoviz.org/user_guide/Categorical.html</a>
	 */
	private static final int[] GLASBEY_LIGHT = { 0xffd60000, 0xff018700, 0xffb500ff, 0xff05acc6, 0xff97ff00, 0xffffa52f, 0xffff8ec8,
			0xff79525e, 0xff00fdcf, 0xffafa5ff, 0xff93ac83, 0xff9a6900, 0xff366962, 0xffd3008c, 0xfffdf490, 0xffc86e66, 0xff9ee2ff,
			0xff00c846, 0xffa877ac, 0xffb8ba01, 0xfff4bfb1, 0xffff28fd, 0xfff2cdff, 0xff009e7c, 0xffff6200, 0xff56642a, 0xff953f1f,
			0xff90318e, 0xffff3464, 0xffa0e491, 0xff8c9ab1, 0xff829026, 0xffae083f, 0xff77c6ba, 0xffbc9157, 0xffe48eff, 0xff72b8ff,
			0xffc6a5c1, 0xffff9070, 0xffd3c37c, 0xffbceddb, 0xff6b8567, 0xff916e56, 0xfff9ff00, 0xffbac1df, 0xffac567c, 0xffffcd03,
			0xffff49b1, 0xffc15603, 0xff5d8c90, 0xffc144bc, 0xff00753f, 0xffba6efd, 0xff00d493, 0xff00ff75, 0xff49a150, 0xffcc9790,
			0xff00ebed, 0xffdb7e01, 0xfff77589, 0xffb89500, 0xffc84248, 0xff00cff9, 0xff755726, 0xff85d401, 0xffebffd4, 0xffa77b87,
			0xffdb72c8, 0xffcae256, 0xff8abf5d, 0xffa1216b, 0xff855b89, 0xff89bacf, 0xffffbad6, 0xffb6cfaa, 0xff97414d, 0xff67aa00,
			0xfffde1b1, 0xffff3628, 0xff80793d, 0xffd6e8ff, 0xffa795c6, 0xff7ea59a, 0xffd182a3, 0xff54823b, 0xffe6a872, 0xff9cffff,
			0xffda5480, 0xff05b3aa, 0xffffaaf6, 0xffd1afef, 0xffda015d, 0xffac1a13, 0xff60b385, 0xffd442fd, 0xffacaa59, 0xfffb9ca7,
			0xffb3723b, 0xfff26952, 0xffaed1d4, 0xff9affc3, 0xffdbb333, 0xffeb01c3, 0xff9900c4, 0xffcfff9e, 0xffa55949, 0xff3b6d01,
			0xff008579, 0xff959167, 0xff89dbb3, 0xff6d7400, 0xffaa5dca, 0xff07ef00, 0xff804f3d, 0xffd88052, 0xffffc862, 0xffb8009e,
			0xff99acdd, 0xff904f00, 0xff8c4470, 0xff4f6e52, 0xffff8734, 0xffc68ecd, 0xffd4e29e, 0xffb1826d, 0xff9cfb75, 0xff56dd77,
			0xfff90087, 0xffa1cdff, 0xff13cad1, 0xff118e54, 0xffd154a5, 0xff00dfc3, 0xffa3832f, 0xff77975b, 0xffbaaa80, 0xff70a3af,
			0xffd6fbff, 0xffe8013a, 0xffd84621, 0xffff82ed, 0xffb63862, 0xffb6cd72, 0xff97626b, 0xff897490, 0xff00a316, 0xff00f4a1,
			0xffbf90f2, 0xff89e4d8, 0xffa34d95, 0xff6e5d00, 0xff8cc68e, 0xff95aa2a, 0xffc672dd, 0xffb33b01, 0xffd69a36, 0xffdfacb6,
			0xff009aa0, 0xff599000, 0xff97bca8, 0xffac8ca8, 0xffdad4ff, 0xff547c72, 0xff00ba69, 0xffffc38e, 0xffb800d4, 0xffdfcf5b,
			0xff629a7b, 0xffbfedbc, 0xffc1bdfd, 0xff80d3dd, 0xffe2857e, 0xfff9eb4d, 0xffbf6d82, 0xffcaff4f, 0xffef72aa, 0xffed67ff,
			0xff9946ae, 0xff6d6942, 0xffe25660, 0xffdd662d, 0xff9cdb5d, 0xffe29ccf, 0xffb87500, 0xffc6002d, 0xffdfbcda, 0xff59b5df,
			0xffff59da, 0xff38c1a1, 0xff9e698c, 0xffacaac8, 0xff95622f, 0xffb55662, 0xff2b7e60, 0xffb1e400, 0xffeda590, 0xff95fde2,
			0xffff548e, 0xffbd6ea1, 0xffaa3b36, 0xffd8cf00, 0xffaa80cd, 0xffa08052, 0xffe100e8, 0xffc35b3d, 0xffb53a85, 0xff8c7700,
			0xffdbbc95, 0xff529e93, 0xffafbc82, 0xff91b5b6, 0xffa75423, 0xffffd4ef, 0xff79ae6b, 0xff5db54b, 0xff80fb9a, 0xff48ffef,
			0xff979548, 0xff9387a7, 0xff31d400, 0xff6ee956, 0xffb6d4eb, 0xff705470, 0xfff2db8a, 0xffaad4c1, 0xff7ecdf2, 0xff89ba00,
			0xff64b6ba, 0xffffb500, 0xffc38285, 0xffcaaa5e, 0xff647748, 0xff59e2ff, 0xffdf4dcd, 0xffe9ff79, 0xffbc66b8, 0xffc395a5,
			0xff64c674, 0xffd19570, 0xff70cf4f, 0xffaa6e66, 0xff9c60a5, 0xff00b800, 0xffe299b3, 0xffbc006b, 0xffb3e8ef, 0xffcdbfe4,
			0xff77a342, 0xff856277, 0xff568e5b, 0xff9eafc4, 0xffe82fa0, 0xff247c2a, 0xff826723, 0xffbfbc4d, 0xffddd3a5 };
}
