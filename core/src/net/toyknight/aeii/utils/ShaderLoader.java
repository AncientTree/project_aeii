package net.toyknight.aeii.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

/**
 * {@link AssetManager} for {@link ShaderProgram} instances. The shader porgram data is loaded asynchronously. The shader
 * is then created on the rendering thread, synchronously.
 *
 * @author Szymon "Veldrin" Jabłoński
 */
public class ShaderLoader extends AsynchronousAssetLoader<ShaderProgram, ShaderLoader.ShaderParameter> {

    private String vertProgram;
    private String fragProgram;

    public ShaderLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ShaderParameter parameter) {
        return null;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, ShaderParameter parameter) {
        FileHandle vertFile = Gdx.files.internal(parameter.vert_file);
        FileHandle fragFile = Gdx.files.internal(parameter.frag_file);
        vertProgram = vertFile.readString();
        fragProgram = fragFile.readString();
    }

    @Override
    public ShaderProgram loadSync(AssetManager manager, String fileName, FileHandle file, ShaderParameter parameter) {
        ShaderProgram.pedantic = false;
        return new ShaderProgram(vertProgram, fragProgram);
    }

    public static class ShaderParameter extends AssetLoaderParameters<ShaderProgram> {

        public String vert_file;
        public String frag_file;

    }

}