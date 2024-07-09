import com.thinking.machines.nframework.client.*;

public class BankUI
{
public static void main(String gg[])
{
NFrameworkClient client=new NFrameworkClient();
try
{
String branchName=(String)client.execute("/banking/branchName",gg[0]);
System.out.println(branchName);
}catch(Throwable t)
{
System.out.println(t.getMessage()+" : ye he message");
}


}
}