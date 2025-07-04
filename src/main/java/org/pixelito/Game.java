package org.pixelito;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.pixelito.block.Block;
import org.pixelito.block.BlockType;
import org.pixelito.camera.Camera;
import org.pixelito.graphics.ShaderProgram;
import org.pixelito.graphics.Texture;
import org.pixelito.input.KeyCode;
import org.pixelito.input.Keyboard;
import org.pixelito.input.Mouse;
import org.pixelito.render.GreedyMesher;
import org.pixelito.render.Mesh;
import org.pixelito.render.VoxelMesher;
import org.pixelito.util.PerformanceMetrics;
import org.pixelito.window.Window;

public class Game {

    private Window window;
    private Mesh mesh;
    private ShaderProgram shader;
    private Texture blockTexture;
    private boolean useGreedyMesher = true; // Toggle to compare meshing algorithms
    
    // Camera for first-person navigation
    private Camera camera;
    
    // World dimensions
    private static final int WORLD_SIZE_X = 32;
    private static final int WORLD_SIZE_Y = 8;
    private static final int WORLD_SIZE_Z = 32;

    // Mesh statistics for performance comparison
    private int vertexCount;
    private int faceCount;
    
    // Toggle for mouse capture (cursor visibility)
    private boolean mouseCaptured = true;

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
        
        // Create camera with initial position slightly above the ground
        camera = new Camera(new Vector3f(WORLD_SIZE_X / 2.0f, WORLD_SIZE_Y + 1.0f, WORLD_SIZE_Z / 2.0f));
        
        // Set appropriate movement speed for world scale
        camera.setMoveSpeed(10.0f);
        
        // Set mouse sensitivity for smoother camera control
        camera.setMouseSensitivity(0.1f);
        
        // Capture mouse by default for first-person navigation
        Mouse.setCaptured(mouseCaptured, window.getId());
        
        System.out.println("Camera initialized. Use WASD to move, mouse to look around, and ESC to toggle mouse capture.");

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
        // Create projection matrix once - doesn't need to be recreated every frame
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0f),
                (float) window.getWidth() / window.getHeight(),
                0.1f,
                1000.0f
        );

        // Track timing for frame rate control
        while (!window.shouldClose()) {
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0f;
            lastFrameTime = currentTime;
            
            // Safety cap to avoid spiral of death if game freezes temporarily
            if (deltaTime > 0.25f) deltaTime = 0.25f;
            accumulator += deltaTime;

            // Process input every frame (delta-based movement)
            processInput(deltaTime);
            
            // Update camera (handles movement and rotation)
            camera.update(deltaTime);
            
            // If mouse is captured, ensure deltas are consumed each frame
            // This prevents drift or continuous rotation
            if (mouseCaptured) {
                // Force consume any remaining mouse movement
                Mouse.getDeltaX();
                Mouse.getDeltaY();
            }

            // Fixed timestep for logic/physics updates
            while (accumulator >= FIXED_DELTA_TIME) {
                // update(FIXED_DELTA_TIME); // placeholder for future physics updates
                accumulator -= FIXED_DELTA_TIME;
            }

            // Render the scene
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            
            shader.bind();
            
            // Set uniforms for the shader
            shader.setUniform("projection", projection);
            shader.setUniform("view", camera.getViewMatrix());
            
            // Create model matrix (identity for world blocks)
            Matrix4f model = new Matrix4f();
            shader.setUniform("model", model);
            
            // Bind texture if available
            if (blockTexture != null) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockTexture.getId());
                shader.setUniform("textureSampler", 0);
                shader.setUniform("useTexture", 1);
            } else {
                shader.setUniform("useTexture", 0);
            }
            
            // Render the mesh
            mesh.render();
            
            // Unbind resources
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            shader.unbind();
            
            // Update the window (swap buffers, poll events)
            window.update();
        }
    }

    // Delta-based movement and input
    private void processInput(float deltaTime) {
        // Toggle mouse capture with Escape key
        if (Keyboard.isKeyPressed(KeyCode.ESCAPE)) {
            mouseCaptured = !mouseCaptured;
            Mouse.setCaptured(mouseCaptured, window.getId());
        }
        
        // Toggle between meshing algorithms (for testing/comparison)
        if (Keyboard.isKeyPressed(KeyCode.G)) {
            useGreedyMesher = !useGreedyMesher;
            System.out.println("Switched to " + (useGreedyMesher ? "Greedy" : "Simple") + " mesher");
            // Regenerate the mesh with the new algorithm
            regenerateMesh();
        }
        
        // Print current position with P key (for debugging)
        if (Keyboard.isKeyPressed(KeyCode.P)) {
            Vector3f pos = camera.getPosition();
            System.out.printf("Camera position: (%.2f, %.2f, %.2f)%n", pos.x, pos.y, pos.z);
        }
    }

    /**
     * Regenerates the mesh using the current meshing algorithm.
     * This is an optimized implementation that properly cleans up resources.
     */
    private void regenerateMesh() {
        // Clean up old mesh to avoid memory leaks
        if (mesh != null) {
            mesh.destroy();
        }
        
        // Create blocks world
        Block[][][] blocks = createTestWorld(WORLD_SIZE_X, WORLD_SIZE_Y, WORLD_SIZE_Z);
        
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
