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

package boofcv.abst.sfm;

import georegression.struct.se.Se3_F64;

/**
 * @author Peter Abeles
 */
public interface VisualOdometry {

	/**
	 * Forget past history and tracking results, returning it to its initial state.
	 */
	public void reset();

	/**
	 * If a fatal error occurred while updating its state then this function will return true.
	 * Before more images can be processed {@link #reset()} must be called.
	 *
	 * @return true if a fatal error has occurred.
	 */
	public boolean isFatal();

	/**
	 * Returns the estimated motion relative to the first frame in which a fatal error
	 * does not happen.
	 *
	 * @return Found pose.
	 */
	public Se3_F64 getLeftToWorld();
}
