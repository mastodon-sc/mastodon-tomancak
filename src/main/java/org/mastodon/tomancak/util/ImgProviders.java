package org.mastodon.tomancak.util;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import ij.ImagePlus;
import bdv.viewer.Source;

import java.io.File;

/**
 * Govering class to provide unified way of obtaining images at given time point
 * either externally from a file system or internally from Mastodon's BDV data.
 *
 * One has to first instantiate either the embedded class ImgProviderFromDisk
 * or ImgProviderFromMastodon as the ImgProvider interface, RAI<?> living at
 * some time index 't' can be then obtained with the method getImage(t).
 *
 * Note that the ImgProviderFromDisk variant may throw IllegalArgumentException
 * if the corresponding file could not be opened.
 *
 * @author Vladimir Ulman, 2019
 */
public class ImgProviders
{
	public interface ImgProvider
	{
		/** the implementing method must return existing non-null image,
		    or throw IllegalArgumentException */
		RandomAccessibleInterval<?> getImage(final int time);
	}

	//-------------------------------------------------------
	public class ImgProviderFromDisk implements ImgProvider
	{
		private final String fileTemplate;

		public ImgProviderFromDisk(final String path, final String fileTemplate)
		{
			//possibly test if 'fileTemplate' contains reference where time can be inserted
			this.fileTemplate = path + File.separator + fileTemplate;
		}

		public ImgProviderFromDisk(final String fullPathFileTemplate)
		{
			this.fileTemplate = fullPathFileTemplate;
		}

		//cache reference on the most recently retrieved image
		Img<?> cachedImg = null;
		int cachedImgTimePoint;

		@Override
		public RandomAccessibleInterval<?> getImage(int time)
		{
			//reuse the cached image if the same time point requested
			if (cachedImg != null && cachedImgTimePoint == time) return cachedImg;

			final String filename = String.format(fileTemplate,time);
			try
			{
				cachedImg = null; //"invalidate" before attempting to read
				cachedImg = ImageJFunctions.wrap(new ImagePlus( filename ));
				cachedImgTimePoint = time;
			}
			catch (RuntimeException e)
			{
				throw new IllegalArgumentException("Error reading image file "+filename+"\n"+e.getMessage());
			}

			//make sure we always return some non-null reference
			if (cachedImg == null)
				throw new IllegalArgumentException("Error reading image file "+filename);
			return cachedImg;
		}

		}
	}

	//-------------------------------------------------------
	public class ImgProviderFromMastodon implements ImgProvider
	{
		private final Source<?> imgSource;
		private final int viewMipLevel;

		public ImgProviderFromMastodon(final Source imgSource)
		{
			this.imgSource = imgSource;
			this.viewMipLevel = 0;       //NB: 0 is full res
		}

		public ImgProviderFromMastodon(final Source imgSource, final int mipLevel)
		{
			this.imgSource = imgSource;
			this.viewMipLevel = mipLevel;
		}

		@Override
		public RandomAccessibleInterval<?> getImage(int time)
		{
			return imgSource.getSource(time,viewMipLevel);
		}
	}
}
