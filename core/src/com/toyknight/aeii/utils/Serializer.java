package com.toyknight.aeii.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author toyknight 11/3/2015.
 */
public class Serializer {

    private final Kryo kryo;

    public Serializer() {
        kryo = new Kryo();
        new ClassRegister().register(kryo);
    }

    public void writeObject(Output output, Object object) throws KryoException {
        kryo.writeObject(output, object);
    }

    public <T> T readObject(Input input, Class<T> type) throws KryoException {
        return kryo.readObject(input, type);
    }

}
