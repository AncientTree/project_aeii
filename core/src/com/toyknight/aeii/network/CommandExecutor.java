package com.toyknight.aeii.network;

import com.toyknight.aeii.concurrent.AsyncTask;
import com.toyknight.aeii.GameContext;
import com.toyknight.aeii.entity.GameCore;

import java.util.Arrays;
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
        if (NetworkManager.isConnected() && isHost()) {
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
        NetworkManager.getListener().onAllocationUpdate();
        getContext().submitAsyncTask(new AsyncTask<Void>() {
            @Override
            public Void doTask() throws Exception {
                int[] temp = new int[4];
                Arrays.fill(temp, 0);
                NetworkManager.notifyAllocationUpdate(temp, getAllocation(), temp);
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
        return NetworkManager.getRoomSetting().game;
    }

    private int[] getAllocation() {
        return NetworkManager.getRoomSetting().allocation;
    }

    private boolean isHost() {
        return NetworkManager.getServiceID() == NetworkManager.getRoomSetting().host;
    }

}
