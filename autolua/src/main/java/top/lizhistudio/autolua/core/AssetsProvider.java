package top.lizhistudio.autolua.core;

import top.lizhistudio.autolua.annotation.RPCMethod;

public interface AssetsProvider {
    @RPCMethod
    byte[] getAssets(String uri);
}
