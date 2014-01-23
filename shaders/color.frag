varying float x;
varying float y;
uniform float width;
uniform float height;
uniform sampler2D txtr;
uniform float floorc;

void main() {
	float dx = 1.0/width;
	float dy = 1.0/height;
	
	vec4 self = texture2D(txtr,vec2(x,y),0.0);	
	float floor=floorc;
	
	float r=self.z;
	float g=self.y;
	float b=self.x;
	float self_av=(self.x+self.y+self.z)/3.0;
	if (r>g*1.2 && r>b*1.2){
		gl_FragColor = vec4(0,0,1,1);
	}else if (g>r*1.2 && g>b*1.2){
		gl_FragColor = vec4(0,1,0,1);
	}else if (b>r*1.2 && b>g*1.2){
		gl_FragColor = vec4(1,0,0,1);
	}else if (1.0-self_av<self_av-floor){
		gl_FragColor = vec4(1,1,1,1);
	} else gl_FragColor=vec4(0,0,0,1);
	return;
}