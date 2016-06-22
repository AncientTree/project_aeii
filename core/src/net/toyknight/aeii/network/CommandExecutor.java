package net.toyknight.aeii.network;

import net.toyknight.aeii.concurrent.AsyncTask;
import net.toyknight.aeii.GameContext;
import net.toyknight.aeii.entity.GameCore;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author toyknight 1/11/2016.
 */
public class CommandExecutor {

    private final GameContext context;

    public CommandExecutor(GameContext context) {
        this.context = context;
    }

    public GameContext getContext() {
        return context;
    }

    public void execute(String command, int selected_id) {
        if (NetworkManager.isConnected() && getContext().getRoomManager().isHost()) {
            Scanner scanner = new Scanner(command);
            try {
                String entry = scanner.next();
                if (entry.equals("/assign")) {
                    int team = scanner.nextInt();
                    if (getGame().isTeamAlive(team)) {
                        assign(selected_id, team);
                    }
                }
            } catch (NoSuchElementException ignored) {
            }
        }
    }

    private void assign(int id, int team) {
        getAllocation()[team] = id;
        NetworkManager.getListener().onAllocationUpdate(new int[4], getAllocation(), new int[4]);
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                NetworkManager.notifyAllocationUpdate(new int[4], getAllocation(), new int[4]);
                return null;
            }

            @Override
            public void onFinish(Void result) {
            }

            @Override
            public void onFail(String message) {
            }
        });
    }

    private GameCore getGame() {
        return getContext().getRoomManager().getGame();
    }

    private int[] getAllocation() {
        return getContext().getRoomManager().getAllocations();
    }

}
