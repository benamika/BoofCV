/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.geo.calibration;

import georegression.struct.point.Point2D_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates descriptions of commonly used calibration targets
 *
 * @author Peter Abeles
 */
public class FactoryPlanarCalibrationTarget {

	/**
	 * Creates a target that is composed of squares.  The squares are spaced out and each corner provides
	 * a calibration point.
	 *
	 * @param numCols Number of column in each calibration target
	 * @param numRows Number of rows in calibration target
	 * @param squareWidth How wide each square is. Units are target dependent.
	 * @param spaceWidth Distance between the sides on each square.  Units are target dependent.
	 * @return Target description
	 */
	public static PlanarCalibrationTarget gridSquare( int numCols , int numRows , double squareWidth , double spaceWidth )
	{
		List<Point2D_F64> all = new ArrayList<Point2D_F64>();

		double startX = -(numCols*squareWidth + (numCols-1)*spaceWidth)/2;
		double startY = -(numRows*squareWidth + (numRows-1)*spaceWidth)/2;

		for( int i = 0; i < numRows; i++ ) {
			double y = startY + i*(squareWidth+spaceWidth);

			List<Point2D_F64> top = new ArrayList<Point2D_F64>();
			List<Point2D_F64> bottom = new ArrayList<Point2D_F64>();

			for( int j = 0; j < numCols; j++ ) {
				double x = startX + j*(squareWidth+spaceWidth);

				top.add( new Point2D_F64(x,y));
				top.add( new Point2D_F64(x+squareWidth,y));
				bottom.add( new Point2D_F64(x,y+squareWidth));
				bottom.add( new Point2D_F64(x + squareWidth, y + squareWidth));
			}
			
			all.addAll(top);
			all.addAll(bottom);
		}

		return new PlanarCalibrationTarget(all);
	}

	/**
	 * This target is composed of a checkered chess board like squares.  Each corner of an interior square
	 * touches an adjacent square, but the sides are separated.  Only interior square corners provide
	 * calibration points.
	 *
	 * @param numCols Number of column in each calibration target
	 * @param numRows Number of rows in calibration target
	 * @param squareWidth How wide each square is.  Units are target dependent.
	 * @return Target description
	 */
	public static PlanarCalibrationTarget gridChess( int numCols , int numRows , double squareWidth )
	{
		return gridSquare(numCols-1,numRows-1,squareWidth,squareWidth);
	}
}
