package org.pixelito.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position;
    private final Vector3f rotation;

    public Camera() {
        position = new Vector3f(0, 20, 40);
        rotation = new Vector3f(0, 0, 0);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0))
                .translate(-position.x, -position.y, -position.z);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getForwardVector() {
        float yawRad = (float) Math.toRadians(rotation.y);
        float pitchRad = (float) Math.toRadians(rotation.x);

        return new Vector3f(
                (float) (-Math.sin(yawRad)),
                0,
                (float) (-Math.cos(yawRad))
        ).normalize();
    }

    public Vector3f getRightVector() {
        float yawRad = (float) Math.toRadians(rotation.y - 90);
        return new Vector3f(
                (float) (-Math.sin(yawRad)),
                0,
                (float) (-Math.cos(yawRad))
        ).normalize();
    }
}
