package com.toyknight.aeii.net;

import java.io.Serializable;

/**
 * @author toyknight 10/27/2015.
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 10272015L;

    private final long id;

    private Object[] params;

    public Response() {
        this(-1);
    }

    public Response(long id) {
        this.id = id;
    }

    public long getRequestID() {
        return id;
    }

    public void setParameters(Object... params) {
        this.params = params;
    }

    public Object getParameter(int index) {
        return params[index];
    }

}
