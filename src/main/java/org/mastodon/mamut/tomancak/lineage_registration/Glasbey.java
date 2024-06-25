/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch
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
package org.mastodon.mamut.tomancak.lineage_registration;

/**
 * Glasbey Color Tables.
 *
 * @author Matthias Arzt
 */
public class Glasbey
{
	/**
	 * Fiji's Glasbey LUT. (Glasbey on white background)
	 * <p>
	 * The color with index 0 is white. It is my impression that the
	 * first 5 colors (white, blue, gree, red, very dark blue) don't really
	 * fit well with the rest of the set.
	 */
	public static final int[] GLASBEY = { 0xffffffff, 0xff0000ff, 0xffff0000, 0xff00ff00, 0xff000033, 0xffff00b6, 0xff005300, 0xffffd300,
			0xff009fff, 0xff9a4d42, 0xff00ffbe, 0xff783fc1, 0xff1f9698, 0xffffacfd, 0xffb1cc71, 0xfff1085c, 0xfffe8f42, 0xffdd00ff,
			0xff201a01, 0xff720055, 0xff766c95, 0xff02ad24, 0xffc8ff00, 0xff886c00, 0xffffb79f, 0xff858567, 0xffa10300, 0xff14f9ff,
			0xff00479e, 0xffdc5e93, 0xff93d4ff, 0xff004cff, 0xff004250, 0xff39a76a, 0xffee70fe, 0xff000064, 0xffabf5cc, 0xffa192ff,
			0xffa4ff73, 0xffffce71, 0xff470015, 0xffd4adc5, 0xfffb766f, 0xffabbc00, 0xff7500d7, 0xffa6009a, 0xff0073fe, 0xffa55dae,
			0xff628402, 0xff0079a8, 0xff00ff83, 0xff563500, 0xff9f003f, 0xff422d42, 0xfffff2bb, 0xff005d43, 0xfffcff7c, 0xff9fbfba,
			0xffa75413, 0xff4a276c, 0xff0010a6, 0xff914e6d, 0xffcf9500, 0xffc3bbff, 0xfffd4440, 0xff424e20, 0xff6a0100, 0xffb58354,
			0xff84e993, 0xff60d900, 0xffff6fd3, 0xff664b3f, 0xfffe6400, 0xffe4037f, 0xff11c7ae, 0xffd2818b, 0xff5b767c, 0xff203b6a,
			0xffb454ff, 0xffe208d2, 0xff000114, 0xff5d8444, 0xffa6faff, 0xff617bc9, 0xff62007a, 0xff7ebe3a, 0xff003cb7, 0xfffffd00,
			0xff07c5e2, 0xffb4a739, 0xff94ba8a, 0xffccbba0, 0xff370031, 0xff002801, 0xff967a81, 0xff278826, 0xffce82b4, 0xff96a4c4,
			0xffb42080, 0xff6e56b4, 0xff9300b9, 0xffc7303d, 0xff7366ff, 0xff0fbbfd, 0xffaca464, 0xffb675fa, 0xffd8dcfe, 0xff578d71,
			0xffd85522, 0xff00c467, 0xfff3a569, 0xffd8ffb6, 0xff0118db, 0xff344236, 0xffff9a00, 0xff575f01, 0xffc6f14f, 0xffff5f85,
			0xff7bacf0, 0xff786431, 0xffa285cc, 0xff69ffdc, 0xffc65264, 0xff791a40, 0xff00ee46, 0xffe7cf45, 0xffd980e9, 0xffffd3d1,
			0xffd1ff8d, 0xff240003, 0xff57a3c1, 0xffd3e7c9, 0xffcb6f4f, 0xff3e1800, 0xff0075df, 0xff70b058, 0xffd11800, 0xff001e6b,
			0xff69c8c5, 0xffffcbff, 0xffe9c289, 0xffbf812e, 0xff452a91, 0xffab4cc2, 0xff0e753d, 0xff001e19, 0xff76497f, 0xffffa9c8,
			0xff5e37d9, 0xffeee68a, 0xff9f3621, 0xff500094, 0xffbd9080, 0xff006d7e, 0xff58df60, 0xff475067, 0xff015d9f, 0xff63303c,
			0xff02ce94, 0xff8b5325, 0xffab00ff, 0xff8d2a87, 0xff555394, 0xff96ff00, 0xff00987b, 0xffff8acb, 0xffde45c8, 0xff6b6de6,
			0xff1e0044, 0xffad4c8a, 0xffff86a1, 0xff00233c, 0xff8acd00, 0xff6fca9d, 0xffe14bfd, 0xffffb04d, 0xffe5e839, 0xff7210ff,
			0xff6f5265, 0xff868930, 0xff632650, 0xff692620, 0xffc86e00, 0xffd1a4ff, 0xffc6d256, 0xff4f674d, 0xffaea5a6, 0xffaa2d65,
			0xffc751af, 0xffff59ac, 0xff92664e, 0xff6686b8, 0xff6f98ff, 0xff5cff9f, 0xffac89b2, 0xffd22262, 0xffc7cf93, 0xffffb91e,
			0xfffa948d, 0xff31224e, 0xfffe5161, 0xfffe8d64, 0xff443617, 0xffc9a254, 0xffc7e8f0, 0xff449800, 0xff93ac3a, 0xff164b1c,
			0xff085479, 0xff742d00, 0xff683cff, 0xff402926, 0xffa471d7, 0xffcf009b, 0xff760123, 0xff530058, 0xff0052e8, 0xff2b5c57,
			0xffa0d992, 0xffb01ae5, 0xff1d0324, 0xff7a3a9f, 0xffd6d1cf, 0xffa06469, 0xff6a9da0, 0xff99db71, 0xffc038cf, 0xff7dff59,
			0xff950022, 0xffd5a2df, 0xff1683cc, 0xffa6f945, 0xff6d6961, 0xff56bc4e, 0xffff6d51, 0xffff03f8, 0xffff0049, 0xffca0023,
			0xff436d12, 0xffeaaaad, 0xffbfa500, 0xff262c33, 0xff55b902, 0xff79b69e, 0xfffeecd4, 0xff8ba559, 0xff8dfec1, 0xff003c2b,
			0xff3f1128, 0xffffddf6, 0xff111a92, 0xff9a4254, 0xff959dee, 0xff7e8248, 0xff3a0665, 0xffbd7565 };

