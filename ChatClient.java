import java.net.*;
import java.io.*;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			sock = new Socket(args[1], 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			// send username.
			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock, br);
			it.start();
			String line = null;
			String[] badwords = {"fuck", "sibal", "shit", "bitch", "dick"}; // list of badwords
			while((line = keyboard.readLine()) != null){
				Boolean Y = false; // badword의 유무확인

				for(String test : badwords){ // badword를 하나씩확인
					if(line.contains(test)){ // 입력받은 메시지에 badword가 포함되어있으면 
						System.out.println("You use bad word! Don't use " + test); //경고메세지 출력하고
						Y = true; // badword의 유무를 true로 바꿈
						break;
					}
				}
				if(Y) continue; // badword가 있을 경우 서버로 보내지 않음.
				pw.println(line);
				pw.flush();
				if(line.equals("/quit")){
					endflag = true;
					break;
				}
			}
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line);
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // InputThread
}

