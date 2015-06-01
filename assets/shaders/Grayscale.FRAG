#ifdef GL_ES
       #define LOWP lowp
       precision mediump float;
#else
       #define LOWP
#endif

varying LOWP vec4 vColor;
varying vec2 vTexCoord;
uniform sampler2D u_texture;
uniform float grayscale;

void main() {
       vec4 texColor = texture2D(u_texture, vTexCoord);
       
       float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
       texColor.rgb = mix(vec3(gray), texColor.rgb, grayscale);
       
       gl_FragColor = texColor * vColor;
}