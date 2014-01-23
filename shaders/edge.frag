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
	
	vec4 n0 = texture2D(txtr,vec2(x-dx,y-dy),0.0);
	vec4 n1 = texture2D(txtr,vec2(x,y-dy),0.0);
	vec4 n2 = texture2D(txtr,vec2(x+dx,y-dy),0.0);
	
	vec4 n3 = texture2D(txtr,vec2(x-dx,y),0.0);
	vec4 n4 = texture2D(txtr,vec2(x+dx,y),0.0);
	
	vec4 n5 = texture2D(txtr,vec2(x-dx,y+dy),0.0);
	vec4 n6 = texture2D(txtr,vec2(x,y+dy),0.0);
	vec4 n7 = texture2D(txtr,vec2(x+dx,y+dy),0.0);
	
	vec4 GX = -1.0*n0+1.0*n2 -2.0*n3+2.0*n4 -1.0*n5+1.0*n7;
	vec4 GY = -1.0*n0+1.0*n5 -2.0*n1+2.0*n6 -1.0*n2+1.0*n7;
	
	float lx = length(GX.xyz);
	float ly = length(GY.xyz);
	float d = ly;
	
	if (d<0.5) {d=0.0;} else {d=1.0;}
	gl_FragColor = vec4(d,d,d,1);
}