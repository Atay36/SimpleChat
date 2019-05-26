import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm; // 뒤에있는 hm은 실체가 있는게 아니라 메인에있는 걸 레퍼런스만 가져온거
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else if(line.equals("/userlist")) // "/userlist" 입력시 
					send_userlist(hm.keySet()); //send_userlist메소드 호출
				else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw != (PrintWriter)hm.get(id)){ // 메세지를 서버로 보낸 사람의 아이디와 서버에서 보낼 유저의 아이디가 같을경우 보내지 않음
					pw.println(msg);
					pw.flush();
				}
				
			}
		}
	} // broadcast
	public void send_userlist(Set msg){
		synchronized(hm){
			Collection collection = hm.keySet();
			Iterator iter = collection.iterator();
			
			PrintWriter pw = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠
			PrintWriter pw1 = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠

			while(iter.hasNext()){ // 모든유저의 아이디를 보내줌

				pw.println((String)iter.next());
				pw.flush();

			}

			pw1.println(hm.size() + "users here."); // 몇명이 잇는지 보내줌
			pw1.flush();
		}
	} // send_userlist
}