	/**
	 * Fiji's Glasbey LUT on a dark background.
	 * <p>
	 * The color with index 0 is black.
	 */
	public static final int[] GLASBEY_ON_DARK = { 0xff000000, 0xffffff00, 0xffff19ff, 0xff009393, 0xff9c4000, 0xff5800c7, 0xfff1ebff,
			0xff144b00, 0xff00bc01, 0xffff9f62, 0xff9190ff, 0xff5d003f, 0xff00ffd6, 0xffff005f, 0xff786477, 0xff004960, 0xff8c884a,
			0xff52cfff, 0xffcf98ba, 0xff9d00b1, 0xffbfd398, 0xff006bd2, 0xffa3335b, 0xff58462b, 0xff6bff66, 0xff9cabae, 0xff008441,
			0xff5c1000, 0xff00008f, 0xfff05100, 0xffcdaa00, 0xffb67264, 0xff4cbe8d, 0xff943cff, 0xff523664, 0xff49655d, 0xff6e84a5,
			0xffaf69c0, 0xffd0b8ff, 0xffffd3be, 0xffd4ffed, 0xffff7b89, 0xff606200, 0xffde009d, 0xff009ff9, 0xffc57801, 0xff0001ff,
			0xffc5011d, 0xffbea388, 0xff625a9d, 0xffff90ff, 0xffa0cd00, 0xffffd761, 0xff6b3b49, 0xff659000, 0xff7c837d, 0xffffffc3,
			0xff95d7d6, 0xff12708d, 0xffffc3ef, 0xffc36692, 0xff8c001e, 0xff8ab15d, 0xff87623b, 0xffb7d1f5, 0xffa399c1, 0xff10bdc1,
			0xffff66c2, 0xff303976, 0xff4d5263, 0xffcdc0c9, 0xff5e3fff, 0xffc587ff, 0xffc300ff, 0xff005039, 0xff8b036e, 0xffd0fc88,
			0xff7fe59f, 0xff974e88, 0xff6e009f, 0xff82aad1, 0xff64966d, 0xff9e8491, 0xffcc4c57, 0xff42007c, 0xffffacb4, 0xff8877be,
			0xff905659, 0xff6d3400, 0xff5d744a, 0xff00f6ff, 0xff6074ff, 0xff540062, 0xff00a953, 0xff894faf, 0xffdbb66b, 0xffc5d5cb,
			0xff1090b8, 0xffe67955, 0xff41552d, 0xff2a6a00, 0xff686057, 0xffffa504, 0xff02dc5f, 0xff97b797, 0xff936d00, 0xfff7001d,
			0xffc332be, 0xff025594, 0xffc05d27, 0xff007d66, 0xff9c9500, 0xfff87d00, 0xfffffcf3, 0xff69a59a, 0xffb8f500, 0xff84382d,
			0xffd6908d, 0xffd10062, 0xffc5f1ff, 0xffded603, 0xffa3b4ff, 0xff5a7c82, 0xff691a2a, 0xffba9649, 0xff724f00, 0xff00d8bd,
			0xff78357e, 0xff9d856d, 0xffd77cce, 0xfffe5551, 0xff006064, 0xffee5589, 0xff17b0da, 0xffbbffc0, 0xff7e00dc, 0xffff96d0,
			0xff494100, 0xffd85aff, 0xffb02187, 0xffa36eff, 0xff4047b1, 0xff3100ba, 0xffbac456, 0xff0e6737, 0xff556988, 0xff890044,
			0xff009e7d, 0xff7dae00, 0xffd1c4a8, 0xff8c8f9c, 0xff9ee06e, 0xff56494e, 0xff9afff5, 0xffb0a2a8, 0xffab3e33, 0xff6799a9,
			0xff92749c, 0xff6a507c, 0xff4d83cc, 0xffb3b6cf, 0xffa01900, 0xff8f9a7b, 0xffaa7540, 0xff5b3b91, 0xff5be300, 0xffcd9cdf,
			0xffebb195, 0xff008c01, 0xffcc3902, 0xffefda9d, 0xffafb075, 0xff8a6f6d, 0xff9c6881, 0xff612a55, 0xffa7e5c8, 0xff81bac7,
			0xfffedbe8, 0xff787b13, 0xff63686f, 0xff655e33, 0xffd955b7, 0xffc88b62, 0xff7361d6, 0xff495046, 0xffe37ea6, 0xffdecbee,
			0xff843964, 0xffd56e79, 0xff9e03e0, 0xffaf003a, 0xff754c3d, 0xff597f6d, 0xff8be5ff, 0xff7d2100, 0xff7b7559, 0xff8593cc,
			0xffb386b8, 0xff74a4fe, 0xff7ec1af, 0xffa25b03, 0xff1a46ff, 0xffff00ca, 0xffd7ecc7, 0xfffff873, 0xff736c91, 0xff00ff96,
			0xff72c978, 0xff507c28, 0xffa0583c, 0xff445180, 0xffced5e1, 0xffc2417e, 0xff395053, 0xffe1953a, 0xfff0b2ff, 0xffb09eee,
			0xffff0094, 0xff74974a, 0xffd6abaf, 0xff652b1d, 0xfffd9685, 0xff00d7e8, 0xfff3be00, 0xffa457dd, 0xff09787b, 0xff365b70,
			0xffb0586c, 0xff6672b0, 0xffd8de78, 0xffbf8195, 0xffb0b2a6, 0xff7fbeff, 0xff730075, 0xff003c9e, 0xffe5dcd6, 0xff6443c3,
			0xff71c841, 0xff4a00fa, 0xff00dc9d, 0xffebf9fe, 0xffaab102, 0xffc7aece, 0xff834a1d, 0xffff68ed, 0xff00b59e, 0xff744d67,
			0xffab8306, 0xffda5a41, 0xffadc5cb, 0xff405500, 0xffffc284, 0xffdc1b44, 0xffa883d1, 0xff7d7983, 0xff116552 };

