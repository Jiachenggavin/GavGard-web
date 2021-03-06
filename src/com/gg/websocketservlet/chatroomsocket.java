package com.gg.websocketservlet;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.json.Json;
import javax.json.JsonReader;

import com.gg.logindata.Userdata;


import com.gg.logindata.User;

@ServerEndpoint(value="/chatroom",configurator=GetHttpSessionConfigurator.class) //This is the url we need to write in jsp/js file.
public class chatroomsocket {

    private static int onlineCount = 0;
    public static ArrayList<String> allusersname = new ArrayList<String>();
    private static CopyOnWriteArraySet<chatroomsocket> setofclient = new CopyOnWriteArraySet<chatroomsocket>();

    private Session session;
    private String nickname;
    private String name_of_user;
    private boolean isguest;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        HttpSession httpSession= (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        nickname = (String)httpSession.getAttribute("nickname");
        isguest = true;
        if (nickname != null){
          isguest = false;
        }
        this.session = session;
        setofclient.add(this);
        chatroomsocket.onlineCount++;
        //String nickname = session.getAttribute("nickname");
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (isguest) {
          name_of_user = "GUEST";
        }else {
          name_of_user = nickname;
        }
        String time = dateFormat.format(now);
        for (chatroomsocket item : setofclient) {
            item.session.getAsyncRemote().sendText(name_of_user+" is joining in!    "+time); //send json rather than text directly
        }
        allusersname.add(name_of_user);
    }

    @OnClose
    public void onClose() {
        setofclient.remove(this);
        allusersname.remove(name_of_user);
        chatroomsocket.onlineCount--;
        //for (chatroomsocket item : setofclient) {
        //    item.session.getAsyncRemote().sendText(name_of_user+" is joining in!    "+time);
        //}
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(now);
        for (chatroomsocket item : setofclient) {
                item.session.getAsyncRemote().sendText(name_of_user+": "+message +"     "+time); // send json
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("ERROR");
        error.printStackTrace();
    }
}
