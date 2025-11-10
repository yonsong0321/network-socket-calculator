import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class CalcServerEx {
    public static String calc(String exp) {
        StringTokenizer st = new StringTokenizer(exp, " ");
        if (st.countTokens() != 4)
            return "400 ERROR\r\nTYPE: INVALID_FORMAT\r\nDESC: invalid request format\r\n\r\n";

        String command = st.nextToken();
        String opcode = st.nextToken();
        double op1 = 0, op2 = 0;

        try {
            op1 = Double.parseDouble(st.nextToken());
            op2 = Double.parseDouble(st.nextToken());
        } catch (NumberFormatException e) {
            return "400 ERROR\r\nTYPE: INVALID_NUMBER\r\nDESC: operands must be numeric\r\n\r\n";
        }

        if (!command.equalsIgnoreCase("CALC"))
            return "400 ERROR\r\nTYPE: UNKNOWN_CMD\r\nDESC: unknown command\r\n\r\n";

        String res = "";
        switch (opcode.toUpperCase()) {
            case "ADD":
                res = Double.toString(op1 + op2);
                break;
            case "SUB":
                res = Double.toString(op1 - op2);
                break;
            case "MUL":
                res = Double.toString(op1 * op2);
                break;
            case "DIV":
                if (op2 == 0)
                    return "400 ERROR\r\nTYPE: DIV_BY_ZERO\r\nDESC: divided by zero\r\n\r\n";
                res = Double.toString(op1 / op2);
                break;
            default:
                return "400 ERROR\r\nTYPE: UNKNOWN_OP\r\nDESC: unsupported operation\r\n\r\n";
        }
        return "200 OK\r\nRESULT: " + res + "\r\n\r\n";
    }

    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        ServerSocket listener = null;
        Socket socket = null;

        // ThreadPool 생성 (최대 10개 스레드)
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try {
            listener = new ServerSocket(9999); // 서버 소켓 생성
            System.out.println("연결을 기다리고 있습니다.....");
            while (true) {
                socket = listener.accept(); // 클라이언트로부터 연결 요청 대기
                System.out.println("연결되었습니다.");
                pool.execute(new ClientHandler(socket)); // ThreadPool을 통해 작업 실행
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (socket != null)
                    socket.close(); // 통신용 소켓 닫기
                if (listener != null)
                    listener.close(); // 서버 소켓 닫기
            } catch (IOException e) {
                System.out.println("클라이언트와 채팅 중 오류가 발생했습니다.");
            }
            // 서버 종료 시 ThreadPool 정리
            pool.shutdown();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                String inputMessage = in.readLine();
                if (inputMessage == null)
                    break;
                if (inputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("클라이언트에서 연결을 종료하였음");
                    out.write("200 OK\r\nRESULT: Disconnected\r\n\r\n");
                    out.flush();
                    break; // "bye"를 받으면 연결 종료
                }
                System.out.println(inputMessage); // 받은 메시지를 화면에 출력
                String res = CalcServerEx.calc(inputMessage); // 계산. 계산 결과는 res
                out.write(res); // 계산 결과 문자열 전송
                out.flush();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (socket != null)
                    socket.close(); // 통신용 소켓 닫기
            } catch (IOException e) {
                System.out.println("클라이언트와 채팅 중 오류가 발생했습니다.");
            }
        }
    }
}
