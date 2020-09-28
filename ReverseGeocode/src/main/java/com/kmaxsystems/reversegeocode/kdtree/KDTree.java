/*
The MIT License (MIT)
[OSI Approved License]
The MIT License (MIT)

Copyright (c) 2014 Daniel Glasson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.kmaxsystems.reversegeocode.kdtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Daniel Glasson
 * A KD-Tree implementation to quickly find nearest points
 * Currently implements createKDTree and findNearest as that's all that's required here
 */
public class KDTree<T extends KDNodeComparator<T>> {
    private KDNode<T> root;

    public KDTree( List<T> items ) {
        root = createKDTree(items, 0);
    }

    public T findNearest( T search ) {
        return findNearest(root, search, 0).location;
    }
        
    // Only ever goes to log2(items.length) depth so lack of tail recursion is a non-issue
    private KDNode<T> createKDTree( List<T> items, int depth ) {
        if ( items.isEmpty() ) {
            return null;
        }
        Collections.sort(items, items.get(0).getComparator(depth % 3));
        int currentIndex = items.size()/2;
        return new KDNode<T>(createKDTree(new ArrayList<T>(items.subList(0, currentIndex)), depth+1), createKDTree(new ArrayList<T>(items.subList(currentIndex + 1, items.size())), depth+1), items.get(currentIndex));
    }

//    searches in the tree comparing with left and right node
//    The nearest neighbour search (NN) algorithm aims to find the point in the tree that is nearest to a given input point. This search can be done efficiently by using the tree properties to quickly eliminate large portions of the search space.
//
//    Searching for a nearest neighbour in a k-d tree proceeds as follows:
//
//    Starting with the root node, the algorithm moves down the tree recursively, in the same way that it would if the search point were being inserted
//    (i.e. it goes left or right depending on whether the point is lesser than or greater than the current node in the split dimension).
//    Once the algorithm reaches a leaf node, it saves that node point as the "current best"
//    The algorithm unwinds the recursion of the tree, performing the following steps at each node:
//    If the current node is closer than the current best, then it becomes the current best.
//    The algorithm checks whether there could be any points on the other side of the splitting plane that are closer to the search point than the current best.
//    In concept, this is done by intersecting the splitting hyperplane with a hypersphere around the search point that has a radius equal to the current nearest distance.
//    Since the hyperplanes are all axis-aligned this is implemented as a simple comparison to see whether the distance between the splitting coordinate of the search point and current node is lesser than the distance (overall coordinates) from the search point to the current best.
//    If the hypersphere crosses the plane, there could be nearer points on the other side of the plane, so the algorithm must move down the other branch of the tree from the current node looking for closer points, following the same recursive process as the entire search.
//    If the hypersphere doesn't intersect the splitting plane, then the algorithm continues walking up the tree, and the entire branch on the other side of that node is eliminated.
//    When the algorithm finishes this process for the root node, then the search is complete.
//    Generally the algorithm uses squared distances for comparison to avoid computing square roots. Additionally, it can save computation by holding the squared current best distance in a variable for comparison.
    private KDNode<T> findNearest(KDNode<T> currentNode, T search, int depth) {
        int direction = search.getComparator(depth % 3).compare( search, currentNode.location );
        KDNode<T> next = (direction < 0) ? currentNode.left : currentNode.right;
        KDNode<T> other = (direction < 0) ? currentNode.right : currentNode.left;
        KDNode<T> best = (next == null) ? currentNode : findNearest(next, search, depth + 1); // Go to a leaf
        if ( currentNode.location.squaredDistance(search) < best.location.squaredDistance(search) ) {
            best = currentNode; // Set best as required
        } 
        if ( other != null ) {
            if ( currentNode.location.axisSquaredDistance(search, depth % 3) < best.location.squaredDistance(search) ) {
                KDNode<T> possibleBest = findNearest( other, search, depth + 1 );
                if (  possibleBest.location.squaredDistance(search) < best.location.squaredDistance(search) ) {
                    best = possibleBest;
                }
            }
        }
        return best; // Work back up
    }
}
