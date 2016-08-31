package net.toyknight.aeii.server;

import com.esotericsoftware.minlog.Log;

/**
 * @author toyknight 8/12/2016.
 */
public class ServerLauncher {

    public static void main(String[] args) {
        try {
            switch (args.length) {
                case 0:
                    new ServerContext().start();
                    break;
                case 1:
                    if (args[0].equals("-index")) {
                        new ServerContext().index();
                        System.exit(-1);
                    }
                    break;
            }
        } catch (ServerException ex) {
            Log.error(ex.getTag(), "Failed launching the server", ex);
            System.exit(-1);
        }
    }

}
