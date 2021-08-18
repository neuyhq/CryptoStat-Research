//******************************************************************************
//
// File:    Adjacent.java
// Package: edu.rit.crst
// Unit:    Class edu.rit.crst.Adjacent
//
// This Java source file is copyright (C) 2018 by Alan Kaminsky. All rights
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

/**
 * Class Adjacent provides an object that generates a series of output bit
 * groups for a cryptographic {@linkplain Function Function}. Each bit group
 * consists of adjacent bit positions. The bit groups do not overlap.
 *
 * @author  Alan Kaminsky
 * @version 21-Feb-2018
 */
public class Adjacent
	extends BitGroup
	{

// Exported constructors.

	/**
	 * Construct a new bit group generator.
	 *
	 * @param  size  Bit size of this bit group (1 .. 8).
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if <TT>size</TT> &lt; 1 or <TT>size</TT>
	 *     &gt; 8.
	 */
	public Adjacent
		(int size)
		{
		super (size);
		}

// Exported operations.

	/**
	 * Get a constructor expression for this bit group generator. The
	 * constructor expression can be passed to the {@link
	 * edu.rit.util.Instance#newInstance(String)
	 * edu.rit.util.Instance.newInstance()} method to construct an object that
	 * is the same as this bit group generator.
	 *
	 * @return  Constructor expression.
	 */
	public String constructor()
		{
		return String.format ("Adjacent(%d)", size);
		}

	/**
	 * Get a description of this bit group generator.
	 *
	 * @return  Description.
	 */
	public String description()
		{
		return String.format ("%d-bit groups, adjacent bits", size);
		}

	/**
	 * Get a description of the given bit group generated by this bit group
	 * generator.
	 *
	 * @param  i  Bit group index (0 .. {@link #count() count()}&minus;1.
	 *
	 * @return  Description of bit group <TT>i</TT>.
	 *
	 * @exception  IndexOutOfBoundsException
	 *     (unchecked exception) Thrown if <TT>i</TT> is out of bounds.
	 */
	public String bitGroupDescription
		(int i)
		{
		return adjacentBitsDescription (i);
		}

// Hidden operations.

	/**
	 * Generate a series of bit groups for the cryptographic function. This
	 * method stores the bit groups in the protected {@link #bgs bgs} matrix. In
	 * the matrix, each row represents one bit group. The elements of a row give
	 * the bit positions for that bit group.
	 */
	protected void generate()
		{
		int bit = 0;
		for (int i = 0; i < count; ++ i)
			for (int j = 0; j < size; ++ j)
				bgs[i][j] = bit ++;
		}

	}
