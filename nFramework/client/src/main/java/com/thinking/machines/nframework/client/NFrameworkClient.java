package com.thinking.machines.nframework.client;

import com.thinking.machines.nframework.common.*;
import com.thinking.machines.nframework.common.exceptions.*;
import java.nio.charset.*;
import java.io.*;
import java.net.*;

public class NFrameworkClient
{
public Object execute(String servicePath,Object ...arguments) throws Throwable
{
try
{
Request requestObject=new Request();
requestObject.setServicePath(servicePath);
requestObject.setArguments(arguments);

String requestJSONString=JSONUtil.toJSON(requestObject);


byte objectBytes[]=requestJSONString.getBytes(StandardCharsets.UTF_8);
int requestLength=objectBytes.length;
byte header[]=new byte[1024];
int i;
int x;
i=1023;
x=requestLength;
while(x>0)
{
header[i]=(byte)(x%10);
x=x/10;
i--;
}

Socket socket=new Socket("localhost",5500);
OutputStream os=socket.getOutputStream();
os.write(header,0,1024);
os.flush();

byte ack[]=new byte[1];
InputStream is=socket.getInputStream();
int bytesReadCount;
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1)
{
continue;
}
break;
}

int bytesToSend=requestLength;
int chunkSize=1024;
int j=0;

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

int bytesToReceive=1024;
byte tmp[]=new byte[1024];
i=0;
j=0;
int k;
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

int responseLength=0;
j=1023;
i=1;
while(j>=0)
{
responseLength=responseLength+(header[j]*i);
j--;
i=i*10;
}

ack[0]=1;
os.write(ack,0,1);
os.flush();


byte response[]=new byte[responseLength];
bytesToReceive=responseLength;
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
response[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}

ack[0]=1;
os.write(ack);
os.flush();
socket.close();

String responseJSONString=new String(response,StandardCharsets.UTF_8);
Response responseObject=JSONUtil.fromJSON(responseJSONString,Response.class);
if(responseObject.getSuccess())
{
return responseObject.getResult();
}
else
{
throw responseObject.getException();
}
}catch(IOException e)
{
System.out.println(e);
}
return null;
}
}