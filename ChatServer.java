import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;



public class ChatServer {


	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			long time = System.currentTimeMillis(); 
			SimpleDateFormat format1 = new SimpleDateFormat ("HH:mm:ss");
			String date = format1.format (System.currentTimeMillis());
			System.out.println("[" + date + "] " + "Waiting connection...");
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
	public static ArrayList<String> spam = new ArrayList<String>();

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
			long time = System.currentTimeMillis(); 
			SimpleDateFormat format1 = new SimpleDateFormat ("HH:mm:ss");
			String date = format1.format (System.currentTimeMillis());

			broadcast("[" + date + "] " + id + " entered.");
			System.out.println("[" + date + "] "+"[Server] User (" + id + ") entered.");
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

			//ArrayList<String> spam = new ArrayList<String>();
			// spam.add("fuck");
			// spam.add("shit");

			while((line = br.readLine()) != null){
				long time = System.currentTimeMillis(); 
				SimpleDateFormat format1 = new SimpleDateFormat ("HH:mm:ss");
				String date = format1.format (System.currentTimeMillis());
				Boolean Y = false; // badword의 유무확인
				//String[] badwords = {"fuck", "sibal", "shit", "bitch", "dick"};
				
				for(String test : spam){ // badword를 하나씩확인
					if(line.contains(test)){ // 입력받은 메시지에 badword가 포함되어있으면 
						usespam("[" + date + "] " + "You use bad word! Don't use " + test);
						//System.out.println("You use bad word! Don't use " + test); //경고메세지 출력하고
						Y = true; // badword의 유무를 true로 바꿈
						break;
					}
				}
				if(Y) continue; 
				if(line.equals("/quit"))
					break;
				if(line.equals("/spamlist")){
					spamlist(spam,date);
				}
				else if(line.indexOf("/addspam ") == 0){
					String spamword = line.split(" ")[1];
					//System.out.println(spamword);
					spam.add(spamword);
				}
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}
				else if(line.equals("/userlist")) // "/userlist" 입력시 
					send_userlist("[" + date + "] "); //send_userlist메소드 호출
				else
					broadcast("[" + date + "] "+id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			long time = System.currentTimeMillis(); 
			SimpleDateFormat format1 = new SimpleDateFormat ("HH:mm:ss");
			String date0 = format1.format (System.currentTimeMillis());
			broadcast("["+ date0 + "] " + id + " exited.");
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
				long time = System.currentTimeMillis(); 
				SimpleDateFormat format1 = new SimpleDateFormat ("HH:mm:ss");
				String date = format1.format (System.currentTimeMillis());

				PrintWriter pw = (PrintWriter)obj;
				pw.println("[" + date + "] "+id + " whisphered. : " + msg2);
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
					// long time = System.currentTimeMillis(); 
					// SimpleDateFormat format1 = new SimpleDateFormat ("HH:mm:ss");
					// String date = format1.format (System.currentTimeMillis());

					pw.println(msg);
					pw.flush();
				}
				
			}
		}
	} // broadcast

	public void spamlist(ArrayList<String> msg,String date){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();

			PrintWriter pw = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠
			PrintWriter pw1 = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠

			//while(iter.hasNext()){

				//if(pw == (PrintWriter)hm.get(id)){ 
					pw1.println("["+date+"]"+"------spamlist-------");
					pw1.flush();
					pw.println(msg);
					pw.flush();
				//}
				
			//}
		}
	} // spamlist

	public void usespam(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(pw == (PrintWriter)hm.get(id)){ 
					pw.println(msg);
					pw.flush();
				}
				
			}
		}
	}
		public void send_userlist(String time){ 
		synchronized(hm){
			Collection collection = hm.keySet();
			Iterator iter = collection.iterator();
			
			PrintWriter pw0 = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠
			PrintWriter pw = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠
			PrintWriter pw1 = (PrintWriter)hm.get(id); //userlist를 요청한 id의 pw를 넣어둠

			pw0.println(time+"------userlist-------"); // 몇명이 있는지 보내줌
			pw0.flush();

			while(iter.hasNext()){ // 모든유저의 아이디를 보내줌

				pw.println((String)iter.next());
				pw.flush(); // 반복문밖에서 한번만 해도 됨!

			}

			pw1.println(hm.size() + "users here." ); // 몇명이 잇는지 보내줌
			pw1.flush();
		}
	} // send_userlist
}

