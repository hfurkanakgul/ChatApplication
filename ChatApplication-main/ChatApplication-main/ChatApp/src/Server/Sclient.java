/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Message.Message;
import Message.Message.messageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mesut
 */
public class Sclient {

    Server server;  //serverdaki objelere erişmek için gerekli değişken
    int id = 0; //Cliente verilen id
    Socket socket;  // Soke bilgisi
    ObjectOutputStream sOutput; //Porta yazmak için gerekli değişken
    ObjectInputStream sInput;   //Portdan gelen bilgiyi okumak için gerekli değişken
    Listen listen;  // Clienti sürekli dinlemek için gerekli olan thread
    String name = "";   //clientin adı

    public Sclient(Socket _socket, int _id, Server _server) {
        server = _server;   //Server bilgisi constructerdan alınır
        socket = _socket;   //Socket bilgisi constructerdan alınır
        id = _id;           //id bilgisi constructerdan alınır
        try {
            sOutput = new ObjectOutputStream(socket.getOutputStream()); //dinlemek için gerekli obje ilgili clientın socketinden dinlenir
            sInput = new ObjectInputStream(socket.getInputStream());    //yazmak için gerekli obje ilgili clientin socketine yazılmak üzere olusturulur
        } catch (IOException ex) {
            Logger.getLogger(Sclient.class.getName()).log(Level.SEVERE, null, ex);
        }
        listen = new Listen(this, server);  //clientin dinleme threadi oluşturulur
    }

    public void Send(Message message) { //mesja göndermek için oluşturulmuş method
        try {
            sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Sclient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

class Listen extends Thread {   //dinlemek için oluşturulmuş thread

    Sclient Client;    //dinlenecek client
    Server server;  //server objelerine erişmek için server değişkenide alınır

    //thread nesne alması için yapıcı metod
    Listen(Sclient Client, Server _server) {
        this.Client = Client;   //ilgili client atanır
        server = _server;   //ilgili server atanır
    }

    @Override
    public void run() {
        while (Client.socket.isConnected()) {   //client cağlı olduğu sürece dinlemededir
            try {
                try {
                    Message received = (Message) (Client.sInput.readObject());  //gelen mesaj message tipine dönüştürülür
                    switch (received.type) {    //gelen mesajın tipine göre ilili yapıya gider
                        case Name:  //Name tipindeki mesaj gelirse sunucuya ilk gönderilen mesaj bilgisi clientin ismi gelmiş demektir
                            Client.name = received.content.toString();  //cliente isim bilgisi atanır
                            Message clients = new Message(messageType.ConnectedClients);    // Kullanıcıa geri cevap vermek üzere mesaj oluşturulur
                            ArrayList<String> users = new ArrayList<String>();  //Diğer kullanıcıların bulunduğu bir liste oluşturulur
                            for (int i = 0; i < server.Clients.size(); i++) {   
                                users.add(server.Clients.get(i).name);  //Sunucudaki client listesinden kişiler bu listeye aktarılır
                            }
                            clients.content = users;    // mesajın contentine bu liste koyulur
                            clients.roomList = server.roomList; //mesajın roomlistinde ise bağlanabileceği özel odalar koyulur
                            
                            server.Send(clients);   //mesaj ilgili cliente gönderilir
                            
                            break;
                        case ChatGroupConnection:
                            server.Send(received);  //mesaj geldiği gibi ilgili client veya clientlere iletilir
                            break;
                        case PrivateRoomCreated:    //yeni oda oluşturulduğunda gelen mesaj tipi budur
                            server.roomList.put(received.roomName, new ArrayList<String>());    //serverda hashmape oluşturulan oda adı ve kullanıcıları koyulur
                            //clientların özel oda kısmında görünmesi için bu oda ismi tüm clientlara framelerine eklenmek üzere gönderilir
                            Message msg = new Message(Message.messageType.PrivateRoomList); 
                            msg.roomName = received.roomName;   //roomname değişkenine roomname bilgisli konur
                              server.Send(msg); //ilgili clientlere gönderilir
                            break;
                        case PrivateRoomJoin:   //özel odaya biri bağlandığında çalışır
                                ArrayList<String> tmp = server.roomList.get(received.roomName);     //sunucudan ilgili odanın userlarını getir ve geçici bir değişkene at
                                tmp.add(received.owner);    //geçici değişkene kişiyi ekle
                                server.roomList.put(received.roomName, tmp);    //sunucudaki listeye bu kişiyi de ekle
                                String[] liste = new String[server.roomList.get(received.roomName).size()]; //güncellenmiş kişileri odalarda bulunan clientlere gönder ve güncelleme yapsınlar
                                for (int i = 0; i < liste.length; i++) {
                                liste[i] = server.roomList.get(received.roomName).get(i); //gönderilecek listeye kişieleri ekle
                            }
                            Message roomInfo = new Message(Message.messageType.PrivateRoomUpdated); //güncellenmiş listenin gideceği mesaj
                            roomInfo.roomName = received.roomName;  //hangi oda
                            roomInfo.roomList = server.roomList;    
                            roomInfo.userList = server.roomList.get(received.roomName);
                            roomInfo.roomListforPrivate = liste;    //yeni kullanıcı listesi
                            server.Send(roomInfo);  //mesajı gönder

                            
                            break;
                        case Text:  //genel odada kullanılır
                            received.content = Client.name + " : " + received.content.toString();   //kullanıcıdan gelen mesajı formatla
                            server.Send(received); //mesajı herkese gönder
                            break;
                    }
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
