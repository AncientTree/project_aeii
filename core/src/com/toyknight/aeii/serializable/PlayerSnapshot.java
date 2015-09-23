package com.toyknight.aeii.serializable;

import java.io.Serializable;

/**
 * @author toyknight 9/1/2015.
 */
public class PlayerSnapshot implements Serializable {

    private static final long serialVersionUID = 9012015L;

    public String service_name;

    public String username;

    public boolean is_host;

    @Override
    public String toString() {
        if (is_host) {
            return username + " *";
        } else {
            return username;
        }
    }

}
