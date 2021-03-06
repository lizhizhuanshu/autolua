package top.lizhistudio.autolua.rpc;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 88888888L;
    public long callID;
    public Object result;
    public Response(long callID,Object result)
    {
        this.callID = callID ;
        this.result = result;
    }
}
