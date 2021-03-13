package top.lizhistudio.autolua.rpc.message;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 666666L;
    public Object serviceID;
    public long callID;
    public String methodName;
    public Object[] params;
}
