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

package gecv.alg.detect.extract;

import gecv.alg.drawing.impl.ImageInitialization_F32;
import gecv.struct.QueueCorner;
import gecv.struct.image.ImageFloat32;
import org.junit.Test;
import pja.geometry.struct.point.Point2D_I16;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Peter Abeles
 */
public class TestFastNonMaxCornerExtractor {
	Random rand = new Random(0x334);

	/**
	 * If a non-empty list of features is passed in it should not add them again to the list nor return
	 * any similar features.
	 */
	@Test
	public void excludePreExisting() {
		ImageFloat32 inten = new ImageFloat32(30, 40);
		ImageInitialization_F32.randomize(inten, new Random(1231), 0, 10);

		QueueCorner cornersFirst = new QueueCorner(inten.getWidth() * inten.getHeight());

		FastNonMaxCornerExtractor alg = new FastNonMaxCornerExtractor(2, 0, 0.6F);
		// find corners the first time
		alg.process(inten,cornersFirst);

		// add points which should be excluded
		QueueCorner cornersSecond = new QueueCorner(inten.getWidth() * inten.getHeight());
		for( int i = 0; i < 20; i++ ) {
			cornersSecond.add(cornersFirst.get(i));
		}

		// recreate the same image
		ImageInitialization_F32.randomize(inten, new Random(1231), 0, 10);
		alg.process(inten,cornersSecond);
		assertEquals(cornersSecond.size(),cornersFirst.size());
		
		//make sure it isn't just clearing the list and finding the same corners again
		ImageInitialization_F32.fill(inten,0);
		alg.process(inten,cornersSecond);
		assertEquals(cornersSecond.size(),cornersFirst.size());
		cornersSecond.reset();
		alg.process(inten,cornersSecond);
		assertEquals(cornersSecond.size(),0);
	}

	/**
	 * Checks to see if {@link FastNonMaxCornerExtractor} produces exactly the same results as
	 * {@link NonMaxCornerExtractorNaive}
	 */
	@Test
	public void compareToNaive() {

		ImageFloat32 inten = new ImageFloat32(30, 40);

		QueueCorner fastCorners = new QueueCorner(inten.getWidth() * inten.getHeight());
		QueueCorner regCorners = new QueueCorner(inten.getWidth() * inten.getHeight());

		for (int useSubImage = 0; useSubImage < 2; useSubImage++) {
			// make sure it handles sub images correctly
			if (useSubImage == 1) {
				ImageFloat32 larger = new ImageFloat32(inten.width + 10, inten.height + 8);
				inten = larger.subimage(0, 0, 30, 40);
			}

			for (int nonMaxWidth = 3; nonMaxWidth <= 9; nonMaxWidth += 2) {
				FastNonMaxCornerExtractor fast = new FastNonMaxCornerExtractor(nonMaxWidth / 2, 0, 0.6F);
				NonMaxCornerExtractorNaive reg = new NonMaxCornerExtractorNaive(nonMaxWidth / 2, 0.6F);

				for (int i = 0; i < 10; i++) {
					ImageInitialization_F32.randomize(inten, rand, 0, 10);

					fast.process(inten, fastCorners);
					reg.process(inten, regCorners);

					assertTrue(fastCorners.size() > 0);

					assertEquals(regCorners.size(), fastCorners.size());

					for (int j = 0; j < regCorners.size(); j++) {
						Point2D_I16 a = fastCorners.get(j);
						Point2D_I16 b = regCorners.get(j);

						assertEquals(b.getX(), a.getX());
						assertEquals(b.getY(), a.getY());
					}
				}
			}
		}
	}
}