/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverresponsetest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;

/**
 *
 * @author Gary
 */
public class ServerResponseTest {
     private static Socket socket;
    private static BufferedReader br;
    private static PrintWriter out;
     private static final String ip = "localhost";
    private static final int port = 5000;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try
        {
            socket = new Socket(ip,port);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(),true);
            sendGet();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    private static void sendGet() throws Exception {
 
        //String url = "http://mighty-temple-9533.herokuapp.com/get_user_info?userid=54da42c6e152070300314b69";
        String url2 = "http://localhost/register_user";
        String urlParameters  = "hi=HELLO&fname=Gary&lname=Chen&email=gary88111@gmail.com&pass=password";
        byte[] postData       = urlParameters.getBytes( Charset.forName( "UTF-8" ));
        int    postDataLength = postData.length;

        URL    url            = new URL( url2 );
        HttpURLConnection cox= (HttpURLConnection) url.openConnection();           
        cox.setDoOutput( true );
        cox.setDoInput ( true );
        cox.setInstanceFollowRedirects( false );
        cox.setRequestMethod( "POST" );
        cox.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
        cox.setRequestProperty( "charset", "utf-8");
        cox.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        cox.setUseCaches( false );
        try( DataOutputStream wr = new DataOutputStream( cox.getOutputStream())) {
           wr.write( postData );
        }
        System.out.println("\nSending 'POST' request to URL : " + url2);
 
        BufferedReader in = new BufferedReader(
                new InputStreamReader(cox.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
 
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
 
        //print result
        System.out.println(response.toString());
 
    }
}
