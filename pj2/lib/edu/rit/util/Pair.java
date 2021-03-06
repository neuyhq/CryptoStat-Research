//******************************************************************************
//
// File:    Pair.java
// Package: edu.rit.util
// Unit:    Class edu.rit.util.Pair
//
// This Java source file is copyright (C) 2019 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java 2 Library ("PJ2"). PJ2 is
// free software; you can redistribute it and/or modify it under the terms of
// the GNU General Public License as published by the Free Software Foundation;
// either version 3 of the License, or (at your option) any later version.
//
// PJ2 is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package edu.rit.util;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import java.io.IOException;

/**
 * Class Pair encapsulates a pair consisting of a key and its associated value.
 * The key must be an object suitable for use in a hashed data structure; that
 * is, the key class must define the {@link Object#equals(Object) equals()} and
 * {@link Object#hashCode() hashCode()} methods properly. The key may be null.
 * Once a key is stored in a pair, the state of the key must not be altered. The
 * value may be any object. The value may be null. Once a value is stored in a
 * pair, the state of the value may be altered, and the value may be replaced by
 * a different object.
 * <P>
 * Class Pair is streamable. For object streaming to work properly, the key
 * class and the value class must be streamable or serializable.
 *
 * @param  <K>  Key data type.
 * @param  <V>  Value data type.
 *
 * @author  Alan Kaminsky
 * @version 05-Jun-2019
 */
public class Pair<K,V>
	extends PairBase<K>
	{

// Hidden data members.

	/**
	 * The pair's value.
	 */
	protected V value;

// Exported constructors.

	/**
	 * Construct a new pair. The key and the value are null.
	 */
	public Pair()
		{
		super();
		}

	/**
	 * Construct a new pair with the given key and value.
	 *
	 * @param  key    Key.
	 * @param  value  Value.
	 */
	public Pair
		(K key,
		 V value)
		{
		super (key);
		this.value = value;
		}

// Exported operations.

	/**
	 * Returns this pair's value.
	 *
	 * @return  Value.
	 */
	public V value()
		{
		return value;
		}

	/**
	 * Sets this pair's value to the given value.
	 *
	 * @param  value  Value.
	 */
	public void value
		(V value)
		{
		this.value = value;
		}

	/**
	 * Write this object's fields to the given out stream. The key and the value
	 * are written using {@link OutStream#writeReference(Object)
	 * writeReference()}.
	 *
	 * @param  out  Out stream.
	 *
	 * @exception  IOException
	 *     Thrown if the key or the value is not streamable or serializable.
	 *     Thrown if an I/O error occurred.
	 */
	public void writeOut
		(OutStream out)
		throws IOException
		{
		out.writeReference (key);
		out.writeReference (value);
		}

	/**
	 * Read this object's fields from the given in stream. The key and the value
	 * are read using {@link InStream#readReference() readReference()}.
	 *
	 * @param  in  In stream.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void readIn
		(InStream in)
		throws IOException
		{
		key = (K) in.readReference();
		value = (V) in.readReference();
		}

	/**
	 * Returns a string version of this pair.
	 *
	 * @return  String version.
	 */
	public String toString()
		{
		return String.format ("(%s,%s)", key, value);
		}

	}
