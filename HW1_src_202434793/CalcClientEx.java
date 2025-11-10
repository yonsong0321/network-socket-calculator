import java.io.*;
import java.net.*;
import java.util.*;

public class CalcClientEx {
    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);

        String serverIP = "localhost";
        int port = 9999;

        // server_info.dat 파일에서 서버 정보 읽기
        File configFile = new File("server_info.dat");
        if (configFile.exists()) {
            try (BufferedReader confReader = new BufferedReader(new FileReader(configFile))) {
                String ipLine = confReader.readLine();
                String portLine = confReader.readLine();
                if (ipLine != null && !ipLine.trim().isEmpty())
                    serverIP = ipLine.trim();
                if (portLine != null && !portLine.trim().isEmpty())
                    port = Integer.parseInt(portLine.trim());
                System.out.println("설정 파일(server_info.dat)에서 서버 정보를 읽었습니다: " + serverIP + ":" + port);
            } catch (Exception e) {
                System.out.println("설정 파일 읽기 오류. 기본 설정을 사용합니다.");
            }
        } else {
            System.out.println("server_info.dat 파일이 없어 기본 설정을 사용합니다 (" + serverIP + ":" + port + ").");
        }

        try {
            socket = new Socket(serverIP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) {
                System.out.print("계산식(빈칸으로 띄어 입력,예: CALC ADD 24 42)>>"); // 프롬프트
                String outputMessage = scanner.nextLine(); // 키보드에서 수식 읽기
                if (outputMessage.equalsIgnoreCase("bye")) {
                    out.write(outputMessage + "\n"); // "bye" 문자열 전송
                    out.flush();
                    break; // 사용자가 "bye"를 입력한 경우 서버로 전송 후 연결 종료
                }
                out.write(outputMessage + "\n"); // 키보드에서 읽은 수식 문자열 전송
                out.flush();
                String inputMessage;
                while ((inputMessage = in.readLine()) != null) {
                    if (inputMessage.equals("")) break; // 빈 줄 만나면 응답 끝
                    System.out.println(inputMessage);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null)
                    socket.close(); // 클라이언트 소켓 닫기
            } catch (IOException e) {
                System.out.println("서버와 채팅 중 오류가 발생했습니다.");
            }
        }
    }
}
