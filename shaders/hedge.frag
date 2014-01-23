varying float x;
varying float y;
uniform float width;
uniform float height;
uniform sampler2D txtr;

void main() {
	float dx = 1.0/width;
	float dy = 1.0/height;
	
	gl_FragColor = vec4(0,0,0,1);
	
	vec4 self = texture2D(txtr,vec2(x,y),0.0);
	
	vec4 top = texture2D(txtr,vec2(x,y+dy),0.0);
	vec4 bottom = texture2D(txtr,vec2(x,y-dy),0.0);
    
    vec4 GY = top-bottom;
	
	
	float ly = length(GY.xyz);
	float d = ly;
	if (d<0.45) {d=0.0;} else {d=1.0;}
	gl_FragColor = vec4(d,d,d,1);
}