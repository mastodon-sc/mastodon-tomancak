package org.mastodon.tomancak.util;

import ij.measure.Calibration;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.realtransform.AffineTransform3D;
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

		/** Returns the spatial dimensionality of fetched images. */
		int numDimensions();

		/** Returns a reference calibration data. */
		VoxelDimensions getVoxelDimensions();

		/** Returns a reference image-to-world transformation. */
		void getSourceTransform(final AffineTransform3D transform);

		/** Returns an image-to-world transformation for this time point. */
		void getSourceTransform(final int time, final AffineTransform3D transform);
	}


	//-------------------------------------------------------
	public class ImgProviderFromDisk implements ImgProvider
	{
		private final String fileTemplate;

		final AffineTransform3D defaultNoTransform = new AffineTransform3D();
		final VoxelDimensions vd;

		public ImgProviderFromDisk(final String fullPathFileTemplate, final VoxelDimensions vd)
		{
			this.fileTemplate = fullPathFileTemplate;
			this.vd = vd;
		}

		public ImgProviderFromDisk(final String path, final String fileTemplate, final VoxelDimensions vd)
		{
			this( path + File.separator + fileTemplate, vd );
		}

		public ImgProviderFromDisk(final String fullPathFileTemplate, final int time)
		{
			this.fileTemplate = fullPathFileTemplate;

			//read the image and try to define the calibration from it
			final String filename = String.format(fileTemplate,time);
			try
			{
				final ImagePlus ip = new ImagePlus( filename );

				final Calibration ipc = ip.getCalibration();
				this.vd = new FinalVoxelDimensions( ipc.getUnit(),
				                ipc.pixelWidth,ipc.pixelHeight,ipc.pixelDepth);

				//update the cache
				this.cachedImg = ImageJFunctions.wrap(ip);
				this.cachedImgTimePoint = time;
			}
			catch (RuntimeException e)
			{
				throw new IllegalArgumentException("Error reading image file "+filename+"\n"+e.getMessage());
			}
		}

		public ImgProviderFromDisk(final String path, final String fileTemplate, final int time)
		{
			this( path + File.separator + fileTemplate, time );
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


		@Override
		public int numDimensions()
		{
			return vd.numDimensions();
		}

		@Override
		public VoxelDimensions getVoxelDimensions()
		{
			return vd;
		}

		@Override
		public void getSourceTransform(final AffineTransform3D transform)
		{
			transform.set(defaultNoTransform);
		}

		@Override
		public void getSourceTransform(final int time, final AffineTransform3D transform)
		{
			getSourceTransform(transform);
		}
	}


	//-------------------------------------------------------
	public class ImgProviderFromMastodon implements ImgProvider
	{
		private final Source<?> imgSource;
		private final int viewMipLevel;
		private final int referenceTime;

		public ImgProviderFromMastodon(final Source imgSource, final int mipLevel, final int referenceTime)
		{
			this.imgSource = imgSource;
			this.viewMipLevel = mipLevel;
			this.referenceTime = referenceTime;
		}

		public ImgProviderFromMastodon(final Source imgSource, final int referenceTime)
		{
			//NB: mipLevel = 0 will request full res images
			this(imgSource,0,referenceTime);
		}


		@Override
		public RandomAccessibleInterval<?> getImage(int time)
		{
			return imgSource.getSource(time,viewMipLevel);
		}


		@Override
		public int numDimensions()
		{
			return imgSource.getSource(referenceTime,viewMipLevel).numDimensions();
		}

		@Override
		public VoxelDimensions getVoxelDimensions()
		{
			return imgSource.getVoxelDimensions();
		}

		@Override
		public void getSourceTransform(final AffineTransform3D transform)
		{
			imgSource.getSourceTransform(referenceTime,viewMipLevel, transform);
		}

		@Override
		public void getSourceTransform(final int time, final AffineTransform3D transform)
		{
			imgSource.getSourceTransform(time,viewMipLevel, transform);
		}
	}
}
