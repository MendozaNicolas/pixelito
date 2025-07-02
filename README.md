# Pixelito 🎮

## Descripción
Pixelito es un motor de juego voxel en 3D desarrollado en Java utilizando OpenGL para renderizado. El proyecto implementa un sistema de renderizado de bloques similar a Minecraft, con capacidad para generar y visualizar mundos compuestos por bloques en 3D.

## Características principales

- **Motor de renderizado 3D**: Utiliza OpenGL a través de LWJGL para renderizar mundos voxel
- **Sistema de bloques**: Implementación de diferentes tipos de bloques (tierra, piedra, aire, etc.)
- **Generación eficiente de mallas**: Algoritmo optimizado para generar mallas 3D a partir de bloques voxel
- **Cámara 3D**: Sistema de cámara para navegación en el mundo 3D
- **Shaders personalizados**: Sistema de shaders para efectos visuales
- **Ventana y eventos GLFW**: Gestión de ventanas y eventos de entrada

## Requisitos

- Java 17 o superior
- Compatibilidad con OpenGL 3.3+
- Sistemas operativos soportados: Windows, macOS, Linux

## Estructura del proyecto

```
org.pixelito
├── block          # Sistema de bloques y tipos
├── camera         # Sistema de cámara y visualización
├── graphics       # Shaders y otros componentes gráficos
├── render         # Sistema de renderizado y generación de mallas
├── window         # Gestión de ventanas con GLFW
└── Game.java      # Lógica principal del juego
```

## Tecnologías utilizadas

- **Java 17**: Lenguaje de programación principal
- **LWJGL 3.3.3**: Librería Java para acceso a OpenGL, GLFW y otras APIs
- **JOML 1.10.5**: Biblioteca de matemáticas optimizada para operaciones 3D
- **Maven**: Gestión de dependencias y construcción

## Cómo ejecutar

1. Asegúrate de tener instalado Java 17 o superior
2. Clona este repositorio
3. Compila el proyecto usando Maven:
   ```
   mvn clean package
   ```
4. Ejecuta el JAR generado:
   ```
   java -jar target/pixelito-1.0-SNAPSHOT.jar
   ```

## Detalles de implementación

### Sistema de renderizado
El motor utiliza un enfoque optimizado para la generación de mallas 3D a partir de bloques voxel, donde solo se generan caras para las superficies visibles (no se crean polígonos para caras adyacentes a otros bloques sólidos).

### Sistema de bloques
Cada bloque tiene propiedades como tipo y solidez, permitiendo definir diferentes comportamientos y apariencias.

### Shaders
Sistema de shaders basado en GLSL para efectos visuales personalizados.

## Contribuir

1. Haz fork del repositorio
2. Crea una rama para tu característica (`git checkout -b feature/amazing-feature`)
3. Realiza tus cambios y haz commit (`git commit -m 'Add some amazing feature'`)
4. Push a la rama (`git push origin feature/amazing-feature`)
5. Abre un Pull Request

## Licencia

Este proyecto está licenciado bajo los términos de la licencia MIT.

## Contacto

Si tienes preguntas o sugerencias sobre el proyecto, no dudes en abrir un issue en este repositorio.