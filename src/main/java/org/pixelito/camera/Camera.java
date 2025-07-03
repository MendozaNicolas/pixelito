package org.pixelito.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.pixelito.input.KeyCode;
import org.pixelito.input.Keyboard;
import org.pixelito.input.Mouse;

/**
 * Camera class that handles 3D perspective and movement.
 * Implements WASD movement and mouse look controls.
 */
public class Camera {
    // Camera position
    private final Vector3f position;
    
    // Camera orientation
    private float yaw = -90.0f;   // Horizontal rotation (start looking down -Z)
    private float pitch = 0.0f;   // Vertical rotation
    
    // Camera vectors
    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);
    private final Vector3f worldUp = new Vector3f(0, 1, 0);
    
    // Movement speed and mouse sensitivity
    private float moveSpeed = 5.0f;
    private float mouseSensitivity = 0.1f;
    
    // View matrix cache
    private final Matrix4f viewMatrix = new Matrix4f();
    private boolean viewMatrixDirty = true;
    
    /**
     * Creates a camera at the specified position
     * @param position Initial camera position
     */
    public Camera(Vector3f position) {
        this.position = new Vector3f(position);
        updateCameraVectors();
    }
    
    /**
     * Creates a camera at the default position
     */
    public Camera() {
        this(new Vector3f(0, 3, 0));
    }
    
    /**
     * Updates camera based on keyboard and mouse input
     * @param deltaTime Time elapsed since last frame in seconds
     */
    public void update(float deltaTime) {
        // Process keyboard input for WASD movement
        processKeyboard(deltaTime);
        
        // Process mouse movement for camera rotation
        processMouse();
    }
    
    /**
     * Handle keyboard input for camera movement
     */
    private void processKeyboard(float deltaTime) {
        float velocity = moveSpeed * deltaTime;
        
        if (Keyboard.isKeyDown(KeyCode.W)) {
            position.add(new Vector3f(front).mul(velocity));
            viewMatrixDirty = true;
        }
        
        if (Keyboard.isKeyDown(KeyCode.S)) {
            position.sub(new Vector3f(front).mul(velocity));
            viewMatrixDirty = true;
        }
        
        if (Keyboard.isKeyDown(KeyCode.A)) {
            position.sub(new Vector3f(right).mul(velocity));
            viewMatrixDirty = true;
        }
        
        if (Keyboard.isKeyDown(KeyCode.D)) {
            position.add(new Vector3f(right).mul(velocity));
            viewMatrixDirty = true;
        }
        
        if (Keyboard.isKeyDown(KeyCode.SPACE)) {
            position.add(new Vector3f(up).mul(velocity));
            viewMatrixDirty = true;
        }
        
        if (Keyboard.isKeyDown(KeyCode.LEFT_SHIFT)) {
            position.sub(new Vector3f(up).mul(velocity));
            viewMatrixDirty = true;
        }
    }
    
    /**
     * Handle mouse input for camera rotation
     */
    private void processMouse() {
        // Only process mouse when it's captured
        if (!Mouse.isCaptured()) {
            return;
        }
        
        double xOffset = Mouse.getDeltaX();
        double yOffset = Mouse.getDeltaY();
        
        // Skip if there's no movement or if movement is too large (which might indicate a glitch)
        if (xOffset == 0 && yOffset == 0) {
            return;
        }
        
        // Uncomment this block to ignore large mouse movements
        // if (Math.abs(xOffset) > 100 || Math.abs(yOffset) > 100) {
        //     System.out.println("Large mouse movement detected and ignored: " + xOffset + ", " + yOffset);
        //     return;
        // }
        
        // Apply sensitivity
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;
        
        // Update camera angles
        yaw += xOffset;
        pitch += yOffset;
        
        // Constrain pitch to avoid flipping
        if (pitch > 89.0f) {
            pitch = 89.0f;
        } else if (pitch < -89.0f) {
            pitch = -89.0f;
        }
        
        // Update camera vectors
        updateCameraVectors();
        viewMatrixDirty = true;
    }
    
    /**
     * Updates the camera's orientation vectors based on yaw and pitch
     */
    private void updateCameraVectors() {
        // Calculate new front vector from Euler angles
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        
        front.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        front.y = (float) Math.sin(pitchRad);
        front.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        front.normalize();
        
        // Recalculate right and up vectors
        right.set(front).cross(worldUp).normalize();
        up.set(right).cross(front).normalize();
    }
    
    /**
     * Gets the view matrix for this camera
     * @return The view matrix
     */
    public Matrix4f getViewMatrix() {
        if (viewMatrixDirty) {
            Vector3f target = new Vector3f(position).add(front);
            viewMatrix.identity().lookAt(position, target, up);
            viewMatrixDirty = false;
        }
        return viewMatrix;
    }
    
    /**
     * Sets the camera's position
     * @param position New position
     */
    public void setPosition(Vector3f position) {
        this.position.set(position);
        viewMatrixDirty = true;
    }
    
    /**
     * Gets the camera's position
     * @return Current position
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    
    /**
     * Gets the camera's front vector (direction it's looking)
     * @return Front vector
     */
    public Vector3f getFront() {
        return new Vector3f(front);
    }
    
    /**
     * Sets the camera's movement speed
     * @param speed New movement speed
     */
    public void setMoveSpeed(float speed) {
        this.moveSpeed = speed;
    }
    
    /**
     * Sets the mouse sensitivity
     * @param sensitivity New mouse sensitivity
     */
    public void setMouseSensitivity(float sensitivity) {
        this.mouseSensitivity = sensitivity;
    }
}
