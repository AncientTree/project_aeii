package net.toyknight.aeii.manager;

import net.toyknight.aeii.entity.Map;

/**
 * @author toyknight 12/29/2015.
 */
public interface MapEditorListener {

    void onModeChange(int mode);

    void onMapChange(Map map);

    void onMapSaved();

    void onError(String message);

}
