import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class SocketClientRunnable implements Runnable {

	private Socket pc4Socket;
	private Socket pc5Socket;
	private int pc4Port;
	private int pc5Port;
	private DataInputStream pc4InputStream;
	private DataInputStream pc5InputStream;

	private Socket socket;
	private int portNumber;
	private String serverName;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private Map<Long, Long> requestList;
	private long freshness = 0L;
	private long prevFreshness = 0L;
	private long recordTime = 0L;
	// public SocketClientRunnable(String serverName, int portNumber) {
	// pc4Port = Integer.parseInt(System.getProperty("pc4Port"));
	// pc5Port = Integer.parseInt(System.getProperty("pc5Port"));
	// pc4Socket = new Socket("pcnode4", pc4Port);
	// pc5Socket = new Socket("pcnode5", pc5Port);
	// logSocketClient = new Socket("pcnode2",portNumber);
	// output = new DataOutputStream(logSocketClient.getOutputStream());
	// input = new DataInputStream(logSocketClient.getInputStream());
	// }

	public SocketClientRunnable(String serverName, int portNumber, Map<Long, Long> requestList) {
		this.portNumber = portNumber;
		this.serverName = serverName;
		this.requestList = requestList;
		this.freshness = System.currentTimeMillis();
		this.prevFreshness = System.currentTimeMillis();
		try {
			socket = new Socket(this.serverName, this.portNumber);
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				if (inputStream.available() != 0) {
					freshness = inputStream.readLong();
					recordTime = System.currentTimeMillis();

					synchronized (requestList) {
						Iterator it = requestList.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Long, Long> request = (Map.Entry<Long, Long>) it.next();
							long staleness = 0L;
							long latency = 0L;
							boolean expired = false;

							if (freshness >= request.getKey() && recordTime <= request.getValue()) {
								latency = recordTime - request.getKey();

								Status status = new Status(staleness, latency, expired);
								StatusWriterRunnable.requestStatusQueue.put(status);
								it.remove();

							} else if (recordTime > request.getValue()) {
								latency = request.getValue() - request.getKey();
								staleness = request.getKey() - prevFreshness;
								expired = true;
								Status status = new Status(staleness, latency, expired);
								StatusWriterRunnable.requestStatusQueue.put(status);
								it.remove();
							}
						}
					}

					prevFreshness = freshness;
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
