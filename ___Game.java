package org.pixelito;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.pixelito.block.Block;
import org.pixelito.block.BlockType;
import org.pixelito.camera.Camera;
import org.pixelito.graphics.ShaderProgram;
import org.pixelito.graphics.Texture;
import org.pixelito.input.InputManager;
import org.pixelito.input.KeyCode;
import org.pixelito.input.Keyboard;
import org.pixelito.input.Mouse;
import org.pixelito.render.GreedyMesher;
import org.pixelito.render.Mesh;
import org.pixelito.render.VoxelMesher;
import org.pixelito.util.PerformanceMetrics;
import org.pixelito.window.Window;

public class ___Game {

    private Window window;
    private Mesh mesh;
    private ShaderProgram shader;
    private Camera camera;
    private Texture blockTexture;
    private boolean useGreedyMesher = true; // Toggle to compare meshing algorithms
    
    // Mesh statistics for performance comparison
    private int vertexCount;
    private int faceCount;

    /**
     * Sets whether to use the optimized greedy meshing algorithm.
     * 
     * @param useGreedyMesher true to use greedy meshing, false for simple meshing
     */
    public void setUseGreedyMesher(boolean useGreedyMesher) {
        this.useGreedyMesher = useGreedyMesher;
    }

    private static final float FIXED_DELTA_TIME = 1.0f / 60.0f; // 60 FPS
    private float accumulator = 0.0f;
    private long lastFrameTime;

    public void run() {
        init();
        lastFrameTime = System.nanoTime();
        loop();
        cleanup();
    }

    private void init() {
        window = new Window(1280, 720, "Pixelito", true);
        window.create();

        InputManager.setup(window.getWindowHandle());
        camera = new Camera();

        // Load shaders
        try {
            shader = new ShaderProgram(
                    "src/main/resources/shaders/block.vert",
                    "src/main/resources/shaders/block.frag"
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Generate mesh
        regenerateMesh();
        
        // Load a simple block texture atlas
        try {
            blockTexture = new Texture("src/main/resources/textures/blocks/blocks.png");
        } catch (Exception e) {
            System.err.println("Warning: Could not load texture: " + e.getMessage());
            // Continue without texture
        }
    }

    /**
     * Creates a test world with some interesting features
     */
    private Block[][][] createTestWorld(int width, int height, int depth) {
        Block[][][] blocks = new Block[width][height][depth];
        
        // Create terrain with some height variation
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Generate terrain height using a simple sine wave pattern
                int terrainHeight = 2 + (int)(Math.sin(x * 0.3) * 1.5 + Math.cos(z * 0.3) * 1.5);
                
                // Fill blocks below terrain height
                for (int y = 0; y < height; y++) {
                    if (y < terrainHeight - 1) {
                        blocks[x][y][z] = new Block(BlockType.STONE);
                    } else if (y < terrainHeight) {
                        blocks[x][y][z] = new Block(BlockType.DIRT);
                    } else if (y == terrainHeight) {
                        blocks[x][y][z] = new Block(BlockType.GRASS);
                    } else {
                        // Air (null or transparent)
                    }
                }
            }
        }
        
        return blocks;
    }

