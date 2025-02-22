package pers.adlered.picuang.controller.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Author: 杨捷宁
 * @DateTime: 2022/5/7 14:10
 * @Description: 该类用于 提供websocket服务
 */
@ServerEndpoint(value="/webSocketCalcResult/{sid}/{type}")
@Component

public class WebSocketCalcResult {
    /**
     * 静态变量，用来记录当前在线连接数,线程安全
     */
    private static AtomicInteger onlineNum = new AtomicInteger();

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
     */
    private static ConcurrentHashMap<String, Session> SessionPools = new ConcurrentHashMap<>();


    /**
     * 向对应客户端发送信息
     * @author 杨捷宁
     * @date 2022/5/7 19:29
     * @param session
     * @param message
     */
    public static void sendMessage(Session session, String message) throws IOException {
        if(session != null){
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
        }
    }

    public static void tryToSend(double totalPrice) {
        if(SessionPools.size()==0){
            return;
        }

        for (Session session: SessionPools.values()) {
            try {
                sendMessage(session, "");
            } catch(Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }
    /**
     * 建立连接成功调用
     * @author 杨捷宁
     * @date 2022/5/7 19:30
     * @param session
     * @param userName 用户名称,用于记录和删除session
     * @param type 需求类型
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "sid") String userName, @PathParam(value = "type") String type){

        SessionPools.put(userName, session);

        addOnlineCount();
        System.out.println("加入webSocketCalcResult！当前人数为" + onlineNum);
        try {
            //sendMessage(session, "欢迎" + userName + "加入连接！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接时调用
     * @author 杨捷宁
     * @date 2022/5/7 9:45
     */
    @OnClose
    public void onClose(@PathParam(value = "sid") String userName){

        SessionPools.remove(userName);

        subOnlineCount();
    }

    //收到客户端信息
    @OnMessage
    public void onMessage(String data, Session session, @PathParam(value = "sid") String userName, @PathParam(value = "type") String type) throws IOException{
        System.out.println(data);
        try {
            SessionPools.get("vue").getBasicRemote().sendText(data);
//            for(ConcurrentHashMap.Entry<String, Session> ce: SessionPools.entrySet()) {
//                ce.getValue().getBasicRemote().sendText(data);
//            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable){
        System.out.println("发生错误");
        throwable.printStackTrace();
    }

    public static void addOnlineCount(){
        onlineNum.incrementAndGet();
    }

    public static void subOnlineCount() {
        onlineNum.decrementAndGet();
    }

}
