//******************************************************************************
//
// File:    Interleave.java
// Package: edu.rit.crst
// Unit:    Class edu.rit.crst.Interleave
//
// This Java source file is copyright (C) 2017 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the CryptoStat Library ("CryptoStat").
// CryptoStat is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 3 of the License, or (at your option) any later
// version.
//
// CryptoStat is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package edu.rit.crst;

import edu.rit.util.GpuBigIntArray;

/**
 * Class Interleave provides an object that generates input values for a
 * cryptographic {@linkplain Function Function}.
 * <P>
 * An instance of class Interleave is layered on top of two or more other input
 * generators. The latter are specified as arguments to the Interleave
 * constructor, in the form of constructor expressions. The Interleave
 * constructor uses the constructor expressions to create the underlying input
 * generators. (For further information about constructor expressions, see class
 * {@linkplain edu.rit.util.Instance edu.rit.util.Instance}.)
 * <P>
 * Class Interleave invokes each underlying input generator to generate a series
 * of cryptographic function input values. Class Interleave then interleaves
 * these input values with each other in a round robin fashion.
 *
 * @author  Alan Kaminsky
 * @version 20-Sep-2017
 */
public class Interleave
	extends CombiningGenerator
	{

// Exported constructors.

	/**
	 * Construct a new interleave input generator.
	 *
	 * @param  ctors  Two or more constructor expressions for the underlying
	 *                input generators.
	 *
	 * @exception  NullPointerException
	 *     (unchecked exception) Thrown if <TT>ctors</TT> is null.
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if fewer than two constructor
	 *     expressions are specified.
	 */
	public Interleave
		(String... ctors)
		{
		super (ctors);
		}

// Exported operations.

	/**
	 * Get a description of this input generator.
	 *
	 * @return  Description.
	 */
	public String description()
		{
		return super.description() + ", interleaved";
		}

// Hidden operations.

	/**
	 * Combine the given individual input value sequences together.
	 *
	 * @param  V    Array of {@linkplain GpuBigIntArray} objects. Each element
	 *              of the <TT>V</TT> array contains an input value series
	 *              generated by one of the underlying input generators.
	 * @param  len  Total number of input values in all the input value series.
	 *
	 * @return  A {@linkplain GpuBigIntArray} object containing the combined
	 *          input value series.
	 */
	protected GpuBigIntArray combine
		(GpuBigIntArray[] V,
		 int len)
		{
		int N = V.length;
		GpuBigIntArray VV = new GpuBigIntArray (V[0].bitSize(), len);
		int i = 0;
		int j = 0;
		while (i < len)
			{
			for (int k = 0; k < N; ++ k)
				if (j < V[k].item.length)
					VV.item[i++] = V[k].item[j];
			++ j;
			}
		return VV;
		}

	}
