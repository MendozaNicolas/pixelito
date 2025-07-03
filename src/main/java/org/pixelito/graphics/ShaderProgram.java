package org.pixelito.graphics;


import org.lwjgl.opengl.GL20;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;


public class ShaderProgram {
    private final int programId;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    public ShaderProgram(String vertexPath, String fragmentPath) throws IOException {
        int vertexShader = loadShader(vertexPath, GL20.GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShader);
        GL20.glAttachShader(programId, fragmentShader);
        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            throw new RuntimeException("Error linking shader: " + GL20.glGetProgramInfoLog(programId));
        }

        GL20.glDetachShader(programId, vertexShader);
        GL20.glDetachShader(programId, fragmentShader);
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    public int getUniformLocation(String name) {
        if (uniformLocations.containsKey(name)) {
            return uniformLocations.get(name);
        }
        int location = GL20.glGetUniformLocation(programId, name);
        uniformLocations.put(name, location);
        return location;
    }

    public void setUniform(String name, Matrix4f value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        GL20.glUniformMatrix4fv(getUniformLocation(name), false, buffer);
    }
    
    /**
     * Sets an integer uniform in the shader.
     * 
     * @param name The name of the uniform
     * @param value The integer value to set
     */
    public void setUniform(String name, int value) {
        GL20.glUniform1i(getUniformLocation(name), value);
    }
    
    /**
     * Sets a float uniform in the shader.
     * 
     * @param name The name of the uniform
     * @param value The float value to set
     */
    public void setUniform(String name, float value) {
        GL20.glUniform1f(getUniformLocation(name), value);
    }

    private int loadShader(String path, int type) throws IOException {
        String source = Files.readString(Paths.get(path));
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            throw new RuntimeException("Error compiling shader " + path + ": " + GL20.glGetShaderInfoLog(shader));
        }

        return shader;
    }


    public void bind() {
        GL20.glUseProgram(programId);
    }


    public void unbind() {
        GL20.glUseProgram(0);
    }


    public void destroy() {
        GL20.glDeleteProgram(programId);
    }

    public int getId() {
        return programId;
    }
}
