package top.lizhistudio.autolua.rpc;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 666666L;
    public String serviceName;
    public long callID;
    public String methodName;
    public Object[] params;
}
