package io.github.vultr.utils.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL33.glGetShaderi;
import static org.lwjgl.opengl.GL33.glGenBuffers;
import static org.lwjgl.opengl.GL33.glBindBuffer;
import static org.lwjgl.opengl.GL33.glBufferData;
import static org.lwjgl.opengl.GL33.glUseProgram;
import static org.lwjgl.opengl.GL33.glLinkProgram;
import static org.lwjgl.opengl.GL33.glGetProgrami;
import static org.lwjgl.opengl.GL33.glCreateShader;
import static org.lwjgl.opengl.GL33.glShaderSource;
import static org.lwjgl.opengl.GL33.glAttachShader;
import static org.lwjgl.opengl.GL33.glDrawElements;
import static org.lwjgl.opengl.GL33.glCreateProgram;
import static org.lwjgl.opengl.GL33.glCompileShader;
import static org.lwjgl.opengl.GL33.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glGenVertexArrays;
import static org.lwjgl.opengl.GL33.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL33.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL33.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL33.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL33.GL_FALSE;
import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.GL_TRIANGLES;
import static org.lwjgl.opengl.GL33.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL33.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL33.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL33.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL33.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL33.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL33.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL33.GL_ELEMENT_ARRAY_BUFFER;

import io.github.vultr.core.exceptions.FailedShaderException;
import io.github.vultr.utils.files.FileReader;
import lombok.Getter;

public class Shader {

    private int programId;
    private int vertexId;
    private int fragmentId;
    private int vaoId;
    private int vboId;
    private int eboId;

    private String vertexShader;
    private String fragmentShader;

    private String vertexFilePath;
    private String fragmentFilePath;

    @Getter
    private boolean shaderCompiled;

    private float[] vertexArray = {
            // positions // colors
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // bottom right
            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // top left
            0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, // top right
            -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // bottom left
    };

    // reveresed order
    private int[] elementArray = {
            2, 1, 0, // top right triangle
            0, 1, 3, // bottom left triangle
    };

    public Shader(String vertexFilePath, String fragmentFilePath) {
        this.vertexFilePath = vertexFilePath;
        this.fragmentFilePath = fragmentFilePath;
        vertexShader = String.join("\r\n", FileReader.readLines(vertexFilePath));
        fragmentShader = String.join("\r\n", FileReader.readLines(fragmentFilePath));
    }

    public void compileShader() {
        // load vertex shader
        vertexId = glCreateShader(GL_VERTEX_SHADER);

        // pass vertex shader source to the GPU
        glShaderSource(vertexId, vertexShader);

        // compile vertex shader
        glCompileShader(vertexId);

        // check for errors in compilation
        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);

            String errorMessage = "Shader error: Vertex shader compilation failed: "
                    + vertexFilePath
                    + "\n\t"
                    + glGetShaderInfoLog(vertexId, len);

            throw new FailedShaderException(errorMessage);
        }

        // load fragment shader
        fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        // pass vertex shader source to the GPU
        glShaderSource(fragmentId, fragmentShader);

        // compile vertex shader
        glCompileShader(fragmentId);

        // check for errors in compilation
        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);

            String errorMessage = "Shader error: Fragment shader compilation failed: "
                    + fragmentFilePath
                    + "\n\t"
                    + glGetShaderInfoLog(fragmentId, len);

            throw new FailedShaderException(errorMessage);
        }

        // create shader program
        programId = glCreateProgram();

        // attach shaders
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);

        // link program
        glLinkProgram(programId);

        success = glGetProgrami(programId, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(programId, GL_INFO_LOG_LENGTH);

            String errorMessage = "Shader error: Shader program linking failed: "
                    + "\n\t"
                    + glGetProgramInfoLog(programId, len);

            throw new FailedShaderException(errorMessage);
        }

        // generate buffer objects

        // generate vao
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // create vertice float buffer
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // generate vbo
        vboId = glGenBuffers();

        // upload vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        // generate ebo
        eboId = glGenBuffers();

        // upload element buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // add vertex attribute pointers
        int posSize = 3; // x, y, z
        int colorSize = 4; // r, g, b, a
        int floatSizeBytes = 4; // 4 bytes per float
        int vertexSizeBytes = (posSize + colorSize) * floatSizeBytes;
        glVertexAttribPointer(0, posSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, posSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

        this.shaderCompiled = true;
    }

    /**
     * Bind the shader
     */
    public void bindShader() {
        if (!shaderCompiled) {
            return;
        }
        // bind shader program
        glUseProgram(programId);

        // bind vao
        glBindVertexArray(vaoId);

        // enable pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // draw
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);
    }

    /**
     * Unbind the shader
     */
    public void unbindShader() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        glUseProgram(0);
    }

    /**
     * Will draw the shader to the screen
     * After drawing the shader, the shader will be unbound
     */
    public void drawShader() {
        bindShader();
        unbindShader();
    }

}
