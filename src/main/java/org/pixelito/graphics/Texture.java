package org.pixelito.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Represents an OpenGL texture loaded from an image file.
 */
public class Texture {
    private final int id;
    private int width;
    private int height;

    /**
     * Creates a texture from an image file.
     *
     * @param filePath Path to the image file
     * @throws IOException If the image cannot be loaded
     */
    public Texture(String filePath) throws IOException {
        // Load the image
        BufferedImage image = ImageIO.read(new File(filePath));
        width = image.getWidth();
        height = image.getHeight();

        // Get RGBA pixel data
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        // Convert to buffer
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                // RGBA components
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                buffer.put((byte) (pixel & 0xFF));         // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();

        // Create and set up the texture
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Generate mipmaps (optional)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        // Unbind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Creates a texture from a raw byte buffer.
     *
     * @param width  Width of the texture
     * @param height Height of the texture
     * @param data   RGBA data
     */
    public Texture(int width, int height, ByteBuffer data) {
        this.width = width;
        this.height = height;

        // Create and set up the texture
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data);

        // Generate mipmaps (optional)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        // Unbind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Binds this texture to the current OpenGL context.
     */
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    /**
     * Unbinds any texture from the current OpenGL context.
     */
    public void unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Destroys this texture, freeing GPU resources.
     */
    public void destroy() {
        GL11.glDeleteTextures(id);
    }

    /**
     * Gets the OpenGL texture ID.
     *
     * @return The texture ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the width of this texture.
     *
     * @return The texture width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of this texture.
     *
     * @return The texture height
     */
    public int getHeight() {
        return height;
    }
}
