import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Created by HW on 1/25/17.
 */
public class JmxClient {

    private JMXConnector jmxCon;
    private MBeanServerConnection con;
    private List<AttributeMapList> attributeMapListList = new ArrayList<AttributeMapList>();
    private String serverName = "cobisServer";

    public static void main(String[] args) throws Exception {
        JmxClient jmxClient = new JmxClient();
        jmxClient.obtainJmxAttributes();
        jmxClient.showAllAtributes();


    }

    private void showAllAtributes(){
        for(AttributeMapList attributeMapList:attributeMapListList){
            System.out.println(attributeMapList.getName()+ " : " +attributeMapList.getAttributeList());

        }
    }



    private void obtainJmxAttributes() {
        try {
            initConnection("127.0.0.1", "7001", "weblogic", "weblogic0");
            processAttributes("com.bea:Name="+serverName+",Location=" + serverName + ",Type=OverloadProtection,Server="+serverName+",*", new String[]{"Name"}, serverName);
            processAttributes("com.bea:Name="+serverName+",*", new String[]{"HeapSizeCurrent"}, serverName);
            processAttributes("com.bea:Name="+serverName+",Type=JVMRuntime,*", new String[]{"OSName","OSVersion"}, serverName);
            processAttributes("com.bea:Name=CSP_BDD,Location=" + serverName + ",*", new String[]{"MinCapacity","MaxCapacity","InactiveConnectionTimeoutSeconds","ConnectionReserveTimeoutSeconds"}, serverName);
            processAttributes("com.bea:Name=CTS_BDD_MF,Location=" + serverName + ",*", new String[]{"MinCapacity","MaxCapacity","InactiveConnectionTimeoutSeconds","ConnectionReserveTimeoutSeconds"}, serverName);
            processAttributes("com.bea:Name=CTS_SQL_BDD_MF,Location=" + serverName + ",*", new String[]{"MinCapacity","MaxCapacity","InactiveConnectionTimeoutSeconds","ConnectionReserveTimeoutSeconds"}, serverName);

//            processAttributes(adminClient, "WebSphere:type=Server,*", new String[]{"name", "nodeName"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:type=JVM,*", new String[]{"heapSize"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:j2eeType=J2EEServer,*", new String[]{"platformName", "platformVersion"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:name=WMQJCAResourceAdapter,*", new String[]{"minimumSize", "maximumSize", "inactivityTimeout"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:name=WebContainer,*", new String[]{"minimumSize", "maximumSize", "inactivityTimeout"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:name=CSP_BDD,*", new String[]{"minConnections", "maxConnections", "unusedTimeout", "connectionTimeout"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:name=CTS_BDD_MF,*", new String[]{"minConnections", "maxConnections", "unusedTimeout", "connectionTimeout"}, attributeMapList);
//            processAttributes(adminClient, "WebSphere:name=CTS_SQL_BDD_MF,*", new String[]{"minConnections", "maxConnections", "unusedTimeout", "connectionTimeout"}, attributeMapList);

//            listAllAttributes();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {

            if (jmxCon != null)
                try {
                    jmxCon.close();
                } catch (Exception e) {
                    //no se puede cerrar la conextion
                    e.printStackTrace();
                }
        }
    }

    private void processAttributes(String query, String[] attrNames, String serverName) throws Exception {
        ObjectName objectName = new ObjectName(query);
        Set<ObjectName> mbeans = con.queryNames(objectName, null);
        for (ObjectName mbeanName : mbeans) {
            AttributeList attributeList = null;
            try {
                attributeList = con.getAttributes(mbeanName, attrNames);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            if (attributeList != null && !attributeList.isEmpty()) {
                AttributeMapList attributeMapList = new AttributeMapList();
                attributeMapList.setName(query);
                attributeMapList.setServerName(serverName);
                attributeMapList.setAttributeList(attributeList.asList());
                attributeMapListList.add(attributeMapList);
            }
        }
    }




    private void initConnection(String hostname, String port, String user, String password) throws Exception {
        JMXServiceURL serviceUrl =
                new JMXServiceURL(
                        "service:jmx:iiop://" + hostname + ":" + port + "/jndi/weblogic.management.mbeanservers.domainruntime");
        System.out.println("Connecting to: " + serviceUrl);
        Hashtable env = new Hashtable();
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
                "weblogic.management.remote");
        env.put(javax.naming.Context.SECURITY_PRINCIPAL, user);
        env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
        jmxCon = JMXConnectorFactory.newJMXConnector(serviceUrl, env);
        jmxCon.connect();
        con = jmxCon.getMBeanServerConnection();

    }


    private void listAllAttributes() throws Exception {
        Set<ObjectName> mbeans = con.queryNames(null, null);
        StringBuilder val=new StringBuilder();
        for (ObjectName mbeanName : mbeans) {
            WriteAttributes(con, mbeanName,val);
        }
        writeToFile(val.toString());
    }


    private void searchAllAttributes() throws Exception {
        Set<ObjectName> mbeans = con.queryNames(null, null);
        String val="MAC";
        for (ObjectName mbeanName : mbeans) {
            verifyAttributes(con, mbeanName, val);
        }
        writeToFile(val);
    }

    private String obtainAttributesValue(ObjectName objectName, String name) {
        String result = null;
        try {
            Object value = con.getAttribute(objectName, name);
            result = value.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private  void WriteAttributes(final MBeanServerConnection mBeanServer, final ObjectName http,StringBuilder val)
            throws Exception {
        MBeanInfo info = mBeanServer.getMBeanInfo(http);
        MBeanAttributeInfo[] attrInfo = info.getAttributes();
        val.append("ObjectName: "+http+" \n ");
        //System.out.println("Attributes for object: " + http + ":\n");
        for (MBeanAttributeInfo attr : attrInfo) {
            //  System.out.println("  " + attr.getName() + " value: "+obtainAttributesValue(http,attr.getName())+"\n");
            val.append("  "+ attr.getName() + " value: "+obtainAttributesValue(http,attr.getName())+" \n ");
        }

    }

    private  void verifyAttributes(final MBeanServerConnection mBeanServer, final ObjectName http,String search)
            throws Exception {
        MBeanInfo info = mBeanServer.getMBeanInfo(http);
        MBeanAttributeInfo[] attrInfo = info.getAttributes();

        for (MBeanAttributeInfo attr : attrInfo) {
            String value = obtainAttributesValue(http,attr.getName());
            if(value != null && value.contains(search)){
                System.out.println("Attributes for object: " + http + ":\n");
                System.out.println("  " + attr.getName() + " value: "+obtainAttributesValue(http,attr.getName())+"\n");
            }
        }

    }

    private void writeToFile(String text) throws  Exception{
        BufferedWriter output = null;
        try {
            File file = new File("/Users/User/Downloads/example.txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write(text);
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                output.close();
            }
        }
    }

}