	/**
	 * A Glasbey color table with only light colors. It is taken from "colorcet", in the python package the color table is called
	 * "glasbey_light".
	 *
	 * @See <a href="https://colorcet.holoviz.org/user_guide/Categorical.html">https://colorcet.holoviz.org/user_guide/Categorical.html</a>
	 */
	public static final int[] GLASBEY_LIGHT = { 0xff000000, 0xffd60000, 0xff018700, 0xffb500ff, 0xff05acc6, 0xff97ff00, 0xffffa52f,
			0xffff8ec8, 0xff79525e, 0xff00fdcf, 0xffafa5ff, 0xff93ac83, 0xff9a6900, 0xff366962, 0xffd3008c, 0xfffdf490, 0xffc86e66,
			0xff9ee2ff, 0xff00c846, 0xffa877ac, 0xffb8ba01, 0xfff4bfb1, 0xffff28fd, 0xfff2cdff, 0xff009e7c, 0xffff6200, 0xff56642a,
			0xff953f1f, 0xff90318e, 0xffff3464, 0xffa0e491, 0xff8c9ab1, 0xff829026, 0xffae083f, 0xff77c6ba, 0xffbc9157, 0xffe48eff,
			0xff72b8ff, 0xffc6a5c1, 0xffff9070, 0xffd3c37c, 0xffbceddb, 0xff6b8567, 0xff916e56, 0xfff9ff00, 0xffbac1df, 0xffac567c,
			0xffffcd03, 0xffff49b1, 0xffc15603, 0xff5d8c90, 0xffc144bc, 0xff00753f, 0xffba6efd, 0xff00d493, 0xff00ff75, 0xff49a150,
			0xffcc9790, 0xff00ebed, 0xffdb7e01, 0xfff77589, 0xffb89500, 0xffc84248, 0xff00cff9, 0xff755726, 0xff85d401, 0xffebffd4,
			0xffa77b87, 0xffdb72c8, 0xffcae256, 0xff8abf5d, 0xffa1216b, 0xff855b89, 0xff89bacf, 0xffffbad6, 0xffb6cfaa, 0xff97414d,
			0xff67aa00, 0xfffde1b1, 0xffff3628, 0xff80793d, 0xffd6e8ff, 0xffa795c6, 0xff7ea59a, 0xffd182a3, 0xff54823b, 0xffe6a872,
			0xff9cffff, 0xffda5480, 0xff05b3aa, 0xffffaaf6, 0xffd1afef, 0xffda015d, 0xffac1a13, 0xff60b385, 0xffd442fd, 0xffacaa59,
			0xfffb9ca7, 0xffb3723b, 0xfff26952, 0xffaed1d4, 0xff9affc3, 0xffdbb333, 0xffeb01c3, 0xff9900c4, 0xffcfff9e, 0xffa55949,
			0xff3b6d01, 0xff008579, 0xff959167, 0xff89dbb3, 0xff6d7400, 0xffaa5dca, 0xff07ef00, 0xff804f3d, 0xffd88052, 0xffffc862,
			0xffb8009e, 0xff99acdd, 0xff904f00, 0xff8c4470, 0xff4f6e52, 0xffff8734, 0xffc68ecd, 0xffd4e29e, 0xffb1826d, 0xff9cfb75,
			0xff56dd77, 0xfff90087, 0xffa1cdff, 0xff13cad1, 0xff118e54, 0xffd154a5, 0xff00dfc3, 0xffa3832f, 0xff77975b, 0xffbaaa80,
			0xff70a3af, 0xffd6fbff, 0xffe8013a, 0xffd84621, 0xffff82ed, 0xffb63862, 0xffb6cd72, 0xff97626b, 0xff897490, 0xff00a316,
			0xff00f4a1, 0xffbf90f2, 0xff89e4d8, 0xffa34d95, 0xff6e5d00, 0xff8cc68e, 0xff95aa2a, 0xffc672dd, 0xffb33b01, 0xffd69a36,
			0xffdfacb6, 0xff009aa0, 0xff599000, 0xff97bca8, 0xffac8ca8, 0xffdad4ff, 0xff547c72, 0xff00ba69, 0xffffc38e, 0xffb800d4,
			0xffdfcf5b, 0xff629a7b, 0xffbfedbc, 0xffc1bdfd, 0xff80d3dd, 0xffe2857e, 0xfff9eb4d, 0xffbf6d82, 0xffcaff4f, 0xffef72aa,
			0xffed67ff, 0xff9946ae, 0xff6d6942, 0xffe25660, 0xffdd662d, 0xff9cdb5d, 0xffe29ccf, 0xffb87500, 0xffc6002d, 0xffdfbcda,
			0xff59b5df, 0xffff59da, 0xff38c1a1, 0xff9e698c, 0xffacaac8, 0xff95622f, 0xffb55662, 0xff2b7e60, 0xffb1e400, 0xffeda590,
			0xff95fde2, 0xffff548e, 0xffbd6ea1, 0xffaa3b36, 0xffd8cf00, 0xffaa80cd, 0xffa08052, 0xffe100e8, 0xffc35b3d, 0xffb53a85,
			0xff8c7700, 0xffdbbc95, 0xff529e93, 0xffafbc82, 0xff91b5b6, 0xffa75423, 0xffffd4ef, 0xff79ae6b, 0xff5db54b, 0xff80fb9a,
			0xff48ffef, 0xff979548, 0xff9387a7, 0xff31d400, 0xff6ee956, 0xffb6d4eb, 0xff705470, 0xfff2db8a, 0xffaad4c1, 0xff7ecdf2,
			0xff89ba00, 0xff64b6ba, 0xffffb500, 0xffc38285, 0xffcaaa5e, 0xff647748, 0xff59e2ff, 0xffdf4dcd, 0xffe9ff79, 0xffbc66b8,
			0xffc395a5, 0xff64c674, 0xffd19570, 0xff70cf4f, 0xffaa6e66, 0xff9c60a5, 0xff00b800, 0xffe299b3, 0xffbc006b, 0xffb3e8ef,
			0xffcdbfe4, 0xff77a342, 0xff856277, 0xff568e5b, 0xff9eafc4, 0xffe82fa0, 0xff247c2a, 0xff826723, 0xffbfbc4d, 0xffddd3a5 };
}