    private void loop() {
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0f),
                (float) window.getWidth() / window.getHeight(),
                0.1f,
                1000.0f
        );

        while (!window.shouldClose()) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
            lastFrameTime = currentTime;
            if (deltaTime > 0.25f) deltaTime = 0.25f; // avoid spiral of death
            accumulator += deltaTime;

            // Process input every frame (delta-based movement)
            processInput(deltaTime);

            // Fixed timestep for logic/physics (if needed)
            while (accumulator >= FIXED_DELTA_TIME) {
                // update(FIXED_DELTA_TIME); // placeholder for future logic
                accumulator -= FIXED_DELTA_TIME;
            }

            // Render
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            shader.bind();
            
            // Set uniforms
            shader.setUniform("projection", projection);
            shader.setUniform("view", camera.getViewMatrix());
            
            // Bind texture if available
            if (blockTexture != null) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockTexture.getId());
                GL20.glUniform1i(GL20.glGetUniformLocation(shader.getId(), "textureSampler"), 0);
                GL20.glUniform1i(GL20.glGetUniformLocation(shader.getId(), "useTexture"), 1);
            } else {
                GL20.glUniform1i(GL20.glGetUniformLocation(shader.getId(), "useTexture"), 0);
            }
            
            // Render the mesh
            mesh.render();
            
            // Unbind resources
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            shader.unbind();
            
            window.update();
            
            // Update input state at the end of each frame
            InputManager.update();
        }
    }

    // Delta-based movement and input
    private void processInput(float deltaTime) {
        float speed = 5.0f * deltaTime; // units per second
        float yaw = (float) Math.toRadians(camera.getRotation().y);
        float dx = (float) Math.sin(yaw);
        float dz = (float) Math.cos(yaw);
        Vector3f pos = camera.getPosition();

        // Forward/backward
        if (Keyboard.isKeyDown(KeyCode.W)) {
            pos.x += dx * speed;
            pos.z -= dz * speed;
        }
        if (Keyboard.isKeyDown(KeyCode.S)) {
            pos.x -= dx * speed;
            pos.z += dz * speed;
        }
        // Strafe
        if (Keyboard.isKeyDown(KeyCode.A)) {
            pos.x -= dz * speed;
            pos.z -= dx * speed;
        }
        if (Keyboard.isKeyDown(KeyCode.D)) {
            pos.x += dz * speed;
            pos.z += dx * speed;
        }
        // Up/down
        if (Keyboard.isKeyDown(KeyCode.SPACE)) {
            pos.y += speed;
        }
        if (Keyboard.isKeyDown(KeyCode.LEFT_SHIFT)) {
            pos.y -= speed;
        }
        if (Keyboard.isKeyDown(KeyCode.ESCAPE)) {
            GLFW.glfwSetInputMode(window.getWindowHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
        // Mouse look
        camera.getRotation().y += (float) Mouse.getDeltaX() * 0.1f;
        camera.getRotation().x += (float) Mouse.getDeltaY() * 0.1f;
        camera.getRotation().x = Math.max(-90, Math.min(90, camera.getRotation().x));
        
        // Toggle between meshing algorithms (for testing/comparison)
        if (Keyboard.isKeyPressed(KeyCode.G)) {
            useGreedyMesher = !useGreedyMesher;
            System.out.println("Switched to " + (useGreedyMesher ? "Greedy" : "Simple") + " mesher");
            // Regenerate the mesh with the new algorithm
            regenerateMesh();
        }
    }
    
    /**
     * Regenerates the mesh using the current meshing algorithm.
     */
    private void regenerateMesh() {
        // Clean up old mesh
        if (mesh != null) {
            mesh.destroy();
        }
        
        // Create blocks
        Block[][][] blocks = createTestWorld(32, 8, 32);
        
        // Start performance measurement
        PerformanceMetrics.startMeasurement();
        
        // Generate mesh using either GreedyMesher or VoxelMesher
        VoxelMesher.MeshData data;
        String mesherType;
        
        if (useGreedyMesher) {
            mesherType = "Greedy Mesher (optimized)";
            data = GreedyMesher.generateMesh(blocks);
        } else {
            mesherType = "Simple Mesher (unoptimized)";
            data = VoxelMesher.generateMesh(blocks);
        }
        
        // Create new mesh with the generated data
        mesh = new Mesh(data.vertices, data.texCoords, data.indices);
        
        // Calculate mesh statistics
        vertexCount = data.vertices.length / 3;
        faceCount = data.indices.length / 6;
        
        // Stop performance measurement and report
        String additionalInfo = "Vertices: " + vertexCount + ", Faces: " + faceCount;
        String metrics = PerformanceMetrics.stopMeasurement("Mesh Generation (" + mesherType + ")", additionalInfo);
        System.out.println(metrics);
    }

    private void cleanup() {
        if (blockTexture != null) {
            blockTexture.destroy();
        }
        shader.destroy();
        mesh.destroy();
        window.destroy();
    }
}
