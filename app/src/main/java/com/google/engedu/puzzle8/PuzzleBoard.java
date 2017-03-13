/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;
    private PuzzleBoard previousBoard;
    private int steps;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        steps=0;
        previousBoard=null;
        Bitmap bmp = Bitmap.createScaledBitmap(bitmap,parentWidth,parentWidth,true);
        tiles = new ArrayList<>();
        int size,xSplice=0,ySplice=0;
        size = parentWidth/NUM_TILES;
        for(int i=1; i<= NUM_TILES*NUM_TILES; i++){
            //index 9 is the null tile
            //divide the bitmap into NUM_TILES^2 -1 pieces and set the last piece to null
            PuzzleTile p;
            if(ySplice ==NUM_TILES-1 && xSplice == NUM_TILES-1){
                p = null;
            }
            else{
                p = new PuzzleTile(Bitmap.createBitmap(bmp,xSplice*size,ySplice*size,size,size),i);
//                Log.i("Board Initialisation","Non-null index : "+i);
            }
            xSplice++;
            if(xSplice==NUM_TILES){
                xSplice = 0;
                ySplice++;
            }
            tiles.add(p);
        }
    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        steps = otherBoard.steps+1;
        previousBoard = otherBoard;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i+1)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> possibilities = new ArrayList<>();
        int nullTileIndex =9,x=0,y=0;
        for(int i=0; i<tiles.size(); i++){
            if(tiles.get(i)==null){
                nullTileIndex = i;
                break;
            }
            x++;
            if(x==NUM_TILES){
                x=0;
                y++;
            }
        }

        for(int i=0; i<NEIGHBOUR_COORDS.length; i++){
            int xN = x+NEIGHBOUR_COORDS[i][0];
            int yN = y+NEIGHBOUR_COORDS[i][1];
            if(0<=xN && xN<NUM_TILES && 0<=yN && yN<NUM_TILES){
                //valid neighbour. Will generate one possibility of board position
                PuzzleBoard p = new PuzzleBoard(this);
                //swap empty tile with neighbour tile
                int neighbourIndex = XYtoIndex(xN,yN);
                p.tiles.set(nullTileIndex,p.tiles.get(neighbourIndex));
                p.tiles.set(neighbourIndex,null);
                possibilities.add(p);
            }
        }

        return possibilities;
    }

    public int priority() {
        return 0;
    }

}
