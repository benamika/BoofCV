/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.geo.d2.stabilization;

import gecv.alg.geo.AssociatedPair;
import gecv.alg.geo.PointSequentialTracker;
import gecv.alg.geo.SingleImageInput;
import gecv.alg.geo.d2.PixelDistortAffine;
import gecv.alg.interpolate.FactoryInterpolation;
import gecv.alg.interpolate.InterpolatePixel;
import gecv.alg.misc.FactoryImageDistort;
import gecv.alg.misc.ImageDistort;
import gecv.numerics.fitting.modelset.ModelMatcher;
import gecv.struct.image.ImageBase;
import pja.geometry.struct.affine.Affine2D_F32;
import pja.geometry.struct.point.Point2D_F32;
import pja.geometry.transform.affine.AffinePointOps;

import java.util.List;


/**
 * <p>
 * Highly configurable point feature based image stabilization using an affine motion model.  The
 * purpose of this class is for demonstration and evaluation purposes with no specific real-world
 * application targeted.
 * </p>
 * <p>
 * Stabilization is done using by tracking point features and finding their motion relative to
 * a keyframe.  From the motion of individual features the motion of the entire image is found
 * robustly (noisy features are pruned) and described using an affine motion model.  A keyframe
 * is changed when there are too few features to reliably compute image motion.
 * </p>
 *
 * <p>
 * To make stabilization more useful the image motion from multiple key frames is combined allowing
 * a correction to be applied over a longer distance.  The reference frame in which this combined motion
 * is found is reset when the distance between the current frame and the reference frame or when
 * a there are too few features remaining.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class PointImageStabilization<I extends ImageBase > {

	// tracks point features in the image
	private PointSequentialTracker<I> tracker;

	// computes the image motion robustly
	private ModelMatcher<Affine2D_F32,AssociatedPair> fitter;
	// Computes the location of each pixel in the stabilized image from the current frame
	private PixelDistortAffine transform = new PixelDistortAffine();

	// Computes the stabilized image given the unstabilized image and the motion model
	private ImageDistort<I> distort;
	// stabilized image
	private I imageOut;

	// image motion at which the reference frame is changed
	private double thresholdDistance2;
	// minimum number of features before the keyframe is changed
	private int thresholdChange;
	// minim number of features before the reference frame is changed.
	private int thresholdReset;

	// total motion (excluding the current frame) to the reference frame
	private Affine2D_F32 totalMotion = new Affine2D_F32();
	// predeclared location values used to test if the image motion is too great
	private Point2D_F32 testPoint = new Point2D_F32();
	private Point2D_F32 testResult = new Point2D_F32();

	// true if the keyframe has been changed to this frame
	private boolean referenceFrameChanged;

	public PointImageStabilization( Class<I> imageType ,
								  PointSequentialTracker tracker ,
								  ModelMatcher<Affine2D_F32,AssociatedPair> fitter ,
								  int thresholdChange ,
								  int thresholdReset ,
								  double thresholdDistance ) {
		if( !(tracker instanceof SingleImageInput) ) {
			throw new IllegalArgumentException("Tracker must implement "+SingleImageInput.class);
		}

		InterpolatePixel<I> bilinear = FactoryInterpolation.bilinearPixel(imageType);
		distort = FactoryImageDistort.create(imageType,transform,bilinear);
		this.tracker = tracker;
		this.fitter = fitter;

		this.thresholdChange = thresholdChange;
		this.thresholdReset = thresholdReset;
		this.thresholdDistance2 = thresholdDistance*thresholdDistance;
	}

	/**
	 * Computes the stabilized image.  Each image is assumed to be sequential.
	 *
	 * @param input Unstabilized input image.
	 */
	public void process( I input ) {
		// if needed, declare the stabilized output image
		if( imageOut == null ) {
			imageOut = (I)input._createNew(input.getWidth(),input.getHeight());
		}

		// track
		((SingleImageInput<I>)tracker).process(input);

		List<AssociatedPair> tracks = tracker.getActiveTracks();

		boolean reset = false;

		if( tracks.size() < thresholdReset ) {
			// too few feature remaining to track
			reset = true;
		} else {
			// compute the image motion from the tracks
			if( fitter.process(tracks,null) ) {

				// find the transform from the current frame to the keyframe
				Affine2D_F32 m = totalMotion.concat(fitter.getModel(),null);
				transform.set(m);
				distort.apply(input,imageOut);

				// see if the distortion is too great
				AffinePointOps.transform(m,testPoint,testResult);
				double distance2 = testPoint.distance2(testResult);

				if( distance2 > thresholdDistance2 ) {
					// not enough overlap with the reference frame
					reset = true;
				} else if( fitter.getMatchSet().size() < thresholdChange ) {
					// too few features in the inlier set, so change the keyframe
					totalMotion.set(m);
					tracker.setCurrentToKeyFrame();
					tracker.spawnTracks();
				}
			} else {
				reset = true;
			}
		}

		if( reset ) {
			// track has been lost, make this the new keyframe
			tracker.setCurrentToKeyFrame();
			tracker.spawnTracks();
			imageOut.setTo(input);
			totalMotion.reset();
		}
	}

	public I getStabilizedImage() {
		return imageOut;
	}

	public PointSequentialTracker<I> getTracker() {
		return tracker;
	}

	public List<AssociatedPair> getInlierFeatures() {
		return fitter.getMatchSet();
	}
}