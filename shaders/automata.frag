varying float x;
varying float y;
uniform float width;
uniform float height;
uniform sampler2D txtr;

/*
    Any live cell with fewer than two live neighbours dies, as if caused by under-population.
    Any live cell with two or three live neighbours lives on to the next generation.
    Any live cell with more than three live neighbours dies, as if by overcrowding.
    Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
*/

void main() {
	float dx = 1.0/width;
	float dy = 1.0/height;
	
	int self = texture2D(txtr,vec2(x,y),0.0).x>0.5?1:0;
	
}