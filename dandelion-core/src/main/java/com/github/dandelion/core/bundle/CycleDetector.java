/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2014 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.bundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.dandelion.core.bundle.Bundle;

/**
 * @author <a href="michal.maczka@dimatics.com">Michal Maczka</a>
 * @version $Id$
 */
public class CycleDetector
{

    private final static Integer NOT_VISTITED = new Integer( 0 );

    private final static Integer VISITING = new Integer( 1 );

    private final static Integer VISITED = new Integer( 2 );


    public static List<String> hasCycle( final BundleDag graph )
    {
        final List<Bundle> verticies = graph.getVerticies();

        final Map<Bundle, Integer> vertexStateMap = new HashMap<Bundle, Integer>();

        List<String> retValue = null;

        for ( Bundle vertex : verticies )
        {
            if ( isNotVisited( vertex, vertexStateMap ) )
            {
                retValue = introducesCycle( vertex, vertexStateMap );

                if ( retValue != null )
                {
                    break;
                }
            }
        }

        return retValue;
    }

    /**
     * This method will be called when an edge leading to given vertex was added
     * and we want to check if introduction of this edge has not resulted
     * in apparition of cycle in the graph
     *
     * @param vertex
     * @param vertexStateMap
     * @return
     */
    public static List<String> introducesCycle( final Bundle vertex, final Map<Bundle, Integer> vertexStateMap )
    {
        final LinkedList<String> cycleStack = new LinkedList<String>();

        final boolean hasCycle = dfsVisit( vertex, cycleStack, vertexStateMap );

        if ( hasCycle )
        {
            // we have a situation like: [b, a, c, d, b, f, g, h].
            // Label of Vertex which introduced  the cycle is at the first position in the list
            // We have to find second occurrence of this label and use its position in the list
            // for getting the sublist of vertex labels of cycle participants
            //
            // So in our case we are searching for [b, a, c, d, b]
            final String label = cycleStack.getFirst();

            final int pos = cycleStack.lastIndexOf( label );

            final List<String> cycle = cycleStack.subList( 0, pos + 1 );

            Collections.reverse( cycle );

            return cycle;
        }

        return null;
    }


    public static List<String> introducesCycle( final Bundle vertex )
    {
        final Map<Bundle, Integer> vertexStateMap = new HashMap<Bundle, Integer>();

        return introducesCycle( vertex, vertexStateMap );
    }

    /**
     * @param vertex
     * @param vertexStateMap
     * @return
     */
    private static boolean isNotVisited( final Bundle vertex, final Map<Bundle, Integer> vertexStateMap )
    {
        final Integer state = vertexStateMap.get( vertex );

        return ( state == null ) || NOT_VISTITED.equals( state );
    }

    /**
     * @param vertex
     * @param vertexStateMap
     * @return
     */
    private static boolean isVisiting( final Bundle vertex, final Map<Bundle, Integer> vertexStateMap )
    {
        final Integer state = vertexStateMap.get( vertex );

        return VISITING.equals( state );
    }

    private static boolean dfsVisit( final Bundle vertex, final LinkedList<String> cycle,
                                     final Map<Bundle, Integer> vertexStateMap )
    {
        cycle.addFirst( vertex.getName() );

        vertexStateMap.put( vertex, VISITING );

        for ( Bundle v : vertex.getChildren() )
        {
            if ( isNotVisited( v, vertexStateMap ) )
            {
                final boolean hasCycle = dfsVisit( v, cycle, vertexStateMap );

                if ( hasCycle )
                {
                    return true;
                }
            }
            else if ( isVisiting( v, vertexStateMap ) )
            {
                cycle.addFirst( v.getName() );

                return true;
            }
        }
        vertexStateMap.put( vertex, VISITED );

        cycle.removeFirst();

        return false;
    }

}