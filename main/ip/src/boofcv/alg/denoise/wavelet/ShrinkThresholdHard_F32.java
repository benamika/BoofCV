/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.denoise.wavelet;

import boofcv.alg.denoise.ShrinkThresholdRule;
import boofcv.alg.misc.ImageMiscOps;
import boofcv.struct.image.ImageFloat32;


/**
 * <p>
 * Hard rule for shrinking an image: T(x) = x*1(|x|>T)
 * </p>
 *
 * @author Peter Abeles
 */
public class ShrinkThresholdHard_F32 implements ShrinkThresholdRule<ImageFloat32> {

	@Override
	public void process(ImageFloat32 image, Number threshold) {
		float f = threshold.floatValue();

		// see if all the coefficients should be set to zero
		if( Float.isInfinite(f)) {
			ImageMiscOps.fill(image, 0);
			return;
		}

		for( int y = 0; y < image.height; y++ ) {
			int index = image.startIndex + y*image.stride;
		    int end = index + image.width;

			for( ; index < end; index++ ) {
				float v = image.data[index];
				if( Math.abs(v) < f ) {
					image.data[index] = 0;
				}
			}
		}
	}
}
