/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Message.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author furkan
 */
public class Server {

    public ServerSocket serverSocket;                                           //Kullanılacak soket, port ve dinleme threadleri oluşturulması
    public int Port = 0;                                                        
    public ServerListen listen;                                                 
    public int clientSayi = 0;                                                  //client sayısı ve onların tutulacağı dizinin tanımlanması ve odaların içindeki
    public ArrayList<Sclient> clients = new ArrayList<>();                      // kullanıcıların listesi
    public HashMap<String, ArrayList<String>> odaListe = new HashMap<String, ArrayList<String>>();  
    
    public Server(int port) {
        try {
            Port = port;                                                        //port bilgisi atanır ve socketler oluşturulur, threadlar atanr
            serverSocket = new ServerSocket(Port);                              
            listen = new ServerListen(this);                                    
            listen.start();                                                     //thread başlatılarak clientler dinlenilmeye başlanır
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Send(Message mesaj) {                                           //mesaj göndermek için 
        for (Sclient client : clients) {                                        //clientler for dönügüsünde dönülür, null değer ise herkese mesajı yollar
            try {                                                               // değilse gerekli kişilere yollar
                if (mesaj.userList == null) { 
                    client.sOutput.writeObject(mesaj);
                } else if (mesaj.userList.contains(client.Client_name)) { 
                    client.sOutput.writeObject(mesaj);                          //mesajı gönder
                    client.sOutput.flush();
                }
                             
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}

class ServerListen extends Thread {                                             //sunucuların bağlanmak isteyen clientları karşılayacağı thread 

    Server server_;                                                     

    public ServerListen(Server s) {
        server_ = s;                                                            //Serverdan değişken gelerek serverdaki değişkenlere erişilir
    }

    @Override
    public void run() {
        while (!server_.serverSocket.isClosed()) {                              //server kapanana kadar dinlenilir, gelen client kabul edilir
            try {
                Socket cSocket = server_.serverSocket.accept();                 
                Sclient client = new Sclient(cSocket, server_.clientSayi, server_); //gelen clientleri ayrı ayrı dinlemek için farklı clientlar oluşturulur
                server_.clientSayi++;   
                server_.clients.add(client); 
                client._listen.start();                                             //client dinlenmeye başlanır

            } catch (IOException ex) {
                Logger.getLogger(ServerListen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}