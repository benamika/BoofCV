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

package boofcv.alg.filter.convolve.normalized;

import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

/**
 * <p>
 * Straight forward implementation of {@link boofcv.alg.filter.convolve.ConvolveNormalizedSparse} with minimal
 * optimizations.
 * </p>
 *
 * <p>
 * NOTE: Do not modify.  Automatically generated by {@link GenerateConvolveNormalizedStandardSparse}.
 * </p>
 *
 * @author Peter Abeles
 */
public class ConvolveNormalizedStandardSparse {

	public static float convolve( Kernel1D_F32 horizontal, Kernel1D_F32 vertical,
								ImageFloat32 input, int c_x , int c_y, float storage[] )
	{
		// convolve horizontally first
		int width = horizontal.getWidth();
		int radius = width/2;

		int x0 = c_x-radius;
		int x1 = c_x+radius+1;
		int y0 = c_y-radius;
		int y1 = c_y+radius+1;

		if( x0 < 0 ) x0 = 0;
		if( y0 < 0 ) y0 = 0;
		if( x1 > input.width ) x1 = input.width;
		if( y1 > input.height ) y1 = input.height;

		final int startJ = x0-c_x+radius;
		final int endJ = width-(c_x+radius+1-x1);
		int indexStorage = y0-c_y+radius;
		for( int i = y0; i < y1; i++ ,indexStorage++) {
			int indexImg = input.startIndex + i*input.stride + x0;

			float total = 0;
			float div = 0;
			for( int j = startJ; j < endJ; j++ ,indexImg++) {
				final float kerVal = horizontal.data[j];
				total += (input.data[indexImg])*kerVal;
				div += kerVal;
			}
			storage[indexStorage] = total/div;
		}

		// convolve vertically
		float total = 0;
		float div = 0;
		final int endI = width-(c_y+radius+1-y1);
		for( int i = y0-c_y+radius; i < endI; i++ ) {
			final float kerVal = vertical.data[i];
			total += storage[i]*kerVal;
			div += kerVal;
		}
		return total/div;
	}

	public static int convolve( Kernel1D_I32 horizontal, Kernel1D_I32 vertical,
								ImageUInt8 input, int c_x , int c_y, int storage[] )
	{
		// convolve horizontally first
		int width = horizontal.getWidth();
		int radius = width/2;

		int x0 = c_x-radius;
		int x1 = c_x+radius+1;
		int y0 = c_y-radius;
		int y1 = c_y+radius+1;

		if( x0 < 0 ) x0 = 0;
		if( y0 < 0 ) y0 = 0;
		if( x1 > input.width ) x1 = input.width;
		if( y1 > input.height ) y1 = input.height;

		final int startJ = x0-c_x+radius;
		final int endJ = width-(c_x+radius+1-x1);
		int indexStorage = y0-c_y+radius;
		for( int i = y0; i < y1; i++ ,indexStorage++) {
			int indexImg = input.startIndex + i*input.stride + x0;

			int total = 0;
			int div = 0;
			for( int j = startJ; j < endJ; j++ ,indexImg++) {
				final int kerVal = horizontal.data[j];
				total += (input.data[indexImg] & 0xFF)*kerVal;
				div += kerVal;
			}
			storage[indexStorage] = total/div;
		}

		// convolve vertically
		int total = 0;
		int div = 0;
		final int endI = width-(c_y+radius+1-y1);
		for( int i = y0-c_y+radius; i < endI; i++ ) {
			final int kerVal = vertical.data[i];
			total += storage[i]*kerVal;
			div += kerVal;
		}
		return total/div;
	}

	public static int convolve( Kernel1D_I32 horizontal, Kernel1D_I32 vertical,
								ImageSInt16 input, int c_x , int c_y, int storage[] )
	{
		// convolve horizontally first
		int width = horizontal.getWidth();
		int radius = width/2;

		int x0 = c_x-radius;
		int x1 = c_x+radius+1;
		int y0 = c_y-radius;
		int y1 = c_y+radius+1;

		if( x0 < 0 ) x0 = 0;
		if( y0 < 0 ) y0 = 0;
		if( x1 > input.width ) x1 = input.width;
		if( y1 > input.height ) y1 = input.height;

		final int startJ = x0-c_x+radius;
		final int endJ = width-(c_x+radius+1-x1);
		int indexStorage = y0-c_y+radius;
		for( int i = y0; i < y1; i++ ,indexStorage++) {
			int indexImg = input.startIndex + i*input.stride + x0;

			int total = 0;
			int div = 0;
			for( int j = startJ; j < endJ; j++ ,indexImg++) {
				final int kerVal = horizontal.data[j];
				total += (input.data[indexImg])*kerVal;
				div += kerVal;
			}
			storage[indexStorage] = total/div;
		}

		// convolve vertically
		int total = 0;
		int div = 0;
		final int endI = width-(c_y+radius+1-y1);
		for( int i = y0-c_y+radius; i < endI; i++ ) {
			final int kerVal = vertical.data[i];
			total += storage[i]*kerVal;
			div += kerVal;
		}
		return total/div;
	}

}
