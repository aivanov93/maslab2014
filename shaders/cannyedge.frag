varying float x;
varying float y;
uniform float width;
uniform float height;
uniform sampler2D txtr;

void main() {
	float dx = 1.0/width;
	float dy = 1.0/height;
	vec4 self = texture2D(txtr,vec2(x,y),0.0);
	
	gl_FragColor = vec4(0,0,0,1);
	
	vec4 neigh;
	float upper=0.55;
	float lower=0.25;
	float d=0.0, has_neigh;
	if (self.x>upper) {
	 d=1.0;
	} else if (self.x>lower){
		has_neigh=0.0;
		for (int i=-1; i<2; i++) {
		  for (int j=-1; j<2; j++) {
			 neigh=texture2D(txtr,vec2(x+float(i)*dx,y+ float(j)*dy),0.0);
			 if (neigh.x>upper){ has_neigh=1.0;} 	
		  }
		 
		}
		if (has_neigh==1.0){ d=0.1;}
	}
	gl_FragColor = vec4(d,d,d,1);
}