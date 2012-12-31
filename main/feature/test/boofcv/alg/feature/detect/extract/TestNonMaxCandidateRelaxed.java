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

package boofcv.alg.feature.detect.extract;

import boofcv.struct.QueueCorner;
import boofcv.struct.image.ImageFloat32;
import org.junit.Test;

/**
 * @author Peter Abeles
 */
public class TestNonMaxCandidateRelaxed {

	@Test
	public void standardTests() {
		GenericNonMaxTests tests = new GenericNonMaxTests(false) {
			@Override
			public void findMaximums(ImageFloat32 intensity, float threshold, int radius, int border, QueueCorner found) {
				NonMaxCandidateRelaxed alg = new NonMaxCandidateRelaxed(radius, threshold, border);

				QueueCorner candidates = new QueueCorner(100);
				for( int i = 0; i < intensity.height; i++ ) {
					for( int j = 0; j < intensity.width; j++ ) {
						if( intensity.get(j,i) >= threshold )
							candidates.add(j,i);
					}
				}

				alg.process(intensity,candidates,found);
			}
		};
		tests.allStandard();
	}

}
