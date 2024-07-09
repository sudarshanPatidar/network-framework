import com.thinking.machines.nframework.server.*;
import com.thinking.machines.nframework.server.annotations.*;

@Path("/banking")
public class Bank
{
@Path("/branchName")
public String getBranchName(String city) throws BankingException
{
System.out.println("Method got called");
if(city.equals("UJJAIN"))
{
return "Freeganj";
}
if(city.equals("Mumbai"))
{
return "Colaba";
}
throw new BankingException( "No branch in that city");
}

public static void main(String gg[])
{
NFrameworkServer server=new NFrameworkServer();
server.registerClass(Bank.class);
server.start();
}
}
