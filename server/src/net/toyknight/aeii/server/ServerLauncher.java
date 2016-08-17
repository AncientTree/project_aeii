package net.toyknight.aeii.server;

import com.esotericsoftware.minlog.Log;

/**
 * @author toyknight 8/12/2016.
 */
public class ServerLauncher {

    public static void main(String[] args) {
        try {
            new ServerContext().start();
        } catch (ServerException ex) {
            Log.error(ex.getTag(), "Failed launching the server", ex);
            System.exit(-1);
        }
    }

}
