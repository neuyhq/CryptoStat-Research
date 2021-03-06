//******************************************************************************
//
// File:    SourceConfig.java
// Package: edu.rit.pjmr
// Unit:    Class edu.rit.pjmr.SourceConfig
//
// This Java source file is copyright (C) 2016 by Alan Kaminsky. All rights
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

package edu.rit.pjmr;

import edu.rit.io.Streamable;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Vbl;
import edu.rit.util.AList;
import java.io.IOException;

/**
 * Class SourceConfig contains specifications for configuring a {@linkplain
 * Source} in a mapper task.
 *
 * @param  <I>  Mapper's data record ID data type.
 * @param  <C>  Mapper's data record contents data type.
 * @param  <K>  Mapper's output key data type.
 * @param  <V>  Mapper's output value data type; must implement interface
 *              {@linkplain edu.rit.pj2.Vbl Vbl}.
 *
 * @author  Alan Kaminsky
 * @version 27-May-2016
 */
public class SourceConfig<I,C,K,V extends Vbl>
	implements Streamable
	{

// Exported data members.

	// Mapper task config object that contains this source config object.
	MapperTaskConfig<I,C,K,V> mapperTaskConfig;

	// Source.
	Source<I,C> source;

	// List of mapper config objects for mappers attached to this source.
	AList<MapperConfig<I,C,K,V>> mapperConfigList =
		new AList<MapperConfig<I,C,K,V>>();

// Exported constructors.

	/**
	 * Construct a new uninitialized source config object. This constructor is
	 * for use only by object deserialization.
	 */
	SourceConfig()
		{
		}

	/**
	 * Construct a new source config object.
	 *
	 * @param  mapperTaskConfig  Mapper task config object that contains this
	 *                           source config object.
	 * @param  source            Source.
	 */
	SourceConfig
		(MapperTaskConfig<I,C,K,V> mapperTaskConfig,
		 Source<I,C> source)
		{
		this.mapperTaskConfig = mapperTaskConfig;
		this.source = source;
		}

// Exported operations.

	/**
	 * Add the given {@linkplain Mapper} to this source. A source must have at
	 * least one mapper; it may have more than one mapper. The argument strings
	 * for the mapper, if any, may also be specified.
	 *
	 * @param  mapperClass  Mapper class.
	 * @param  args         Zero or more argument strings for the mapper.
	 *
	 * @return  Mapper config object for <TT>mapperClass</TT>.
	 *
	 * @exception  NullPointerException
	 *     (unchecked exception) Thrown if <TT>mapperClass</TT> is null.
	 */
	public <T extends Mapper<I,C,K,V>> MapperConfig<I,C,K,V> mapper
		(Class<T> mapperClass,
		 String... args)
		{
		return mapper (1, mapperClass, args);
		}

	/**
	 * Add the given number of copies of the given {@linkplain Mapper} to this
	 * source. A source must have at least one mapper; it may have more than one
	 * mapper. The argument strings for the mappers, if any, may also be
	 * specified; each mapper has the same argument strings.
	 *
	 * @param  copies       Number of copies to add (&ge; 1).
	 * @param  mapperClass  Mapper class.
	 * @param  args         Zero or more argument strings for the mappers.
	 *
	 * @return  Mapper config object for <TT>mapperClass</TT>.
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if <TT>copies</TT> &lt; 1.
	 * @exception  NullPointerException
	 *     (unchecked exception) Thrown if <TT>mapperClass</TT> is null.
	 */
	public <T extends Mapper<I,C,K,V>> MapperConfig<I,C,K,V> mapper
		(int copies,
		 Class<T> mapperClass,
		 String... args)
		{
		if (copies < 1)
			throw new IllegalArgumentException (String.format
				("SourceConfig.mapper(): copies = %d illegal", copies));
		MapperConfig<I,C,K,V> mapperConfig =
			new MapperConfig<I,C,K,V> (this, mapperClass, args);
		for (int i = 0; i < copies; ++ i)
			mapperConfigList.addLast (mapperConfig);
		return mapperConfig;
		}

	/**
	 * Add the given {@linkplain Source} to this source's mapper task. For
	 * further information, see {@link MapperTaskConfig#source(Source)
	 * MapperTaskConfig.source()}.
	 *
	 * @param  source  Source.
	 *
	 * @return  Source config object for <TT>source</TT>.
	 *
	 * @exception  NullPointerException
	 *     (unchecked exception) Thrown if <TT>source</TT> is null.
	 */
	public SourceConfig<I,C,K,V> source
		(Source<I,C> source)
		{
		return mapperTaskConfig.source (source);
		}

	/**
	 * Write this source config object to the given out stream.
	 *
	 * @param  out  Out stream.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void writeOut
		(OutStream out)
		throws IOException
		{
		out.writeObject (source);
		out.writeReference (mapperConfigList);
		out.clearCache();
		}

	/**
	 * Read this source config object from the given in stream.
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
		source = (Source) in.readObject();
		mapperConfigList = (AList<MapperConfig<I,C,K,V>>)
			in.readReference();
		in.clearCache();
		}

	}
