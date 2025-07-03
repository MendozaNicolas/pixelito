#version 330 core

in vec2 fragTexCoord;

uniform sampler2D textureSampler;
uniform bool useTexture = false;

out vec4 fragColor;

void main() {
    if (useTexture) {
        fragColor = texture(textureSampler, fragTexCoord);
        
        // Discard fully transparent pixels
        if (fragColor.a < 0.1) {
            discard;
        }
    } else {
        // Fallback to a color based on texture coordinates
        fragColor = vec4(0.6, 0.4, 0.2, 1.0); // marrón tipo dirt
    }
}