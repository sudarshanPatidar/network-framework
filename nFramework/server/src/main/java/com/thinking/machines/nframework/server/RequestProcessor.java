package com.thinking.machines.nframework.server;

import java.io.*;
import java.nio.charset.*;
import java.lang.reflect.*;
import com.thinking.machines.nframework.common.*;
import java.net.*;

class RequestProcessor extends Thread
{
private NFrameworkServer server;
private Socket socket;

RequestProcessor(NFrameworkServer server,Socket socket)
{
this.server=server;
this.socket=socket;
start();
}


public void run()
{
try{
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
int bytesToReceive=1024;
byte header[]=new byte[1024];
byte tmp[]=new byte[1024];
int i,j,bytesReadCount,k;
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1)
{
continue;
}
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

int requestLength=0;
j=1023;
i=1;
while(j>=0)
{
requestLength=requestLength+(header[j]*i);
j--;
i=i*10;
}
byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();

byte request[]=new byte[requestLength];
bytesToReceive=requestLength;
i=0;
j=0;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1)
{
continue;
}
for(k=0;k<bytesReadCount;k++)
{
request[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

String requestJSONString=new String(request,StandardCharsets.UTF_8);
Request requestObject=JSONUtil.fromJSON(requestJSONString,Request.class);
String servicePath=requestObject.getServicePath();
TCPService tcpService=this.server.getTCPService(servicePath);
Response responseObject=new Response();
if(tcpService==null)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Invalid Path : "+servicePath));
}
else
{
Class c=tcpService.c;
Method method=tcpService.method;
try
{
Object serviceObject=c.newInstance();
Object result=method.invoke(serviceObject,requestObject.getArguments());
responseObject.setSuccess(true);
responseObject.setResult(result);
responseObject.setException(null);
}catch(InstantiationException instantiationException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create the object of the service class associated with the given path : "+servicePath));
}catch(IllegalAccessException illegalAccessException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create the object of the service class associated with the given path : "+servicePath));
}catch(InvocationTargetException invocationTargetException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
Throwable t=invocationTargetException.getCause();
responseObject.setException(t);
}
}
String responseJSONString=JSONUtil.toJSON(responseObject);
byte objectBytes[]=responseJSONString.getBytes(StandardCharsets.UTF_8);
int responseLength=objectBytes.length;
int x;
i=1023;
x=responseLength;
header=new byte[1024];
while(x>0)
{
header[i]=(byte)(x%10);
x=x/10;
i--;
}
os.write(header,0,1024);
os.flush();
System.out.println("Response header sent : "+responseLength);

while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1)
{
continue;
}
break;
}

System.out.println("Acknowledgement received");
int bytesToSend=responseLength;
int chunkSize=1024;
j=0;

while(j<bytesToSend)
{
if((bytesToSend-j)<chunkSize)
{
chunkSize=bytesToSend-j;
}
os.write(objectBytes,j,chunkSize);
os.flush();
j=j+chunkSize;
}
System.out.println("Response sent");
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1)
{
continue;
}
break;
}
System.out.println("Acknowledgement received");
socket.close();

}catch(IOException ioException)
{
System.out.println(ioException);
}
}
}