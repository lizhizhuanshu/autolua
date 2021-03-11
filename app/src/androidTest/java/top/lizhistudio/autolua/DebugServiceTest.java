package top.lizhistudio.autolua;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import top.lizhistudio.app.thrift.DebuggerService;
import top.lizhistudio.app.thrift.ProjectInfo;

import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class DebugServiceTest
{
    @Test
    public void testGetInfo() throws Exception
    {
        TTransport tTransport = new TFramedTransport(new TSocket("localhost",7777));
        TProtocol tProtocol = new TBinaryProtocol(tTransport);
        DebuggerService.Client client = new DebuggerService.Client(tProtocol);
        ProjectInfo projectInfo = client.getInfo("test");
        assert projectInfo == null;

    }
}
