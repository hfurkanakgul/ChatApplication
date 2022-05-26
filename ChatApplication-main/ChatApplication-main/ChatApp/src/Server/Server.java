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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mesut
 */
public class Server {

    public ServerSocket ss; //Kullanılacak soket
    public int port = 0;    //Kullanılacak port
    public ServerListen listenThread;   //Sunucuyu meşgul etmemek için oluşturulan dinleme threadi
    public int clientCount = 0; //client sayısı
    public ArrayList<Sclient> Clients = new ArrayList<>(); //clientlerin tutulacağı dizi
    public HashMap<String, ArrayList<String>> roomList = new HashMap<String, ArrayList<String>>();  //Grup konuşmaları için odalar ve içindeki kullanıcıların listesi
    
    public Server(int _port) {
        try {
            port = _port;   //Kullanıcıdan alınan port bilgisi atanır
            ss = new ServerSocket(port);     //Bu portla yeni bir soket oluşturulur
            listenThread = new ServerListen(this);  //Sunucuyu meşgul etmemek için oluşturulan thread atanır.
            listenThread.start();   //thread baslatılarak sunucu bağlanacak clientleri dinlemeye başlar
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Send(Message msg) { //clientlere mesaj göndermek için kullanılan fonksiyon
        for (Sclient c : Clients) {    //tüm clientler dönülür
            try {
                if (msg.userList == null) { //userlist null ise herkese gönderecek
                    c.sOutput.writeObject(msg);
                } else if (msg.userList.contains(c.name)) { //değilse sadece listedeki kişilere gönder
                    c.sOutput.writeObject(msg); //mesajı gönder
                    c.sOutput.flush();
                }
                             
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}

class ServerListen extends Thread { //sunucuların bağlanmak isteyen clientları karşılayacağı thread classı

    Server server;  //Server classını tutmak için gerekli değişken

    public ServerListen(Server _s) {
        server = _s;    //constructordan gelen değer buradaki servera atılır. Bu sayede server classındaki objelere erişilir
    }

    @Override
    public void run() {
        while (!server.ss.isClosed()) { //server kapanana kadar dinle
            try {
                Socket clientSocket = server.ss.accept();   //client geldiğinde kabul eet
                //Bağlanan clientların her birini ayrı ayrı dinlemek gerektiği için her cliente bir thread atanır
                Sclient client = new Sclient(clientSocket, server.clientCount, server);
                server.clientCount++;   //client sayısı arttırılır
                server.Clients.add(client); //bağlanan client listeye eklenir
                client.listen.start();  //client dinlenmeye başlanır

            } catch (IOException ex) {
                Logger.getLogger(ServerListen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
